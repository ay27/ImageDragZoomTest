package com.example.ImageDragZoomTest;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Proudly to use Intellij IDEA.
 * Created by ay27 on 14-7-6.
 */
public class ChooseArea extends View {

    private Context _context;

    public ChooseArea(Context context) {
        super(context);
        _context = context;
    }

    public ChooseArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;
    }

    public ChooseArea(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _context = context;
    }

    // 这4个点就是代表着4个控制点，顺序是这样的：
    /**        0*********1
     *         *         *
     *         *         *
     *         *         *
     *         3*********2
     *
     */
    private Point[] p = null;
    public void setRegion(Point p0, Point p1, Point p2, Point p3) {
        p = new Point[4];
        p[0] = new Point(p0);
        p[1] = new Point(p1);
        p[2] = new Point(p2);
        p[3] = new Point(p3);
    }

    private int currentPoint = -1;

    private BottomImageView bottomView = null;

    public void setBottomView(BottomImageView bottomView) {
        this.bottomView = bottomView;
    }

    private void checkAndCorrect(MotionEvent event, int width, int height) {
        float x = event.getX(), y = event.getY();
        if (x<0)
            x = 0;
        else if (x>width)
            x = width;

        if (y<0)
            y = 0;
        else if (y>height)
            y = height;
        event.setLocation(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 对于超出边界的动作进行纠正， 这里的width和height在onDraw中已经初始化
        checkAndCorrect(event, width, height);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int index = findTheCoveredPoint(event.getX(), event.getY());
                if (index == -1) {
                    return false;
                }
                currentPoint = index;
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentPoint == -1)
                    return false;

                p[currentPoint].x = (int)event.getX();
                p[currentPoint].y = (int)event.getY();

                break;
            case MotionEvent.ACTION_UP:
                correctPoints();
                currentPoint = -1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                currentPoint = -1;
                return false;
            default:
                return false;
        }

        // 把touch动作投递到底部的bottomView去，用于控制ZoomArea区域的同步移动
        bottomView.perform(event);

        invalidate();

        return true;
    }

    private void correctPoints() {
        // 把各个点纠正到正常的位置
        if (p[0].x>p[1].x) exchange(0, 1);
        if (p[0].y>p[3].y) exchange(0, 3);
        if (p[3].x>p[2].x) exchange(3, 2);
        if (p[1].y>p[2].y) exchange(1, 2);

        // 计算每个角，角度过大的不能纠偏
        boolean flag = true;
        double edge1 = calculateEdge(p[0], p[1]);
        double edge2 = calculateEdge(p[2], p[1]);
        double edge3 = calculateEdge(p[3], p[2]);
        double edge4 = calculateEdge(p[3], p[0]);
        double diagonal1 = calculateEdge(p[2], p[0]);
        double diagonal2 = calculateEdge(p[3], p[1]);
        flag &= calculateAngle(edge1, edge4, diagonal2);
        flag &= calculateAngle(edge1, edge2, diagonal1);
        flag &= calculateAngle(edge2, edge3, diagonal2);
        flag &= calculateAngle(edge3, edge4, diagonal1);
        if (!flag) {
            Toast.makeText(_context, "无法裁剪", Toast.LENGTH_SHORT).show();
            // TODO : 当然这里还可以对全透明区的边框进行变色
        }
    }

    private double calculateEdge(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x-p2.x, 2)+Math.pow(p1.y-p2.y, 2));
    }

    /**
     * 使用余弦定理
     * cos(x) = (a^2+b^2-c^2) / 2*a*b
     * 其中p1对应的角度就是x
     * @return
     */
    private boolean calculateAngle(double a, double b, double c) {
        double cosX = (a*a+b*b-c*c) / (2*a*b);
        if (Math.abs(cosX)>0.707)
            return false;
        return true;
    }

    private void exchange(int p1, int p2) {
        Point temp = new Point(p[p1]);
        p[p1] = new Point(p[p2]);
        p[p2] = new Point(temp);
    }


    // 与控制点相邻80个像素点就选中，因为我的手机是1080p的分辨率，如果分辨率不同这里最好设置成不同的数值
    private static final int BOUND = 80;
    // 计算出当前手指触控的是哪个控制点
    private int findTheCoveredPoint(float x, float y) {
        if (p == null)
            return -1;
        for (int i = 0; i < 4; i++) {
            if (Math.sqrt((p[i].x - x) * (p[i].x - x) + (p[i].y - y) * (p[i].y - y))-BOUND <= 0) {
                return i;
            }
        }
        return -1;
    }

    private static Paint paintFillRegion;
    private static Paint paintDrawCircle;
    private static Paint paintDrawLine;
    static {
        // 半透明区的画笔
        paintFillRegion = new Paint();
        paintFillRegion.setStyle(Paint.Style.FILL);
        paintFillRegion.setStrokeWidth(10);
        paintFillRegion.setAlpha(80);

        // 四个角的圆圈的画笔
        paintDrawCircle = new Paint();
        paintDrawCircle.setStyle(Paint.Style.FILL);
        paintDrawCircle.setStrokeWidth(8);
        paintDrawCircle.setColor(Color.argb(100, 0, 221, 255));

        // 全透明区边框的画笔
        paintDrawLine = new Paint();
        paintDrawLine.setStyle(Paint.Style.STROKE);
        paintDrawLine.setStrokeWidth(1);
        paintDrawLine.setColor(Color.argb(100, 0, 221, 255));
    }


    private Point leftTop, rightTop, leftBottom, rightBottom;
    private int width, height;
    private boolean getParams = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!getParams) {
            if (bottomView == null)
                throw new IllegalStateException("you must set the bottom view !");

            // 这里根据底部的BottomView的位置和宽高来对表面的View进行设置，以便使得表面的这一层View与底部的View位置上完全重合
            this.setLayoutParams(new FrameLayout.LayoutParams(bottomView.getWidth(), bottomView.getHeight()));
            int[] tt = new int[2]; bottomView.getLocationInWindow(tt);
            WidgetController.setLayout(this, tt[0], 0);
            //*************************************************************************************************

            width = bottomView.getWidth();
            height = bottomView.getHeight();

            // 这里的几个Point的位置，是相对于这个View的，所以这里的x, y都设置为 0
            int x = 0, y = 0;
            leftTop = new Point(x, y);
            leftBottom = new Point(x, height);
            rightTop = new Point(x+width, y);
            rightBottom = new Point(x+width, y+height);

            getParams = true;
        }

        // 填充4个半透明区域
        Path[] paths = new Path[4];
        paths[0] = getPath(leftTop, rightTop, p[1], p[0]);
        paths[1] = getPath(rightTop, rightBottom, p[2], p[1]);
        paths[2] = getPath(rightBottom, leftBottom, p[3], p[2]);
        paths[3] = getPath(leftBottom, leftTop, p[0], p[3]);
        for (int i = 0; i < 4; i++) {
            canvas.drawPath(paths[i], paintFillRegion);
        }

        // 画出中间全透明区域的边框
        Path path = getPath(p[0], p[1], p[2], p[3]);
        canvas.drawPath(path, paintDrawLine);

        // 画出四个角的小圈
        for (int i = 0; i < 4; i++) {
            canvas.drawCircle(p[i].x, p[i].y, 50, paintDrawCircle);
        }
    }

    private Path getPath(Point... points) {
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        path.close();

        return path;
    }
}
