package cn.misection.supermario.item;

import android.graphics.Bitmap;

import java.util.List;

/**
 * @author javaman
 * @date 2017/12/1
 */
public class Star extends ItemSprite {


    public Star(Bitmap bitmap) {
        super(bitmap);
    }

    public Star(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    @Override
    public void logic() {
        super.logic();
        if (isVisiable()) {
            nextFrame();
        }
    }
}
