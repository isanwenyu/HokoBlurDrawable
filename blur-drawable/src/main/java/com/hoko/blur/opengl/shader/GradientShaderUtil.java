package com.hoko.blur.opengl.shader;

import com.hoko.blur.anno.Mode;

import static com.hoko.blur.drawable.BlurDrawable.MODE_BOX;
import static com.hoko.blur.drawable.BlurDrawable.MODE_GAUSSIAN;
import static com.hoko.blur.drawable.BlurDrawable.MODE_STACK;

/**
 * 渐进模糊片段着色器生成器。
 *
 * 与 {@link com.hoko.blur.util.ShaderUtil} 的关键差异：
 * <ul>
 *   <li>uniform 从 {@code int uRadius} 改为 {@code float uRadiusBase}</li>
 *   <li>片元内部按屏幕 Y 比例动态推导 {@code currentRadius = uRadiusBase * yNorm}，
 *       实现"顶部 radius≈0、底部 radius≈baseRadius"的线性渐变</li>
 *   <li>GLSL ES 2.0 要求 {@code for} 循环上界为编译期常量，
 *       故用 {@link #MAX_DIAMETER} 作为常量上界，内部用 {@code if (i >= diameter) break;} 早退</li>
 * </ul>
 *
 * Y 翻转约定：{@code vTexCoord.y == 1} 对应屏幕顶部，{@code vTexCoord.y == 0} 对应屏幕底部。
 * 因此 {@code yNorm = 1.0 - vTexCoord.y} 表示"屏幕自顶向下的归一化 Y"。
 *
 * Created by hoko team.
 */
public class GradientShaderUtil {

    /**
     * 最大直径常量。GLSL ES 2.0 要求循环上界必须为编译期常量。
     * 101 对应最大半径 50；超过 50 的设置会被 shader 自动 clamp 到 50。
     */
    public static final int MAX_DIAMETER = 101;

    private static final String HEADER =
            "precision mediump float;\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform float uRadiusBase;\n" +
            "uniform float uWidthOffset;\n" +
            "uniform float uHeightOffset;\n" +
            "mediump float getGaussWeight(mediump float currentPos, mediump float sigma) {\n" +
            "    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma));\n" +
            "}\n";

    private static final String MAIN_HEAD =
            "void main() {\n" +
            "    float yNorm = 1.0 - vTexCoord.y;\n" +
            "    float currentRadius = uRadiusBase * yNorm;\n" +
            "    int diameter = int(2.0 * currentRadius) + 1;\n" +
            "    if (diameter > " + MAX_DIAMETER + ") diameter = " + MAX_DIAMETER + ";\n" +
            "    if (diameter < 1) diameter = 1;\n" +
            "    vec4 sampleTex = vec4(0.0);\n" +
            "    vec3 col = vec3(0.0);\n" +
            "    float weightSum = 0.0;\n";

    private static String boxLoopBody() {
        return
                "    for (int i = 0; i < " + MAX_DIAMETER + "; i++) {\n" +
                "        if (i >= diameter) break;\n" +
                "        float samplePos = float(i) - currentRadius;\n" +
                "        vec2 offset = vec2(samplePos * uWidthOffset, samplePos * uHeightOffset);\n" +
                "        sampleTex = vec4(texture2D(uTexture, vTexCoord.st + offset));\n" +
                "        float boxWeight = 1.0 / float(diameter);\n" +
                "        col += sampleTex.rgb * boxWeight;\n" +
                "        weightSum += boxWeight;\n" +
                "    }\n";
    }

    private static String gaussianLoopBody() {
        return
                "    for (int i = 0; i < " + MAX_DIAMETER + "; i++) {\n" +
                "        if (i >= diameter) break;\n" +
                "        float samplePos = float(i) - currentRadius;\n" +
                "        vec2 offset = vec2(samplePos * uWidthOffset, samplePos * uHeightOffset);\n" +
                "        sampleTex = vec4(texture2D(uTexture, vTexCoord.st + offset));\n" +
                "        float sigma = (currentRadius + 1.0) / 2.0;\n" +
                "        float gaussWeight = getGaussWeight(samplePos, sigma);\n" +
                "        col += sampleTex.rgb * gaussWeight;\n" +
                "        weightSum += gaussWeight;\n" +
                "    }\n";
    }

    private static String stackLoopBody() {
        return
                "    for (int i = 0; i < " + MAX_DIAMETER + "; i++) {\n" +
                "        if (i >= diameter) break;\n" +
                "        float samplePos = float(i) - currentRadius;\n" +
                "        vec2 offset = vec2(samplePos * uWidthOffset, samplePos * uHeightOffset);\n" +
                "        sampleTex = vec4(texture2D(uTexture, vTexCoord.st + offset));\n" +
                "        float weight = currentRadius + 1.0 - abs(samplePos);\n" +
                "        if (weight < 0.0) weight = 0.0;\n" +
                "        col += sampleTex.rgb * weight;\n" +
                "        weightSum += weight;\n" +
                "    }\n";
    }

    private static String loopBody(@Mode int mode) {
        switch (mode) {
            case MODE_BOX:
                return boxLoopBody();
            case MODE_GAUSSIAN:
                return gaussianLoopBody();
            case MODE_STACK:
                return stackLoopBody();
            default:
                return gaussianLoopBody();
        }
    }

    private static final String MAIN_TAIL =
            "    gl_FragColor = vec4(col / weightSum, sampleTex.a);\n" +
            "}\n";

    private GradientShaderUtil() {
    }

    /**
     * 生成"按 Y 渐变 radius"的片段着色器源码。
     */
    public static String getGradientFragmentCode(@Mode int mode) {
        return HEADER + MAIN_HEAD + loopBody(mode) + MAIN_TAIL;
    }
}