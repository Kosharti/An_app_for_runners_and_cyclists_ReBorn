package com.example.an_app_for_runners_and_cyclists.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.databinding.ItemRunnerBinding
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator

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

            // TODO: Установить аватар, когда будет функционал
            // binding.ivRunnerAvatar.setImageResource(...)
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}