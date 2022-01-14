package com.synerf.pretask.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synerf.pretask.R
import com.synerf.pretask.adapters.MemberListItemsAdapter
import com.synerf.pretask.databinding.ActivityMembersBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.Board
import com.synerf.pretask.models.User
import com.synerf.pretask.utils.Constants

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding

    // global variable for Board Details
    private lateinit var mBoardDetails: Board

    // global variable for list of assigned members
    private lateinit var mAssignedMembersList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        // setup action bar
        setupActionBar()

        // show progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    /**
     * function to setup actionBar
     */
    private fun setupActionBar() {
        // initialized toolbar with findViewById because direct binding cannot be done
        val toolbarMembersActivity = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }
        toolbarMembersActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    /**
     * function to create option menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * function o handle option selection
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * function to show dialog for adding a member
     */
    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this,
                    "Please enter member email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     *
     */
    fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    /**
     * function to setup assigned members list into recyclerview.
     */
    fun setUpMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()

        // setup recycler view
        val rvMembersList = findViewById<RecyclerView>(R.id.rv_members_list)
        rvMembersList.layoutManager = LinearLayoutManager(this)
        rvMembersList.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rvMembersList.adapter = adapter

    }

    /**
     * A function to get the result of assigning the members.
     */
    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        setUpMembersList(mAssignedMembersList)
    }
}