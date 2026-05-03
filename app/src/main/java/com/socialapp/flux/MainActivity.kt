package com.socialapp.flux

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.socialapp.flux.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebase()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    fun setupFirebase(){
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun setupListeners(){
        binding.btnLogin.setOnClickListener{
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            firebaseAuth
                .signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    else{
                        Toast.makeText(this,"erro no login", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnCreateAccount.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}