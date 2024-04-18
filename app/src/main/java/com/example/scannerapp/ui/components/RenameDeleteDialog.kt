package com.example.scannerapp.ui.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.scannerapp.R
import com.example.scannerapp.ui.viewModels.PdfViewModel
import com.example.scannerapp.utils.Resource
import com.example.scannerapp.utils.deleteFile
import com.example.scannerapp.utils.renameFile
import com.example.scannerapp.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Date


@Composable
fun RenameDeleteDialog(pdfViewModel: PdfViewModel) {

    var newNameText by remember(pdfViewModel.currentPdfEntity) {
        mutableStateOf(
            pdfViewModel.currentPdfEntity?.name ?: ""
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    if (pdfViewModel.showRenameDialog) {
        Dialog(onDismissRequest = {
            pdfViewModel.showRenameDialog = false
        }) {
            Surface(
                shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.rename_pdf),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newNameText,
                        onValueChange = { newText -> newNameText = newText },

                        label = { Text(stringResource(R.string.pdf_name)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = {
                            pdfViewModel.currentPdfEntity?.let {
                                coroutineScope.launch {
                                    pdfViewModel.showRenameDialog = false
                                    pdfViewModel.loadingDialog = true
                                    if (deleteFile(context, it.name)) {
                                        pdfViewModel.deletePdf(it).flowOn(Dispatchers.IO).collect {
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
                                    } else {
                                        pdfViewModel.loadingDialog = false
                                        context.showToast("Something Went Wrong")
                                    }

                                }
                            }
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_delete),
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            pdfViewModel.showRenameDialog = false
                        }) { Text(stringResource(R.string.cancel)) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (newNameText.isEmpty()) {
                                context.showToast("File is Required")
                            } else {
                                pdfViewModel.currentPdfEntity?.let { pdf ->
                                    if (!pdf.name.equals(
                                            newNameText,
                                            true
                                        )
                                    ) {
                                        pdfViewModel.showRenameDialog = false
                                        pdfViewModel.loadingDialog = true
                                        renameFile(
                                            context,
                                            pdf.name,
                                            newNameText
                                        )

                                        val updatedPdf =
                                            pdf.copy(name = newNameText, lastModifiedTime = Date())
                                        coroutineScope.launch {
                                            pdfViewModel.updatePdf(updatedPdf)
                                                .flowOn(Dispatchers.IO).collect {
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
                                    } else {
                                        pdfViewModel.loadingDialog = false
                                    }
                                }
                            }

                        }) { Text(stringResource(R.string.update)) }
                    }
                }
            }
        }
    }
}