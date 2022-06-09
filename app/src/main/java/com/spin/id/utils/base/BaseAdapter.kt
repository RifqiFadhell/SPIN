package com.spin.id.utils.base

import androidx.recyclerview.widget.RecyclerView
import com.spin.id.utils.listener.RecyclerViewItemClickListener


abstract class BaseAdapter<S, T : BaseHolder<S>>: RecyclerView.Adapter<T>() {
  lateinit var listener: RecyclerViewItemClickListener<S>
  var list: ArrayList<S> = ArrayList()

  fun setItemClickListener(listener: RecyclerViewItemClickListener<S>) {
    this.listener = listener
  }

  override fun onBindViewHolder(holder: T, position: Int) {
    val data = getItem(position)
    holder.bindData(position, data)
    bindViewHolder(holder, data)
  }

  fun add(data: S) {
    list.add(data)
    notifyItemInserted(list.size)
  }

  fun addAll(dataList: ArrayList<S>) {
    for (item: S in dataList) {
      add(item)
    }
  }

  fun clear() {
    list.clear()
    notifyDataSetChanged()
  }

  fun getItem(position: Int) =
    if (position < list.size) {
      list[position]
    } else {
      null
    }

  override fun getItemCount() = list.size

  protected abstract fun bindViewHolder(holder: T, data: S?)
}