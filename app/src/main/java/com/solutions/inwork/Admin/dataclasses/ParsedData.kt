package com.solutions.inwork.Admin.dataclasses

data class ParsedData(
    val date: String,
    val employeeName: String,
    val employeeID: String,
    val companyID: String,
    val companyName: String,
    val checkInCount: Int,
    val checkOutCount: Int,
    val alertDetailsList: List<AlertDetails>
)