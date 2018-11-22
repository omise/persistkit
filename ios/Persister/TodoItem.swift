import Foundation
import PersistKit

struct TodoItem : Recordable {
    let identifier: String
    let description: String
    let completed: Bool

    init(new description: String) {
        self.identifier = UUID().uuidString
        self.description = description
        self.completed = false
    }

    init(complete another: TodoItem) {
        self.identifier = another.identifier
        self.description = another.description
        self.completed = true
    }

    init(uncomplete another: TodoItem) {
        self.identifier = another.identifier
        self.description = another.description
        self.completed = false
    }
}
