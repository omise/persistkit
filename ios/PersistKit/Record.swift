import Foundation

public struct Record {
    typealias Encoder = PropertyListEncoder
    typealias Decoder = PropertyListDecoder

    let identifier: String
    let kind: String
    let flags: UInt32
    let content: Data

    init(identifier: String, kind: String, flags: UInt32, content: Data) {
        self.identifier = identifier
        self.kind = kind
        self.flags = flags
        self.content = content
    }
}
