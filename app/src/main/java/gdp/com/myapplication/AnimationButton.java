package gdp.com.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * Created by doupan on 9/6/15.
 */
public class AnimationButton extends ImageView {

    private static final int ANIMATION_DURATION = 200;
    private Drawable mCheckDrawable;
    private Drawable mUnCheckDrawable;
    private int mCheckColor;
    private int mUnCheckColor;
    private boolean mIsCheck;
    private int mAnimationColor;
    private boolean mIsAnimating;
    private AnimatorSet mAnimator;

    public AnimationButton(Context context) {
        super(context);
    }

    public AnimationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimationButton);
        if (a != null) {
            mCheckColor = a.getColor(R.styleable.AnimationButton_checkBackgroundColor, 0);
            mUnCheckColor = a.getColor(R.styleable.AnimationButton_unCheckBackgroundColor, 0);
            int checkId = a.getResourceId(R.styleable.AnimationButton_checkDrawable, 0);
            int unCheckId = a.getResourceId(R.styleable.AnimationButton_unCheckDrawable, 0);
            mCheckDrawable = getResources().getDrawable(checkId);
            mUnCheckDrawable = getResources().getDrawable(unCheckId);
            mIsCheck = a.getBoolean(R.styleable.AnimationButton_checked, false);
        }
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleState();
            }
        });
        setChecked(mIsCheck);
    }

    /**
     *
     * @return current checked state
     */
    public boolean getChecked() {
        return mIsCheck;
    }

    /**
     * set the current check state
     * @param check
     */
    public void setChecked(boolean check) {
        mIsCheck = check;
        mAnimationColor = mIsCheck ? mCheckColor : mUnCheckColor;
        setImageDrawable(mIsCheck ? mCheckDrawable : mUnCheckDrawable);
    }

    /**
     * need to call this method when the view is out of screen.
     * for example: when the view is recycled in ListView
     */
    public void reset() {
        mIsCheck = false;
        mIsAnimating = false;
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private void toggleState() {
        if (mIsAnimating) {
            return;
        }
        mIsAnimating = true;

        if (mAnimator == null) {
            mAnimator = new AnimatorSet();
            mAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsCheck = !mIsCheck;
                    mIsAnimating = false;
                    mAnimationColor = mIsCheck ? mCheckColor : mUnCheckColor;
                    setImageDrawable(mIsCheck ? mCheckDrawable : mUnCheckDrawable);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        ObjectAnimator firstRotate = ObjectAnimator.ofFloat(this, "rotation",0f, 90f);
        firstRotate.setDuration(ANIMATION_DURATION / 2);
        firstRotate.setInterpolator(new AccelerateInterpolator());
        firstRotate.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setImageDrawable(mIsCheck ? mUnCheckDrawable : mCheckDrawable);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator secondRotate = ObjectAnimator.ofFloat(this, "rotation", 270f, 360f);
        secondRotate.setDuration(ANIMATION_DURATION / 2);
        secondRotate.setInterpolator(new DecelerateInterpolator());
        AnimatorSet rotateAnimator = new AnimatorSet();
        rotateAnimator.playSequentially(firstRotate, secondRotate);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.3f, 1f);
        alphaAnimator.setInterpolator(new LinearInterpolator());
        alphaAnimator.setDuration(ANIMATION_DURATION);

        int fromColor = mIsCheck ? mCheckColor : mUnCheckColor;
        int toColor = mIsCheck ? mUnCheckColor : mCheckColor;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.setInterpolator(new LinearInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimationColor = (Integer) animation.getAnimatedValue();
                invalidate();
            }
        });
        colorAnimation.setDuration(ANIMATION_DURATION);

        mAnimator.playTogether(rotateAnimator, alphaAnimator, colorAnimation);
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        int w = getWidth(), h = getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(mAnimationColor);

        int r = Math.min(w / 2, h / 2);
        canvas.drawCircle(w / 2, h / 2, r, paint);
        canvas.drawBitmap(b, (w - b.getWidth()) / 2, (h - b.getHeight()) / 2, paint);
    }

}
