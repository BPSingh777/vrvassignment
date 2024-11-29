package com.solutions.inwork.Admin.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.solutions.inwork.Admin.dataclasses.LeaveData
import com.solutions.inwork.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.solutions.inwork.retrofitPost.PostLeaveStatusAdmin
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaveAdapter(private var context: Context,private var Leavelist : List<LeaveData>) : RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder>() {

    fun submitList(newList: List<LeaveData>) {
        Leavelist = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leave_data_item,parent,false)
        return LeaveViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val data = Leavelist[position]

        holder.EmployeeIdTextView.text = "Employee ID : " + data.employee_id
        holder.EmployeeNameTextView.text = "Employee Name : ${ data.employee_name }"
        holder.designationTextView.text = "Designation : ${ data.designation }"
        holder.leaveReasonTextView.text = "Leave Reason : ${ data.leave_reason }"
        holder.leavedataTextView.text = "Leave date : ${ data.leave_date }"
        holder.leavetilldataTextView.text = "Leave till date : ${ data.till_date }"


        if(data.approved == 1 || data.approved == 0){
            holder.btnlayout.visibility = View.GONE
            if(data.approved == 1){
                holder.leavestatus.text = "Leave Status : Approved"
            }
            else{
                holder.leavestatus.text = "Leave Status : Declined"
            }
        }
        else{
            holder.leavestatus.visibility = View.GONE
            holder.approvebtn.setOnClickListener {
                holder.btnlayout.visibility = View.GONE
                holder.leavestatus.visibility = View.VISIBLE
                holder.leavestatus.text = "Leave Status : Approved"
                ApproveLeave(data.company_id.toString(),data.employee_id,data.leave_date,"1")
                Notify(data.company_id,data.employee_id,"Your Leave is Approved")
            }
            holder.declinebtn.setOnClickListener {
                holder.btnlayout.visibility = View.GONE
                holder.leavestatus.visibility = View.VISIBLE
                holder.leavestatus.text = "Leave Status : Declined"
                ApproveLeave(data.company_id.toString(),data.employee_id,data.leave_date,"0")
                Notify(data.company_id,data.employee_id,"Your Leave is Declined")
            }
        }


    }

    override fun getItemCount(): Int {
        return Leavelist.size
    }
    class LeaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val EmployeeIdTextView: TextView = itemView.findViewById(R.id.textViewEmployeeId)
        val EmployeeNameTextView: TextView = itemView.findViewById(R.id.textViewEmployeeName)
        val designationTextView: TextView = itemView.findViewById(R.id.textViewDesignation)
        val leavedataTextView: TextView = itemView.findViewById(R.id.textViewLeaveDate)
        val leavetilldataTextView: TextView = itemView.findViewById(R.id.textViewLeavetillDate)
        val leaveReasonTextView: TextView = itemView.findViewById(R.id.textViewLeaveReason)
        val approvebtn : Button = itemView.findViewById(R.id.approvebtn)
        val declinebtn : Button = itemView.findViewById(R.id.declinebtn)
        val btnlayout : LinearLayout = itemView.findViewById(R.id.btnLayout)
        val leavestatus: TextView = itemView.findViewById(R.id.textViewLeavestatus)


    }
    fun Notify(companyId: String,employeeId: String,message: String){
        val database = FirebaseDatabase.getInstance()
        val path = database.getReference(companyId).child(employeeId)

        path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tokenList = mutableListOf<String>()
                for (tokenSnapshot in dataSnapshot.children) {
                    val token = tokenSnapshot.getValue(String::class.java)
                    if (token != null) {
                       // tokenList.add(token)


                        val url = "https://fcm.googleapis.com/fcm/send"

                        val jsonObject = JSONObject()
                        val notificationObject = JSONObject()
                        notificationObject.put("title", "Inwork Leave Request")
                        notificationObject.put("message", message)
                        jsonObject.put("data", notificationObject)
                        jsonObject.put("to", token)

                        val request: JsonObjectRequest = object : JsonObjectRequest(
                            Method.POST, url, jsonObject,
                            { response ->

                                // Handle the response here
                                Log.d("volley","success")
                              //  Toast.makeText(context,"Successful", Toast.LENGTH_SHORT).show()
                            },
                            { error ->


                                // Handle the error here
                                Log.d("volley","fail")

                                Toast.makeText(context,"Failed to Notify", Toast.LENGTH_SHORT).show()

                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Authorization"] ="key=AAAAa8gk1zc:APA91bEXlVMS52U7wSCnoNjRwmS0SsZWC__LZIh3cazVJDB7U_nN3x8VvvLR8T2YOckUMuXD8FB66v5pNTLqILQUFTecetIE-RMpzGyJCM-NlqjVOv6n3jmG_xdjxTzphB82Yf41kP-k"
                                headers["Content-Type"] = "application/json"
                                return headers
                            }
                        }
                        Volley.newRequestQueue(context).add(request)





                    }
                }
                // Do something with the tokenList for the specific employee
                // For example, you can store it in a variable or use it as needed
                // You can also call a function passing the tokenList as an argument
                // for further processing.
               // processEmployeeTokens(tokenList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors
            }
        })

    }


    fun ApproveLeave(companyId: String,employeeId: String,leaveDate: String,approved:String){

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        val timestamp = dateFormat.format(currentDate)



        val jsonObject = JSONObject().apply {
            put("company_id",companyId)
            put("employee_id",employeeId)
            put("leave_date",leaveDate)
            put("approved",approved)
            put("approval_time_stamp",timestamp.toString())
        }


        val request = RequestBody.create("application/json".toMediaTypeOrNull(),jsonObject.toString())

        val apiservice = PostLeaveStatusAdmin.apiService
        val call = apiservice.postData(request)


        call.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("LeaveApprove","success")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context,"Failed to approve/decline the leave",Toast.LENGTH_SHORT).show()
            }

        })
    }

}
