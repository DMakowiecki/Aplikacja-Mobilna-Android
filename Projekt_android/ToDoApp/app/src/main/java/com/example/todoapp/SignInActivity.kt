package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        checkIfUserIsLoggedIn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        binding.btnSignIn.setOnClickListener {
            signin()
        }
    }

    private fun signin() {
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Wprowadź poprawny adres e-mail i hasło", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Zalogowano pomyślnie.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ToDoActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                    println("Exception: ${task.exception}")
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun checkIfUserIsLoggedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, ToDoActivity::class.java))
            finish()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }

        binding.toolbarActivity.setNavigationOnClickListener { onBackPressed() }
    }
}
