package com.example.scannerapp.data.repository

import android.app.Application
import com.example.scannerapp.data.database.PdfDatabase
import com.example.scannerapp.data.models.PdfEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn


class PdfRepository(application: Application) {
    private val pdfDao = PdfDatabase.getInstance(application).pdfDao

    fun getPdfList() = pdfDao.getAllPdfs().flowOn(Dispatchers.IO)


    suspend fun insertPdf(pdfEntity: PdfEntity): Long {
        return pdfDao.insertPdf(pdfEntity)
    }


    suspend fun deletePdf(pdfEntity: PdfEntity): Int {
        return pdfDao.deletePdf(pdfEntity)
    }


    suspend fun updatePdf(pdfEntity: PdfEntity):Int {
        return pdfDao.updatePdf(pdfEntity)
    }
}