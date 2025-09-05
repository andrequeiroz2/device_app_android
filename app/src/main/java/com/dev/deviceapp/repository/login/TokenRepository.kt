package com.dev.deviceapp.repository.login

import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dev.deviceapp.model.token.TokenInfo
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

@Singleton
class TokenRepository @Inject constructor(

    private val dataStore: DataStore<Preferences>
){
    companion object{
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun saveToken(token: String){
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
        Log.i("TokenRepository", "Token saved: $token")
    }

    suspend fun clearToken(){
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun getTokenInfoRepository(): TokenInfo? {
        val token = getToken()?: return null

        return try{

            val parts = token.split(".")
            if(parts.size < 2) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            val json = JSONObject(decodedString)
            val inf = json.getJSONObject("inf")

            TokenInfo(
                username = inf.optString("username"),
                uuid = inf.optString("uuid"),
                email = inf.optString("email"),
            )

        }catch (e: Exception){
            Log.e("TokenRepository", "Error getting token info: ${e.message}")
            null
        }
    }

    suspend fun getToken(): String? {
        return token.first()
    }
}