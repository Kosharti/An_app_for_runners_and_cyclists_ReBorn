package com.example.an_app_for_runners_and_cyclists.ui.profile

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.auth.GoogleAuthManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentProfileBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    private var currentPhotoPath: String? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val uri = Uri.fromFile(File(path))
                handleImageSelection(uri)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            openCamera()
        } else {
            Snackbar.make(binding.root, "Camera permission is required", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupHeader()
        observeViewModel()
    }

    private fun setupHeader() {
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }
        binding.toolbarTitle.text = "My Profile"
    }

    private fun showDropdownMenu() {
        val popup = PopupMenu(requireContext(), binding.menuIcon)
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)

        try {
            popup.setForceShowIcon(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tracking -> {
                    findNavController().navigate(R.id.runTrackingFragment)
                    true
                }
                R.id.action_history -> {
                    findNavController().navigate(R.id.runHistoryFragment)
                    true
                }
                R.id.action_profile -> true
                R.id.action_community -> {
                    findNavController().navigate(R.id.otherRunnersFragment)
                    true
                }
                else -> false
            }
        }

        popup.show()

        try {
            val popupMenu = popup::class.java.getDeclaredField("mPopup")
            popupMenu.isAccessible = true
            val menu = popupMenu.get(popup)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            viewModel.startEditing()
        }

        binding.btnSave.setOnClickListener {
            saveUserData()
        }

        binding.btnBack.setOnClickListener {
            if (viewModel.isEditing.value) {
                viewModel.cancelEditing()
            } else {
                requireActivity().onBackPressed()
            }
        }

        binding.btnCamera.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_logout_dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun performLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userRepository = (requireActivity().application as RunnersExchangeApplication).userRepository

                val currentUser = userRepository.getCurrentUser()

                if (currentUser?.authProvider == "google") {
                    val googleAuthManager = GoogleAuthManager(requireActivity(), userRepository)
                    googleAuthManager.signOut()
                }

                userRepository.logout()

                navigateToSignIn()

            } catch (e: Exception) {
                navigateToSignIn()
            }
        }
    }

    private fun navigateToSignIn() {
        try {
            val intent = Intent(requireContext(), com.example.an_app_for_runners_and_cyclists.SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: Exception) {
                    Snackbar.make(binding.root, "Error creating file", Snackbar.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir("Pictures")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun handleImageSelection(uri: Uri) {
        val imagePath = if (uri.scheme == "file") {
            uri.path ?: uri.toString()
        } else {
            uri.toString()
        }

        viewModel.updateProfileImage(imagePath)

        try {
            if (uri.scheme == "content") {
                binding.profileImg.setImageURI(uri)
            } else {
                val file = File(uri.path ?: return)
                if (file.exists()) {
                    val fileUri = Uri.fromFile(file)
                    binding.profileImg.setImageURI(fileUri)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set temporary profile image")
        }

        Snackbar.make(binding.root, "Profile photo updated", Snackbar.LENGTH_SHORT).show()
    }

    private fun saveUserData() {
        val name = binding.etUserName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val height = binding.etHeight.text.toString().toIntOrNull()
        val weight = binding.etWeight.text.toString().toIntOrNull()
        val runningReason = binding.etRunningReason.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Snackbar.make(binding.root, "Please fill name and email", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.saveUserData(name, email, height, weight, runningReason, address)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectLatest { user ->
                user?.let { updateUI(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditing.collectLatest { editing ->
                updateEditModeUI(editing)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveState.collectLatest { state ->
                when (state) {
                    is ProfileViewModel.SaveState.Loading -> {
                        showSaveProgress(true)
                    }
                    is ProfileViewModel.SaveState.Success -> {
                        showSaveProgress(false)
                        Snackbar.make(binding.root, "Profile saved successfully", Snackbar.LENGTH_SHORT).show()
                    }
                    is ProfileViewModel.SaveState.Error -> {
                        showSaveProgress(false)
                        Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                    }
                    ProfileViewModel.SaveState.Idle -> {
                        showSaveProgress(false)
                    }
                }
            }
        }
    }

    private fun updateEditModeUI(editing: Boolean) {
        if (editing) {
            binding.btnSave.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
            enableEditing(true)
        } else {
            binding.btnSave.visibility = View.GONE
            binding.btnEdit.visibility = View.VISIBLE
            enableEditing(false)
        }
    }

    private fun showSaveProgress(show: Boolean) {
        binding.btnSave.isEnabled = !show
    }

    private fun enableEditing(editable: Boolean) {
        binding.etUserName.isEnabled = editable
        binding.etEmail.isEnabled = editable
        binding.etHeight.isEnabled = editable
        binding.etWeight.isEnabled = editable
        binding.etRunningReason.isEnabled = editable
        binding.etAddress.isEnabled = editable
    }

    private fun updateUI(user: com.example.an_app_for_runners_and_cyclists.data.model.User) {
        binding.tvUserName.text = user.name

        binding.etUserName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etHeight.setText(user.height?.toString() ?: "")
        binding.etWeight.setText(user.weight?.toString() ?: "")
        binding.etRunningReason.setText(user.runningReason ?: "")
        binding.etAddress.setText(user.address ?: "")

        user.profileImage?.let { imagePath ->
            try {
                if (imagePath.startsWith("/") && File(imagePath).exists()) {
                    val file = File(imagePath)
                    val uri = Uri.fromFile(file)
                    binding.profileImg.setImageURI(uri)
                    Timber.d("Profile image loaded from internal storage: $imagePath")
                } else {
                    val uri = Uri.parse(imagePath)
                    binding.profileImg.setImageURI(uri)
                    Timber.d("Profile image loaded as URI: $imagePath")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile image: $imagePath")
                binding.profileImg.setImageResource(R.drawable.base_profile_img)
            }
        } ?: run {
            binding.profileImg.setImageResource(R.drawable.base_profile_img)
        }

        updateStatsUI(user)
    }

    private fun updateStatsUI(user: com.example.an_app_for_runners_and_cyclists.data.model.User) {
        binding.tvDistanceValue.text = String.format("%.1f km", user.totalDistance)
        binding.tvDurationValue.text = RunCalculator.formatDuration(user.totalTime)
        binding.tvCaloriesValue.text = "${user.totalCalories} kCal"

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.calculatedStats.collect { stats ->
                android.util.Log.d("ProfileFragment", "Total runs: ${stats.totalRuns}, Average pace: ${stats.averagePace}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}