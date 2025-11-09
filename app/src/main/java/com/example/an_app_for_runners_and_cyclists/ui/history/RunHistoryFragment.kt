package com.example.an_app_for_runners_and_cyclists.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.an_app_for_runners_and_cyclists.R
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
        setupHeader()
    }

    private fun setupRecyclerView() {
        monthRunAdapter = MonthRunAdapter(
            onMonthClick = { monthKey ->
                viewModel.toggleMonthExpansion(monthKey)
            },
            onRunClick = { runId ->
                navigateToRunDetails(runId)
            }
        )

        binding.rvRunHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = monthRunAdapter
        }
    }

    private fun setupHeader() {
        // Обработчик меню (три полоски)
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }

        // Обработчик иконки истории
        binding.historyIcon.setOnClickListener {
            // Уже в истории, ничего не делаем
        }

        // Заголовок
        binding.toolbarTitle.text = "Run History"
    }

    private fun showDropdownMenu() {
        val popup = PopupMenu(requireContext(), binding.menuIcon)
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tracking -> {
                    findNavController().navigate(R.id.runTrackingFragment)
                    true
                }
                R.id.action_history -> {
                    // Уже в истории
                    true
                }
                R.id.action_profile -> {
                    findNavController().navigate(R.id.profileFragment)
                    true
                }
                R.id.action_community -> {
                    findNavController().navigate(R.id.otherRunnersFragment)
                    true
                }
                else -> false
            }
        }
        popup.show()
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

    private fun navigateToRunDetails(runId: String) {
        val action = RunHistoryFragmentDirections.actionRunHistoryFragmentToRunDetailsFragment(runId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}