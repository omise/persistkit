import Foundation
import SQLite3
import Dispatch


public class SQLite3Driver: Driver {
  
  private static let queueKey = DispatchSpecificKey<SQLite3Driver>()

  static let transiantDestructor = unsafeBitCast(-1, to: sqlite3_destructor_type.self)
  
  let fileURL: URL
  let db: OpaquePointer
  let queue: DispatchQueue
  
  public init?(databaseFileURL: URL) {
    queue = DispatchQueue(label: "persistkit.sqlite3-driver.operating-queue")
    
    var dbout: OpaquePointer? = nil
    guard databaseFileURL.isFileURL &&
      sqlite3_open(databaseFileURL.absoluteString, &dbout) == SQLITE_OK else { return nil }
    guard let dbptr = dbout else { return nil }
    
    self.fileURL = databaseFileURL
    self.db = dbptr
    
    queue.setSpecific(key: SQLite3Driver.queueKey, value: self)
    ensureInitialized()
  }
  
  deinit {
    sqlite3_close(db)
  }
  
  private func ensureInitialized() {
    try! _ = execute(CreateStatement())
  }
  
  public func query(_ command: Command) throws -> [Record] {
    let workItem: () throws -> [Record] = {
      let stmt = try self.newStatementFrom(command: command)
      return try self.query(stmt)
    }
    
    if DispatchQueue.getSpecific(key: SQLite3Driver.queueKey) === self {
      return try workItem()
    } else {
      return try queue.sync(execute: workItem)
    }
  }
  
  func query(_ stmt: Statement) throws -> [Record] {
    let statement = try prepareSQLiteStatement(stmt: stmt)
    defer {
      sqlite3_finalize(statement)
    }
    
    var records: [Record] = []
    while true {
      switch sqlite3_step(statement) {
      case SQLITE_DONE:
        return stmt.processRecords(records.map(decode(_:)))
      case SQLITE_ROW:
        records.append(extractRecord(statement))
      default: // not _ROW or _DONE means we have an issue
        throw formatError(statement, def: "Statement execution failure")
      }
    }
  }
  
  public func execute(_ command: Command) throws -> Int {
    let workItem: () throws -> Int = {
      let stmt = try self.newStatementFrom(command: command)
      return try self.execute(stmt)
    }
    
    if DispatchQueue.getSpecific(key: SQLite3Driver.queueKey) === self {
      return try workItem()
    } else {
      return try queue.sync(execute: workItem)
    }
  }
  
  func execute(_ stmt: Statement) throws -> Int {
    let statement = try prepareSQLiteStatement(stmt: stmt)
    defer {
      sqlite3_finalize(statement)
    }
    
    while true {
      switch sqlite3_step(statement) {
      case SQLITE_DONE:
        return countEffectedRows(self.db)
      case SQLITE_ROW:
        continue
      default: // not _ROW or _DONE means we have an issue
        throw formatError(statement, def: "Statement execution failure")
      }
    }
  }
  
  private func prepareSQLiteStatement(stmt: Statement) throws -> OpaquePointer? {
    var statement: OpaquePointer? = nil
    guard sqlite3_prepare_v2(self.db, stmt.sql, -1, &statement, nil) == SQLITE_OK else {
      throw formatError(self.db, def: "Statement preparation failure")
    }
    
    
    guard stmt.bindTo(statement: statement) else {
      throw formatError(statement, def: "Statement parameter binding failure")
    }
    return statement
  }
  
  func countEffectedRows(_ ptr: OpaquePointer?) -> Int {
    return Int(sqlite3_changes(ptr))
  }
  
  func extractRecord(_ ptr: OpaquePointer?) -> Record {
    guard let identifier = sqlite3_column_text(ptr, 0) else {
      fatalError("database column 0 must be a non-empty identifier")
    }
    
    guard let kind = sqlite3_column_text(ptr, 1) else {
      fatalError("database column 1 must be the record's kind")
    }
    
    let flags = sqlite3_column_int(ptr, 2)
    guard let content = sqlite3_column_blob(ptr, 3) else {
      fatalError("database column 2 must be the record's content")
    }
    
    let contentLen = sqlite3_column_bytes(ptr, 3)
    
    return Record(
      identifier: String(cString: identifier),
      kind: String(cString: kind),
      flags: UInt32(bitPattern: flags),
      content: Data(bytes: content, count: Int(contentLen))
    )
  }
  
  func formatError(_ ptr: OpaquePointer?, def defaultMessage: String) -> DatabaseError {
    guard let ptr = ptr else { return .driverError(reason: defaultMessage) }
    
    if let errmsgptr = sqlite3_errmsg(ptr) {
      return .driverError(reason: String(cString: errmsgptr))
    } else {
      return .driverError(reason: defaultMessage)
    }
  }
  
  func newStatementFrom(command: Command) throws -> Statement {
    switch command {
    case .loadAll:
      return SelectAllStatement()
    case .load(let identifier):
      return SelectIdentifierStatement(identifier)
    case .loadWithIDs(let ideitifiers):
      return SelectIdentifiersStatement(ideitifiers)
    case .save(let record):
      return UpsertStatement(encode(record))
    case .delete(let identifier):
      return DeleteIdentifierStatement(identifier)
    case .clearDatabase:
      return DeleteAllStatement()
    }
  }
  
  public func deleteDatabase() throws {
    let workItem = {
      if #available(iOS 8.2, *) {
        sqlite3_close_v2(self.db)
      } else {
        sqlite3_close(self.db)
      }
      try FileManager.default.removeItem(at: self.fileURL)
    }
    
    if DispatchQueue.getSpecific(key: SQLite3Driver.queueKey) === self {
      try workItem()
    } else {
      try queue.sync(execute: workItem)
    }
  }
  
  func encode(_ record: Record) -> Record {
    return record
  }
  
  func decode(_ record: Record) -> Record {
    return record
  }
}

