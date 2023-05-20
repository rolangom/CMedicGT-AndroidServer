package com.rolangom.cmedicgt.domains.auth

interface AuthRepo {
    suspend fun createAccount(email: String, password: String)
    suspend fun login(email: String, password: String)
    suspend fun logout()
}
