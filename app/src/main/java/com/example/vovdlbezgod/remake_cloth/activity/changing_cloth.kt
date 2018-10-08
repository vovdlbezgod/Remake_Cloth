package com.example.vovdlbezgod.remake_cloth.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.example.photoeditor.PhotoEditor
import com.example.photoeditor.PhotoEditorView
import com.example.photoeditor.SaveSettings
import com.example.vovdlbezgod.remake_cloth.R
import com.example.vovdlbezgod.remake_cloth.StickerBSFragment
import kotlinx.android.synthetic.main.activity_changing_cloth.*
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException

class changing_cloth : MediaActivity(), StickerBSFragment.StickerListener{
    override fun onPhotoTaken() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStickerClick(bitmap: Bitmap?) {

    }

    private var selectedImagePathClothChangActivity: String? = null
    private val TAG = "Changing_cloth"
    private var localBitmap: Bitmap? = null
    private var imageView: PhotoEditorView? = null
    private var mPhotoEditor: PhotoEditor? = null
    private var mStickerBSFragment: StickerBSFragment? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_change -> {
                mStickerBSFragment!!.show(supportFragmentManager, mStickerBSFragment!!.getTag())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_save -> {
                saveImage()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changing_cloth)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        selectedImagePathClothChangActivity = intent.extras!!.getString("selectedImagePath")
        activityInit()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //takePictureButton!!.isEnabled = false
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    fun activityInit() {
        try {
            mStickerBSFragment = StickerBSFragment()
            mStickerBSFragment!!.setStickerListener(this)
            imageView = findViewById<View>(R.id.imageShow) as PhotoEditorView
            val selectedImagePath = intent.extras!!.getString("selectedImagePath")
            getImage(this, selectedImagePath)
            //maskBitmap = mas
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }

    fun getImage(activity: Activity, imagePath: String) {
        Thread {
            Log.i(TAG, imagePath)
            activity.runOnUiThread {
                localBitmap = BitmapFactory.decodeFile(imagePath)
                drawBitmap(this, localBitmap)
            }

        }.start()
    }

    fun drawBitmap(activity: Activity, src: Bitmap?) {
        Thread {
            try {
                activity.runOnUiThread {
                    mPhotoEditor = PhotoEditor.Builder(this, imageView).build() // build photo editor sdk
                    imageView!!.source.setImageBitmap(src)
                }

            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo")
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Failed to create directory")
                }
            }
            val file = File(mediaStorageDir.path + File.separator + ""
                    + System.currentTimeMillis() + ".jpg")
            try {
                file.createNewFile()

                val saveSettings = SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build()

                mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        hideLoading()
                        showSnackbar("Image Saved Successfully in $imagePath")
                        imageView!!.source.setImageURI(Uri.fromFile(File(imagePath)))
                    }

                    override fun onFailure(exception: Exception) {
                        hideLoading()
                        showSnackbar("Failed to save Image")
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                showSnackbar(e.message!!)
            }

        }
    }

}
