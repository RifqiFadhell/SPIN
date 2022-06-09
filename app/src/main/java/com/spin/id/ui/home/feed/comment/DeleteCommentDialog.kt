package com.spin.id.ui.home.feed.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.spin.id.R
import com.spin.id.utils.base.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.delete_comment_dialog.*

class DeleteCommentDialog : RoundedBottomSheetDialogFragment() {
    lateinit var callback: () -> Unit
    companion object {
        fun show(
            fm: FragmentManager,
            callback: () -> Unit
        ) = DeleteCommentDialog().apply {
            this.callback = callback
        }.show(fm, "DIALOG DELETE COMMENT")
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
        return inflater.inflate(R.layout.delete_comment_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventUI()
    }

    private fun eventUI() {
        buttonDeleteComment?.setOnClickListener {
            callback.invoke()
            dismiss()
        }
        buttonCancel?.setOnClickListener {
            dismiss()
        }
    }
}