package com.antitheft.alarm.view;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.R;
import com.antitheft.alarm.utils.SystemUtils;

/**
 * 自定义数字键盘
 * Created by Jacky on 19/10/19.
 */
public class NumericKeyboard extends View {
    private int number_color = 0;
    private int circle_border_color = 0;
    private float number_size = 0;
    private float stroke_width = 0;
    private int circle_pressed_color = 0;
    private int screen_width = 0;// 屏幕的宽度
    private float first_x = 0;// 绘制1的x坐标
    private float first_y = 0;// 绘制1的y坐标
    final static private float cx_offset = 10;
    final static private float tx_offset = 23;
    final static private float ty_offset = 25;
    final static private float y_offset = 40;
    final static private float y_pos = 15;
    private float radius = 60;
    private float[] xs = new float[3];//声明数组保存每一列的圆心横坐标
    private float[] ys = new float[4];//声明数组保存每一排的圆心纵坐标
    private float circle_x, circle_y;//点击处的圆心坐标
    private int number = -1;//点击的数字
    private boolean isNonumeric = false;
    private OnNumberClick onNumberClick;//数字点击事件
    private boolean showFingprintFlag = true;

    /*
     * 判断刷新数据
     * -1 不进行数据刷新
     * 0  按下刷新
     * 1  弹起刷新
     */
    private int type = -1;

    /**
     * 构造方法
     *
     * @param context
     */
    public NumericKeyboard(Context context) {
        super(context);
        initData(context);// 初始化数据
    }

