package com.spin.id.utils.base

interface BaseViewPullToRefresh {
  fun showLoading()

  fun hideLoading()

  fun showRefreshLoading()

  fun hideRefreshLoading()

  fun showFooterLoading()

  fun hideFooterLoading()

  fun showEmptyState()

  fun hideErrorState()

  fun clearAllList()

  fun showNetworkError()

  fun showServerError(errorMessage: String)

  fun clearOnScrollListener()
}