package com.synerf.pretask.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synerf.pretask.R
import com.synerf.pretask.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private var mSelectedColor: String = ""
): Dialog(context) {

    // global variable for adapter
    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create a custom view and set it
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)

        // when clicked outside of view, close it
        setCanceledOnTouchOutside(true)
        // property to allow to cancel it
        setCancelable(true)

        // setup the recyclerview
        setupRecyclerView(view)
    }

    /**
     * function to setupRecyclerView
     */
    private fun setupRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = title

        val rvList = view.findViewById<RecyclerView>(R.id.rvList)
        rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        rvList.adapter = adapter

        // when clicked on adapter
        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    /**
     * abstract function for item selected
     */
    protected abstract fun onItemSelected(color: String)
}