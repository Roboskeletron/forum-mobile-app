package ru.vsu.forum.data.source

import ru.vsu.forum.features.common.data.ForumService
import ru.vsu.forum.model.User
import ru.vsu.forum.utils.Config

class UserRepository(private val forumService: ForumService) {
    suspend fun getUserProfile() : Result<User> {
        return try {
            val response = forumService.getProfile("Bearer ${Config.AUTH_TOKEN}")

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}