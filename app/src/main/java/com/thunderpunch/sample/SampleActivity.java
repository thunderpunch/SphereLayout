package com.thunderpunch.sample;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.CompoundButton;

import com.thunderpunch.sample.view.ElectricView;
import com.thunderpunch.spherelayout.R;
import com.thunderpunch.spherelayoutlib.layout.SLTouchListener;
import com.thunderpunch.spherelayoutlib.layout.SphereLayout;


/**
 * Created by thunderpunch on 2017/4/6
 * Description:
 */

public class SampleActivity extends AppCompatActivity {
    private SphereLayout sl;
    private View fabFront, fabBack;
    private ValueAnimator animator = new ValueAnimator();
    private ElectricView evL, evR;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        sl = (SphereLayout) findViewById(R.id.sl0);
        sl.setOnTouchListener(new SLTouchListener(sl));
        sl.rotate(-30, 30);//静态示例

        fabFront = findViewById(R.id.fab_front);
        fabBack = findViewById(R.id.fab_back);

        configReverseAnimator();//配置反转动画

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!animator.isRunning()) {
                    animator.start();
                }
            }
        };
        fabFront.setOnClickListener(clickListener);
        fabBack.setOnClickListener(clickListener);

        ViewCompat.setScaleX(fabBack, 0);
        ViewCompat.setScaleY(fabBack, 0);


        ((AppCompatCheckBox) findViewById(R.id.cb_hideBack)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sl.hideBack(isChecked);
                    }
                });

        evR = (ElectricView) findViewById(R.id.electric_r);
        evL = (ElectricView) findViewById(R.id.electric_l);
        evR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evR.shock();
                evL.shock();
            }
        });

        evL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evR.shock();
                evL.shock();
            }
        });
    }

    private void configReverseAnimator() {
        animator = ValueAnimator.ofInt(0, 180);
        animator.setInterpolator(new OvershootInterpolator(2));
        animator.setDuration(1400);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sl.rotate(0, (Integer) animation.getAnimatedValue());
                float fraction = animation.getAnimatedFraction();
                if (fraction > 0.5) {
                    ViewCompat.setScaleX(sl.isReverse() ? fabFront : fabBack, (fraction - 0.5f) / 0.5f);
                    ViewCompat.setScaleY(sl.isReverse() ? fabFront : fabBack, (fraction - 0.5f) / 0.5f);
                } else {
                    fraction = Math.min(fraction, 0.4f);
                    ViewCompat.setScaleX(sl.isReverse() ? fabBack : fabFront, 1 - fraction / 0.4f);
                    ViewCompat.setScaleY(sl.isReverse() ? fabBack : fabFront, 1 - fraction / 0.4f);
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                sl.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sl.reverse(!sl.isReverse());//在动画结束后设置状态为反转
                sl.setEnabled(true);
                final boolean isReverse = sl.isReverse();
                fabFront.setEnabled(!isReverse);
                fabBack.setEnabled(isReverse);
                evL.setEnabled(!isReverse);
                evR.setEnabled(!isReverse);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

}
