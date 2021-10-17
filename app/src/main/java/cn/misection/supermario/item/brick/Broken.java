package cn.misection.supermario.item.brick;

import android.graphics.Bitmap;
import cn.misection.supermario.sprite.Sprite;

import java.util.List;

/**
 * @author javaman
 * @date 2017/12/24
 */
public class Broken extends Sprite {
    public Broken(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    @Override
    public void logic() {
        if (isVisiable()) {
            nextFrame();
            move(0, -3);
            if (getFrameSequenceIndex() == 0) {
                setVisiable(false);
            }
        }
    }
}
