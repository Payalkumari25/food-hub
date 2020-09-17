package com.example.foodapp.Fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    // Save the uri of ur image
    private val DEFAULT_IMAGE_URL =  "https://picsum.photos/200"
    private lateinit var imageUri : Uri
    private val REQUEST_IMAGE_CAPTURE = 100

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .into(img_view)
            et_name.setText(user.displayName)
            txt_email.text = user.email

            txt_phone.text = if (user.phoneNumber.isNullOrEmpty()) "Add Number"
            else user.phoneNumber

            if (user.isEmailVerified) {
                txt_not_verified.visibility = View.INVISIBLE
            } else {
                txt_not_verified.visibility = View.VISIBLE
            }
        }

        img_view.setOnClickListener {
            takePictureIntent()
        }

        btn_save.setOnClickListener {
            val photo = when {
                ::imageUri.isInitialized -> imageUri
                currentUser?.photoUrl == null -> Uri.parse(DEFAULT_IMAGE_URL)
                else -> currentUser.photoUrl
            }

            val name = et_name.text.toString().trim()

            if (name.isEmpty()) {
                et_name.error = " name required "
                et_name.requestFocus()
                return@setOnClickListener
            }
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photo)
                .build()

            progressbar.visibility = View.VISIBLE

            currentUser?.updateProfile(updates)
                ?.addOnCompleteListener { task ->
                    progressbar.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        context?.toast("Profile Updated")
                    } else {
                        context?.toast(task.exception?.message!!)
                    }
                }
        }

        txt_not_verified.setOnClickListener {

            currentUser?.sendEmailVerification()
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        context?.toast("Verification Email Sent")
                    } else {
                        context?.toast(it.exception?.message!!)
                    }
                }
        }

        txt_phone.setOnClickListener {
            val action = ProfileFragmentDirections.actionVerifyPhone()
            Navigation.findNavController(it).navigate(action)
        }

        txt_email.setOnClickListener {
            val action = ProfileFragmentDirections.actionUpdateEmail()
            Navigation.findNavController(it).navigate(action)
        }
        txt_password.setOnClickListener {
            val action = ProfileFragmentDirections.actionUpdatePassword()
            Navigation.findNavController(it).navigate(action)
        }


    }

        private fun takePictureIntent() {
            // Open the default app in the camera
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pictureIntent ->
                pictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                    startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_IMAGE_CAPTURE && requestCode == RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                uploadImageAndSaveUri(imageBitmap)
            }
        }

        private fun uploadImageAndSaveUri(bitmap: Bitmap) {
            val baos = ByteArrayOutputStream()
            val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val image = baos.toByteArray()

            val upload = storageRef.putBytes(image)

            progressbar_pic.visibility = View.VISIBLE
            upload.addOnCompleteListener { uploadTask ->
                progressbar_pic.visibility = View.INVISIBLE

                if (uploadTask.isSuccessful) {
                    storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                        urlTask.result?.let {
                            imageUri = it
                            activity?.toast(imageUri.toString())
                            img_view.setImageBitmap(bitmap)
                        }
                    }
                } else {
                    uploadTask.exception?.let {
                        activity?.toast(it.message!!)
                    }
                }

            }
        }
    }
