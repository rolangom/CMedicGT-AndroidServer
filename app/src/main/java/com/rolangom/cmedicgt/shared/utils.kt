package com.rolangom.cmedicgt.shared

import android.content.Context


fun readAssetsFile(context: Context, fileName: String): String {
    val fileContent = context.assets.open(fileName).bufferedReader().use { it.readText() }
    return fileContent
}