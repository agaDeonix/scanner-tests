package com.pinkunicorp.scannertest.ui.FirebaseML

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_firebase_ml.*
import com.pinkunicorp.scannertest.filter.util.AndroidUtils
import android.R.attr.src
import android.R.attr.bitmap
import com.pinkunicorp.scannertest.filter.*


class FirebaseMLActivity : AppCompatActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.pinkunicorp.scannertest.R.layout.activity_firebase_ml)
        FirebaseApp.initializeApp(application)

        btnTakePhoto?.setOnClickListener {
            btnTakePhoto?.visibility = View.GONE
            layoutWait.visibility = View.VISIBLE
            camera?.captureImage { cameraKitView, bytes ->
                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap = applyFilters(bitmap)
                getCardDetails(bitmap)
            }
        }
        layoutWait?.visibility = View.GONE
        btnTakePhoto?.visibility = View.VISIBLE
        layoutPhoto?.visibility = View.GONE
    }

    private fun applyFilters(bitmap: Bitmap?): Bitmap? {
        val width = bitmap?.width ?: 0
        val height = bitmap?.height ?: 0
        var src = AndroidUtils.bitmapToIntArray(bitmap)

        GrayscaleFilter().apply {
            src = filter(src, width, height)
        }
//        InvertFilter().apply {
//            src = filter(src, width, height)
//        }

        //Change the Bitmap int Array (Supports only ARGB_8888)
        return Bitmap.createBitmap(src, width, height, Bitmap.Config.ARGB_8888)
    }

    @SuppressLint("RestrictedApi")
    private fun getCardDetails(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val firebaseVisionTextDetector = FirebaseVision.getInstance().cloudTextRecognizer
        tvCardInfo.text = ""
        firebaseVisionTextDetector.processImage(image)
            .addOnSuccessListener {
                val words = it.text.split("\n")
                for (word in words) {
                    Log.e("TAG", word)
                    //REGEX for detecting a credit card
                    if (word.replace(
                            " ",
                            ""
                        ).matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$"))
                    ) tvCardInfo.text = word
                    //Find a better way to do this
                    if (word.contains("/")) {
                        for (year in word.split(" ")) {
                            if (year.contains("/"))
                                tvCardInfo.text = tvCardInfo.text.toString() + "\n" + year
                        }
                    }

                }
                layoutPhoto?.visibility = View.VISIBLE
                ivPhoto?.setImageBitmap(bitmap)
                layoutWait?.visibility = View.GONE
            }
            .addOnFailureListener {
                layoutWait?.visibility = View.GONE
                btnTakePhoto?.visibility = View.VISIBLE
                Toast.makeText(baseContext, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        camera?.onStart()
    }

    override fun onResume() {
        super.onResume()
        camera?.onResume()
    }

    override fun onPause() {
        camera?.onPause()
        super.onPause()
    }

    override fun onStop() {
        camera?.onStop()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        camera?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (layoutPhoto?.visibility == View.VISIBLE){
            layoutPhoto?.visibility = View.GONE
            btnTakePhoto?.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}
