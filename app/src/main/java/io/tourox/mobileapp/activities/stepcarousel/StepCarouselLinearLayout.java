package io.tourox.mobileapp.activities.stepcarousel;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

import io.tourox.mobileapp.R;

/**
 * Adaptation of https://github.com/mrleolink/SimpleInfiniteCarousel/blob/master/src/net/leolink/android/simpleinfinitecarousel/MyLinearLayout.java
 * Created by Marc Plouhinec on 05/03/16.
 */
public class StepCarouselLinearLayout extends LinearLayout {
    private final List<Integer> stepDrawableIds = Arrays.asList(R.drawable.step1, R.drawable.step2);
    private float scale = StepCarouselAdapter.STEP_CAROUSEL_BIG_SCALE;

    public StepCarouselLinearLayout(Context context) {
        super(context);
    }

    public StepCarouselLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScaleBoth(float scale) {
        this.scale = scale;
        this.invalidate();
    }

    public void setStep(int step) {
        ImageView imageView = (ImageView) findViewById(R.id.stepImageView);
        imageView.setImageResource(stepDrawableIds.get(step));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(scale, scale, this.getWidth() / 2, this.getHeight() / 2);
        super.onDraw(canvas);
    }
}
