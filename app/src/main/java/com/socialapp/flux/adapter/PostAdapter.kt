package com.socialapp.flux.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.socialapp.flux.R
import com.socialapp.flux.model.Post
import com.socialapp.flux.tool.Base64Converter

class PostAdapter(private val posts: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAutorPost: TextView = itemView.findViewById(R.id.txtAutorPost)
        val txtCidadePost: TextView = itemView.findViewById(R.id.txtCidadePost)
        val txtDescricaoPost: TextView = itemView.findViewById(R.id.txtDescricaoPost)
        val imgPostItem: ImageView = itemView.findViewById(R.id.imgPostItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        holder.txtAutorPost.text = post.autor
        holder.txtCidadePost.text = post.cidade
        holder.txtDescricaoPost.text = post.texto
        
        try {
            val bitmap = Base64Converter.stringToBitmap(post.imagem)
            holder.imgPostItem.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addPosts(novosPosts: List<Post>) {
        val startPosition = posts.size
        posts.addAll(novosPosts)
        notifyItemRangeInserted(startPosition, novosPosts.size)
    }
    
    fun clear() {
        val size = posts.size
        posts.clear()
        notifyItemRangeRemoved(0, size)
    }
}
