package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {
    fun saveUriToInternalStorage(context: Context, uri: Uri, folderName: String = "product_images"): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, folderName)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
