package com.spin.id.utils.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spin.id.utils.listener.RecyclerViewItemClickListener

open class BaseHolder<T> constructor(private val listener: RecyclerViewItemClickListener<T>, itemView: View) :
  RecyclerView.ViewHolder(itemView), View.OnClickListener {

  protected var itemPosition: Int = 0
    private set
  protected var item: T? = null
    private set

  init {
    itemView.setOnClickListener(this)
  }

  override fun onClick(view: View) {
    listener.itemClick(itemPosition, item, view.id)
  }

  fun bindData(position: Int, data: T?) {
    itemPosition = position
    item = data
  }
}