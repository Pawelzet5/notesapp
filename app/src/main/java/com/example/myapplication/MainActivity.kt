package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApplicationTheme {
                val notes by viewModel.notes.collectAsState()
                val showFavouritesOnly by viewModel.showFavouritesOnly.collectAsState()
                val isRefreshing by viewModel.syncInProgress.collectAsState()

                val layoutDirection = LocalLayoutDirection.current

                val gestureInsets = WindowInsets.safeGestures.asPaddingValues()
                val statusBarInset = WindowInsets.statusBars.asPaddingValues()
                val imeInset = WindowInsets.ime.asPaddingValues()
                val navigationInset = WindowInsets.navigationBars.asPaddingValues()
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::onRefresh
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(top = statusBarInset.calculateTopPadding())
                    ) {
                        var contentInput by rememberSaveable { mutableStateOf("") }
                        var titleInput by rememberSaveable { mutableStateOf("") }
                        Row(Modifier.padding(8.dp)) {
                            FilterChip(
                                selected = showFavouritesOnly,
                                onClick = { viewModel.setShowFavouritesOnly(!showFavouritesOnly) },
                                label = { Text("Ulubione") }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding =
                                PaddingValues(
                                    start = gestureInsets.calculateStartPadding(layoutDirection),
                                    end = gestureInsets.calculateEndPadding(layoutDirection),
                                ),
                        ) {
                            items(notes, key = { it.localId }) { note ->
                                DismissableNote(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 300),
                                        fadeOutSpec = tween(durationMillis = 200),
                                        placementSpec = spring(
                                            stiffness = Spring.StiffnessLow,
                                            dampingRatio = Spring.DampingRatioLowBouncy
                                        )
                                    ),
                                    note = note,
                                    onDismiss = { viewModel.deleteNote(note) },
                                    onToggleFavorite = { viewModel.toggleNoteFavourite(note) }
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
                            top = if (showTitle) 0.dp else 8.dp,
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
