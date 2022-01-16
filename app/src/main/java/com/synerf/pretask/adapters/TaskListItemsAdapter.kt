package com.synerf.pretask.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synerf.pretask.R
import com.synerf.pretask.activities.TaskListActivity
import com.synerf.pretask.models.Task

open class TaskListItemsAdapter(
    private val context: Context, private var list:  ArrayList<Task>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Inflates the item view which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        // Here the layout params are converted dynamically according to the screen size as
        // width is 70% and height is wrap_content.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins(15.toDp().toPx(), 0, 40.toDp().toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // all view variables required
        val tvAddTaskList = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
        val llTaskItem = holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item)
        val tvTaskListTitle = holder.itemView.findViewById<TextView>(R.id.tv_task_list_title)
        val cvAddTaskListName = holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name)
        val ibCloseListName = holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name)
        val ibDoneListName = holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name)
        val etTaskListName = holder.itemView.findViewById<EditText>(R.id.et_task_list_name)
        val ibEditListName = holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name)
        val etEditTaskListName = holder.itemView.findViewById<TextView>(R.id.et_edit_task_list_name)
        val llTitleView = holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view)
        val cvEditTaskListName = holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name)
        val ibCloseEditableView = holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)
        val ibDoneEditListName = holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
        val ibDeleteList = holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list)
        val tvAddCard = holder.itemView.findViewById<TextView>(R.id.tv_add_card)
        val cvAddCard = holder.itemView.findViewById<CardView>(R.id.cv_add_card)
        val ibCloseCardName = holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name)
        val ibDoneCardName = holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name)
        val etCardName = holder.itemView.findViewById<EditText>(R.id.et_card_name)
        val rvCardList = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)

        val model = list[position]

        if (holder is MyViewHolder) {
            // show add task list textview and hide task item layout for last item
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                // vice versa
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            // show title
            tvTaskListTitle.text = model.title

            // when clicked on add_task_list textview
            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }

            // when clicked on close_list_name button (cross button)
            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            // when clicked on done_list_name button (check button)
            ibDoneListName.setOnClickListener {
                val listName = etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please enter list name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // when clicked on edit_list_name button
            ibEditListName.setOnClickListener {
                etEditTaskListName.text = model.title
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }

            // when clicked close_editable_view button (cross button)
            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }

            // when clicked on done_edit_list_name button
            ibDoneEditListName.setOnClickListener {
                val listName = etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please enter list name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // when clicked on delete_task_list
            ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            // when clicked on add_card
            tvAddCard.setOnClickListener {
                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }

            // when clicked on close_card_name button
            ibCloseCardName.setOnClickListener {
                tvAddCard.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }

            // when clicked on done_card_name button (check button)
            ibDoneCardName.setOnClickListener {
                val cardName = etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please enter card name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // setup recyclerview for cardsList
            rvCardList.layoutManager = LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context, model.cards)
            rvCardList.adapter = adapter

            // handle click events on cards
            adapter.setOnClickListener(
                object: CardListItemsAdapter.OnClickListener {
                    override fun onClick(cardPosition: Int) {
                        if (context is TaskListActivity) {
                            context.cardDetails(position, cardPosition)
                        }
                    }
                }
            )
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * function to show the Alert Dialog for deleting the task list.
     */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    /**
     * function to get density pixel from pixel
     */
    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    /**
     * function to get pixel from density pixel
     */
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    /**
     * Custom view holder
     */
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}