package com.example.todoapp

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.todoapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            Log.d("SignUpActivity", "start")
            register()
        }

        setupActionBar()
    }

    private fun register() {
        val name: String = binding.etName.text.toString().trim { it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }


        if (validateForm(name, email, password)) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser =task.result!!.user!!
                        val registeredEmail: String? = firebaseUser.email
                        Toast.makeText(
                            this@SignUpActivity,
                            "Gratulacje, zarejestrowałeś się. $registeredEmail",
                            Toast.LENGTH_SHORT
                        ).show()
                            FirebaseAuth.getInstance().signOut()
                        finish()
                    } else {

                        Toast.makeText(
                            this@SignUpActivity,
                            "Błąd podczas rejestracji: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
    }
}
