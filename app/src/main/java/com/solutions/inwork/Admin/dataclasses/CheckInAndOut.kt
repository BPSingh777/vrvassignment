package com.solutions.inwork.Admin.dataclasses

data class CheckInAndOut(
    val sno : String?,
    val date: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val timeSpent: String?,
    val remarks : String?
)
