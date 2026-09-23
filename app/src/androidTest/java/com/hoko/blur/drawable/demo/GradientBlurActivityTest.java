package com.hoko.blur.drawable.demo;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hoko.blur.view.GradientBlurFrameLayout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link GradientBlurActivity} 的 smoke 测试。
 *
 * 验证：
 * <ul>
 *   <li>Activity 能正常启动不崩</li>
 *   <li>布局中的 {@link GradientBlurFrameLayout} 被正确 inflate</li>
 *   <li>Activity 配置的默认模糊参数生效</li>
 * </ul>
 */
@RunWith(AndroidJUnit4.class)
public class GradientBlurActivityTest {

    @Rule
    public ActivityTestRule<GradientBlurActivity> activityRule =
            new ActivityTestRule<>(GradientBlurActivity.class);

    @Test
    public void activity_launchesSuccessfully() {
        GradientBlurActivity activity = activityRule.getActivity();
        assertNotNull(activity);
        assertNotNull(activity.findViewById(R.id.gradient_frameLayout));
        assertNotNull(activity.findViewById(R.id.animate_button));
    }

    @Test
    public void gradientFrameLayout_hasConfiguredDrawable() {
        GradientBlurActivity activity = activityRule.getActivity();
        GradientBlurFrameLayout layout =
                activity.findViewById(R.id.gradient_frameLayout);
        assertNotNull(layout);
        assertNotNull(layout.getBlurDrawable());
        assertEquals(20, layout.getBlurDrawable().radius());
    }
}