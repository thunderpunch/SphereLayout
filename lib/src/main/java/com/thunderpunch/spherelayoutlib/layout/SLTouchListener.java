package com.thunderpunch.spherelayoutlib.layout;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by thunderpunch on 2017/2/23
 * Description:
 */

public class SLTouchListener implements View.OnTouchListener {
    private static final int MAX_ROTATE_DEGREE = 50;
    private static final int DURATION = 1800;
    private SphereLayout mSphereLayout;
    private AnimatorSet mAnimatorSet;
    private int mDownX, mDownY;
    private boolean mShouldRunAnimation;
    private ValueAnimator mFixAnimator;
    private ValueAnimator mRotateAnimator;
    private int mTouchSlop;
    private SpringInterpolator mInterpolator;
    private RotateState mState;

    public SLTouchListener(SphereLayout sphereLayout) {
        mSphereLayout = sphereLayout;
        mTouchSlop = ViewConfiguration.get(mSphereLayout.getContext()).getScaledTouchSlop();
        mInterpolator = new SpringInterpolator();
        mState = new RotateState();

        mRotateAnimator = new ValueAnimator();
        mRotateAnimator.setInterpolator(mInterpolator);
        mRotateAnimator.setIntValues(MAX_ROTATE_DEGREE, 0);
        mRotateAnimator.setDuration(DURATION);

        mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mState.rotateDegree = (Integer) animation.getAnimatedValue();
                mSphereLayout.rotate(mState.direction, mState.rotateDegree);
            }
        });

        mFixAnimator = new ValueAnimator();
        mFixAnimator.setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        boolean result = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mShouldRunAnimation = true;
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mDownX) >= mTouchSlop || Math.abs(event.getY() - mDownY) >= mTouchSlop) {
                    mShouldRunAnimation = false;
                } else {
                    result = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mShouldRunAnimation) {
                    final int upX = (int) event.getX();
                    final int upY = (int) event.getY();
                    if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
                        mAnimatorSet.cancel();
                    }
                    configFixAnimator(calculateDirection(upX, upY));
                    mAnimatorSet = new AnimatorSet();
                    mAnimatorSet.play(mRotateAnimator).after(mFixAnimator);
                    mAnimatorSet.start();
                }
                result = true;
                break;
        }
        return result;
    }

    /**
     * @param toDirection 手指按下的方向
     */
    private void configFixAnimator(int toDirection) {
        final int radius = mSphereLayout.getRadius();
        int currentDegree, currentDirection;
        mState.direction = mSphereLayout.makeDirectionWithinRange(mState.direction);
        //取当前翻转角度较大的那个方向
        if (mState.rotateDegree >= 0) {
            currentDegree = mState.rotateDegree;
            currentDirection = mState.direction;
        } else {
            currentDegree = -mState.rotateDegree;
            currentDirection = mState.direction + (mState.direction > 0 ? -180 : 180);
        }

        //获取当前翻转角度较大的方向和手指按下方向的差值
        int directionDiff = currentDirection - toDirection;

        //获取当前距离点击位置最近的圆边缘点的Z轴距离
        int distZ = (int) Math.abs(Math.sin(Math.toRadians(currentDegree))
                * Math.cos(Math.toRadians(directionDiff)) * radius);

        //获取当前点击位置的旋转角度
        int degree = (int) Math.toDegrees(Math.asin(distZ * 1.0f / radius))
                * (directionDiff > 90 || directionDiff < -90 ? -1 : 1);

        //根据旋转角度获取和总周期得出点击位置移动到最大角度的动画所需要的时间
        int fixAnimDuration = (int) ((MAX_ROTATE_DEGREE - degree) * 1.0f / MAX_ROTATE_DEGREE
                * DURATION / (1.0f / mInterpolator.factor) / 4);

        mFixAnimator.setIntValues(degree, MAX_ROTATE_DEGREE);
        mFixAnimator.setDuration(fixAnimDuration);
        mFixAnimator.addUpdateListener(new FixAnimatorUpdateListener(currentDegree, degree, currentDirection, toDirection));
    }

    /**
     * @param x 相对于自身左上角的x轴偏移量
     * @param y 相对于自身左上角的y轴偏移量
     * @return
     */
    private int calculateDirection(int x, int y) {
        y -= mSphereLayout.getCenterY();
        x -= mSphereLayout.getCenterX();
        return (int) Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * 持有翻转状态
     */
    private class RotateState {
        private int direction;
        private int rotateDegree;
    }


    private class FixAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        static final int DIRECTION_ACCEPTABLE_OFFSET = 5;//方向偏移量
        int fromDegree;//起始角度
        int fromDirection;//起始方向
        int directionDiff;

        FixAnimatorUpdateListener(int fromDegree, int toDegree, int fromDirection, int toDirection) {
            directionDiff = toDirection - fromDirection;

         /*   if (fromDegree < 10) {
                this.fromDegree = toDegree;
                this.fromDirection = toDirection;
                directionDiff = 0;
            } else {*/
                //确保差值在[-180,180)
                if (directionDiff < -180) {
                    directionDiff += 360;
                }
                if (directionDiff >= 180) {
                    directionDiff -= 360;
                }

                final int absDirDiff = Math.abs(directionDiff);

                //如果在方向偏移量范围内，那么不进行fixDirection动画
                if (absDirDiff >= 180 - DIRECTION_ACCEPTABLE_OFFSET
                        || absDirDiff <= DIRECTION_ACCEPTABLE_OFFSET) {
                    this.fromDegree = toDegree;
                    this.fromDirection = toDirection;
                    directionDiff = 0;
                } else {
                    this.fromDegree = fromDegree;
                    this.fromDirection = fromDirection;
                }
           // }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final float fraction = animation.getAnimatedFraction();
            final int direction = (int) (fromDirection + directionDiff * fraction);
            final int rotateDegree = (int) (fromDegree + (MAX_ROTATE_DEGREE - fromDegree) * fraction);
            mSphereLayout.rotate(direction, rotateDegree);
            mState.direction = direction;
            mState.rotateDegree = rotateDegree;
        }
    }

    /**
     * reference: http://inloop.github.io/interpolator/
     */
    private class SpringInterpolator implements Interpolator {
        public static final float factor = 0.4f;

        @Override
        public float getInterpolation(float input) {

            if (input >= 0.7) return 1;//避免振幅过小导致的卡顿感

            return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
        }
    }
}
