package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.db.UserEntity
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime

@Composable
fun AuthScreen(
    errorMessage: String?,
    onClearError: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onDemoLogin: () -> Unit,
    onSignUp: (
        fullName: String, email: String, password: String, role: String,
        age: Int, sex: String, heightCm: Int, weightKg: Double,
        fitnessLevel: String, fitnessGoal: String, weeklyTarget: Int
    ) -> Unit
) {
    var modeTab by remember { mutableIntStateOf(0) } // 0 = Connexion, 1 = Inscription
    var email by remember { mutableStateOf("coach@sportcoachpro.fr") }
    var password by remember { mutableStateOf("Coach2026!") }
    var showPassword by remember { mutableStateOf(false) }

    // Sign-up fields
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Coach") }
    var ageText by remember { mutableStateOf("28") }
    var sex by remember { mutableStateOf("Homme") }
    var heightText by remember { mutableStateOf("178") }
    var weightText by remember { mutableStateOf("76.0") }
    var level by remember { mutableStateOf("Intermédiaire") }
    var goal by remember { mutableStateOf("Prise de masse") }
    var weeklyText by remember { mutableStateOf("4") }

    Scaffold(containerColor = ObsidianBg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, CharcoalBorder, MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_hero_coach),
                        contentDescription = "Sport Coach Pro Studio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.65f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ObsidianBg.copy(alpha = 0.25f), ObsidianBg.copy(alpha = 0.92f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        Text(
                            text = "SPORT COACH PRO",
                            style = MaterialTheme.typography.displayMedium,
                            color = ElectricLime
                        )
                        Text(
                            text = "Plateforme Musculation, Nutrition & Suivi Performances",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = modeTab,
                    containerColor = CharcoalSurface,
                    contentColor = ElectricLime,
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                ) {
                    Tab(
                        selected = modeTab == 0,
                        onClick = { modeTab = 0; onClearError() },
                        text = { Text("Connexion") }
                    )
                    Tab(
                        selected = modeTab == 1,
                        onClick = { modeTab = 1; onClearError() },
                        text = { Text("Créer un compte") }
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = CoralAlert.copy(alpha = 0.16f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralAlert)
                    ) {
                        Text(
                            text = errorMessage,
                            color = CoralAlert,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (modeTab == 1) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Nom complet") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                            )
                            Text("Profil du compte", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Coach", "Pratiquant").forEach { r ->
                                    FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r) })
                                }
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Adresse e-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher mot de passe"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        if (modeTab == 1) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = ageText, onValueChange = { ageText = it }, label = { Text("Âge") }, modifier = Modifier.weight(1f), singleLine = true)
                                OutlinedTextField(value = heightText, onValueChange = { heightText = it }, label = { Text("Taille cm") }, modifier = Modifier.weight(1f), singleLine = true)
                                OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Poids kg") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Homme", "Femme").forEach { s ->
                                    FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s) })
                                }
                                OutlinedTextField(
                                    value = weeklyText,
                                    onValueChange = { weeklyText = it },
                                    label = { Text("Séances/sem") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Text("Niveau fitness", style = MaterialTheme.typography.labelLarge)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Débutant", "Intermédiaire", "Avancé").forEach { l ->
                                    FilterChip(selected = level == l, onClick = { level = l }, label = { Text(l) })
                                }
                            }
                            Text("Objectif principal", style = MaterialTheme.typography.labelLarge)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Prise de masse", "Perte de gras", "Force", "Recomposition", "Endurance / HIIT").forEach { g ->
                                    FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text(g) })
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (modeTab == 0) {
                                    onLogin(email, password)
                                } else {
                                    onSignUp(
                                        fullName,
                                        email,
                                        password,
                                        role,
                                        ageText.toIntOrNull() ?: 28,
                                        sex,
                                        heightText.toIntOrNull() ?: 178,
                                        weightText.replace(",", ".").toDoubleOrNull() ?: 76.0,
                                        level,
                                        goal,
                                        weeklyText.toIntOrNull() ?: 4
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_submit_button")
                        ) {
                            Text(
                                if (modeTab == 0) "Se connecter" else "Créer mon compte & Accéder",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (modeTab == 0) {
                            OutlinedButton(
                                onClick = onDemoLogin,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("demo_login_button")
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = CyberCyan)
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Accès rapide Coach Démo (Marc Delacroix)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileManagementScreen(
    user: UserEntity,
    onSaveProfile: (UserEntity) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    var fullName by remember(user) { mutableStateOf(user.fullName) }
    var specialty by remember(user) { mutableStateOf(user.specialty) }
    var certifications by remember(user) { mutableStateOf(user.certifications) }
    var ageText by remember(user) { mutableStateOf(user.age.toString()) }
    var sex by remember(user) { mutableStateOf(user.sex) }
    var heightText by remember(user) { mutableStateOf(user.heightCm.toString()) }
    var weightText by remember(user) { mutableStateOf(user.weightKg.toString()) }
    var level by remember(user) { mutableStateOf(user.fitnessLevel) }
    var goal by remember(user) { mutableStateOf(user.fitnessGoal) }
    var weeklyText by remember(user) { mutableStateOf(user.weeklySessionsTarget.toString()) }
    var bio by remember(user) { mutableStateOf(user.bio) }

    Scaffold(containerColor = ObsidianBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mon Profil & Compte", style = MaterialTheme.typography.headlineLarge, color = ElectricLime)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer le profil")
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ElectricLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = OnElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                        Text("${user.role} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Informations personnelles", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Spécialité") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = certifications, onValueChange = { certifications = it }, label = { Text("Certifications / Diplômes") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = ageText, onValueChange = { ageText = it }, label = { Text("Âge") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = heightText, onValueChange = { heightText = it }, label = { Text("Taille (cm)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Poids (kg)") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("Homme", "Femme").forEach { s ->
                            FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s) })
                        }
                        OutlinedTextField(value = weeklyText, onValueChange = { weeklyText = it }, label = { Text("Séances/sem") }, modifier = Modifier.weight(1f))
                    }

                    Text("Niveau Fitness", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Débutant", "Intermédiaire", "Avancé").forEach { l ->
                            FilterChip(selected = level == l, onClick = { level = l }, label = { Text(l) })
                        }
                    }

                    Text("Objectif Personnel", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Performance & Force", "Prise de masse", "Perte de gras", "Recomposition", "Endurance / HIIT").forEach { g ->
                            FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text(g) })
                        }
                    }

                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Biographie / Notes") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            onSaveProfile(
                                user.copy(
                                    fullName = fullName.trim().ifBlank { user.fullName },
                                    specialty = specialty.trim(),
                                    certifications = certifications.trim(),
                                    age = ageText.toIntOrNull() ?: user.age,
                                    sex = sex,
                                    heightCm = heightText.toIntOrNull() ?: user.heightCm,
                                    weightKg = weightText.replace(",", ".").toDoubleOrNull() ?: user.weightKg,
                                    fitnessLevel = level,
                                    fitnessGoal = goal,
                                    weeklySessionsTarget = weeklyText.toIntOrNull() ?: user.weeklySessionsTarget,
                                    bio = bio.trim()
                                )
                            )
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_button")
                    ) {
                        Text("Enregistrer le profil", fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAlert),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralAlert),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Se déconnecter")
            }
        }
    }
}
