package com.luismibm.photospot.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.luismibm.photospot.PhotoSpot
import com.luismibm.photospot.R
import com.luismibm.photospot.SharedViewModel
import com.luismibm.photospot.databinding.FragmentHomeBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.jvm.Throws

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    lateinit var lastLocation: Location
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var mCurrentPhotoPath = ""
    lateinit var photoUri: Uri
    val REQUEST_TAKE_PHOTO: Int = 1



    lateinit var authUser: FirebaseUser

    @SuppressLint("DefaultLocale", "MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedViewModel.currentLatLng.observe(viewLifecycleOwner) { latlng ->
            binding.txtLatitud.setText(latlng.latitude.toString())
            binding.txtLongitud.setText(latlng.longitude.toString())
        }

        sharedViewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            val newString = String.format("%s", address)
            binding.txtDireccio.text = Editable.Factory.getInstance().newEditable(newString)
        }

        sharedViewModel.buttonText.observe(viewLifecycleOwner) { s ->
            binding.buttonGetLocation.text = s
        }

        // Progress Bar Logic Here

        binding.buttonGetLocation.setOnClickListener { _ ->
            sharedViewModel.switchTrackingLocation()
            Log.d("DEBUG", "Clicked Button Get Location (HomeFragment)")
        }

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        binding.buttonNotify.setOnClickListener { button ->
            val photoSpot = PhotoSpot (
                latitude = binding.txtLatitud.text.toString(),
                longitude = binding.txtLongitud.text.toString(),
                location = binding.txtDireccio.text.toString(),
                description = binding.txtDescripcio.text.toString(),
                url = ""
            )
            var base: DatabaseReference = FirebaseDatabase.getInstance("https://photo-spot-luismi-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
            var users: DatabaseReference = base.child("users")
            var uid = users.child(authUser.uid)
            var photoSpots = uid.child("photospots")
            var reference = photoSpots.push()
            reference.setValue(photoSpot)
        }


        // var Foto = binding.foto
        val buttonFoto = binding.buttonFoto

        buttonFoto.setOnClickListener { button ->
            dispatchTakePictureIntent()
        }

        return root

    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date::class.java)
        var imageFileName = "JPEG_" + timeStamp + "_"
        var storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageFileName, ".jpg", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun dispatchTakePictureIntent() {
        var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // if (takePictureIntent.resolveActivity(context.packageManager) != null) {
        if (context?.let { takePictureIntent.resolveActivity(it.packageManager) } != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                Log.d("PHOTO", "Failed: dispatchTakePictureIntent()")
            }
            if (photoFile != null) {
                // photoUri = FileProvider.getUriForFile(context, "com.luismibm.photospot.fileprovider", photoFile)
                photoUri = context?.let { FileProvider.getUriForFile(it, "com.luismibm.photospot.fileprovider", photoFile) }!!
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoUri).into(binding.foto)
            } else {
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}