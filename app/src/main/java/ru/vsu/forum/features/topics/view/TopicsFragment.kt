package ru.vsu.forum.features.topics.view

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.vsu.forum.R
import ru.vsu.forum.databinding.FragmentTopicsBinding
import ru.vsu.forum.features.auth.domain.UserProvider
import ru.vsu.forum.features.topics.data.TopicRepository
import ru.vsu.forum.features.topics.models.Topic
import kotlin.apply

class TopicsFragment : Fragment() {
    private lateinit var binding: FragmentTopicsBinding

    private val viewModel: TopicsViewModel by viewModel()
    private val userProvider: UserProvider by inject()
    private val topicRepository: TopicRepository by inject()

    private lateinit var topicAdapter: TopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopicsBinding.inflate(inflater, container, false)

        val toolbar = binding.topicsToolbar

        toolbar.setupWithNavController(findNavController())
        toolbar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.action_profile -> {
                    findNavController().navigate(TopicsFragmentDirections.actionNavigationHomeToNavigationProfile())
                    true
                }
                else -> false
            }
        }
        setupSearchView(toolbar.menu)

        binding.topicsAddTopicFab.setOnClickListener {
            findNavController().navigate(TopicsFragmentDirections.actionNavigationHomeToAddTopicFragment())
        }

        userProvider.user.observe(viewLifecycleOwner) {
            binding.topicsAddTopicFab.isEnabled = it != null
        }

        topicAdapter = TopicAdapter(
            this,
            userProvider,
            topicRepository
        )

        binding.topicsRecyclerView.apply {
            adapter = topicAdapter
        }

        lifecycleScope.launch{
            viewModel.topicsFlow.collectLatest {
                    pagedData -> topicAdapter.submitData(pagedData)
            }
        }

        return binding.root
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Search topics..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = MenuInflater(v.context)
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val topic = topicAdapter.getSelectedTopic() ?: return super.onContextItemSelected(item)
        return when (item.itemId) {
            R.id.action_edit -> {
                editTopic(topic)
                true
            }

            R.id.action_delete -> {
                deleteTopic(topic)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    fun editTopic(topic: Topic) {
        val action = TopicsFragmentDirections.actionNavigationHomeToEditTopicFragment(topic.id.toString())
        findNavController().navigate(action)
    }

    private fun deleteTopic(topic: Topic) {
        lifecycleScope.launch {
            topicRepository.deleteTopic(topic.id)
            viewModel.topicsFlow.collectLatest {
                    pagedData -> topicAdapter.submitData(pagedData)
            }
        }
    }
}