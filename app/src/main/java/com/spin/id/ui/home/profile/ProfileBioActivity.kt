package com.spin.id.ui.home.profile

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import com.spin.id.R
import com.spin.id.api.response.login.LoginData
import com.spin.id.preference.tinyDb.TinyConstant
import com.spin.id.preference.tinyDb.TinyDB
import com.spin.id.utils.base.BaseActivity
import kotlinx.android.synthetic.main.edit_instagram_dialog.*
import kotlinx.android.synthetic.main.profile_bio_activity.*

class ProfileBioActivity : BaseActivity() {
    private lateinit var dialogInstagram: AlertDialog
    override fun provideLayout() {
        setContentView(R.layout.profile_bio_activity)
    }

    override fun init(bundle: Bundle?) {
        setProfileBio()
    }

    override fun initData(bundle: Bundle?) {
    }

    override fun initListener(bundle: Bundle?) {
        textInstagram?.setOnClickListener {
            showDialogChangeInsta()
        }
    }

    override fun getScreenName(): String? {
        return ""
    }

    private fun setProfileBio(){
        val data = TinyDB(this).getObject(TinyConstant.TINY_PROFILE, LoginData::class.java)
        textUsername?.text = data.displayName
        textUserEmail?.text = data.userNumber
    }

    private fun showDialogChangeInsta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogInstagram = AlertDialog.Builder(this)
                .setView(R.layout.edit_instagram_dialog)
                .setCancelable(false)
                .create()

            dialogInstagram.show()
            dialogInstagram.buttonCancelInsta.setOnClickListener {
                dialogInstagram.dismiss()
            }
            dialogInstagram.buttonChangeInsta.setOnClickListener {
                changeInstagram()
            }
        }
    }

    private fun changeInstagram(){

    }

}