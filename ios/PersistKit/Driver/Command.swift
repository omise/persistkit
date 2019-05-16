import Foundation

public enum Command {
  case loadAll
  case load(_ identifier: String)
  case loadWithIDs(_ identifiers: [String])
  case save(_ record: Record)
  case delete(_ identifier: String)
  case clearDatabase
}

