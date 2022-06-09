package com.spin.id.ui.webview.webview.constant;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class HttpMethodConstant {
    @IntDef({Value.GET, Value.POST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Value {
        int GET = 1;
        int POST = 2;
    }
}
