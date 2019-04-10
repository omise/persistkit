import Foundation
import SQLite3

public protocol Driver {
  func query(_ command: Command) throws -> [Record]
  func execute(_ command: Command) throws -> Int
}
