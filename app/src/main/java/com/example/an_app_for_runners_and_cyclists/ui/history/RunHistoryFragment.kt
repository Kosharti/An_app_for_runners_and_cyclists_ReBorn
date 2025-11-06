package com.example.an_app_for_runners_and_cyclists.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentRunHistoryBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.example.an_app_for_runners_and_cyclists.ui.history.adapter.MonthRunAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class RunHistoryFragment : Fragment() {

    private var _binding: FragmentRunHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RunHistoryViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    private lateinit var monthRunAdapter: MonthRunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        monthRunAdapter = MonthRunAdapter(
            onMonthClick = { monthKey ->
                viewModel.toggleMonthExpansion(monthKey)
            }
        )

        binding.rvRunHistory.apply {
            adapter = monthRunAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlyRuns.collectLatest { monthlyRuns ->
                monthRunAdapter.submitList(monthlyRuns)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expandedMonths.collectLatest { expandedMonths ->
                monthRunAdapter.updateExpandedMonths(expandedMonths)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}