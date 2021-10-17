package cn.misection.supermario.item.brick;


import android.graphics.Bitmap;

import java.util.List;

/**
 * @author javaman
 * @date 2017/12/25
 */
public class Pipe extends Brick {
    private boolean isTransfer;

    public Pipe(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    public boolean isTransfer() {
        return isTransfer;
    }

    public void setTransfer(boolean transfer) {
        isTransfer = transfer;
    }

    @Override
    public void logic() {
    }
}
