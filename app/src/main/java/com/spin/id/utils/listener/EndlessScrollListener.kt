package com.spin.id.utils.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class EndlessScrollListener(private val mLinearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

  private var previousTotal = 0 // The total number of items in the dataset after the last load
  private var loading = true // True if we are still waiting for the last set of data to load.
  private val visibleThreshold = 1 // The minimum amount of items to have below your current scroll position before loading more.
  private var firstVisibleItem: Int = 0
  private var visibleItemCount: Int = 0
  private var totalItemCount: Int = 0

  private var currentPage = 2

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    super.onScrolled(recyclerView, dx, dy)

    visibleItemCount = recyclerView.childCount
    totalItemCount = mLinearLayoutManager.itemCount
    firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()

    if (loading) {
      if (totalItemCount > previousTotal) {
        loading = false
        previousTotal = totalItemCount
      }
    }
    if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
      onLoadMore(currentPage)
      currentPage++
      loading = true
    }
  }

  abstract fun onLoadMore(current_page: Int)

  companion object {

    val TAG = EndlessScrollListener::class.java.simpleName
  }

}