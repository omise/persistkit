import Foundation

public protocol Recordable: Codable, Identifiable {
}

public extension Recordable {
    static func decode(from record: Record) throws -> Self {
        let decoder = Record.Decoder()
        return try decoder.decode(Self.self, from: record.content)
    }
}

public extension Record {
    init<ObjType: Recordable>(from obj: ObjType) throws {
        self.identifier = obj.identifier
        self.kind = String(describing: type(of: obj))
        self.flags = 0
        self.content = try Record.Encoder().encode(obj)
    }
}
