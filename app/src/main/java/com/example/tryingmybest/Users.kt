package com.example.tryingmybest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madness.connections.queries.user.SuspendedQueriesUser
import com.example.tryingmybest.adapters.AdapterUsers
import com.example.tryingmybest.data.DataUser
import com.example.tryingmybest.databinding.ActivityUsersBinding
import com.example.tryingmybest.db.files.user.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/**
 * Users activity displays a list of users in a RecyclerView. Users can search for
 * specific users using the SearchView.
 */
class Users : AppCompatActivity() {

    private val usersList = ArrayList<DataUser>()
    private lateinit var binding: ActivityUsersBinding
    private lateinit var adapter: AdapterUsers
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()

        val goVaccines = findViewById<LinearLayout>(R.id.vaccines)
        goVaccines.setOnClickListener{goToVaccines()}


        fetchDataFromDatabase()
    }

    /**
     * Sets up the RecyclerView with the AdapterUsers.
     */
    private fun setupRecyclerView() {
        adapter = AdapterUsers(usersList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Sets up the SearchView with a query listener.
     */
    private fun setupSearchView() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText)
                return true
            }
        })

        // Customize search view text color
        val editText = binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.off_white))
        editText.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    /**
     * Fetches user data from Firestore and updates the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDataFromFirestore() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    usersList.add(document.toObject(DataUser::class.java))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { _ ->
            }
    }

    /**
     * Fetches user data from the database and updates the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDataFromDatabase() {
        lifecycleScope.launch {
            val users = SuspendedQueriesUser.getAllUsers() // this is List<UserData>

            // Convert List<UserData> to Collection<DataUser>
            val dataUsersCollection: Collection<DataUser> = users.map { userData ->
                DataUser(username = userData.username)
            }.toCollection(ArrayList())

            usersList.addAll(dataUsersCollection)
            adapter.notifyDataSetChanged()
        }
    }


    /**
     * Filters the user list based on the search query.
     * @param query The search query entered by the user.
     */
    private fun filterItems(query: String?) {
        adapter.filter.filter(query)
    }

    /**
     * Navigates to the Vaccines activity.
     */
    private fun goToVaccines() {
        val intent = Intent(this, Vaccines::class.java)
        startActivity(intent)
        finish()
    }
}