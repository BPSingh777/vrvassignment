package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.Admin.dataclasses.CheckIn
import com.solutions.inwork.Admin.dataclasses.CheckInAndOut
import com.solutions.inwork.Admin.dataclasses.CheckOut
import com.solutions.inwork.Admin.dataclasses.EmployeeDetails
import com.solutions.inwork.Admin.dataclasses.ParsedAttendanceData
import com.solutions.inwork.Admin.dataclasses.ParsedData
import com.solutions.inwork.Admin.dataclasses.ParsedScreenData
import com.solutions.inwork.Admin.dataclasses.Screentimedata
import com.solutions.inwork.PdfClass
import com.solutions.inwork.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class EmployeeReportAdapter(private var reportlist : List<EmployeeDetails>) : RecyclerView.Adapter<EmployeeReportAdapter.ReportViewholder>() {


    fun submitList(newList: List<EmployeeDetails>) {
        reportlist = newList
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.employee_report_item,parent,false)
        return ReportViewholder(view)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: ReportViewholder, position: Int) {

        val empdata = reportlist[position]
        holder.bind(holder.itemView.context, empdata)
    }

    override fun getItemCount(): Int {
        return reportlist.size
    }


    class ReportViewholder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val EmployeeName : TextView = itemView.findViewById(R.id.EmpName)
        private val EmployeeId : TextView = itemView.findViewById(R.id.EmpId)
        private val RepotViewbtn : Button = itemView.findViewById(R.id.EmpReportBtn)
        private val AttendaceViewbtn : Button = itemView.findViewById(R.id.EmpAttendanceReportBtn)
        private val ScreenReportbtn : Button = itemView.findViewById(R.id.EmpScreentTimeReportBtn)

        private val progressDialog = ProgressDialog(itemView.context)

        init {
            progressDialog.apply {
                setMessage("Generating PDF...")
                setCancelable(false)
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun bind(context: Context, employeeData: EmployeeDetails) {

            val sharedPreferences = context.getSharedPreferences("ADMIN_PREF",Context.MODE_PRIVATE)
            val companyname = sharedPreferences.getString("company_name",null)

            EmployeeId.text = employeeData.employee_id
            EmployeeName.text = "${employeeData.first_name} ${employeeData.last_name}"

            RepotViewbtn.setOnClickListener {
               progressDialog.show()
                PdfClass().fetchNotifications(employeeData.employee_id.toString(), employeeData.company_id.toString(), context) { notificationsList ->
                    if (notificationsList != null) {

                        PdfClass().getCheckInCount(employeeData.employee_id.toString()){ checkInCount ->
                            PdfClass().getCheckOutCount(employeeData.employee_id.toString()){ checkOutCount ->

                                val parsedData = ParsedData(
                                    date = LocalDate.now().toString(),
                                    employeeName = "${employeeData.first_name} ${employeeData.last_name}",
                                    employeeID = "${employeeData.employee_id}",
                                    companyID = employeeData.company_id.toString(),
                                    companyName = companyname.toString(),
                                    checkInCount = checkInCount,
                                    checkOutCount = checkOutCount,
                                    alertDetailsList = notificationsList
                                )

                                PdfClass().generatePdfAsync(context, parsedData, employeeData.first_name.toString(),progressDialog){
                                    progressDialog.dismiss()
                                }
                                // PdfClass().generatePdf(context,parsedData,employeeData.first_name.toString())
                            }

                        }
                    } else {
                        // Handle the case where fetching notifications failed
                        progressDialog.dismiss()
                        Toast.makeText(context,"Notifications are null",Toast.LENGTH_SHORT).show()
                        Log.d("failure","Failed to fetch notifications")
                    }
                }
            }


            AttendaceViewbtn.setOnClickListener {
               progressDialog.show()
                GlobalScope.launch(Dispatchers.Main) {
                    Log.d("checkIn/Out1", "${PdfClass().getCheckOutInfo(employeeData.employee_id.toString())}")
                    Log.d("checkIn/Out1", "${PdfClass().getCheckInInfo(employeeData.employee_id.toString())}")
                    val checkOutList: List<CheckOut>? = PdfClass().getCheckOutInfo(employeeData.employee_id.toString())
                    val checkInList: List<CheckIn>? = PdfClass().getCheckInInfo(employeeData.employee_id.toString())
//
//                PdfClass().getCheckOutInfo(employeeData.employee_id.toString()) { checkOutList ->
//                    PdfClass().getCheckInInfo(employeeData.employee_id.toString()){checkInList ->

                        if (checkOutList!=null || checkInList!=null){
                            val matchedList = PdfClass().matchCheckInAndOut(checkInList, checkOutList)
                            Log.d("checkIn/Out1", "${matchedList}")

                            // Assuming your list of CheckInAndOut objects is named 'checkInsAndOuts'
                            val sortedList = matchedList.sortedWith(compareBy({ it.date }, { it.checkInTime ?: it.checkOutTime }))
                            Log.d("Sorted list", "${matchedList}")
// Iterate over the sorted list
                            for (item in sortedList) {
                                // Process each item in the sorted order
                                println(item)

                            }


                            if (sortedList != null) {



                         PdfClass().getCheckInCount(employeeData.employee_id.toString()) { checkInCount ->
                              PdfClass().getCheckOutCount(employeeData.employee_id.toString()) { checkOutCount ->

                                        val parsedData = ParsedAttendanceData(
                                            date = LocalDate.now().toString(),
                                            employeeName = "${employeeData.first_name} ${employeeData.last_name}",
                                            employeeID = "${employeeData.employee_id}",
                                            companyID = employeeData.company_id.toString(),
                                            companyName = companyname.toString(),
                                            checkInCount = checkInCount,
                                            checkOutCount = checkOutCount,
                                            alertDetailsList = sortedList.mapIndexed { index, checkInAndOut ->
                                                CheckInAndOut(
                                                    sno = (index + 1).toString(),
                                                    date = checkInAndOut.date,
                                                    checkInTime = checkInAndOut.checkInTime,
                                                    checkOutTime = checkInAndOut.checkOutTime,
                                                    timeSpent = checkInAndOut.timeSpent,
                                                    remarks = checkInAndOut.remarks
                                                )
                                            }
                                        )
                                        PdfClass().generateAttenancePdfAsync(
                                            context,
                                            parsedData,
                                            {employeeData.first_name}.toString(),progressDialog){
                                            progressDialog.dismiss()
                                        }

//                                        PdfClass().generateAttendancePdf(context,parsedData,"${employeeData.first_name} ${employeeData.last_name}")
  }
  }
                            } else{
                                 progressDialog.dismiss()
                                Toast.makeText(context,"Notifications are null",Toast.LENGTH_SHORT).show()
                                Log.d("failure","Failed to fetch notifications")
                            }
                        }else{
                              progressDialog.dismiss()
                            Toast.makeText(context,"Notifications are null",Toast.LENGTH_SHORT).show()
                        }
//
//                    }
//
//                }





               }


            }


            ScreenReportbtn.setOnClickListener {
                progressDialog.show()
                GlobalScope.launch(Dispatchers.Main) {
                    val screenTime = PdfClass().screentimeDetails(employeeData.company_id.toString(),employeeData.employee_id.toString())

                    if(screenTime!=null){

                        val parsedData = ParsedScreenData(
                            date = LocalDate.now().toString(),
                            employeeName = "${employeeData.first_name} ${employeeData.last_name}",
                            employeeID = "${employeeData.employee_id}",
                            companyID = employeeData.company_id.toString(),
                            companyName = companyname.toString(),
                            alertDetailsList = screenTime.mapIndexed { index, screentimedata ->
                                Screentimedata(
                                    (index+1).toString(),
                                    screentimedata.employee_id,
                                    screentimedata.company_name,
                                    screentimedata.employee_id,
                                    screentimedata.employee_name,
                                    screentimedata.designation,
                                    screentimedata.work_start_time,
                                    screentimedata.work_end_time,
                                    screentimedata.whatsapp_duration,
                                    screentimedata.facebook_duration,
                                    screentimedata.instagram_duration,
                                    screentimedata.twitter_duration,
                                    screentimedata.news_duration,
                                    screentimedata.games_duration,
                                    screentimedata.calls_duration,
                                    screentimedata.others_duration,
                                    screentimedata.time_stamp
                                )
                            }
                        )
                        PdfClass().generateScreenPdfAsync(context,parsedData,{employeeData.first_name}.toString(),progressDialog){
                            progressDialog.dismiss()
                        }
                    }else{
                        progressDialog.dismiss()
                        Toast.makeText(context,"Notifications are null",Toast.LENGTH_SHORT).show()
                    }

                }

            }


        }
    }
}