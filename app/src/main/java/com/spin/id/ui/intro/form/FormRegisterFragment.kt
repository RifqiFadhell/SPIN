package com.spin.id.ui.intro.form

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
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
import com.spin.id.api.request.preference.UpdatePreferenceRequest
import com.spin.id.api.request.register.RegisterRequest
import com.spin.id.api.request.register.RegisterSSORequest
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.api.response.game.GameData
import com.spin.id.api.response.login.LoginData
import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.preference.PreferenceResponse
import com.spin.id.api.response.register.RegisterResponse
import com.spin.id.api.response.topic.TopicData
import com.spin.id.api.response.validation.CheckUserResponse
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyConstant.TINY_CONFIG
import com.spin.id.preference.tinyDb.TinyConstant.TINY_PASSWORD
import com.spin.id.preference.tinyDb.TinyConstant.TINY_USERNAME
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.ui.boarding.BoardingActivity
import com.spin.id.ui.home.HomeActivity
import com.spin.id.ui.intro.IntroActivity
import com.spin.id.ui.otp.OtpActivity
import com.spin.id.utils.LoaderIndicatorHelper
import com.spin.id.utils.RegexFilter
import com.spin.id.utils.analytic.AnalyticsTrackerConstant
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_LOAD_REGISTRATION
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_NORMAL_REGISTRATION
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SOCIAL_REGISTRATION
import com.spin.id.utils.analytic.AnalyticsTrackerConstant.PARAM_SUBMIT_REGISTRATION
import com.spin.id.utils.base.BaseFragment
import com.spin.id.utils.extensions.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.add_number_dialog.*
import kotlinx.android.synthetic.main.form_register_fragment.*
import kotlinx.android.synthetic.main.form_register_fragment.editNumber
import kotlinx.android.synthetic.main.intro_activity.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.simple_form_fragment.*
import kotlinx.android.synthetic.main.success_registered_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class FormRegisterFragment : BaseFragment(), FormRegisterContract.View {

    private lateinit var presenter: FormRegisterPresenter
    private var tinyDB: TinyDB? = null

    private var code: Int = 0
    private var codeNumber: Int = 0
    private var upper = false
    private var first = false
    private var min = false
    private var statusEmail = false
    private var statusTracker: Boolean = false

    private var firstName: String = ""
    private var lastName: String = ""
    private var userName: String = ""
    private var passwords: String = ""
    private var email: String = ""
    private var mobile: String = ""
    private var type = ""
    private var idToken = ""

    private var status: String = ""

    private var progress: LoaderIndicatorHelper? = null
    private var tracker: AmplitudeClient? = null

    private val RC_SIGN_IN = 1
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private lateinit var callbackManager: CallbackManager

    private lateinit var auth: FirebaseAuth

    private lateinit var dialog: AlertDialog

    override fun provideLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.form_register_fragment, container, false)
    }

    override fun getScreenName() = AnalyticsTrackerConstant.REGISTER

    override fun init(bundle: Bundle?) {
        dialogNumber()
        tracker = Amplitude.getInstance()
        buttonNext.toDisable()
        layoutSuccessRegister.toGone()
        buttonNext.text = getString(R.string.next_label)
        layoutPassword.toDisable()
        tinyDB = TinyDB(context)
        progress = LoaderIndicatorHelper()
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                firebaseAuthWithFacebook(loginResult)
                val request = GraphRequest.newMeRequest(
                    loginResult.accessToken,
                    object : GraphRequest.GraphJSONObjectCallback {
                        override fun onCompleted(
                            `object`: JSONObject,
                            response: GraphResponse
                        ) {

                            // Application code
                            idToken = `object`.getString("id")
                            email = if (`object`.has("email"))
                                `object`.getString("email") else ""
                            firstName = if (`object`.has("first_name"))
                                `object`.getString("first_name") else ""
                            lastName = if (`object`.has("last_name"))
                                `object`.getString("last_name") else ""
                            type = "facebook"

                            verifyLoginViaSSO()
                        }
                    })
                val parameters = Bundle()
                parameters.putString(
                    "fields",
                    "id, first_name, last_name, email,gender, birthday, location"
                )
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                context?.showShortToast("Batal authentikasi facebook")
            }

            override fun onError(error: FacebookException) {
                Log.e("DATAFACEBOOK", Gson().toJson(error))
                context?.showShortToast("Terjadi error ketika authentikasi facebook")
            }
        })
        auth = FirebaseAuth.getInstance()
    }

    private fun dialogNumber() {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.add_number_dialog)
            .setCancelable(false)

        dialog = progressDialog.create()
    }

    override fun initData(bundle: Bundle?) {
        presenter = FormRegisterPresenter()
        if ((activity as IntroActivity).idToken.isNotEmpty() || this.idToken.isNotEmpty()) {
            showSimpleForm()
        } else {
            hideSimpleForm()
        }
    }

    @SuppressLint("CheckResult")
    override fun initListener(bundle: Bundle?) {
        editPassword.setOnFocusChangeListener { _, _ ->
            showPasswordDialog()
            editPassword.afterTextChangeEvents()
                .skip(1)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validatePassword(it.editable.toString())
                }
        }

        editNumber.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editNumber.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    codeNumber = 1
                    validateNumber(it.editable.toString())
                    hideKeyboard()
                }
        }
        editFirstName.setOnFocusChangeListener { _, _ ->
            if (!statusTracker) {
                tracker?.logEvent(PARAM_NORMAL_REGISTRATION)
                statusTracker = true
            }
            hidePasswordDialog()
            editFirstName.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateFirstName(it.editable.toString())
                    hidePasswordDialog()
                }
        }
        editLastName.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editLastName.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateLastName(it.editable.toString())
                }
        }
        editEmail.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editEmail.afterTextChangeEvents()
                .skip(1)
                .debounce(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateEmail(it.editable.toString())
                }
        }
        editUserName.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editUserName.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateUserName(it.editable.toString())
                }
        }
        editUserNameSimple.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editUserNameSimple.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateUserNameSimple(it.editable.toString())
                }
        }
        editNumberSimple.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editNumberSimple.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateNumberSimple(it.editable.toString())
                    hideKeyboard()
                }
        }
        editEmailSimple.setOnFocusChangeListener { _, _ ->
            hidePasswordDialog()
            editEmailSimple.afterTextChangeEvents()
                .skip(1)
                .debounce(1200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    validateEmailSimple(it.editable.toString())
                    hideKeyboard()
                }
        }
        buttonNext.setSingleClickListener { validateRegist() }

        layoutTouchOutside.setOnClickListener { hideKeyboard() }

        buttonFacebook.setOnClickListener {
            tracker?.logEvent(PARAM_SOCIAL_REGISTRATION)
            signInFacebook()
        }

        buttonGoogle.setOnClickListener {
            tracker?.logEvent(PARAM_SOCIAL_REGISTRATION)
            signInGoogle()
        }

        checkReferral.setOnCheckedChangeListener { _, _ ->
            if (checkReferral.isChecked) {
                textInputReferral.toVisible()
            } else {
                textInputReferral.toGone()
            }
        }

        buttonLogin.setOnClickListener {
            val b = Bundle()
            b.putString("FROM_PAGE", (activity as IntroActivity).fromPage)
            activity?.goToActivity(HomeActivity::class.java, b)
            tinyDB?.putBoolean("SPLASH", true)
            activity?.finishAffinity()
        }
    }

    private fun checkUser(value: String) {
        buttonNext.toDisable()
        val request = CheckUserRequest()
        when (code) {
            1 -> {
                request.username = value
            }
            2 -> {
                request.mobile = value
            }
            3 -> {
                request.email = value
            }
            4 -> {
                request.username = value
            }
            5 -> {
                request.mobile = value
            }
            6 -> {
                request.mobile = value
            }
            7 -> {
                request.email = value
            }
        }
        presenter.checkValidation(this, request)
    }

    override fun getData(result: CheckUserResponse) {
        if (code == 6) {
            if (result.status == 401) {
                dialog.buttonSave.toDisable()
                dialog.buttonSave.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.dustyGray
                    )
                )
                dialog.textInputNumberDialog.error = getString(R.string.login_number_available)
            } else {
                dialog.buttonSave.toEnable()
                dialog.buttonSave.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimary
                    )
                )
                dialog.textInputNumberDialog.error = null
                dialog.editNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                    null
                )
            }
        } else {
            when (code) {
                1 -> {
                    if (result.status == 401) {
                        textInputUserName.error =
                            getString(R.string.register_username_was_available_error)
                    } else {
                        textInputUserName.error = null
                        editUserName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                    }
                }
                2 -> {
                    if (result.status == 401) {
                        textInputNumber.error =
                            getString(R.string.register_number_was_available_error)
                    } else {
                        textInputNumber.error = null
                        editNumber.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                    }
                }
                3 -> {
                    if (result.status == 401) {
                        textInputEmail.error =
                            getString(R.string.register_email_was_available_error)
                        hideKeyboard()
                    } else {
                        textInputEmail.error = null
                        statusEmail = true
                        editEmail.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                        hideKeyboard()
                    }
                }
                4 -> {
                    if (result.status == 401) {
                        textInputUserNameSimple.error =
                            getString(R.string.register_username_was_available_error)
                    } else {
                        textInputUserNameSimple.error = null
                        editUserNameSimple.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                    }
                }
                5 -> {
                    if (result.status == 401) {
                        textInputNumberSimple.error =
                            getString(R.string.register_number_was_available_error)
                    } else {
                        textInputNumberSimple.error = null
                        editNumberSimple.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                    }
                }
                7 -> {
                    if (result.status == 401) {
                        textInputEmailSimple.error =
                            getString(R.string.register_email_was_available_error)
                        hideKeyboard()
                    } else {
                        textInputEmailSimple.error = null
                        statusEmail = true
                        editEmailSimple.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_verified),
                            null
                        )
                        hideKeyboard()
                    }
                }
            }
            code = 0
            checkAllValidation()
            hideKeyboard()
        }
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

    private fun validateUserName(username: String) {
        when {
            username.isEmpty() -> {
                textInputUserName.error = getString(R.string.register_username_empty_error)
            }
            username.matches(".*[!@#\$%^&*+=?-].*".toRegex()) -> {
                textInputUserName.error = getString(R.string.register_username_special_error)
            }
            username.matches(".*[A-Z].*".toRegex()) -> {
                textInputUserName.error = getString(R.string.register_username_upper_error)
            }
            username.matches(".*[\\s].*".toRegex()) -> {
                textInputUserName.error = getString(R.string.register_username_space_error)
            }
            username.length < 3 -> {
                textInputUserName.error = getString(R.string.register_username_min_error)
            }
            else -> {
                textInputUserName.error = null
                userName = username
                code = 1
                checkUser(username)
            }
        }
        checkAllValidation()
    }

    private fun validateUserNameSimple(username: String) {
        when {
            username.isEmpty() -> {
                textInputUserNameSimple.error = getString(R.string.register_username_empty_error)
            }
            username.matches(".*[!@#\$%^&*+=?-].*".toRegex()) -> {
                textInputUserNameSimple.error = getString(R.string.register_username_special_error)
            }
            username.matches(".*[A-Z].*".toRegex()) -> {
                textInputUserNameSimple.error = getString(R.string.register_username_upper_error)
            }
            username.matches(".*[\\s].*".toRegex()) -> {
                textInputUserNameSimple.error = getString(R.string.register_username_space_error)
            }
            username.length < 3 -> {
                textInputUserNameSimple.error = getString(R.string.register_username_min_error)
            }
            else -> {
                textInputUserNameSimple.error = null
                userName = username
                code = 4
                checkUser(username)
            }
        }
        checkAllValidation()
    }

    private fun validateNumber(number: String) {
        if (codeNumber == 1) {
            when {
                number.isEmpty() -> {
                    textInputNumber.error = getString(R.string.register_number_empty_error)
                }
                number.length < 9 -> {
                    textInputNumber.error = getString(R.string.register_number_min_error)
                }
                else -> {
                    textInputNumber.error = null
                    mobile = number
                    code = 2
                    checkUser(number)
                }
            }
        } else if (codeNumber == 2) {
            when {
                number.isEmpty() -> {
                    dialog.textInputNumberDialog.error =
                        getString(R.string.register_number_empty_error)
                }
                number.length < 9 -> {
                    dialog.textInputNumberDialog.error =
                        getString(R.string.register_number_min_error)
                }
                else -> {
                    dialog.textInputNumberDialog.error = null
                    checkUser(number)
                }
            }
        }
    }

    private fun validateNumberSimple(number: String) {
        when {
            number.isEmpty() -> {
                textInputNumberSimple.error = getString(R.string.register_number_empty_error)
            }
            number.length < 9 -> {
                textInputNumberSimple.error = getString(R.string.register_number_min_error)
            }
            else -> {
                textInputNumberSimple.error = null
                mobile = number
                code = 5
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
                email = value
                code = 3
                checkUser(value)
            }
        }
    }

    private fun validateEmailSimple(value: String) {
        when {
            value.isEmpty() -> {
                textInputEmailSimple.error = getString(R.string.register_email_empty_error)
                hideKeyboard()
            }
            !RegexFilter.isEmailValid(value) -> {
                textInputEmailSimple.error = getString(R.string.register_email_error)
                hideKeyboard()
            }
            else -> {
                textInputEmailSimple.error = null
                email = value
                code = 7
                checkUser(value)
            }
        }
    }

    private fun validatePassword(password: String) {
        /*if (password.matches(".*[A-Z].*".toRegex())) {
            textUppercase.setTextColor(ContextCompat.getColor(requireContext(), R.color.limeAde))
            textUppercase.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_check
                ), null, null, null
            )
            upper = true
        } else {
            textUppercase.setTextColor(ContextCompat.getColor(requireContext(), R.color.dustyGray))
            textUppercase.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_uncheck
                ), null, null, null
            )
            upper = false
        }*/

        if (password.length > 7) {
            textMinPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.limeAde))
            textMinPassword.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_check
                ), null, null, null
            )
            min = true
        } else {
            textMinPassword.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dustyGray
                )
            )
            textMinPassword.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_uncheck
                ), null, null, null
            )
            min = false
        }

        /*if (password.matches(".*[0-9].*".toRegex())) {
            textSpecialChar.setTextColor(ContextCompat.getColor(requireContext(), R.color.limeAde))
            textSpecialChar.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_check
                ), null, null, null
            )
            sensitive = true
        } else {
            textSpecialChar.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dustyGray
                )
            )
            textSpecialChar.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_uncheck
                ), null, null, null
            )
            sensitive = false
        }*/

        when {
            password.isEmpty() -> {
                textInputPassword.error = getString(R.string.register_password_empty_error)
            }
            else -> {
                textInputPassword.error = null
            }
        }
        passwords = password
        if (min) {
            textInputPassword.error = null
        }
        checkAllValidation()
    }

    private fun checkAllValidation() {
        if (((textInputFirstName.error == null && editFirstName.editableText.toString().isNotEmpty()
                    && textInputLastName.error == null && editLastName.editableText.toString()
                .isNotEmpty()
                    && textInputUserName.error == null && editUserName.editableText.toString()
                .isNotEmpty()
                    && textInputPassword.error == null && editPassword.editableText.toString()
                .isNotEmpty()
                    && textInputNumber.error == null && editNumber.editableText.toString()
                .isNotEmpty() && editEmail.editableText.toString()
                .isNotEmpty() && textInputEmail.error == null) && min && statusEmail) ||
            (textInputUserNameSimple.error == null && editUserNameSimple.editableText.toString()
                .isNotEmpty() && textInputNumberSimple.error == null && editNumberSimple.editableText.toString()
                .isNotEmpty() && if (textInputEmailSimple.isVisible) {
                textInputEmailSimple.error == null && editEmailSimple.editableText.toString()
                    .isNotEmpty() && statusEmail
            } else true)
        ) {
            buttonNext.toEnable()
        } else {
            buttonNext.toDisable()
        }
    }

    override fun getDataRegister(result: RegisterResponse) {
        context?.showLongToast(result.message.toString())
        when (result.code) {
            200 -> {
                verifyLogin()
            }
            411 -> {
                showDialogError(getString(R.string.register_number_email_was_available_error))
            }
            410 -> {
                showDialogError(getString(R.string.register_username_was_available_error))
            }
            else -> showDialogError(result.message.toString())
        }
    }

    private fun verifyLogin() {
        val request = LoginRequest()
        request.password = passwords
        request.username = userName
        presenter.login(this, request)
    }

    /*SSO*/

    private fun verifyLoginViaSSO() {
        val idToken =
            if (this.idToken.isNotEmpty()) this.idToken else (activity as IntroActivity).idToken
        val email =
            if (this.email.isNotEmpty()) this.email else (activity as IntroActivity).email
        val platform =
            if (this.type.isNotEmpty()) this.type else (activity as IntroActivity).type
        val request = LoginSSORequest()
        request.token = idToken
        request.email = email
        request.platform = platform
        request.utm_source =
            if ((activity as IntroActivity).fromPage.equals("DetailProduct")) "shop" else ""
        presenter.login(this, request)
    }

    override fun getDataLogin(result: LoginResponse) {
        when (result.status) {
            200 -> {
                tinyDB?.putBoolean(TinyConstant.TINY_STATUS_LOGIN, true)
                tinyDB?.putObject(TinyConstant.TINY_PROFILE, result.data)
                tinyDB?.putString(
                    TinyConstant.TINY_TOKEN,
                    "Bearer " + result.data?.token.toString()
                )
                hideProgress()
                layoutSuccessRegister.toVisible()
                activity?.toolbar?.visibility = View.INVISIBLE
                activity?.indicatorPagerIntro?.visibility = View.INVISIBLE
                layoutNavigation.visibility = View.INVISIBLE
            }
            401 -> {
                showSimpleForm()
            }
            410 -> {
                showSimpleForm()
            }
            402 -> {
                result.data?.let { showDialog(getString(R.string.login_number_request), "otp", it) }
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
                        getString(R.string.login_number_request),
                        "mobile",
                        it
                    )
                }
            }
            406 -> {
                result.data?.let {
                    showDialog(
                        getString(R.string.login_number_request),
                        "mobile",
                        it
                    )
                }
            }
            else -> {
                requireActivity().showOkDialog(
                    result.message.toString(),
                    "Ok",
                    DialogInterface.OnClickListener { _, _ ->
                        activity?.goToActivity(BoardingActivity::class.java)
                        activity?.finishAffinity()
                    })
            }
        }
    }

    private fun showDialog(message: String, status: String, data: LoginData) {
        requireContext().showOkDialog(
            message,
            "Oke",
            DialogInterface.OnClickListener { _, _ ->
                goToOtpPage(
                    data.userId.toString(),
                    status,
                    data.userNumber.toString()
                )
            })
    }

    private fun goToOtpPage(id: String, status: String, mobile: String) {
        if (tinyDB?.getString(TINY_CONFIG) == "otp") {
            val b = Bundle()
            b.putString("id", id)
            b.putString("status", status)
            b.putString("mobile", mobile)
            activity?.goToActivity(OtpActivity::class.java, b)
        } else if (tinyDB?.getString(TINY_CONFIG) == "non_otp") {
            dialog.show()
            dialog.buttonSave.toDisable()
            dialog.buttonSave.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dustyGray
                )
            )
            dialog.buttonSave.setOnClickListener { saveNumber(id) }
            dialog.buttonCancel.setOnClickListener {
                dialog.hide()
                buttonLogin.toEnable()
            }
            dialog.editNumber.afterTextChangeEvents()
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    code = 6
                    codeNumber = 2
                    validateNumber(it.editable.toString())
                }
        }
    }

    private fun saveNumber(userId: String) {
        showProgress()
        val number = dialog.editNumber.editableText.toString()
        val request = AddNumberRequest(number, userId)
        presenter.addNumber(this, request)
    }

    override fun getDataAddNumber(result: LoginResponse) {
        if (result.status == 200) {
            tinyDB?.putString(TinyConstant.TINY_TOKEN, "Bearer " + result.data?.token.toString())
            tinyDB?.putBoolean(TinyConstant.TINY_STATUS_LOGIN, true)
            tinyDB?.putObject(TinyConstant.TINY_PROFILE, result.data)
            tinyDB?.putBoolean("SPLASH", true)
            activity?.goToActivity(HomeActivity::class.java)
            activity?.finishAffinity()
        } else if (result.status == 404 || result.status == 403 || result.status == 405) {
            tinyDB?.putString(TinyConstant.TINY_TOKEN, "Bearer " + result.data?.token.toString())
            tinyDB?.putObject(TinyConstant.TINY_PROFILE, result.data)
            val request = UpdatePreferenceRequest(
                getSelectedIdGames(),
                getSelectedIdTopic(),
                result.data?.userId.toString()
            )
            presenter.updatePreference(this, request, "Bearer " + result.data?.token.toString())
        } else {
            showDialogError(result.message.toString())
        }
    }

    override fun getDataUpdatePreference(result: PreferenceResponse) {
        tinyDB?.putBoolean(TinyConstant.TINY_STATUS_LOGIN, true)
        tinyDB?.putBoolean("SPLASH", true)
        requireActivity().goToActivity(HomeActivity::class.java)
        requireActivity().finishAffinity()
    }

    private fun showDialogPreference(response: LoginResponse) {
        tinyDB?.putString(TinyConstant.TINY_TOKEN, "Bearer " + response.data?.token.toString())
        tinyDB?.putString("from", "login")
        tinyDB?.putBoolean(TinyConstant.TINY_STATUS_LOGIN, true)
        tinyDB?.putObject(TinyConstant.TINY_PROFILE, response.data)
        val b = Bundle()
        b.putString("type", "set-preference")
        activity?.goToActivity(IntroActivity::class.java, b)
        activity?.finishAffinity()
    }

    private fun validateRegist() {
        //VALIDATE VIA SOSIAL REGISTRATION
        if (idToken.isNotEmpty() || (activity as IntroActivity).idToken.isNotEmpty()) {
            val idToken =
                if (this.idToken.isNotEmpty()) this.idToken else (activity as IntroActivity).idToken
            val type = if (this.type.isNotEmpty()) this.type else (activity as IntroActivity).type
            val firstName =
                if (this.firstName.isNotEmpty()) this.firstName else (activity as IntroActivity).firstName
            val lastName =
                if (this.lastName.isNotEmpty()) this.lastName else (activity as IntroActivity).lastName
            val email =
                if (this.email.isNotEmpty()) this.email else (activity as IntroActivity).email

            val eventProperties = JSONObject()
            try {
                eventProperties.put("Selected Registration Flow", type)
            } catch (exception: JSONException) {

            }
            tracker?.logEvent(PARAM_SUBMIT_REGISTRATION, eventProperties)

            if (email.isNotEmpty()) {
                socialRegister(
                    idToken,
                    type,
                    firstName,
                    lastName,
                    email,
                    editUserNameSimple.editableText.toString(),
                    editNumberSimple.editableText.toString()
                )
            } else {
                socialRegister(
                    idToken,
                    type,
                    firstName,
                    lastName,
                    editEmailSimple.text.toString(),
                    editUserNameSimple.editableText.toString(),
                    editNumberSimple.editableText.toString()
                )
            }
        } else {

            if (!tinyDB?.getString(TINY_CONFIG).isNullOrEmpty()) {
                status = tinyDB?.getString(TINY_CONFIG).toString()
            }
            val eventProperties = JSONObject()
            try {
                eventProperties.put("Selected Registration Flow", "Normal")
            } catch (exception: JSONException) {

            }
            tracker?.logEvent(PARAM_SUBMIT_REGISTRATION, eventProperties)

            tinyDB?.putString(TINY_PASSWORD, passwords)
            tinyDB?.putString(TINY_USERNAME, userName)

            if (status == "otp") {
                val intent = Intent(activity, OtpActivity::class.java)
                intent.putExtra("mobile", mobile)
                intent.putExtra("status", "register")
                intent.putExtra("username", userName)
                intent.putExtra("firstname", firstName)
                intent.putExtra("lastname", lastName)
                intent.putExtra("email", email)
                intent.putExtra("password", passwords)
                activity?.startActivity(intent)
            } else if (status == "non_otp") {
                register()
            }
        }
    }

    private fun register() {
        val request = RegisterRequest()
        request.username = userName
        request.first_name = firstName
        request.last_name = lastName
        request.email = email
        request.password = passwords
        request.mobile = mobile
        request.games = getSelectedIdGames()
        request.topics = getSelectedIdTopic()
        request.utm_source =
            if ((activity as IntroActivity).fromPage.equals("DetailProduct")) "shop" else ""

        presenter.register(this, request)
    }

    private fun socialRegister(
        token: String, platform: String, firstName: String, lastName: String,
        email: String, username: String, mobile: String
    ) {
        val request = RegisterSSORequest()
        request.token = token
        request.platform = platform
        request.username = username
        request.first_name = firstName
        request.last_name = lastName
        request.email = email
        request.mobile = mobile
        request.games = getSelectedIdGames()
        request.topics = getSelectedIdTopic()
        request.utm_source =
            if ((activity as IntroActivity).fromPage.equals("DetailProduct")) "shop" else ""

        presenter.registerSSO(this, request)
    }

    private fun loadRegisterTracker() {
        val topic = tinyDB?.getListObject(
            TinyConstant.TINY_LIST_TOPIC,
            TopicData::class.java
        ) as ArrayList<TopicData>
        val listTopic = ArrayList<String>()
        val game = tinyDB?.getListObject(
            TinyConstant.TINY_LIST_GAME,
            GameData::class.java
        ) as ArrayList<GameData>
        val listGame = ArrayList<String>()
        for (x in 0 until topic.size) {
            listTopic.plusAssign(topic[x].name)
        }
        for (x in 0 until game.size) {
            listGame.plusAssign(game[x].name)
        }

        val eventProperties = JSONObject()
        try {
            eventProperties.put("Selected Game", listGame)
            eventProperties.put("Selected Topic", listTopic)
        } catch (exception: JSONException) {

        }
        tracker?.logEvent(PARAM_LOAD_REGISTRATION, eventProperties)
        first = true
    }

    override fun getDataRegisterSSO(result: RegisterResponse) {
        when (result.code) {
            200 -> {
                verifyLoginViaSSO()
            }
            411 -> {
                showDialogError(getString(R.string.register_number_email_was_available_error))
            }
            410 -> {
                showDialogError(getString(R.string.register_username_was_available_error))
            }
            else -> context?.showShortToast(result.message.toString())
        }
    }

    override fun showErrorRegisterSSO(throwable: Throwable) {
        showDialogError(throwable.message.toString())
        code = 0
    }

    private fun showDialogError(message: String) {
        activity?.showOkDialog(message, "Oke", null)
        hideProgress()
    }

    override fun showError(throwable: Throwable) {
        showDialogError(throwable.message.toString())
        code = 0
    }

    private fun showPasswordDialog() {
        layoutPassword.toVisible()
        imageGatePassword.toVisible()
    }

    private fun hidePasswordDialog() {
        layoutPassword.toGone()
        imageGatePassword.toGone()
    }

    override fun showProgress() {
        progress?.showDialog(requireContext())
    }

    override fun hideProgress() {
        progress?.dismissDialog()
    }

    private fun resetAll() {
        editLastName.setText("")
        editFirstName.setText("")
        editEmail.setText("")
        editNumber.setText("")
        editPassword.setText("")
        editUserName.setText("")
    }

    private fun resetError() {
        textInputFirstName.error = null
        textInputLastName.error = null
        textInputEmail.error = null
        textInputNumber.error = null
        textInputPassword.error = null
        textInputUserName.error = null
    }

    override fun onPause() {
        super.onPause()
        statusTracker = false
        resetError()
    }

    override fun onResume() {
        super.onResume()
        resetError()
        if (!first) loadRegisterTracker()
    }

    private fun getSelectedIdGames(): ArrayList<String> {
        val idListGame = ArrayList<String>()
        val itemList = ArrayList<GameData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_GAME, GameData::class.java
            ) as ArrayList<GameData>
        )
        Log.e("DATA GAMES SELECTED 2", Gson().toJson(itemList))
        for (item in itemList) {
            if (item.isSelected) {
                idListGame.add(item.id)
            }
        }
        if (idListGame.isEmpty()) {
            idListGame.add("0")
        }
        Log.e("DATA GAMES SELECTED 2", Gson().toJson(idListGame))
        return idListGame
    }

    private fun getSelectedIdTopic(): ArrayList<String> {
        val idListTopic = ArrayList<String>()
        val itemList = ArrayList<TopicData>()
        itemList.clear()
        itemList.addAll(
            TinyDB(requireContext()).getListObject(
                TinyConstant.TINY_LIST_TOPIC, TopicData::class.java
            ) as ArrayList<TopicData>
        )
        for (item in itemList) {
            if (item.isSelected) {
                idListTopic.add(item.id)
            }
        }
        if (idListTopic.isEmpty()) {
            idListTopic.add("0")
        }
        return idListTopic
    }

    private fun showSimpleForm() {
        (activity as IntroActivity).hideButtonSkip()
        layoutRegisterForm.toGone()
        layoutSimpleForm.toVisible()
        val email =
            if (this.email.isNotEmpty()) this.email else (activity as IntroActivity).email
        if (email.isNotEmpty()) {
            textInputEmailSimple?.toGone()
        } else {
            textInputEmailSimple?.toVisible()
        }
    }

    private fun hideSimpleForm() {
        layoutRegisterForm.toVisible()
        layoutSimpleForm.toGone()
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
                Bundle()
                this.type = "google"
                this.firstName = account.givenName.toString()
                this.lastName = account.familyName.toString()
                this.idToken = account.id.toString()
                this.email = account.email.toString()
                verifyLoginViaSSO()

            } catch (e: ApiException) {

                Log.e("TAG", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    //context?.showShortToast("Authentication Success")
                } else {
                    // context?.showShortToast("Authentication failed")
                }
            }
    }

    private fun firebaseAuthWithFacebook(loginResult: LoginResult) {
        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    //context?.showShortToast("Authentication Success")
                } else {
                    //context?.showShortToast("Authentication failed")
                }
            }
    }

}