package com.synerf.pretask.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivitySignUpBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.User

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // handle click on signUp btn
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    /**
     * function to setup action bar
     */
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarSignUpActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * function to be called when user is registered successfully and entry is made in the firestore database.
     */
    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()
        // hide progress dialog
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    /**
     * function to register a user
     */
    private fun registerUser() {
        // data from the textViews
        val name: String = binding.etName.text.toString().trim{ it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim{ it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim{ it <= ' ' }

        // before registering, validate the form
        if (validateForm(name, email, password)) {
            // show progress dialog
            showProgressDialog(resources.getString(R.string.please_wait))
            // create user (register user)
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    /**
     * function to validate the form
     */
    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
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