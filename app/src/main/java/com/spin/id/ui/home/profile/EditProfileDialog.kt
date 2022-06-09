package com.spin.id.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.spin.id.R
import com.spin.id.utils.base.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.edit_profile_bottom_sheet.*

class EditProfileDialog: RoundedBottomSheetDialogFragment() {
    lateinit var callbackChangePhoto: () -> Unit
    lateinit var callbackChangeUsername: () -> Unit
    lateinit var callbackChangeBio: () -> Unit
    companion object {
        fun show(
            fm: FragmentManager,
            callbackChangePhoto: () -> Unit,
            callbackChangeUsername: () -> Unit,
            callbackChangeBio: () -> Unit
        ) = EditProfileDialog().apply {
            this.callbackChangePhoto = callbackChangePhoto
            this.callbackChangeUsername = callbackChangeUsername
            this.callbackChangeBio = callbackChangeBio
        }.show(fm, "DIALOG EDIT PROFILE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(R.id.design_bottom_sheet) as View
            BottomSheetBehavior.from(bottomSheetInternal).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return inflater.inflate(R.layout.edit_profile_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventUI()
    }

    private fun eventUI() {
        textChangePhoto?.setOnClickListener {
            callbackChangePhoto.invoke()
            dialog?.dismiss()
        }
        textUsername?.setOnClickListener {
            callbackChangeUsername.invoke()
            dialog?.dismiss()
        }
        textBio?.setOnClickListener {
            callbackChangeBio.invoke()
            dialog?.dismiss()
        }
        buttonCancel?.setOnClickListener {
            dialog?.dismiss()
        }
    }
}