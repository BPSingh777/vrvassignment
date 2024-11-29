package com.solutions.inwork

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.adapters.AlertDetailsAdapter
import com.solutions.inwork.Admin.adapters.AttendanceDetailsAdapter
import com.solutions.inwork.Admin.adapters.ScreenTimeDetailsReportAdapter
import com.solutions.inwork.Admin.dataclasses.AlertDetails
import com.solutions.inwork.Admin.dataclasses.CheckIn
import com.solutions.inwork.Admin.dataclasses.CheckInAndOut
import com.solutions.inwork.Admin.dataclasses.CheckOut
import com.solutions.inwork.Admin.dataclasses.ParsedAttendanceData
import com.solutions.inwork.Admin.dataclasses.ParsedData
import com.solutions.inwork.Admin.dataclasses.ParsedScreenData
import com.solutions.inwork.Admin.dataclasses.Screentimedata
import com.solutions.inwork.client.dataclasses.ClientNotification
import com.solutions.inwork.retrofitget.RetrofitGetClientNotifications
import com.solutions.inwork.retrofitget.RetrofitGetScreenDetailsEmployee
import com.solutions.inwork.retrofitget.RetrofitgetCheckOut
import com.solutions.inwork.retrofitget.RetrofitgetcheckIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PdfClass {

    private var notificationsList: MutableList<AlertDetails> = mutableListOf()
  //  private var progressDialog: ProgressDialog? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlertDetailsAdapter

    @SuppressLint("InflateParams", "SetTextI18n")


//    fun generatePdfAsync(context: Context, parsedData: ParsedData, name: String) {
//        GlobalScope.launch(Dispatchers.IO) {
//            generatePdf(context, parsedData, name)
//
//        }
//    }

    fun generatePdfAsync(context: Context, parsedData: ParsedData, name: String, progressDialog: ProgressDialog?, callback: (() -> Unit)? = null) {
        GlobalScope.launch() {
            generatePdf(context, parsedData, name)
            withContext(Dispatchers.Main) {
                // Dismiss progress dialog
                progressDialog?.dismiss()

                // Call the callback function if it's not nul, progressDialog: ProgressDialog?,
                callback?.invoke()
            }
        }
    }

    fun generateScreenPdfAsync(context: Context, parsedData: ParsedScreenData, name: String, progressDialog: ProgressDialog?,callback: (() -> Unit)? = null) {
        GlobalScope.launch() {
            generateScreenPdf(context, parsedData, name)
            withContext(Dispatchers.Main) {
                // Dismiss progress dialog
                progressDialog?.dismiss()

                // Call the callback function if it's not nul, progressDialog: ProgressDialog?,
                callback?.invoke()
            }
        }
    }

//    fun generateAttenancePdfAsync(context: Context, parsedData: ParsedAttendanceData, name: String) {
//        GlobalScope.launch(Dispatchers.IO) {
//            generateAttendancePdf(context, parsedData, name)
//
//        }
//    }

    fun generateAttenancePdfAsync(context: Context, parsedData: ParsedAttendanceData, name: String, progressDialog: ProgressDialog?,callback: (() -> Unit)? = null) {
        GlobalScope.launch() {
            generateAttendancePdf(context, parsedData, name)
            withContext(Dispatchers.Main) {
                // Dismiss progress dialog
                progressDialog?.dismiss()

                // Call the callback function if it's not nul, progressDialog: ProgressDialog?,
                callback?.invoke()
            }
        }
    }

    fun generatePdf(context: Context, parsedData: ParsedData, name: String) {
        val document = PdfDocument()
        val marginDp = 10
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 1)
            .create() // A4 size in points (1 point = 1/72 inch)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Inflate the layout for the first page
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.report_format, null) as LinearLayout

        // Set the parsed values to the layout views
        val headerTextView: TextView = layout.findViewById(R.id.headerTextView)
        val dateDurationTextView: TextView = layout.findViewById(R.id.dateDurationTextView)
        val employeeNameTextView: TextView = layout.findViewById(R.id.employeeNameTextView)
        val employeeIDTextView: TextView = layout.findViewById(R.id.employeeIDTextView)
        val companyIDTextView: TextView = layout.findViewById(R.id.companyIDTextView)
        val companyNameTextView: TextView = layout.findViewById(R.id.companyNameTextView)
        val checkInCountTextView: TextView = layout.findViewById(R.id.checkInCountTextView)
        val checkOutCountTextView: TextView = layout.findViewById(R.id.checkOutCountTextView)
        val alertDetailsListView: RecyclerView = layout.findViewById(R.id.alertDetailsListView)

        headerTextView.text = "INWORK – Monthly Report"
        dateDurationTextView.text = "Date: ${parsedData.date}"
        employeeNameTextView.text = "Employee Name: ${parsedData.employeeName}"
        employeeIDTextView.text = "Employee ID: ${parsedData.employeeID}"
        companyIDTextView.text = "Company ID: ${parsedData.companyID}"
        companyNameTextView.text = "Company Name: ${parsedData.companyName}"
        checkInCountTextView.text = "Check In Count: ${parsedData.checkInCount}"
        checkOutCountTextView.text = "Check Out Count: ${parsedData.checkOutCount}"

        // Set the adapter for the alertDetailsListView
        val layoutManager = LinearLayoutManager(context)
        alertDetailsListView.layoutManager = layoutManager
        val alerts = parsedData.alertDetailsList.subList(0, minOf(15, parsedData.alertDetailsList.size))
        val alertAdapter = AlertDetailsAdapter(alerts)
        alertDetailsListView.adapter = alertAdapter

// Measure and layout the view
        layout.measure(
            View.MeasureSpec.makeMeasureSpec(page.canvas.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(page.canvas.height, View.MeasureSpec.EXACTLY)
        )
        layout.layout(0, 0, page.canvas.width, page.canvas.height)

// Draw the layout on the canvas
        layout.draw(canvas)

// Finish the first page
        document.finishPage(page)

// Create subsequent pages for the remaining alerts
        if(parsedData.alertDetailsList.size > 15){
            val alertChunks = parsedData.alertDetailsList.subList(15, parsedData.alertDetailsList.size).chunked(30)

            val alertsPerPage = 30 // Number of alerts to display per page

// Create subsequent pages for the remaining alerts
            for (pageIndex in alertChunks.indices) {
                val nextPage = document.startPage(pageInfo)
                val nextCanvas = nextPage.canvas
                nextCanvas.drawColor(Color.WHITE)

                // Create a new RecyclerView for the remaining alerts
                val remainingAlerts = alertChunks[pageIndex]
                val remainingAlertDetailsListView = RecyclerView(context)
                val remainingLayoutManager = LinearLayoutManager(context)
                remainingAlertDetailsListView.layoutManager = remainingLayoutManager
                val remainingAlertDetailsAdapter = AlertDetailsAdapter(remainingAlerts)
                remainingAlertDetailsListView.adapter = remainingAlertDetailsAdapter

                // Set the number of alerts per page for the remaining RecyclerView
                remainingLayoutManager.isItemPrefetchEnabled = true
                remainingLayoutManager.initialPrefetchItemCount = alertsPerPage
                remainingAlertDetailsListView.setItemViewCacheSize(alertsPerPage)
                remainingAlertDetailsListView.recycledViewPool.setMaxRecycledViews(0, alertsPerPage)

                // Measure and layout the RecyclerView
                remainingAlertDetailsListView.measure(
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.height, View.MeasureSpec.EXACTLY)
                )
                remainingAlertDetailsListView.layout(0, 0, nextPage.canvas.width, nextPage.canvas.height)

                // Draw the RecyclerView on the canvas
                remainingAlertDetailsListView.draw(nextCanvas)

                // Finish the page
                document.finishPage(nextPage)
            }
        }

        val fileName = "${parsedData.employeeID}_monthly_report.pdf"
        val mimeType = "application/pdf"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

        uri?.let { fileUri ->
            val outputStream: OutputStream? = resolver.openOutputStream(fileUri)
            outputStream?.use { output ->
                try {
                    document.writeTo(output)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, mimeType)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        context.grantUriPermission(
                            "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
                            fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            // Handle the case where no PDF viewer app is installed on the device
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    document.close()
                }
            }
        }
