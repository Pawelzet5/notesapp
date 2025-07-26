package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.model.db.entity.DbNote
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApplicationTheme {
                val notes by viewModel.notes.collectAsState()

                val layoutDirection = LocalLayoutDirection.current

                val gestureInsets = WindowInsets.safeGestures.asPaddingValues()
                val statusBarInset = WindowInsets.statusBars.asPaddingValues()
                val imeInset = WindowInsets.ime.asPaddingValues()
                val navigationInset = WindowInsets.navigationBars.asPaddingValues()
                Column(modifier = Modifier.fillMaxSize()) {
                    var contentInput by rememberSaveable { mutableStateOf("") }
                    var titleInput by rememberSaveable { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding =
                            PaddingValues(
                                top = statusBarInset.calculateTopPadding(),
                                start = gestureInsets.calculateStartPadding(layoutDirection),
                                end = gestureInsets.calculateEndPadding(layoutDirection),
                            ),
                    ) {
                        items(notes) { note ->
                            DismissableNote(modifier = Modifier.animateItem(), note = note) {
                                viewModel.deleteNote(note)
                            }
                        }
                    }

                    Column(
                        Modifier
                        .fillMaxWidth()
                        .padding(
                            PaddingValues(
                                start =
                                    gestureInsets.calculateStartPadding(layoutDirection),
                                end = gestureInsets.calculateEndPadding(layoutDirection),
                                bottom = imeInset.calculateBottomPadding()
                                    .takeIf { it > 0.dp }
                                    ?: navigationInset.calculateBottomPadding()
                            )
                        )
                    ) {
                        TextField(
                            modifier =
                                Modifier.fillMaxWidth(),
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Tytuł notatki") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = contentInput,
                            onValueChange = { contentInput = it },
                            label = { Text("Treść notatki") },
                            keyboardActions =
                                KeyboardActions(
                                    onSend = {
                                        if (contentInput.isNotBlank()) {
                                            viewModel.addNote(contentInput, titleInput)
                                            contentInput = ""
                                            titleInput = ""
                                        }
                                    }
                                ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun DismissableNote(modifier: Modifier = Modifier, note: DbNote, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val confirmValueChange = { it: SwipeToDismissBoxValue -> it == StartToEnd }
    val positionalThreshold = { it: Float -> it / 3 * 2 }
    val dismissState =
        remember(
            note.localId
        ) {
            SwipeToDismissBoxState(Settled, density, confirmValueChange, positionalThreshold)
        }
    val backgroundColor by
    rememberUpdatedState(
        when (dismissState.dismissDirection) {
            StartToEnd -> lerp(Color.Transparent, Color.Red, dismissState.progress)
            else -> Color.Transparent
        }
    )
    if (dismissState.currentValue == StartToEnd) {
        LaunchedEffect(note.localId) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        modifier = modifier.fillMaxWidth(),
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(backgroundColor),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp),
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(modifier = Modifier.padding(16.dp), text = note.content)
        }
    }
}
