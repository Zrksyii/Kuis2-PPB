package com.akmalzarkasyi.authfirebase

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.akmalzarkasyi.authfirebase.data.Quote
import com.akmalzarkasyi.authfirebase.databinding.ActivityQuoteAddUpdateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class QuoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {
    private val binding: ActivityQuoteAddUpdateBinding by lazy {
        ActivityQuoteAddUpdateBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isEdit = false
    private var categoriesSpinnerArray = ArrayList<String>()
    private var quote: Quote? = null
    private var position: Int = 0
    private var categorySelection: Int = 0
    private var categoryName: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = Firebase.firestore
        auth = Firebase.auth
        categoriesSpinnerArray = getCategories()
        quote = intent.parcelable(Helper.EXTRA_QUOTE)
        if (quote != null) {
            position = intent.getIntExtra(Helper.EXTRA_POSITION, 0)
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
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnSubmit.text = btnTitle
        binding.btnSubmit.setOnClickListener(this)

    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private fun getCategories(): ArrayList<String> {
        binding.progressbar.visibility = View.VISIBLE
        firestore.collection("categories")
            .whereEqualTo("is_active", true)
            .get()
            .addOnSuccessListener { documents ->
                for ((selection, document) in documents.withIndex()) {
                    val name = document.get("name").toString()
                    quote?.let {
                        if (name == it.category) {
                            categorySelection = selection
                        }
                    }
                    categoriesSpinnerArray.add(name)
                }
                setCategories(categoriesSpinnerArray)
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@QuoteAddUpdateActivity,
                    "Categories cannot be retrieved ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        return categoriesSpinnerArray
    }

    private fun setCategories(spinnerArray: ArrayList<String>) {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spinnerArray)
        binding.apply {
            edtCategory.adapter = spinnerAdapter
            edtCategory.setSelection(categorySelection)
            edtCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    categoryName = edtCategory.selectedItem.toString()
                    progressbar.visibility = View.INVISIBLE
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    override fun onClick(p0: View?) {
        if (p0?.id == R.id.btn_submit) {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()
            if (title.isEmpty()) {
                binding.edtTitle.error = "Field can not be blank"
                return
            }
            if (isEdit) {
                val currentUser = auth.currentUser
                val user = hashMapOf(
                    "uid" to currentUser?.uid,
                    "title" to title,
                    "description" to description,
                    "category" to categoryName,
                    "date" to FieldValue.serverTimestamp()
                )
                firestore.collection("quotes").document(quote?.id.toString())
                    .set(user)
                    .addOnSuccessListener {
                        setResult(Helper.RESULT_UPDATE, intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@QuoteAddUpdateActivity, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val currentUser = auth.currentUser
                val user = hashMapOf(
                    "uid" to currentUser?.uid,
                    "title" to title,
                    "description" to description,
                    "category" to categoryName,
                    "date" to FieldValue.serverTimestamp()
                )
                firestore.collection("quotes")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this@QuoteAddUpdateActivity,
                            "DocumentSnapshot added with ID: ${documentReference.id}",
                            Toast.LENGTH_SHORT).show()
                        setResult(Helper.RESULT_ADD, intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@QuoteAddUpdateActivity, "Error adding document", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showAlertDialog(Helper.ALERT_DIALOG_DELETE)
                true
            }
            android.R.id.home -> {
                showAlertDialog(Helper.ALERT_DIALOG_CLOSE)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == Helper.ALERT_DIALOG_CLOSE
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
                    firestore.collection("quotes").document(quote?.id.toString())
                        .delete()
                        .addOnSuccessListener {
                            Log.d("delete", "DocumentSnapshot successfully deleted!"+quote?.id.toString())
                            val intent = Intent()
                            intent.putExtra(Helper.EXTRA_POSITION, position)
                            setResult(Helper.RESULT_DELETE, intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("a", "Error deleting document", e)
                            Toast.makeText(this@QuoteAddUpdateActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}