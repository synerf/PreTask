package com.synerf.pretask.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.synerf.pretask.activities.*
import com.synerf.pretask.models.Board
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
            // execute when success
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            // execute when failure
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
     * function to get board details
     */
    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            // get board
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching the boards", e)
            }
    }

    /**
     * A function to make an entry of a board in the firestore database.
     */
    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            // give a random id
            .document()
            // Here the board are the fields and the SetOption is set to merge.
            .set(board, SetOptions.merge())
            // execute when success
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(
                    activity,
                    "Board created successfully",
                    Toast.LENGTH_LONG
                ).show()
                activity.boardCreatedSuccessfully()
            }
            // execute when failure
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating the board", exception)
            }
    }

    /**
     * function to get boards from database
     */
    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            // condition (query)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            // get board
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                // populate ui with boards
                activity.populateBoardsListToUI(boardsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while fetching the boards", e)
            }
    }

    /**
     * function to create a task list in the board detail.
     */
    fun addUpdateTaskList(activity: TaskListActivity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully")
                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while updating the board", exception)
            }
    }

    /**
     * function to update user profile data
     */
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully!")
                Toast.makeText(
                    activity,
                    "Profile updated successfully!",
                    Toast.LENGTH_LONG
                ).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error when updating the profile")
                Toast.makeText(
                    activity,
                    "Error when updating the profile",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * function to load user data
     */
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document is the User ID.
            .document(getCurrentUserId())
            // to get the user info
            .get()
            // on success
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
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
                    is MyProfileActivity -> {
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