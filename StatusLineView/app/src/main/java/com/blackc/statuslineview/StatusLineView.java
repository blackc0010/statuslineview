package com.blackc.statuslineview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
public class StatusLineView extends View {
    /**
     * 定义画笔
     */
    private Paint mPaint;
    /**
     * 文字画笔
     */
    private Paint painText;
    /**
     * 圆角矩形画笔
     */
    private Paint paintRec;
    /**
     * 延长线专用画笔
     */
    private Paint exPaint;
    /*
    字体大小
     */
    private float mTextSize;

    private Paint txtPaint;
    /**
     * 圆形半径
     */
    private float mRadius;
    /**
     * 线的粗度
     */
    private float mLineWidth;
    /**
     * 完成的颜色
     */
    private int mCompleteColor;
    /**
     * 未完成的颜色
     */
    private int mNoCompleteColor;
    /**
     * 当前执行到的步数 从0开始计算
     */
    private float mStep = 0;
    /**
     * 传入的文字的list
     */
    private List<String> pointStringList;
    /**
     * 每个点的X坐标
     */
    private Float[] pointXArray;
    /**
     * 文字高度
     */
    private float mTextHeight;

    /**
     * 自定义的节点点击事件
     */
    OnTimeLineStepClickListener onTimeLineStepClickListener;

    /**
     * 存放圆心的列表
     */
    List<CircleCenter> circleCenterList;

    /**
     * 点与点之间的阶段长度
     */
    float sectionLength;

    /**
     * 计算保留两位小数用的工具类
     */
    BigDecimal bigDecimal;

    public StatusLineView(Context context) {
        super(context);
    }

