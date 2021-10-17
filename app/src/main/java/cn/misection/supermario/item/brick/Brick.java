package cn.misection.supermario.item.brick;

import android.graphics.Bitmap;

import java.util.List;

import cn.misection.supermario.enums.ItemType;
import cn.misection.supermario.item.Coin;
import cn.misection.supermario.item.Flower;
import cn.misection.supermario.item.ItemSprite;
import cn.misection.supermario.item.Mushroom;
import cn.misection.supermario.item.Star;

/**
 * 砖块类
 * @author javaman
 */
public class Brick extends ItemSprite {

    /**
     * 表示道具类型 枚举;
     */
    protected ItemType itemType;

    protected ItemSprite itemSprite;

    /**
     * 标志道具是否已显示;
     */
    private boolean hasItem;

    private int delay;

    public Brick(int width, int height, List<Bitmap> bitmaps) {
        super(width, height, bitmaps);
    }

    public Brick(Bitmap bitmap) {
        super(bitmap);
    }

    //region Getter&Setter
    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public boolean hasItem() {
        return hasItem;
    }

    public void setHasItem(boolean hasItem) {
        this.hasItem = hasItem;
    }

    //endregion

    public ItemSprite getItemSprite() {
        return itemSprite;
    }

    public void setItemSprite(ItemSprite itemSprite) {
        this.itemSprite = itemSprite;
    }

    @Override
    public void logic() {
        if (isJumping()) {
            if (hasItem) {
                itemSprite.setVisiable(true);
                itemSprite.setPosition(getX(), getY() - getHeight());
                hasItem = false;
            }
            move(0, mSpeedY++);
            if (mSpeedY > 4) {
                setJumping(false);
            }
        }
        if (!hasItem) {
            setFrameSequenceIndex(4);
        } else {
            if (delay++ > 10) {
                nextFrame();
                if (getFrameSequenceIndex() >= 4) {
                    setFrameSequenceIndex(0);
                }
                delay = 0;

            }

        }
    }

    /**
     * 为砖块添加道具
     *
     * @param e      是否添加标志位
     * @param bitmap 道具图片（单帧方式）
     */
    public void createItem(boolean e, Bitmap bitmap, ItemType type) {
        setItemType(type);
        if (e) {
            switch (type) {
                case Mushroom: {
                    //蘑菇默认往右移动
                    itemSprite = new Mushroom(bitmap);
                    itemSprite.setMirror(true);
                }
                break;
                case Coin: {

                }
                break;

            }

            hasItem = true;
        }
    }

    /**
     * 为砖块添加道具
     *
     * @param e       是否添加标志位
     * @param bitmaps 道具图片（多帧方式）
     */
    public void createItem(boolean e, List<Bitmap> bitmaps, ItemType type) {
        setItemType(type);
        if (e) {
            switch (type) {

                case Coin: {
                    itemSprite = new Coin(40, 40, bitmaps);
                    itemSprite.setRunning(false);
                }
                break;
                case Star: {
                    itemSprite = new Star(28, 30, bitmaps);
                    itemSprite.setRunning(true);
                    itemSprite.setMirror(true);
                }
                break;

                case Flower: {
                    //花默认不移动
                    itemSprite = new Flower(32, 32, bitmaps);
                    itemSprite.setPosition(getX() + 4, getY() - 32);
                    itemSprite.setRunning(false);
                }
                break;

            }

            hasItem = true;
        }
    }
}
