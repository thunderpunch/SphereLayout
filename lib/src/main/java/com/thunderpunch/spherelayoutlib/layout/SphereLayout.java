package com.thunderpunch.spherelayoutlib.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;


import com.thunderpunch.spherelayoutlib.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by thunderpunch on 2017/2/20
 */

public class SphereLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int mRadius;
    private Point mCenter;
    private Camera mCamera;
    private Matrix mMatrix;
    private float mDensity;
    private int mRotateDegree;
    private int mDirection;
    private float[] mValues = new float[9];

    private boolean mReverse;
    private boolean mHideBack;
    private ArrayList<View> mPositiveChildren, mNegativeChildren;
    private int mOrientation;

    private OnRotateListener mRotateListener;

    /**
     * @see #rotateDependsOn(SphereLayout)
     */
    private SphereLayout mDependency;
    /**
     * 自身翻转中心到依赖视图的翻转中心的偏移量
     *
     * @see #rotateDependsOn(SphereLayout)
     */
    private int[] mDependencyOffset;
    private boolean mCheckedDependencyOffset;

    public SphereLayout(Context context) {
        this(context, null);
    }


    public SphereLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SphereLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SphereLayout);
        mRadius = a.getDimensionPixelSize(R.styleable.SphereLayout_radius, 0);//球体半径
        mOrientation = a.getInt(R.styleable.SphereLayout_snapOrientation, HORIZONTAL);//depth为负的子视图是否进行横向或纵向的翻转
        mHideBack = a.getBoolean(R.styleable.SphereLayout_hideBack, false);//是否隐藏处在背面的布局
        a.recycle();
        mCenter = new Point();
        mCamera = new Camera();
        mDependencyOffset = new int[2];
        //mCamera.setLocation(0, 0, -20);
        mMatrix = new Matrix();
        mPositiveChildren = new ArrayList<>();//depth为正的childview集合
        mNegativeChildren = new ArrayList<>();//depth为负的childview集合
        mDensity = getResources().getDisplayMetrics().density;
    }

    /**
     * @param hideBack 是否隐藏背面
     */
    public void hideBack(boolean hideBack) {
        if (mHideBack != hideBack) {
            mHideBack = hideBack;
            postInvalidate();
        }
    }

    /**
     * @param radius 球体半径
     */
    public void setRadius(int radius) {
        mRadius = radius;
        requestLayout();
    }

    public int getRadius() {
        return mRadius;
    }

    /**
     * @param isReverse 是否翻转
     */
    public void reverse(boolean isReverse) {
        if (isReverse != mReverse) {
            mReverse = isReverse;
            mRotateDegree = 0;
            invalidate();
        }
    }

    public boolean isReverse() {
        return mReverse;
    }

    public int getCenterX() {
        return mCenter.x;
    }

    public int getCenterY() {
        return mCenter.y;
    }

    /**
     * @param direction    翻转方向 (-180，180] 以布局中心为原点，向量与x轴正方向的夹角，顺加逆减
     * @param rotateDegree 翻转角度 (-180，180]
     */
    public void rotate(int direction, int rotateDegree) {
        mDirection = makeDirectionWithinRange(direction);
        mRotateDegree = makeRoatateDegreeWithinRange(rotateDegree);
        postInvalidate();
        if (mRotateListener != null) {
            mRotateListener.onRotate(direction, rotateDegree);
        }
    }

    /**
     * @param sphereLayout 翻转依赖
     */

    public void rotateDependsOn(SphereLayout sphereLayout) {
        mDependency = sphereLayout;
        mCheckedDependencyOffset = false;
        mDependency.mRotateListener = new OnRotateListener() {
            @Override
            public void onRotate(int direction, int degree) {
                rotate(direction, degree);
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRadius == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mRadius << 1, MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mRadius << 1, MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec)
                , MeasureSpec.getSize(heightMeasureSpec));
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.fitBounds) {
                    lp.depth = Math.max(Math.min(lp.depth, mRadius), -mRadius);
                    lp.bottomMargin = lp.leftMargin = lp.rightMargin = lp.topMargin = 0;
                    final int boundsSize = (int) Math.max(getMinimumBoundsSize(),
                            2 * Math.sqrt(mRadius * mRadius - lp.depth * lp.depth));
                    child.measure(MeasureSpec.makeMeasureSpec(boundsSize, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(boundsSize, MeasureSpec.EXACTLY));
                } else {
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenter.x = w >> 1;
        mCenter.y = h >> 1;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mPositiveChildren.clear();
        mNegativeChildren.clear();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();
                int childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                int childTop = (b - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
                if (lp.depth >= 0) {
                    mPositiveChildren.add(child);
                } else {
                    mNegativeChildren.add(child);
                }
            }
        }
        final Comparator<View> comparator = new Comparator<View>() {
            @Override
            public int compare(View o1, View o2) {
                Integer d0 = Math.abs(((LayoutParams) o1.getLayoutParams()).depth);
                Integer d1 = Math.abs(((LayoutParams) o2.getLayoutParams()).depth);
                return d0.compareTo(d1);
            }
        };
        //按照depth绝对值排列绘制顺序
        Collections.sort(mPositiveChildren, comparator);
        Collections.sort(mNegativeChildren, comparator);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mRadius == 0) {
            super.dispatchDraw(canvas);
            return;
        }
        if (mDependency != null && !mCheckedDependencyOffset) {
            int[] location = new int[2];
            int[] dependencyLocation = new int[2];
            getLocationOnScreen(location);
            mDependency.getLocationOnScreen(dependencyLocation);
            mDependencyOffset[0] = dependencyLocation[0] + mDependency.getCenterX() -
                    (location[0] + getCenterX());
            mDependencyOffset[1] = dependencyLocation[1] + mDependency.getCenterY() -
                    (location[1] + getCenterY());
            mCheckedDependencyOffset = true;
        }
        if (!mReverse) {
            if (mRotateDegree > 90 || mRotateDegree <= -90) {
                drawChildren(canvas, mNegativeChildren, mPositiveChildren);
            } else {
                drawChildren(canvas, mPositiveChildren, mNegativeChildren);
            }
        } else {
            if (mRotateDegree >= 90 || mRotateDegree < -90) {
                drawChildren(canvas, mPositiveChildren, mNegativeChildren);
            } else {
                drawChildren(canvas, mNegativeChildren, mPositiveChildren);
            }
        }
    }

    private void drawChildren(Canvas canvas, List<View> frontChildren, List<View> backChildren) {
        if (!mHideBack) {
            final int c0 = backChildren.size();
            for (int i = c0 - 1; i >= 0; i--) {
                drawChild(canvas, backChildren.get(i));
            }
        }
        final int c1 = frontChildren.size();
        for (int i = 0; i < c1; i++) {
            drawChild(canvas, frontChildren.get(i));
        }
    }

    private void drawChild(Canvas canvas, View child) {
        final long drawingTime = getDrawingTime();
        final LayoutParams p = (LayoutParams) child.getLayoutParams();
        final int depth = mReverse ? -p.depth : p.depth;
        mCamera.save();
        mCamera.rotateY(mRotateDegree);
        mCamera.translate(0, 0, -depth);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        //fix camera bug
        mMatrix.getValues(mValues);
        mValues[6] = mValues[6] / mDensity;
        mValues[7] = mValues[7] / mDensity;

        mMatrix.setValues(mValues);
        mMatrix.postRotate(mDirection, 0, 0);
        mMatrix.postTranslate(mCenter.x + mDependencyOffset[0], mCenter.y + mDependencyOffset[1]);
        mMatrix.preRotate(-mDirection, 0, 0);
        mMatrix.preTranslate(-mCenter.x - mDependencyOffset[0], -mCenter.y - mDependencyOffset[1]);
        if (depth < 0) {
            if (mOrientation == HORIZONTAL) {
                mMatrix.preScale(-1, 1, mCenter.x, mCenter.y);
            } else if (mOrientation == VERTICAL) {
                mMatrix.preScale(1, -1, mCenter.x, mCenter.y);
            }
        }
        canvas.save();
        canvas.concat(mMatrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }


    public int makeRoatateDegreeWithinRange(int degree) {
        while (degree <= -180) {
            degree += 360;
        }
        while (degree > 180) {
            degree -= 360;
        }
        return degree;
    }

    public int makeDirectionWithinRange(int direction) {
        while (direction <= -180) {
            direction += 360;
        }
        while (direction > 180) {
            direction -= 360;
        }
        return direction;
    }

    /**
     * @return 当fitBounds为true时, 该childview的最小宽高
     */
    public int getMinimumBoundsSize() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static class LayoutParams extends MarginLayoutParams {
        /**
         * 视图在z轴的位置
         * 该z轴以指向观察者的方向为正方向，与{@link Camera}的z轴方向相反
         * {@link SphereLayout}的直系子视图必须设置depth属性
         */
        public int depth;
        /**
         * 为true时，子视图会宽高适配当前所处{@link #depth}层级的圆的直径且无视自身margin属性
         */
        public boolean fitBounds;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a =
                    c.obtainStyledAttributes(attrs, R.styleable.SphereLayout_Layout);
            depth = a.getLayoutDimension(R.styleable.SphereLayout_Layout_layout_depth, "layout_depth");
            fitBounds = a.getBoolean(R.styleable.SphereLayout_Layout_layout_fitBounds, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(LayoutParams p) {
            super(p);
            depth = p.depth;
            fitBounds = p.fitBounds;
        }

        public LayoutParams(MarginLayoutParams p) {
            super(p);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SphereSavedState ss = new SphereSavedState(superState);
        ss.radius = mRadius;
        ss.orientation = mOrientation;
        ss.hideBack = mHideBack;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SphereSavedState ss = (SphereSavedState) state;
        mRadius = ss.radius;
        mOrientation = ss.orientation;
        mHideBack = ss.hideBack;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    static class SphereSavedState extends BaseSavedState {
        int radius;
        int orientation;
        boolean hideBack;

        SphereSavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SphereSavedState(Parcel in) {
            super(in);
            radius = in.readInt();
            orientation = in.readInt();
            hideBack = in.readInt() == 0 ? false : true;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(radius);
            out.writeInt(orientation);
            out.writeInt(hideBack ? 1 : 0);
        }

        public static final Creator<SphereSavedState> CREATOR
                = new Creator<SphereSavedState>() {
            public SphereSavedState createFromParcel(Parcel in) {
                return new SphereSavedState(in);
            }

            public SphereSavedState[] newArray(int size) {
                return new SphereSavedState[size];
            }
        };
    }

    private interface OnRotateListener {
        void onRotate(int direction, int degree);
    }
}
