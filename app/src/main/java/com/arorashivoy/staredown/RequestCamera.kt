package com.arorashivoy.staredown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.activity.result.contract.ActivityResultContracts

class RequestCamera: BottomSheetDialogFragment() {
    private lateinit var requestBtn: Button
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                dismiss() // Dismiss only if permission is granted
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    // TODO: Fix this animation
    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_request_camera, container, false)

        requestBtn = view.findViewById<Button>(R.id.requestPermissionBtn)

        requestBtn.setOnClickListener{
            val context = requireContext()

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                dismiss()
            }
        }

        return view

    }

    companion object {
        fun newInstance(): RequestCamera {
            return RequestCamera()
        }
    }
}