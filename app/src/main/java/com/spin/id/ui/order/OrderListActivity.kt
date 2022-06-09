package com.spin.id.ui.order

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.spin.id.R
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.order.orderlist.Data
import com.spin.id.api.response.order.orderlist.OrderData
import com.spin.id.api.response.order.orderlist.OrderResponse
import com.spin.id.api.response.shop.Category
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.shop.CategoryAdapter
import com.spin.id.ui.order.detail.OrderDetailActivity
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.showShortToast
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import kotlin.collections.ArrayList

class OrderListActivity : BaseActivity(), OrderContract.OrderListView {
    private var presenter: OrderPresenter? = null

    private var itemListCategory = ArrayList<Category>()
    private var itemListOrder = ArrayList<OrderData>()

    private lateinit var adapterCategory: CategoryAdapter
    private lateinit var adapterOrder: OrderListAdapter

    private var tinyDB: TinyDB? = null
    override fun provideLayout() {
        setContentView(R.layout.activity_order_list)
    }

    override fun init(bundle: Bundle?) {
        setupEmptyLayout()
        presenter = OrderPresenter()
        tinyDB = TinyDB(this)
        adapterCategory =
            CategoryAdapter(this, itemListCategory) { pos: Int, item: Category ->
                for (index in itemListCategory.indices) {
                    itemListCategory[index].isChoosen = pos == index
                }
                adapterCategory.notifyDataSetChanged()
                setupOrderList(item.id)
            }
        adapterOrder = OrderListAdapter(this, itemListOrder) { pos: Int, item: OrderData ->
            val b = Bundle()
            b.putString(OrderDetailActivity.ORDER_ID, item.order_id)
            goToActivity(OrderDetailActivity::class.java, b)
        }

        textTitleToolbar?.text = getString(R.string.history_title_toolbar)
        listStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        listStatus?.adapter = adapterCategory

        listOrder?.layoutManager =
            LinearLayoutManager(this)
        listOrder?.adapter = adapterOrder
    }

    override fun initData(bundle: Bundle?) {
        presenter?.getHistoryOrder(this, getUserId())
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener { finish() }
        swipeRefresh?.setOnRefreshListener { initData(bundle) }
    }

    override fun getScreenName(): String? {
        return ""
    }

    private fun getUserId(): String {
        val data = TinyDB(this).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        return if (data != null) {
            val userId = data.userId
            if (userId != null) userId
            else ""
        } else ""
    }

    override fun getDataOrderList(result: OrderResponse) {
        val data = result.data
        tinyDB?.remove(TinyConstant.TINY_ORDER_HISTORY)
        tinyDB?.putObject(TinyConstant.TINY_ORDER_HISTORY, data)

        val countPendingPayment = data.counts.unpaid_orders
        val countProcessPayment = data.counts.waiting_orders + data.counts.process_orders
        val countCompletedPayment = data.counts.completed_orders + data.counts.cancelled_orders

        setupStatusList(countPendingPayment, countProcessPayment, countCompletedPayment)
        setupOrderList("Unpaid Orders")
    }

    override fun getErrorOrderList(throwable: Throwable) {
        showShortToast(throwable.message.toString())
    }

    override fun showLoading() {
        swipeRefresh?.isRefreshing = true
    }

    override fun hideLoading() {
        swipeRefresh?.isRefreshing = false
    }

    private fun setupOrderList(orderStatus: String) {
        val data = tinyDB?.getObject(TinyConstant.TINY_ORDER_HISTORY, Data::class.java)
        val dataOrder = data?.orders
        Log.e("Order_LIST", Gson().toJson(dataOrder))
        itemListOrder.clear()
        dataOrder?.filter { it.flag_status.equals(orderStatus) }?.forEach { orderList ->
            itemListOrder.add(orderList)
        }
        if (itemListOrder.isEmpty()) {
            emptyLayout.toVisible()
            listOrder.toGone()
        } else {
            listOrder.toVisible()
            emptyLayout.toGone()
        }
        Collections.reverse(itemListOrder)
        adapterOrder.notifyDataSetChanged()
    }

    private fun setupEmptyLayout() {
        textTitle?.text = "Belum Memiliki Order"
        textSubtitle?.text = "Opps ! Maaf untuk saat ini Anda belum memiliki order aktif"
        buttonRefreshError?.toGone()
    }

    private fun setupStatusList(pending: Int, process: Int, complete: Int) {
        itemListCategory.clear()
        itemListCategory.add(Category("Menunggu Pembayaran (${pending})", "Unpaid Orders", true))
        itemListCategory.add(Category("Dalam Proses (${process})", "On Process", false))
        itemListCategory.add(Category("Selesai (${complete})", "Completed", false))

        adapterCategory.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        initData(bundle = null)
    }
}