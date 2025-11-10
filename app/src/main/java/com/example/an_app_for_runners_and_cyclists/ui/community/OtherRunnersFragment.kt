package com.example.an_app_for_runners_and_cyclists.ui.community

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
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentOtherRunnersBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherRunnersFragment : Fragment() {

    private var _binding: FragmentOtherRunnersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OtherRunnersViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    private lateinit var adapter: OtherRunnersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherRunnersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupHeader() {
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }

        binding.toolbarTitle.text = "Community"
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
                    findNavController().navigate(R.id.runHistoryFragment)
                    true
                }
                R.id.action_profile -> {
                    findNavController().navigate(R.id.profileFragment)
                    true
                }
                R.id.action_community -> {
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        adapter = OtherRunnersAdapter()
        binding.rvOtherRunners.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOtherRunners.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.runners.collectLatest { runners ->
                adapter.submitList(runners)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}