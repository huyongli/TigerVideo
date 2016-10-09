package cn.ittiger.video.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载动画
 * @author: laohu on 2016/7/3
 * @site: http://ittiger.cn
 */
public class LoadingView extends View {
    /**
     * 球由大变小动画的持续时间
     */
    private static final int DURATION = 800;
    /**
     * 球在动画变化过程中的最大半径
     */
    private static final int MAX_RADIUS = 20;
    /**
     * 球在动画变化过程中的最小半径
     */
    private static final int MIN_RADIUS = 10;
    /**
     * 球1的背景色
     */
    private static final int BALL_COLOR_2 = Color.parseColor("#ff33b5e5");
    /**
     * 球2的背景色
     */
    private static final int BALL_COLOR_1 = Color.RED;
    /**
     * 球的动画变化器
     */
    private BallAnimator mBallAnimator;
    private boolean mIsAnimating = false;

    public LoadingView(Context context) {

        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mBallAnimator = new BallAnimator(DURATION, MIN_RADIUS, MAX_RADIUS);

        Ball ball1 = new Ball(BALL_COLOR_1, MIN_RADIUS);
        ball1.setPosition(MAX_RADIUS, MAX_RADIUS);

        Ball ball2 = new Ball(BALL_COLOR_2, MIN_RADIUS);
        ball2.setPosition(MIN_RADIUS * 4 + MAX_RADIUS, MAX_RADIUS);

        mBallAnimator.addBall(ball1);
        mBallAnimator.addBall(ball2);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        for(Ball ball : mBallAnimator.getBalls()) {
            ball.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_RADIUS * 2 + MIN_RADIUS * 2 + MAX_RADIUS * 2, MAX_RADIUS * 2);
        } else if(widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_RADIUS * 2 + MIN_RADIUS * 2 + MAX_RADIUS * 2, heightSpecSize);
        } else if(heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, MAX_RADIUS * 2);
        }
    }

    public void start() {

        mBallAnimator.start();
        mIsAnimating = true;
    }

    public void stop() {

        if(mIsAnimating) {
            mBallAnimator.stop();
            mIsAnimating = false;
        }
    }

    public class Ball {

        private Paint mPaint;
        private float mInitRadius;
        private float mRadius;
        private Point mPosition;
        private int mColor;

        public Ball(int color, float radius) {

            mColor = color;
            mInitRadius = mRadius = radius;

            mPaint = new Paint();
            mPaint.setColor(mColor);
            mPaint.setAntiAlias(true);
            mPosition = new Point();
        }

        public void setRadius(float radius) {

            this.mRadius = radius;
        }

        public void setPosition(int x, int y) {

            this.mPosition.set(x, y);
        }

        public void draw(Canvas canvas) {

            canvas.drawCircle(mPosition.x, mPosition.y, mRadius, mPaint);
        }

        public void reset() {

            this.mRadius = mInitRadius;
        }
    }

    public class BallAnimator {
        private int mDuration;
        private float mMinRadius;
        private float mMaxRadius;
        private List<Ball> mBalls;
        private List<ValueAnimator> mBallAnimators;

        public BallAnimator(int duration, float minRadius, float maxRadius) {

            mDuration = duration;
            mMinRadius = minRadius;
            mMaxRadius = maxRadius;
            mBalls = new ArrayList<>();
            mBallAnimators = new ArrayList<>();
        }

        public void start() {

            mBallAnimators.clear();
            for(int i = 0; i < mBalls.size(); i++) {
                createBallAnimator(mBalls.get(i), mDuration * i);
            }
        }

        public void stop() {

            for(ValueAnimator valueAnimator : mBallAnimators) {
                valueAnimator.cancel();
            }
            for(Ball ball : mBalls) {
                ball.reset();
            }
        }

        public void restart() {

            stop();
            start();
        }

        private void createBallAnimator(Ball ball, int startDelay) {

            ValueAnimator valueAnimator = new ValueAnimator();
            valueAnimator.setFloatValues(mMinRadius, mMaxRadius);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.setDuration(mDuration);
            valueAnimator.addUpdateListener(new BallUpdateListener(ball));
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setStartDelay(startDelay);
            mBallAnimators.add(valueAnimator);
            valueAnimator.start();
        }

        public void addBall(Ball ball) {

            this.mBalls.add(ball);
        }

        public List<Ball> getBalls() {

            return mBalls;
        }
    }

    private class BallUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private Ball ball;

        public BallUpdateListener(Ball ball) {

            this.ball = ball;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            float value = (float) animation.getAnimatedValue();
            ball.setRadius(value);
            invalidate();
        }
    }
}
