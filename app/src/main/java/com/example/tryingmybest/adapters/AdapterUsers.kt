package com.example.tryingmybest.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.madness.connections.queries.user.SuspendedQueriesUser
import com.example.tryingmybest.R
import com.example.tryingmybest.data.DataUser
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Adapter class for the RecyclerView in the Users screen.
 * This class handles the creation of user items and their corresponding actions.
 */
class AdapterUsers(private var usersList: List<DataUser>) :
    RecyclerView.Adapter<AdapterUsers.UserViewHolder>(), Filterable {

    private var filteredList: List<DataUser> = usersList

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: EditText = itemView.findViewById(R.id.name)
        var delete: ShapeableImageView = itemView.findViewById(R.id.trash)
    }

    /**
     * Creates a new ViewHolder object whenever the RecyclerView needs a new one.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    /**
     * Binds the data to the ViewHolder object at the specified position.
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item: DataUser = filteredList[position]
        holder.name.isEnabled = false
        holder.name.setText(item.username)

        // Set click listeners for delete and edit actions
        holder.delete.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, position)

        }

    }

    /**
     * Returns the number of items in the list.
     */
    override fun getItemCount(): Int {
        return filteredList.size
    }

    /**
     * Returns a filter that can be used to constrain data with a filtering pattern.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString().lowercase(Locale.getDefault())
                filteredList = if (charString.isEmpty()) {
                    usersList
                } else {
                    usersList.filter { user ->
                        user.username.lowercase(Locale.getDefault()).contains(charString)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                filteredList = filterResults?.values as? List<DataUser> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }


    /**
     * Shows a dialog to confirm the deletion of a user.
     */
    private fun showDeleteConfirmationDialog(context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the item if the user confirms
                deleteItem(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes a user from the list and Firestore.
     */
    private fun deleteItem(position: Int) {
        val deletedUser = filteredList[position]

        // Remove the user from the filtered list
        filteredList = filteredList.toMutableList().apply {
            removeAt(position)
        }

        // Also remove the user from the original list
        val indexInOriginalList = usersList.indexOf(deletedUser)
        if (indexInOriginalList != -1) {
            usersList.toMutableList().removeAt(indexInOriginalList)
        }

        // Notify adapter about item removal
        notifyItemRemoved(position)

        // Delete the corresponding document from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("email", deletedUser.email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Document successfully deleted
                            println("DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            // Deletion failed
                            println("Error deleting document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Query failed
                println("Error querying document: $e")
            }

        //delete the user from php
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                SuspendedQueriesUser.deleteUser(deletedUser.email)
            }
        }
    }


}
