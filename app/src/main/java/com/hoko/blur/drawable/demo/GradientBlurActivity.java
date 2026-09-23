package com.hoko.blur.drawable.demo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.hoko.blur.drawable.GradientBlurDrawable;
import com.hoko.blur.view.GradientBlurFrameLayout;

/**
 * 渐进模糊 Drawable 的 demo 入口。
 *
 * ScrollView 提供可变背景内容；中部的 {@link GradientBlurFrameLayout} 覆盖在背景上，
 * 滚动 ScrollView 或点击底部按钮即可观察"顶部清晰—底部模糊"的渐变效果。
 *
 * Created by hoko team.
 */
public class GradientBlurActivity extends Activity {

    private ValueAnimator mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradient_blur);

        final GradientBlurFrameLayout frameLayout = findViewById(R.id.gradient_frameLayout);
        final GradientBlurDrawable drawable = frameLayout.getBlurDrawable();

        drawable.mode(GradientBlurDrawable.MODE_GAUSSIAN);
        drawable.radius(20);
        drawable.sampleFactor(4.0f);
        drawable.mixColor(Color.argb(99, 255, 255, 255));
        drawable.mixPercent(0.5f);

        mAnimator = ValueAnimator.ofInt(0, 30);
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawable.radius((Integer) animation.getAnimatedValue());
            }
        });

        findViewById(R.id.animate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnimator.start();
            }
        });
    }
}