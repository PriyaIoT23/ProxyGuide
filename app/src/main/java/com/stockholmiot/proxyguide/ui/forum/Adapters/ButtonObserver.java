package com.stockholmiot.proxyguide.ui.forum.Adapters;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;

public class ButtonObserver implements TextWatcher {
    private final ImageButton mButton;

    public ButtonObserver(ImageButton button) {
        mButton = button;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        // No-op
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        mButton.setEnabled(!TextUtils.isEmpty(charSequence.toString().trim()));
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // No-op
    }

}
