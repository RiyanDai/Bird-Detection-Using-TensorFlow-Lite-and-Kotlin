package com.dicoding.birdie.view.fragment.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.birdie.R
import com.dicoding.birdie.adapter.HistoryAdapter
import com.dicoding.birdie.database.AnalysisResult
import com.dicoding.birdie.databinding.FragmentHistoryFragmentsBinding
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragments.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragments : Fragment(R.layout.fragment_history_fragments) {
    private var _binding: FragmentHistoryFragmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    private val analysisResultViewModel: HistoryViewModel by viewModels {
        AnalysisResultViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryFragmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()


    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { analysisResult ->
            deleteResult(analysisResult)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun observeData() {
        analysisResultViewModel.allResults.observe(viewLifecycleOwner) { results ->
            historyAdapter.updateData(results)
        }
    }

    private fun deleteResult(analysisResult: AnalysisResult) {
        lifecycleScope.launch {
            analysisResultViewModel.delete(analysisResult)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}