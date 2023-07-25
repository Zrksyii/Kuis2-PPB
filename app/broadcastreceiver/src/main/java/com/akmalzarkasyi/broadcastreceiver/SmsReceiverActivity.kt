package com.akmalzarkasyi.broadcastreceiver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.akmalzarkasyi.broadcastreceiver.databinding.ActivitySmsReceiverBinding

class SmsReceiverActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmsReceiverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            title = getString(R.string.incoming_message)
            val senderNo = intent.getStringExtra(EXTRA_SMS_NO)
            val senderMessage = intent.getStringExtra(EXTRA_SMS_MESSAGE)
            tvFrom.text = getString(R.string.coming_from, senderNo)
            tvMessage.text = senderMessage
            btnClose.setOnClickListener { finish() }
        }
    }

    companion object {
        const val EXTRA_SMS_NO = "extra_sms_no"
        const val EXTRA_SMS_MESSAGE = "extra_sms_message"
    }
}