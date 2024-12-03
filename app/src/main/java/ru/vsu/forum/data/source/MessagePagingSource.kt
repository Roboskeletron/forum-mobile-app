package ru.vsu.forum.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.vsu.forum.features.common.data.ForumService
import ru.vsu.forum.features.common.models.PagedList
import ru.vsu.forum.model.Message
import java.util.UUID

class MessagePagingSource(
    private val forumService: ForumService,
    private val topicId: UUID,
    private val searchQuery: String
) : PagingSource<Int, Message>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Message> {
        val page = params.key ?: 1
        return try {
            val response = forumService.getMessages(topicId, page, params.loadSize, searchQuery)
            if (response.isSuccessful) {
                val messages = response.body() ?: PagedList(listOf(), 0)
                LoadResult.Page(
                    data = messages.items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (messages.count < params.loadSize) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to load Messages"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Message>): Int? {
        return state.anchorPosition
    }
}

