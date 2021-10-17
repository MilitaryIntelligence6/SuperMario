package cn.misection.supermario.item;

import android.graphics.Bitmap;

import java.util.List;


/**
 * @author javaman
 * @date 2017/11/29
 */
public class Flower extends ItemSprite {

    private int delay2;

    public Flower(Bitmap bitmap) {
        super(bitmap);
    }

    public Flower(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    @Override
    public void logic() {
        super.logic();
        if (delay2++ > 10) {
            nextFrame();
            delay2 = 0;
        }
    }
}
