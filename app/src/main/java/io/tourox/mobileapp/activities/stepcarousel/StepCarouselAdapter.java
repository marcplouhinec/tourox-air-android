package io.tourox.mobileapp.activities.stepcarousel;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import io.tourox.mobileapp.R;

/**
 * Adapter for the step carousel.
 * Created by Marc Plouhinec on 12/03/16.
 */
public class StepCarouselAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

    private static final List<Integer> STEP_DESCRIPTION_RESOURCE_IDS = Arrays.asList(R.string.step1_description, R.string.step2_description, R.string.step3_description);
    private final static int STEP_CAROUSEL_PAGES = STEP_DESCRIPTION_RESOURCE_IDS.size();
    public final static float STEP_CAROUSEL_BIG_SCALE = 1.0f;
    private final static float STEP_CAROUSEL_SMALL_SCALE = 0.6f;
    private final static float STEP_CAROUSEL_DIFF_SCALE = STEP_CAROUSEL_BIG_SCALE - STEP_CAROUSEL_SMALL_SCALE;

    private final Context context;
    private final FragmentManager fragmentManager;
    private final TextView stepDescriptionTextView;
    private final ViewPager stepCarouselViewPager;

    public StepCarouselAdapter(Context context, TextView stepDescriptionTextView, ViewPager stepCarouselViewPager, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.stepDescriptionTextView = stepDescriptionTextView;
        this.stepCarouselViewPager = stepCarouselViewPager;
    }

    @Override
    public Fragment getItem(int position) {
        // make the first pager bigger than others
        float scale;
        if (position == 0)
            scale = STEP_CAROUSEL_BIG_SCALE;
        else
            scale = STEP_CAROUSEL_SMALL_SCALE;

        position = position % STEP_CAROUSEL_PAGES;
        return StepCarouselFragment.newInstance(context, position, scale);
    }

    @Override
    public int getCount() {
        return STEP_CAROUSEL_PAGES;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset >= 0f && positionOffset <= 1f) {
            StepCarouselLinearLayout stepCarouselLinearLayout = getRootView(position);
            if (stepCarouselLinearLayout != null)
                stepCarouselLinearLayout.setScaleBoth(STEP_CAROUSEL_BIG_SCALE - STEP_CAROUSEL_DIFF_SCALE * positionOffset);

            if (position < STEP_CAROUSEL_PAGES) {
                stepCarouselLinearLayout = getRootView(position + 1);
                if (stepCarouselLinearLayout != null)
                    stepCarouselLinearLayout.setScaleBoth(STEP_CAROUSEL_BIG_SCALE - STEP_CAROUSEL_DIFF_SCALE * (1f - positionOffset));
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        stepDescriptionTextView.setText(STEP_DESCRIPTION_RESOURCE_IDS.get(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private StepCarouselLinearLayout getRootView(int position) {
        String fragmentTag = this.getFragmentTag(position);
        Fragment fragmentByTag = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragmentByTag == null)
            return null;

        View view = fragmentByTag.getView();
        return view == null ? null : (StepCarouselLinearLayout) view.findViewById(R.id.root);
    }

    private String getFragmentTag(int position) {
        return "android:switcher:" + stepCarouselViewPager.getId() + ":" + position;
    }
}
