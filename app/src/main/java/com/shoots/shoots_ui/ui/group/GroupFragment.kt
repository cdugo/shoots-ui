package com.shoots.shoots_ui.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shoots.shoots_ui.R
import com.shoots.shoots_ui.data.model.Ranking
import com.shoots.shoots_ui.data.model.UserHistoricalRankings
import com.shoots.shoots_ui.ui.auth.AuthState
import com.shoots.shoots_ui.ui.auth.AuthViewModel
import com.shoots.shoots_ui.ui.formatDisplayScreenTime
import com.shoots.shoots_ui.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun GroupFragment(
    groupId: Int,
    navController: NavController,
    viewModel: GroupViewModel = viewModel(
        factory = GroupViewModelFactory(LocalContext.current, groupId)
    ),
    homeViewModel: HomeViewModel,
    authModel: AuthViewModel,
    onNavigateToPayouts: (Int) -> Unit
) {
    val groupState by viewModel.groupState.collectAsStateWithLifecycle()
    val isHistoricalView by viewModel.isHistoricalView.collectAsStateWithLifecycle()
    val authState by authModel.authState.collectAsStateWithLifecycle()
    val isEnterScreenTimeDialogVisible by homeViewModel.isEnterScreenTimeDialogVisible.collectAsStateWithLifecycle()

    GroupScreen(
        groupState = groupState,
        isHistoricalView = isHistoricalView,
        onToggleHistoricalView = viewModel::toggleHistoricalView,
        onNavigateBack = { navController.popBackStack() },
        onJoinGroup = viewModel::joinGroup,
        onAddScreenTime = homeViewModel::showEnterScreenTimeDialog,
        authState = authState,
        onNavigateToPayouts = onNavigateToPayouts
    )

    if (isEnterScreenTimeDialogVisible) {
        com.shoots.shoots_ui.ui.home.EnterScreenTimeDialog(
            onDismiss =
            homeViewModel::hideEnterScreenTimeDialog,
            onEnter = { time: Int ->
                homeViewModel.enterScreenTime(time)
                viewModel.loadGroupData()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupState: GroupState,
    isHistoricalView: Boolean,
    onToggleHistoricalView: () -> Unit,
    onAddScreenTime: () -> Unit,
    onNavigateBack: () -> Unit,
    onJoinGroup: (String) -> Unit,
    authState: AuthState,
    onNavigateToPayouts: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (groupState) {
                        is GroupState.Success -> Text(groupState.group.name)
                        else -> Text("Group")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleHistoricalView) {
                        Icon(Icons.Default.History, contentDescription = "Toggle Historical View")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (groupState) {
                is GroupState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is GroupState.Success -> {
                    when (val state = authState) {
                        is AuthState.Authenticated -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                if (groupState.members.filter { user -> state.user.id == user.id }
                                        .isNotEmpty()
                                ) {
                                    Text(
                                        text = if (isHistoricalView) "Historical Rankings" else "This Week's Rankings",
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        "Invite Code: " + groupState.group.code,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    if (groupState.weeklyRankings.filter { ranking: Ranking ->
                                            ranking.user.id == state.user.id
                                        }.isEmpty())
                                        SubmitYourTimeCard(onAddScreenTime)
                                } else {
                                    Text(
                                        text = if (isHistoricalView) "Historical Rankings" else "This Week's Rankings",
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                if (isHistoricalView) {
                                    if (groupState.historicalRankings.isEmpty()) NoEntriesCard()
                                    HistoricalRankingsList(historicalRankings = groupState.historicalRankings)
                                } else {
                                    if (groupState.weeklyRankings.isEmpty()) NoEntriesCard()
                                    LeaderboardList(rankings = groupState.weeklyRankings)
                                }
                            }
                            if (groupState.members.filter { user -> state.user.id == user.id }
                                    .isNotEmpty()
                            ) {
                                Button(
                                    onClick = { onNavigateToPayouts(groupState.group.id) },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
                                        disabledContentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Text(
                                        "Complete Week",
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onJoinGroup(groupState.group.code) },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    colors = ButtonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
                                        disabledContentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Text(
                                        "Join Group",
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        is AuthState.Error -> TODO()
                        AuthState.Initial -> TODO()
                        AuthState.Loading -> TODO()
                        AuthState.NotAuthenticated -> TODO()
                    }
                }

                is GroupState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = groupState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitYourTimeCard(onEnterScreenTimeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "You still need to submit your time for this week!",
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
            ) {
                Button(
                    onClick = onEnterScreenTimeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Enter Screen Time")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoEntriesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No submissions yet!", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun HistoricalRankingsList(historicalRankings: List<UserHistoricalRankings>) {
    // First, get all unique weeks and sort them in descending order
    val allWeeks = historicalRankings
        .flatMap { it.weekRankings }
        .map { it.week }
        .distinct()
        .sortedDescending()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(allWeeks) { week ->
            WeeklyLeaderboard(
                week = week,
                rankings = historicalRankings.mapNotNull { userRankings ->
                    userRankings.weekRankings
                        .find { it.week == week }
                        ?.let { weekRanking ->
                            Ranking(
                                rank = weekRanking.rank,
                                user = userRankings.user,
                                time = weekRanking.time
                            )
                        }
                }.sortedBy { it.time }
            )
        }
    }
}

@Composable
fun WeeklyLeaderboard(week: String, rankings: List<Ranking>) {
    val formattedDate = remember(week) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(week)

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            "Week of ${outputFormat.format(date)}"
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            rankings.forEachIndexed { index, ranking ->
                LeaderboardItem(ranking.copy(rank = index + 1))
                if (index < rankings.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LeaderboardList(rankings: List<Ranking>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rankings) { ranking ->
            LeaderboardItem(ranking = ranking)
        }
    }
}

@Composable
fun LeaderboardItem(ranking: Ranking) {
    // Define medal colors for top 3
    val goldColor = colorResource(R.color.gold)  // Classic gold
    val silverColor = colorResource(R.color.silver) // Classic silver
    val bronzeColor = colorResource(R.color.bronze) // Classic bronze

    // Create a list of container colors for remaining positions
    val containerColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.inversePrimary
    )

    // Get color based on ranking
    val backgroundColor = when (ranking.rank) {
        1 -> goldColor
        2 -> silverColor
        3 -> bronzeColor
        else -> containerColors[(ranking.rank - 4) % containerColors.size]
    }

    // Determine if this is a medal position
    val isMedalPosition = ranking.rank <= 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${ranking.rank}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isMedalPosition) {
                        Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = ranking.user.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isMedalPosition) {
                        Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text(
                text = formatDisplayScreenTime(ranking.time),
                style = MaterialTheme.typography.titleMedium,
                color = if (isMedalPosition) {
                    Color.Black
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}


@Composable
fun EnterScreenTimeDialog(
    onDismiss: () -> Unit,
    onEnter: (Int) -> Unit
) {
    var screenTime by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Screen Time") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = screenTime,
                    onValueChange = { input: String ->
                        if (input.all { it.isDigit() }) {
                            screenTime = input
                        }
                    },
                    label = { Text("Daily Average") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (screenTime.isNotBlank()) {
                                onEnter(screenTime.toInt())
                            }
                        }
                    )
                )
                Text(
                    text = "Enter your daily average screen time for this past week",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (screenTime.isNotBlank()) {
                        onEnter(screenTime.toInt())
                    }
                }
            ) {
                Text("Enter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}