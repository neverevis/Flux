package com.socialapp.flux

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.socialapp.flux.adapter.PostAdapter
import com.socialapp.flux.databinding.ActivityHomeBinding
import com.socialapp.flux.model.Post
import com.socialapp.flux.tool.Base64Converter

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    
    private var lastVisibleDocument: DocumentSnapshot? = null
    private var buscaAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        
        postAdapter = PostAdapter(ArrayList())
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFeed.adapter = postAdapter

        carregarPerfil()
        setupListeners()
        
        carregarFeed()
    }

    private fun carregarPerfil() {
        val email = firebaseAuth.currentUser?.email
        if (email != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(email).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            val data = document.data
                            if (data != null && data.containsKey("fotoPerfil") && data.containsKey("username")) {
                                try {
                                    val imageString = data["fotoPerfil"].toString()
                                    val bitmap = Base64Converter.stringToBitmap(imageString)
                                    binding.imgLogo.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                }

                                binding.txtUsername.text = data["username"].toString()
                                binding.txtNomeCompleto.text = data["nomeCompleto"]?.toString() ?: data["nomecompleto"]?.toString() ?: ""
                            } else {
                                startActivity(Intent(this, ProfileActivity::class.java))
                                finish()
                            }
                        } else {
                            startActivity(Intent(this, ProfileActivity::class.java))
                            finish()
                        }
                    }
                }
        }
    }

    private val newPostLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { 
        postAdapter.clear()
        lastVisibleDocument = null
        carregarFeed()
    }

    private val editProfileLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) {
        carregarPerfil()
    }

    private fun setupListeners() {
        binding.btnNovaPostagem.setOnClickListener {
            newPostLauncher.launch(Intent(this, NewPostActivity::class.java))
        }

        binding.btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("isEditing", true)
            editProfileLauncher.launch(intent)
        }

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        binding.btnBuscarCidade.setOnClickListener {
            buscaAtual = binding.editBuscaCidade.text.toString().trim()
            postAdapter.clear()
            lastVisibleDocument = null
            carregarFeed()
        }
        
        binding.btnCarregarMais.setOnClickListener {
            carregarFeed()
        }
    }

    private fun carregarFeed() {
        val db = FirebaseFirestore.getInstance()
        var query = db.collection("postagens")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            
        if (buscaAtual.isNotEmpty()) {
            query = db.collection("postagens")
                .whereEqualTo("cidade", buscaAtual)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
        }

        if (lastVisibleDocument != null) {
            query = query.startAfter(lastVisibleDocument!!)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    lastVisibleDocument = documents.documents[documents.size() - 1]
                    
                    val novosPosts = ArrayList<Post>()
                    for (document in documents) {
                        val post = Post(
                            autor = document.data["autor"]?.toString() ?: "",
                            texto = document.data["texto"]?.toString() ?: "",
                            imagem = document.data["imagem"]?.toString() ?: "",
                            cidade = document.data["cidade"]?.toString() ?: "",
                            timestamp = document.data["timestamp"] as? Long ?: 0L
                        )
                        novosPosts.add(post)
                    }
                    postAdapter.addPosts(novosPosts)
                } else {
                    if (postAdapter.itemCount == 0) {
                        Toast.makeText(this, "Nenhum post encontrado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Você chegou ao fim do feed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar feed. Cheque o Logcat para criar Índices!", Toast.LENGTH_LONG).show()
                exception.printStackTrace()
            }
    }
}