//
//        val fileName = "${parsedData.employeeID}_monthly_report.pdf"
//        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
//        val file = File("$directory/$fileName")
//
//        try {
//            document.writeTo(FileOutputStream(file))
//            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//
//                val file = File(directory, fileName)
//                val fileUri = FileProvider.getUriForFile(context, "com.example.inwork.fileprovider", file)
//                val intent = Intent(Intent.ACTION_VIEW)
//
//                intent.setDataAndType(fileUri, "application/pdf")
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                context.grantUriPermission(
//                    "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
//                    fileUri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//
//                try {
//                    context.startActivity(intent)
//                } catch (e: ActivityNotFoundException) {
//                    e.printStackTrace()
//                    // Handle the case where no PDF viewer app is installed on the device
//                }
//
//
//                Log.d("pdf", "generated")
////            Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//            }
//        }catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            document.close()

    }



    fun generateAttendancePdf(context: Context, parsedData: ParsedAttendanceData, name: String) {
        val document = PdfDocument()
        val marginDp = 10
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 1)
            .create() // A4 size in points (1 point = 1/72 inch)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Inflate the layout for the first page
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.report_attendance_format, null) as LinearLayout

        // Set the parsed values to the layout views
        val headerTextView: TextView = layout.findViewById(R.id.headerTextView)
        val dateDurationTextView: TextView = layout.findViewById(R.id.dateDurationTextView)
        val employeeNameTextView: TextView = layout.findViewById(R.id.employeeNameTextView)
        val employeeIDTextView: TextView = layout.findViewById(R.id.employeeIDTextView)
        val companyIDTextView: TextView = layout.findViewById(R.id.companyIDTextView)
        val companyNameTextView: TextView = layout.findViewById(R.id.companyNameTextView)
        val checkInCountTextView: TextView = layout.findViewById(R.id.checkInCountTextView)
        val checkOutCountTextView: TextView = layout.findViewById(R.id.checkOutCountTextView)
        val avergaetimespent : TextView = layout.findViewById(R.id.AvgMonthlytime)
        val alertDetailsListView: RecyclerView = layout.findViewById(R.id.attendanceDetailsRecyclerView)

        headerTextView.text = "INWORK – Monthly Attendance Report"
        dateDurationTextView.text = "Date: ${parsedData.date}"
        employeeNameTextView.text = "Employee Name: ${parsedData.employeeName}"
        employeeIDTextView.text = "Employee ID: ${parsedData.employeeID}"
        companyIDTextView.text = "Company ID: ${parsedData.companyID}"
        companyNameTextView.text = "Company Name: ${parsedData.companyName}"
        checkInCountTextView.text = "Check In Count: ${parsedData.checkInCount}"
        checkOutCountTextView.text = "Check Out Count: ${parsedData.checkOutCount}"

        // Set the adapter for the alertDetailsListView
        val layoutManager = LinearLayoutManager(context)
        alertDetailsListView.layoutManager = layoutManager
        val alerts =
            parsedData.alertDetailsList.subList(0, minOf(20, parsedData.alertDetailsList.size))
        val alertAdapter = AttendanceDetailsAdapter(alerts)
        alertDetailsListView.adapter = alertAdapter


        // Calculate the total duration for all entries
        val totalDuration = (parsedData.alertDetailsList).sumOf{ alertdetails ->
            calculateTotalDuration(alertdetails.timeSpent)
        }

        // Count distinct dates
        val distinctDates = (parsedData.alertDetailsList).map { it.date }.distinct().size

        if (distinctDates != 0) {
            val averageMonthlyDuration = totalDuration.div(distinctDates)
            // Convert averageMonthlyDuration to hours, minutes, and seconds
            val hours = averageMonthlyDuration / 3600
            val minutes = (averageMonthlyDuration % 3600) / 60
            val seconds = averageMonthlyDuration % 60

            // Format the duration as "hh:mm:ss"
            val formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            avergaetimespent.text = "Average Time Spent: $formattedDuration"
        } else {
            // Handle the case when distinctDates is zero
            avergaetimespent.text = "Average Time Spent: N/A (No data available)"
        }

//        val averageMonthlyDuration = totalDuration.div(distinctDates)
//        // Convert averageMonthlyDuration to hours, minutes, and seconds
//        val hours = averageMonthlyDuration / 3600
//        val minutes = (averageMonthlyDuration % 3600) / 60
//        val seconds = averageMonthlyDuration % 60
//
//        // Format the duration as "hh:mm:ss"
//        val formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//        avergaetimespent.text = "Average Time Spent : $formattedDuration"
//


        // Measure and layout the view
        layout.measure(
            View.MeasureSpec.makeMeasureSpec(page.canvas.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(page.canvas.height, View.MeasureSpec.EXACTLY)
        )
        layout.layout(0, 0, page.canvas.width, page.canvas.height)

        // Draw the layout on the canvas
        layout.draw(canvas)

        // Finish the first page
        document.finishPage(page)

        // Create subsequent pages for the remaining alerts
        if(parsedData.alertDetailsList.size > 20){
            val alertChunks = parsedData.alertDetailsList.subList(20, parsedData.alertDetailsList.size).chunked(30)

            val alertsPerPage = 30 // Number of alerts to display per page

// Create subsequent pages for the remaining alerts
            for (pageIndex in alertChunks.indices) {
                val nextPage = document.startPage(pageInfo)
                val nextCanvas = nextPage.canvas
                nextCanvas.drawColor(Color.WHITE)

                // Create a new RecyclerView for the remaining alerts
                val remainingAlerts = alertChunks[pageIndex]
                val remainingAlertDetailsListView = RecyclerView(context)
                val remainingLayoutManager = LinearLayoutManager(context)
                remainingAlertDetailsListView.layoutManager = remainingLayoutManager
                val remainingAlertDetailsAdapter = AttendanceDetailsAdapter(remainingAlerts)
                remainingAlertDetailsListView.adapter = remainingAlertDetailsAdapter

                // Set the number of alerts per page for the remaining RecyclerView
                remainingLayoutManager.isItemPrefetchEnabled = true
                remainingLayoutManager.initialPrefetchItemCount = alertsPerPage
                remainingAlertDetailsListView.setItemViewCacheSize(alertsPerPage)
                remainingAlertDetailsListView.recycledViewPool.setMaxRecycledViews(0, alertsPerPage)

                // Measure and layout the RecyclerView
                remainingAlertDetailsListView.measure(
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.height, View.MeasureSpec.EXACTLY)
                )
                remainingAlertDetailsListView.layout(0, 0, nextPage.canvas.width, nextPage.canvas.height)

                // Draw the RecyclerView on the canvas
                remainingAlertDetailsListView.draw(nextCanvas)

                // Finish the page
                document.finishPage(nextPage)
            }
        }







        val fileName =  "${parsedData.employeeID}_attendance_report.pdf"

        val mimeType = "application/pdf"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

        uri?.let { fileUri ->
            val outputStream: OutputStream? = resolver.openOutputStream(fileUri)
            outputStream?.use { output ->
                try {
                    document.writeTo(output)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, mimeType)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        context.grantUriPermission(
                            "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
                            fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            // Handle the case where no PDF viewer app is installed on the device
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    document.close()
                }
            }
        }


     //   val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.absolutePath



