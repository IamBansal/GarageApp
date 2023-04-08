@file:Suppress("DEPRECATION")

package com.example.garageapp.ui.screens

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.garageapp.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            startActivity(Intent(this, DashBoard::class.java)).apply { finish() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.garageapp.R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        binding.btnGoogleSignUp.setOnClickListener {
            signUpWithGoogle()
        }

    }

    private fun signUpWithGoogle() = launcher.launch(mGoogleSignInClient.signInIntent)

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) handleResult(
                GoogleSignIn.getSignedInAccountFromIntent(
                    it.data
                )
            )
            else Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show()
        }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        if (completedTask.isSuccessful) {
            val account: GoogleSignInAccount? = completedTask.result
            if (account != null) {
                val progress = ProgressDialog(this)
                progress.setMessage("Signing you in....")
                progress.show()
                updateUI(account, progress)
            }
        } else {
            Toast.makeText(this, completedTask.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount, dialog: ProgressDialog) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            dialog.dismiss()
            if (task.isSuccessful) startActivity(
                Intent(
                    this,
                    DashBoard::class.java
                )
            ).apply { finish() }
            else Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}