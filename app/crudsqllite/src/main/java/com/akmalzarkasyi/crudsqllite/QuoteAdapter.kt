package com.akmalzarkasyi.crudsqllite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akmalzarkasyi.crudsqllite.Helper.categoryList
import com.akmalzarkasyi.crudsqllite.data.Quote
import com.akmalzarkasyi.crudsqllite.databinding.ItemQuoteBinding

class QuoteAdapter(private val onItemClickCallback: OnItemClickCallback) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {
    var listQuotes = ArrayList<Quote>()

    inner class QuoteViewHolder(private val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: Quote) {
            with(binding) {
                tvItemTitle.text = quote.title
                tvItemCategory.text = categoryList[quote.category.toString().toInt()]
                tvItemDescription.text = quote.description
                tvItemDate.text = quote.date
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

    override fun getItemCount(): Int = listQuotes.size

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(listQuotes[position])
    }

    interface OnItemClickCallback {
        fun onItemClicked(selectedNote: Quote?, position: Int?)
    }
    fun addItem(quote: Quote) {
        this.listQuotes.add(quote)
        notifyItemInserted(this.listQuotes.size - 1)
    }
    fun updateItem(position: Int, quote: Quote) {
        this.listQuotes[position] = quote
        notifyItemChanged(position, quote)
    }
    fun removeItem(position: Int) {
        this.listQuotes.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listQuotes.size)
    }

}
