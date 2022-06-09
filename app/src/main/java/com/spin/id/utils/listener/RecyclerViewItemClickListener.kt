package com.spin.id.utils.listener

import androidx.annotation.IdRes


interface RecyclerViewItemClickListener<in T> {
  fun itemClick(position: Int, item: T?, @IdRes viewId: Int)
}