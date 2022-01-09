package com.synerf.pretask.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivityMyProfileBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.User
import com.synerf.pretask.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityMyProfileBinding

    // companion object to declare the constants.
    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    // global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileURI: Uri? = null

    // global variable for user details.
    private lateinit var mUserDetails: User

    // global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup action bar
        setupActionBar()

        // load user data
        FirestoreClass().loadUserData(this@MyProfileActivity    )

        // when clicked on profile image (also checking permissions)
        binding.ivProfileUserImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        // when clicked on update button
        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileURI != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    /**
     * function to setup actionBar
     */
    private fun setupActionBar() {
        // initialized toolbar with findViewById because direct binding cannot be done
        val toolbarMyProfileActivity = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbarMyProfileActivity.setNavigationOnClickListener {
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
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
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
     * function to open gallery to choose image
     */
    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /**
     * execute when we get result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
            requestCode == PICK_IMAGE_REQUEST_CODE &&
            data!!.data != null) {

            mSelectedImageFileURI = data.data!!

            // set image
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(mSelectedImageFileURI.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * function to set user details in UI
     */
    fun setUserDataInUI(user: User) {
        mUserDetails = user
        // set profile image in circularImageView
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        // set name, email and mobile in textViews
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    /**
     * A function to update the user profile details into the database.
     */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }
        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }

    /**
     * function to upload user image in firebase storage
     */
    private fun uploadUserImage() {
        // show progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileURI != null) {

            // create a reference (prepare file for upload)
            val sRef: StorageReference =
                FirebaseStorage.getInstance()
                    .reference
                    .child("USER_IMAGE" + System.currentTimeMillis() +
                            "." + getFileExtension(mSelectedImageFileURI))

            // put i.e. upload the image in firebase storage
            sRef.putFile(mSelectedImageFileURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i("Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    // use the downloadable url from firebase storage
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.i("Downloadable image url", uri.toString())
                            mProfileImageURL = uri.toString()

                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                    hideProgressDialog()
                }
        }
    }

    /**
     * function to know the extension of uri and return it
     */
    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    /**
     * A function to notify that the user profile is updated successfully.
     */
    fun profileUpdateSuccess() {
        // hide progress dialog
        hideProgressDialog()

        // this will call on activity result in main activity
        setResult(Activity.RESULT_OK)

        finish()
    }
}