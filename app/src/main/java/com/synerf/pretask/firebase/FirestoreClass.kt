package com.synerf.pretask.firebase

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.synerf.pretask.activities.MainActivity
import com.synerf.pretask.activities.SignInActivity
import com.synerf.pretask.activities.SignUpActivity
import com.synerf.pretask.models.User
import com.synerf.pretask.utils.Constants

/**
 * A custom class where we will add the operation performed for the firestore database.
 */
class FirestoreClass {

    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document is the User ID.
            .document(getCurrentUserId())
            // Here the userInfo are the fields and the SetOption is set to merge.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here we call the function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    /**
     * function to get signed in user data
     */
    fun signInUser(activity: Activity) {
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document is the User ID.
            .document(getCurrentUserId())
            // to get the user info
            .get()
            // on success
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser)
                        }
                    }
                }
            }

            // on failure
            .addOnFailureListener { e ->
                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    "SignInUser",
                    "Error writing document",
                    e
                )
            }
    }

    /**
     * function to get current user id
     */
    fun getCurrentUserId(): String {
        var currentUSer = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUSer != null) {
            currentUserID = currentUSer.uid
        }
        return currentUserID
    }
}