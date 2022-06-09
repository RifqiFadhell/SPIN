package com.spin.id.utils.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spin.id.utils.listener.RecyclerViewItemClickListener

abstract class BaseLoadingAdapter<T>(context: Context) : BaseAdapter<T, BaseHolder<T>>() {

  var isLoading: Boolean = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      LOADING_VIEW ->
        LoadingViewHolder(LayoutInflater.from(parent.context).inflate(layoutLoading(), parent, false), listener)
      else ->
        onCreateMainViewHolder(parent, viewType)
    }

  abstract fun onCreateMainViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<T>

  abstract fun layoutLoading(): Int

  override fun getItemViewType(position: Int) =
    if (isLoading && position == list.size - 1) {
      LOADING_VIEW
    } else {
      ITEM_VIEW
    }


  fun showLoadingFooter() {
    isLoading = true
    notifyItemInserted(itemCount)
  }

  fun hideLoadingFooter() {
    isLoading = false
    notifyItemInserted(itemCount)
  }

  inner class LoadingViewHolder(itemView: View, listener: RecyclerViewItemClickListener<T>) : BaseHolder<T>(listener, itemView) {

    override fun onClick(v: View) {}
  }

  companion object {
    private val LOADING_VIEW = 1
    private val ITEM_VIEW = 2
  }
}