package com.synerf.pretask.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivityBaseBinding
import com.synerf.pretask.databinding.DialogProgressBinding

open class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    private var doubleBackToExitPressesOnce = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * This function is used to show the progress dialog with the title and message to user.
     */
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    /**
     * fun to hide progress dialog
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    /**
     * function to get current user id (uid)
     */
    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    /**
     * function to exit when double pressed back
     */
    fun doubleBackToExit() {
        if(doubleBackToExitPressesOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressesOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressesOnce = false
        }, 2000)
    }

    /**
     * function to show an error snackBar
     */
    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))

        snackBar.show()
    }
}