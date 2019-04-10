import XCTest

import PersistKit

class PersistKitTests: XCTestCase {
  
  private let todos = (1...20).map({
    Todo(title: "Todo \($0)", detail: "Todo Item number \($0)", dueDate: Date())
  })
  
  var database: Database!
  
  var databaseFileURL: URL = {
    var directory = try! FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
    return directory.appendingPathComponent("mydb.sqlite3")
  }()
  
  override func setUp() {
    guard let driver = SQLite3Driver(databaseFileURL: databaseFileURL) else {
      fatalError("failed to initialize SQLite3Driver for Database")
    }
    
    database = Database(driver: driver)
    try! database.clearDatabase()
    
    try! todos.forEach(database.save)
  }
  
  override func tearDown() {
    XCTAssertNoThrow(try database.deleteDatabase())
    database = nil
  }
  
  func testLoadAllData() throws {
    let loadedTodos: [Todo] = try database.loadAll()
    XCTAssertEqual(loadedTodos.count, todos.count)
    
    loadedTodos.enumerated().forEach { (index, todo) in
      let todoIndex = index + 1
      XCTAssertEqual(todo.title, "Todo \(todoIndex)")
      XCTAssertEqual(todo.detail, "Todo Item number \(todoIndex)")
    }
  }
  
  func testLoadSingleData() throws {
    let todo = todos[0]
    guard let loadedTodo: Todo = try database.load(todo.identifier) else {
      XCTFail("Cannot load the saved data")
      return
    }
    
    XCTAssertEqual(loadedTodo.title, todo.title)
    XCTAssertEqual(loadedTodo.detail, todo.detail)
    XCTAssertEqual(loadedTodo.dueDate, todo.dueDate)
  }
  
  func testLoadNotExistedData() throws {
    let loadedTodo: Todo? = try database.load("Undefined Todo")
    XCTAssertNil(loadedTodo)
  }
  
  func testLoadMultipleData() throws {
    let loadedTodos: [Todo] = try database.load(identifiers: ["Todo 5", "Todo 3", "Todo 1", ])
    XCTAssertEqual(loadedTodos.count, 3)
    
    zip([5, 3, 1], loadedTodos).forEach({
      XCTAssertEqual($1.title, "Todo \($0)")
      XCTAssertEqual($1.detail, "Todo Item number \($0)")
    })
  }
  
  func testDeleteDatabase() throws {
    XCTAssertTrue(FileManager.default.fileExists(atPath: databaseFileURL.path))
    XCTAssertNoThrow(try database.deleteDatabase())
    XCTAssertFalse(FileManager.default.fileExists(atPath: databaseFileURL.path))
    
    // Restore the database state
    guard let driver = SQLite3Driver(databaseFileURL: databaseFileURL) else {
      fatalError("failed to initialize SQLite3Driver for Database")
    }
    database = Database(driver: driver)
  }
}


struct Todo : Recordable {
  let title: String
  var detail: String?
  var dueDate: Date?
  
  var identifier: String {
    return title
  }
}
