package io.tourox.mobileapp.activities.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

import io.tourox.mobileapp.R;

/**
 * View used to display a radial gradient background.
 * Thanks to http://stackoverflow.com/a/28822349
 * Created by Marc Plouhinec on 06/03/16.
 */
public class RadialGradientView extends View {
    private final int endColor;
    private final int startColor;
    private final float gradientRadiusWidthPercent;
    private final float centerY;
    private final float centerX;
    private final Paint paint;

    public RadialGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadialGradientView, 0, 0);

        startColor = a.getColor(R.styleable.RadialGradientView_startColor, Color.RED);
        endColor = a.getColor(R.styleable.RadialGradientView_endColor, Color.BLACK);
        gradientRadiusWidthPercent = a.getFloat(R.styleable.RadialGradientView_gradientRadiusWidthPercent, 1);
        centerX = a.getFloat(R.styleable.RadialGradientView_centerX, .5f);
        centerY = a.getFloat(R.styleable.RadialGradientView_centerY, .5f);

        a.recycle();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        RadialGradient gradient = new RadialGradient(
                parentWidth * centerX,
                parentHeight * centerY,
                parentWidth * gradientRadiusWidthPercent,
                new int[]{startColor, endColor},
                null,
                android.graphics.Shader.TileMode.CLAMP);

        paint.setDither(true);
        paint.setShader(gradient);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

}
