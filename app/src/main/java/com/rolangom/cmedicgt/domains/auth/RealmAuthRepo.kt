package com.rolangom.cmedicgt.domains.auth

import com.rolangom.cmedicgt.app
import io.realm.kotlin.mongodb.Credentials

object RealmAuthRepo : AuthRepo {
    override suspend fun createAccount(email: String, password: String) {
        app.emailPasswordAuth.registerUser(email, password)
    }

    override suspend fun login(email: String, password: String) {
        app.login(Credentials.emailPassword(email, password))
    }

    override suspend fun logout() {
        app.currentUser?.logOut()
    }
}