package com.akmalzarkasyi.authfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.akmalzarkasyi.authfirebase.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        binding.apply {
            btnSignOut.setOnClickListener(this@MainActivity)
            btnEmailVerify.setOnClickListener(this@MainActivity)
            btnQuote.setOnClickListener(this@MainActivity)
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        } else {
            updateUI(currentUser)
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnSignOut -> signOut()
            R.id.btnEmailVerify -> sendEmailVerification()
            R.id.btnQuote -> startActivity(Intent(this, DashboardQuoteActivity::class.java))
        }
    }

    private fun sendEmailVerification() {
        binding.btnEmailVerify.isEnabled = false
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            binding.btnEmailVerify.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(
                    this, "Verification email sent to ${user.email} ", Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        googleSignInClient.signOut().addOnCompleteListener(this) {
        }
    }
    private fun updateUI(currentUser: FirebaseUser) {
        binding.btnEmailVerify.isVisible = false
        currentUser.let {
            val name = it.displayName
            val phoneNumber = it.phoneNumber
            val email = it.email
            val photoUrl = it.photoUrl
            val emailVerified = it.isEmailVerified
            val uid = it.uid
            binding.tvName.text = name
            if (TextUtils.isEmpty(name)) {
                binding.tvName.text = "20102224_Akmal Zarkasyi"
            }
            binding.tvUserId.text = email
            for (profile in it.providerData) {
                val providerId = profile.providerId
                if (providerId == "password" && !emailVerified) {
                    binding.btnEmailVerify.isVisible = true
                }
                if (providerId == "phone") {
                    binding.tvName.text = phoneNumber
                    binding.tvUserId.text = providerId
                }
            }
        }
    }

}
