package com.akmalzarkasyi.crudsqllite

import android.database.Cursor
import com.akmalzarkasyi.crudsqllite.data.Quote
import com.akmalzarkasyi.crudsqllite.db.DatabaseContract
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper {
    var categoryList = arrayOf(
        "Motivasi",
        "Persahabatan",
        "Percintaan",
        "Keluarga",
        "Musik",
        "Film"
    )
    const val EXTRA_QUOTE = "extra_quote"
    const val EXTRA_POSITION = "extra_position"
    const val RESULT_ADD = 101
    const val RESULT_UPDATE = 201
    const val RESULT_DELETE = 301
    const val ALERT_DIALOG_CLOSE = 10
    const val ALERT_DIALOG_DELETE = 20
    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<Quote> {
        val quotesList = ArrayList<Quote>()
        notesCursor?.apply {
            while (moveToNext()) {
                val id =
                    getInt(getColumnIndexOrThrow(DatabaseContract.QuoteColumns._ID))
                val title =
                    getString(getColumnIndexOrThrow(DatabaseContract.QuoteColumns.TITLE))
                val description =
                    getString(getColumnIndexOrThrow(DatabaseContract.QuoteColumns.DESCRIPTION))
                val category =
                    getString(getColumnIndexOrThrow(DatabaseContract.QuoteColumns.CATEGORY))
                val date =
                    getString(getColumnIndexOrThrow(DatabaseContract.QuoteColumns.DATE))
                quotesList.add(Quote(id, title, description, category, date))
            }
        }
        return quotesList
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}
