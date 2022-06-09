package com.spin.id.ui.home.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.amplitude.api.Amplitude
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.spin.id.BuildConfig
import com.spin.id.R
import com.spin.id.api.request.profile.EditUserNameRequest
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.order.orderlist.OrderCounts
import com.spin.id.api.response.profile.EditUserNameResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DAILY_DATE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_DATA_COMMENT_LIKE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.order.OrderListActivity
import com.spin.id.ui.webview.webview.builder.WebviewBuilder
import com.spin.id.ui.webview.webview.constant.AppSourceConstant
import com.spin.id.ui.webview.webview.constant.HttpMethodConstant
import com.spin.id.utils.ImageUtils
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.PermissionUtils
import com.spin.id.utils.PhotoProvider
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PROFILE
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.*
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.edit_bio_dialog.*
import kotlinx.android.synthetic.main.edit_username_dialog.*
import kotlinx.android.synthetic.main.profile_fragment.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class ProfileFragment : BaseFragment(), ProfileContract.View {

    private lateinit var dialog: AlertDialog
    private lateinit var dialogBio: AlertDialog
    private var presenter: ProfilePresenter? = null
    private var tinyDB: TinyDB? = null

    companion object {
        const val INTERVAL_TIME = 1000L
        const val REQ_CODE_CAMERA = 100
        const val REQ_CODE_GALLERY = 200
    }

    private var userChoosenTask: Int? = null
    lateinit var cameraTempUri: Uri
    private var imageBase64: String? = ""
    private var imageName: String? = ""

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun getScreenName() = PROFILE

    override fun init(bundle: Bundle?) {
        presenter = ProfilePresenter()
        tinyDB = TinyDB(requireContext())
        setProfile()
        initDialogUsername()
    }

    override fun initData(bundle: Bundle?) {
        presenter?.getTotalActiveOrder(this, getUserId())
    }

    override fun initListener(bundle: Bundle?) {
        layoutCardLogout.setOnClickListener {
            activity?.showYesNoDialog(
                "Jika log out kamu tidak akan bisa menggunakan fitur like dan komen ?",
                "Ok",
                "Batal",
                DialogInterface.OnClickListener { _, _ -> logout() })
        }
        layoutCardConnection.setOnClickListener {
            goToWebView(
                "https://discord.gg/mwz6P8JDDY",
                "Join Discord"
            )
        }
        layoutFeedback.setOnClickListener {
            goToWebView(
                "https://docs.google.com/forms/d/e/1FAIpQLSebczdyMO92rqZX1nCVtJNkPflbgJhopn04jxc9F_kvXcjuWw/viewform",
                "Beri Masukan"
            )
        }
        layoutInformation.setOnClickListener {
            goToWebView(
                "https://dailyspin.id/about-us-indonesia/",
                "About Us"
            )
        }
        layoutReferral.setOnClickListener {
//            requireActivity().showOkDialog("Coming Soon", "Oke", null)
        }

        layoutCardSupport.setOnClickListener {
            goToWebView(
                "https://instagram.com/spin_esports/",
                "About Us"
            )
        }
        buttonEditProfile.setOnClickListener {
            EditProfileDialog.show(
                childFragmentManager,
                callbackChangePhoto = {
//                    setDialog()
                    requireActivity().showOkDialog("Coming Soon", "Oke", null)
                },
                callbackChangeUsername = {
                    dialog.show()
                },
                callbackChangeBio = {
//                    showDialogChangeBio()
                    requireActivity().showOkDialog("Coming Soon", "Oke", null)
                }
            )
        }
        layout_top?.setOnClickListener {
//            requireActivity().goToActivity(ProfileBioActivity::class.java)
        }
        layoutOrder?.setOnClickListener {
            requireActivity().goToActivity(OrderListActivity::class.java)
        }
    }

    private fun initDialogUsername() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = AlertDialog.Builder(requireContext())
                .setView(R.layout.edit_username_dialog)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.label_batal)) { _, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.label_ganti)) { _, _ -> changeUsername() }
                .create()
        }
    }

    private fun changeUsername() {
        if (dialog.editUserName.editableText.toString().isNotEmpty()) {
            val request = EditUserNameRequest()
            request.user_id =
                tinyDB?.getObject(TINY_PROFILE, LoginData::class.java)?.userId.toString()
            request.username = dialog.editUserName.editableText.toString()
            presenter?.editProfile(this, request, tinyDB?.getString(TINY_TOKEN).toString())
        } else {
            dialog.textInputUserName.error = getString(R.string.register_username_empty_error)
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        requireContext().signOutGoogle()
        requireContext().signOutFacebook()
        val tiny = TinyDB(context)
        tiny.remove(TINY_STATUS_LOGIN)
        tiny.remove(TINY_PROFILE)
        tiny.remove(TINY_DATA_COMMENT_LIKE)
        tiny.remove(TINY_DAILY_DATE)
        tiny.putBoolean(TinyConstant.TINY_STATUS_LOGOUT, true)
        activity?.goToActivity(BoardingActivity::class.java)
        requireActivity().finish()
        val eventProperties = JSONObject()
        try {
            eventProperties.put("Login Status", false)
        } catch (exception: JSONException) {
        }
        Amplitude.getInstance().setUserProperties(eventProperties)
    }

    private fun goToWebView(url: String, title: String) {
        WebviewBuilder(requireContext())
            .setUrl(url)
            .setTitle(title)
            .setVersionCode("0")
            .setWebSettingJavaScriptEnable(true)
            .setHttpMethod(HttpMethodConstant.Value.GET)
            .setAppSource(AppSourceConstant.Value.SPIN_ANDROID)
            .show()
    }

    private fun setProfile() {
        val tinyDB = TinyDB(context).getObject(TINY_PROFILE, LoginData::class.java)
        textBio.text = tinyDB?.displayName
        textUserEmail.text = tinyDB?.userNumber
        textVersion.text = BuildConfig.VERSION_NAME
    }

    private fun setDialog() {
        val items = resources.getStringArray(R.array.choose_photo)
        val builder = AlertDialog.Builder(context)

        builder.setItems(items) { _, item ->
            if (item == 0) {
                cameraIntent()
            } else if (item == 1) {
                galleryIntent()
            }
        }
        builder.show()
    }

    private fun showDialogChangeBio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogBio = AlertDialog.Builder(requireContext())
                .setView(R.layout.edit_bio_dialog)
                .setCancelable(false)
                .create()

            dialogBio.show()
            dialogBio.buttonCancelBio.setOnClickListener {
                dialogBio.dismiss()
            }
            dialogBio.buttonChangeBio.setOnClickListener {
                changeBio()
            }
        }
    }

    private fun changeBio() {
        context?.showShortToast("Change Bio")
        dialogBio.dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChoosenTask == REQ_CODE_CAMERA)
                    cameraIntent()
                else if (userChoosenTask == REQ_CODE_GALLERY)
                    galleryIntent()
            } else {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    val uri = PhotoProvider().getPhotoUri(data!!)
                    Thread(Runnable {
                        imageBase64 = ImageUtils.encodeImage(
                            ImageUtils.convertUriToBitmap(
                                uri,
                                requireContext()
                            )
                        )
                        println("imageBase nih $imageBase64")
                        activity?.contentResolver?.notifyChange(uri, null)
                    }).start()
                    imageName = uri.lastPathSegment
                    imageUser.setImageURI(uri)
//                    enableButtonSave()
                    return
                }
            }
            when (userChoosenTask) {
                REQ_CODE_GALLERY -> {
                    val ucrop =
                        data?.data?.let { ImageUtils.createCropActivity(requireContext(), it) }
                    ucrop?.start(requireContext(), this)
                }
                REQ_CODE_CAMERA -> {
                    val ucrop = ImageUtils.createCropActivity(requireContext(), cameraTempUri)
                    ucrop.start(requireContext(), this)
                }
            }
        }
    }

    private fun cameraIntent() {
        userChoosenTask = REQ_CODE_CAMERA
        val result = PermissionUtils.checkPermission(requireContext())
        if (result) {

            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val file = File(Environment.getExternalStorageDirectory(), "spin")
                if (!file.exists()) {
                    file.mkdir()
                }
                val imageFile = File(
                    file.path, Calendar.getInstance()
                        .timeInMillis.toString() + "" + ".jpg"
                )
                cameraTempUri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + "" + ".provider", imageFile
                )
            } else {
                cameraTempUri = activity?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )!!
            }

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri)
            startActivityForResult(intent, REQ_CODE_CAMERA)
        }
    }

    private fun galleryIntent() {
        userChoosenTask = REQ_CODE_GALLERY
        val result = PermissionUtils.checkPermission(requireContext())
        if (result) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            val intent = Intent()
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.select_file)),
                REQ_CODE_GALLERY
            )
        }
    }

    override fun showProgress() {
        LoaderIndicatorHelper.getInstance().showDialog(requireContext())
    }

    override fun hideProgress() {
        LoaderIndicatorHelper.getInstance().dismissDialog()
    }

    override fun getData(result: EditUserNameResponse) {
        if (result.status == 200) {
            setDataProfile(result.data?.get(0)?.username.toString())
        } else {
            activity?.showErrorDialog()
        }
    }

    override fun getTotalActiveOrder(result: OrderCounts) {
        val countActiveOrder = result.unpaid_orders + result.waiting_orders + result.process_orders
        if (countActiveOrder != 0) {
            textTotalOrder?.text = countActiveOrder.toString()
            textTotalOrder?.toVisible()
        } else {
            textTotalOrder?.toGone()
        }
    }

    private fun setDataProfile(username: String) {
        val data = tinyDB?.getObject(TINY_PROFILE, LoginData::class.java)
        val update = LoginData()
        update.displayName = username
        update.games = data?.games
        update.otpConfirmation = data?.otpConfirmation
        update.role = data?.role
        update.topics = data?.topics
        update.userEmail = data?.userEmail
        update.userId = data?.userId
        update.userLogin = data?.userLogin
        update.userNicename = data?.userNicename
        update.userNumber = data?.userNumber
        update.userRegistered = data?.userRegistered

        tinyDB?.putObject(TINY_PROFILE, update)
    }

    override fun showError(throwable: Throwable) {
        activity?.showOkDialog(throwable.message.toString(), "Oke", null)
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