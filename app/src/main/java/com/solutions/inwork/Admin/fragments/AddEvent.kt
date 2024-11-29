package com.solutions.inwork.Admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.solutions.inwork.R
import java.text.SimpleDateFormat
import java.util.*

class AddEvent : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var editText: EditText
    private lateinit var savebutton: Button
    private lateinit var databaseReference: DatabaseReference
    private var selectedDate: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_event, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        editText = view.findViewById(R.id.editText)
        savebutton = view.findViewById(R.id.button)

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance("https://inwork-480a5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Calendar")

        Log.d("DatabaseReference", "Reference path: ${databaseReference.toString()}")

        // Set calendar view listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth" // Use a delimiter for date format
            calendarClicked(selectedDate)
        }

        // Set save button click listener
        savebutton.setOnClickListener {
            buttonSaveEvent()
        }

        return view
    }

    private fun calendarClicked(date: String) {
        databaseReference.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventTitle = snapshot.value?.toString() ?: "No Event"
                editText.setText(eventTitle)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun buttonSaveEvent() {
        val eventTitle = editText.text.toString().trim()

        if (eventTitle.isNotEmpty()) {
            databaseReference.child(selectedDate).setValue(eventTitle)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Data saved successfully
                        Toast.makeText(requireContext(), "Event saved successfully!", Toast.LENGTH_SHORT).show()
                        editText.setText("") // Clear EditText after saving
                    } else {
                        // Failed to save data
                        Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show()
                        Log.e("AddEvent", "Error saving event", task.exception)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Log.e("AddEvent", "Error saving event", exception)
                    Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Please enter event title", Toast.LENGTH_SHORT).show()
        }
    }
}
