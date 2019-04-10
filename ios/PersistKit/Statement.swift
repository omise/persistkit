import Foundation
import SQLite3


protocol Statement {
    var sql: String { get }
    
    func bindTo(statement: OpaquePointer?) -> Bool
    
    func processRecords(_ records: [Record]) -> [Record]
}

extension Statement {
    func processRecords(_ records: [Record]) -> [Record] {
        return records
    }
}

struct CreateStatement: Statement {
    
    let sql: String = """
        CREATE TABLE IF NOT EXISTS records (
            identifier TEXT PRIMARY KEY UNIQUE NOT NULL,
            kind       TEXT NOT NULL,
            flags      INTEGER NOT NULL,
            content    BLOB NOT NULL
        );

        CREATE INDEX records_identifier_pkey ON records (identifier);
        """
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        return true
    }
}

struct UpsertStatement: Statement {
    
    let sql: String = """
        INSERT INTO records (identifier, kind, flags, content)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(identifier) DO
            UPDATE SET kind = excluded.kind,
                flags = excluded.flags,
                content = excluded.content;
        """
    
    let record: Record
    init(_ record: Record) {
        self.record = record
    }
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        guard record.identifier.withCString({ (ptr) -> Int32 in
            sqlite3_bind_text(statement, 1, ptr, -1, SQLite3Driver.transiantDestructor)
        }) == SQLITE_OK else { return false }
        guard record.kind.withCString({ (ptr) -> Int32 in
            sqlite3_bind_text(statement, 2, ptr, -1, SQLite3Driver.transiantDestructor)
        }) == SQLITE_OK else { return false }
        guard sqlite3_bind_int(statement, 3, Int32(record.flags)) == SQLITE_OK else { return false }
        guard record.content.withUnsafeBytes({ (bytes: UnsafeRawBufferPointer) -> Int32 in
            sqlite3_bind_blob(statement, 4, UnsafeRawPointer(bytes.baseAddress), Int32(self.record.content.count), SQLite3Driver.transiantDestructor)
        }) == SQLITE_OK else { return false }
        return true
    }
}

struct DeleteIdentifierStatement: Statement {
    
    let sql: String = """
        DELETE FROM records WHERE identifier = ?;
        """
    
    let identifier: String
    init (_ identifier: String) {
        self.identifier = identifier
    }
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        return identifier.withCString({ (ptr) -> Int32 in
            sqlite3_bind_text(statement, 1, ptr, -1, SQLite3Driver.transiantDestructor)
        }) == SQLITE_OK
    }
}

struct SelectIdentifierStatement: Statement {
    
    let sql: String = """
        SELECT * FROM records WHERE identifier = ?;
        """
    
    let identifier: String
    init (_ identifier: String) {
        self.identifier = identifier
    }
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        return identifier.withCString({ (ptr) -> Int32 in
            sqlite3_bind_text(statement, 1, ptr, -1, SQLite3Driver.transiantDestructor)
        }) == SQLITE_OK
    }
}

struct SelectIdentifiersStatement: Statement {
    let sql: String
    
    let identifiers: [String]
    init (_ identifiers: [String]) {
        let inQuery = Array(repeating: "?", count: identifiers.count)
        self.sql = "SELECT * FROM records WHERE identifier IN (\(inQuery.joined(separator: ",")));"
        self.identifiers = identifiers
    }
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        for (index, identifier) in identifiers.enumerated() {
            guard identifier.withCString({ (ptr) -> Int32 in
                sqlite3_bind_text(statement, Int32(index + 1), ptr, -1, SQLite3Driver.transiantDestructor)
            }) == SQLITE_OK else {
                return false
            }
        }
        return true
    }
    
    func processRecords(_ records: [Record]) -> [Record] {
        return records.sorted(by: {
            (identifiers.firstIndex(of: $0.identifier) ?? Int.max) < (identifiers.firstIndex(of: $1.identifier) ?? Int.max)
        })
    }
}

struct SelectAllStatement: Statement {
    
    let sql: String = """
        SELECT * FROM records;
        """
    
    func bindTo(statement: OpaquePointer?) -> Bool {
        return true
    }
}