//        val outputStream = FileOutputStream(File(directory, fileName))
//        val file = File("$directory/${fileName}")
//
//        // Save the document as PDF
//      //  val file = File(directory, "$name attendance_report.pdf").absolutePath
//        try {
//            document.writeTo(outputStream)
//            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//
//                val file = File(directory, fileName)
//                val fileUri = FileProvider.getUriForFile(context, "com.example.inwork.fileprovider", file)
//                val intent = Intent(Intent.ACTION_VIEW)
//
//                intent.setDataAndType(fileUri, "application/pdf")
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                context.grantUriPermission(
//                    "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
//                    fileUri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//
//                try {
//                    context.startActivity(intent)
//                } catch (e: ActivityNotFoundException) {
//                    // Handle the case where no PDF viewer app is installed on the device
//                }
//
//
//                Log.d("pdf", "generated")
////            Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//            }
//        }catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        finally {
//            document.close()
//        }
////
//        document.close()

    }


    private fun createDirectory(context: Context, folderName: String) {
        val file = File(context.getExternalFilesDir(null), folderName)
        if (!file.exists()) {
            if (file.mkdir()) {
                Log.d("Dirk", "Directory created successfully")
            } else {
                Log.d("Dirk", "Error creating directory")
            }
        } else {
            Log.d("Dirk", "Directory already exists")
        }
    }






    @SuppressLint("SetTextI18n")
    fun generateScreenPdf(context: Context, parsedData: ParsedScreenData, name: String) {
        val document = PdfDocument()
        val marginDp = 10
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 1)
            .create() // A4 size in points (1 point = 1/72 inch)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Inflate the layout for the first page
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.report_screentime_format, null) as LinearLayout

        // Set the parsed values to the layout views
        val headerTextView: TextView = layout.findViewById(R.id.headerTextView)
        val dateDurationTextView: TextView = layout.findViewById(R.id.dateDurationTextView)
        val employeeNameTextView: TextView = layout.findViewById(R.id.employeeNameTextView)
        val employeeIDTextView: TextView = layout.findViewById(R.id.employeeIDTextView)
        val companyIDTextView: TextView = layout.findViewById(R.id.companyIDTextView)
        val companyNameTextView: TextView = layout.findViewById(R.id.companyNameTextView)
        val averageTimespent: TextView = layout.findViewById(R.id.AverageMonthTimeSpent)
        val alertDetailsListView: RecyclerView = layout.findViewById(R.id.screentimeDetailsrecyclerview)

        headerTextView.text = "INWORK – Monthly ScreenTime Report"
        dateDurationTextView.text = "Date: ${parsedData.date}"
        employeeNameTextView.text = "Employee Name: ${parsedData.employeeName}"
        employeeIDTextView.text = "Employee ID: ${parsedData.employeeID}"
        companyIDTextView.text = "Company ID: ${parsedData.companyID}"
        companyNameTextView.text = "Company Name: ${parsedData.companyName}"




        // Calculate the total duration for all entries
        val totalDuration = (parsedData.alertDetailsList).sumOf { alertdetails ->
            calculateTotalDuration(
                alertdetails.whatsapp_duration,
                alertdetails.facebook_duration,
                alertdetails.games_duration,
                alertdetails.instagram_duration,
                alertdetails.twitter_duration,
                alertdetails.calls_duration,
                alertdetails.others_duration
            )
        }

        // Count distinct dates
        val distinctDates = (parsedData.alertDetailsList).map { it.time_stamp }.distinct().size
        val averageMonthlyDuration = totalDuration.div(distinctDates)
        // Convert averageMonthlyDuration to hours, minutes, and seconds
        val hours = averageMonthlyDuration / 3600
        val minutes = (averageMonthlyDuration % 3600) / 60
        val seconds = averageMonthlyDuration % 60

        // Format the duration as "hh:mm:ss"
        val formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        averageTimespent.text = "Average Time Spent:: $formattedDuration"

        Log.d("avgmonthlyduration", "$formattedDuration  size: ${parsedData.alertDetailsList.size}")

        Log.d("info","${parsedData.alertDetailsList.toString()}")

        // Set the adapter for the alertDetailsListView
        val layoutManager = LinearLayoutManager(context)
        alertDetailsListView.layoutManager = layoutManager
        val alerts =
            parsedData.alertDetailsList.subList(0, minOf(20, parsedData.alertDetailsList.size))
        val alertAdapter = ScreenTimeDetailsReportAdapter(alerts)
        alertDetailsListView.adapter = alertAdapter

        // Measure and layout the view
        layout.measure(
            View.MeasureSpec.makeMeasureSpec(page.canvas.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(page.canvas.height, View.MeasureSpec.EXACTLY)
        )
        layout.layout(0, 0, page.canvas.width, page.canvas.height)

        // Draw the layout on the canvas
        layout.draw(canvas)

        // Finish the first page
        document.finishPage(page)


        if(parsedData.alertDetailsList.size > 20){
            val alertChunks = parsedData.alertDetailsList.subList(20, parsedData.alertDetailsList.size).chunked(30)

            val alertsPerPage = 30 // Number of alerts to display per page

// Create subsequent pages for the remaining alerts
            for (pageIndex in alertChunks.indices) {
                val nextPage = document.startPage(pageInfo)
                val nextCanvas = nextPage.canvas
                nextCanvas.drawColor(Color.WHITE)

                // Create a new RecyclerView for the remaining alerts
                val remainingAlerts = alertChunks[pageIndex]
                val remainingAlertDetailsListView = RecyclerView(context)
                val remainingLayoutManager = LinearLayoutManager(context)
                remainingAlertDetailsListView.layoutManager = remainingLayoutManager
                val remainingAlertDetailsAdapter = ScreenTimeDetailsReportAdapter(remainingAlerts)
                remainingAlertDetailsListView.adapter = remainingAlertDetailsAdapter

                // Set the number of alerts per page for the remaining RecyclerView
                remainingLayoutManager.isItemPrefetchEnabled = true
                remainingLayoutManager.initialPrefetchItemCount = alertsPerPage
                remainingAlertDetailsListView.setItemViewCacheSize(alertsPerPage)
                remainingAlertDetailsListView.recycledViewPool.setMaxRecycledViews(0, alertsPerPage)

                // Measure and layout the RecyclerView
                remainingAlertDetailsListView.measure(
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(nextPage.canvas.height, View.MeasureSpec.EXACTLY)
                )
                remainingAlertDetailsListView.layout(0, 0, nextPage.canvas.width, nextPage.canvas.height)

                // Draw the RecyclerView on the canvas
                remainingAlertDetailsListView.draw(nextCanvas)

                // Finish the page
                document.finishPage(nextPage)
            }
        }


        val fileName = "${parsedData.employeeID}_ScreenTime_report.pdf"

        val mimeType = "application/pdf"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

        uri?.let { fileUri ->
            val outputStream: OutputStream? = resolver.openOutputStream(fileUri)
            outputStream?.use { output ->
                try {
                    document.writeTo(output)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(fileUri, mimeType)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        context.grantUriPermission(
                            "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
                            fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            // Handle the case where no PDF viewer app is installed on the device
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    document.close()
                }
            }
        }
//        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
//        val outputStream = FileOutputStream(File(directory, fileName))
//        val file = File("$directory/$fileName")
//        try {
//            document.writeTo(outputStream)
//            Handler(Looper.getMainLooper()).post {
//                Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//
//                val file = File(directory, fileName)
//                val fileUri = FileProvider.getUriForFile(context, "com.example.inwork.fileprovider", file)
//                val intent = Intent(Intent.ACTION_VIEW)
//
//                intent.setDataAndType(fileUri, "application/pdf")
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                context.grantUriPermission(
//                    "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
//                    fileUri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//
//                try {
//                    context.startActivity(intent)
//                } catch (e: ActivityNotFoundException) {
//                    // Handle the case where no PDF viewer app is installed on the device
//                }
//
//
//                Log.d("pdf", "generated")
////            Toast.makeText(context, "Pdf Saved Successfully", Toast.LENGTH_SHORT).show()
//            }
//        }catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        finally {
//            document.close()
//        }

    }

    fun calculateTotalDuration(vararg durations: String?): Long {
        var totalDuration = 0L

        for (duration in durations) {
            duration?.let {
                val parts = it.split(":")
                if (parts.size == 3) {
                    val hours = parts[0].toLongOrNull() ?: 0
                    val minutes = parts[1].toLongOrNull() ?: 0
                    val seconds = parts[2].toLongOrNull() ?: 0

                    val durationInSeconds = hours * 3600 + minutes * 60 + seconds
                    totalDuration += durationInSeconds
                }
            }
        }

        return totalDuration
    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun matchCheckInAndOut(checkInList: List<CheckIn>?, checkOutList: List<CheckOut>?): List<CheckInAndOut> {
        val matchedList = mutableListOf<CheckInAndOut>()

        if (checkInList != null && checkOutList != null) {
            val maxIndex = minOf(checkInList.size, checkOutList.size)

            for (i in 0 until maxIndex) {
                val checkIn = checkInList.getOrNull(i)
                val checkOut = checkOutList.getOrNull(i)

                if (checkIn != null && checkOut != null) {
                    val checkInDate = checkIn.checkin_time?.substringBefore(" ")
                    val checkOutDate = checkOut.checkout_time?.substringBefore(" ")

                    if (checkInDate == checkOutDate) {
                        val checkInTime = checkIn.checkin_time?.substringAfter(" ")
                        val checkOutTime = checkOut.checkout_time?.substringAfter(" ")

                        if (checkOutTime!! < checkInTime!!) {
                            // Add separate entries for check-in and check-out times
                            matchedList.add(
                                CheckInAndOut(
                                    sno = null,
                                    date = checkInDate!!,
                                    checkInTime = checkInTime,
                                    checkOutTime = null,
                                    timeSpent = null,
                                    remarks = checkIn.remarks
                                )
                            )

                            matchedList.add(
                                CheckInAndOut(
                                    sno = null,
                                    date = checkOutDate!!,
                                    checkInTime = null,
                                    checkOutTime = checkOutTime,
                                    timeSpent = null,
                                    remarks = checkOut.remarks
                                )
                            )
                        } else {
                            // Calculate time spent when check-in and check-out are on the same date
                            val timeSpent = calculateTimeSpent(checkInTime, checkOutTime)

                            matchedList.add(
                                CheckInAndOut(
                                    sno = null,
                                    date = checkInDate!!,
                                    checkInTime = checkInTime,
                                    checkOutTime = checkOutTime,
                                    timeSpent = timeSpent,
                                    remarks = "${checkIn.remarks} ${checkOut.remarks}"
                                )
                            )
                        }
                    } else {
                        // Treat as different entries since the dates don't match
                        matchedList.add(
                            CheckInAndOut(
                                sno = null,
                                date = checkInDate ?: "",
                                checkInTime = checkIn.checkin_time?.substringAfter(" "),
                                checkOutTime = null,
                                timeSpent = null,
                                remarks = checkIn.remarks
                            )
                        )

                        matchedList.add(
                            CheckInAndOut(
                                sno = null,
                                date = checkOutDate ?: "",
                                checkInTime = null,
                                checkOutTime = checkOut.checkout_time?.substringAfter(" "),
                                timeSpent = null,
                                remarks = checkOut.remarks
                            )
                        )
                    }
                } else if (checkIn != null) {
                    // Record the map from checkInList as a new entry
                    val checkInDate = checkIn.checkin_time?.substringBefore(" ")

                    matchedList.add(
                        CheckInAndOut(
                            sno = null,
                            date = checkInDate ?: "",
                            checkInTime = checkIn.checkin_time?.substringAfter(" "),
                            checkOutTime = null,
                            timeSpent = null,
                            remarks = checkIn.remarks
                        )
                    )
                } else if (checkOut != null) {
                    // Record the map from checkOutList as a new entry
                    val checkOutDate = checkOut.checkout_time?.substringBefore(" ")

                    matchedList.add(
                        CheckInAndOut(
                            sno = null,
                            date = checkOutDate ?: "",
                            checkInTime = null,
                            checkOutTime = checkOut.checkout_time?.substringAfter(" "),
                            timeSpent = null,
                            remarks = checkOut.remarks
                        )
                    )
                }
            }

// Record any remaining entries from checkInList or checkOutList
            for (i in maxIndex until checkInList.size) {
                val checkIn = checkInList[i]
                val checkInDate = checkIn.checkin_time?.substringBefore(" ")

                matchedList.add(
                    CheckInAndOut(
                        sno = null,
                        date = checkInDate ?: "",
                        checkInTime = checkIn.checkin_time?.substringAfter(" "),
                        checkOutTime = null,
                        timeSpent = null,
                        remarks = checkIn.remarks
                    )
                )
            }

            for (i in maxIndex until checkOutList.size) {
                val checkOut = checkOutList[i]
                val checkOutDate = checkOut.checkout_time?.substringBefore(" ")

                matchedList.add(
                    CheckInAndOut(
                        sno = null,
                        date = checkOutDate ?: "",
                        checkInTime = null,
                        checkOutTime = checkOut.checkout_time?.substringAfter(" "),
                        timeSpent = null,
                        remarks = checkOut.remarks
                    )
                )
            }
        } else if (checkInList != null) {
            for (checkIn in checkInList) {
                val checkInTime = checkIn.checkin_time?.substringAfter(" ")
                matchedList.add(
                    CheckInAndOut(
                        sno = null,
                        date = checkIn.checkin_time?.substringBefore(" ") ?: "",
                        checkInTime = checkInTime,
                        checkOutTime = null,
                        timeSpent = null,
                        remarks = checkIn.remarks ?: ""
                    )
                )
            }
        } else if (checkOutList != null) {
            for (checkOut in checkOutList) {
                val checkOutTime = checkOut.checkout_time?.substringAfter(" ")
                matchedList.add(
                    CheckInAndOut(
                        sno = null,
                        date = checkOut.checkout_time?.substringBefore(" ") ?: "",
                        checkInTime = null,
                        checkOutTime = checkOutTime,
                        timeSpent = null,
                        remarks = checkOut.remarks ?: ""
                    )
                )
            }
        }

        // Sort the matched list by date in ascending order
        matchedList.sortBy { it.date }

        // Filter the matched list to include only entries from the last 30 days
        val currentDate = LocalDate.now()
        val thirtyDaysAgo = currentDate.minusDays(30)
        val filteredList = matchedList.filter { LocalDate.parse(it.date) >= thirtyDaysAgo }

        return filteredList
    }


//
//
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    fun matchCheckInAndOut(checkInList: List<CheckIn>?, checkOutList: List<CheckOut>?): List<CheckInAndOut> {
//        val matchedList = mutableListOf<CheckInAndOut>()
//
//        if (checkInList != null && checkOutList != null) {
//            val maxIndex = minOf(checkInList.size, checkOutList.size)
//
//            for (i in 0 until maxIndex) {
//                val checkIn = checkInList[i]
//                val checkOut = checkOutList[i]
//
//                if (checkIn.checkin_time?.substringBefore(" ") == checkOut.checkout_time?.substringBefore(
//                        " "
//                    )
//                ) {
//                    val checkInTime = checkIn.checkin_time?.substringAfter(" ")
//                    val checkOutTime = checkOut.checkout_time?.substringAfter(" ")
//
//                    if (checkOutTime!! < (checkInTime!!)) {
//                        // Add separate entries for check-in and check-out times
//                        matchedList.add(
//                            CheckInAndOut(
//                                date = checkIn.checkin_time?.substringBefore(" ")!!,
//                                checkInTime = checkInTime,
//                                checkOutTime = null,
//                                timeSpent = null,
//                                remarks = checkIn.remarks
//                            )
//                        )
//
//                        matchedList.add(
//                            CheckInAndOut(
//                                date = checkOut.checkout_time?.substringBefore(" ")!!,
//                                checkInTime = null,
//                                checkOutTime = checkOutTime,
//                                timeSpent = null,
//                                remarks = checkOut.remarks
//                            )
//                        )
//                    } else {
//                        // Calculate time spent when check-in and check-out are on the same date
//                        val timeSpent = calculateTimeSpent(checkInTime, checkOutTime)
//
//                        matchedList.add(
//                            CheckInAndOut(
//                                date = checkIn.checkin_time?.substringBefore(" ")!!,
//                                checkInTime = checkInTime,
//                                checkOutTime = checkOutTime,
//                                timeSpent = timeSpent,
//                                remarks = "${checkIn.remarks} ${checkOut.remarks}"
//                            )
//                        )
//                    }
//                } else {
//                    matchedList.add(
//                        CheckInAndOut(
//                            date = checkIn.checkin_time?.substringBefore(" ")!!,
//                            checkInTime = checkIn.checkin_time?.substringAfter(" "),
//                            checkOutTime = null,
//                            timeSpent = null,
//                            remarks = checkIn.remarks
//                        )
//                    )
//
//                    matchedList.add(
//                        CheckInAndOut(
//                            date = checkOut.checkout_time?.substringBefore(" ")!!,
//                            checkInTime = null,
//                            checkOutTime = checkOut.checkout_time?.substringAfter(" "),
//                            timeSpent = null,
//                            remarks = checkOut.remarks
//                        )
//                    )
//                }
//            }
//        }else if (checkInList != null) {
//            // Process checkInList when checkOutList is null
//            for (checkIn in checkInList) {
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkIn.checkin_time?.substringBefore(" ") ?: "",
//                        checkInTime = checkIn.checkin_time?.substringAfter(" "),
//                        checkOutTime = null,
//                        timeSpent = null,
//                        remarks = checkIn.remarks ?: ""
//                    )
//                )
//            }
//        } else if (checkOutList != null) {
//            // Process checkOutList when checkInList is null
//            for (checkOut in checkOutList) {
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkOut.checkout_time?.substringBefore(" ") ?: "",
//                        checkInTime = null,
//                        checkOutTime = checkOut.checkout_time?.substringAfter(" "),
//                        timeSpent = null,
//                        remarks = checkOut.remarks ?: ""
//                    )
//                )
//            }
//        }
//
//            // Sort the matched list by date in ascending order
//            matchedList.sortBy { it.date }
//
//            // Filter the matched list to include only entries from the last 30 days
//            val currentDate = LocalDate.now()
//            val thirtyDaysAgo = currentDate.minusDays(30)
//            val filteredList = matchedList.filter { LocalDate.parse(it.date) >= thirtyDaysAgo }
//
//            return filteredList
//
//    }






    @RequiresApi(Build.VERSION_CODES.S)
    fun calculateTimeSpent(checkInTime: String, checkOutTime: String): String {

        // Example calculation using Java 8 Date and Time API
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val startTime = LocalTime.parse(checkInTime, formatter)
        val endTime = LocalTime.parse(checkOutTime, formatter)
        val duration = Duration.between(startTime, endTime)

        val hours = duration.toHours()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toMillis())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toMillis()) % 60


        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    suspend fun getCheckOutInfo(employee_id: String): List<CheckOut>? {
        val call = RetrofitgetCheckOut.apiService.getData(employee_id)
        return withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val checkOutMap = response.body()
                    checkOutMap?.values?.toList()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCheckInInfo(employee_id: String): List<CheckIn>? {
        val call = RetrofitgetcheckIn.apiService.getData(employee_id)
        return withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val checkOutMap = response.body()
                    checkOutMap?.values?.toList()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun screentimeDetails(companyID: String, employeeID: String): List<Screentimedata>? {
        val call = RetrofitGetScreenDetailsEmployee.apiService.getData(companyID, employeeID)
        return withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val screenMap = response.body()
                    val currentDate = LocalDate.now()
                    Log.d("screen1",screenMap?.values?.toList().toString())
                    screenMap?.values?.toList()
//                    val filteredList = screenMap?.values?.filter { data ->
//                        val dataDate = LocalDate.parse(data.date)
//                        ChronoUnit.DAYS.between(dataDate, currentDate) <= 30
//                    }
                   // filteredList
//                    filteredList?.sortedByDescending { data ->
//                        LocalDate.parse(data.date)
//                    }
                } else {
                    Log.d("screen2",response.raw().toString())
                    null
                }
            } catch (e: Exception) {
                Log.d("screen3",e.toString())
                null
            }
        }
    }


    // Function to download the generated PDF
    fun downloadPdf(context: Context,name: String) {
        val directory = File(Environment.getExternalStorageDirectory(),Environment.DIRECTORY_DOCUMENTS)
     //   val directory = File(Environment.getExternalStorageDirectory(), "Download")
        if (!directory.exists()) {
            directory.mkdirs()
            Log.d("dir","Making")
        }
      //  getCheckInCount()
        getCheckInCounts()
        val file = File(directory, "$name monthly_report.pdf")

        // Download the file
        val fileUri = FileProvider.getUriForFile(context, "com.example.inwork.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)

        intent.setDataAndType(fileUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        context.grantUriPermission(
            "com.google.android.apps.docs", // Replace with the package name of the PDF viewer app
            fileUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        context.startActivity(intent)

    }


    fun getCheckInCount(companyID: String, callback: (Int) -> Unit) {
        val call = RetrofitgetcheckIn.apiService.getData(companyID)

        call.enqueue(object : Callback<Map<String, CheckIn>> {
            override fun onResponse(
                call: Call<Map<String, CheckIn>>,
                response: Response<Map<String, CheckIn>>
            ) {
                if (response.isSuccessful) {
                    val resultMap = response.body()
                    val currentDate = Date()
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                    val startDate = calendar.time

                    val checkInsInRange = resultMap?.filter { (_, checkIn) ->
                        val checkinTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkIn.checkin_time)
                        checkinTime != null && checkinTime >= startDate && checkinTime <= currentDate
                    }
                    val checkInCount = checkInsInRange?.size ?: 0
                    callback(checkInCount)
                    Log.d("callback",checkInCount.toString())
                }else{
                    callback(0)
                }
            }

            override fun onFailure(call: Call<Map<String, CheckIn>>, t: Throwable) {
                Log.e("APIError", "API call failed with code: $t")
                callback(0)
            }
        })
    }

    fun getCheckOutCount(companyID: String, callback: (Int) -> Unit) {
        val call = RetrofitgetCheckOut.apiService.getData(companyID)

        call.enqueue(object : Callback<Map<String, CheckOut>> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<Map<String, CheckOut>>,
                response: Response<Map<String, CheckOut>>
            ) {
                if (response.isSuccessful) {
                    val resultMap = response.body()
                    val currentDate = Date()
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                    val startDate = calendar.time

                                    val checksOutRange = resultMap?.filter { (_, checkOut) ->
                    val checkoutTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkOut.checkout_time)
                    checkoutTime != null && checkoutTime >= startDate && checkoutTime <= currentDate
                }
                val checkOutCount = checksOutRange?.size ?: 0
                    callback(checkOutCount)
                    Log.d("callback",checkOutCount.toString())
                }else{
                    callback(0)
                }
            }

            override fun onFailure(call: Call<Map<String, CheckOut>>, t: Throwable) {
                Log.e("APIError", "API call failed with code: $t")
                callback(0)
            }
        })
    }


    fun fetchNotifications(
        employeeID: String,
        companyID: String,
        context: Context,
        callback: (List<AlertDetails>?) -> Unit
    ) {
        val apiService = RetrofitGetClientNotifications.apiService
        val call = apiService.getData(employeeID, companyID)

        call.enqueue(object : Callback<Map<String, ClientNotification>> {
            override fun onResponse(
                call: Call<Map<String, ClientNotification>>,
                response: Response<Map<String, ClientNotification>>
            ) {
                if (response.isSuccessful) {
                    val notificationMap = response.body()
                    if (notificationMap != null) {

                        val notificationsList = notificationMap.values.toList().mapIndexed { index, notification ->
                            val timestamp = notification.notification_date
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val date = dateFormat.parse(timestamp)

                            // Extracting date and time from the parsed date object
                            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val alertDate = dateFormatter.format(date)
                            val alertTime = timeFormatter.format(date)
                            val description = extractRemarks(notification.notification)

                            val lastKnownLocation = extractLastKnownCoordinates(notification.notification)

                            AlertDetails(
                                sno = "${index + 1}",
                                alertTitle = notification.title,
                                alertTime = alertTime,
                                alertRemarks = lastKnownLocation.toString(), // Assign an appropriate value for alertRemarks
                                alertDate = alertDate,
                                alertDescription = description
                            )
                        }
//                        val notificationsList = notificationMap.values.toList().mapIndexed { index, notification ->
//                            AlertDetails(
//                                sno = "Alert ${index + 1}",
//                                alertTitle = notification.title,
//                                alertTime = "", // Assign an appropriate value for alertTime
//                                alertRemarks = "", // Assign an appropriate value for alertRemarks
//                                alertDate = notification.notification_date,
//                                alertDescription = notification.notification
//                            )
//                        }
//                        val sharedPreferences =
//                            context.getSharedPreferences("Report", Context.MODE_PRIVATE)
//                        val editor = sharedPreferences.edit()
//                        val jsonString: String = Gson().toJson(notificationsList)
//                        editor.putString("ReportList", jsonString)
//                        editor.apply()
                        callback(notificationsList)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Map<String, ClientNotification>>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun extractLastKnownCoordinates(description: String): String {
        val startIndex = description.indexOf("Last Known Coordinates") + "Last Known Coordinates".length
        return if (startIndex >= 0 && startIndex < description.length) {
            description.substring(startIndex).trim()
        } else {
            ""
        }
    }

    private fun extractRemarks(description: String): String {
        val pattern = Regex("(.*) Last Known")
        val matchResult = pattern.find(description)
        return matchResult?.groupValues?.get(1)?.trim() ?: description
    }




//
//
//    private fun dismissprogressbar(){
//        progressDialog?.dismiss()
//    }





    fun getCheckInCounts(){
        // Make the API call
        val call = RetrofitgetcheckIn.apiService.getData("M20543")
        call.enqueue(object : Callback<Map<String, CheckIn>> {
            override fun onResponse(
                call: Call<Map<String, CheckIn>>,
                response: Response<Map<String, CheckIn>>
            ) {
                if (response.isSuccessful) {
                    val resultMap = response.body()
                    val currentDate = Date()
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                    val startDate = calendar.time

                    val checkInsInRange = resultMap?.filter { (_, checkIn) ->
                        val checkoutTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkIn.checkin_time)
                        checkoutTime != null && checkoutTime >= startDate && checkoutTime <= currentDate
                    }
                    val checkInCount = checkInsInRange?.size ?: 0
                    // Use the checkInCount value as needed

                    Log.d("CheckInCount", "Count: $checkInCount")
                } else {
                    // Handle the API call failure
                    Log.e("APIError", "API call failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, CheckIn>>, t: Throwable) {
                // Handle the API call failure
                Log.e("APIError", "API call failed: ${t.message}")
            }
        })


    }







//    fun getCheckOutInfo(employee_id: String, callback: (List<CheckOut>?) -> Unit) {
//        val call = RetrofitgetCheckOut.apiService.getData(employee_id)
//        call.enqueue(object : Callback<Map<String,CheckOut>> {
//            //            override fun onResponse(call: Call<CheckOut>, response: Response<CheckOut>) {
////                if (response.isSuccessful) {
////                    val checkOutMap = response.body()
////                    val checkOutList = checkOutMap?.values?.toList()
////                    callback(checkOutList)
////                } else {
////                    callback(null)
////                }
////            }
////
////            override fun onFailure(call: Call<CheckOut>, t: Throwable) {
////                callback(null)
////            }
//            override fun onResponse(
//                call: Call<Map<String, CheckOut>>,
//                response: Response<Map<String, CheckOut>>
//            ) {
//                                if (response.isSuccessful) {
//                    val checkOutMap = response.body()
//                    val checkOutList = checkOutMap?.values?.toList()
//                    callback(checkOutList)
//                } else {
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<Map<String, CheckOut>>, t: Throwable) {
//                callback(null)
//            }
//        })
//    }
//
//    fun getCheckInInfo(employee_id: String, callback: (List<CheckIn>?) -> Unit) {
//        val call = RetrofitgetcheckIn.apiService.getData(employee_id)
//        call.enqueue(object : Callback<Map<String,CheckIn>> {
//            //            override fun onResponse(call: Call<CheckInMap>, response: Response<CheckInMap>) {
////                if (response.isSuccessful) {
////                    val checkInMap = response.body()
////                    val checkInList = checkInMap?.values?.toList()
////                    callback(checkInList)
////                } else {
////                    callback(null)
////                }
////            }
////
////            override fun onFailure(call: Call<CheckInMap>, t: Throwable) {
////                callback(null)
////            }
//            override fun onResponse(
//                call: Call<Map<String, CheckIn>>,
//                response: Response<Map<String, CheckIn>>
//            ) {
//                                if (response.isSuccessful) {
//                    val checkInMap = response.body()
//                    val checkInList = checkInMap?.values?.toList()
//                    callback(checkInList)
//                } else {
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<Map<String, CheckIn>>, t: Throwable) {
//                callback(null)
//            }
//        })
//    }

//    @RequiresApi(Build.VERSION_CODES.S)
//    fun matchCheckInAndOut(checkInList: List<CheckIn>, checkOutList: List<CheckOut>): List<CheckInAndOut> {
//        val matchedList = mutableListOf<CheckInAndOut>()
//
//        val minSize = minOf(checkInList.size, checkOutList.size)
//        for (i in 0 until minSize) {
//            val checkIn = checkInList[i]
//            val checkOut = checkOutList[i]
//
//            val checkInDate = checkIn.checkin_time!!.substringBefore(" ")
//            val checkOutDate = checkOut.checkout_time!!.substringBefore(" ")
//
//            if (checkInDate == checkOutDate) {
//                val checkInTime = checkIn.checkin_time!!.substringAfter(" ")
//                val checkOutTime = checkOut.checkout_time!!.substringAfter(" ")
//
//                val timeSpent = calculateTimeSpent(checkInTime, checkOutTime)
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkOutDate,
//                        checkInTime = checkInTime,
//                        checkOutTime = checkOutTime,
//                        timeSpent = timeSpent
//                    )
//                )
//            }
//        }
//
//        return matchedList
//    }


//    @RequiresApi(Build.VERSION_CODES.S)
//    fun matchCheckInAndOut(checkInList: List<CheckIn>, checkOutList: List<CheckOut>): List<CheckInAndOut> {
//        val matchedList = mutableListOf<CheckInAndOut>()
//
//        val maxIndex = minOf(checkInList.size, checkOutList.size)
//
//        for (i in 0 until maxIndex) {
//            val checkIn = checkInList[i]
//            val checkOut = checkOutList[i]
//
//            if (checkIn.checkin_time?.substringBefore(" ") == checkOut.checkout_time?.substringBefore(" ")) {
//                val checkInTime = checkIn.checkin_time?.substringAfter(" ")
//                val checkOutTime = checkOut.checkout_time?.substringAfter(" ")
//                val timeSpent = calculateTimeSpent(checkInTime!!, checkOutTime!!)
//
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkIn.checkin_time?.substringBefore(" ")!!,
//                        checkInTime = checkInTime,
//                        checkOutTime = checkOutTime,
//                        timeSpent = timeSpent
//                    )
//                )
//            } else {
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkIn.checkin_time?.substringBefore(" ")!!,
//                        checkInTime = checkIn.checkin_time?.substringAfter(" "),
//                        checkOutTime = null,
//                        timeSpent = null
//                    )
//                )
//
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkOut.checkout_time?.substringBefore(" ")!!,
//                        checkInTime = null,
//                        checkOutTime = checkOut.checkout_time?.substringAfter(" "),
//                        timeSpent = null
//                    )
//                )
//            }
//        }
//
//        // Sort the matched list by date in ascending order
//        matchedList.sortBy { it.date }
//
//        // Filter the matched list to include only entries from the last 30 days
//        val currentDate = LocalDate.now()
//        val thirtyDaysAgo = currentDate.minusDays(30)
//        val filteredList = matchedList.filter { LocalDate.parse(it.date) >= thirtyDaysAgo }
//
//        return filteredList
//    }

//
//    @RequiresApi(Build.VERSION_CODES.S)
//    fun matchCheckInAndOut(checkInList: List<CheckIn>, checkOutList: List<CheckOut>): List<CheckInAndOut> {
//        val matchedList = mutableListOf<CheckInAndOut>()
//
//        val maxIndex = minOf(checkInList.size, checkOutList.size)
//
//        for (i in 0 until maxIndex) {
//            val checkIn = checkInList[i]
//            val checkOut = checkOutList[i]
//
//            if (checkIn.checkin_time?.substringBefore(" ") == checkOut.checkout_time?.substringBefore(" ")) {
//                val checkInTime = checkIn.checkin_time?.substringAfter(" ")
//                val checkOutTime = checkOut.checkout_time?.substringAfter(" ")
//
//                if (checkOutTime!! < (checkInTime!!)) {
//                    // Add separate entries for check-in and check-out times
//                    matchedList.add(
//                        CheckInAndOut(
//                            date = checkIn.checkin_time?.substringBefore(" ")!!,
//                            checkInTime = checkInTime,
//                            checkOutTime = null,
//                            timeSpent = null,
//                            remarks = checkIn.remarks
//                        )
//                    )
//
//                    matchedList.add(
//                        CheckInAndOut(
//                            date = checkOut.checkout_time?.substringBefore(" ")!!,
//                            checkInTime = null,
//                            checkOutTime = checkOutTime,
//                            timeSpent = null,
//                            remarks = checkOut.remarks
//                        )
//                    )
//                } else {
//                    // Calculate time spent when check-in and check-out are on the same date
//                    val timeSpent = calculateTimeSpent(checkInTime!!, checkOutTime)
//
//                    matchedList.add(
//                        CheckInAndOut(
//                            date = checkIn.checkin_time?.substringBefore(" ")!!,
//                            checkInTime = checkInTime,
//                            checkOutTime = checkOutTime,
//                            timeSpent = timeSpent,
//                            remarks = checkIn.remarks
//                        )
//                    )
//                }
//            } else {
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkIn.checkin_time?.substringBefore(" ")!!,
//                        checkInTime = checkIn.checkin_time?.substringAfter(" "),
//                        checkOutTime = null,
//                        timeSpent = null,
//                        remarks = checkIn.remarks
//                    )
//                )
//
//                matchedList.add(
//                    CheckInAndOut(
//                        date = checkOut.checkout_time?.substringBefore(" ")!!,
//                        checkInTime = null,
//                        checkOutTime = checkOut.checkout_time?.substringAfter(" "),
//                        timeSpent = null,
//                        remarks = checkOut.remarks
//                    )
//                )
//            }
//        }
//
//        // Sort the matched list by date in ascending order
//        matchedList.sortBy { it.date }
//
//        // Filter the matched list to include only entries from the last 30 days
//        val currentDate = LocalDate.now()
//        val thirtyDaysAgo = currentDate.minusDays(30)
//        val filteredList = matchedList.filter { LocalDate.parse(it.date) >= thirtyDaysAgo }
//
//        return filteredList
//    }


//     fun fetchNotifications(employeeID: String, companyID: String): List<AlertDetails>? = runBlocking {
//         return@runBlocking withContext(Dispatchers.IO) {
//
//
//             val apiService = RetrofitGetClientNotifications.apiService
//             val call = apiService.getData(employeeID, companyID)
//
//
//                 val response = call.execute()
//                 if (response.isSuccessful) {
//                     val notificationMap = response.body()
//                     if (notificationMap != null) {
//                         notificationMap.values.toList()
//                             .mapIndexed { index, notification ->
//                                 AlertDetails(
//                                     sno = "Alert ${index + 1}",
//                                     alertTitle = notification.title,
//                                     alertTime = "", // Assign an appropriate value for alertTime
//                                     alertRemarks = "", // Assign an appropriate value for alertRemarks
//                                     alertDate = notification.notification_date,
//                                     alertDescription = notification.notification
//                                 )
//                             }
//                     } else {
//                         null
//                     }
//                 } else{
//                     null
//                 }
//
//
//         }
//    }




//    fun fetchNotifications(employeeID: String, companyID: String,context: Context) {
//        val apiService = RetrofitGetClientNotifications.apiService
//        val call = apiService.getData(employeeID, companyID)
//
//        call.enqueue(object : Callback<Map<String, ClientNotification>> {
//            override fun onResponse(
//                call: Call<Map<String, ClientNotification>>,
//                response: Response<Map<String, ClientNotification>>
//            ) {
//                if (response.isSuccessful) {
//                    val notificationMap = response.body()
//                    if (notificationMap != null) {
//                        notificationMap.values.toList().mapIndexed { index, notification ->
//                            val alertDetails =   AlertDetails(
//                                sno = "Alert ${index + 1}",
//                                alertTitle = notification.title,
//                                alertTime = "", // Assign an appropriate value for alertTime
//                                alertRemarks = "", // Assign an appropriate value for alertRemarks
//                                alertDate = notification.notification_date,
//                                alertDescription = notification.notification
//                            )
//                            notificationsList.add(alertDetails)
//                        }
//                        val sharedPreferences = context.getSharedPreferences("Report",Context.MODE_PRIVATE)
//                        val editor = sharedPreferences.edit()
//                        val jsonString: String = Gson().toJson(notificationsList)
//                        editor.putString("ReportList",jsonString)
//                        editor.apply()
//                      //  return  notificationsList
//                        Log.d("alertnotification", notificationsList.toString())
//
////                        val recyclerView : RecyclerView =
////                        val alertDetailsAdapter = AlertDetailsAdapter(notificationsList.toList())
////                        alertDetailsListView.adapter = alertDetailsAdapter
//
//
//                      //  callback(notificationList)
//                    } else {
//                        Log.d("alertnotification","null")
//                    //    callback(null)
//                    }
//                } else {
//                    Log.d("alertnotification","failed")
//                   // callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<Map<String, ClientNotification>>, t: Throwable) {
//               // callback(null)
//                Log.d("alertnotification",t.toString())
//            }
//        })
//
//
//    }





//    fun fetchNotifications(employeeID: String, companyID: String, callback: (List<AlertDetails>?) -> Unit) {
//        val apiService = RetrofitGetClientNotifications.apiService
//        val call = apiService.getData(employeeID, companyID)
//
//        call.enqueue(object : Callback<Map<String, ClientNotification>> {
//            override fun onResponse(
//                call: Call<Map<String, ClientNotification>>,
//                response: Response<Map<String, ClientNotification>>
//            ) {
//                if (response.isSuccessful) {
//                    val notificationMap = response.body()
//                    if (notificationMap != null) {
//                        val notificationList = notificationMap.values.toList().mapIndexed { index, notification ->
//                            AlertDetails(
//                                sno = "Alert ${index + 1}",
//                                alertTitle = notification.title,
//                                alertTime = "", // Assign an appropriate value for alertTime
//                                alertRemarks = "", // Assign an appropriate value for alertRemarks
//                                alertDate = notification.notification_date,
//                                alertDescription = notification.notification
//                            )
//                        }
//
//                        callback(notificationList)
//                    } else {
//                        Log.d("alertnotification","null")
//                        callback(null)
//                    }
//                } else {
//                    Log.d("alertnotification","failed")
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<Map<String, ClientNotification>>, t: Throwable) {
//                callback(null)
//                Log.d("alertnotification",t.toString())
//            }
//        })
//    }



//    fun getCheckInCount(companyID: String): Int = runBlocking {
//        return@runBlocking withContext(Dispatchers.IO) {
//            // Make the API call on the IO dispatcher
//            val call = RetrofitgetcheckIn.apiService.getData(companyID)
//            val response = call.execute()
//
//            if (response.isSuccessful) {
//                val resultMap = response.body()
//                val currentDate = Date()
//                val calendar = Calendar.getInstance()
//                calendar.add(Calendar.DAY_OF_MONTH, -30)
//                val startDate = calendar.time
//
//                val checkInsInRange = resultMap?.filter { (_, checkIn) ->
//                    val checkinTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkIn.checkin_time)
//                    checkinTime != null && checkinTime >= startDate && checkinTime <= currentDate
//                }
//                checkInsInRange?.size ?: 0
//            } else {
//                // Handle the API call failure
//                Log.e("APIError", "API call failed with code: ${response.code()}")
//                0 // Return 0 for failure case
//            }
//        }
//    }

//    fun getCheckOutCount(employee_id : String): Int = runBlocking {
//        return@runBlocking withContext(Dispatchers.IO) {
//            // Make the API call on the IO dispatcher
//            val call = RetrofitgetCheckOut.apiService.getData(employee_id)
//            val response = call.execute()
//
//            if (response.isSuccessful) {
//                val resultMap = response.body()
//                val currentDate = Date()
//                val calendar = Calendar.getInstance()
//                calendar.add(Calendar.DAY_OF_MONTH, -30)
//                val startDate = calendar.time
//
//                val checksOutRange = resultMap?.filter { (_, checkOut) ->
//                    val checkoutTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkOut.checkout_time)
//                    checkoutTime != null && checkoutTime >= startDate && checkoutTime <= currentDate
//                }
//                checksOutRange?.size ?: 0
//              //  Log.d("checkOut","${checksOutRange?.size}")
//            } else {
//                // Handle the API call failure
//                Log.e("APIError", "API call failed with code: ${response.code()}")
//                0 // Return 0 for failure case
//            }
//        }
//    }


//    fun getCheckInCount(): Int = runBlocking {
//        return@runBlocking withContext(Dispatchers.IO) {
//            // Make the API call on the IO dispatcher
//            val call = RetrofitgetcheckIn.apiService.getData("M20543")
//            val response = call.execute()
//
//            if (response.isSuccessful) {
//                val resultMap = response.body()
//                resultMap?.size ?: 0
//            } else {
//                // Handle the API call failure
//                Log.e("APIError", "API call failed with code: ${response.code()}")
//                0 // Return 0 for failure case
//            }
//        }
//    }










    //    fun generatePdf(context: Context, parsedData: ParsedData,name:String) {
//        val document = PdfDocument()
//        val marginDp = 10
//        val marginPx = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
//            context.resources.displayMetrics
//        ).toInt()
//
////        val pageInfo = PdfDocument.PageInfo.Builder(
////            1080 - 2 * marginPx, 2160 - 2 * marginPx, 1
////        ).create()
//
//       // showprogressbar(context)
//      //  val pageCount = calculatePageCount()
//        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 2).create() // A4 size in points (1 point = 1/72 inch)
//        val page = document.startPage(pageInfo)
//        val canvas = page.canvas
//
//        // Inflate the layout
//        val inflater = LayoutInflater.from(context)
//        val layout = inflater.inflate(R.layout.report_format, null) as LinearLayout
//
//        // Set the parsed values to the layout views
//        val headerTextView: TextView = layout.findViewById(R.id.headerTextView)
//        val dateDurationTextView: TextView = layout.findViewById(R.id.dateDurationTextView)
//        val employeeNameTextView: TextView = layout.findViewById(R.id.employeeNameTextView)
//        val employeeIDTextView: TextView = layout.findViewById(R.id.employeeIDTextView)
//        val companyIDTextView: TextView = layout.findViewById(R.id.companyIDTextView)
//        val companyNameTextView: TextView = layout.findViewById(R.id.companyNameTextView)
//        val checkInCountTextView: TextView = layout.findViewById(R.id.checkInCountTextView)
//        val checkOutCountTextView: TextView = layout.findViewById(R.id.checkOutCountTextView)
//        val alertDetailsListView: RecyclerView = layout.findViewById(R.id.alertDetailsListView)
//
//        headerTextView.text = "INWORK – Monthly Report"
//        dateDurationTextView.text = "Date: ${parsedData.date}"
//        employeeNameTextView.text = "Employee Name: ${parsedData.employeeName}"
//        employeeIDTextView.text = "Employee ID: ${parsedData.employeeID}"
//        companyIDTextView.text = "Company ID: ${parsedData.companyID}"
//        companyNameTextView.text = "Company Name: ${parsedData.companyName}"
//        checkInCountTextView.text = "Check In Count: ${parsedData.checkInCount}"
//        checkOutCountTextView.text = "Check Out Count: ${parsedData.checkOutCount}"
//
//        Log.d("alertnotification","${parsedData.companyID} ${parsedData.employeeID}")
//
//
//        val sharedPreferences = context.getSharedPreferences("Report",Context.MODE_PRIVATE)
//        sharedPreferences.edit().putString("ReportList",null).apply()
//        Log.d("ReportList1",sharedPreferences.getString("ReportList",null).toString())
//
//        // Set up the RecyclerView with the parsed alert details data
//        val layoutManager = LinearLayoutManager(context)
//        alertDetailsListView.layoutManager = layoutManager
//        val alertDetailsAdapter = AlertDetailsAdapter(parsedData.alertDetailsList)
//        alertDetailsListView.adapter = alertDetailsAdapter
//        Log.d("alertnotification","enter not null ${parsedData.alertDetailsList}")
////
////        layout.measure(
////            View.MeasureSpec.makeMeasureSpec(page.canvas.width - 2 * marginPx, View.MeasureSpec.EXACTLY),
////            View.MeasureSpec.makeMeasureSpec(page.canvas.height - 2 * marginPx, View.MeasureSpec.EXACTLY)
////        )
////
////        layout.layout(
////            marginDp, 0,
////            page.canvas.width - marginPx, page.canvas.height - marginPx
////        )
//
//        // Measure and layout the view
//        val widthSpec = View.MeasureSpec.makeMeasureSpec(page.canvas.width, View.MeasureSpec.EXACTLY)
//        val heightSpec = View.MeasureSpec.makeMeasureSpec(page.canvas.height, View.MeasureSpec.EXACTLY)
//       layout.measure(widthSpec, heightSpec)
//       layout.layout(0, 0, page.canvas.width, page.canvas.height)
//
//
//        // Draw the layout on the canvas
//        Log.d("width","${layout.width}")
//        layout.draw(canvas)
//
//        document.finishPage(page)
//
//        val directory = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//
//        // Save the PDF file
//      //  pdfGenerator(context,parsedData.alertDetailsList)
//
//        val file = File(directory, "$name monthly_report.pdf").absolutePath
//        try {
//            document.writeTo(FileOutputStream(file))
//     //       dismissprogressbar()
//            Log.d("pdf","generated")
//          //  Toast.makeText(context,"Pdf Saved Successfully",Toast.LENGTH_SHORT).show()
//            pdfGenerator(context,parsedData.alertDetailsList)
//        } catch (e: IOException) {
//            e.printStackTrace()
//         //   dismissprogressbar()
//        }
//
//        document.close()
//    }
//    fun pdfGenerator(context: Context, alerts: List<AlertDetails>) {
//        // Define the number of alerts per page
//        val alertsPerPage = 15
//
//        // Create the PDF document
//        val pdfDocument = PdfDocument()
//
//        // Create a temporary file to save the PDF
//        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val file = File(downloadsDirectory, "sample.pdf")
//
//        // Create a page info object
//        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 1).create() // A4 size: 595x842 points
//
//        // Get the total number of alerts and calculate the number of pages needed
//        val totalAlerts = alerts.size
//        val totalPages = (totalAlerts + alertsPerPage - 1) / alertsPerPage
//
//        // Iterate through the pages
//        for (page in 0 until totalPages) {
//            // Create a new list of alerts for the current page
//            val startIndex = page * alertsPerPage
//            val endIndex = minOf(startIndex + alertsPerPage, totalAlerts)
//            val pageAlerts = alerts.subList(startIndex, endIndex)
//
//            // Create a RecyclerView with the alerts for the current page
//            val recyclerView = createRecyclerView(context, pageAlerts)
//
//            // Measure and layout the RecyclerView
//            recyclerView.measure(
//                View.MeasureSpec.makeMeasureSpec(pageInfo.pageWidth, View.MeasureSpec.EXACTLY),
//                View.MeasureSpec.makeMeasureSpec(pageInfo.pageHeight, View.MeasureSpec.EXACTLY)
//            )
//            recyclerView.layout(0, 0, recyclerView.measuredWidth, recyclerView.measuredHeight)
//
//            // Start a new PDF page
//            val currentPage = pdfDocument.startPage(pageInfo)
//            val canvas = currentPage.canvas
//            canvas.drawColor(Color.WHITE)
//
//            // Draw the RecyclerView onto the current page
//            recyclerView.draw(canvas)
//
//            // Finish the current page
//            pdfDocument.finishPage(currentPage)
//        }
//
//        // Save the PDF document to a file
//        pdfDocument.writeTo(FileOutputStream(file))
//        Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()
//
//        // Close the PDF document
//        pdfDocument.close()
//    }

//    private fun createRecyclerView(context: Context, alerts: List<AlertDetails>): RecyclerView {
//        // Create a RecyclerView with your data and layout manager
//        val recyclerView = RecyclerView(context)
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        val alertDetailsAdapter = AlertDetailsAdapter(alerts)
//        recyclerView.adapter = alertDetailsAdapter
//
//        return recyclerView
//    }



    // @SuppressLint("InflateParams", "SetTextI18n")
//    fun generatePdf(context: Context, parsedData: ParsedData, name: String) {
//        val document = PdfDocument()
//
//        val pageInfo = PdfDocument.PageInfo.Builder(1440, 2560, 1).create()
//        val pageWidth = pageInfo.pageWidth
//        val pageHeight = pageInfo.pageHeight
//
//        val directory = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//        val marginDp = 10
//        val marginPx = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
//            context.resources.displayMetrics
//        ).toInt()
//
//        val inflater = LayoutInflater.from(context)
//        val layout = inflater.inflate(R.layout.report_format, null) as LinearLayout
//
//        // Set up the layout and content
//
//        val headerTextView: TextView = layout.findViewById(R.id.headerTextView)
//        val dateDurationTextView: TextView = layout.findViewById(R.id.dateDurationTextView)
//        val employeeNameTextView: TextView = layout.findViewById(R.id.employeeNameTextView)
//        val employeeIDTextView: TextView = layout.findViewById(R.id.employeeIDTextView)
//        val companyIDTextView: TextView = layout.findViewById(R.id.companyIDTextView)
//        val companyNameTextView: TextView = layout.findViewById(R.id.companyNameTextView)
//        val checkInCountTextView: TextView = layout.findViewById(R.id.checkInCountTextView)
//        val checkOutCountTextView: TextView = layout.findViewById(R.id.checkOutCountTextView)
//        val alertDetailsLinearLayout: LinearLayout = layout.findViewById(R.id.alertDetailsLinearLayout)
//
//        headerTextView.text = "INWORK – Monthly Report"
//        dateDurationTextView.text = "Date: ${parsedData.date}"
//        employeeNameTextView.text = "Employee Name: ${parsedData.employeeName}"
//        employeeIDTextView.text = "Employee ID: ${parsedData.employeeID}"
//        companyIDTextView.text = "Company ID: ${parsedData.companyID}"
//        companyNameTextView.text = "Company Name: ${parsedData.companyName}"
//        checkInCountTextView.text = "Check In Count: ${parsedData.checkInCount}"
//        checkOutCountTextView.text = "Check Out Count: ${parsedData.checkOutCount}"
//
//        val alertsPerPage = calculateAlertsPerPage(context, pageHeight)
//
//        // Split alerts into pages
//
//        val alerts = parsedData.alertDetailsList
//        var pageIndex = 1
//        var currentPage = document.startPage(pageInfo)
//
//        for (i in alerts.indices) {
//            val alert = alerts[i]
//
//            if (i > 0 && i % alertsPerPage == 0) {
//                document.finishPage(currentPage)
//                pageIndex++
//                currentPage = document.startPage(pageInfo)
//            }
//
//            val alertView = inflater.inflate(R.layout.item_alert_details, null) as LinearLayout
//            // Set up alertView with alert data
//
//            // Add alertView to layout
//            alertDetailsLinearLayout.addView(alertView)
//        }
//
//        // Draw the last page
//        document.finishPage(currentPage)
//
//        val outputStream = FileOutputStream(File(directory, "$name.pdf"))
//
//        try {
//            document.writeTo(outputStream)
//            document.close()
//            outputStream.close()
//
//            Toast.makeText(context, "PDF Saved Successfully", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    private fun calculateAlertsPerPage(context: Context, pageHeight: Int): Int {
//        val inflater = LayoutInflater.from(context)
//        val layout = inflater.inflate(R.layout.report_format, null) as LinearLayout
//        val alertDetailsListView: RecyclerView = layout.findViewById(R.id.alertDetailsListView)
//
//        val layoutManager = LinearLayoutManager(context)
//        alertDetailsListView.layoutManager = layoutManager
//        val alertDetailsAdapter = AlertDetailsAdapter(ArrayList()) // Empty adapter for measurement
//        alertDetailsListView.adapter = alertDetailsAdapter
//
//        val alertItemHeight = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP, 72f,
//            context.resources.displayMetrics
//        ).toInt() // Adjust this value based on your item height
//
//        val marginDp = 10
//        val marginPx = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(),
//            context.resources.displayMetrics
//        ).toInt()
//
//        val contentHeight = pageHeight - 2 * marginPx
//        val remainingHeight = contentHeight - layout.height
//        val alertsPerPage = remainingHeight / alertItemHeight
//
//        return alertsPerPage
//    }








//
//    private fun showprogressbar(context: Context){
//        progressDialog = ProgressDialog(context)
//        progressDialog?.setMessage("downloading...")
//        progressDialog?.setCancelable(false)
//        progressDialog?.show()
//
//    }
}

