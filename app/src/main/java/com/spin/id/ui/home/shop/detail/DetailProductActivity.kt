package com.spin.id.ui.home.shop.detail

import android.graphics.Paint
import android.os.Bundle
import android.text.InputFilter
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.shop.buy.CustomerData
import com.spin.id.api.request.shop.buy.PaymentRequest
import com.spin.id.api.request.shop.detail.DetailProductRequest
import com.spin.id.api.request.shop.discount.DiscountRequest
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.shop.ConfigFormItem
import com.spin.id.api.response.shop.buy.BuyData
import com.spin.id.api.response.shop.buy.BuyResponse
import com.spin.id.api.response.shop.detail.DetailProductResponse
import com.spin.id.api.response.shop.detail.JsonMemberPackageItem
import com.spin.id.api.response.shop.detail.ProductsItem
import com.spin.id.api.response.shop.discount.DiscountResponse
import com.spin.id.api.response.shop.payment.DataPaymentItem
import com.spin.id.api.response.shop.payment.PaymentItem
import com.spin.id.api.response.shop.payment.PaymentMethodResponse
import com.spin.id.api.response.validation.CheckUserResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DEEP_LINK_VALUE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DYNAMIC_CONFIG
import com.spin.id.preference.tinyDb.TinyConstant.TINY_LAST_SELECTED_LIST
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.shop.detail.adapter.MerchantAdapter
import com.spin.id.ui.home.shop.detail.adapter.PackageAdapter
import com.spin.id.ui.home.shop.detail.adapter.PaymentMethodAdapter
import com.spin.id.ui.home.shop.payment.PaymentOVOActivity
import com.spin.id.ui.home.shop.payment.PaymentVAActivity
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.RegexFilter
import com.spin.id.utils.UnitFilter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.detail_product_activity.*
import kotlinx.android.synthetic.main.error_layout_shop.*
import kotlinx.android.synthetic.main.payment_selection_dialog.*
import kotlinx.android.synthetic.main.shop_navigation_layout.*
import kotlinx.android.synthetic.main.shop_toolbar.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class DetailProductActivity : BaseActivity(), DetailProductContract.View {

    private val fieldInputs = mutableListOf<FieldInput>()
    private val fieldInputRender = mutableListOf<ConfigFormItem>()

    private var adapterPackage: PackageAdapter? = null
    private var adapterMerchant: MerchantAdapter? = null
    private var adapterPaymentMethod: PaymentMethodAdapter? = null

    private var presenter: DetailProductPresenter? = null

    private var listPackages = ArrayList<JsonMemberPackageItem>()
    private var listMerchant = ArrayList<ProductsItem>()
    private var listPaymentMethod = ArrayList<PaymentItem>()

    private var progress: LoaderIndicatorHelper? = null
    private var tinyDB: TinyDB? = null
    private lateinit var dialogPaymentMethod: AlertDialog

    private var id: Int? = 0
    private var idSelected: Int? = 0
    private var fromDetail: Boolean? = null
    private var price: Int? = 0
    private var finalPrice: Int? = 0
    private var priceDiscount: Int? = 0
    private var product: String = ""
    private var titleCoupon: String = ""
    private var token: String = ""
    private var status = false
    private var errorCoupon = false

    private var categoryName: String? = ""
    private var productName: String? = ""
    private var productMasterName: String? = ""
    private var packageNameSelected: String? = ""

    private var productPriceId: String? = ""
    private var merchantId: String? = ""
    private var channelCode: String? = ""
    private var channelCategory: String? = ""
    private var discountType: String? = ""

    private var firstName: String = ""
    private var lastName: String = ""
    private var email: String = ""
    private var mobile: String = ""
    private var code: Int = 0

    private var tracker: AmplitudeClient? = null
    private lateinit var dialog: AlertDialog

    override fun provideLayout() {
        setContentView(R.layout.detail_product_activity)
    }

    override fun init(bundle: Bundle?) {
        progress = LoaderIndicatorHelper()
        presenter = DetailProductPresenter()
        tracker = Amplitude.getInstance()
        tinyDB = TinyDB(this)
        if (intent != null) {
            id = if (!tinyDB?.getString(TINY_DEEP_LINK_VALUE).isNullOrEmpty()) {
                tinyDB?.getString(TINY_DEEP_LINK_VALUE)?.toInt()
            } else {
                intent.getIntExtra("id", 0)
            }
            idSelected = intent.getIntExtra("id_selected", 0)
            categoryName = intent.getStringExtra("category_product_selected")
            productName = intent.getStringExtra("product_name_selected")
            productMasterName = intent.getStringExtra("product_master_name_selected")
            fromDetail = intent.getBooleanExtra("from_detail", false)
        }
        initDialogPaymentMethod()
        detailPackage()
        setDialogError()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                showOkDialog("Fetching FCM registration token failed", "Oke", null)
                return@OnCompleteListener
            }
            token = task.result
        })
    }

    override fun initData(bundle: Bundle?) {
        status = tinyDB?.getBoolean(TinyConstant.TINY_STATUS_LOGIN) != false
        if (status) {
            groupLogin.toGone()
            groupFormLogin.toVisible()
            initContact()
        } else {
            groupLogin.toVisible()
            groupFormLogin.toVisible()
        }
        initAdapterPackage()
        editCoupon.filters += InputFilter.AllCaps()
    }

    override fun initListener(bundle: Bundle?) {
        buttonSubmit.setOnClickListener { onUserSubmitSection() }
        buttonBack.setOnClickListener { onBackPressed() }
        buttonApply.setOnClickListener { checkDiscount() }
        buttonCancelCoupon.setOnClickListener { cancelCoupon() }
        imageDropDown.setOnClickListener {
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Category Name", categoryName)
                eventProperties.put("Subcategory Name", productName)
                eventProperties.put("Product Master Name", productMasterName)
                eventProperties.put("Package Name", packageNameSelected)
            } catch (exception: JSONException) {
            }
            tracking(
                AnalyticsTrackerConstant.PARAM_SELECT_MERCHANT_DETAIL_PRODUCT_SHOP,
                eventProperties
            )
            spinnerShop.performClick()
        }
        imageDropDownPayment.setOnClickListener { showDialogPaymentMethod() }
        textPaymentValue.setOnClickListener { showDialogPaymentMethod() }
        textLogin?.setOnClickListener { redirectToBoarding() }
        backgroundLogin?.setOnClickListener { redirectToBoarding() }
        buttonDismissSnack.setOnClickListener { showSnakeBar(titleCoupon, 0) }
        scroll.setOnClickListener { hideKeyboard() }
        layoutSecondParent.setOnClickListener { hideKeyboard() }
        formContainer.setOnClickListener { hideKeyboard() }

        editNumber.setOnFocusChangeListener { _, _ ->
            editNumber.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateNumber(it.editable.toString())
                    hideKeyboard()
                }
        }
        editFirstName.setOnFocusChangeListener { _, _ ->
            editFirstName.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateFirstName(it.editable.toString())
                }
        }
        editLastName.setOnFocusChangeListener { _, _ ->
            editLastName.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateLastName(it.editable.toString())
                }
        }
        editEmail.setOnFocusChangeListener { _, _ ->
            editEmail.afterTextChangeEvents()
                .skip(1)
                .debounce(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateEmail(it.editable.toString())
                }
        }
    }

    override fun getScreenName() = "Detail Product"

    private fun initContact() {
        val profile = tinyDB?.getObject(TINY_PROFILE, LoginData::class.java)
        editFirstName?.setText(profile?.firstName)
        editLastName?.setText(profile?.lastName)
        editNumber?.setText(profile?.userNumber)
        editEmail?.setText(profile?.userEmail)
    }

    private fun setDialogError() {
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.error_layout_shop)
            .setCancelable(false)
        dialog = progressDialog.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun validateFirstName(name: String) {
        when {
            name.isEmpty() -> {
                textInputFirstName.error = getString(R.string.register_name_empty_error)
            }
            name.matches(".*[!@#\$%^&*+=?-].*".toRegex()) -> {
                textInputFirstName.error = getString(R.string.register_name_invalid_error)
            }
            name.length < 3 -> {
                textInputFirstName.error = getString(R.string.register_name_min_error)
            }
            else -> {
                textInputFirstName.error = null
                firstName = name
            }
        }
        checkAllValidation()
    }

    private fun validateLastName(name: String) {
        when {
            name.isEmpty() -> {
                textInputLastName.error = getString(R.string.register_name_empty_error)
            }
            name.matches(".*[!@#\$%^&*+=?-].*".toRegex()) -> {
                textInputLastName.error = getString(R.string.register_name_invalid_error)
            }
            name.length < 3 -> {
                textInputLastName.error = getString(R.string.register_name_min_error)
            }
            else -> {
                textInputLastName.error = null
                lastName = name
            }
        }
        checkAllValidation()
    }

    private fun validateNumber(number: String) {
        when {
            number.isEmpty() -> {
                textInputNumber.error = getString(R.string.register_number_empty_error)
            }
            number.length < 9 -> {
                textInputNumber.error = getString(R.string.register_number_min_error)
            }
            else -> {
                textInputNumber.error = null
                code = 1
                mobile = number
                checkUser(number)
            }
        }
    }

    private fun validateEmail(value: String) {
        when {
            value.isEmpty() -> {
                textInputEmail.error = getString(R.string.register_email_empty_error)
                hideKeyboard()
            }
            !RegexFilter.isEmailValid(value) -> {
                textInputEmail.error = getString(R.string.register_email_error)
                hideKeyboard()
            }
            else -> {
                textInputEmail.error = null
                code = 2
                email = value
                checkUser(value)
            }
        }
    }

    private fun checkUser(value: String) {
        buttonSubmit.toDisable()
        val request = CheckUserRequest()
        when (code) {
            1 -> {
                request.mobile = value
            }
            2 -> {
                request.email = value
            }
        }
        presenter?.checkValidation(this, request)
    }

    override fun getDataValidation(result: CheckUserResponse) {
        when (code) {
            1 -> {
                if (result.status == 401) {
                    textInputNumber.error =
                        getString(R.string.register_number_was_available_error)
                } else {
                    textInputNumber.error = null
                    editNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(this, R.drawable.ic_verified),
                        null
                    )
                }
            }
            2 -> {
                if (result.status == 401) {
                    textInputEmail.error =
                        getString(R.string.register_email_was_available_error)
                    hideKeyboard()
                } else {
                    textInputEmail.error = null
                    editEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(this, R.drawable.ic_verified),
                        null
                    )
                    hideKeyboard()
                }
            }
        }
    }

    private fun checkAllValidation() {
        if (textInputFirstName.error == null && editFirstName.editableText.toString().isNotEmpty()
            && textInputLastName.error == null && editLastName.editableText.toString().isNotEmpty()
            && textInputNumber.error == null && editNumber.editableText.toString().isNotEmpty()
            && editEmail.editableText.toString().isNotEmpty() && textInputEmail.error == null
            && !errorCoupon && textPaymentValue.text != getString(R.string.select_label)
        ) {
            enableButton()
        } else {
            disableButton()
        }
    }

    private fun initAdapterPackage() {
        adapterPackage = PackageAdapter(
            this,
            listPackages,
            onItemClick = { pos: Int, item: JsonMemberPackageItem ->
                setDataPackage(item)
                packageNameSelected = item.packageName
                val eventProperties = JSONObject()
                try {
                    eventProperties.put("Category Name", categoryName)
                    eventProperties.put("Subcategory Name", productName)
                    eventProperties.put("Product Master Name", productMasterName)
                    eventProperties.put("Package Name", packageNameSelected)
                } catch (exception: JSONException) {
                }
                tracking(
                    AnalyticsTrackerConstant.PARAM_SELECT_PACKAGE_DETAIL_PRODUCT_SHOP,
                    eventProperties
                )
            })
        listPackage?.layoutManager = GridLayoutManager(this, 3)
        listPackage?.adapter = adapterPackage
    }

    private fun initAdapterPayment() {
        adapterPaymentMethod = PaymentMethodAdapter(
            this,
            listPaymentMethod,
            onItemClick = { pos: Int, item: PaymentItem ->
                layoutPayment.setBackgroundResource(R.drawable.background_stroke_12)
                textPaymentValue.text = item.name
                channelCode = item.channelCode
                channelCategory = item.channelCategory
                if (!errorCoupon) {
                    if (!status) {
                        checkAllValidation()
                    }
                }
                dialogPaymentMethod.dismiss()
            })
        dialogPaymentMethod.listPayment.layoutManager = LinearLayoutManager(this)
        dialogPaymentMethod.listPayment.adapter = adapterPaymentMethod
    }

    private fun showDialogPaymentMethod() {
        dialogPaymentMethod.show()
        initAdapterPayment()
        adapterPaymentMethod?.notifyDataSetChanged()
        dialogPaymentMethod.buttonDismiss.setOnClickListener { dialogPaymentMethod.hide() }
    }

    private fun detailPackage() {
        showProgress()
        val request = DetailProductRequest(id.toString(), idSelected.toString())
        presenter?.detailProduct(this, request)
        presenter?.getPaymentMethod(this)
    }

    private fun initDialogPaymentMethod() {
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.payment_selection_dialog)
            .setCancelable(false)
        dialogPaymentMethod = progressDialog.create()
    }

    private fun checkDiscount() {
        val coupon = editCoupon.editableText.toString()
        if (coupon.isNotEmpty()) {
            titleCoupon = coupon
            textInputCoupon.error = null
            val request = DiscountRequest(price.toString(), coupon)
            presenter?.discount(this, request)
        } else {
            textInputCoupon.error = getString(R.string.shop_error_coupon)
        }
    }

    override fun getDataDiscount(result: DiscountResponse) {
        if (result.status == 200) {
            errorCoupon = false
            if (textPaymentValue.text != getString(R.string.select_label)) {
                if (!status) {
                    checkAllValidation()
                } else {
                    enableButton()
                }
            }
            textInputCoupon.error = null
            groupInputCoupon.toInvisible()
            groupCoupon.toVisible()
            groupPriceCoupon.toVisible()
            priceDiscount = result.data?.discountAmount
            textTitleItemCoupon.text = getString(
                R.string.shop_title_success_coupon,
                UnitFilter.convertToRupiah(result.data?.discountAmount)
            )
            showSnakeBar(titleCoupon, 1)
            textSubCoupon.text = getString(R.string.shop_caption_success_coupon, titleCoupon)
            textCouponUse.text = titleCoupon
            textPriceCoupon.text = UnitFilter.convertToRupiah(priceDiscount)
            validateFinalDiscount()
        } else {
            showSnakeBar(titleCoupon, 3)
        }
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Category Name", categoryName)
            eventProperties.put("Subcategory Name", productName)
            eventProperties.put("Product Master Name", productMasterName)
            eventProperties.put("Coupon Code", titleCoupon)
            eventProperties.put("Coupon Attempt Status", result.message)
        } catch (exception: JSONException) {
        }
        tracking(AnalyticsTrackerConstant.PARAM_APPLY_COUPON_CLICKED, eventProperties)
    }

    private fun showSnakeBar(message: String, code: Int) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 500
        transition.addTarget(layoutSnack)
        TransitionManager.beginDelayedTransition(layoutParent, transition)
        when (code) {
            1 -> {
                layoutSnack.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.jungleGreen
                    )
                )
                textCaptionSnack.text =
                    getString(R.string.shop_caption_apply_coupon_snack, message)
                layoutSnack.visibility = View.VISIBLE
            }
            2 -> {
                layoutSnack.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.jungleGreen
                    )
                )
                textCaptionSnack.text =
                    getString(R.string.shop_caption_cancel_coupon_snack, message)
                layoutSnack.visibility = View.VISIBLE
            }
            3 -> {
                layoutSnack.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.burntSienna
                    )
                )
                textCaptionSnack.text =
                    getString(R.string.shop_caption_invalid_coupon_snack, message)
                layoutSnack.visibility = View.VISIBLE
                errorCoupon = true
            }
            0 -> {
                layoutSnack.visibility = View.GONE
            }
        }
    }

    private fun cancelCoupon() {
        showSnakeBar(titleCoupon, 2)
        priceDiscount = 0
        groupCoupon.toGone()
        groupPriceCoupon.toGone()
        groupInputCoupon.toVisible()
        editCoupon.setText("")
        validateFinalDiscount()
    }

    private fun redirectToBoarding() {
        val b = Bundle()
        b.putString(BoardingActivity.FROM_PAGE, "DetailProduct")
        val finalAnswer = mutableListOf<ConfigFormItem>()
        if (fieldInputs.isNotEmpty()) {
            fieldInputs.forEach {
                finalAnswer.add(
                    ConfigFormItem(
                        it.key,
                        it.text,
                        it.type,
                        it.getValue(),
                        it.required
                    )
                )
            }
            tinyDB?.putListObject(TINY_LAST_SELECTED_LIST, finalAnswer as java.util.ArrayList<Any>)
        }
        goToActivity(BoardingActivity::class.java, b)
    }

    override fun getDataDetail(result: DetailProductResponse) {
        if (result.status == 200) {
            if (fromDetail == true) {
                fieldInputRender.clear()
                val listFrom = tinyDB?.getListObject(
                    TINY_LAST_SELECTED_LIST,
                    ConfigFormItem::class.java
                ) as ArrayList<ConfigFormItem>
                if (!listFrom.isNullOrEmpty()) {
                    fieldInputRender.plusAssign(listFrom)
                    reRenderFields()
                } else {
                    renderFields()
                }
            } else {
                renderFields()
            }
            product = result.data?.product?.productName.toString()
            textDiamond.text = product
            textTitleValue.text = product
            textSubValue.text = result.data?.product?.subcategory
            image.loadImage(
                this,
                result.data?.product?.currencyImage,
                R.drawable.ic_place_holder
            )
            val data = result.data?.jsonMemberPackage
            if (data != null && data.isNotEmpty()) {
                listPackages.clear()
                listPackages.addAll(data)
                adapterPackage?.notifyDataSetChanged()
                setDataPackage(data[0])
            }
        }
    }

    override fun getDataPaymentMethod(result: PaymentMethodResponse) {
        if (result.status == 200) {
            listPaymentMethod.clear()
            var list = ArrayList<DataPaymentItem>()
            val listFinal = ArrayList<PaymentItem>()
            if (result.data != null) list = result.data as ArrayList<DataPaymentItem>
            for (x in 0 until list.size) {
                if (!list[x].virtualAccount.isNullOrEmpty()) {
                    val item = PaymentItem(
                        0, "Virtual Account", "", "", "", "", ""
                    )
                    listFinal.plusAssign(item)
                    list[x].virtualAccount?.let { listFinal.plusAssign(it) }

                }
                if (!list[x].retailOutlet.isNullOrEmpty()) {
                    val item = PaymentItem(
                        0, "Retail Outlet", "", "", "", "", ""
                    )
                    listFinal.plusAssign(item)
                    list[x].retailOutlet?.let { listFinal.plusAssign(it) }

                }
                if (!list[x].ewallet.isNullOrEmpty()) {
                    val item = PaymentItem(
                        0, "E-Wallet", "", "", "", "", ""
                    )
                    listFinal.plusAssign(item)
                    list[x].ewallet?.let { listFinal.plusAssign(it) }

                }
                if (!list[x].creditCard.isNullOrEmpty()) {
                    val item = PaymentItem(
                        0, "Credit Card", "", "", "", "", ""
                    )
                    listFinal.plusAssign(item)
                    list[x].creditCard?.let { listFinal.plusAssign(it) }

                }
                if (!list[x].qris.isNullOrEmpty()) {
                    val item = PaymentItem(
                        0, "QRIS", "", "", "", "", ""
                    )
                    listFinal.plusAssign(item)
                    list[x].qris?.let { listFinal.plusAssign(it) }
                }
            }
            listPaymentMethod.plusAssign(listFinal)
        }
    }

    private fun setDataPackage(data: JsonMemberPackageItem) {
        textTime.text = data.packageTitle
        textCaptionTime.text = data.packageDescription

        if (!data.refundTitle.isNullOrEmpty()) {
            groupInputRefund.toVisible()
            textRefund.text = data.refundTitle
            textCaptionRefund.text = data.refundDescription
            imageVerified.setImageResource(R.drawable.ic_verified_green)
        } else {
            groupInputRefund.toGone()
        }

        if (data.products != null && data.products.isNotEmpty()) {
            initSpinnerMerchant(data.products as ArrayList<ProductsItem>)
        }
    }

    private fun initSpinnerMerchant(data: ArrayList<ProductsItem>) {
        listMerchant.clear()
        listMerchant.plusAssign(data)
        adapterMerchant = MerchantAdapter(this, listMerchant)
        spinnerShop.adapter = adapterMerchant
        spinnerMerchantListener()
    }

    private fun spinnerMerchantListener() {
        spinnerShop.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var prev = spinnerShop.selectedItemPosition
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val eventProperties = JSONObject()
                try {
                    eventProperties.put("Category Name", categoryName)
                    eventProperties.put("Subcategory Name", productName)
                    eventProperties.put("Product Master Name", productMasterName)
                    eventProperties.put("Package Name", packageNameSelected)
                    eventProperties.put(
                        "Previous Merchant Name",
                        listMerchant[prev].merchantName.toString()
                    )
                    eventProperties.put(
                        "New Merchant Name",
                        listMerchant[position].merchantName.toString()
                    )
                } catch (exception: JSONException) {
                }
                tracking(
                    AnalyticsTrackerConstant.PARAM_SELECT_MERCHANT_APPLIED_DETAIL_PRODUCT_SHOP,
                    eventProperties
                )
                prev = position
                price = listMerchant[position].price
                productPriceId = listMerchant[position].productId.toString()
                merchantId = listMerchant[position].merchantId.toString()
                validateFinalDiscount()
            }
        }
    }

    private fun validateFinalDiscount() {
        finalPrice = if (priceDiscount != 0) {
            price?.minus(priceDiscount!!)
        } else {
            price
        }
        if (priceDiscount == 0) {
            textTitlePriceDiscount.toInvisible()
        } else {
            textTitlePriceDiscount.toVisible()
        }
        textPriceDiamond.text = UnitFilter.convertToRupiah(price)
        textPriceTotal.text = UnitFilter.convertToRupiah(finalPrice)
        textTitlePriceDiscount.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text = UnitFilter.convertToRupiah(price)
        }
        textTitleTotalPrice.text = UnitFilter.convertToRupiah(finalPrice)
    }

    private fun renderFields() {
        textCaptionInformation.text = HtmlCompat.fromHtml(
            tinyDB?.getString(TinyConstant.TINY_LAST_SELECTED_CAPTION).toString(),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        textCaptionInformation.movementMethod = LinkMovementMethod.getInstance()
        val formContainer = findViewById<LinearLayout>(R.id.formContainer)
        val list = tinyDB?.getListObject(
            TINY_DYNAMIC_CONFIG,
            ConfigFormItem::class.java
        ) as ArrayList<ConfigFormItem>

        val fields = ArrayList<ConfigFormItem>()
        fields.plusAssign(list)
        fields.forEach {
            val fieldInput: FieldInput = when (it.type) {
                "text" -> {
                    TextInput(this)
                }
                "number" -> {
                    TextInput(this)
                }
                else -> {
                    throw IllegalStateException("unknown type")
                }
            }

            //we must set a key to each field so we send it back to the api as a map of the key and the value inserted
            fieldInput.apply {
                key = it.name.toString()
                required = it.required!!
                type = it.type
                text = it.placeholder.toString()
                required = it.required
                setHint(it.placeholder.toString())
                setInputType(it.type)
            }

            //Then we add this field to the list of the fields, so we can validate all of them and get their values
            fieldInputs.add(fieldInput)

            //Our view only as a linear layout to hold the fields and place them one on each line
            formContainer.addView(fieldInput as View)
        }
    }

    private fun focusOnViewForm() {
        scroll.post { scroll.smoothScrollTo(0, formContainer.top) }
    }

    private fun focusOnViewPayment() {
        scroll.post { scroll.smoothScrollTo(0, layoutPayment.top) }
    }

    private fun reRenderFields() {
        fieldInputs.clear()
        formContainer.removeAllViews()
        val fields = ArrayList<ConfigFormItem>()
        fields.plusAssign(fieldInputRender)
        fields.forEach {
            val fieldInput: FieldInput = when (it.type) {
                "text" -> {
                    TextInput(this)
                }
                "number" -> {
                    TextInput(this)
                }
                else -> {
                    throw IllegalStateException("unknown type")
                }
            }

            //we must set a key to each field so we send it back to the api as a map of the key and the value inserted
            fieldInput.apply {
                key = it.name.toString()
                required = it.required!!
                type = it.type
                text = it.placeholder.toString()
                required = it.required
                setHint(it.placeholder.toString())
                setInputType(it.type)
                setError(it.value.toString())
            }

            //Then we add this field to the list of the fields, so we can validate all of them and get their values
            fieldInputs.add(fieldInput)

            //Our view only as a linear layout to hold the fields and place them one on each line
            formContainer.addView(fieldInput as View)
        }
    }

    private fun onUserSubmitSection() {

        var statusRepeat = false
        if (fieldInputs.isNotEmpty()) {
            val answers = mutableListOf<Answer>()
            val finalAnswer = mutableListOf<ConfigFormItem>()

            fieldInputs.forEach {
                answers.add(Answer(it.getValue(), it.key))
                finalAnswer.add(
                    ConfigFormItem(
                        it.key,
                        it.text,
                        it.type,
                        it.getValue(),
                        it.required
                    )
                )
            }

            for (x in 0 until finalAnswer.size) {
                if (finalAnswer[x].value.isNullOrEmpty()) {
                    fieldInputRender.clear()
                    fieldInputRender.plusAssign(finalAnswer)
                    reRenderFields()
                    statusRepeat = true
                    focusOnViewForm()
                    break
                }
            }

            if (!statusRepeat) {
                var data: Answer
                val params = HashMap<String, String>()
                for (x in 0 until answers.size) {
                    data = answers[x]
                    params[data.key.toString()] = data.value.toString()
                }

                if (textPaymentValue.text == getString(R.string.select_label)) {
                    layoutPayment.setBackgroundResource(R.drawable.background_stroke_12_red)
                    focusOnViewPayment()
                } else {
                    layoutPayment.setBackgroundResource(R.drawable.background_stroke_12)
                    if (status) {
                        val request = PaymentRequest(
                            getUserId(),
                            token,
                            merchantId,
                            productPriceId,
                            finalPrice.toString(),
                            channelCode,
                            channelCategory,
                            titleCoupon,
                            priceDiscount.toString(),
                            discountType,
                            null, params
                        )
                        presenter?.requestPayment(this, request)
                    } else {
                        val dataUser = CustomerData(firstName, lastName, email, mobile)
                        val request = PaymentRequest(
                            "",
                            token,
                            merchantId,
                            productPriceId,
                            finalPrice.toString(),
                            channelCode,
                            channelCategory,
                            titleCoupon,
                            priceDiscount.toString(),
                            discountType,
                            dataUser, params
                        )
                        presenter?.requestPayment(this, request)
                    }
                }
            }
        }
    }

    override fun getDataPayment(result: BuyResponse) {
        when (result.status) {
            200 -> {
                result.data?.let {
                    val eventProperties = JSONObject()
                    try {
                        eventProperties.put("Category Name", categoryName)
                        eventProperties.put("Subcategory Name", productName)
                        eventProperties.put("Product Master Name", productMasterName)
                        eventProperties.put("Package Name", packageNameSelected)
                    } catch (exception: JSONException) {
                    }
                    tracking(AnalyticsTrackerConstant.PARAM_PURCHASE_CLICKED, eventProperties)
                    redirectToPayment(it)
                }
            }
            420 -> {
                dialog.show()
                dialog.textTitleError?.text = "Batas Minimal Pembelian"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
            422 -> {
                dialog.show()
                dialog.textTitleError?.text = "Akun Tidak Terdaftar"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
            423 -> {
                dialog.show()
                dialog.textTitleError?.text = "Melewati Batas Limit Transaksi"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
            425 -> {
                dialog.show()
                dialog.textTitleError?.text = "Saldo Tidak Cukup"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
            426 -> {
                dialog.show()
                dialog.textTitleError?.text = "Akun Tidak Aktif"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
            else -> {
                dialog.show()
                dialog.textTitleError?.text = "Pembayaran Gagal"
                dialog.textSubtitleError?.text = result.message
                dialog.buttonRefreshError?.text = getString(R.string.ok_label)
                dialog.buttonRefreshError?.setOnClickListener {
                    dialog.hide()
                }
            }
        }
    }

    private fun getUserId(): String {
        val data = TinyDB(this).getObject(
            TinyConstant.TINY_PROFILE, LoginData::class.java
        )
        if (data != null) {
            val userId = data.userId
            if (userId != null) return userId
            else return ""
        } else return ""
    }

    override fun showErrorPayment(throwable: Throwable) {
        val message = throwable.message
        if (message != null && message.contains("500")) {
            dialog.show()
            dialog.textTitleError?.text = "Sistem Sedang Dalam Perbaikan"
            dialog.textSubtitleError?.text =
                "Opps ! Maaf saat ini sistem pembayaran sedang dalam perbaikan. Silahkan mencoba lagi beberapa menit lagi."
            dialog.buttonRefreshError?.text = getString(R.string.ok_label)
            dialog.buttonRefreshError?.setOnClickListener {
                dialog.hide()
            }
        } else {
            showOkDialog(throwable.message.toString(), "Oke", null)
        }
    }

    override fun showProgress() {
        progress?.showDialog(this)
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    private fun enableButton() {
        buttonSubmit.toEnable()
    }

    private fun disableButton() {
        buttonSubmit.toDisable()
    }

    override fun showError(throwable: Throwable) {
        showOkDialog(throwable.message.toString(), "Oke", null)
    }

    private fun redirectToPayment(data: BuyData) {
        val redirectedUrl = data.action
        val channel = data.channelCategory
        if (redirectedUrl.isNullOrEmpty()) {
            if (channel.equals("VIRTUAL_ACCOUNT")) {
                val b = Bundle()
                b.putParcelable(PaymentVAActivity.DATA_PAYMENT, data)
                goToActivity(PaymentVAActivity::class.java, b)
            } else {
                val b = Bundle()
                b.putString(PaymentOVOActivity.ORDER_ID, data.orderId)
                b.putString(PaymentOVOActivity.MESSAGE_TITLE, data.messageTitle)
                b.putString(PaymentOVOActivity.MESSAGE_CONTENT, data.messageContent)
                goToActivity(PaymentOVOActivity::class.java, b)
            }
            finish()
        } else {
            goToWebView(
                redirectedUrl,
                data.messageTitle.toString()
            )
            finish()
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

