package cn.ittiger.player.demo.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import cn.ittiger.player.ui.FullScreenGestureView;

/**
 * @author: ylhu
 * @time: 2017/12/13
 */

public class MyFullScreenGestureView extends FullScreenGestureView {

    public MyFullScreenGestureView(@NonNull Context context) {

        super(context);
    }

    public MyFullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
    }

    public MyFullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyFullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
