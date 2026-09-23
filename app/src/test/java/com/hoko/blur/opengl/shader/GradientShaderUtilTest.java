package com.hoko.blur.opengl.shader;

import com.hoko.blur.drawable.BlurDrawable;

import org.junit.Test;

import static com.hoko.blur.drawable.BlurDrawable.MODE_BOX;
import static com.hoko.blur.drawable.BlurDrawable.MODE_GAUSSIAN;
import static com.hoko.blur.drawable.BlurDrawable.MODE_STACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 验证 {@link GradientShaderUtil} 生成的片段着色器源码包含关键逻辑。
 *
 * 因为该类只生成字符串、纯 JVM 即可运行，测试放在 {@code app/src/test}（JUnit）。
 */
public class GradientShaderUtilTest {

    @Test
    public void maxDiameterIs101() {
        assertEquals(101, GradientShaderUtil.MAX_DIAMETER);
    }

    @Test
    public void gaussianShader_containsKeyUniforms() {
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_GAUSSIAN);

        assertNotNull(shader);
        assertTrue("must declare uRadiusBase as float uniform",
                shader.contains("uniform float uRadiusBase"));
        assertTrue("must declare uWidthOffset", shader.contains("uniform float uWidthOffset"));
        assertTrue("must declare uHeightOffset", shader.contains("uniform float uHeightOffset"));
        assertTrue("must declare uTexture sampler", shader.contains("uniform sampler2D uTexture"));
    }

    @Test
    public void shader_doesNotUseLegacyIntRadius() {
        // 旧 BlurDrawable shader 用的是 int uRadius，渐进模糊必须不再使用它
        for (int mode : new int[]{MODE_BOX, MODE_GAUSSIAN, MODE_STACK}) {
            String shader = GradientShaderUtil.getGradientFragmentCode(mode);
            assertFalse("legacy uniform int uRadius must not appear",
                    shader.contains("uniform int uRadius"));
        }
    }

    @Test
    public void shader_computesYNormFromTextureCoord() {
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_STACK);
        assertTrue("yNorm 必须基于 vTexCoord.y 反转得到（屏幕顶→0，底→1）",
                shader.contains("float yNorm = 1.0 - vTexCoord.y"));
    }

    @Test
    public void shader_derivesCurrentRadiusFromY() {
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_BOX);
        assertTrue("currentRadius 必须按屏幕 Y 比例由 uRadiusBase 推导",
                shader.contains("float currentRadius = uRadiusBase * yNorm"));
    }

    @Test
    public void shader_clampsDiameterToMax() {
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_GAUSSIAN);
        assertTrue("diameter 必须 clamp 到 MAX_DIAMETER",
                shader.contains("if (diameter > 101) diameter = 101"));
    }

    @Test
    public void shader_loopUsesConstantUpperBound() {
        // GLSL ES 2.0 硬约束：for 循环上界必须为常量表达式
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_STACK);
        assertTrue("循环上界必须是 MAX_DIAMETER(101) 的字面量",
                shader.contains("for (int i = 0; i < 101; i++)"));
    }

    @Test
    public void shader_loopHasEarlyBreak() {
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_BOX);
        assertTrue("必须有 if (i >= diameter) break; 早退分支",
                shader.contains("if (i >= diameter) break"));
    }

    @Test
    public void allModesGenerateDistinctShaders() {
        String box = GradientShaderUtil.getGradientFragmentCode(MODE_BOX);
        String gauss = GradientShaderUtil.getGradientFragmentCode(MODE_GAUSSIAN);
        String stack = GradientShaderUtil.getGradientFragmentCode(MODE_STACK);

        // 三种内核权重计算方式不同，循环体必然不同
        assertFalse(box.equals(gauss));
        assertFalse(gauss.equals(stack));
        assertFalse(box.equals(stack));
    }

    @Test
    public void unknownModeFallsBackToGaussian() {
        // 传入非法 mode 应走 fallback (Gaussian)，不抛异常
        String shader = GradientShaderUtil.getGradientFragmentCode(-1);
        String gaussian = GradientShaderUtil.getGradientFragmentCode(MODE_GAUSSIAN);
        assertEquals(gaussian, shader);
    }

    @Test
    public void shader_endsWithMainCloseBrace() {
        // 防止字符串拼接断尾
        for (int mode : new int[]{MODE_BOX, MODE_GAUSSIAN, MODE_STACK}) {
            String shader = GradientShaderUtil.getGradientFragmentCode(mode);
            String trimmed = shader.replaceAll("\\s+", "");
            assertTrue("shader 必须以 main 函数的 '}' 结尾，mode=" + mode,
                    trimmed.endsWith("}"));
        }
    }

    @Test
    public void shader_doesNotLeakBuiltinNameConflict() {
        // sanity check: 不要误把 sampler2D 写错
        String shader = GradientShaderUtil.getGradientFragmentCode(MODE_GAUSSIAN);
        assertTrue(shader.contains("sampler2D uTexture"));
        assertTrue(shader.contains("varying vec2 vTexCoord"));
    }

    @Test
    public void blurdRawableModeConstantsStable() {
        // 这条断言防止 GradientBlurDrawable 引入时不小心改动了 BlurDrawable 的 mode 常量
        assertEquals(0, BlurDrawable.MODE_BOX);
        assertEquals(1, BlurDrawable.MODE_GAUSSIAN);
        assertEquals(2, BlurDrawable.MODE_STACK);
    }
}