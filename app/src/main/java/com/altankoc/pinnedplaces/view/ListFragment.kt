package com.altankoc.pinnedplaces.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.altankoc.pinnedplaces.R
import com.altankoc.pinnedplaces.adapter.PlaceAdapter
import com.altankoc.pinnedplaces.databinding.FragmentAddBinding
import com.altankoc.pinnedplaces.databinding.FragmentListBinding
import com.altankoc.pinnedplaces.db.PlaceDao
import com.altankoc.pinnedplaces.db.PlaceDatabase
import com.altankoc.pinnedplaces.model.Place
import com.altankoc.pinnedplaces.util.ImageUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var placeDao: PlaceDao
    private lateinit var db: PlaceDatabase
    private var places: ArrayList<Place> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = PlaceDatabase.getDatabase(requireContext())
        placeDao = db.placeDao()

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        binding.recyclerView.adapter = PlaceAdapter(places)

        binding.floatingActionButton.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(ListFragmentDirections.actionListFragmentToMapsFragment())
        }

        loadPlaces()
    }

    private fun loadPlaces() {
        lifecycleScope.launch {
            try {
                val placeList = withContext(Dispatchers.IO) {
                    placeDao.getAll()
                }
                places.clear()
                places.addAll(placeList)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                ImageUtils.showToast(requireContext(),"Bir hata meydana geldi!")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlaces()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}