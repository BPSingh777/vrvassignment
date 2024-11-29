package com.solutions.inwork.client.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.solutions.inwork.R
import com.solutions.inwork.databinding.FragmentContactUsBinding


class ContactUsFragment : Fragment() {

    private lateinit var binding : FragmentContactUsBinding
    private var PHONE_NUMBER = "+918002256140"

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactUsBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment


        binding.emailbtn.setOnClickListener {

            showEditTextDialog()
        }


        binding.Phonebtn.setOnClickListener {
//            val phone_intent = Intent(Intent.ACTION_CALL)
//            phone_intent.data = Uri.parse("tel:$PHONE_NUMBER")
//            startActivity(phone_intent)

            makePhoneCall()
        }




        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun makePhoneCall() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // The permission is granted, make the phone call
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$PHONE_NUMBER")
            startActivity(intent)
        } else {
            // The permission is not granted, request it from the user
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.CALL_PHONE),
                100
            )
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showEditTextDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_email, null)
        val SubjectEMail = dialogView.findViewById<EditText>(R.id.SubjectEmail)
        val BodyEmail = dialogView.findViewById<EditText>(R.id.BodyEmail)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Enter your Query")

        dialogBuilder.setPositiveButton("Send") { _, _ ->
            val enteredSubject = SubjectEMail.text.toString()
            val enteredBody = BodyEmail.text.toString()

            sendEmail(enteredSubject,enteredBody)


            // Handle the text entered in EditText and perform any necessary actions
            // For example, you can save the text to a database or display it on the screen.
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun sendEmail(subject: String,body : String) {
        val recipients = arrayOf("invyusolutions@gmail.com")
        val subject = subject
        val body = body
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, recipients)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(
                Intent.createChooser(intent, "Choose an email client:"),
                101
            )
        } else {
            // Handle case when no email client is available on the device
            // For example, you could show a Toast or an AlertDialog.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                // Email was sent successfully
                Toast.makeText(requireContext(), "Email sent successfully!", Toast.LENGTH_SHORT).show();
            } else {
                // Email sending failed or was canceled
                Toast.makeText(requireContext(), "Failed to send email.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, make the phone call
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + PHONE_NUMBER)
                startActivity(intent)
            } else {
                // Permission was denied, show a Toast or handle the denial
                Toast.makeText(
                    requireContext(),
                    "Phone call permission denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}