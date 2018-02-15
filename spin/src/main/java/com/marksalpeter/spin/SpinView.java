package com.marksalpeter.spin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;


/**
 * SpinView is a port of Spin.js to a native android view
 * Created by Mark Salpeter
 */
public class SpinView extends View {

    public static final String TAG = SpinView.class.getSimpleName();

    /**
     * DIRECTION_CLOCKWISE causes the spinner to spin in the clockwise direction
     */
    public static final int DIRECTION_CLOCKWISE = 1;

    /**
     * DIRECTION_COUNTERCLOCKWISE causes the spinner to spin in the counterclockwise direction
     */
    public static final int DIRECTION_COUNTERCLOCKWISE = -1;

    /**
     * sFPS is the frames per second of the animation
     */
    private static int sFPS = 50;

    private int mLines;
    private float mLength;
    private float mWidth;
    private float mRadius;
    private float mScale;
    private float mCorners;
    private int mColor;
    private int mFadeColor;
    private int mOpacity;
    private int mRotate;
    private int mDirection;
    private float mSpeed;
    private float mTrail;
    private Paint mColorPaint;
    private Paint mFadeColorPaint;
    private int mFrame;
    private HandlerThread mAnimationHandlerThread;
    private Handler mAnimationHandler;


    public SpinView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        DisplayMetrics dm = getResources().getDisplayMetrics();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpinView, defStyle, 0);
        mLines = a.getInt(R.styleable.SpinView_lines, 13);
        mLength = a.getDimension(R.styleable.SpinView_length, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, dm));
        mWidth = a.getDimension(R.styleable.SpinView_width, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, dm));
        mRadius = a.getDimension(R.styleable.SpinView_radius, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, dm));
        mScale = a.getFloat(R.styleable.SpinView_scale, 1);
        mCorners = a.getFloat(R.styleable.SpinView_corners, 1);
        mColor = a.getColor(R.styleable.SpinView_ccolor, Color.WHITE);
        mFadeColor = a.getColor(R.styleable.SpinView_fadeColor, Color.TRANSPARENT);
        mOpacity = (int)(255 * a.getFloat(R.styleable.SpinView_opacity, .25f));
        mRotate = a.getInt(R.styleable.SpinView_rotate, 0);
        mDirection = a.getInt(R.styleable.SpinView_direction, DIRECTION_CLOCKWISE);
        mSpeed = a.getFloat(R.styleable.SpinView_speed, 1);
        mTrail = ((float)a.getInt(R.styleable.SpinView_trail, 60) / 100f);
        a.recycle();

        // set up the view
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setColor(mColor);
        mFadeColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFadeColorPaint.setColor(mFadeColor);

        // create the animation handler thread
        mAnimationHandlerThread = new HandlerThread(TAG);
        mAnimationHandlerThread.start();
        mAnimationHandler = new Handler(mAnimationHandlerThread.getLooper());
    }

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // start the animation
        mAnimationHandler.post(new Runnable() {
            @Override public void run() {
            // stop the animation
            if (!isAttachedToWindow()) {
                mAnimationHandlerThread.quitSafely();
                return;
            }
            mFrame = (mFrame + 1) % Integer.MAX_VALUE;
            postInvalidate();
            mAnimationHandler.postDelayed(this, 1000/sFPS);
            }
        });
    }

    @Override protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.makeMeasureSpec((int)((mRadius + mLength) * 2 * mScale), MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec((int)((mRadius + mLength) * 2 * mScale), MeasureSpec.EXACTLY);
        setMeasuredDimension(width, height);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLines; i++) {
            canvas.save();
            canvas.translate(getWidth() / 2, getHeight() / 2);
            canvas.rotate((float)Math.floor(360 / mLines * i + mRotate));
            // draw the fade color
            if (Color.alpha(mFadeColor) > 0) {
                canvas.drawRoundRect(
                    mRadius * mScale,
                    (-mWidth/2) * mScale,
                    (mRadius + mLength) * mScale,
                    (mWidth/2) * mScale,
                    mCorners * mWidth * mScale,
                    mCorners * mWidth * mScale,
                    mFadeColorPaint
                );
            }
            // draw the main color over top
            mColorPaint.setAlpha(getLineOpacity(i, (((float)mFrame / sFPS) * mSpeed) % 1));
            canvas.drawRoundRect(
                    mRadius * mScale,
                    (-mWidth/2) * mScale,
                    (mRadius + mLength) * mScale,
                    (mWidth/2) * mScale,
                    mCorners * mWidth * mScale,
                    mCorners * mWidth * mScale,
                    mColorPaint
            );
            canvas.restore();
        }
    }

    private int getLineOpacity(int line, float animationPercentCompleted) {
        float linePercent = (float)(line + 1) / mLines;
        float diff = animationPercentCompleted - (linePercent * mDirection);

        if (diff < 0 || diff > 1) {
            diff += mDirection;
        }

        // opacity should start at 1, and approach opacity option as diff reaches trail percentage
        float opacityPercent = 1 - diff / mTrail;
        if (opacityPercent < 0) {
            return mOpacity;
        }

        return (int)((255 - mOpacity) * opacityPercent) + mOpacity;
    }

    public int getLines() {
        return mLines;
    }

    public void setLines(int mLines) {
        this.mLines = mLines;
    }

    public float getLength() {
        return mLength;
    }

    public void setLength(float mLength) {
        this.mLength = mLength;
    }

    public float getLineWidth() {
        return mWidth;
    }

    public void setLineWidth(float mWidth) {
        this.mWidth = mWidth;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public float getCorners() {
        return mCorners;
    }

    public void setCorners(float mCorners) {
        this.mCorners = mCorners;
    }

    public int getFadeColor() {
        return mFadeColor;
    }

    public void setFadeColor(int mFadeColor) {
        this.mFadeColor = mFadeColor;
    }

    public int getOpacity() {
        return mOpacity;
    }

    public void setOpacity(int mOpacity) {
        this.mOpacity = mOpacity;
    }

    public int getRotate() {
        return mRotate;
    }

    public void setRotate(int mRotate) {
        this.mRotate = mRotate;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int mDirection) {
        this.mDirection = mDirection;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
    }

    public float getTrail() {
        return mTrail;
    }

    public void setTrail(float mTrail) {
        this.mTrail = mTrail;
    }
}
