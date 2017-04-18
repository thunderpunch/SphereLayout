package com.thunderpunch.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.thunderpunch.spherelayout.R;

import java.util.Random;

/**
 * Created by thunderpunch on 2017/4/11
 * Description:
 */

public class ElectricView extends View {
    private int mDegree;
    private int mDegreeOffset = 30;
    private int mStartMargin;
    private Random mRandom;
    private Path mPath;
    private int mMaxOffset;
    private Paint mPaint;
    private int mRemainCount, mElectricCount;
    private Interpolator mInterpolator = new AccelerateInterpolator();
    private boolean mStartFromRight = true;

    public ElectricView(Context context) {
        this(context, null);
    }

    public ElectricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElectricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPath = new Path();
        mRandom = new Random();
        mPaint = new Paint();
        mPaint.setColor(0xffffff8e);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mMaxOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getContext().getResources().getDisplayMetrics());
        mStartMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                getContext().getResources().getDisplayMetrics());

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ElectricView);
        mElectricCount = typedArray.getInt(R.styleable.ElectricView_electricCount, 1);
        mStartFromRight = typedArray.getInt(R.styleable.ElectricView_startFrom, 0) == 1;
        mDegree = typedArray.getInt(R.styleable.ElectricView_degree, 0);
        typedArray.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mRemainCount == 0) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();

        final int w = getWidth();
        final int h = getHeight();
        if (mStartFromRight) {
            canvas.rotate(180);
            canvas.translate(-w, -h);
        }

        canvas.translate(0, (h >> 1) - mStartMargin);
        final float percent = mInterpolator.getInterpolation(mRemainCount / 5);
        int degreeOffset = (int) (mDegreeOffset * percent);
        canvas.rotate(mDegree - degreeOffset);
        for (int i = 0; i < mElectricCount; i++) {
            int currentX = 5 + mRemainCount;
            mPath.rewind();
            int j = 0;
            while (currentX < w * 0.4 + w * 0.6 * percent || (j <= 1)) {
                currentX += mRandom.nextDouble() * 0.5 * w;
                mPath.lineTo(currentX, (float) ((mRandom.nextDouble() * 2 - 1) * mMaxOffset));
                j++;
            }
            canvas.drawPath(mPath, mPaint);
            if (i != mElectricCount - 1) {
                canvas.translate(0, 2 * mStartMargin / (mElectricCount - 1));
                canvas.rotate(2 * degreeOffset / (mElectricCount - 1));
            }
        }
        mRemainCount--;
        canvas.restore();
        postInvalidateDelayed(70);
    }

    public void shock() {
        mRemainCount = 5;
        postInvalidate();
    }


}
