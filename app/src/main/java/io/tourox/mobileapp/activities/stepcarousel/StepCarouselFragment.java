package io.tourox.mobileapp.activities.stepcarousel;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.tourox.mobileapp.R;

/**
 * Adaptation of https://github.com/mrleolink/SimpleInfiniteCarousel/blob/master/src/net/leolink/android/simpleinfinitecarousel/MyFragment.java.
 * Created by Marc Plouhinec on 05/03/16.
 */
public class StepCarouselFragment extends Fragment {

    public static Fragment newInstance(Context context, int pos, float scale) {
        Bundle bundle = new Bundle();
        bundle.putFloat("scale", scale);
        bundle.putInt("pos", pos);
        return Fragment.instantiate(context, StepCarouselFragment.class.getName(), bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.step_image_layout, container, false);
        StepCarouselLinearLayout rootLayout = (StepCarouselLinearLayout) linearLayout.findViewById(R.id.root);
        rootLayout.setScaleBoth(this.getArguments().getFloat("scale"));
        rootLayout.setStep(this.getArguments().getInt("pos"));

        return linearLayout;
    }
}
