package com.solutions.inwork.Admin.dataclasses

data class LeaveData(
    val company_id: String,
    val company_name: String,
    val employee_id: String,
    val employee_name: String,
    val designation: String,
    val leave_date: String,
    val leave_reason: String,
    val time_stamp: String,
    val approved : Int?,
    val approval_time_stamp : String?,
    val till_date: String
)

//"company_id": "INVYU1",
//"company_name": "",
//"employee_id": "M20554",
//"employee_name": "Sai Krishna Thogaru",
//"designation": "Manager",
//"leave_date": "2023-04-20",
//"leave_reason": "Vacation",
//"time_stamp": null,
//"approved": 1,
//"approval_time_stamp": "2023-07-06 15:15:11"