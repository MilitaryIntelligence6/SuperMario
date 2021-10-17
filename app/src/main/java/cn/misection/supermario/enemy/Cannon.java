package cn.misection.supermario.enemy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import cn.misection.supermario.audio.MySoundPool;
import cn.misection.supermario.item.EnemyBullet;
import cn.misection.supermario.sprite.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * 敌人类-大炮
 *
 * @author javaman
 */
public class Cannon extends Enemy {

    private List<Sprite> bullets;
    private long delay2;
    private MySoundPool soundPool;

    public Cannon(int width, int height, List<Bitmap> bitmaps, MySoundPool soundPool) {
        super(width, height, bitmaps);
        this.soundPool = soundPool;
        bullets = new ArrayList<>();
    }

    public List<Sprite> getBullets() {
        return bullets;
    }

    public void setBullets(List<Sprite> bullets) {
        this.bullets = bullets;
    }

    @Override
    public void logic() {
        if (isJumping()) {
            move(0, mSpeedY++);
        }
        if (delay2++ > 90) {
            fire();
            delay2 = 0;
        }
        if (bullets != null) {
            for (int i = 0; i < bullets.size(); i++) {
                bullets.get(i).logic();
            }
        }
    }

    public void fire() {
        if (bullets != null) {
            for (Sprite bullet : bullets) {
                EnemyBullet enemyBullet = (EnemyBullet) bullet;
                if (!enemyBullet.isVisiable()) {
                    soundPool.play(soundPool.getCannonSound());
                    enemyBullet.setVisiable(true);
                    enemyBullet.setDead(false);
                    if (enemyBullet.isMirror()) {
                        enemyBullet.setPosition(getX() + getWidth() - 5, getY() + 6);
                    } else {
                        enemyBullet.setPosition(getX() - 15, getY() + 6);
                    }

                }
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (bullets != null) {
            for (Sprite bullet : bullets) {
                bullet.draw(canvas);
            }
        }
    }
}
