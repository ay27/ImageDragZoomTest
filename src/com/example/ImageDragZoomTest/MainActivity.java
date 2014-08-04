package com.example.ImageDragZoomTest;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.example.ImageDragZoomText.R;

/**
 * Proudly to use Intellij IDEA.
 * Created by ay27 on 14-7-7.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);


        BottomImageView bottomImageView = (BottomImageView)findViewById(R.id.layout2_bottomView);
        ImageView zoomArea = (ImageView)findViewById(R.id.layout2_imageAbove);
        ChooseArea chooseArea = (ChooseArea)findViewById(R.id.layout2_topView);

        bottomImageView.setZoomView(zoomArea);
        chooseArea.setBottomView(bottomImageView);
        chooseArea.setRegion(new Point(200, 400), new Point(600, 400), new Point(600, 900), new Point(200, 900));

    }
}
