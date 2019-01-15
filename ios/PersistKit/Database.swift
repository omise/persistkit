import Foundation

public final class Database {
    let driver: Driver

    public init?(filename: String) {
        guard let driver = Driver(filename: filename) else {
            return nil
        }

        self.driver = driver
    }

    public func ensureInitialized() {
        switch driver.execute(CreateStatement(), mode: .ignoreResult) {
        case .completed, .completedWithRecords(records: _):
            break
        case .failed(let message):
            fatalError("failed to initialize database: \(message)")
        }
    }

    public func loadAll<T: Recordable>() throws -> [T] {
        switch driver.execute(SelectAllStatement(), mode: .extractRows) {
        case .completed:
            return []
        case .completedWithRecords(let records):
            return try records.map { (rec) -> T in try T.decode(from: rec) }
        case .failed(let message):
            fatalError("failed to load objects from database: \(message)")
        }
    }

    public func load<T: Recordable>(_ identifier: String) throws -> T? {
        switch driver.execute(SelectIdentifierStatement(identifier), mode: .extractRows) {
        case .completed:
            return nil
        case .completedWithRecords(let records):
            guard let record = records.first else {
                return nil
            }
            return try T.decode(from: record)
        case .failed(let message):
            fatalError("failed to load object from database: \(message)")
        }
    }

    public func save<T: Recordable>(_ obj: T) {
        let stmt = UpsertStatement(try! Record(from: obj))
        switch driver.execute(stmt, mode: .ignoreResult) {
        case .completed, .completedWithRecords(records: _):
            break
        case .failed(let message):
            fatalError("failed to save object to database: \(message)")
        }
    }
}
