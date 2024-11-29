package com.solutions.inwork.client.dataclasses

data class ClientemailInfo(
    var email_exists : Int?,
    var company_id :String?,
    var company_name : String?,
    var employee_id : String?,
    var first_name : String?,
    var work_start_time : String?,
    var work_end_time : String?
)
