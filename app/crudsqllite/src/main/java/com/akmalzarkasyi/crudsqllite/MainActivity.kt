package com.akmalzarkasyi.crudsqllite

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akmalzarkasyi.crudsqllite.Helper.EXTRA_POSITION
import com.akmalzarkasyi.crudsqllite.Helper.EXTRA_QUOTE
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_ADD
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_DELETE
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_UPDATE
import com.akmalzarkasyi.crudsqllite.Helper.mapCursorToArrayList
import com.akmalzarkasyi.crudsqllite.data.Quote
import com.akmalzarkasyi.crudsqllite.databinding.ActivityMainBinding
import com.akmalzarkasyi.crudsqllite.db.QuoteHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var quoteHelper: QuoteHelper
    private lateinit var adapter: QuoteAdapter

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                when (it.resultCode) {
                    RESULT_ADD -> {
                        val quote = it?.data?.parcelable<Quote>(EXTRA_QUOTE) as Quote
                        adapter.addItem(quote)
                        binding.rvQuotes.smoothScrollToPosition(adapter.itemCount - 1)
                        showSnackBarMessage("Satu item berhasil ditambahkan")
                    }

                    RESULT_UPDATE -> {
                        val quote = it?.data?.parcelable<Quote>(EXTRA_QUOTE) as Quote
                        val position = it.data?.getIntExtra(EXTRA_POSITION, 0) as Int
                        adapter.updateItem(position, quote)
                        binding.rvQuotes.smoothScrollToPosition(position)
                        showSnackBarMessage("Satu item berhasil diubah")
                    }

                    RESULT_DELETE -> {
                        val position = it?.data?.getIntExtra(EXTRA_POSITION, 0) as Int
                        adapter.removeItem(position)
                        showSnackBarMessage("Satu item berhasil dihapus")
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.title = "Quotes"
        binding.apply {
            rvQuotes.layoutManager = LinearLayoutManager(this@MainActivity)
            rvQuotes.setHasFixedSize(true)
            adapter = QuoteAdapter(object : QuoteAdapter.OnItemClickCallback {
                override fun onItemClicked(selectedNote: Quote?, position: Int?) {
                    val intent = Intent(this@MainActivity, QuoteAddUpdateActivity::class.java)
                    intent.putExtra(EXTRA_QUOTE, selectedNote)
                    intent.putExtra(EXTRA_POSITION, position)
                    resultLauncher.launch(intent)
                }
            })
            rvQuotes.adapter = adapter
            quoteHelper = QuoteHelper.getInstance(applicationContext)
            quoteHelper.open()
            if (savedInstanceState == null) {
                loadQuotes()
            } else {
                val list = savedInstanceState.parcelableArrayList<Quote>(EXTRA_STATE)
                if (list != null) {
                    adapter.listQuotes = list
                }
            }
            fabAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, QuoteAddUpdateActivity::class.java)
                resultLauncher.launch(intent)
            }

        }
    }
    private inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listQuotes)
    }

    private fun loadQuotes() {
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            quoteHelper = QuoteHelper.getInstance(applicationContext)
            quoteHelper.open()
            val deferredQuotes = async(Dispatchers.IO) {
                val cursor = quoteHelper.queryAll()
                mapCursorToArrayList(cursor)
            }

            binding.progressbar.visibility = View.INVISIBLE
            val quotes = deferredQuotes.await()
            if (quotes.size > 0) adapter.listQuotes = quotes
            else {
                adapter.listQuotes = ArrayList()
                showSnackBarMessage("Tidak ada data saat ini")
            }
            quoteHelper.close()
        }
    }

    private fun showSnackBarMessage(message: String) {
        Snackbar.make(binding.rvQuotes, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_STATE = "EXTRA_STATE"
    }
}
