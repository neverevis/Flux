package com.socialapp.flux

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.socialapp.flux.databinding.ActivityProfileBinding
import com.socialapp.flux.tool.Base64Converter

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        carregarDadosAtuais()

        val galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                binding.profilePicture.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSalvar.setOnClickListener {
            salvarDados()
        }
    }

    private fun carregarDadosAtuais() {
        val email = firebaseAuth.currentUser?.email
        if (email != null) {
            db.collection("usuarios").document(email).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.username.setText(document.data?.get("username")?.toString() ?: "")
                        binding.nomeCompleto.setText(document.data?.get("nomeCompleto")?.toString() ?: "")
                        
                        try {
                            val fotoString = document.data?.get("fotoPerfil")?.toString() ?: ""
                            if (fotoString.isNotEmpty()) {
                                val bitmap = Base64Converter.stringToBitmap(fotoString)
                                binding.profilePicture.setImageBitmap(bitmap)
                            }
                        } catch (e: Exception) { }
                        
                        binding.btnSalvar.text = "Atualizar Perfil"
                    }
                }
        }
    }

    private fun salvarDados() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email.toString()
            val username = binding.username.text.toString()
            val nomeCompleto = binding.nomeCompleto.text.toString()
            val novaSenha = binding.editNovaSenha.text.toString()

            if (username.isEmpty() || nomeCompleto.isEmpty()) {
                Toast.makeText(this, "Preencha username e nome!", Toast.LENGTH_SHORT).show()
                return
            }

            if (novaSenha.isNotEmpty()) {
                currentUser.updatePassword(novaSenha).addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao alterar senha: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            val fotoPerfilString = Base64Converter.drawableToString(binding.profilePicture.drawable)
            val dados = hashMapOf(
                "nomeCompleto" to nomeCompleto,
                "username" to username,
                "fotoPerfil" to fotoPerfilString
            )

            db.collection("usuarios").document(email)
                .set(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil Salvo!", Toast.LENGTH_SHORT).show()
                    val isEditing = intent.getBooleanExtra("isEditing", false)
                    if (isEditing) {
                        finish()
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}