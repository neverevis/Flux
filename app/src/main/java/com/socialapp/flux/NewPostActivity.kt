package com.socialapp.flux

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.socialapp.flux.databinding.ActivityNewPostBinding
import com.socialapp.flux.tool.Base64Converter
import com.socialapp.flux.tool.LocalizacaoHelper
import java.util.Locale

class NewPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
    private lateinit var binding: ActivityNewPostBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var nomeCidade: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val galeria = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                binding.imgPost.setImageURI(uri)
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEscolherFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnObterLocalizacao.setOnClickListener {
            solicitarLocalizacao()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }

        binding.btnPostar.setOnClickListener {
            val descricao = binding.txtDescricao.text.toString()

            if (descricao.isEmpty() || binding.imgPost.drawable == null) {
                Toast.makeText(this, "Adicione uma foto e uma descrição", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = firebaseAuth.currentUser?.email.toString()
            val fotoString = Base64Converter.drawableToString(binding.imgPost.drawable)

            val db = FirebaseFirestore.getInstance()
            val postagem = hashMapOf(
                "autor" to email,
                "texto" to descricao,
                "imagem" to fotoString,
                "cidade" to nomeCidade,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("postagens")
                .add(postagem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Postagem criada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao postar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(this)
            Toast.makeText(this, "Buscando localização...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            solicitarLocalizacao()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocalizacaoRecebida(latitude: Double, longitude: Double) {
        runOnUiThread {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val enderecos = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (!enderecos.isNullOrEmpty()) {
                    val cidade = enderecos[0].subAdminArea ?: enderecos[0].locality ?: "Cidade desconhecida"
                    val estado = enderecos[0].adminArea ?: ""
                    nomeCidade = "$cidade - $estado"
                    binding.txtLocalizacao.text = "📍 $nomeCidade"
                } else {
                    nomeCidade = "Localização Desconhecida"
                    binding.txtLocalizacao.text = "📍 $nomeCidade"
                }
            } catch (e: Exception) {
                nomeCidade = "Erro ao buscar cidade"
                binding.txtLocalizacao.text = "📍 $nomeCidade"
                e.printStackTrace()
            }
        }
    }

    override fun onErro(mensagem: String) {
        runOnUiThread {
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        }
    }
}
