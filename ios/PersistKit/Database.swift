import Foundation

public final class Database {
    let driver: Driver

    public init?(filename: String) {
        guard let driver = Driver(filename: filename) else {
            return nil
        }

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
