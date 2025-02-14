package com.example.tryingmybest.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tryingmybest.R
import com.example.tryingmybest.data.DataVaxx
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
/**
 * Adapter for displaying vaccination history in a RecyclerView.
 * This adapter takes a list of [DataVaxx] objects and displays them in the RecyclerView.
 * It also provides filtering functionality to search for specific vaccine names.
 * @param historyList The list of vaccination data to be displayed.
 */
class AdapterHistory(private val historyList: List<DataVaxx>) :
    RecyclerView.Adapter<AdapterHistory.HistoryViewHolder>(), Filterable {

        //filtered list to allow for the search view changeable list size
    private var filteredList: List<DataVaxx> = historyList
    private var isEditMode: Boolean = false

    /**
     * ViewHolder class for holding views of each item in the RecyclerView.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        var dateNDTV: TextView = itemView.findViewById(R.id.dateND)
        var dateADMTV: TextView = itemView.findViewById(R.id.dateADM)
        var descTV: TextView = itemView.findViewById(R.id.desc)
        var linearLayout: LinearLayout = itemView.findViewById(R.id.stable)
        var adminLayout: LinearLayout = itemView.findViewById(R.id.adminLayout)
        var expendableLayout: RelativeLayout = itemView.findViewById(R.id.Expandable)
        var delete: ShapeableImageView = itemView.findViewById(R.id.delete)
        var edit: ShapeableImageView = itemView.findViewById(R.id.edit)
    }

    /**
     * Create a new ViewHolder object by inflating the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_vaccine, parent, false)
        return HistoryViewHolder(view)
    }

    /**
     * Bind data to the views in each item of the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item: DataVaxx = filteredList[position]
        holder.name.text = item.name

        // formats
        val nextDoseDate = item.nextDose?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
        }
        holder.dateNDTV.text = nextDoseDate

        // formats when isn't null, make the linear layout invisible when is null.
        if (item.lastDose != null) {
            val lastDoseDate = item.lastDose?.let {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
            }
            holder.adminLayout.visibility = View.VISIBLE
            holder.dateADMTV.text = lastDoseDate
        } else {
            holder.adminLayout.visibility = View.GONE
        }

        holder.descTV.text = item.desc

        val isExpandable: Boolean = historyList[position].expandable
        holder.expendableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

        holder.linearLayout.setOnClickListener {
            val version = historyList[position]
            version.expandable = !item.expandable
            notifyItemChanged(position)
        }
        holder.delete.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, position)
        }

        holder.edit.setOnClickListener {
            isEditMode = !isEditMode
            if (!isEditMode) {
                holder.edit.setImageResource(R.drawable.edit_off)
                val newNextDose = filteredList[position].nextDose
                val newLastDose = filteredList[position].lastDose
                updateDataInFirestoreWithDates(newNextDose, newLastDose)
            }else{
                holder.edit.setImageResource(R.drawable.edit)

            }
            notifyDataSetChanged()
        }

        holder.dateNDTV.setOnClickListener {
            if (isEditMode) {
                openDatePicker(holder.itemView.context) { selectedDate ->
                    holder.dateNDTV.text =
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)
                    filteredList[position].nextDose = Date(selectedDate)
                }
            }
        }

        holder.dateADMTV.setOnClickListener {
            if (isEditMode) {
                openDatePicker(holder.itemView.context) { selectedDate ->
                    holder.dateADMTV.text =
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)
                    filteredList[position].lastDose = Date(selectedDate)
                }
            }
        }
    }

    /**
     * return the size of the List
     */
    override fun getItemCount(): Int {
        return filteredList.size
    }

    /**
     * filter the items by the name
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                filteredList = if (charString.isEmpty()) {
                    historyList
                } else {
                    historyList.filter { item ->
                        item.name.lowercase(Locale.getDefault()).contains(
                            charString.lowercase(
                                Locale.getDefault()
                            )
                        )
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                filteredList = filterResults?.values as? List<DataVaxx> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Updates the nextDose and lastDose fields in Firestore with the new values.
     */
    private fun updateDataInFirestoreWithDates(newNextDose: Date?, newLastDose: Date?) {
        val db = FirebaseFirestore.getInstance()
        val appointmentsCollection = db.collection("appointments")
        val query = appointmentsCollection
            .whereEqualTo("nextDose", newNextDose)
            .whereEqualTo("lastDose", newLastDose)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val appointmentDocument = querySnapshot.documents.first()
                    appointmentDocument.reference.update(
                        mapOf(
                            "nextDose" to newNextDose,
                            "lastDose" to newLastDose
                        )
                    )
                        .addOnSuccessListener {
                            println("DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating document: $e")
                        }
                } else {
                    println("No matching document found.")
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
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
                deleteItem(position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes the item at the given position from the list and Firestore.
     */
    private fun deleteItem(position: Int) {
        val deletedUser = filteredList[position]
        filteredList = filteredList.toMutableList().apply {
            removeAt(position)
        }

        val indexInOriginalList = historyList.indexOf(deletedUser)
        if (indexInOriginalList != -1) {
            historyList.toMutableList().removeAt(indexInOriginalList)
        }

        notifyItemRemoved(position)
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            println("DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            println("Error deleting document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error querying document: $e")
            }
    }

    /**
     * Opens a DatePickerDialog to select a date.
     */
    private fun openDatePicker(context: Context, onDateSelected: (selectedDate: Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

}
