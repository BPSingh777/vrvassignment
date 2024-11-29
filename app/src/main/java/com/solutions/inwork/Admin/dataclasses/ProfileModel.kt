package com.solutions.inwork.Admin.dataclasses

data class ProfileModel(
val Sl_No: Int,
val company_id: String,
val company_name: String,
val industry: String,
val managing_director: String,
val mobile: Long,
val email: String,
val address: String,
val location_lat: Double?,
val location_long: Double?,
val radius: Double?


)
