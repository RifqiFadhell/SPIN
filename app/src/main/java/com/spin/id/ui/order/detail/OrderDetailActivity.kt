package com.spin.id.ui.order.detail

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.spin.id.R
import com.spin.id.api.response.order.orderdetail.Data
import com.spin.id.api.response.order.orderdetail.Detail
import com.spin.id.api.response.order.orderdetail.OrderDetailResponse
import com.spin.id.api.response.shop.buy.BuyData
import com.spin.id.ui.home.shop.payment.PaymentVAActivity
import com.spin.id.ui.order.OrderContract
import com.spin.id.ui.order.OrderPresenter
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.shop_toolbar.*

class OrderDetailActivity : BaseActivity(), OrderContract.OrderDetailView {
    private var presenter: OrderPresenter? = null
    private lateinit var adapter: ListMetadataAdapter

    private lateinit var data: Data
    private var orderId = ""
    private var progress: LoaderIndicatorHelper? = null
    private var itemList = ArrayList<Detail>()

    companion object {
        const val ORDER_ID = "ORDER_ID"
    }

    override fun provideLayout() {
        setContentView(R.layout.activity_order_detail)
    }

    override fun init(bundle: Bundle?) {
        progress = LoaderIndicatorHelper()
        presenter = OrderPresenter()
        adapter = ListMetadataAdapter(this, itemList)
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            orderId = intent.getStringExtra(ORDER_ID) ?: ""
        }
        presenter?.getDetailOrder(this, orderId)
    }

    override fun initListener(bundle: Bundle?) {
        buttonBack?.setOnClickListener { finish() }
        buttonPaymentInformation?.setOnClickListener { redirectToDetailPayment(data) }
        buttonHelpOrder.setOnClickListener { goToWebView(data.contact_link.toString(), "Bantuan") }
        buttonCancelOrder.setOnClickListener { cancelOrder() }
    }

    override fun getScreenName(): String? {
        return ""
    }

    private fun hideInfoDeadline() {
        informationDeadlineCard?.toGone()
        buttonCancelOrder?.toGone()
    }

    private fun showInfoDeadline() {
        informationDeadlineCard?.toVisible()
        buttonCancelOrder?.toVisible()
    }

    override fun getDataOrder(result: OrderDetailResponse) {
        data = result.data
        data.order.detail.let { itemList.addAll(it) }
        adapter.notifyDataSetChanged()
        setDetailView(data)
    }

    override fun getErrorOrder(throwable: Throwable) {
        showShortToast(throwable.message.toString())
    }

    override fun showLoading() {
        progress?.showDialog(this)
    }

    override fun hideLoading() {
        progress?.dismissDialog()
    }

    @SuppressLint("SetTextI18n")
    private fun setDetailView(data: Data) {
        buttonShare?.toGone()
        textTitleToolbar?.text = data.order.flag_status
        textOrderId?.text = data.order.order_id
        textOrderDate?.text =
            data.order.created_at?.convertDate("yyyy-MM-dd HH:mm:ss", "dd MMMM yyyy - HH:mm")
        if (data.payment.status.equals(
                "Pending",
                true
            ) && data.payment.channel_category.equals("VIRTUAL_ACCOUNT", true)
        ) {
            showInfoDeadline()
        } else {
            hideInfoDeadline()
        }
        textDeadlinePayment?.text = getString(
            R.string.history_complete_payment,
            "${
                data.payment.expiration_date?.convertDate(
                    "yyyy-MM-dd HH:mm:ss",
                    "dd MMMM yyyy - HH:mm"
                )
            }"
        )

        imageLogo?.loadImage(this, data.product.subcategory.currency_img, R.drawable.ic_diamond)
        textOrderName?.text = data.product.name
        textOrderProductName?.text = data.product.subcategory.subcategory_name

        imageStore?.loadImage(this, data.merchant.logo, R.drawable.ic_outlet)
        textOrderMerchantName?.text = data.merchant.name

        textPriceOrder?.text = data.order.gmv?.toInt()?.rupiahFormat()
        if (!data.order.discount.isNullOrEmpty() && data.order.discount != "0") {
            groupCoupon?.toVisible()
            textCouponNameOrder?.text = data.order.coupon_code
            textCouponPriceOrder?.text = data.order.discount.toInt().rupiahFormat()
        } else {
            groupCoupon?.toGone()
        }
        textTotalBayar?.text = data.order.paid?.rupiahFormat()
        textStatusPayment?.text = data.order.flag_status

        listMetadata?.adapter = adapter
        listMetadata?.layoutManager = LinearLayoutManager(this)

        textPackageOrder?.text = "Paket ${data.`package`.package_name}"
        textSubPackageOrder?.text = data.`package`.package_title
        textDescPackageOrder?.text = data.`package`.package_description
        textRefund?.text = data.`package`.refund_title
        textCaptionRefund?.text = data.`package`.refund_description
    }

    private fun redirectToDetailPayment(data: Data) {
        val buyData = BuyData(
            orderId,
            data.payment.channel_code,
            data.payment.expiration_date,
            data.payment.logo,
            data.payment.account_number,
            data.order.paid
        )
        val b = Bundle()
        b.putParcelable(PaymentVAActivity.DATA_PAYMENT, buyData)
        goToActivity(PaymentVAActivity::class.java, b)
    }

    private fun cancelOrder() {
        presenter?.cancelOrder(this, orderId)
    }
}