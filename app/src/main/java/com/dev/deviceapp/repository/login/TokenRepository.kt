package com.dev.deviceapp.repository.login

import android.util.Base64
import android.util.Log
import com.dev.deviceapp.model.token.TokenInfo
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.json.JSONObject


@Singleton
class TokenRepository @Inject constructor(){
    private var token: String? = null

    fun saveToken(token: String){
        this.token = token
        Log.i("TokenRepository", "Token saved: $token")
    }

    fun clearToken(){
        token = null
    }

    fun getTokenInfoRepository(): TokenInfo? {
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

    fun getToken(): String? = token
}