package com.hoko.blur.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.hoko.blur.drawable.GradientBlurDrawable;

/**
 * FrameLayout 子类，背景默认挂载一个 {@link GradientBlurDrawable}。
 * 用于在 XML 中直接以视图形式使用渐进模糊。
 *
 * Created by hoko team.
 */
public class GradientBlurFrameLayout extends FrameLayout {
    private GradientBlurDrawable mBlurDrawable;

    public GradientBlurFrameLayout(Context context) {
        super(context);
        init();
    }

    public GradientBlurFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientBlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBlurDrawable = new GradientBlurDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBlurDrawable);
        } else {
            setBackgroundDrawable(mBlurDrawable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBlurDrawable.freeGLResource();
    }

    public GradientBlurDrawable getBlurDrawable() {
        return mBlurDrawable;
    }
}