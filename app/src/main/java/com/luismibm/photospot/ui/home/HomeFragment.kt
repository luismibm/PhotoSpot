package com.luismibm.photospot.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.luismibm.photospot.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) { textView.text = it } // Default Text

        val buttonHome = binding.button
        buttonHome.setOnClickListener { getLocation() }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Request Permissions", Toast.LENGTH_SHORT).show()
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            Toast.makeText(requireContext(), "Permissions granted: getLocation()", Toast.LENGTH_SHORT).show()
        }
    }

    var locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val fineLocationGranted =
            result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseLocationGranted =
            result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (fineLocationGranted) {
            getLocation()
        } else if (coarseLocationGranted) {
            getLocation()
        } else {
            Toast.makeText(requireContext(), "Failed: locationPermissionRequest", Toast.LENGTH_SHORT).show()
        }
    }

}