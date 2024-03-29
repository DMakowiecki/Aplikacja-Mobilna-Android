package com.example.todoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ActivityToDoBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.example.todoapp.OnTaskActionListener
import com.google.firebase.auth.FirebaseAuth

class ToDoActivity : AppCompatActivity(),OnTaskActionListener {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityToDoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@ToDoActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        taskAdapter = TaskAdapter(firestore, this)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        tabLayout.getTabAt(0)?.select()
        refreshCurrentTab()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.fabAddTask.visibility= View.VISIBLE
                        loadTasksWithStatus("Nierozpoczęte")
                    }
                    1 -> {binding.fabAddTask.visibility= View.INVISIBLE
                        loadTasksWithStatus("W trakcie")

                    }
                    2 -> { binding.fabAddTask.visibility= View.INVISIBLE
                        loadTasksWithStatus("Ukończone")

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dodaj nowe zadanie")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val description = input.text.toString().trim()
            if (description.isNotEmpty()) {
                val status = "Nierozpoczęte"
                addTaskToFirestore(description, status)
            } else {
                Toast.makeText(this, "Podałeś puste pole", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun addTaskToFirestore(description: String, status: String) {
        val tasksCollection = firestore.collection("Tasks")
        val newTaskDocument = tasksCollection.document()
        val currentUser=FirebaseAuth.getInstance().currentUser
        val taskData = hashMapOf(
            "Description" to description,
            "Status" to status,
            "userId" to (currentUser?.uid ?: "" )
        )

        newTaskDocument.set(taskData)
            .addOnSuccessListener {
                refreshCurrentTab()
                Toast.makeText(this, "Zadanie zostało dodane", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Nie udało się dodać zadania", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTasksWithStatus(status: String) {
val currentUser=FirebaseAuth.getInstance().currentUser
        firestore.collection("Tasks")
            .whereEqualTo("Status", status)
            .whereEqualTo("userId", currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                val taskList = mutableListOf<Task>()
                for (document in documents) {
                    val taskId = document.id
                    val taskDescription = document.getString("Description") ?: ""
                    val taskStatus = document.getString("Status") ?: ""
                    taskList.add(Task(taskId, taskDescription, taskStatus))
                }

                taskAdapter.setTasks(taskList)
                taskAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun refreshCurrentTab() {
        val currentTabPosition = binding.tabLayout.selectedTabPosition
        when (currentTabPosition) {
            0 -> loadTasksWithStatus("Nierozpoczęte")
            1 -> loadTasksWithStatus("W trakcie")
            2 -> loadTasksWithStatus("Ukończone")
        }
    }
    override fun onTaskDeleted() {
        refreshCurrentTab()
    }

    override fun onTaskStatusChanged() {
        refreshCurrentTab()
    }
}
