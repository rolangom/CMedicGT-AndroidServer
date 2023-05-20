package com.rolangom.cmedicgt.shared

open class CustomExceptions(val status: Int, val description: String) : Exception(description)

class MissingParamsException(param: String) : CustomExceptions(100, "Missing parameter: $param")
class GeneralException(description: String) : CustomExceptions(999, description)