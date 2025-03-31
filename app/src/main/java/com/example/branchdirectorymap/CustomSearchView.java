package com.example.branchdirectorymap;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.widget.SearchView;

public class CustomSearchView extends SearchView {

    private boolean isKeyboardOpen = false;

    public CustomSearchView(Context context) {
        super(context);
        init();
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            getWindowVisibleDisplayFrame(r);
            int screenHeight = getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            isKeyboardOpen = keypadHeight > screenHeight * 0.15;
        });
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (isKeyboardOpen) {
                closeKeyboard();
                return true;
            } else {
                clearFocus();
                return false;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
}
