package com.example.todoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore


import com.google.firebase.firestore.QueryDocumentSnapshot

class FragmentUnstarted : Fragment(), OnTaskActionListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tasks, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewUnstarted)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        taskAdapter = TaskAdapter(firestore, this)

        recyclerView.adapter = taskAdapter

        loadUnstartedTasks()

        return view
    }

    private fun loadUnstartedTasks() {
        val firestoreTaskList = mutableListOf<QueryDocumentSnapshot>()
        val currentUser= FirebaseAuth.getInstance().currentUser
        val status = "W trakcie"
        firestore.collection("Tasks")
            .whereEqualTo("Status", status)
            .whereEqualTo("userId",currentUser)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestoreTaskList.add(document)
                }
                val taskList = firestoreTaskList.map { document ->
                    val taskId = document.id
                    val taskDescription = document.getString("Description") ?: ""
                    Task(taskId, taskDescription, "W trakcie")
                }

                taskAdapter.setTasks(taskList)
                taskAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->

            }
    }

    override fun onTaskDeleted() {

    }

    override fun onTaskStatusChanged() {

    }
}
