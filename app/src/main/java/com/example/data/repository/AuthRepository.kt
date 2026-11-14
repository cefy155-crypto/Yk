package com.example.data.repository

import android.content.Context
import com.example.data.db.CoachDao
import com.example.data.db.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }
}

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(
    context: Context,
    private val dao: CoachDao
) {
    private val prefs = context.applicationContext.getSharedPreferences("scp_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow<Long?>(readStoredUserId())
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(null) else dao.observeUserById(uid)
    }

    private fun readStoredUserId(): Long? {
        val id = prefs.getLong("logged_in_user_id", -1L)
        return if (id > 0L) id else null
    }

    private fun saveSessionUserId(id: Long?) {
        if (id == null) {
            prefs.edit().remove("logged_in_user_id").apply()
        } else {
            prefs.edit().putLong("logged_in_user_id", id).apply()
        }
        _currentUserId.value = id
    }

    suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return AuthResult.Error("Veuillez saisir votre e-mail et votre mot de passe.")
        }
        val user = dao.getUserByEmail(cleanEmail)
            ?: return AuthResult.Error("Aucun compte trouvé pour cet e-mail.")
        if (!PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            return AuthResult.Error("Mot de passe incorrect.")
        }
        saveSessionUserId(user.id)
        return AuthResult.Success(user)
    }

    suspend fun loginDemoCoach(): AuthResult {
        val demo = dao.getUserByEmail("coach@sportcoachpro.fr")
            ?: return AuthResult.Error("Compte démo en cours d'initialisation, réessayez.")
        saveSessionUserId(demo.id)
        return AuthResult.Success(demo)
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: String,
        age: Int,
        sex: String,
        heightCm: Int,
        weightKg: Double,
        fitnessLevel: String,
        fitnessGoal: String,
        weeklySessionsTarget: Int
    ): AuthResult {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        if (cleanName.length < 2) return AuthResult.Error("Le nom complet doit contenir au moins 2 caractères.")
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) return AuthResult.Error("Adresse e-mail invalide.")
        if (password.length < 6) return AuthResult.Error("Le mot de passe doit contenir au moins 6 caractères.")
        if (dao.getUserByEmail(cleanEmail) != null) return AuthResult.Error("Un compte existe déjà avec cet e-mail.")

        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(password, salt)
        val newUser = UserEntity(
            fullName = cleanName,
            role = role,
            specialty = if (role == "Coach") "Coaching Sportif & Préparation Physique" else "Athlète Suivi",
            certifications = if (role == "Coach") "Coach Certifié Sport Coach Pro" else "Membre Athlète",
            email = cleanEmail,
            passwordHash = hash,
            passwordSalt = salt,
            age = age.coerceIn(13, 95),
            sex = sex,
            heightCm = heightCm.coerceIn(120, 230),
            weightKg = weightKg.coerceIn(35.0, 220.0),
            fitnessLevel = fitnessLevel,
            fitnessGoal = fitnessGoal,
            weeklySessionsTarget = weeklySessionsTarget.coerceIn(1, 7),
            bio = "Objectif : $fitnessGoal ($fitnessLevel)"
        )
        val newId = dao.insertUser(newUser)
        val saved = newUser.copy(id = newId)
        saveSessionUserId(newId)
        return AuthResult.Success(saved)
    }

    suspend fun updateProfile(user: UserEntity) {
        dao.updateUser(user)
    }

    fun logout() {
        saveSessionUserId(null)
    }
}
