package ru.vsu.forum.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.vsu.forum.features.messages.view.MessagesViewModel
import ru.vsu.forum.features.profile.ProfileViewModel
import ru.vsu.forum.features.topics.view.TopicsViewModel
import java.util.UUID

val appModule = module {
    viewModelOf(::TopicsViewModel)
    viewModel { (topicId:UUID, topicTitle:String) -> MessagesViewModel(get(), topicId, topicTitle) }
    viewModelOf(::ProfileViewModel)
}