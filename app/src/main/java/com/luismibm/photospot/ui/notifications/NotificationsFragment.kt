package com.luismibm.photospot.ui.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.luismibm.photospot.PhotoSpot
import com.luismibm.photospot.R
import com.luismibm.photospot.SharedViewModel
import com.luismibm.photospot.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.g_map) as SupportMapFragment

        val auth = FirebaseAuth.getInstance()
        val base = FirebaseDatabase.getInstance("https://photo-spot-luismi-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val user = base.child("users")
        val uid = user.child(auth.uid!!)
        val photoSpots = uid.child("photospots")

        mapFragment.getMapAsync { map ->
            map.isMyLocationEnabled = true
            val currentLatLng = sharedViewModel.currentLatLng
            val owner = viewLifecycleOwner
            Log.d("MapDebug", "Valor inicial de currentLatLng: ${sharedViewModel.currentLatLng.value}")
            currentLatLng.observe(owner) { latLng ->
                if (latLng != null) {
                    Log.d("MapDebug", "Moviendo cámara a: $latLng")
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    map.animateCamera(cameraUpdate)
                } else {
                    Log.d("MapDebug", "currentLatLng aún no tiene un valor válido.")
                }
                currentLatLng.removeObservers(owner)
            }
            photoSpots.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val photoSpot = dataSnapshot.getValue(PhotoSpot::class.java)
                    Log.d("FirebaseData", "Nuevo PhotoSpot: ${dataSnapshot.value}")
                    if (photoSpot != null) {
                        val aux = LatLng(photoSpot.latitude.toDouble(), photoSpot.longitude.toDouble())
                        Log.d("MapMarkers", "Añadiendo marcador en: ${photoSpot.latitude}, ${photoSpot.longitude}")
                        map.addMarker(
                            MarkerOptions()
                                .title(photoSpot.description)
                                .snippet(photoSpot.location)
                                .position(aux)
                        )
                        Log.d("MapMarkers", "MARCADOR AÑADIDO en: ${photoSpot.latitude}, ${photoSpot.longitude}")
                    } else {
                        Log.e("FirebaseData", "El objeto PhotoSpot es null")
                    }
                }
                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }

            })
        }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}