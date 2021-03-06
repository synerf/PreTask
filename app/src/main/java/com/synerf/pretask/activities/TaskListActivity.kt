package com.synerf.pretask.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synerf.pretask.R
import com.synerf.pretask.adapters.TaskListItemsAdapter
import com.synerf.pretask.databinding.ActivityTaskListBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.Board
import com.synerf.pretask.models.Card
import com.synerf.pretask.models.Task
import com.synerf.pretask.utils.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskListBinding

    // global variable for Board Details
    private lateinit var mBoardDetails: Board

    // global variable for board document id
    private lateinit var mBoardDocumentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        // Show progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))
        // get board details from database
        FirestoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    /**
     * function to setup actionBar
     */
    private fun setupActionBar() {
        // initialized toolbar with findViewById because direct binding cannot be done
        val toolbarTaskListActivity = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * function to create option menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * function to handle option selection
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * function to get result from activity and get board details if changes were made
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE ||
                requestCode == CARD_DETAILS_REQUEST_CODE) {
            // Show progress dialog
            showProgressDialog(resources.getString(R.string.please_wait))
            // get board details from database
            FirestoreClass().getBoardDetails(this, mBoardDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * function to start CardDetails activity
     */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    /**
     * function to execute after getting board details
     */
    fun boardDetails(board: Board) {
        mBoardDetails = board
        // hide progress dialog
        hideProgressDialog()
        // set action bar title as name of the board
        setupActionBar()

        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        val rvTaskList = findViewById<RecyclerView>(R.id.rv_task_list)
        rvTaskList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, board.taskList)
        rvTaskList.adapter = adapter
    }

    /**
     * A function to set the result of add or updating the task list.
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }

    /**
     * A function to get the task list name from the adapter class which
     * we will be using to create a new task list in the database.
     */
    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * function to update task list
     */
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * function to delete task list
     */
    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * function to add card to task list
     */
    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUsersList)
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(mBoardDetails.taskList[position].title, mBoardDetails.taskList[position].createdBy, cardsList)

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }
}