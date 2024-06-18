package com.dicoding.birdie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.birdie.data.burunge
import com.dicoding.birdie.databinding.ItemCardBinding

class BurungAdapter(private val listBird: ArrayList<burunge>) : RecyclerView.Adapter<BurungAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class ListViewHolder(var binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)


    open class OnItemClickCallback {
        open fun onItemClicked(data: burunge) {}
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = listBird.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val (name, description, photo) = listBird[position]
        holder.binding.imgItemPhoto.setImageResource(photo)
        holder.binding.tvItemName.text = name
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(listBird[holder.adapterPosition])
        }
    }



}