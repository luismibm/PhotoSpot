package com.luismibm.photospot.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.luismibm.photospot.SharedViewModel
import com.luismibm.photospot.databinding.FragmentHomeBinding
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    lateinit var lastLocation: Location
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("DefaultLocale", "MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedViewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            binding.textHome.text = String.format("Address: %s \n Time: %tr", address, System.currentTimeMillis())
        }

        sharedViewModel.buttonText.observe(viewLifecycleOwner) { s ->
            binding.button.text = s
        }

        // Progress Bar Logic Here

        binding.button.setOnClickListener { _ ->
            sharedViewModel.switchTrackingLocation()
            Log.d("DEBUG", "Clicked Button Get Location (HomeFragment)")
        }


        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}