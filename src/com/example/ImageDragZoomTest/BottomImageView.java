package com.example.ImageDragZoomTest;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Proudly to use Intellij IDEA.
 * Created by ay27 on 14-7-5.
 */
public class BottomImageView extends ImageView {

    private Context _context;
    private ImageView zoomView;

    public BottomImageView(Context context) {
        super(context);
        _context = context;
    }

    public BottomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
    }

    public BottomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
    }

    public void setZoomView(ImageView imageView) {
        this.zoomView = imageView;
        // 这个ScaleType一定要是Matrix
        zoomView.setScaleType(ScaleType.MATRIX);
    }


    // 根据TopView投递过来的动作进行图片的放大区的跟踪
    public boolean perform(MotionEvent event) {

        if (event.getX()>this.getWidth() || event.getY()>this.getHeight()
                || event.getX()<0 || event.getY()<0)
            return false;

        // 这里的event获取到的 x,y 已经是基于这个view的偏移了
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:

                // 下面的一堆计算就是为了把当前触控点的位置与放大区的中心位置对齐
                // ZoomView的ScaleType必须设置成Matrix，这样才能保证它是放大的。放大系数默认是2
                // 下面的方法是这样的：先把event的坐标对应到图片上去，再把图片上的坐标转换成ZoomView的中心点上。

                int[] x0y0 = new int[2]; getLocationInWindow(x0y0);
                int imageWidth = getDrawable().getBounds().width();
                int imageHeight = getDrawable().getBounds().height();

                int thisWidth = this.getWidth();
                int thisHeight = this.getHeight();

                int zoomViewWidth = zoomView.getWidth();
                int zoomViewHeight = zoomView.getHeight();
                int[] zoomViewX0Y0 = new int[2]; zoomView.getLocationInWindow(zoomViewX0Y0);

                int[] imageXY = xy2ImageXY((int)event.getX(), (int)event.getY(), thisWidth, thisHeight, imageWidth, imageHeight);

                Matrix mm1 = imageXY2ViewXY(imageXY[0], imageXY[1], zoomViewWidth, zoomViewHeight);
                zoomView.setImageMatrix(mm1);
                break;
            default:
                break;
        }

        return true;
    }

    private Matrix imageXY2ViewXY(int xi, int yi, int imageViewWidth, int imageViewHeight) {
        Matrix mm = new Matrix();
        float dx = xi-(imageViewWidth/2);
        float dy = yi - (imageViewHeight/2);
        mm.postTranslate(-dx, -dy);

        return mm;
    }

    private int[] xy2ImageXY(int x, int y, int viewWidth, int viewHeight, int imageWidth, int imageHeight) {
        int[] xy = new int[2];
        xy[0] = (x)*imageWidth/viewWidth;
        xy[1] = (y)*imageHeight/viewHeight;

        return xy;
    }
}
