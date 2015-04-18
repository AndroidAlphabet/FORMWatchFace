/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.nurik.roman.formwatchface.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class FormClockView extends View {
    private FormClockRenderer mHourMinRenderer;
    private FormClockRenderer mSecondsRenderer;

    private int mWidth, mHeight;

    public FormClockView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FormClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public FormClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FormClockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FormClockView,
                defStyleAttr, defStyleRes);

        // General config
        FormClockRenderer.Options options = new FormClockRenderer.Options();
        options.textSize = a.getDimension(R.styleable.FormClockView_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
                        getResources().getDisplayMetrics()));
        options.charSpacing = a.getDimension(R.styleable.FormClockView_charSpacing,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 6,
                        getResources().getDisplayMetrics()));

        options.glyphAnimAverageDelay = 500;
        options.glyphAnimDuration = 2000;

        // Set up renderers
        mHourMinRenderer = new FormClockRenderer(options, null);

        options = new FormClockRenderer.Options(options);
        options.onlySeconds = true;
        options.textSize /= 2;
        options.glyphAnimAverageDelay = 0;
        options.glyphAnimDuration = 750;

        mSecondsRenderer = new FormClockRenderer(options, null);

        setColors(
                a.getColor(R.styleable.FormClockView_color1, 0xff000000),
                a.getColor(R.styleable.FormClockView_color2, 0xff888888),
                a.getColor(R.styleable.FormClockView_color3, 0xffcccccc));

        a.recycle();
    }

    public void setColors(int color1, int color2, int color3) {
        FormClockRenderer.ClockPaints paints = new FormClockRenderer.ClockPaints();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(color1);
        paints.fills[0] = paint;

        paint = new Paint(paint);
        paint.setColor(color2);
        paints.fills[1] = paint;

        paint = new Paint(paint);
        paint.setColor(color3);
        paints.fills[2] = paint;

        mHourMinRenderer.setPaints(paints);
        mSecondsRenderer.setPaints(paints);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mHourMinRenderer.updateTime();
        PointF hourMinSize = mHourMinRenderer.measure();
        mHourMinRenderer.draw(canvas,
                (mWidth - hourMinSize.x) / 2,
                (mHeight - hourMinSize.y) / 2,
                false);

        mSecondsRenderer.updateTime();
        PointF secondsSize = mSecondsRenderer.measure();
        mSecondsRenderer.draw(canvas,
                (mWidth + hourMinSize.x) / 2 - secondsSize.x,
                (mHeight + hourMinSize.y) / 2
                        + TypedValue.applyDimension(5, TypedValue.COMPLEX_UNIT_DIP,
                        getResources().getDisplayMetrics()),
                false);

        long timeToNextSecondsAnimation = mSecondsRenderer.timeToNextAnimation();
        long timeToNextHourMinAnimation = mHourMinRenderer.timeToNextAnimation();
        if (timeToNextHourMinAnimation < 0 || timeToNextSecondsAnimation < 0) {
            postInvalidateOnAnimation();
        } else {
            postInvalidateDelayed(Math.min(timeToNextHourMinAnimation, timeToNextSecondsAnimation));
        }
    }
}
