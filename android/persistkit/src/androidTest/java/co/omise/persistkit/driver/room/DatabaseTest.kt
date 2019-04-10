package co.omise.persistkit.driver.room

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import co.omise.persistkit.BasicDatabase
import co.omise.persistkit.Identifiable
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseTest  {
    private lateinit var database: BasicDatabase
    private val todos: List<Todo> by lazy { (1..20).map { Todo("todo-$it", "todo-item-$it", this.date) } }
    private val date = Date()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getContext()
        val driver = RoomDriver(context, "room-driver-test.sqlite3")
        database = BasicDatabase(driver)

        for (todo in todos) {
            database.save(todo)
        }
    }

    @After
    fun tearDown() {
        database.deleteDatabase()
    }

    @Test
    fun loadAll_shouldRetrieveAllRecord() {
        val storedRecords = database.loadAll<Todo>()
        assertEquals(todos.size, storedRecords.size)
    }

    @Test
    fun load_shouldReturnSingleRecordIfFound() {
        val todo = database.load<Todo>("todo-1")
        assertNotNull(todo)

        val record = todo ?: return

        assertEquals("todo-1", record.identifier)
        assertEquals("todo-item-1", record.detail)
        assertEquals(date, record.dueDate)

        val todoItem10 = database.load<Todo>("todo-10")
        assertNotNull(todoItem10)

        val recordOfItem10 = todoItem10 ?: return

        assertEquals("todo-10", recordOfItem10.identifier)
        assertEquals("todo-item-10", recordOfItem10.detail)
        assertEquals(date, recordOfItem10.dueDate)
    }

    @Test
    fun loadWithIDs_shouldReturnMultipleRecordsIfFound() {
        val ids = listOf("todo-3", "todo-1", "todo-5")
        val todos = database.load<Todo>(ids)
        assertEquals(todos.size, 3)

        ids.zip(todos).forEach {
            val id = it.first
            val todo = it.second
            assertEquals(id, todo.identifier)
        }
    }

    @Test
    fun load_shouldReturnEmptyArrayIfNotExists() {
        val todos = database.load<Todo>("not-existing-todo")
        assertNull(todos)
    }

    @Test
    fun save_shouldReplaceContentIfAlreadyExist() {
        val record = database.load<Todo>("todo-1")
        assertNotNull(record)
        val todo = record ?: return
        todo.detail = "foobar"
        database.save(todo)

        val updatedRecord = database.load<Todo>("todo-1")
        assertNotNull(updatedRecord)
        val updatedTodo = updatedRecord ?: return

        assertEquals("todo-1", updatedTodo.identifier)
        assertEquals(todo.detail, updatedTodo.detail)
    }
}


data class Todo(val title: String, var detail: String, var dueDate: Date) : Serializable, Identifiable {
    override val identifier: String
        get() = title
}

