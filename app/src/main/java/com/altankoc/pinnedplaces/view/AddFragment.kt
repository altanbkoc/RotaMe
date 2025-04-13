package com.altankoc.pinnedplaces.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.altankoc.pinnedplaces.R
import com.altankoc.pinnedplaces.databinding.FragmentAddBinding
import com.altankoc.pinnedplaces.databinding.FragmentMapsBinding
import com.altankoc.pinnedplaces.db.PlaceDao
import com.altankoc.pinnedplaces.db.PlaceDatabase
import com.altankoc.pinnedplaces.model.Place
import com.altankoc.pinnedplaces.util.BitmapUtils
import com.altankoc.pinnedplaces.util.ImageUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var args: AddFragmentArgs? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private lateinit var placeDao: PlaceDao
    private lateinit var db: PlaceDatabase

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedUri: Uri? = null
    private var selectedByteArray: ByteArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = arguments?.let { AddFragmentArgs.fromBundle(it) }



        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {

                    ImageUtils.openGallery(galleryLauncher)
                } else {

                    ImageUtils.showToast(requireContext(), "İzin gerekli!")
                }
            }


        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    intent?.data?.let { uri ->
                        selectedUri = uri
                        ImageUtils.getBitmapFromUri(requireContext(), uri)?.let { bitmap ->
                            val sclaedImage = BitmapUtils.bitmapKucult(bitmap, 300)
                            selectedByteArray = BitmapUtils.bitmapToByteArray(sclaedImage)
                            binding.imageView.setImageBitmap(sclaedImage)
                        }
                    }
                }
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = PlaceDatabase.getDatabase(requireContext())
        placeDao = db.placeDao()

       args?.let { konum ->
           lat = konum.latitude.toDouble()
           lng = konum.longitude.toDouble()

           binding.textLat.text = "Enlem: $lat"
           binding.textLng.text = "Boylam: $lng"
       }?: run {
           Snackbar.make(requireView(), "Konum bilgisi alınamadı!", Snackbar.LENGTH_LONG).show()

       }

        binding.imageView.setOnClickListener {
            ImageUtils.checkAndRequestPermission(
                requireActivity(),
                requireView(),
                permissionLauncher
            ) {
                ImageUtils.openGallery(galleryLauncher)
            }
        }


        binding.btnKonumuKaydet.setOnClickListener {
            val konumAd = binding.editTextKonumAd.text.toString().trim()

            selectedByteArray?.let { byteArray ->
                lat?.let { latitude ->
                    lng?.let { longitude ->
                        if (konumAd.isNotEmpty()) {
                            lifecycleScope.launch {
                                try {
                                    val place = Place(
                                        name = konumAd,
                                        latitude = latitude,
                                        longitude = longitude,
                                        placeImage = byteArray
                                    )
                                    placeDao.insert(place)
                                     findNavController().popBackStack(R.id.listFragment, false)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Snackbar.make(requireView(), "Kayıt basarısız: ${e.message}", Snackbar.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            ImageUtils.showToast(requireContext(), "Konum adı giriniz!")
                        }
                    }
                }
            } ?: run {
                ImageUtils.showToast(requireContext(), "Konum icin bir görsel secin!")
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().supportFragmentManager.popBackStack()
    }


}