package ru.vsu.forum.features.profile.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.vsu.forum.R
import ru.vsu.forum.databinding.FragmentEditProfileBinding
import ru.vsu.forum.features.auth.domain.UserProvider
import ru.vsu.forum.features.common.domain.ImageService
import ru.vsu.forum.features.profile.data.UserRepository
import kotlin.text.isNullOrEmpty
import kotlin.toString

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModel()
    private val userRepository: UserRepository by inject()
    private val userProvider: UserProvider by inject()
    private val imageService = ImageService(userRepository)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val toolbar = binding.profileToolBar
        toolbar.setupWithNavController(findNavController())

        viewModel.name.observe(viewLifecycleOwner) {
            onNameChanged(it)
        }

        binding.profileUpdateButton.setOnClickListener {
            lifecycleScope.launch {
                userRepository.updateProfile(viewModel.name.value.toString(), viewModel.description.value, viewModel.avatar.value)
                val profile = userRepository.getUserProfile()
                viewModel.setProfile(profile)
            }
        }

        binding.profileLogoutButton.setOnClickListener {
            lifecycleScope.launch {
                userProvider.user.removeObservers(viewLifecycleOwner)
                userProvider.logout()
                findNavController().navigateUp()
            }
        }

        imageService.avatar.observe(viewLifecycleOwner) {
            viewModel.avatar.value = it
        }

        viewModel.avatar.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.profilePictureView.setImageResource(R.drawable.ic_avatar_placeholder)
            }
            else {
                binding.profilePictureView.setImageBitmap(it)
            }
        }

        val pickAvatar = registerForActivityResult(PickVisualMedia()) { uri ->
            uri?.also {
                context?.contentResolver?.openInputStream(uri).use {
                    viewModel.avatar.value = BitmapFactory.decodeStream(it)
                }
            }
        }

        binding.profilePictureView.setOnClickListener {
            pickAvatar.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        userProvider.user.observe(viewLifecycleOwner) {
            viewModel.setProfile(it)

            it?.also {
                lifecycleScope.launch {
                    imageService.fetchAvatar(it.id)
                }
            }

            if (it == null) {
                lifecycleScope.launch {
                    if (!userProvider.tryLogin()) {
                        findNavController().navigate(EditProfileFragmentDirections.actionNavigationProfileToLoginFragment())
                    }
                }
            }
        }

        return binding.root
    }

    private  fun onNameChanged(name: String?){
        binding.profileUsernameTextInputLayout.error = if (name.isNullOrEmpty()) "Name is required" else null
    }
}