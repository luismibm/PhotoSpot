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

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.getLastLocation().addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                binding.textHome.setText(
                    String.format("Latitud: %.4f \n Longitud: %.4f \n Time: %d",
                        lastLocation.latitude,
                        lastLocation.longitude,
                        lastLocation.time)
                )
            } else {
                binding.textHome.setText("No location found")
            }
        }

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

    @SuppressLint("SetTextI18n")
    fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Request Permissions", Toast.LENGTH_SHORT).show()
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            Toast.makeText(requireContext(), "Permissions granted: getLocation()", Toast.LENGTH_SHORT).show()
            fusedLocationClient.getLastLocation().addOnSuccessListener { location ->
                if (location != null) {
                    fetchAddress(location)
                } else {
                    binding.textHome.setText("No location known")
                }
            }
        }
    }

    fun fetchAddress(location: Location) {

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        executor.execute {
            val addresses: List<Address>
            var resultMessage = ""

            try {
                addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )!! // cuidao con esto
                if (addresses == null || addresses.size == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No Address Found"
                        Log.e("LUISMIBM", resultMessage)
                    }
                } else {
                    var address = addresses.get(0)
                    var addressParts = ArrayList<String>()
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                    resultMessage = TextUtils.join("\n", addressParts)
                    handler.post { binding.textHome.setText(String.format("%s\n%tr", resultMessage, Date(System.currentTimeMillis()))) }
                }

            } catch (e: Exception) {
                resultMessage = "Failed: fetchAddress()"
                Log.e("LUISMIBM", resultMessage)
            }

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