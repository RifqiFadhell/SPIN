package com.spin.id.utils.base

interface BasePresenterPullToRefresh {

  fun loadFromTheFirstTime()

  fun loadNextPage()

  fun loadFormPullToRefresh()

  fun unbind()

  fun bind()
}