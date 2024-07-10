package com.dicoding.picodiploma.mybottomnavigation.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dicoding.birdie.R
import com.dicoding.birdie.adapter.BurungAdapter
import com.dicoding.birdie.data.burunge
import com.dicoding.birdie.databinding.FragmentHomeBinding
import com.dicoding.birdie.view.detail.detail_home.DetailHomeActivity



class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val list = ArrayList<burunge>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list.addAll(getListBirds())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showRecyclerGrid()
        ClickListener()
    }

    private fun ClickListener() {
        binding.buttonScan.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_scan)
        }
        binding.tips.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_tipsFragment)
        }
        binding.Attracting.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_trickFragment)
        }
    }

    private fun getListBirds(): ArrayList<burunge> {
        val dataName = resources.getStringArray(R.array.data_name)
        val dataDescription = resources.getStringArray(R.array.data_description)
        val dataPhoto = resources.obtainTypedArray(R.array.data_photo)

        val listBird = ArrayList<burunge>()

        // Pastikan panjang array sesuai dengan ekspektasi sebelum menggunakan indeksnya
        val minSize = minOf(dataName.size, dataDescription.size, dataPhoto.length())

        for (i in 0 until minSize) {
            val bird = burunge(dataName[i], dataDescription[i], dataPhoto.getResourceId(i, -1))
            listBird.add(bird)
        }
        dataPhoto.recycle()
        return listBird
    }

    private fun showRecyclerGrid() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        val birdAdapter = BurungAdapter(list)
        binding.recyclerView.adapter = birdAdapter
        birdAdapter.setOnItemClickCallback(object : BurungAdapter.OnItemClickCallback() {
            override fun onItemClicked(data: burunge) {
                val intent = Intent(requireActivity(), DetailHomeActivity::class.java)
                intent.putExtra("EXTRA_NAME", data.name)
                intent.putExtra("EXTRA_DESCRIPTION", data.description)
                intent.putExtra("EXTRA_PHOTO", data.photo)
                startActivity(intent)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}