package com.synerf.pretask.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.synerf.pretask.R
import com.synerf.pretask.databinding.ActivityMainBinding
import com.synerf.pretask.firebase.FirestoreClass
import com.synerf.pretask.models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup action bar
        setupActionBar()

        // when clicked on item from drawer
        binding.navView.setNavigationItemSelectedListener(this)

        // again sign in the user when main activity starts
        FirestoreClass().loadUserData(this@MainActivity)
    }

    /**
     * function to setup actionBar with drawer icon
     */
    private fun setupActionBar() {
        // initialized toolbar_main_activity with findViewById because direct binding cannot be done
        val toolbarMainActivity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMainActivity)

        // set drawer icon on actionBar
        toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        // navigation listener to toggle drawer
        toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    /**
     * function to toggle drawer
     */
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * function to handle back presses
     */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    /**
     * function to handle item selection from drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            // if my profile item selected
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this@MainActivity, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            // if sign out item selected
            R.id.nav_sign_out -> {
                // sign out user from firebase
                FirebaseAuth.getInstance().signOut()

                // intent to move to intro activity,
                // with flags to show the intro activity state if there was one
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        // when user clicked on anything, close the drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * function to load user data after updating it in my profile activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * function to update navigation user details
     */
    fun updateNavigationUserDetails(user: User) {
        // set profile image in circularImageView
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))

        // set username in textView
        findViewById<TextView>(R.id.tv_username).text = user.name
    }
}