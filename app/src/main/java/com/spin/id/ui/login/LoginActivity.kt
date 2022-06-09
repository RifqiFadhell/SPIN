package com.spin.id.ui.login

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.spin.id.R
import com.spin.id.api.request.login.AddNumberRequest
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.login.LoginSSORequest
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.GamesItem
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.login.TopicsItem
import com.spin.id.api.response.topic.TopicData
import com.spin.id.api.response.validation.CheckUserResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_CONFIG
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PROFILE
import com.spin.id.preference.tinyDb.TinyConstant.TINY_STATUS_LOGIN
import com.spin.id.preference.tinyDb.TinyConstant.TINY_TOKEN
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.forgotpassword.ForgotPasswordActivity
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.otp.OtpActivity
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.RegexFilter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.base.BaseActivity
import com.spin.id.utils.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.add_number_dialog.*
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity(), LoginContract.View, LoginContract.SSOView {

    private lateinit var presenter: LoginPresenter

    private var userName: String = ""
    private var password: String = ""
    private var statusLogin: String = ""
    private var code: Int = 0
    private var isValid: Boolean = false

    private var progress: LoaderIndicatorHelper? = null
    private var tinyDB: TinyDB? = null
    private var loginResponse: LoginResponse? = null
    private lateinit var dialog: AlertDialog

    private val RC_SIGN_IN = 1
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private lateinit var callbackManager: CallbackManager

    private lateinit var auth: FirebaseAuth

    private var fromPage = ""
    override fun provideLayout() {
        setContentView(R.layout.login_activity)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.LOGIN

    override fun init(bundle: Bundle?) {
        disableButton()
        dialogNumber()
        initToolbar()
        tinyDB = TinyDB(this)
        presenter = LoginPresenter()
        progress = LoaderIndicatorHelper()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    //Log.d("DATAFACEBOOK", "facebook:onSuccess:${Gson().toJson(loginResult)}")
                    //setFacebookData(loginResult)
                    firebaseAuthWithFacebook(loginResult)
                    val request = GraphRequest.newMeRequest(
                        loginResult.accessToken,
                        object : GraphRequest.GraphJSONObjectCallback {
                            override fun onCompleted(
                                `object`: JSONObject,
                                response: GraphResponse
                            ) {

                                // Application code
                                val idToken = `object`.getString("id")
                                val email = if (`object`.has("email"))
                                    `object`.getString("email") else ""
                                val firstName = if (`object`.has("first_name"))
                                    `object`.getString("first_name") else ""
                                val lastName = if (`object`.has("last_name"))
                                    `object`.getString("last_name") else ""

                                ssoLogin(idToken, firstName, lastName, email, "facebook")
                            }
                        })
                    val parameters = Bundle()
                    parameters.putString(
                        "fields",
                        "id, first_name, last_name, email, gender, birthday, location"
                    )
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Log.d("DATA LOGIN FACEBOOK", "facebook:onCancel")
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d("DATA LOGIN FACEBOOK", "facebook:onError", error)
                    // ...
                }
            })
        auth = FirebaseAuth.getInstance()
    }

    override fun initData(bundle: Bundle?) {
        if (intent != null) {
            fromPage = intent.getStringExtra(BoardingActivity.FROM_PAGE) ?: ""
        }
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        buttonBack.setOnClickListener { onBackPressed() }

        editUserName.afterTextChangeEvents()
            .skip(1)
            .debounce(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                code = 1
                validateUsernameNumber(it.editable.toString())
            }

        editPassword.afterTextChangeEvents()
            .skip(1)
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                validatePassword(it.editable.toString())
            }

        buttonLogin.setOnClickListener { login() }

        textForgotPassword?.setOnClickListener {
            goToActivity(ForgotPasswordActivity::class.java)
        }

        textTitleRegister.setOnClickListener {
            goToActivity(IntroActivity::class.java)
        }
        layout.setOnClickListener { hideKeyboard() }

        buttonGoogle.setOnClickListener {
            signInGoogle()
        }

        buttonFacebook.setOnClickListener {
            signInFacebook()
        }
    }

    private fun validateUsernameNumber(value: String) {
        if (code == 1) {
            val request = CheckUserRequest()
            when {
                value.isEmpty() -> {
                    textInputUserName.error = getString(R.string.register_username_empty_error)
                }
                else -> {
                    if (RegexFilter.isNumberValid(value)) {
                        request.mobile = value
                    } else {
                        request.username = value
                    }
                    presenter.checkValidation(this, request)
                    userName = value
                }
            }
            validateAll()
        } else if (code == 2) {
            dialog.buttonSave.toDisable()
            dialog.buttonSave.setTextColor(ContextCompat.getColor(this, R.color.dustyGray))
            val request = CheckUserRequest()
            when {
                value.isEmpty() -> {
                    dialog.textInputNumberDialog.error =
                        getString(R.string.register_number_empty_error)
                    dialog.buttonSave.toDisable()
                    dialog.buttonSave.setTextColor(ContextCompat.getColor(this, R.color.dustyGray))
                }
                else -> {
                    if (RegexFilter.isNumberValid(value)) {
                        request.mobile = value
                        dialog.textInputNumberDialog.error = null
                        if (value.length > 9) {
                            presenter.checkValidation(this, request)
                        }
                    } else {
                        dialog.textInputNumberDialog.error = getString(R.string.number_was_error)
                        dialog.buttonSave.toDisable()
                        dialog.buttonSave.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.dustyGray
                            )
                        )
                    }
                }
            }
        }
    }

    private fun validatePassword(value: String) {
        password = value
        when {
            value.isEmpty() -> {
                textInputPassword.error = getString(R.string.register_password_empty_error)
            }
            value.length < 8 -> {
                textInputPassword.error = getString(R.string.register_password_min_dialog_title)
            }
            else -> {
                textInputPassword.error = null
            }
        }
        validateAll()
    }

    private fun validateAll() {
        buttonLogin.isEnabled =
            isValid && textInputPassword.error == null && textInputUserName.error == null && editPassword.editableText.toString()
                .isNotEmpty()
    }

    private fun login() {
        val request = LoginRequest()
        request.password = password
        request.username = userName
        buttonLogin.toDisable()
        presenter.login(this, request)
    }

    private fun initToolbar() {
        textTitleToolbar.text = getString(R.string.login_label)
    }

    private fun disableButton() {
        buttonLogin.isEnabled = false
    }

    override fun showProgress() {
        progress?.showDialog(this)
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    override fun getData(result: LoginResponse) {
        loginResponse = result
        /*
        * 402 not otp
        * 403 not game
        * 404 not topic
        * 405 not mobile
        * 406 from web
        * 400 not username
        * 401 not registered
        */

        when (result.status) {
            406 -> {
                result.data?.let { showDialog("web", getString(R.string.login_web_request), it) }
                tinyDB?.putString(TinyConstant.TINY_USERNAME, userName)
                tinyDB?.putString(TinyConstant.TINY_PASSWORD, password)
            }
            405 -> {
                result.data?.let {
                    showDialog(
                        "mobile",
                        getString(R.string.login_number_request),
                        it
                    )
                }
                tinyDB?.putString(TinyConstant.TINY_USERNAME, userName)
                tinyDB?.putString(TinyConstant.TINY_PASSWORD, password)
            }
            402 -> {
                result.data?.let { showDialog("otp", getString(R.string.login_number_request), it) }
            }
            404 -> {
                showDialogPreference(result)
            }
            403 -> {
                showDialogPreference(result)
            }
            407 -> {
                result.data?.let { showOkDialog(result.message.toString(), "Oke", null) }
            }
            200 -> {
                Log.e("PROFILE DATA", Gson().toJson(result))
                saveLogin(result)
                tinyDB?.putBoolean("SPLASH", true)
                val b = Bundle()
                b.putString("FROM_PAGE", fromPage)
                goToActivity(HomeActivity::class.java, b)
                finishAffinity()
            }
            else -> {
                result.data?.let { showOkDialog(result.message.toString(), "Oke", null) }
            }
        }
    }

    private fun showDialogPreference(response: LoginResponse) {
        tinyDB?.putString(TINY_TOKEN, "Bearer " + response.data?.token.toString())
        tinyDB?.putString("from", "login")
        tinyDB?.putBoolean(TINY_STATUS_LOGIN, true)
        tinyDB?.putObject(TINY_PROFILE, response.data)
        val intent = Intent(this, IntroActivity::class.java)
        intent.putExtra("type", "set-preference")
        startActivity(intent)
        finishAffinity()
    }

    private fun showDialog(status: String, caption: String, data: LoginData) {
        this.showOkDialog(caption,
            "Oke",
            DialogInterface.OnClickListener { _, _ ->
                goToOtpPage(
                    data.userId.toString(),
                    status,
                    data.userNumber.toString()
                )
            })
    }

    private fun saveLogin(response: LoginResponse) {
        response.data?.games?.let {
            updateGame(it)
        }
        response.data?.topics?.let {
            updateTopic(it)
        }
        tinyDB?.putString(TINY_TOKEN, "Bearer " + response.data?.token.toString())
        tinyDB?.putBoolean(TINY_STATUS_LOGIN, true)
        tinyDB?.putObject(TINY_PROFILE, response.data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateGame(game: List<GamesItem>) {
        (TinyDB(this).getListObject(
            TinyConstant.TINY_LIST_GAME, GameData::class.java
        ) as ArrayList<*>).clear()
        val gameList = ArrayList<GameData>()
        game.let {
            for (item in it) {
                gameList.add(
                    GameData(
                        item.gameCategoryId.toString(),
                        item.image.toString(),
                        item.name.toString(),
                        item.codename.toString(),
                        item.sort_order.toString(),
                        item.status.toString(),
                        isSelected = true
                    )
                )
            }
        }
        tinyDB?.putListObject(TinyConstant.TINY_LIST_GAME, gameList as ArrayList<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateTopic(topic: List<TopicsItem>) {
        (TinyDB(this).getListObject(
            TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
        ) as ArrayList<*>).clear()
        val topicList = ArrayList<TopicData>()
        topic.let {
            for (item in it) {
                topicList.add(
                    TopicData(
                        item.description.toString(),
                        item.topicCategoryId.toString(),
                        item.image.toString(),
                        item.name.toString(),
                        item.codename.toString(),
                        item.sort_order.toString(),
                        item.status.toString(),
                        isSelected = true
                    )
                )
            }
        }
        tinyDB?.putListObject(TinyConstant.TINY_LIST_TOPIC, topicList as ArrayList<Any>)

    }

    /*Add Number Phone*/

    private fun goToOtpPage(id: String, status: String, mobile: String) {
        statusLogin = status
        if (tinyDB?.getString(TINY_CONFIG) == "otp") {
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("status", status)
            intent.putExtra("mobile", mobile)
            startActivity(intent)
        } else if (tinyDB?.getString(TINY_CONFIG) == "non_otp") {
            dialog.show()
            dialog.buttonSave.toDisable()
            dialog.buttonSave.setTextColor(ContextCompat.getColor(this, R.color.dustyGray))
            dialog.buttonSave.setOnClickListener { saveNumber() }
            dialog.buttonCancel.setOnClickListener {
                dialog.hide()
                buttonLogin.toEnable()
            }
            dialog.editNumber.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    code = 2
                    validateUsernameNumber(it.editable.toString())
                }
        }
    }

    private fun dialogNumber() {
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.add_number_dialog)
            .setCancelable(false)

        dialog = progressDialog.create()
    }

    private fun saveNumber() {
        val number = dialog.editNumber.editableText.toString()
        if (number.isNotEmpty()) {
            val request = AddNumberRequest(number, loginResponse?.data?.userId)
            presenter.addNumber(this, request)
        } else {
            showOkDialog(getString(R.string.register_number_empty_error), "Oke", null)
        }
    }

    override fun getDataAddNumber(result: LoginResponse) {
        if (result.status == 200) {
            saveLogin(result)
            tinyDB?.putBoolean("SPLASH", true)
            goToActivity(HomeActivity::class.java)
            finishAffinity()
        } else if (result.status == 404 || result.status == 403 || result.status == 405) {
            showDialogPreference(result)
        } else {
            showOkDialog(result.message.toString() + " " + result.status, "Oke", null)
        }
    }

    override fun getDataUser(result: CheckUserResponse) {
        if (code == 1) {
            if (result.status == 401) {
                isValid = true
                textInputUserName.error = null
                editUserName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.ic_verified),
                    null
                )
            } else {
                isValid = false
                textInputUserName.error = getString(R.string.login_username_not_found)
            }
            validateAll()
        } else if (code == 2) {
            if (result.status == 401) {
                dialog.buttonSave.toDisable()
                dialog.buttonSave.setTextColor(ContextCompat.getColor(this, R.color.dustyGray))
                dialog.textInputNumberDialog.error = getString(R.string.login_number_available)
            } else {
                dialog.buttonSave.toEnable()
                dialog.buttonSave.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                dialog.textInputNumberDialog.error = null
                dialog.editNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.ic_verified),
                    null
                )
            }
        }
    }

    override fun getDataSSO(
        result: LoginResponse,
        idToken: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        type: String
    ) {
        loginResponse = result
        when (result.status) {
            //LOGIN SUKSES
            200 -> {
                Log.e("PROFILE DATA", Gson().toJson(result))
                saveLogin(result)
                tinyDB?.putBoolean("SPLASH", true)
                val b = Bundle()
                b.putString("FROM_PAGE", fromPage)
                goToActivity(HomeActivity::class.java, b)
                finishAffinity()
            }
            //USER BELUM TERDAFTAR, ALIHKAN HALAMAN REGISTER
            401 -> {
                val b = Bundle()
                b.putString("type", type)
                b.putString("firstName", firstName)
                b.putString("lastName", lastName)
                b.putString("idToken", idToken)
                b.putString("email", email)
                b.putString(BoardingActivity.FROM_PAGE, fromPage)
                goToActivity(IntroActivity::class.java, b)
            }
            //USER BELUM TERDAFTAR, ALIHKAN HALAMAN REGISTER
            410 -> {
                val b = Bundle()
                b.putString("type", type)
                b.putString("firstName", firstName)
                b.putString("lastName", lastName)
                b.putString("idToken", idToken)
                b.putString("email", email)
                b.putString(BoardingActivity.FROM_PAGE, fromPage)
                goToActivity(IntroActivity::class.java, b)
            }
            402 -> {
                result.data?.let { showDialog("otp", getString(R.string.login_number_request), it) }
            }
            403 -> {
                showDialogPreference(result)
            }
            404 -> {
                showDialogPreference(result)
            }
            405 -> {
                result.data?.let {
                    showDialog(
                        "mobile",
                        getString(R.string.login_number_request),
                        it
                    )
                }
                tinyDB?.putString(TinyConstant.TINY_USERNAME, userName)
                tinyDB?.putString(TinyConstant.TINY_PASSWORD, password)
            }
            406 -> {
                result.data?.let { showDialog("mobile", getString(R.string.login_web_request), it) }
                tinyDB?.putString(TinyConstant.TINY_USERNAME, userName)
                tinyDB?.putString(TinyConstant.TINY_PASSWORD, password)
            }
            else -> {
                result.data?.let { showOkDialog(result.message.toString(), "Oke", null) }
            }
        }
    }

    override fun showError(throwable: Throwable) {
        validateAll()
        showOkDialog(throwable.message.toString(), "Oke", null)
    }

    override fun showErrorSSO(throwable: Throwable) {
        Log.e("TAGAUTH", Gson().toJson(throwable))
        this.showShortToast(throwable.message.toString())
    }

    private fun signInGoogle() {
        val intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun signInFacebook() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                Log.e("DATAGOOGLE", Gson().toJson(account))
                firebaseAuthWithGoogle(account?.idToken!!)
                ssoLogin(
                    account.id!!,
                    account.givenName,
                    account.familyName,
                    account.email,
                    "google"
                )
                //goToActivity(IntroActivity::class.java, b)
            } catch (e: ApiException) {
                Log.e("TAG", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //showShortToast("Authentication Success")
                } else {
                    Log.w("DATA GOOGLE", "signInWithCredential:failure", task.exception)
                    //showShortToast("Authentication failed")
                }
            }
    }

    private fun firebaseAuthWithFacebook(loginResult: LoginResult) {
        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //showShortToast("Authentication Success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("DATAFACEBOOK", "signInWithCredential:failure", task.exception)
                    //showShortToast("Authentication failed")
                }

                // ...
            }
    }

    private fun ssoLogin(
        idToken: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        type: String
    ) {
        val request = LoginSSORequest(idToken, email, type)
        presenter.login(this, request, firstName, lastName, email, type)
    }
}