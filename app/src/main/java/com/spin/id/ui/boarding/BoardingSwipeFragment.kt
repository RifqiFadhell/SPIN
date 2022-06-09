package com.spin.id.ui.boarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spin.id.R
import kotlinx.android.synthetic.main.boarding_swipe_fragment.*

class BoardingSwipeFragment : Fragment() {

    private var drawableImage: Int? = null
    private var description: String? = null
    private var title: String? = null

    companion object {
        private const val DRAWABLE_IMAGE = "drawableImage"
        private const val DESCRIPTION = "description"
        private const val TITLE = "title"

        @JvmStatic
        fun newInstance(_drawableImage: Int? = null, _title: String? = null, _subtitle: String? = null) =
            BoardingSwipeFragment().apply {
                arguments = Bundle().apply {
                    _drawableImage?.let { putInt(DRAWABLE_IMAGE, it) }
                    putString(TITLE, _title)
                    putString(DESCRIPTION, _subtitle)
                }
            }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.boarding_swipe_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init(){
        initUI()
    }

    private fun initUI(){
        arguments?.let {
            drawableImage = it.getInt(DRAWABLE_IMAGE)
            description = it.getString(DESCRIPTION)
            title = it.getString(TITLE)
        }
        drawableImage?.let {
            Glide.with(requireActivity())
                .load(it)
                .apply(
                    RequestOptions().centerCrop()
                )
                .centerCrop()
                .into(imageBoarding)
        }

        textTitleBoarding?.text = title
        textSubtitleBoarding?.text = description

    }
}