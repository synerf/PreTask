package com.synerf.pretask.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivityCreateBoardBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.Board
import com.synerf.pretask.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateBoardBinding

    // global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileURI: Uri? = null

    // Global variable to hold name of the user
    private lateinit var mUserName: String

    // global variable for board image
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup action bar
        setupActionBar()

        // get name of the user from intent
        if(intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        // when clicked on board image view
        binding.ivBoardImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        // when clicked on create board button
        binding.btnCreate.setOnClickListener {
            if (mSelectedImageFileURI != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    /**
     * function to setup actionBar
     */
    private fun setupActionBar() {
        // initialized toolbar with findViewById because direct binding cannot be done
        val toolBarCreateBoardActivity = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolBarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        toolBarCreateBoardActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * handling permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(
                    this,
                    "You have denied the permissions, you can allow the permissions from settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * execute when we get result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
            requestCode == Constants.PICK_IMAGE_REQUEST_CODE &&
            data!!.data != null) {

            mSelectedImageFileURI = data.data!!

            // set image
            try {
                Glide
                    .with(this)
                    .load(Uri.parse(mSelectedImageFileURI.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * function to upload board image in firebase storage
     */
    private fun uploadBoardImage() {
        // show progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // create a reference (prepare file for upload)
        val sRef: StorageReference =
            FirebaseStorage.getInstance()
                .reference
                .child("BOARD_IMAGE" + System.currentTimeMillis() +
                        "." + Constants.getFileExtension(this, mSelectedImageFileURI))

        // put i.e. upload the image in firebase storage
        sRef.putFile(mSelectedImageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.i("Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                // use the downloadable url from firebase storage
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.i("Downloadable image url", uri.toString())
                        mBoardImageURL = uri.toString()

                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
    }

    /**
     * function to create board
     */
    private fun createBoard() {
        // variable for assigned users array to border
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        // add current user to the array
        assignedUsersArrayList.add(getCurrentUserID())

        // make a board object
        val board = Board(
            binding.etBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        // finally, create board from fireStore class
        FirestoreClass().createBoard(this, board)
    }

    /**
     * function to execute when board is created
     */
    fun boardCreatedSuccessfully() {
        // hide progress dialog
        hideProgressDialog()

        // this will call on activity result in main activity
        setResult(Activity.RESULT_OK)
        finish()
    }
}