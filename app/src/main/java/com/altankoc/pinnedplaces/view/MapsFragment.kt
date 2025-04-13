package com.altankoc.pinnedplaces.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.altankoc.pinnedplaces.R
import com.altankoc.pinnedplaces.databinding.FragmentMapsBinding
import com.altankoc.pinnedplaces.model.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private val PERMISSION_REQUEST_CODE = 123
    private var isFromRecyclerView = false
    private var receivedPlace: Place? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map.apply {
            uiSettings.isMyLocationButtonEnabled = !isFromRecyclerView
            uiSettings.isCompassEnabled = true
        }

        if (isFromRecyclerView) {
            receivedPlace?.let { place ->
                val location = LatLng(place.latitude, place.longitude)
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(place.name)
                )
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

                binding.btnKaydet.text = "Geri Dön"
                binding.btnKaydet.isVisible = true
            }
        } else {
            setupMapFeatures()
            checkLocationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            receivedPlace = it.getParcelable("thePlace")
            isFromRecyclerView = receivedPlace != null
        }
    }

    private fun setupMapFeatures() {
        googleMap?.apply {
            setOnMapLongClickListener { latLng ->
                selectedLocation = latLng
                clear()
                addMarker(MarkerOptions().position(latLng).title("Seçilen Konum"))
                binding.btnKaydet.isVisible = true
            }

            setOnMyLocationButtonClickListener {
                checkLocationPermission()
                true
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableLocationFeatures()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionSnackbar()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun enableLocationFeatures() {
        try {
            googleMap?.isMyLocationEnabled = true

            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                10f,
                locationListener
            )

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude), 15f))
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showPermissionSnackbar() {
        Snackbar.make(binding.root, "Konum erisimi icin izin gerekiyor", Snackbar.LENGTH_INDEFINITE)
            .setAction("İzin Ver") {
                requestLocationPermission()
            }
            .show()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures()
            } else {
                showPermissionSnackbar()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMapFragment()
        setupSaveButton()
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setupSaveButton() {
        if (isFromRecyclerView) {
            binding.btnKaydet.text = "Geri Dön"
            binding.btnKaydet.setOnClickListener {
                findNavController().popBackStack()
            }
        } else {
            binding.btnKaydet.setOnClickListener {
                selectedLocation?.let { location ->
                    val action = MapsFragmentDirections.actionMapsFragmentToAddFragment(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    )
                    findNavController().navigate(action)
                } ?: run {
                    Snackbar.make(binding.root, "Lütfen bir konum seciniz!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        try {
            if (!isFromRecyclerView) {
                locationManager.removeUpdates(locationListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}