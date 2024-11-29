package com.solutions.inwork.Admin.dataclasses

data class ParsedScreenData (
    val date: String,
    val employeeName: String,
    val employeeID: String,
    val companyID: String,
    val companyName: String,
    val alertDetailsList: List<Screentimedata>
)