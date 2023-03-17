package com.storyous.commonutils.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.storyous.commonutils.R;

public class LoadingOverlayElement extends FrameLayout {

    private View mViewToOverlay;
    private View mLoadingOverlay;
    private boolean mLoading;

    public LoadingOverlayElement(@NonNull Context context) {
        super(context);
        init();
    }

    public LoadingOverlayElement(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingOverlayElement(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LoadingOverlayElement(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setViewToOverlay(View viewToOverlay) {
        mViewToOverlay = viewToOverlay;
        addView(mViewToOverlay);
    }

    private void init() {
        inflate(getContext(), R.layout.overlay_loading, this);
        mLoadingOverlay = findViewById(R.id.overlayLoading);
        setPadding(0, 0, 0, 0);
        setBackgroundResource(R.color.transparent);
    }

    protected void setOverlayListener(OnClickListener listener) {
        if (listener != null) {
            mViewToOverlay.setOnClickListener(listener);
        }
    }

    public void showOverlay(boolean show) {
        mLoading = show;
        if (mLoadingOverlay != null) {
            mLoadingOverlay.setVisibility(show ? VISIBLE : GONE);
        }
        if (mViewToOverlay != null) {
            mViewToOverlay.setVisibility(show ? INVISIBLE : VISIBLE);
        }
    }

    @Override
    public boolean performClick() {
        return isEnabled() && mViewToOverlay.performClick();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mViewToOverlay.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mViewToOverlay == null ? false : mViewToOverlay.isEnabled();
    }

    public boolean isLoading() {
        return mLoading;
    }
}
