package com.example.tryingmybest

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.madness.connections.queries.vacxaddinfo.SuspendedQueriesVaxAddInfo
import com.example.madness.connections.vaccinations.SuspendedQueriesVaccinations
import com.example.tryingmybest.db.files.entities.VaxStatus
import com.example.tryingmybest.db.files.scheduled.SuspendedQueriesVaxScheduled
import com.example.tryingmybest.db.files.scheduled.VaxScheduledData
import com.example.tryingmybest.db.files.vacxaddinfo.VaxAddInfoData
import com.example.tryingmybest.notifications.Notifications
import com.example.tryingmybest.notifications.Notify
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.sql.Date

/**
 * Add activity allows users to schedule a new appointment for a vaccine.
 * It verifies the user input and saves appointment data to Firestore database when successful.
 */
class Add : DialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var name: String
    private lateinit var dropdownMenu: Spinner
    private var isVaccineChosen = false
    private lateinit var saveButton: Button
    private lateinit var date: TextView
    private lateinit var addNotifTV: TextView
    private lateinit var setAlarmLayout: View
    private lateinit var showFormat: SimpleDateFormat
    private lateinit var sendDateFormat: SimpleDateFormat
    private lateinit var fullFormat: SimpleDateFormat
    private lateinit var fullSendFormat: SimpleDateFormat
    private var sendDate: String = ""
    private var sendNoti: String = ""
    private var lastDate: String = ""
    private var desc: String = ""
    private var doses: Long = 0
    private var duration: Long = 0
    private lateinit var email: String

    /**
     * Creates the view for the Add dialog fragment.
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add, container, false)

        val user = FirebaseAuth.getInstance().currentUser
        email = user?.email.toString()

        dropdownMenu = view.findViewById(R.id.dropdownMenu)
        date = view.findViewById(R.id.date)
        addNotifTV = view.findViewById(R.id.add_notif)
        setAlarmLayout = view.findViewById(R.id.SetAlarm)

        fullSendFormat = SimpleDateFormat("dd-MM-yyyy, HH:mm")
        fullFormat = SimpleDateFormat("EEE, MMM dd, yyyy HH:mm")
        showFormat = SimpleDateFormat("EEE, MMM dd, yyyy")
        sendDateFormat = SimpleDateFormat("dd-MM-yyyy")

        date.text = "Set appointment date"
        addNotifTV.text = "Set notification date"

        val intent = requireActivity().intent
        val userId = intent.getIntExtra("user_id", 0)

        setupDropdownMenu(userId)

        date.setOnClickListener {
            showDatePicker()
        }

        setAlarmLayout.setOnClickListener {
            val alertDialog = Notifications()
            alertDialog.show(parentFragmentManager, "NotificationsDialogFragment")
        }

        saveButton = view.findViewById(R.id.save)
        saveButton.setOnClickListener {
            if (sendNoti.isEmpty()) {
                Toast.makeText(requireContext(), "Please set a notification date and time.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    saveToFireStore()
                    scheduleNotification(sendNoti)

                    val dropdownResult = dropdown(userId)
                    try { //sending the data to Php database
                        lifecycleScope.launch {
                            val result = dropdownResult
                            if (result != null) {
                                val (vaxId, userId, date) = result
                                val vaxScheduled = VaxScheduledData(vaxId, userId, date)
                                lifecycleScope.launch {
                                    SuspendedQueriesVaxScheduled.insertVaxScheduled(vaxScheduled)
                                }
                            }
                        }}catch (e: CancellationException) {
                            Log.e(TAG, "Error getting dropdown result: ${e.message}")
                        }
                }
            }
        }

        return view
    }

    /**
     * Sets up the dropdown menu for selecting a vaccine.
     * @param userId The user ID of the current user.
     */
    private fun setupDropdownMenu(userId: Int) {
        lifecycleScope.launch {
            val addVaxInfo = SuspendedQueriesVaxAddInfo.getAllVaxAddInfo()

            val options = mutableListOf<String>()
            val vaccineMap = mutableMapOf<String, VaxAddInfoData>()
            options.add("Select Vaccine")
            for (document in addVaxInfo) {
                val optionName = document.vaxNameCompany
                optionName?.let {
                    options.add(it)
                    vaccineMap[it] = document
                }
            }
            val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, options)
            adapter.setDropDownViewResource(R.layout.item_dropdown)
            dropdownMenu.adapter = adapter

            dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SimpleDateFormat")
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    isVaccineChosen = position != 0
                    enableSaveButton()
                    name = dropdownMenu.selectedItem.toString()
                    val selectedDocument = vaccineMap[name]
                    if (selectedDocument != null) {
                        lifecycleScope.launch {
                            val vaxId = SuspendedQueriesVaxAddInfo.getVaxId(name)
                            val noOfDoses = SuspendedQueriesVaccinations.getNumberOfDoses(vaxId)
                            val timeBetweenDoses = SuspendedQueriesVaccinations.getTimeBetweenDoses(vaxId)

                            doses = noOfDoses.toLong()
                            duration = timeBetweenDoses.toLong()

                            var latestNextDose: java.util.Date? = null
                            var latestAppointmentDocument: DocumentSnapshot? = null

                            db.collection("appointments")
                                .whereEqualTo("name", name)
                                .get()
                                .addOnCompleteListener { appointmentDocuments ->
                                    if (appointmentDocuments.isSuccessful) {
                                        for (appointmentDocument in appointmentDocuments.result!!) {
                                            val nextDose = appointmentDocument.getDate("nextDose")
                                            if (nextDose != null && (latestNextDose == null || nextDose.after(latestNextDose))) {
                                                latestNextDose = nextDose
                                                latestAppointmentDocument = appointmentDocument
                                                sendDate = nextDose.toString()
                                            }
                                        }

                                        if (latestAppointmentDocument != null) {
                                            lastDate = sendDateFormat.format(latestNextDose!!)
                                            sendDate = sendDateFormat.format(latestNextDose!!)
                                            updateProposedDate()
                                        }

                                        updateProposedDate()
                                    } else {
                                        Log.e(TAG, "Error getting documents: ${appointmentDocuments.exception}")
                                    }
                                }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where nothing is selected
                }
            }
        }
    }

    /**
     * Suspends the coroutine until the user selects a vaccine from the dropdown menu.
     * @param userId The user ID of the current user.
     * @return A Triple containing the vaccine ID, user ID, and the appointment date.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun dropdown(userId: Int): Triple<Int, Int, java.sql.Date>? {
        return suspendCancellableCoroutine { continuation ->
            dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SimpleDateFormat")
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position != 0) {
                        isVaccineChosen = true
                        enableSaveButton()
                        name = dropdownMenu.selectedItem.toString()
                        lifecycleScope.launch {
                            val vaxId = SuspendedQueriesVaxAddInfo.getVaxId(name)
                            val noOfDoses = SuspendedQueriesVaccinations.getNumberOfDoses(vaxId)
                            val timeBetweenDoses = SuspendedQueriesVaccinations.getTimeBetweenDoses(vaxId)

                            doses = noOfDoses.toLong()
                            duration = timeBetweenDoses.toLong()

                            var latestNextDose: java.util.Date? = null
                            var latestAppointmentDocument: DocumentSnapshot? = null

                            db.collection("appointments")
                                .whereEqualTo("name", name)
                                .get()
                                .addOnCompleteListener { appointmentDocuments ->
                                    if (appointmentDocuments.isSuccessful) {
                                        for (appointmentDocument in appointmentDocuments.result!!) {
                                            val nextDose = appointmentDocument.getDate("nextDose")
                                            if (nextDose != null && (latestNextDose == null || nextDose.after(latestNextDose))) {
                                                latestNextDose = nextDose
                                                latestAppointmentDocument = appointmentDocument
                                                sendDate = nextDose.toString()
                                            }
                                        }

                                        if (latestAppointmentDocument != null) {
                                            lastDate = sendDateFormat.format(latestNextDose!!)
                                            sendDate = sendDateFormat.format(latestNextDose!!)
                                            updateProposedDate()
                                        }

                                        updateProposedDate()
                                        if (!continuation.isCompleted) {
                                            continuation.resume(Triple(vaxId, userId, java.sql.Date(sendDateFormat.parse(sendDate)!!.time))) {
                                                // Handle cancellation if needed
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "Error getting documents: ${appointmentDocuments.exception}")
                                        if (!continuation.isCompleted) {
                                            continuation.resume(null) {
                                                // Handle cancellation if needed
                                            }
                                        }
                                    }
                                }
                        }
                    } else {
                        if (!continuation.isCompleted) {
                            continuation.resume(null) {
                                // Handle cancellation if needed
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    if (!continuation.isCompleted) {
                        continuation.resume(null) {
                            // Handle cancellation if needed
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the proposed appointment date based on the selected vaccine.
     */
    private fun updateProposedDate() {
        val calendar = Calendar.getInstance()

        if (sendDate.isEmpty()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            try {
                calendar.time = sendDateFormat.parse(sendDate)!!
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            calendar.add(Calendar.DAY_OF_YEAR, duration.toInt())
        }

        val proposedDate = showFormat.format(calendar.time)
        date.text = proposedDate
        sendDate = sendDateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dayBefore = fullFormat.format(calendar.time)
        addNotifTV.text = dayBefore
    }


    /**
     * Shows the date picker dialog for selecting the appointment date.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)

                val proposedDate = showFormat.format(calendar.time)
                date.text = proposedDate

                sendDate = sendDateFormat.format(calendar.time)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    /**
     * Enables the save button when a vaccine is chosen.
     */
    private fun enableSaveButton() {
        if (isVaccineChosen) {
            context?.let {
                saveButton.setBackgroundColor(
                    ContextCompat.getColor(
                        it,
                        R.color.green
                    )
                )
            }
            saveButton.isEnabled = true
        } else {
            saveButton.isEnabled = false
        }
    }

    /**
     * Saves the appointment data to Firestore database.
     */
    private suspend fun saveToFireStore() {

        val appointmentData: HashMap<String, Any> = if (lastDate.isNotEmpty()) {
            hashMapOf(
                "name" to name,
                "email" to email,
                "nextDose" to sendDateFormat.parse(sendDate)!!,
                "lastDose" to sendDateFormat.parse(lastDate)!!,
                "desc" to desc
            )
        } else {
            hashMapOf(
                "name" to name,
                "email" to email,
                "nextDose" to sendDateFormat.parse(sendDate)!!,
                "desc" to desc
            )
        }

        db.collection("appointments").add(appointmentData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val vaccineRef = db.collection("vaccinations").whereEqualTo("name", name)

                    vaccineRef.get().addOnSuccessListener { querySnapshot ->
                        db.runTransaction { transaction ->
                            for (document in querySnapshot.documents) {
                                val currentDoses = document.getLong("doses") ?: 0
                                val newDoses = currentDoses - 1
                                transaction.update(document.reference, "doses", newDoses)
                            }
                        }.addOnSuccessListener {
                            Log.d(TAG, "Transaction successfully committed.")
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Transaction failed: ", e)
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error getting documents: ", e)
                    }

                } else {
                    Log.e(TAG, "Error saving appointment data: ", task.exception)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error saving appointment data: ", exception)
            }

        val notificationData = hashMapOf(
            "name" to name,
            "date" to sendNoti,
            "email" to email
        )

        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { _ ->
                // Handle failure
            }

    }

    /**
     * Sets the selected date and time for the notification.
     * @param selectedDate The selected date for the notification.
     * @param selectedTime The selected time for the notification.
     */
    @SuppressLint("SetTextI18n")
    fun setSelectedDateTime(selectedDate: String, selectedTime: String) {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Log.e(TAG, "Selected date or time is empty.")
            return
        }

        sendNoti = "$selectedDate, $selectedTime"

        try {
            val dateObj = sendDateFormat.parse(selectedDate)
            val sqlDate = if (dateObj != null) Date(dateObj.time) else Date(Calendar.getInstance().timeInMillis)

            val formattedDate = showFormat.format(sqlDate)

            addNotifTV.text = "$formattedDate, $selectedTime"
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e(TAG, "Error parsing selected date: ${e.message}")
        }
    }

    /**
     * Schedules a notification for the selected date and time.
     * @param selectedDateTime The selected date and time for the notification.
     */
    private fun scheduleNotification(selectedDateTime: String) {
        if (selectedDateTime.isEmpty()) {
            Log.e(TAG, "Selected date time is empty.")
            return
        }

        val intentNot = Intent(requireContext(), Notify::class.java).apply {
            putExtra("vaccinations", name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intentNot,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()

        val selectedDateTimeMillis = try {
            fullSendFormat.parse(selectedDateTime)?.time ?: throw ParseException("Unparseable date", 0)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e(TAG, "Error parsing date: ${e.message}")
            return
        }

        calendar.timeInMillis = selectedDateTimeMillis

        val delayMillis = selectedDateTimeMillis - System.currentTimeMillis()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayMillis,
            pendingIntent
        )
    }
}
