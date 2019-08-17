import UIKit
import PersistKit

class ListViewModel {
  lazy var db: Database = ListViewModel.loadDatabase()
  
  private(set) var todoItems: [TodoItem] = []
  
  init() {
    todoItems = try! db.loadAll(TodoItem.self)
  }
  
  func add(item: TodoItem) {
    try! db.save(item)
    todoItems = try! db.loadAll(TodoItem.self)
  }
  
  func toggle(item: TodoItem) -> TodoItem {
    var newItem = item        
    if item.completed {
      newItem = TodoItem(uncomplete: item)
    } else {
      newItem = TodoItem(complete: item)
    }
    
    try! db.save(newItem)
    newItem = reload(item: newItem)
    
    return newItem
  }
  
  func remove(at index: Int) -> Bool {
    guard 0..<todoItems.count ~= index else { return false }
    let todoItem = todoItems[index]
    
    guard try! db.delete(todoItem.identifier) else { return false }
    todoItems.remove(at: index)
    return true
  }
  
  func reload(item: TodoItem) -> TodoItem {
    guard let index = todoItems.firstIndex(where: { (todoItem) -> Bool in return todoItem.identifier == item.identifier }) else {
      fatalError("failed to find item in existing todoItems")
    }
    
    guard let reloadedItem: TodoItem = try! db.load(TodoItem.self, identifier: item.identifier) else {
      fatalError("failed to find item \"\(item.identifier)\" in database")
    }
    
    todoItems[index] = reloadedItem
    return reloadedItem
  }
  
  private static func loadDatabase() -> Database {
    guard let dir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first else {
      fatalError("could not access the filesystem")
    }
    
    let dbPath = URL(fileURLWithPath: dir).appendingPathComponent("mydb.sqlite3")
    guard let driver = SQLite3Driver(databaseFileURL: dbPath) else {
      fatalError("failed to initialize SQLite3Driver for Database")
    }
    
    return Database(driver: driver)
  }
}
