package com.luismibm.photospot.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.luismibm.photospot.PhotoSpot
import com.luismibm.photospot.SharedViewModel
import com.luismibm.photospot.databinding.FragmentDashboardBinding
import com.luismibm.photospot.databinding.ItemSpotBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    lateinit var authUser: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user as FirebaseUser
            val base = FirebaseDatabase.getInstance("https://photo-spot-luismi-default-rtdb.europe-west1.firebasedatabase.app/").reference
            val users = base.child("users")
            val uid = users.child(authUser.uid)
            val photoSpots = uid.child("photospots")
            val options = FirebaseRecyclerOptions.Builder<PhotoSpot>()
                .setQuery(photoSpots, PhotoSpot::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()
            val adapter = SpotAdapter(options)
            binding.rvPhotoSpots.adapter = adapter
            binding.rvPhotoSpots.layoutManager = LinearLayoutManager(requireContext())
            Log.d("DASHBOARD", "User is not null")
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SpotAdapter(options: FirebaseRecyclerOptions<PhotoSpot>): FirebaseRecyclerAdapter<PhotoSpot, SpotAdapter.SpotViewHolder>(options) {

        override fun onBindViewHolder(
            holder: SpotAdapter.SpotViewHolder,
            position: Int,
            model: PhotoSpot
        ) {
            holder.binding.txtDescripcio.text = model.description
            holder.binding.txtAdreca.text = model.location
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotAdapter.SpotViewHolder {
            val binding = ItemSpotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SpotViewHolder(binding)
        }

        class SpotViewHolder(val binding: ItemSpotBinding): RecyclerView.ViewHolder(binding.root)

    }

}