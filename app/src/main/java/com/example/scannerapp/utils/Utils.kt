package com.example.scannerapp.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream


fun getFileSize(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    val fileSizeInBytes = file.length()
    val fileSizeInKB = fileSizeInBytes / 1024
    return if (fileSizeInKB > 1024) {
        val fileSizeInMB = fileSizeInKB / 1024
        "$fileSizeInMB MB"
    } else {
        "$fileSizeInKB KB"
    }
}

fun copyPdfFileToAppDirectory(context: Context, pdfUri: Uri, destinationFileName: String) {
    val inputStream = context.contentResolver.openInputStream(pdfUri)
    val outputFile = File(context.filesDir, destinationFileName)
    FileOutputStream(outputFile).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
}

fun getFileUri(context: Context, fileName: String): Uri {
    val file = File(context.filesDir, fileName)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun renameFile(context: Context, oldFileName: String, newFileName: String) {
    val oldFile = File(context.filesDir, oldFileName)
    val newFile = File(context.filesDir, newFileName)
    oldFile.renameTo(newFile)
}

fun deleteFile(context: Context, fileName: String): Boolean {
    val oldFile = File(context.filesDir, fileName)
    return oldFile.deleteRecursively()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}