package com.example.todoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class TaskAdapter(
    private val firebaseFirestore: FirebaseFirestore,
    private val listener: OnTaskActionListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = mutableListOf()

    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)

        holder.deleteButton.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Usunięto zadanie ${task.description}",
                Toast.LENGTH_SHORT
            ).show()
            holder.deleteTask(task)
        }

        holder.changeStatusButton.setOnClickListener {
            holder.showPopupMenu(task)
        }

        holder.editButton.setOnClickListener {
            holder.showEditTaskDialog(task)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView
        val deleteButton: Button
        val changeStatusButton: Button
        val editButton: Button

        init {
            taskNameTextView = itemView.findViewById(R.id.taskNameTextView)
            deleteButton = itemView.findViewById(R.id.buttonDelete)
            changeStatusButton = itemView.findViewById(R.id.changeStatusButton)
            editButton = itemView.findViewById(R.id.buttonEdit)
        }

        fun bind(task: Task) {
            taskNameTextView.text = task.description
        }

        fun deleteTask(task: Task) {
            firebaseFirestore.collection("Tasks").document(task.taskid).delete()
                .addOnSuccessListener {
                    Toast.makeText(
                        itemView.context,
                        "Usunięto zadanie ${task.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onTaskDeleted()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        itemView.context,
                        "Błąd podczas usuwania zadania",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun changeTaskStatus(task: Task, newStatus: String) {
            firebaseFirestore.collection("Tasks").document(task.taskid)
                .update("Status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(
                        itemView.context,
                        "Zmieniono status zadania na $newStatus",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onTaskStatusChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        itemView.context,
                        "Błąd podczas zmiany statusu zadania",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun showPopupMenu(task: Task) {
            val popupMenu = PopupMenu(itemView.context, changeStatusButton)
            popupMenu.menuInflater.inflate(R.menu.status_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_in_progress -> {
                        changeTaskStatus(task, "W trakcie")
                        true
                    }
                    R.id.menu_completed -> {
                        changeTaskStatus(task, "Ukończone")
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        fun showEditTaskDialog(task: Task) {
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Edytuj zadanie")

            val input = EditText(itemView.context)
            input.setText(task.description)

            builder.setView(input)

            builder.setPositiveButton("Zapisz") { dialog, _ ->
                val newDescription = input.text.toString().trim()
                if (newDescription.isNotEmpty()) {
                    editTaskDescription(task, newDescription)
                } else {
                    Toast.makeText(itemView.context, "Podałeś puste pole", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Anuluj") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        fun editTaskDescription(task: Task, newDescription: String) {
            firebaseFirestore.collection("Tasks").document(task.taskid)
                .update("Description", newDescription)
                .addOnSuccessListener {
                    Toast.makeText(
                        itemView.context,
                        "Zaktualizowano opis zadania",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onTaskStatusChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        itemView.context,
                        "Błąd podczas aktualizacji opisu zadania",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
