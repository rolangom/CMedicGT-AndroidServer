package com.rolangom.cmedicgt.shared

data class ResponseBase<T>(
    val status: Int = 0,
    val data: T? = null,
    val message: String = "Success"
)