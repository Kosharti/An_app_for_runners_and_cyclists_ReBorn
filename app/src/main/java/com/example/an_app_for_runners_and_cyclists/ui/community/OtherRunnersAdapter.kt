package com.example.an_app_for_runners_and_cyclists.ui.community

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.databinding.ItemRunnerBinding
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import timber.log.Timber
import java.io.File

class OtherRunnersAdapter : ListAdapter<User, OtherRunnersAdapter.RunnerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunnerViewHolder {
        val binding = ItemRunnerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RunnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunnerViewHolder, position: Int) {
        val runner = getItem(position)
        holder.bind(runner)
    }

    class RunnerViewHolder(
        private val binding: ItemRunnerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(runner: User) {
            binding.tvRunnerName.text = runner.name
            binding.tvRunnerAddress.text = runner.address ?: "No address provided"
            binding.tvRunnerStats.text = "Total: ${String.format("%.1f", runner.totalDistance)} km, ${RunCalculator.formatDuration(runner.totalTime)}"

            loadProfileImage(runner.profileImage)

            Timber.d("Loading user: ${runner.name}, photo: ${runner.profileImage}")
        }

        private fun loadProfileImage(imageUri: String?) {
            if (!imageUri.isNullOrEmpty()) {
                try {
                    Timber.d("Attempting to load image: $imageUri")

                    Glide.with(binding.root.context)
                        .load(imageUri)
                        .error(R.drawable.base_profile_img)
                        .placeholder(R.drawable.base_profile_img)
                        .into(binding.ivRunnerAvatar)

                    Timber.d("✅ Image loaded successfully: $imageUri")

                } catch (e: Exception) {
                    Timber.e(e, "❌ Failed to load profile image: $imageUri")
                    setDefaultAvatar()
                }
            } else {
                Timber.d("🔄 No profile image, using default")
                setDefaultAvatar()
            }
        }

        private fun setDefaultAvatar() {
            binding.ivRunnerAvatar.setImageResource(R.drawable.base_profile_img)
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}