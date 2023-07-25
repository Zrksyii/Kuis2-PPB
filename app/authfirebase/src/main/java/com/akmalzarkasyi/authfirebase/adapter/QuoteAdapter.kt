package com.akmalzarkasyi.authfirebase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.akmalzarkasyi.authfirebase.data.Quote
import com.akmalzarkasyi.authfirebase.databinding.ItemQuoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuoteAdapter(private val onItemClickCallback: OnItemClickCallback) :
    RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, callback)
    
    inner class QuoteViewHolder(private val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: Quote) {
            with(binding) {
                tvItemTitle.text = quote.title
                tvItemCategory.text = quote.category
                val timestamp = quote.date as com.google.firebase.Timestamp
                val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                val sdf = SimpleDateFormat("dd/MMM/yyyy, HH:mm", Locale.getDefault())
                val netDate = Date(milliseconds)
                val date = sdf.format(netDate).toString()
                tvItemDate.text = date
                tvItemDescription.text = quote.description
                cvItemQuote.setOnClickListener {
                    onItemClickCallback.onItemClicked(quote, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    interface OnItemClickCallback {
        fun onItemClicked(selectedNote: Quote?, position: Int?)
    }
}