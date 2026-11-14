package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.CommunityChallengeEntity
import com.example.data.db.CommunityCommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime

@Composable
fun CommunityScreen(
    posts: List<CommunityPostEntity>,
    comments: List<CommunityCommentEntity>,
    challenges: List<CommunityChallengeEntity>,
    onPublishPost: (category: String, title: String, content: String, badge: String) -> Unit,
    onToggleLike: (CommunityPostEntity) -> Unit,
    onAddComment: (postId: Long, text: String) -> Unit,
    onCreateChallenge: (title: String, groupName: String, desc: String, target: String) -> Unit,
    onToggleJoinChallenge: (CommunityChallengeEntity) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var tabIndex by remember { mutableIntStateOf(0) } // 0 = Fil Communauté, 1 = Groupes & Défis
    var categoryFilter by remember { mutableStateOf("Tout") }
    var showNewPostDialog by remember { mutableStateOf(false) }
    var showNewChallengeDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, categoryFilter) {
        if (categoryFilter == "Tout") posts else posts.filter { it.category == categoryFilter }
    }

    Scaffold(containerColor = ObsidianBg) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Communauté & Défis", style = MaterialTheme.typography.headlineMedium, color = ElectricLime)
                    Text("Partagez routines, progrès et idées repas sains", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_community_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Retour")
                }
            }

            TabRow(selectedTabIndex = tabIndex, containerColor = CharcoalSurface, contentColor = ElectricLime) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Fil Communauté") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Groupes & Défis (${challenges.size})") })
            }

            if (tabIndex == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Tout", "Routine", "Progrès", "Repas Sain").forEach { cat ->
                            FilterChip(selected = categoryFilter == cat, onClick = { categoryFilter = cat }, label = { Text(cat) })
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showNewPostDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                        modifier = Modifier.testTag("new_community_post_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publier")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        val postComments = comments.filter { it.postId == post.id }
                        CommunityPostCard(
                            post = post,
                            comments = postComments,
                            onToggleLike = { onToggleLike(post) },
                            onAddComment = { text -> onAddComment(post.id, text) }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Groupes & Challenges Actifs", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { showNewChallengeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Créer Défi")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(challenges, key = { it.id }) { ch ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = CyberCyan.copy(alpha = 0.16f), shape = MaterialTheme.shapes.small) {
                                        Text(ch.groupName, style = MaterialTheme.typography.labelSmall, color = CyberCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberKinetic)
                                        Text("${ch.membersCount} membres", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Text(ch.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(ch.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LinearProgressIndicator(
                                    progress = { ch.currentProgressPct / 100f },
                                    color = ElectricLime,
                                    trackColor = CharcoalElevated,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Objectif : ${ch.targetLabel} (${ch.currentProgressPct}%)", style = MaterialTheme.typography.labelMedium, color = ElectricLime)
                                    if (ch.isJoined) {
                                        OutlinedButton(onClick = { onToggleJoinChallenge(ch) }) { Text("Inscrit ✓") }
                                    } else {
                                        Button(onClick = { onToggleJoinChallenge(ch) }) { Text("Rejoindre") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPostDialog) {
        var cat by remember { mutableStateOf("Routine") }
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var badge by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("Partager avec la communauté") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Routine", "Progrès", "Repas Sain").forEach { c ->
                            FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, singleLine = true)
                    OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Badge clé (ex: 520 kcal • 40g P)") }, singleLine = true)
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Détails (exercices, ressenti, recette)") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onPublishPost(cat, title, content, badge)
                            showNewPostDialog = false
                        }
                    }
                ) { Text("Publier") }
            },
            dismissButton = { TextButton(onClick = { showNewPostDialog = false }) { Text("Annuler") } }
        )
    }

    if (showNewChallengeDialog) {
        var title by remember { mutableStateOf("") }
        var groupName by remember { mutableStateOf("Club Performance") }
        var desc by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("12 séances / mois") }

        AlertDialog(
            onDismissRequest = { showNewChallengeDialog = false },
            title = { Text("Créer un Groupe / Défi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nom du défi") }, singleLine = true)
                    OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Nom du groupe") }, singleLine = true)
                    OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Objectif cible") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Règles du défi") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateChallenge(title, groupName, desc.ifBlank { "Challenge collectif Sport Coach Pro" }, target)
                            showNewChallengeDialog = false
                        }
                    }
                ) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showNewChallengeDialog = false }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostEntity,
    comments: List<CommunityCommentEntity>,
    onToggleLike: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentDraft by remember { mutableStateOf("") }
    val catColor = when (post.category) {
        "Progrès" -> ElectricLime
        "Repas Sain" -> EmeraldSuccess
        else -> CyberCyan
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(catColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.authorName.take(2).uppercase(), style = MaterialTheme.typography.labelLarge, color = catColor)
                    }
                    Column {
                        Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                        Text(post.authorRole, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(color = catColor.copy(alpha = 0.16f), shape = MaterialTheme.shapes.small) {
                    Text(post.category, style = MaterialTheme.typography.labelSmall, color = catColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(post.content, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = CharcoalElevated, shape = MaterialTheme.shapes.small) {
                    Text(post.highlightBadge, style = MaterialTheme.typography.labelMedium, color = AmberKinetic, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleLike) {
                        Icon(
                            if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "J'aime",
                            tint = if (post.isLikedByMe) CoralAlert else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${post.likesCount}", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (comments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(CharcoalElevated)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    comments.forEach { c ->
                        Text("${c.authorName} : ${c.text}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    placeholder = { Text("Commenter...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        onAddComment(commentDraft)
                        commentDraft = ""
                    },
                    enabled = commentDraft.isNotBlank()
                ) { Text("OK") }
            }
        }
    }
}
