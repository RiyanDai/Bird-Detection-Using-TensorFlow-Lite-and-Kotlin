package com.dicoding.birdie.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.dicoding.birdie.database.AnalysisResult
import com.dicoding.birdie.databinding.ItemHistoryBinding
import com.dicoding.birdie.view.detail.detail_history.DetailActivity

class HistoryAdapter(
    private var items: List<AnalysisResult>,
    private val onDeleteClick: (AnalysisResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalysisResult) {
            binding.apply {
                imageView.load(item.imageUri)
                tvLabel.text = item.label



               btndelete.setOnClickListener {
                   AlertDialog.Builder(root.context)
                       .setTitle("Delete Confirmation")
                       .setMessage("Are you sure you want to delete this item?")
                       .setPositiveButton("Yes") { dialog, which ->
                           onDeleteClick(item)
                       }
                       .setNegativeButton("No", null)
                       .show()
                }
                root.setOnClickListener {
                    val context = root.context
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra("imageUri", item.imageUri)
                        putExtra("label", item.label)

                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AnalysisResult>) {
        items = newItems.reversed() // Ensure the newest item is on top
        notifyDataSetChanged()
    }


}
