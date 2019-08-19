package com.example.photometa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.example.photometa.adapter.PhotoCollectionAdapter
import com.example.photometa.extension.dp
import com.example.photometa.model.PhotoData
import com.example.photometa.ui.GridSpaceItemDecoration
import kotlinx.android.synthetic.main.full_photo_activity.*
import kotlinx.android.synthetic.main.full_photo_activity.view.*
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.android.synthetic.main.image_collection_cell.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class HomeActivity: AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL)
        setContentView(R.layout.home_activity)

        photoCollection.layoutManager = GridLayoutManager(this, 3)
        photoCollection.addItemDecoration(GridSpaceItemDecoration(1.dp(this.resources)))
        photoCollection.setHasFixedSize(true)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        } else {
            launch {
                loadImageList()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                launch {
                    loadImageList()
                }
            }
        }

    }

    private fun loadImageList() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.SIZE
        )
        val dataCursor = this.contentResolver.query(uri, projection, null, null, null)

        val dataIndex = dataCursor.getColumnIndex(MediaStore.MediaColumns.DATA)
        val nameIndex = dataCursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
        val heightIndex =dataCursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
        val widthIndex =dataCursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
        val sizeIndex =dataCursor.getColumnIndex(MediaStore.MediaColumns.SIZE)

        var photoData: ArrayList<PhotoData> = ArrayList()

        while (dataCursor.moveToNext()) {
            photoData.add(PhotoData(
                dataCursor.getString(dataIndex),
                dataCursor.getString(nameIndex) ?: "",
                dataCursor.getInt(sizeIndex),
                dataCursor.getInt(widthIndex),
                dataCursor.getInt(heightIndex)
            ))
        }

        setImagesData(photoData)
    }

    @UiThread
    private fun setImagesData(photoData: List<PhotoData>) {
        photoCollection.adapter = PhotoCollectionAdapter(this, photoData) { view ->
            val data: PhotoData = view.tag as PhotoData
            var intent = Intent(this, FullPhotoActivity::class.java)
            intent.putExtra("sample", data)
            val activityOpt = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view.imageView, "fullphoto")
            startActivity(intent, activityOpt.toBundle() )
        }
    }


}