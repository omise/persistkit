package co.omise.persistkit.driver.room

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import co.omise.persistkit.Command
import co.omise.persistkit.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDriverTest  {
    private lateinit var driver: RoomDriver
    private val records: List<Record> by lazy { (1..20).map { Record("todo-$it", "kind", 0, "todo-item-$it".toByteArray()) } }

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getContext();
        driver = RoomDriver(context, "room-driver-test.sqlite3")
        driver.deleteAll()

        for (record in records) {
            driver.execute(Command.Save(record))
        }
    }

    @Test
    fun loadAll_shouldRetrieveAllRecord() {
        val storedRecords = driver.query(Command.LoadAll)
        assertEquals(records.size, storedRecords.size)
    }

    @Test
    fun load_shouldReturnSingleRecordIfFound() {
        val records = driver.db.queries().load("todo-1")
        assertEquals(records.size, 1)

        val record = records.first()
        assertEquals("todo-1", record.identifier)
        assertEquals("todo-item-1", String(record.content))
    }

    @Test
    fun loadWithIDs_shouldReturnMultipleRecordsIfFound() {
        val records = driver.db.queries().load(listOf("todo-1", "todo-3", "todo-5"))
        assertEquals(records.size, 3)

        records.forEachIndexed { index, record ->
            val todoItemIndex = (index * 2) + 1
            assertEquals("todo-$todoItemIndex", record.identifier)
            assertEquals("todo-item-$todoItemIndex", String(record.content))
        }
    }

    @Test
    fun load_shouldReturnEmptyArrayIfNotExists() {
        val records = driver.db.queries().load("not-existing-todo")
        assertEquals(0, records.size)
    }

    @Test
    fun save_shouldReplaceContentIfAlreadyExist() {
        val record = driver.db.queries().load("todo-1").first()
        record.content = "foobar".toByteArray()
        driver.execute(Command.Save(record))

        val updatedRecord = driver.db.queries().load("todo-1").first()
        assertNotNull(record)

        assertEquals("todo-1", record.identifier)
        assertEquals(String(record.content), String(updatedRecord.content))
    }
}

