package ru.vsu.forum.features.topics.data

import android.util.Log
import ru.vsu.forum.features.common.data.ForumService
import ru.vsu.forum.features.topics.models.CreateTopicModel
import ru.vsu.forum.features.topics.models.Topic
import ru.vsu.forum.features.topics.models.TopicSearchParameters
import ru.vsu.forum.features.topics.models.UpdateTopicModel
import java.util.UUID

interface TopicRepository {
    suspend fun getTopics(
        pageIndex: Int,
        pageSize: Int,
        searchParameters: TopicSearchParameters
    ): List<Topic>

    suspend fun createTopic(title: String, description: String? = null): UUID?
    suspend fun isTitleUnique(title: String): Boolean
    suspend fun getById(id: UUID): Topic?
    suspend fun updateTopic(id: UUID, title: String, description: String? = null)
    suspend fun likeTopic(id: UUID): Topic
    suspend fun dislikeTopic(id: UUID): Topic
    suspend fun deleteTopic(id: UUID)
}

class TopicRepositoryImpl(private val forumService: ForumService) : TopicRepository {
    override suspend fun getTopics(
        pageIndex: Int,
        pageSize: Int,
        searchParameters: TopicSearchParameters
    ): List<Topic> {
        try {
            val response = forumService.getTopics(
                pageIndex,
                pageSize,
                searchParameters.author,
                searchParameters.title,
                searchParameters.content
            )
            return response.body()?.items ?: listOf()
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to get topics", e)
            return listOf()
        }
    }

    override suspend fun createTopic(title: String, description: String?): UUID? {
        try {
            val response = forumService.createTopic(CreateTopicModel(title, description))
            return response.body()
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to create topic", e)
            return null
        }
    }

    override suspend fun isTitleUnique(title: String): Boolean {
        try {
            val response = forumService.topicExistsByTitle(title)
            return !response.body()!!
        } catch (e: Exception) {
            Log.e(
                TopicRepositoryImpl::class.qualifiedName,
                "Unable to check topic existence by title",
                e
            )
            return false
        }
    }

    override suspend fun getById(id: UUID): Topic? {
        try {
            val response = forumService.getTopicById(id)
            return response.body()
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to get topic by id", e)
            return null
        }
    }

    override suspend fun updateTopic(id: UUID, title: String, description: String?) {
        try {
            forumService.updateTopic(id, UpdateTopicModel(id, title, description))
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to update topic", e)
        }
    }

    override suspend fun likeTopic(id: UUID): Topic {
        try {
            return forumService.likeTopic(id).body()!!
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to like topic", e)
            throw e
        }
    }

    override suspend fun dislikeTopic(id: UUID): Topic {
        try {
            return forumService.dislikeTopic(id).body()!!
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to dislike topic", e)
            throw e
        }
    }

    override suspend fun deleteTopic(id: UUID) {
        try {
            forumService.deleteTopic(id)
        } catch (e: Exception) {
            Log.e(TopicRepositoryImpl::class.qualifiedName, "Unable to delete topic", e)
        }
    }
}