package com.example.an_app_for_runners_and_cyclists.ui.history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.an_app_for_runners_and_cyclists.databinding.ItemRunMonthBinding
import com.example.an_app_for_runners_and_cyclists.ui.history.RunHistoryViewModel
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator

class MonthRunAdapter(
    private val onMonthClick: (String) -> Unit,
    private val onRunClick: (String) -> Unit // ДОБАВЛЯЕМ КОЛБЭК ДЛЯ КЛИКА НА ПРОБЕЖКУ
) : ListAdapter<RunHistoryViewModel.MonthlyRunGroup, MonthRunAdapter.MonthViewHolder>(DiffCallback) {

    private var expandedMonths: Set<String> = emptySet()

    fun updateExpandedMonths(newExpandedMonths: Set<String>) {
        expandedMonths = newExpandedMonths
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ItemRunMonthBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MonthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthGroup = getItem(position)
        holder.bind(monthGroup)
    }

    inner class MonthViewHolder(
        private val binding: ItemRunMonthBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentMonthGroup: RunHistoryViewModel.MonthlyRunGroup
        private lateinit var runAdapter: RunAdapter

        init {
            binding.root.setOnClickListener {
                onMonthClick(currentMonthGroup.monthKey)
            }

            runAdapter = RunAdapter(onRunClick) // ПЕРЕДАЕМ КОЛБЭК В RUN ADAPTER
            binding.rvRuns.adapter = runAdapter
        }

        fun bind(monthGroup: RunHistoryViewModel.MonthlyRunGroup) {
            currentMonthGroup = monthGroup

            binding.tvMonth.text = monthGroup.displayName
            binding.tvMonthStats.text = "${monthGroup.totalRuns} Runs"
            binding.tvDistance.text = String.format("%.1f km", monthGroup.totalDistance)
            binding.tvTime.text = RunCalculator.formatDuration(monthGroup.totalDuration)
            binding.tvCalories.text = "${monthGroup.totalCalories} kCal"

            // Set expand/collapse icon rotation
            val isExpanded = expandedMonths.contains(monthGroup.monthKey)
            binding.iconArrowDown.rotation = if (isExpanded) 180f else 0f

            // Show/hide runs list
            binding.rvRuns.visibility = if (isExpanded) View.VISIBLE else View.GONE

            if (isExpanded) {
                runAdapter.submitList(monthGroup.runs)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<RunHistoryViewModel.MonthlyRunGroup>() {
        override fun areItemsTheSame(
            oldItem: RunHistoryViewModel.MonthlyRunGroup,
            newItem: RunHistoryViewModel.MonthlyRunGroup
        ): Boolean = oldItem.monthKey == newItem.monthKey

        override fun areContentsTheSame(
            oldItem: RunHistoryViewModel.MonthlyRunGroup,
            newItem: RunHistoryViewModel.MonthlyRunGroup
        ): Boolean = oldItem == newItem
    }
}