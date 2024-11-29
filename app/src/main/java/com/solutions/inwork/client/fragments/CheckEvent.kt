package com.solutions.inwork.client.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.solutions.inwork.R
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*



class CheckEvent : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var textView: TextView
    private lateinit var databaseReference: DatabaseReference
    private var selectedDate: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_check_event, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        textView = view.findViewById(R.id.textView)

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance("https://inwork-480a5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Calendar")

        Log.d("DatabaseReference", "Reference path: ${databaseReference.toString()}")

        // Set calendar view listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth" // Use a delimiter for date format
            calendarClicked(selectedDate)
        }

        return view
    }

    private fun calendarClicked(date: String) {
        databaseReference.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventTitle = snapshot.value?.toString() ?: "No Event"
                textView.setText(eventTitle)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}