package com.hoko.blur.drawable;

import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * {@link GradientBlurDrawable} 的 instrumentation 测试。
 *
 * 构造会触发 native functor 初始化（{@code System.loadLibrary("hoko_blur")}），
 * 必须在真机/模拟器上跑，JUnit JVM 单测不支持。
 */
@RunWith(AndroidJUnit4.class)
public class GradientBlurDrawableTest {

    @Test
    public void constructor_succeeds() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        assertNotNull(d);
    }

    @Test
    public void modeConstants_alignWithBlurDrawable() {
        // 防止以后误改 MODE_* 数值
        assertEquals(0, GradientBlurDrawable.MODE_BOX);
        assertEquals(1, GradientBlurDrawable.MODE_GAUSSIAN);
        assertEquals(2, GradientBlurDrawable.MODE_STACK);
        assertEquals(GradientBlurDrawable.MODE_BOX, BlurDrawable.MODE_BOX);
        assertEquals(GradientBlurDrawable.MODE_GAUSSIAN, BlurDrawable.MODE_GAUSSIAN);
        assertEquals(GradientBlurDrawable.MODE_STACK, BlurDrawable.MODE_STACK);
    }

    @Test
    public void defaults_matchRendererBuilder() {
        GradientBlurDrawable d = new GradientBlurDrawable();

        assertEquals(BlurDrawable.MODE_STACK, d.mode());
        assertEquals(5, d.radius());
        assertEquals(4.0f, d.sampleFactor(), 0.0001f);
        assertEquals(Color.TRANSPARENT, d.mixColor());
        assertEquals(1.0f, d.mixPercent(), 0.0001f);
    }

    @Test
    public void setters_propagateToGetters() {
        GradientBlurDrawable d = new GradientBlurDrawable();

        d.mode(GradientBlurDrawable.MODE_GAUSSIAN);
        assertEquals(GradientBlurDrawable.MODE_GAUSSIAN, d.mode());

        d.radius(25);
        assertEquals(25, d.radius());

        d.sampleFactor(8.0f);
        assertEquals(8.0f, d.sampleFactor(), 0.0001f);

        d.mixColor(0x80FF0000);
        assertEquals(0x80FF0000, d.mixColor());

        d.mixPercent(0.5f);
        assertEquals(0.5f, d.mixPercent(), 0.0001f);
    }

    @Test
    public void mixPercent_inRange_succeeds() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        d.mixPercent(0f);
        assertEquals(0f, d.mixPercent(), 0.0001f);

        d.mixPercent(1.0f);
        assertEquals(1.0f, d.mixPercent(), 0.0001f);

        d.mixPercent(0.42f);
        assertEquals(0.42f, d.mixPercent(), 0.0001f);
    }

    @Test
    public void mixPercent_aboveOne_throws() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        try {
            d.mixPercent(1.1f);
            fail("mixPercent > 1 must throw");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void mixPercent_belowZero_throws() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        try {
            d.mixPercent(-0.01f);
            fail("mixPercent < 0 must throw");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void disableEnableBlur_togglesFlag() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        // 默认 enabled；具体 flag 是 private，仅验证 setter 不崩溃即可
        d.disableBlur();
        d.enableBlur();
    }

    @Test
    public void onlyDirtyRegion_doesNotThrow() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        d.onlyDirtyRegion(false);
        d.onlyDirtyRegion(true);
    }

    @Test
    public void freeGLResource_doesNotThrow() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        d.freeGLResource();
        // free 后再调用 setter 不应崩溃
        d.radius(7);
        assertEquals(7, d.radius());
    }

    @Test
    public void deprecatedSetAlpha_acceptsValue() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        @SuppressWarnings("deprecation")
        android.graphics.drawable.Drawable dd = d;
        dd.setAlpha(128);
    }

    @Test
    public void getOpacity_translucentByDefault() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        int op = d.getOpacity();
        // 默认 alpha=0，应当是 TRANSLUCENT 或更弱
        assertEquals(android.graphics.PixelFormat.TRANSLUCENT, op);
    }

    @Test
    public void setColorFilter_isNoOp() {
        GradientBlurDrawable d = new GradientBlurDrawable();
        // setColorFilter 应该静默忽略，不抛异常
        d.setColorFilter(null);
    }
}