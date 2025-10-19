package com.productivityapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(navController: NavController, noteViewModel: NoteViewModel, noteId: String?) {
    val context = LocalContext.current
    val notes by noteViewModel.allNotes.collectAsState()
    val note = remember(notes, noteId) { notes.find { it.id == noteId } }

    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var body by remember(note) { mutableStateOf(note?.body ?: "") }
    var password by remember(note) { mutableStateOf(note?.password ?: "") }

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    context,
                    "Microphone permission is required for voice-to-text",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    // Speech recognizer launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results?.get(0) ?: ""
                
                // Add spoken text to body with proper spacing
                body = if (body.isBlank()) {
                    spokenText
                } else {
                    "$body $spokenText"
                }
                
                Toast.makeText(context, "Text added!", Toast.LENGTH_SHORT).show()
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, "Speech recognition cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Function to start voice recognition
    fun startVoiceRecognition() {
        // Check if permission is granted
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Launch speech recognizer
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to add text to your note")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            }
            
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Speech recognition is not available on this device",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // Request permission
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { startVoiceRecognition() },
                        enabled = true
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice to Text",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }
                    
                    val finalPassword = password.ifBlank { null }
                    val noteToSave = note?.copy(
                        title = title,
                        body = body,
                        password = finalPassword
                    ) ?: Note(
                        title = title,
                        body = body,
                        password = finalPassword
                    )

                    if (noteId == null) {
                        noteViewModel.insert(noteToSave)
                        Toast.makeText(context, "Note created", Toast.LENGTH_SHORT).show()
                    } else {
                        noteViewModel.update(noteToSave)
                        Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Body") },
                placeholder = { Text("Start typing or use voice-to-text...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (optional)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}
