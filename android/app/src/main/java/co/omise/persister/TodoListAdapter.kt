package co.omise.persister

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoListAdapter(private val itemSelectListener: OnItemSelectListener) :
    RecyclerView.Adapter<TodoListViewHolder>() {
    private var todoItems: List<TodoItem> = emptyList()

    fun setItems(items: List<TodoItem>) {
        this.todoItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item_todo, parent, false)
        val holder = TodoListViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                itemSelectListener.onSelected(todoItems[position])
            }
        }

        return holder
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        val tv = holder.view as TextView
        val todoItem = todoItems[position]
        tv.text = todoItem.description
        tv.isSelected = todoItem.completed
    }
}

class TodoListViewHolder(val view: View) : RecyclerView.ViewHolder(view)

interface OnItemSelectListener {
    fun onSelected(item: TodoItem)
}