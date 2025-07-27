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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
                            DismissableNote(
                                modifier = Modifier.animateItem(),
                                note = note,
                                onDismiss = { viewModel.deleteNote(note) },
                                onToggleFavorite = { viewModel.toggleNoteFavorite(note) }
                            )
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
                                            viewModel.addNote(
                                                titleInput = titleInput,
                                                contentInput = contentInput
                                            )
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

val lorem =
    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

@Preview(showBackground = true)
@Composable
fun DismissableNoteNoTitlePreview() {
    MyApplicationTheme {

        DismissableNote(
            note = DbNote(1, 2, "", lorem, isFavourite = false),
            onDismiss = {}, onToggleFavorite = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DismissableNoteWithTitlePreview() {
    DismissableNote(
        note = DbNote(1, 2, "lorem", lorem, isFavourite = true),
        onDismiss = {}, onToggleFavorite = {}
    )
}

@Composable
fun DismissableNote(
    modifier: Modifier = Modifier,
    note: DbNote,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit
) {
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
            Column {
                val showTitle = note.title.isNotBlank()
                if (showTitle) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        FavoriteButton(note.isFavourite, onToggleFavorite)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                ) {
                    Text(
                        modifier = Modifier.weight(1f).padding(
                            start = 8.dp,
                            end = 8.dp,
                            top =  if (showTitle) 0.dp else 8.dp,
                            bottom = 8.dp
                        ),
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!showTitle)
                        FavoriteButton(note.isFavourite, onToggleFavorite)
                }

            }
        }
    }
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(modifier = Modifier, onClick = onClick) {
        if (isFavorite)
            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Add to Favorite")
        else
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Remove From Favorite"
            )
    }
}
