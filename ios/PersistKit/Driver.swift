import Foundation
import SQLite3

final class Driver {
    public enum Result {
        case completed
        case completedWithRecords(records: [Record])
        case failed(message: String)
    }

    public enum Mode {
        case ignoreResult
        case extractRows
    }

    let filename: String
    let db: OpaquePointer

    init?(filename: String) {
        var dbout: OpaquePointer? = nil
        guard sqlite3_open(filename, &dbout) == SQLITE_OK else { return nil }
        guard let dbptr = dbout else { return nil }

        self.filename = filename
        self.db = dbptr
    }

    deinit {
        sqlite3_close(db)
    }

    func execute(_ stmt: Statement, mode: Mode) -> Result {
        let name = String(describing: type(of: stmt))

        var statement: OpaquePointer? = nil
        guard sqlite3_prepare_v2(self.db, stmt.sql, -1, &statement, nil) == SQLITE_OK else {
            return formatError(self.db, def: "Statement preparation failure")
        }

        print("statement prepared: \(name)")
        defer {
            sqlite3_finalize(statement)
            print("statement finalized: \(name)")
        }

        guard stmt.bindTo(statement: statement) else {
            return formatError(statement, def: "Statement parameter binding failure")
        }
        print("statement bound: \(name)")

        var records: [Record]? = []
        while true {
            switch sqlite3_step(statement) {
            case SQLITE_DONE:
                if let records = records {
                    return .completedWithRecords(records: records)
                } else {
                    return .completed
                }
            case SQLITE_ROW:
                switch mode {
                case .ignoreResult:
                    continue
                case .extractRows:
                    records = records ?? []
                    records?.append(extractRecord(statement))
                }
            default: // not _ROW or _DONE means we have an issue
                return formatError(statement, def: "Statement execution failure")
            }
        }
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

    func formatError(_ ptr: OpaquePointer?, def defaultMessage: String) -> Result {
        guard let ptr = ptr else { return .failed(message: defaultMessage) }

        if let errmsgptr = sqlite3_errmsg(ptr) {
            return .failed(message: String(cString: errmsgptr))
        } else {
            return .failed(message: defaultMessage)
        }
    }
}
