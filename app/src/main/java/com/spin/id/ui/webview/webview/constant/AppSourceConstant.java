package com.spin.id.ui.webview.webview.constant;


import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.spin.id.ui.webview.webview.constant.AppSourceConstant.Value.SPIN_ANDROID;

public class AppSourceConstant {

    @StringDef({SPIN_ANDROID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Value {
        String SPIN_ANDROID = "spin-android";
    }

}