    public NumericKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumericKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumericKeyboard, defStyle, 0);
        circle_border_color = a.getColor(R.styleable.NumericKeyboard_circle_border_color, Color.parseColor("#7dc5eb"));
        circle_pressed_color = a.getColor(R.styleable.NumericKeyboard_circle_pressed_color, Color.parseColor("#8a8a8a"));
        number_color = a.getColor(R.styleable.NumericKeyboard_number_color, Color.parseColor("#8a8a8a"));
        stroke_width = a.getDimensionPixelSize(R.styleable.NumericKeyboard_stroke_width, 8);
        number_size = a.getDimensionPixelSize(R.styleable.NumericKeyboard_number_size, 120);
        radius = a.getDimensionPixelSize(R.styleable.NumericKeyboard_circle_radius, 90);
        a.recycle();
        initData(context);// 初始化数据
    }

    public void setShowFingprintFlag(boolean showFingprintFlag) {
        this.showFingprintFlag = showFingprintFlag;
    }

    /**
     * 设置数字点击事件
     *
     * @param onNumberClick
     */
    public void setOnNumberClick(OnNumberClick onNumberClick) {
        this.onNumberClick = onNumberClick;
    }

    // 初始化数据
    private void initData(Context context) {
        // 获取屏幕的宽度
        screen_width = SystemUtils.getSystemDisplay(context)[0];
        // 获取绘制1的x坐标
        first_x = screen_width / 4;
        // 获取绘制1的y坐标
        first_y = (SystemUtils.getSystemDisplay(context)[1] - SystemUtils.getSystemDisplay(context)[1] / 3) / 4;
        //添加每一排的横坐标
        xs[0] = first_x + cx_offset;
        xs[1] = first_x * 2 + cx_offset;
        xs[2] = first_x * 3 + cx_offset;
        //添加每一列的纵坐标
        ys[0] = y_offset + first_y - y_pos;
        ys[1] = y_offset + first_y + first_x - y_pos;
        ys[2] = y_offset + first_y + first_x * 2 - y_pos;
        ys[3] = y_offset + first_y + first_x * 3 - y_pos;
    }

    private void drawCircle(Canvas canvas, Paint paint) {
        //为每一个数字绘制一个圆
        paint.setColor(circle_border_color);//设置画笔颜色
        //设置绘制空心圆
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(stroke_width);
        //依次绘制第一排的圆
        canvas.drawCircle(first_x + cx_offset, y_offset + first_y - y_pos, radius, paint);
        canvas.drawCircle(first_x * 2 + cx_offset, y_offset + first_y - y_pos, radius, paint);
        canvas.drawCircle(first_x * 3 + cx_offset, y_offset + first_y - y_pos, radius, paint);
        //依次绘制第2排的圆
        canvas.drawCircle(first_x + cx_offset, y_offset + first_y + first_x - y_pos, radius, paint);
        canvas.drawCircle(first_x * 2 + cx_offset, y_offset + first_y + first_x - y_pos, radius, paint);
        canvas.drawCircle(first_x * 3 + cx_offset, y_offset + first_y + first_x - y_pos, radius, paint);
        //依次绘制第3排的圆
        canvas.drawCircle(first_x + cx_offset, y_offset + first_y + first_x * 2 - y_pos, radius, paint);
        canvas.drawCircle(first_x * 2 + cx_offset, y_offset + first_y + first_x * 2 - y_pos, radius, paint);
        canvas.drawCircle(first_x * 3 + cx_offset, y_offset + first_y + first_x * 2 - y_pos, radius, paint);
        //绘制最后一个圆
        canvas.drawCircle(first_x * 2 + cx_offset, y_offset + first_y + first_x * 3 - y_pos, radius, paint);
        canvas.drawCircle(first_x * 3 + cx_offset, y_offset + first_y + first_x * 3 - y_pos, radius, paint);
    }

    private void drawNumber(Canvas canvas, Paint paint) {
        // 绘制文本,注意是从坐标开始往上绘制
        // 这里较难的就是算坐标
        // 绘制第一排1,2,3
        paint.setColor(number_color);// 设置画笔颜色
        paint.setTextSize(number_size);// 设置字体大小
        canvas.drawText("1", first_x - tx_offset, y_offset + first_y + ty_offset, paint);
        canvas.drawText("2", first_x * 2 - tx_offset, y_offset + first_y + ty_offset, paint);
        canvas.drawText("3", first_x * 3 - tx_offset, y_offset + first_y + ty_offset, paint);
        // 绘制第2排4,5,6
        canvas.drawText("4", first_x - tx_offset, y_offset + first_y + first_x + ty_offset, paint);
        canvas.drawText("5", first_x * 2 - tx_offset, y_offset + first_y + first_x + ty_offset, paint);
        canvas.drawText("6", first_x * 3 - tx_offset, y_offset + first_y + first_x + ty_offset, paint);
        // 绘制第3排7,8,9
        canvas.drawText("7", first_x - tx_offset, y_offset + first_y + first_x * 2 + ty_offset, paint);
        canvas.drawText("8", first_x * 2 - tx_offset, y_offset + first_y + first_x * 2 + ty_offset, paint);
        canvas.drawText("9", first_x * 3 - tx_offset, y_offset + first_y + first_x * 2 + ty_offset, paint);
        // 绘制第4排0
        canvas.drawText("0", first_x * 2 - tx_offset, y_offset + first_y + first_x * 3 + ty_offset, paint);
        // 绘制第4排X
        canvas.drawText("X", first_x * 3 - tx_offset, y_offset + first_y + first_x * 3 + ty_offset, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 创建画笔对象
        Paint paint = new Paint();
        paint.setAntiAlias(true);//设置抗锯齿
        drawCircle(canvas, paint);
        drawNumber(canvas, paint);
        //判断是否点击数字(点击数字产生的渐变效果)
        if (showFingprintFlag) return;
        if (circle_x > 0 && circle_y > 0) {
            if (type == 0) {//按下刷新
                paint.setColor(circle_pressed_color);//设置画笔颜色
                paint.setStyle(Paint.Style.FILL_AND_STROKE);//按下的时候绘制实心圆
                canvas.drawCircle(circle_x, circle_y, radius, paint);//绘制圆
            } else if (type == 1) {//弹起刷新
                paint.setColor(Color.parseColor("#7dc5eb"));//设置画笔颜色
                paint.setStyle(Paint.Style.STROKE);//弹起的时候再绘制空心圆
                canvas.drawCircle(circle_x, circle_y, radius, paint);//绘制圆
                //绘制完成后,重置
                circle_x = 0;
                circle_y = 0;
            }
        }
    }

    /**
     * 获取触摸点击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showFingprintFlag) return true;
        //事件判断
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                //判断点击的坐标位置
                float x = event.getX();//按下时的X坐标
                float y = event.getY();//按下时的Y坐标
                //判断点击的是哪一个数字圆
                handleDown(x, y);
                return true;
            case MotionEvent.ACTION_UP://弹起
                type = 1;//弹起刷新
                invalidate();//刷新界面
                //返回点击的数字
                if (onNumberClick != null) {
                    if (number != -1) {
                        onNumberClick.onNumberReturn(number);
                    }
                    if (isNonumeric) {
                        onNumberClick.onDelete();
                    }
                }
                setDefault();//恢复默认
                //发送辅助事件
                sendAccessEvent(R.string.numeric_keyboard_up);
                return true;
            case MotionEvent.ACTION_CANCEL://取消
                //恢复默认值
                setDefault();
                return true;
        }
        return false;
    }

    /*
     * 恢复默认值
     */
    private void setDefault() {
        circle_x = 0;
        circle_y = 0;
        type = -1;
        number = -1;
        sendAccessEvent(R.string.numeric_keyboard_cancel);
    }

    /*
     * 设置辅助功能描述
     */
    private void sendAccessEvent(int resId) {
        //设置描述
        setContentDescription(AppContext.getContext().getString(resId));
        //发送辅助事件
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
//        setContentDescription(null);
    }

    /*
     * 判断点击的是哪一个数字圆
     */
    private void handleDown(float x, float y) {
        //判断点击的是那一列的数据
        if (xs[0] - radius <= x && x <= xs[0] + radius) {//第一列
            //获取点击处的圆心横坐标
            circle_x = xs[0];
            //判断点击的是哪一排
            if (ys[0] - radius <= y && ys[0] + radius >= y) {//第1排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[0];
                number = 1;//设置点击的数字
                isNonumeric = false;
            } else if (ys[1] - radius <= y && ys[1] + radius >= y) {//第2排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[1];
                number = 4;//设置点击的数字
                isNonumeric = false;
            } else if (ys[2] - radius <= y && ys[2] + radius >= y) {//第3排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[2];
                number = 7;//设置点击的数字
                isNonumeric = false;
            }
        } else if (xs[1] - radius <= x && x <= xs[1] + radius) {//第2列
            //获取点击处的圆心横坐标
            circle_x = xs[1];
            //判断点击的是哪一排
            if (ys[0] - radius <= y && ys[0] + radius >= y) {//第1排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[0];
                number = 2;//设置点击的数字
                isNonumeric = false;
            } else if (ys[1] - radius <= y && ys[1] + radius >= y) {//第2排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[1];
                number = 5;//设置点击的数字
                isNonumeric = false;
            } else if (ys[2] - radius <= y && ys[2] + radius >= y) {//第3排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[2];
                number = 8;//设置点击的数字
                isNonumeric = false;
            } else if (ys[3] - radius <= y && ys[3] + radius >= y) {//第4排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[3];
                number = 0;//设置点击的数字
                isNonumeric = false;
            }
        } else if (xs[2] - radius <= x && x <= xs[2] + radius) {//第3列
            //获取点击处的圆心横坐标
            circle_x = xs[2];
            //判断点击的是哪一排
            if (ys[0] - radius <= y && ys[0] + radius >= y) {//第1排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[0];
                number = 3;//设置点击的数字
                isNonumeric = false;
            } else if (ys[1] - radius <= y && ys[1] + radius >= y) {//第2排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[1];
                number = 6;//设置点击的数字
                isNonumeric = false;
            } else if (ys[2] - radius <= y && ys[2] + radius >= y) {//第3排
                //获取点击的数字圆的圆心纵坐标
                circle_y = ys[2];
                number = 9;//设置点击的数字
                isNonumeric = false;
            } else if (ys[3] - radius <= y && ys[3] + radius >= y) {//第4排
                circle_y = ys[3];
                isNonumeric = true;
            }
        }
        sendAccessEvent(R.string.numeric_keyboard_down);
        type = 0;//按下刷新
        //绘制点击时的背景圆
        invalidate();
    }

    /**
     * 数字点击事件
     */
    public interface OnNumberClick {
        /**
         * 返回点击的数字
         *
         * @param number
         */
        public void onNumberReturn(int number);
        public void onDelete();
    }
}
