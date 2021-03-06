package io.moka.lib.imagepicker.image.editor


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.theartofdev.edmodo.cropper.CropImageView
import io.moka.lib.imagepicker.R
import io.moka.lib.imagepicker.util.LocationType
import io.moka.lib.imagepicker.util.SaveImageUtil
import kotlinx.android.synthetic.main.fragment_image_editor_crop.*
import java.io.File
import java.util.*


class ImageCropFragment : Fragment() {

    private var selectedImagePaths = ArrayList<String>()

    private var aspectX: Float = 0.toFloat()
    private var aspectY: Float = 0.toFloat()
    private lateinit var locationType: LocationType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_editor_crop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProperty()

        imageView_home.setOnClickListener { activity!!.onBackPressed() }
        textView_menu.setOnClickListener { onSaveCropImage() }
    }

    /**
     */

    private fun setProperty() {
        selectedImagePaths = activity!!.intent.extras.getStringArrayList(ImageEditorActivity.IMAGE_PATHES)

        aspectX = activity!!.intent.extras.getFloat(ImageEditorActivity.ASPECT_X)
        aspectY = activity!!.intent.extras.getFloat(ImageEditorActivity.ASPECT_Y)

        locationType = LocationType.get(activity!!.intent.extras.getInt(ImageEditorActivity.IMAGE_LOCATION))

        setImage(selectedImagePaths, aspectX, aspectY)
    }

    private fun setImage(selectedImagePaths: ArrayList<String>, aspectX: Float, aspectY: Float) {
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true

        val imagePath = selectedImagePaths[0]

        BitmapFactory.decodeFile(imagePath, option)
        val rate = option.outHeight.toFloat() / option.outWidth.toFloat()

        cropImageView_image.setAspectRatio(aspectX.toInt(), aspectY.toInt())
        cropImageView_image.setFixedAspectRatio(true)
        cropImageView_image.guidelines = CropImageView.Guidelines.ON

        if (imagePath.startsWith("http"))
            setImageFromRemote(imagePath)
        else
            setImageFromLocal(imagePath, rate)
    }

    fun setImageFromRemote(imagePath: String) {
        val imageUri = Uri.parse(imagePath)

        cropImageView_image.setImageUriAsync(imageUri)
    }

    fun setImageFromLocal(imagePath: String, rate: Float) {
        val imageUri = Uri.fromFile(File(imagePath))
        setImage(imageUri, rate)
    }

    fun setImage(imageUri: Uri, rate: Float) {
        if (0 < rate) {

            val outMetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(outMetrics)
            val targetWidth = outMetrics.widthPixels
            val targetHeight = (outMetrics.widthPixels * rate).toInt()

            cropImageView_image.setImageUriAsync(imageUri)
        }
    }

    /**
     * Listener, onClickSave
     */

    fun onSaveCropImage() {
        try {
            if (!isAdded || null == cropImageView_image.croppedImage)
                return

            val resizedBitmap = Bitmap.createScaledBitmap(cropImageView_image.croppedImage, aspectX.toInt(), aspectY.toInt(), true)

            SaveImageUtil
                    .from(resizedBitmap, activity!!)
                    .setImageLocation(locationType)
                    .start { imagePaths ->

                        val intent = Intent()
                        val imagesArray = ArrayList<String>()

                        imagesArray.add(imagePaths[0])

                        intent.putStringArrayListExtra(ImageEditorActivity.SAVED_IMAGE_PATHS, imagesArray)

                        activity?.setResult(Activity.RESULT_OK, intent)
                        activity?.finish()
                    }
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
    }

    /**
     */

}
