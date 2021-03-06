package co.omise.persister

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import co.omise.persistkit.EncryptedDatabase
import co.omise.persistkit.Database
import co.omise.persistkit.driver.room.RoomDriver
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val database: Database by lazy {
        val driver = RoomDriver(applicationContext, "persistkit")
        EncryptedDatabase(driver, "persiskit-test")
    }

    private val adapter = TodoListAdapter(object : OnItemSelectListener {
        override fun onDelete(identifier: String) {
            background.execute {
                database.delete(identifier)
                loadTodoListItems()
            }
        }

        override fun onSelected(item: TodoItem) {
            background.execute {
                val selectedItem = item.copy(completed = !item.completed)
                database.save(selectedItem)
                reloadTodoItem(selectedItem)
            }
        }

    })

    private val background = Executors.newSingleThreadExecutor()!!
    private val handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        background.execute {
            val items: List<TodoItem> = database.loadAll()
            handler.post {
                adapter.setItems(items)
            }
        }

        main_recycler_view.adapter = adapter
        main_recycler_view.layoutManager = LinearLayoutManager(this)
        main_recycler_view.addItemDecoration(CustomItemDecoration(this))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.main_menu_new -> handleNewItemTapped()
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun handleNewItemTapped(): Boolean {
        showCreateTodoDialog { title ->
            if (title.isEmpty()) return@showCreateTodoDialog

            val randomId = UUID.randomUUID().toString()

            val item = TodoItem(randomId, title, false)
            background.execute {
                database.save(item)
                loadTodoListItems()
            }
        }
        return true
    }

    private fun showCreateTodoDialog(okListener: (title: String) -> Unit) {
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.todo_create_dialog, (main_recycler_view.parent as ViewGroup), false)
        val editText: EditText = view.findViewById(R.id.todo_create_dialog_edit_text)

        AlertDialog.Builder(this)
            .setTitle(R.string.create_todo_title)
            .setView(view)
            .setPositiveButton(R.string.create_todo_create_button_title) { _, _ -> okListener.invoke(editText.text.toString()) }
            .setNegativeButton(R.string.create_todo_cancel_button_title, null)
            .show()
    }

    private fun reloadTodoItem(item: TodoItem) {
        background.execute {
            val updatedItem: TodoItem = database.load(item.identifier) ?: return@execute
            handler.post {
                adapter.updateItem(updatedItem)
            }
        }
    }

    private fun loadTodoListItems() {
        background.execute {
            val newList: List<TodoItem> = database.loadAll()
            handler.post {
                adapter.setItems(newList)
            }
        }
    }
}
