package cn.misection.supermario.item;

import android.graphics.Bitmap;

import java.util.List;


/**
 * @author javaman
 */
public class Coin extends ItemSprite {
    private int delay;

    public Coin(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    @Override
    public void logic() {
        if (isVisiable()) {
            if (delay++ > 5) {
                nextFrame();
                delay = 0;
            }

        }
    }
}
