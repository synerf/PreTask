package com.synerf.pretask.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivitySignInBinding

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    // firebase auth - followed from firebase sign in docs
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initializing firebase auth
        auth = FirebaseAuth.getInstance()

        // make activity fullscreen
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // setting up action bar
        setUpActionBar()

        // handle click on signIn btn
        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }

    /**
     * function to setup action bar
     */
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarSignInActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        binding.toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * function to signIn user
     */
    private fun signInRegisteredUser() {
        val email: String = binding.etEmailSignIn.text.toString().trim{ it <= ' ' }
        val password: String = binding.etPasswordSignIn.text.toString().trim{ it <= ' ' }

        // validate the form
        if (validateForm(email, password)) {
            // show progress dialog
            showProgressDialog(resources.getString(R.string.please_wait))
            // sign in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // hide progress dialog
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d("Sign in", "signInWithEmail:success")
                        val user = auth.currentUser
                        // intent to move to main activity
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // If sign in fails
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * function to validate the form
     */
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            } else -> {
                true
            }
        }
    }
}