    public StatusLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public StatusLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    /**
     * 初始化自定义属性的
     *
     * @param attrs attrs.xml里定义的属性
     */
    private void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.StatusLineView);
            mTextSize = typedArray.getDimension(R.styleable.StatusLineView_textSize, 20);
            mLineWidth = typedArray.getDimension(R.styleable.StatusLineView_lineWidth, 10);
            mCompleteColor = typedArray.getColor(R.styleable.StatusLineView_CompleteColor, getResources().getColor(R.color.color_yellow));
            mNoCompleteColor = typedArray.getColor(R.styleable.StatusLineView_NormalColor, getResources().getColor(R.color.color_status_normal));
            //切记回收防止内存泄漏
            typedArray.recycle();
        }
    }

    /**
     * 初始化画笔、字体高度、传入步数文字内容的列表以及圆心的列表
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setStrokeWidth(mLineWidth);
        exPaint = new Paint();

        painText = new Paint();
        painText.setAntiAlias(true);
        painText.setTextSize(mTextSize);
        painText.setColor(Color.WHITE);
        painText.setStrokeWidth(mLineWidth);

        paintRec = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintRec.setAntiAlias(true);

        mTextHeight = painText.descent() - painText.ascent();

        pointStringList = new ArrayList<>();
        pointStringList.add("等待");
        pointStringList.add("建仓");
        pointStringList.add("满仓");
        pointStringList.add("平仓");
        circleCenterList = new ArrayList<>();

    }

    /**
     * 自定义的测量方式 主要处理的是wrap_content模式下的一些默认值
     * 宽度是不管如何设置都是占用的整个屏幕的宽度，这里可以自行调整
     * 高度是两倍字体的高度加上圆的直径 作为默认高度，如果有需求也可以自行调整
     *
     * @param widthMeasureSpec  宽度尺
     * @param heightMeasureSpec 高度尺
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();

        int width = measureWidth(minimumWidth, widthMeasureSpec);
        int height = measureHeight(minimumHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }


    /**
     * 测量宽度
     *
     * @param defaultWidth 默认宽度
     * @param measureSpec  宽度尺
     * @return 计算后的宽度
     */
    private int measureWidth(int defaultWidth, int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);


        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth = getWidth();
                break;
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
                break;
            default:
                break;
        }

        return defaultWidth;
    }

    /**
     * 高度测量
     *
     * @param defaultHeight 默认高度
     * @param measureSpec   高度尺
     * @return 计算后的高度
     */
    private int measureHeight(int defaultHeight, int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight = (int) (2 * mTextHeight) + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                break;

            default:
                break;
        }
        return defaultHeight;
    }

    /**
     * 绘制思路：
     * 1.确定第一步骤的圆形和文字的宽度，哪个宽就用哪个作为第一步绘制的左边距离
     * 2.确定最后步骤的圆形和文字的宽度，那个宽就用哪个最为最后一个步骤绘制的右边距离
     * 3.用自定义组件整体的宽度减去左边距和右边距再加上两个半径的距离，剩下的就是要平分剩下点的距离
     * 4.平分剩下的距离，画圆后连线。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //先判断传入的步数是不是大于0，如果传入一个空数据的List则不调用显示步数的方法
        if (pointStringList.size() > 0) {
            //先设置成完成后的颜色，因为绘制过程肯定是先绘制完成后的颜色且传入的步数至少有一步是完成的
            mPaint.setColor(mCompleteColor);
            paintRec.setColor(mCompleteColor);
            initViewsPos();
            //当前绘制到那一步
            int currentStep;
            //是否绘制半截进程的线 如2.3就绘制第三步到第四步的百分之30的完成线
            boolean isDrawExtrasLine = false;
            //循环X轴的点进行绘制
            for (int i = 0; i < pointXArray.length; i++) {
                currentStep = i;
                //当前的步骤大于设置的步骤了就需要将线和圆改成未完成色,如果要绘制未完成色就说明可能要绘制延长线，那就将绘制延长线的开关打开。
                if (currentStep > mStep) {
                    mPaint.setColor(mNoCompleteColor);
                    paintRec.setColor(mNoCompleteColor);
                    isDrawExtrasLine = true;
                }
                drawCircleAndText(canvas, pointStringList.get(i), pointXArray[i]);
                //当i大于1说明有起码两个点时才有必要画线
                if (i >= 1) {
                    drawLine(canvas, pointStringList.get(i), pointXArray[i - 1], pointXArray[i]);
                }
            }
            //绘制延长线，主要是处理步数为类似2.3时，这百分之30的第三个圆到第四个圆之间的完成颜色的线
            if (isDrawExtrasLine) {
                bigDecimal = new BigDecimal(mStep);
                float mStepTwoPoint = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                //取小数点后的数字
                float littleCountFloat = mStepTwoPoint - (int) mStepTwoPoint;
//                drawExtrasLine(canvas, littleCountFloat, pointStringList.get(0));
            }
        } else {
            mPaint.setColor(Color.RED);
            String noStepsWarnText = "传入步数为0，请重新传入数据";
            canvas.drawText(noStepsWarnText, (getWidth() / 2) - mPaint.measureText(noStepsWarnText) / 2, getHeight(), mPaint);
        }
    }

    /**
     * 初始化组件的位置
     * 主要是计算各个圆形的位置
     * 并将其圆心位置记录到数组中去
     */
    private void initViewsPos() {
        //传入的步数
        int pointCount = pointStringList.size();
        pointXArray = new Float[pointCount];
        //如果文字的长度大于半径 就用文字的长度来计算 开始的点和结束的点同理
        float startDistance = mPaint.measureText(pointStringList.get(0));
        float endDistance = mPaint.measureText(pointStringList.get(pointStringList.size() - 1));
        //每段线的距离 宽度减去左留白和右留白后除以点数减一即可 帮助思考的图例：*---*---*---*
        sectionLength = (getWidth() - startDistance - endDistance) / (pointCount - 1);
        //循环将起始点、结束点和中间的点的X的值放入数组中
        for (int i = 0; i < pointCount; i++) {
            if (i == 0) {
                pointXArray[i] = startDistance;
            } else if (i == pointCount - 1) {
                pointXArray[i] = getWidth() - endDistance;
            } else {
                pointXArray[i] = startDistance + sectionLength * i;
            }
        }
    }

    /**
     * 绘制圆形和下面的文字
     *
     * @param canvas 画布
     * @param text   文字内容
     * @param x      x轴的值
     */
    private void drawCircleAndText(Canvas canvas, String text, float x) {

        Log.e("mTextHeight:", mTextHeight + "");

        RectF rec = new RectF(x - mPaint.measureText(text), getHeight() / 2 - mTextHeight / 2 - 5
                , x + mPaint.measureText(text), getHeight() / 2 + mTextHeight / 2 + 5);

        canvas.drawRoundRect(rec, mPaint.measureText(text) * 2, mPaint.measureText(text) * 2, paintRec);

        circleCenterList.add(new CircleCenter(x, getHeight() / 2));

        painText.setColor(Color.WHITE);
        canvas.drawText(text, x - mPaint.measureText(text) / 2, getHeight() / 2 + mTextHeight / 3, painText);

    }

    /**
     * 绘制两圆之间的连接线 总数为点数-1
     *
     * @param canvas 画布
     * @param startX 线开始的X轴的值
     * @param endX   线结束的X轴的值
     */
    private void drawLine(Canvas canvas, String text, float startX, float endX) {
        mPaint.setStrokeWidth(10);
        canvas.drawLine(startX + mPaint.measureText(text), getHeight() / 2,
                endX - mPaint.measureText(text), getHeight() / 2, mPaint);
    }

    /**
     * 绘制多余的小数线段
     * 这里的绘制原理是 计算百分比并找到完成步数的值 并在其之后 绘制一条完成颜色的线 该线的长度是一个线段乘以完成的百分比
     *
     * @param canvas  画布
     * @param percent 完成的百分比
     */
    private void drawExtrasLine(Canvas canvas, float percent, String text) {
        //找到最大步数的圆形的X轴
        float maxGreenPointX = pointXArray[(int) mStep];
        //设置延长线专用Paint的颜色和宽度
        exPaint.setColor(mCompleteColor);
        exPaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(maxGreenPointX + mPaint.measureText(text), getHeight() / 2,
                maxGreenPointX - mPaint.measureText(text) + sectionLength * percent, getHeight() / 2, exPaint);
    }


    /**
     * 传参的方法，对外开放
     *
     * @param mpointStringArray 传入的步骤的数组(这里之所以用数组是因为可以使用@Size注解)
     * @param step              传入的完成步骤数
     */
    public void setPointStrings(@Size(min = 2) String[] mpointStringArray, @FloatRange(from = 0.0) float step) {
        if (mpointStringArray.length == 0) {
            pointStringList.clear();
            circleCenterList.clear();
            mStep = 0;
        } else {
            pointStringList = Arrays.asList(mpointStringArray);
            mStep = Math.min(step, pointStringList.size());
            invalidate();
        }
    }

    /**
     * 设置完成颜色
     *
     * @param color
     */
    public void setCompleteColor(int color) {
        this.mCompleteColor = color;
        invalidate();
    }

    /**
     * 动态设置步数的方法
     *
     * @param step 步数
     */
    public void setStep(@FloatRange(from = 0.0) float step) {
        mStep = Math.min(step, pointStringList.size());
        invalidate();
    }

    /**
     * 动态设置步数的方法
     *
     * @param step 步数
     */
    public void setStep(@FloatRange(from = 0.0) float step, boolean isOpen) {
        mStep = Math.min(step, pointStringList.size());
        if (isOpen) {
            mCompleteColor = getResources().getColor(R.color.color_yellow);
        } else {
            mCompleteColor = getResources().getColor(R.color.color_status_close);
        }

        invalidate();
    }

    /**
     * 手势方法重写 返回true来消费此方法
     * 原理是当手指抬起时 计算点击的点是不是在步骤圆形中的某一个范围内，如果在就触发自定义点击事件
     *
     * @param event 点击事件
     * @return true 消费事件
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (x + getLeft() < getRight() && y + getTop() < getBottom()) {
                    int clickStep = isInTheCircles(x, y);
                    if (clickStep >= 0 && onTimeLineStepClickListener != null) {
                        onTimeLineStepClickListener.onStepClick(clickStep);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 判断点击位置是否在多个圆中的某一个内
     *
     * @param x 点击事件的x轴
     * @param y 点击事件的y轴
     * @return boolean是否在某圆内:是 true,否 false
     */
    private int isInTheCircles(float x, float y) {
        int clickStep = -1;
        for (int i = 0; i < circleCenterList.size(); i++) {
            CircleCenter circleCenter = circleCenterList.get(i);
            //点击位置x坐标与圆心的x坐标的距离
            float distanceX = Math.abs(circleCenter.getX() - x);
            //点击位置y坐标与圆心的y坐标的距离
            float distanceY = Math.abs(circleCenter.getY() - y);
            //点击位置与圆心的直线距离
            int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            //如果点击位置与圆心的距离小于等于圆的半径，证明点击位置有在圆内
            if (distanceZ <= mRadius) {
                clickStep = i;
                break;
            }
        }
        return clickStep;
    }

    /**
     * 设置接口的方法 对外提供调用
     */
    public void setOnTimeLineStepChangeListener(OnTimeLineStepClickListener onTimeLineStepClickListener) {
        this.onTimeLineStepClickListener = onTimeLineStepClickListener;
    }

    /**
     * 点击事件的接口定义
     */
    public interface OnTimeLineStepClickListener {

        void onStepClick(float step);
    }

    /**
     * 圆心的内部实体类
     * 主要为是判断点击事件是否在圆上提供的Bean
     */
    class CircleCenter {
        float x;
        float y;

        private CircleCenter(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        private float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
