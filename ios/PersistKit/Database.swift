import Foundation

public final class Database {
  let driver: Driver
  
  public init(driver: Driver) {
    self.driver = driver
  }
  
  public func loadAll<T: Recordable>() throws -> [T] {
    let records = try driver.query(.loadAll)
    let recordablesOpt = try? records.map { (record) -> T in try T.decode(from: record) }
    guard let recordables = recordablesOpt else {
      throw DatabaseError.cannotConvertToRecordable
    }
    return recordables
  }
  
  public func load<T: Recordable>(_ identifier: String) throws -> T? {
    let records = try driver.query(.load(identifier))
    guard let record = records.first else {
      return nil
    }
    guard let recordable = try? T.decode(from: record) else {
      throw DatabaseError.cannotConvertToRecordable
    }
    return recordable
  }
  
  public func load<T: Recordable>(identifiers: [String]) throws -> [T] {
    let records = try driver.query(Command.loadWithIDs(identifiers))
    return try records.map({
      do {
        return try T.decode(from: $0)
      } catch _ {
        throw DatabaseError.cannotConvertToRecordable
      }
    })
  }
  
  public func save<T: Recordable>(_ obj: T) throws {
    guard let record = try? Record(from: obj) else {
      throw DatabaseError.cannotConvertToRecord
    }
    try _ = driver.execute(.save(record))
  }
  
  public func delete(_ identifier: String) throws -> Bool {
    let count = try driver.execute(.delete(identifier))
    guard 0...1 ~= count else {
      fatalError("invalid count: expected: 0...1 actual: \(count)")
    }
    return count == 1
  }
}
