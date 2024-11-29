package com.solutions.inwork.Admin.dataclasses

data class AdminGetNotifications(
    var company_id : String,
    var employee_id : String,
    var title  : String,
    var notification: String,
    var notification_date: String
)
