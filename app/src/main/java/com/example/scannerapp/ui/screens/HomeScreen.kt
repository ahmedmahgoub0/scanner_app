package com.example.scannerapp.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scannerapp.R
import com.example.scannerapp.data.models.PdfEntity
import com.example.scannerapp.ui.components.ErrorScreen
import com.example.scannerapp.ui.components.LoadingDialog
import com.example.scannerapp.ui.components.PdfLayout
import com.example.scannerapp.ui.components.RenameDeleteDialog
import com.example.scannerapp.ui.viewModels.PdfViewModel
import com.example.scannerapp.utils.Resource
import com.example.scannerapp.utils.copyPdfFileToAppDirectory
import com.example.scannerapp.utils.getFileSize
import com.example.scannerapp.utils.showToast
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(pdfViewModel: PdfViewModel) {
    LoadingDialog(pdfViewModel = pdfViewModel)
    RenameDeleteDialog(pdfViewModel = pdfViewModel)

    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val pdfList by pdfViewModel.pdfStateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult =
                GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pdf?.let { pdf ->
                pdfViewModel.loadingDialog = true
                val newDate = Date()
                val fileName = SimpleDateFormat(
                    "dd-MMM-yyyy HH:mm:ss",
                    Locale.getDefault()
                ).format(newDate) + ".pdf"

                copyPdfFileToAppDirectory(
                    context,
                    pdf.uri, fileName
                )

                val pdfEntity = PdfEntity(
                    UUID.randomUUID().toString(),
                    fileName,
                    getFileSize(context, fileName),
                    newDate
                )
                coroutineScope.launch {
                    pdfViewModel.insertPdf(pdfEntity).collect {
                        when (it) {
                            Resource.Idle -> {

                            }

                            Resource.Loading -> {
                                pdfViewModel.loadingDialog = true
                            }

                            is Resource.Success -> {
                                context.showToast(
                                    it.data
                                )
                            }

                            is Resource.Error -> {
                                pdfViewModel.loadingDialog = false
                                context.showToast(
                                    it.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder().setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL).build()
        )
    }
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                scanner.getStartScanIntent(activity).addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }.addOnFailureListener {
                    it.printStackTrace()
                    context.showToast(it.message.toString())
                }
            }, text = {
                Text(text = stringResource(R.string.scan))
            }, icon = {
                Icon(
                    painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                )
            })
        }, content = { paddingValues ->
            pdfList.DisplayResult(
                onLoading = {
                }, onSuccess = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        if (it.isEmpty()) {
                            item {
                                ErrorScreen("No Pdf")
                            }
                        } else {
                            items(items = it, key = { item ->
                                item.id
                            }) { pdf ->
                                PdfLayout(pdf = pdf, pdfViewModel = pdfViewModel)
                            }
                        }
                    }
                }, onError = {
                    ErrorScreen(it)
                })
        })
}
