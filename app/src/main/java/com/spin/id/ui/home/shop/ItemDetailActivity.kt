package com.spin.id.ui.home.shop

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.spin.id.R
import com.spin.id.api.response.shop.Category
import com.spin.id.api.response.shop.ProductMaster
import com.spin.id.api.response.shop.ProductMasterResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.home.shop.detail.DetailProductActivity
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.goToActivity
import com.spin.id.utils.extensions.showShortToast
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONException
import org.json.JSONObject

class ItemDetailActivity : BaseActivity(), ShopContract.ItemProductView {
    companion object {
        const val PRODUCT_ID = "PRODUCT_ID"
        const val PRODUCT_NAME = "PRODUCT_NAME"
        const val CATEGORY_PRODUCT_NAME = "CATEGORY_PRODUCT_NAME"
        const val FROM_PAGE = "FROM_PAGE"
    }

    private var productId = 0
    private var productName: String? = ""
    private var categoryProductName: String? = ""
    private var fromPage: String? = ""

    private var presenter: ShopPresenter? = null
    private var progress: LoaderIndicatorHelper? = null

    private var itemList = ArrayList<ProductMaster>()
    private lateinit var adapter: ItemDetailAdapter

    private var itemListCategory = ArrayList<Category>()
    private lateinit var adapterCategory: CategoryAdapter
    private var tinyDB: TinyDB? = null
    private var tracker: AmplitudeClient? = null

    override fun provideLayout() {
        setContentView(R.layout.activity_item_detail)
    }

    override fun init(bundle: Bundle?) {
        presenter = ShopPresenter()
        progress = LoaderIndicatorHelper()
        tinyDB = TinyDB(this)
        tracker = Amplitude.getInstance()
        if (intent != null) {
            productId = intent.getIntExtra(PRODUCT_ID, 0)
            productName = intent.getStringExtra(PRODUCT_NAME)
            categoryProductName = intent.getStringExtra(CATEGORY_PRODUCT_NAME)
            fromPage = intent.getStringExtra(FROM_PAGE)
        }
        adapterCategory = CategoryAdapter(this, itemListCategory) { pos: Int, item: Category ->
            for (index in itemListCategory.indices) {
                itemListCategory[index].isChoosen = pos == index
            }
            adapterCategory.notifyDataSetChanged()
            setProduct(item.id)
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Category Name", categoryProductName)
                eventProperties.put("Subcategory Name", productName)
                eventProperties.put("Sort Type", item.category_name)
            } catch (exception: JSONException) {
            }
            tracking(AnalyticsTrackerConstant.PARAM_SELECT_SORT_PRODUCT_SHOP, eventProperties)
        }
        adapter = ItemDetailAdapter(this, itemList) { pos: Int, item: ProductMaster ->
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Category Name", categoryProductName)
                eventProperties.put("Subcategory Name", productName)
                eventProperties.put("Product Master Name", item.product_name)
            } catch (exception: JSONException) {
            }
            tracking(AnalyticsTrackerConstant.PARAM_SELECT_DETAIL_PRODUCT_SHOP, eventProperties)

            val bundles = Bundle()
            bundles.putInt("id", item.id)
            bundles.putInt("id_selected", item.product_price_id)
            bundles.putString("category_product_selected", categoryProductName)
            bundles.putString("product_name_selected", productName)
            bundles.putString("product_master_name_selected", item.product_name)
            tinyDB?.putInt(TinyConstant.TINY_LAST_SELECTED_ITEM_PRODUCT_ID, item.id)
            goToActivity(DetailProductActivity::class.java, bundles)
        }

        listSort?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        listSort?.adapter = adapterCategory

        listItemDetail?.layoutManager = LinearLayoutManager(this)
        listItemDetail?.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    override fun initData(bundle: Bundle?) {
        textTitleToolbar?.text = "Top up: $productName"

        itemListCategory.clear()
        itemListCategory.add(Category("Terpopuler", "popular", true))
        itemListCategory.add(Category("Termurah", "price", false))
        adapterCategory.notifyDataSetChanged()

        setProduct("popular")
        redirectToDetailProduct()
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener {
            finish()
        }
    }

    override fun getScreenName(): String? {
        return ""
    }

    override fun showLoading() {
        progress?.showDialog(this)
    }

    override fun hideLoading() {
        progress?.dismissDialog()
    }

    override fun getDataItemProduct(result: ProductMasterResponse) {
        val data = result.data
        itemList.clear()
        itemList.addAll(data)
        adapter.notifyDataSetChanged()
    }

    override fun showError(throwable: Throwable) {
        showShortToast(throwable.message.toString())
    }

    private fun setProduct(sort: String) {
        presenter?.getProductItem(this, productId, sort)
    }

    private fun redirectToDetailProduct() {
        if (fromPage.equals("DetailProduct")) {
            val selectedId = tinyDB?.getInt(TinyConstant.TINY_LAST_SELECTED_ITEM_PRODUCT_ID) ?: 0
            val b = Bundle()
            b.putInt("id", selectedId)
            b.putBoolean("from_detail", true)
            goToActivity(DetailProductActivity::class.java, b)
        }
    }

    private fun tracking(params: String, properti: JSONObject? = null) {
        if (properti == null) {
            tracker?.logEvent(params)
        } else {
            tracker?.logEvent(params, properti)
        }
    }
}