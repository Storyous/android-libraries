package com.storyous.commonutils.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * Created by Václav on 11. 11. 2014.
 */
public class LoadingButton extends LoadingOverlayElement {

    protected Button mButton;
    private OnClickListener mClickListener;

    public LoadingButton(Context context) {
        this(context, null);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mButton = new Button(getContext(), attrs, defStyle);
        mButton.setId(android.R.id.button1);
        mButton.setVisibility(VISIBLE);
        setViewToOverlay(mButton);
        setOverlayListener(this::onButtonClicked);
    }

    protected void onButtonClicked(View v) {
        if (!isLoading() && mClickListener != null) {
            mClickListener.onClick(v);
        }
    }

    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }

    public void setText(@StringRes int resId) {
        mButton.setText(resId);
    }

    public void setText(CharSequence text) {
        mButton.setText(text);
    }

    public void setButtonBackgroundColor(@ColorRes int resId) {
        mButton.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }
}
