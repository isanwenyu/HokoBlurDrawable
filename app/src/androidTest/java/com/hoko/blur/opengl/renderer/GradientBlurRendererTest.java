package com.hoko.blur.opengl.renderer;

import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hoko.blur.drawable.BlurDrawable;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link GradientBlurRenderer} 的 instrumentation 测试。
 *
 * Renderer 构造本身不依赖 native 库（仅分配 NIO buffer、设置字段），
 * 因此可以在 androidTest 中直接实例化验证 Builder / setter / getter。
 */
@RunWith(AndroidJUnit4.class)
public class GradientBlurRendererTest {

    @Test
    public void builder_defaults() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder().build();

        assertEquals(BlurDrawable.MODE_STACK, r.mode());
        assertEquals(5, r.radius());
        assertEquals(4.0f, r.sampleFactor(), 0.0001f);
        assertEquals(Color.TRANSPARENT, r.mixColor());
        assertEquals(1.0f, r.mixPercent(), 0.0001f);
    }

    @Test
    public void builder_mode() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder()
                .mode(BlurDrawable.MODE_GAUSSIAN)
                .build();
        assertEquals(BlurDrawable.MODE_GAUSSIAN, r.mode());
    }

    @Test
    public void builder_radius() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder()
                .radius(20)
                .build();
        assertEquals(20, r.radius());
    }

    @Test
    public void builder_sampleFactor() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder()
                .sampleFactor(8.0f)
                .build();
        assertEquals(8.0f, r.sampleFactor(), 0.0001f);
    }

    @Test
    public void builder_mixColor() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder()
                .mixColor(0x80FFFFFF)
                .build();
        assertEquals(0x80FFFFFF, r.mixColor());
    }

    @Test
    public void builder_mixPercentInRange_succeeds() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder()
                .mixPercent(0.5f)
                .build();
        assertEquals(0.5f, r.mixPercent(), 0.0001f);

        // 边界 0 和 1.0
        new GradientBlurRenderer.Builder().mixPercent(0f).build();
        new GradientBlurRenderer.Builder().mixPercent(1.0f).build();
    }

    @Test
    public void builder_mixPercentOutOfRange_throws() {
        try {
            new GradientBlurRenderer.Builder().mixPercent(1.5f).build();
            fail("mixPercent > 1 should throw");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            new GradientBlurRenderer.Builder().mixPercent(-0.1f).build();
            fail("mixPercent < 0 should throw");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void builder_chainedCallsReturnSameBuilder() {
        GradientBlurRenderer.Builder b = new GradientBlurRenderer.Builder();
        assertNotSame(null, b);
        assertEquals(b, b.mode(BlurDrawable.MODE_BOX));
        assertEquals(b, b.radius(10));
        assertEquals(b, b.sampleFactor(2f));
        assertEquals(b, b.mixColor(0xFFFF0000));
        assertEquals(b, b.mixPercent(0.5f));
    }

    @Test
    public void copyBuilder_carriesExistingState() {
        GradientBlurRenderer src = new GradientBlurRenderer.Builder()
                .mode(BlurDrawable.MODE_BOX)
                .radius(15)
                .sampleFactor(8f)
                .build();

        GradientBlurRenderer copy = src.newBuilder().build();

        assertEquals(BlurDrawable.MODE_BOX, copy.mode());
        assertEquals(15, copy.radius());
        assertEquals(8f, copy.sampleFactor(), 0.0001f);
    }

    @Test
    public void copyBuilder_nullSource_throws() {
        try {
            new GradientBlurRenderer.Builder(null);
            fail("null source must throw");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void setters_updateGetters() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder().build();

        r.mode(BlurDrawable.MODE_BOX);
        assertEquals(BlurDrawable.MODE_BOX, r.mode());

        r.radius(30);
        assertEquals(30, r.radius());

        r.sampleFactor(2.5f);
        assertEquals(2.5f, r.sampleFactor(), 0.0001f);

        r.mixColor(0x12345678);
        assertEquals(0x12345678, r.mixColor());

        r.mixPercent(0.8f);
        assertEquals(0.8f, r.mixPercent(), 0.0001f);
    }

    @Test
    public void free_doesNotThrow() {
        GradientBlurRenderer r = new GradientBlurRenderer.Builder().build();
        r.free();
        // free 后再调用 setter 不应崩溃
        r.radius(7);
        assertEquals(7, r.radius());
    }

    @Test
    public void twoBuilders_produceDistinctInstances() {
        GradientBlurRenderer a = new GradientBlurRenderer.Builder().build();
        GradientBlurRenderer b = new GradientBlurRenderer.Builder().build();
        assertNotEquals(a, b);
        assertTrue(a != b);
    }
}