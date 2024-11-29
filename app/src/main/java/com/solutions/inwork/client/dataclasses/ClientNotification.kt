package com.solutions.inwork.client.dataclasses

data class ClientNotification(
    var company_id : String,
    var title : String,
    var employee_id : String,
    var notification : String,
    var notification_date : String
)
