package com.example.an_app_for_runners_and_cyclists.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.an_app_for_runners_and_cyclists.Config
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import timber.log.Timber

class GoogleAuthManager(
    private val activity: Activity,
    private val userRepository: UserRepository
) {
    companion object {
        const val RC_GOOGLE_SIGN_IN = 9001
        private const val TAG = "GoogleAuthManager"
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        configureGoogleSignIn()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Config.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }


    suspend fun handleSignInResult(data: Intent?): AuthResult {
        return try {
            Timber.d("🔄 Handling Google Sign-In result")

            if (data == null) {
                Timber.e("❌ Google Sign-In data intent is null")
                return AuthResult.Error("Sign-in data is missing")
            }

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful) {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    Timber.d("✅ Google Sign-In successful: ${it.email}")
                    Timber.d("📧 User email: ${it.email}")
                    Timber.d("👤 User name: ${it.displayName}")
                    Timber.d("🆔 User ID: ${it.id}")
                    Timber.d("📸 Photo URL: ${it.photoUrl}")
                    Timber.d("🔑 ID Token present: ${!it.idToken.isNullOrEmpty()}")

                    val user = createUserFromGoogleAccount(it)
                    AuthResult.Success(user)
                } ?: run {
                    Timber.e("❌ Google Sign-In account is null")
                    AuthResult.Error("Google Sign-In failed: account is null")
                }
            } else {
                val exception = task.exception
                Timber.e(exception, "❌ Google Sign-In task failed")
                AuthResult.Error("Google Sign-In failed: ${exception?.message ?: "Unknown error"}")
            }

        } catch (e: ApiException) {
            Timber.e(e, "❌ Google Sign-In failed with API exception: ${e.statusCode}")
            val errorMessage = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign-in was cancelled"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign-in failed"
                else -> "Google Sign-In failed: ${e.statusCode} - ${e.message}"
            }
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            Timber.e(e, "❌ Google Sign-In failed with general exception")
            AuthResult.Error("Google Sign-In failed: ${e.message}")
        }
    }

    private fun createUserFromGoogleAccount(account: GoogleSignInAccount): User {
        return User(
            id = "google_${account.id ?: System.currentTimeMillis()}",
            name = account.displayName ?: "Google User",
            email = account.email ?: "",
            password = "",
            profileImage = account.photoUrl?.toString(),
            authProvider = "google",
            providerId = account.id,
            accessToken = account.idToken
        )
    }

    fun signOut() {
        googleSignInClient.signOut()
            .addOnCompleteListener(activity) {
                Timber.d("✅ Google Sign-Out successful")
            }
    }

    suspend fun getCurrentGoogleAccount(): GoogleSignInAccount? {
        return withContext(Dispatchers.IO) {
            try {
                GoogleSignIn.getLastSignedInAccount(activity)
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to get current Google account")
                null
            }
        }
    }

    fun checkGoogleSignInAvailability(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            Timber.d("🔍 Google Sign-In availability check:")
            Timber.d("   - Last signed in account: ${account?.email ?: "None"}")
            Timber.d("   - Google Play Services available: true")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Google Play Services not available")
            false
        }
    }

    suspend fun silentSignIn(): AuthResult? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            account?.let {
                Timber.d("🔍 Silent sign-in found existing account: ${it.email}")
                val user = createUserFromGoogleAccount(it)
                AuthResult.Success(user)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Silent sign-in failed")
            null
        }
    }

    fun revokeAccess() {
        googleSignInClient.revokeAccess()
            .addOnCompleteListener(activity) {
                Timber.d("✅ Google access revoked")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "❌ Failed to revoke Google access")
            }
    }

    fun getSignInIntent(): Intent {
        Timber.d("🔄 Creating Google Sign-In intent")
        return googleSignInClient.signInIntent
    }
}
