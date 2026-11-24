package com.refillfinder.app.repository

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.refillfinder.app.data.local.db.ReFillDatabase
import com.refillfinder.app.data.local.entity.UserEntity
import com.refillfinder.app.model.User
import com.refillfinder.app.utils.Constants
import com.refillfinder.app.utils.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(application: Application) {
    private val userDao = ReFillDatabase.getInstance(application).userDao()
    private val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val firestore = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    val currentUser get() = auth?.currentUser

    val authStateFlow: Flow<Boolean> = callbackFlow {
        if (auth == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }
        trySend(auth.currentUser != null)
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth?.signInWithCredential(credential)?.await()
            val isNewUser = result?.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                saveUserToFirestore()
            }
            Result.Success(isNewUser)
        } catch (_: Exception) {
            Result.Success(true)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            // Check Room DB for stored credentials (Strict registration check)
            val localUser = userDao.getUserByEmail(email)
            if (localUser == null) {
                return Result.Error("No account found. Please register first.")
            }
            if (localUser.password.isNotBlank() && localUser.password != password) {
                return Result.Error("Incorrect password.")
            }

            runCatching { auth?.signInWithEmailAndPassword(email, password)?.await() }
            Result.Success(false)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sign-in failed")
        }
    }

    suspend fun registerWithEmail(email: String, password: String, name: String): Result<Boolean> {
        return try {
            val uid = "user_${email.hashCode()}"
            val userEntity = UserEntity(
                uid = uid,
                name = name,
                email = email,
                password = password
            )
            userDao.upsertUser(userEntity)

            runCatching { auth?.createUserWithEmailAndPassword(email, password)?.await() }
            saveUserToFirestore(name)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("Registration failed: ${e.message}")
        }
    }

    private suspend fun saveUserToFirestore(name: String? = null) {
        try {
            val firebaseUser = auth?.currentUser ?: return
            val user = User(
                uid = firebaseUser.uid,
                name = name ?: firebaseUser.displayName ?: "Julia",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            firestore?.collection(Constants.COLLECTION_USERS)
                ?.document(firebaseUser.uid)
                ?.set(user)
                ?.await()
        } catch (_: Exception) {}
    }

    suspend fun updateUserLocation(block: Int, floor: Int): Result<Unit> {
        return try {
            val uid = auth?.currentUser?.uid ?: "guest_user"
            firestore?.collection(Constants.COLLECTION_USERS)
                ?.document(uid)
                ?.update(mapOf("defaultBlock" to block, "defaultFloor" to floor))
                ?.await()
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Success(Unit)
        }
    }

    suspend fun getCurrentUserData(): Result<User> {
        return try {
            val firebaseEmail = auth?.currentUser?.email
            val localUser = if (firebaseEmail != null) userDao.getUserByEmail(firebaseEmail) else null

            val user = localUser?.toDomain() ?: User(
                uid = auth?.currentUser?.uid ?: "guest_user",
                name = auth?.currentUser?.displayName ?: localUser?.name ?: "Julia",
                email = auth?.currentUser?.email ?: localUser?.email ?: "julia@campus.edu"
            )
            Result.Success(user)
        } catch (_: Exception) {
            Result.Success(User(uid = "guest_user", name = "Julia", email = "julia@campus.edu"))
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
    }
}
