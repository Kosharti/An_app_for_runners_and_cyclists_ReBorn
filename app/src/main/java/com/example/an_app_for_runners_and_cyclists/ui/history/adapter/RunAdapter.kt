package com.example.an_app_for_runners_and_cyclists.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.databinding.ItemRunBinding
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import java.text.SimpleDateFormat
import java.util.Locale

class RunAdapter(
    private val onRunClick: (String) -> Unit // ДОБАВЛЯЕМ КОЛБЭК ДЛЯ КЛИКА
) : ListAdapter<Run, RunAdapter.RunViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = getItem(position)
        holder.bind(run)
    }

    inner class RunViewHolder( // ДЕЛАЕМ INNER CLASS ДЛЯ ДОСТУПА К КОЛБЭКУ
        private val binding: ItemRunBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(run: Run) {
            val dateFormat = SimpleDateFormat("EEE dd, hh:mma", Locale.getDefault())
            val dateString = dateFormat.format(run.startTime)

            binding.tvDate.text = dateString
            binding.tvStats.text = "${String.format("%.2f", run.distance)} km in ${RunCalculator.formatDuration(run.duration)}"

            // ОБРАБОТКА КЛИКА НА ПРОБЕЖКУ
            binding.root.setOnClickListener {
                onRunClick(run.id)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem == newItem
    }
}