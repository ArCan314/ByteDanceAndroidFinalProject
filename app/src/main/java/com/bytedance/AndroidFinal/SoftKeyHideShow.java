package com.bytedance.AndroidFinal;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

class SoftKeyHideShow {
    static void hideShowSoftKey(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
