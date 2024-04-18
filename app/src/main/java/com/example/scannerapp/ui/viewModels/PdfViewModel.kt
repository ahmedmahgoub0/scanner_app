package com.example.scannerapp.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scannerapp.data.models.PdfEntity
import com.example.scannerapp.data.repository.PdfRepository
import com.example.scannerapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


class PdfViewModel(application: Application) : ViewModel() {

    private val pdfRepository = PdfRepository(application)

    var isSplashScreen by mutableStateOf(true)

    private val _pdfStateFlow =
        MutableStateFlow<Resource<List<PdfEntity>>>(Resource.Idle)
    val pdfStateFlow: StateFlow<Resource<List<PdfEntity>>>
        get() = _pdfStateFlow

    var currentPdfEntity: PdfEntity? by mutableStateOf(null)
    var showRenameDialog by mutableStateOf(false)
    var loadingDialog by mutableStateOf(false)

    init {
        viewModelScope.launch {
            delay(1500)
            isSplashScreen = false
        }
        viewModelScope.launch {
            pdfStateFlow.collect {
                when (it) {
                    is Resource.Error -> {
                        loadingDialog = false
                    }

                    Resource.Idle -> {}
                    Resource.Loading -> {
                        loadingDialog = true
                    }

                    is Resource.Success -> {
                        loadingDialog = false
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _pdfStateFlow.emit(Resource.Loading)
            pdfRepository.getPdfList().catch {
                it.printStackTrace()
                _pdfStateFlow.emit(Resource.Error(it.message.toString()))
            }.collect { result ->
                _pdfStateFlow.emit(Resource.Success(result))
            }
        }
    }

    suspend fun insertPdf(pdfEntity: PdfEntity) = flow {
        try {
            emit(Resource.Loading)
            val result = pdfRepository.insertPdf(pdfEntity)
            if (result.toInt() != -1) {
                emit(Resource.Success("Inserted Pdf Successfully"))
            } else {
                emit(Resource.Error("Something Went Wrong"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(Resource.Error(it.message.toString()))
    }

    suspend fun deletePdf(pdfEntity: PdfEntity) = flow {
        try {
            emit(Resource.Loading)
            val result = pdfRepository.deletePdf(pdfEntity)
            if (result != -1) {
                emit(Resource.Success("Deleted Pdf Successfully"))
            } else {
                emit(Resource.Error("Something Went Wrong"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(Resource.Error(it.message.toString()))
    }

    suspend fun updatePdf(pdfEntity: PdfEntity) = flow {
        try {
            emit(Resource.Loading)
            val result = pdfRepository.updatePdf(pdfEntity)
            if (result != -1) {
                emit(Resource.Success("Updated Pdf Successfully"))
            } else {
                emit(Resource.Error("Something Went Wrong"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO).catch {
        emit(Resource.Error(it.message.toString()))
    }

}