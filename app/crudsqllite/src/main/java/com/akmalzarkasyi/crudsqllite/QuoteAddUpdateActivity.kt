package com.akmalzarkasyi.crudsqllite

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.akmalzarkasyi.crudsqllite.Helper.ALERT_DIALOG_CLOSE
import com.akmalzarkasyi.crudsqllite.Helper.ALERT_DIALOG_DELETE
import com.akmalzarkasyi.crudsqllite.Helper.EXTRA_POSITION
import com.akmalzarkasyi.crudsqllite.Helper.EXTRA_QUOTE
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_ADD
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_DELETE
import com.akmalzarkasyi.crudsqllite.Helper.RESULT_UPDATE
import com.akmalzarkasyi.crudsqllite.Helper.categoryList
import com.akmalzarkasyi.crudsqllite.Helper.getCurrentDate
import com.akmalzarkasyi.crudsqllite.data.Quote
import com.akmalzarkasyi.crudsqllite.databinding.ActivityQuoteAddUpdateBinding
import com.akmalzarkasyi.crudsqllite.db.DatabaseContract
import com.akmalzarkasyi.crudsqllite.db.DatabaseContract.QuoteColumns.Companion.DATE
import com.akmalzarkasyi.crudsqllite.db.QuoteHelper

class QuoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {
    private val binding: ActivityQuoteAddUpdateBinding by lazy {
        ActivityQuoteAddUpdateBinding.inflate(layoutInflater)
    }
    private var isEdit = false
    private var quote: Quote? = null
    private var position: Int = 0
    private var category: String = "0"
    private lateinit var quoteHelper: QuoteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        binding.apply {
            edtCategory.adapter = spinnerAdapter
            edtCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    category = p2.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Toast.makeText(this@QuoteAddUpdateActivity, "Array terpilih = $position", Toast.LENGTH_SHORT).show()
                }

            }

            quoteHelper = QuoteHelper.getInstance(applicationContext)
            quoteHelper.open()
            quote = intent.parcelable(EXTRA_QUOTE)
            if (quote != null) {
                position = intent.getIntExtra(EXTRA_POSITION, 0)
                isEdit = true
            } else {
                quote = Quote()
            }
            val actionBarTitle: String
            val btnTitle: String
            if (isEdit) {
                actionBarTitle = "Ubah"
                btnTitle = "Update"
                quote?.let {
                    edtTitle.setText(it.title)
                    edtDescription.setText(it.description)
                    edtCategory.setSelection(it.category!!.toInt())
                }!!
            } else {
                actionBarTitle = "Tambah"
                btnTitle = "Simpan"
            }
            supportActionBar?.title = actionBarTitle
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            btnSubmit.text = btnTitle
            btnSubmit.setOnClickListener(this@QuoteAddUpdateActivity)
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit) {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()
            val penulis = binding.edtPenulis.text.toString().trim()
            if (title.isEmpty()) {
                binding.edtTitle.error = "Field can not be blank"
                return
            }
            quote?.title = title
            quote?.description = description
            quote?.category = category
            val intent = Intent()
            intent.putExtra(EXTRA_QUOTE, quote)
            intent.putExtra(EXTRA_POSITION, position)
            val values = ContentValues()
            values.put(DatabaseContract.QuoteColumns.TITLE, title)
            values.put(DatabaseContract.QuoteColumns.DESCRIPTION, description)
            values.put(DatabaseContract.QuoteColumns.CATEGORY, category)
            if (isEdit) {
                val result = quoteHelper.update(quote?.id.toString(),
                    values).toLong()
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this@QuoteAddUpdateActivity, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                }
            } else {
                quote?.date = getCurrentDate()
                values.put(DATE, getCurrentDate())
                val result = quoteHelper.insert(values)
                if (result > 0) {
                    quote?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast.makeText(this@QuoteAddUpdateActivity, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Quote"
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = quoteHelper.deleteById(quote?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@QuoteAddUpdateActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }
}
