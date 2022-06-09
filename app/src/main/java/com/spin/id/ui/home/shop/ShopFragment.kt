package com.spin.id.ui.home.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.spin.id.R
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.order.orderlist.OrderCounts
import com.spin.id.api.response.shop.Category
import com.spin.id.api.response.shop.CategoryResponse
import com.spin.id.api.response.shop.DataProduct
import com.spin.id.api.response.shop.ProductCategoryResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DYNAMIC_CONFIG
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LAST_SELECTED_CAPTION
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.HomeActivity
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.showShortToast
import com.spin.id.utils.extensions.toGone
import com.spin.id.utils.extensions.toVisible
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.shop_fragment.*
import org.json.JSONException
import org.json.JSONObject

class ShopFragment : BaseFragment(), ShopContract.ShopView {

    private var presenter: ShopPresenter? = null

    private var itemListCategory = ArrayList<Category>()
    private var itemListProduct = ArrayList<DataProduct>()

    private lateinit var adapterCategory: CategoryAdapter
    private lateinit var adapterProduct: ProductAdapter

    private var tracker: AmplitudeClient? = null
    private var categorySelected = ""

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.shop_fragment, container, false)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.SHOP

    override fun init(bundle: Bundle?) {
        setupEmptyLayout()
        presenter = ShopPresenter()
        tracker = Amplitude.getInstance()
        tracking(AnalyticsTrackerConstant.PARAM_SHOP_LOAD)
        adapterCategory =
            CategoryAdapter(requireContext(), itemListCategory) { pos: Int, item: Category ->
                for (index in itemListCategory.indices) {
                    itemListCategory[index].isChoosen = pos == index
                }
                adapterCategory.notifyDataSetChanged()
                setProduct(item.id)
                categorySelected = item.category_name
                val eventProperties = JSONObject()
                try {
                    eventProperties.put("Category Name", item.category_name)
                } catch (exception: JSONException) {
                }
                tracking(AnalyticsTrackerConstant.PARAM_SELECT_CATEGORY_SHOP, eventProperties)
            }
        adapterProduct =
            ProductAdapter(requireContext(), itemListProduct) { pos: Int, item: DataProduct ->
                setDataConfig(item)
                val eventProperties = JSONObject()
                try {
                    eventProperties.put("Category Name", categorySelected)
                    eventProperties.put("Subcategory Name", item.subcategoryName)
                } catch (exception: JSONException) {
                }
                tracking(AnalyticsTrackerConstant.PARAM_SELECT_PRODUCT_SHOP, eventProperties)
                val b = Bundle()
                item.id?.let { b.putInt(ItemDetailActivity.PRODUCT_ID, it) }
                b.putString(ItemDetailActivity.PRODUCT_NAME, item.subcategoryName)
                b.putString(ItemDetailActivity.CATEGORY_PRODUCT_NAME, categorySelected)
                activity?.goToActivity(ItemDetailActivity::class.java, b)
            }

        listCategory?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        listCategory?.adapter = adapterCategory

        listProduct?.layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        listProduct?.adapter = adapterProduct
    }

    override fun initData(bundle: Bundle?) {
        presenter?.getCategoryProduct(this)
        presenter?.getTotalActiveOrder(this, getUserId())
        TinyDB(requireContext()).remove(TINY_LAST_SELECTED_CAPTION)
    }

    override fun initListener(bundle: Bundle?) {
        swipeRefresh?.setOnRefreshListener {
            presenter?.getCategoryProduct(this)
        }
    }

    override fun getDataCategory(result: CategoryResponse) {
        val data = result.data
        itemListCategory.clear()
        itemListCategory.addAll(data)
        itemListCategory[0].isChoosen = true
        adapterCategory.notifyDataSetChanged()

        val category = data[0].id
        categorySelected = data[0].category_name
        setProduct(category)
    }

    override fun getDataProduct(result: ProductCategoryResponse) {
        swipeRefresh.isRefreshing = false
        val data = result.data
        itemListProduct.clear()
        if (data != null && data.isNotEmpty()) {
            listProduct.toVisible()
            emptyLayout.toGone()
            itemListProduct.addAll(data)
        } else {
            listProduct.toGone()
            emptyLayout.toVisible()
        }
        adapterProduct.notifyDataSetChanged()
    }

    override fun getTotalActiveOrder(result: OrderCounts) {
        val countActiveOrder = result.unpaid_orders + result.waiting_orders + result.process_orders
        (activity as HomeActivity).counterOrder = countActiveOrder
    }

    private fun setupEmptyLayout() {
        textTitle?.text = "Produk Tidak Tersedia"
        textSubtitle?.text = "Maaf produk yang Anda cari belum \u2028tersedia di SPIN Shop"
        buttonRefreshError?.toGone()
    }

    private fun setDataConfig(item: DataProduct) {
        val tinyDb = TinyDB(requireContext())
        tinyDb.putString(TINY_LAST_SELECTED_CAPTION, item.configForm?.caption)
        tinyDb.putListObject(
            TINY_DYNAMIC_CONFIG,
            item.configForm?.configForm as java.util.ArrayList<Any>
        )
        tinyDb.putInt(TinyConstant.TINY_LAST_SELECTED_PRODUCT_ID, item.id ?: 0)
        tinyDb.putString(TinyConstant.TINY_LAST_SELECTED_PRODUCT_NAME, item.subcategoryName)
    }

    override fun showError(throwable: Throwable) {
        context?.showShortToast(throwable.message.toString())
    }

    private fun setProduct(category: String) {
        presenter?.getProduct(this, category)
    }

    private fun tracking(params: String, properti: JSONObject? = null) {
        if (properti == null) {
            tracker?.logEvent(params)
        } else {
            tracker?.logEvent(params, properti)
        }
    }

    private fun getUserId(): String {
        val data = TinyDB(requireContext()).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        return if (data != null) {
            val userId = data.userId
            if (userId != null) userId
            else ""
        } else ""
    }
}