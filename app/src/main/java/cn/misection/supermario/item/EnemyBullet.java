package cn.misection.supermario.item;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author javaman
 * @date 2017/12/10
 */
public class EnemyBullet extends Bullet {
    public EnemyBullet(Bitmap bitmap) {
        super(bitmap);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isMirror()) {
            canvas.save();
            //翻转画布 相当于翻转人物
            canvas.scale(-1, 1, getX() + getWidth() / 2, getY() + getHeight() / 2);
            super.draw(canvas);
            canvas.restore();
        } else {
            super.draw(canvas);

        }
    }

}
