package com.spin.id.ui.home.feed.banner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spin.id.R
import com.spin.id.utils.extensions.loadImage
import kotlinx.android.synthetic.main.carousel_item.*

class BannerFragment : Fragment() {
    private var urlThumbnail: String? = null
    private var link: String? = null

    companion object {
        private const val URL_THUMBNAIL = "URL_THUMBNAIL"

        fun newInstance(_urlThumbnail: String) = BannerFragment().apply {
            arguments = Bundle().apply {
                putString(URL_THUMBNAIL, _urlThumbnail)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.carousel_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        arguments?.let {
            urlThumbnail = it.getString(URL_THUMBNAIL)
        }
        image?.loadImage(requireContext(), urlThumbnail, R.drawable.ic_place_holder_big)
    }
}