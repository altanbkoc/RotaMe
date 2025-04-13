package com.altankoc.pinnedplaces.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.altankoc.pinnedplaces.databinding.RecyclerRowBinding
import com.altankoc.pinnedplaces.db.PlaceDao
import com.altankoc.pinnedplaces.db.PlaceDatabase
import com.altankoc.pinnedplaces.model.Place
import com.altankoc.pinnedplaces.util.BitmapUtils
import com.altankoc.pinnedplaces.util.ImageUtils
import com.altankoc.pinnedplaces.view.ListFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.findNavController

class PlaceAdapter(val placeList: ArrayList<Place>): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var placeDao: PlaceDao
    private lateinit var db: PlaceDatabase
    private lateinit var context: Context


    class PlaceHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {

        context = parent.context
        db = PlaceDatabase.getDatabase(context)
        placeDao = db.placeDao()


        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return PlaceHolder(binding)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.binding.textKonumAd.text = placeList[position].name

        val gorsel = BitmapUtils.byteArrayToBitmap(placeList[position].placeImage)

        holder.binding.imageKonum.setImageBitmap(gorsel)

        holder.binding.imageButton.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Silme İşlemi")
                .setMessage("Bu konumu silmek istediginize emin misiniz?")
                .setPositiveButton("Evet"){dialog, which ->
                    coroutineScope.launch(Dispatchers.IO) {
                        placeDao.delete(placeList[position])
                        launch(Dispatchers.Main) {
                            placeList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position,placeList.size)
                            ImageUtils.showToast(context,"Silindi!")

                        }
                    }

                    dialog.dismiss()

                }
                .setNegativeButton("Hayır"){dialog,which ->
                    dialog.dismiss()
                }.show()
        }

        holder.itemView.setOnClickListener {
            holder.itemView.findNavController().navigate(ListFragmentDirections.actionListFragmentToMapsFragment(placeList[position]))
        }


    }


}