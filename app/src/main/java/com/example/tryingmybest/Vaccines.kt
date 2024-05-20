package com.example.tryingmybest

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.madness.connections.vaccinations.SuspendedQueriesVaccinations
import com.example.tryingmybest.adapters.AdapterVaccines
import com.example.tryingmybest.data.DataVaccines
import com.example.tryingmybest.databinding.ActivityVaccinesBinding
import com.example.tryingmybest.db.files.vaccinations.VaccinationsData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Vaccines : AppCompatActivity() {

    private val vaccinesList = ArrayList<DataVaccines>()
    private lateinit var binding:ActivityVaccinesBinding
    private lateinit var adapter: AdapterVaccines
    private val db = FirebaseFirestore.getInstance()

    /**
     * Creates the activity.
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =   ActivityVaccinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AdapterVaccines(vaccinesList)
        binding.recyclerView.adapter = adapter

        val goUsers = findViewById<LinearLayout>(R.id.users)
        goUsers.setOnClickListener{goToUsers()}

//        fetchVaccinationsFromDatabase()

        db.collection("vaccinations")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val dataVaccines = document.toObject(DataVaccines::class.java)
                        // Ensure you parse doses and duration to Int
                        vaccinesList.add(dataVaccines)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Error getting documents: ", exception)
            }


        val editText =
            binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.off_white))
        editText.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText)
                return true
            }
        })
    }

    /**
     * Filters the vaccination history based on the search query.
     * @param query The search query entered by the user.
     */
    private fun filterItems(query: String?) {
        adapter.filter.filter(query)
    }

    private fun goToUsers() {
        val intent = Intent(this, Users::class.java)
        startActivity(intent)
        finish()
    }

    //Function to get the scheduled vaccinations from the database. Works to long to be implemented in the app.
//    @SuppressLint("NotifyDataSetChanged")
//    private fun fetchVaccinationsFromDatabase() {
//        lifecycleScope.launch {
//            try {
//                val suspendedQueriesVa: Set<VaccinationsData?>? = withContext(Dispatchers.IO) {
//                    SuspendedQueriesVaccinations.getAllVaccinations()
//                }
//
//                // Convert Set<VaccinationsData?>? to Collection<DataVaccines>
//                val dataVaccinesList: Collection<DataVaccines> = suspendedQueriesVa?.mapNotNull { vaccinationsData ->
//                    vaccinationsData?.let {
//                        DataVaccines(
//                            name = it.vaxAddInfoDataId?.toString() ?: "", // Adjust this as needed
//                            doses = it.noOfDoses ?: 0,
//                            duration = it.timeBetweenDoses ?: 0
//                        )
//                    }
//                } ?: emptyList()
//
//                // Add the converted data to vaccinesList
//                vaccinesList.addAll(dataVaccinesList)
//                adapter.notifyDataSetChanged()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Handle the error, e.g., show a message to the user
//            }
//        }
//    }


}
