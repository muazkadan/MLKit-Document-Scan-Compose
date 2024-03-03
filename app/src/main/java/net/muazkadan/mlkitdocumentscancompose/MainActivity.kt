package net.muazkadan.mlkitdocumentscancompose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import net.muazkadan.mlkitdocumentscancompose.ui.theme.DocumentScanComposeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocumentScanComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val modes = listOf(
                        stringResource(R.string.full),
                        stringResource(R.string.base), stringResource(R.string.base_with_filter)
                    )
                    var expanded by remember { mutableStateOf(false) }
                    var selectedOptionText by remember { mutableStateOf(modes[0]) }
                    var enableGalleryImport by remember { mutableStateOf(true) }
                    var pageLimit by remember { mutableIntStateOf(2) }
                    var scannerMode by remember { mutableIntStateOf(GmsDocumentScannerOptions.SCANNER_MODE_FULL) }

                    val scannerLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                            if (result.resultCode == RESULT_OK) {
                                val gmsResult =
                                    GmsDocumentScanningResult.fromActivityResultIntent(result.data) // get the result
                                gmsResult?.pages?.let { pages ->
                                    pages.forEach { page ->
                                        val imageUri = page.imageUri // do something with the image
                                    }
                                }
                                gmsResult?.pdf?.let { pdf ->
                                    val pdfUri = pdf.uri // do something with the PDF
                                }
                            }
                        }

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.scanner_feature_mode))
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                ) {
                                    OutlinedTextField(
                                        // The `menuAnchor` modifier must be passed to the text field for correctness.
                                        modifier = Modifier.menuAnchor(),
                                        value = selectedOptionText,
                                        onValueChange = { selectedOptionText = it },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded
                                            )
                                        },
                                        readOnly = true,
                                    )
                                    // filter options based on text field value
                                    val filteringOptions =
                                        modes.filterNot {
                                            it.equals(
                                                selectedOptionText,
                                                ignoreCase = true
                                            )
                                        }
                                    if (filteringOptions.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                        ) {
                                            filteringOptions.forEach { selectionOption ->
                                                DropdownMenuItem(
                                                    text = { Text(selectionOption) },
                                                    onClick = {
                                                        selectedOptionText = selectionOption
                                                        expanded = false
                                                        scannerMode = when (selectionOption) {
                                                            context.getString(R.string.full) -> GmsDocumentScannerOptions.SCANNER_MODE_FULL
                                                            context.getString(R.string.base) -> GmsDocumentScannerOptions.SCANNER_MODE_BASE
                                                            context.getString(R.string.base_with_filter) -> GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER
                                                            else -> GmsDocumentScannerOptions.SCANNER_MODE_FULL
                                                        }
                                                    },
                                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.enable_gallery_import))
                                Checkbox(
                                    checked = enableGalleryImport,
                                    onCheckedChange = { enableGalleryImport = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.set_page_limit_per_scan))
                                OutlinedTextField(
                                    value = "$pageLimit",
                                    onValueChange = {
                                        if (it.isNotEmpty() && it.isDigitsOnly()) {
                                            pageLimit = it.toInt()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Button(onClick = {
                                val scannerOptions = GmsDocumentScannerOptions.Builder()
                                    .setGalleryImportAllowed(enableGalleryImport)
                                    .setPageLimit(pageLimit)
                                    .setResultFormats(
                                        GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                                        GmsDocumentScannerOptions.RESULT_FORMAT_PDF
                                    )
                                    .setScannerMode(scannerMode)
                                    .build()
                                val scanner = GmsDocumentScanning.getClient(scannerOptions)
                                scanner.getStartScanIntent(context as Activity)
                                    .addOnSuccessListener { intentSender ->
                                        scannerLauncher.launch(
                                            IntentSenderRequest.Builder(intentSender).build()
                                        )
                                    }.addOnFailureListener {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                it.localizedMessage
                                                    ?: getString(R.string.something_went_wrong)
                                            )
                                        }
                                    }
                            }) {
                                Text(stringResource(R.string.scan))
                            }
                        }
                    }
                }
            }
        }
    }
}