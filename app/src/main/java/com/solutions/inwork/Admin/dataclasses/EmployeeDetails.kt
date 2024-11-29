package com.solutions.inwork.Admin.dataclasses

data class EmployeeDetails(
    val company_id: String?,
    val employee_id: String?,
    val first_name: String?,
    val last_name: String?,
    val date_of_birth: String?,
    val designation: String?,
    val address: String?,
    val adhaar_number: String?,
    val mobile: String?,
    val email: String?,
    val office: String?,
    val work_start_time: String?,
    val work_end_time: String?,
    val work_effect_from_date: String?,
    val work_end_from_date: String?
)
