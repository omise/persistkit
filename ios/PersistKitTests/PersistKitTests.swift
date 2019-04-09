import XCTest

import PersistKit

class PersistKitTests: XCTestCase {
  
  private let todos = (1...20).map({
    Todo(title: "Todo \($0)", detail: "Todo Item number \($0)", dueDate: Date())
  })
  
  var database: Database!
  
  override func setUp() {
    guard let dir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first else {
      fatalError("could not access the filesystem")
    }
    
    let dbPath = URL(fileURLWithPath: dir).appendingPathComponent("mydb.sqlite3").absoluteString
    
    guard let driver = SQLite3Driver(filename: dbPath) else {
      fatalError("failed to initialize SQLite3Driver for Database")
    }
    
    database = Database(driver: driver)
  }
  
  override func tearDown() {
    database = nil
    guard let dir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first else {
      fatalError("could not access the filesystem")
    }
    let dbPath = URL(fileURLWithPath: dir).appendingPathComponent("mydb.sqlite3")
    try! FileManager.default.removeItem(at: dbPath)
  }
  
  func testSaveData() {
    
  }
}


private struct Todo : Identifiable {
  let id: String = UUID().uuidString
  var title: String
  var detail: String?
  var dueDate: Date?
  
  var identifier: String {
    return id
  }
}
