package ru.vsu.forum.features.messages.data

import android.util.Log
import ru.vsu.forum.features.common.data.ForumService
import ru.vsu.forum.features.messages.models.Message
import ru.vsu.forum.features.messages.models.MessageSearchParameters
import ru.vsu.forum.features.messages.models.SendMessageModel
import ru.vsu.forum.features.messages.models.UpdateMessageModel
import java.util.UUID

interface MessageRepository{
    suspend fun sendMessage(topicId: UUID, text: String) : UUID
    suspend fun getMessages(topicId: UUID, pageIndex: Int, pageSize: Int, searchParameters: MessageSearchParameters) : List<Message>
    suspend fun deleteMessage(id: UUID)
    suspend fun updateMessage(id: UUID, text: String)
    suspend fun likeMessage(id: UUID): Message
    suspend fun dislikeMessage(id: UUID): Message
}

class MessageRepositoryImpl(private val forumService: ForumService) : MessageRepository {

    override suspend fun sendMessage(topicId: UUID, text: String): UUID {
        try {
            val response = forumService.sendMessage(
                topicId = topicId,
                sendMessageRequest = SendMessageModel(topicId = topicId, text = text)
            )

            return response.body()!!
        } catch (e: Exception) {
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to send message", e)
            throw e
        }
    }

    override suspend fun getMessages(
        topicId: UUID,
        pageIndex: Int,
        pageSize: Int,
        searchParameters: MessageSearchParameters
    ): List<Message> {
        try {
            val messages = forumService.getMessages(
                topicId,
                pageIndex,
                pageSize,
                searchParameters.author,
                searchParameters.content
            )
            return messages.body()?.items ?: listOf()
        }
        catch (e: Exception){
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to get messages", e)
            return listOf()
        }
    }

    override suspend fun deleteMessage(id: UUID) {
        try {
            forumService.deleteMessage(id)
        }
        catch (e: Exception){
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to delete message", e)
        }
    }

    override suspend fun updateMessage(id: UUID, text: String) {
        try {
            forumService.updateMessage(id, UpdateMessageModel(id, text))
        }
        catch (e: Exception){
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to update message", e)
        }
    }

    override suspend fun likeMessage(id: UUID): Message {
        try {
            return forumService.likeMessage(id).body()!!
        }
        catch (e: Exception) {
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to like message", e)
            throw e
        }
    }

    override suspend fun dislikeMessage(id: UUID): Message {
        try {
            return forumService.dislikeMessage(id).body()!!
        }
        catch (e: Exception) {
            Log.e(MessageRepositoryImpl::class.qualifiedName, "Unable to dislike message", e)
            throw e
        }
    }
}
