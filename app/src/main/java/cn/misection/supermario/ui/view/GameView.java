package cn.misection.supermario.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import cn.misection.supermario.audio.MyMusic;
import cn.misection.supermario.audio.MySoundPool;
import cn.misection.supermario.enemy.Cannon;
import cn.misection.supermario.enemy.Chestunt;
import cn.misection.supermario.enemy.Enemy;
import cn.misection.supermario.enemy.Turtle;
import cn.misection.supermario.enums.GameState;
import cn.misection.supermario.enums.ItemType;
import cn.misection.supermario.enums.Site;
import cn.misection.supermario.item.*;
import cn.misection.supermario.item.brick.Brick;
import cn.misection.supermario.item.brick.Broken;
import cn.misection.supermario.item.brick.CommonBrick;
import cn.misection.supermario.item.brick.Pipe;
import cn.misection.supermario.sprite.Mario;
import cn.misection.supermario.sprite.Sprite;
import cn.misection.supermario.ui.widget.TiledLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static cn.misection.supermario.enums.GameState.*;
import static cn.misection.supermario.enums.Site.*;


/**
 * @author javaman
 */
public class GameView extends SurfaceView implements Callback, Runnable {
    public static final String TAG = "aaa";
    private final MyMusic myMusic;
    private final MySoundPool mySoundPool;
    private final SharedPreferences sp;
    private final SurfaceHolder holder;
    private final float scaleX;
    private final float scaleY;
    private final Context context;
    //region Field
    private boolean pause;
    private boolean running;
    private Canvas lockCanvas;
    private Bitmap aBitmap;
    private Bitmap bBitmap;
    private Bitmap leftBitmap;
    private Bitmap rightBitmap;
    private RectF rectF;
    private Mario mario;
    private TiledLayer tiledLayerPeng01;
    private Bitmap gameOverBitmap;
    private List<Sprite> chestunts;
    private List<Bitmap> chestuntBitmaps;
    private boolean enemyShown1;
    private boolean enemyShown2;
    private boolean enemyShown3;

    /**
     * ??????????????????;
     */
    private Integer score;
    private Paint mPaint;
    private Bitmap marioBitmap;
    private int lifeNumber = 3;
    private boolean inited;
    private String scoreString;
    private int delay;
    private Bitmap logoBitmap;
    private List<Sprite> bricks;

    /**
     * ??????????????????;
     */
    private int time;
    private Thread thread;
    private Bitmap finishBitmap;
    private boolean threadRunning;
    private Bitmap coinBitmap;
    private int coinNumber;

    /**
     * ????????????????????????;
     */
    private GameState gameState;
    private int stateDelayLifeCounter;
    private int stateDelayFinish;
    private int stateDelayLogo;
    private int stateDelayGameOver;
    private boolean firstRun;
    private boolean maioDieSoundPlayed;
    private List<Sprite> commonBricks;
    private TiledLayer tiledLayer_cover;
    private Bitmap enemyBulletBitmap;
    private List<Bitmap> cannonBitmaps;
    private List<Bitmap> turtleBitmaps;
    private List<Sprite> cannons;
    private List<Sprite> turtles;
    private List<Sprite> items;
    private boolean isEnemyShown4;
    private boolean isEnemyShown5;
    private List<Bitmap> fireBitmaps;
    private Bitmap downBitmap;
    private List<Sprite> pipes;
    private boolean isTransferReady;
    private boolean isTransferDone;

    //endregion
    //region ????????????
    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        sp = context.getSharedPreferences("sp", Context.MODE_PRIVATE);
        holder = getHolder();
        holder.addCallback(this);
        Point outSize = new Point();
        //??????????????????????????????????????????
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealSize(outSize);
        //???????????????????????????
        scaleX = (float) outSize.x / 800;
        scaleY = (float) outSize.y / 480;
        myMusic = new MyMusic(context);
        mySoundPool = new MySoundPool(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        myMusic.stop();
        threadRunning = false;
        //??????????????????????????????
        Integer hiscore = sp.getInt("hiscore", 0);
        if (hiscore < score) {
            sp.edit().putInt("hiscore", score).apply();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameState != null) {
            switch (gameState) {
                case LOGO: {
                    if (stateDelayLogo > 40) {
                        init();
                        stateDelayLogo = 0;
                        coinNumber = 0;
                    }
                    break;
                }
                case FINISH: {
                    if (stateDelayFinish > 100) {
                        gameState = LOGO;
                        lifeNumber = 3;
                        stateDelayFinish = 0;
                    }
                    break;
                }
                case GAME_OVER: {
                    if (stateDelayGameOver > 40) {
                        gameState = LOGO;
                        lifeNumber = 3;
                        stateDelayGameOver = 0;
                    }
                    // FIXME: 2021/10/17 missing break;
                }
                case GAMING: {
                    int pointCount = event.getPointerCount();
                    for (int i = 0; i < pointCount; i++) {
                        rectF.set(0, 360 * scaleY, 60 * scaleX, 420 * scaleY);
                        if (rectF.contains(event.getX(i), event.getY(i))) {
                            //left running
                            mario.setMirror(true);
                            mario.setRunning(true);
                        }
                        rectF.set(60, 420 * scaleY, 120 * scaleX, 480 * scaleY);
                        if (rectF.contains(event.getX(i), event.getY(i))) {
                            if (isTransferReady) {
                                gameState = TRANSFER;
                                mySoundPool.play(mySoundPool.getTransferSound());
                            }
                        }

                        rectF.set(120, 360 * scaleY, 180 * scaleX, 420 * scaleY);
                        if (rectF.contains(event.getX(i), event.getY(i))) {
                            //right running
                            mario.setMirror(false);
                            mario.setRunning(true);
                        }
                        rectF.set(740, 360 * scaleY, 800 * scaleX, 420 * scaleY);
                        if (rectF.contains(event.getX(i), event.getY(i))) {
                            //jump
                            if (!mario.isDead() && !mario.isJumping()) {
                                mario.setJumping(true);
                                mySoundPool.play(mySoundPool.getJumpSound());
                                mario.setSpeedY(-16);
                            }
                        }
                        rectF.set(680, 420 * scaleY, 740 * scaleX, 4800 * scaleY);
                        if (rectF.contains(event.getX(i), event.getY(i))) {
                            mario.fire();
                        }
                        //???????????????????????????
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            //stop running
                            mario.setRunning(false);
                            performClick();
                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + gameState);
            }
        }
        return true;
    }


    @Override
    public void run() {
        firstRun = true;
        init();
        while (running) {
            //????????????
            if (!isPause()) {
                myLogic();
            }
            long startTime = System.currentTimeMillis();
            myDraw();
            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;
            if (time < 1000 / 35) {
                SystemClock.sleep(1000 / 35 - time);
            }
        }
    }

    /**
     * ????????????
     *
     * @param fileName ???????????????
     * @return Bitmap
     */
    public Bitmap getBitmap(String fileName) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(fileName));
        } catch (IOException e) {
            Log.e(TAG, "getBitmap is null !!!");
        }
        return bitmap;
    }

    private int[][] getMapArray() {
        return new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 27, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 27, 27, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 27, 27, 27, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 27, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 27, 27, 27, 27, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 27, 27, 27, 27, 27, 27, 27, 27, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
                        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
                        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
                        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 0, 0, 26, 26, 26, 26,
                        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
                        26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,
                        26, 26, 26, 26},
                {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 0, 0, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30},
                {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 0, 0, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30,
                        30, 30, 30, 30}
        };
    }

    private int[][] getMapCoverArray() {
        return new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 50, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 45, 50, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 45, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41,
                        42, 43, 44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 46,
                        47, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 18, 19,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 12, 13, 14, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51,
                        52, 53, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0}
        };
    }

    /**
     * ???????????????????????????
     *
     * @param sprites   ????????????
     * @param distenceX x???????????????
     */
    private void spritesMove(List<Sprite> sprites, int distenceX) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                Sprite sprite = sprites.get(i);
                sprite.move(distenceX, 0);
                if (sprite instanceof CommonBrick) {
                    Broken broken = ((CommonBrick) sprite).getBroken();
                    if (broken != null && broken.isVisiable()) {
                        broken.move(distenceX, 0);
                    }
                }
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param sprites ????????????
     * @param canvas  ????????????
     */
    private void spritesDraw(List<Sprite> sprites, Canvas canvas) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                Sprite sprite = sprites.get(i);
                sprite.draw(canvas);
                //??????????????????????????????
                if (sprite instanceof CommonBrick) {
                    Broken broken = ((CommonBrick) sprite).getBroken();
                    if (broken != null && broken.isVisiable()) {
                        broken.draw(canvas);
                    }
                }
            }
        }
    }

    private void spritesLogic(List<Sprite> sprites) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                Sprite sprite = sprites.get(i);
                sprite.logic();
                //??????????????????????????????
                if (sprite instanceof CommonBrick) {
                    Broken broken = ((CommonBrick) sprite).getBroken();
                    if (broken != null && broken.isVisiable()) {
                        broken.logic();
                    }
                }
            }

        }
    }


    //endregion

    private void myLogic() {
        switch (gameState) {
            case TRANSFER: {
                if (!mario.isDead()) {
                    mario.move(0, 3);
                    if (mario.getY() > 280) {
                        tiledLayerPeng01.move(-980, 0);
                        tiledLayer_cover.move(-980, 0);
                        //?????????????????? ????????????
                        spritesMove(chestunts, -980);
                        spritesMove(cannons, -980);
                        spritesMove(turtles, -980);
                        spritesMove(bricks, -980);
                        spritesMove(commonBricks, -980);
                        spritesMove(items, -980);
                        spritesMove(pipes, -980);
                        isTransferDone = false;
                        gameState = GAMING;
                        break;
                    }
                }
            }
            case GAMING: {
                if (!threadRunning && inited) {
                    thread.start();
                    threadRunning = true;
                }
                if (!isTransferDone && mario.getY() > 216) {
                    mario.move(0, -3);
                    if (mario.getY() <= 216) {
                        isTransferDone = true;
                    }
                } else {
                    mario.logic();
                    if (!mario.isDead()) {

                        if (mario.isInvincible()) {
                            myMusic.play("music/invincible.mp3", true);
                        } else {
                            myMusic.play("music/bgm.mp3", true);
                        }
                        spritesLogic(cannons);
                        spritesLogic(chestunts);
                        spritesLogic(mario.getBullets());
                        spritesLogic(commonBricks);
                        spritesLogic(items);
                        spritesLogic(turtles);
                        spritesLogic(bricks);
                        spritesLogic(pipes);
                    } else {
                        if (!maioDieSoundPlayed) {
                            myMusic.play("music/over.mp3", false);
                            maioDieSoundPlayed = true;
                        }
                    }
                    marioMove();
                    myStep();
                    collisionWithMap();
                    myCollision();
                }

                myTime();
                break;
            }
            case LIFT_COUNTER: {
                myMusic.stop();
                if (lifeNumber < 1) {
                    gameState = GAME_OVER;
                } else {
                    if (stateDelayLifeCounter++ > 50) {
                        gameState = GAMING;
                        init();
                        stateDelayLifeCounter = 0;
                        break;
                    }
                }
            }
            case GAME_OVER: {
                stateDelayGameOver++;

                break;
            }
            case FINISH: {
                myMusic.play("music/finish.mp3", false);
                threadRunning = false;
                stateDelayFinish++;
                break;
            }
            case LOGO: {
                stateDelayLogo++;
                myMusic.stop();
                break;
            }
        }
    }

    /**
     * ????????????
     */
    private void myTime() {
        if (time <= 0) {
            mario.setDead(true);
        }
    }

    /**
     * ????????????
     *
     * @param scoreValue ?????????
     */
    private void updateScore(int scoreValue) {
        if (scoreValue > 0) {
            score += scoreValue;
            scoreString = "+" + scoreValue;
        }
        if (scoreValue < 0) {
            score -= scoreValue;
            scoreString = "-" + scoreValue;
        }

    }

    /**
     * ??????????????????
     */
    private void myStep() {
        if (tiledLayerPeng01.getX() == 0 && !enemyShown1) {
            for (int i = 0; i < 3; i++) {
                Enemy chestunt = new Chestunt(32, 32, chestuntBitmaps);
                chestunt.setVisiable(true);
                chestunt.setMirror(true);
                chestunt.setPosition(500 + 60 * i, 328);
                chestunts.add(chestunt);

            }
            for (int i = 0; i < 2; i++) {
                Turtle turtle = new Turtle(32, 48, turtleBitmaps);
                turtle.setVisiable(true);
                turtle.setMirror(false);
                turtle.setPosition(360 + 40 * i, 312);
                turtles.add(turtle);
            }
            enemyShown1 = true;

        }
        if (tiledLayerPeng01.getX() == -480 && !enemyShown2) {
            for (int i = 0; i < 3; i++) {
                Enemy enemy = new Chestunt(32, 32, chestuntBitmaps);
                enemy.setVisiable(true);
                enemy.setPosition(720 + 60 * i, 328);
                chestunts.add(enemy);
            }
            for (int i = 0; i < 2; i++) {
                Cannon cannon = new Cannon(40, 80, cannonBitmaps, mySoundPool);
                cannon.setVisiable(true);
                cannon.setPosition(800 + 80 * i, 160 - 40 * i);
                EnemyBullet enemyBullet = new EnemyBullet(enemyBulletBitmap);
                enemyBullet.setMirror(false);
                List<Sprite> bullets = new ArrayList<>();
                bullets.add(enemyBullet);
                cannon.setBullets(bullets);
                cannons.add(cannon);
            }

            enemyShown2 = true;
        }
        if (tiledLayerPeng01.getX() == -1000 && !enemyShown3) {
            for (int i = 0; i < 3; i++) {
                Enemy chestunt = new Chestunt(32, 32, chestuntBitmaps);
                chestunt.setVisiable(true);
                chestunt.setPosition(500 + 60 * i, 0);
                chestunts.add(chestunt);
            }
            enemyShown3 = true;
        }
        if (tiledLayerPeng01.getX() == -1400 && !isEnemyShown4) {
            for (int i = 0; i < 2; i++) {
                Turtle turtle = new Turtle(32, 48, turtleBitmaps);
                turtle.setVisiable(true);
                turtle.setMirror(false);
                turtle.setCanFly(true);
                turtle.setPosition(700 + 40 * i, 100);
                turtles.add(turtle);
            }
            Cannon cannon = new Cannon(40, 80, cannonBitmaps, mySoundPool);
            cannon.setVisiable(true);
            cannon.setPosition(640, 160);
            EnemyBullet enemyBullet = new EnemyBullet(enemyBulletBitmap);
            enemyBullet.setMirror(false);
            List<Sprite> bullets = new ArrayList<>();
            bullets.add(enemyBullet);
            cannon.setBullets(bullets);
            cannons.add(cannon);
            isEnemyShown4 = true;
        }
        if (tiledLayerPeng01.getX() == -2200 && !isEnemyShown5) {
            for (int i = 0; i < 7; i++) {
                Enemy chestunt = new Chestunt(32, 32, chestuntBitmaps);
                chestunt.setVisiable(true);
                chestunt.setPosition(360 + 40 * i, 240 - 40 * i);
                chestunts.add(chestunt);
            }
            isEnemyShown5 = true;
        }


        if (tiledLayerPeng01.getX() == -2800) {
            gameState = FINISH;
        }
    }

    /**
     * ??????????????????????????????
     */
    private void marioMove() {

        //1 ??????????????????
        if (mario.isDead()) {
            mario.move(0, mario.mSpeedY++);

        } else {
            if (mario.isRunning()) {
                //??????
                if (mario.isMirror()) {
                    mario.move(-4, 0);
                    //?????????????????????
                } else if (mario.getX() < 400) {
                    mario.move(4, 0);
                    //?????????????????? ????????????
                } else if (tiledLayerPeng01.getX() > 800 - tiledLayerPeng01.getCols()
                        * tiledLayerPeng01.getWidth()) {
                    tiledLayerPeng01.move(-4, 0);
                    tiledLayer_cover.move(-4, 0);
                    //?????????????????? ????????????
                    spritesMove(chestunts, -4);
                    spritesMove(cannons, -4);
                    spritesMove(turtles, -4);
                    spritesMove(bricks, -4);
                    spritesMove(commonBricks, -4);
                    spritesMove(items, -4);
                    spritesMove(pipes, -4);


                } else {
                    mario.move(4, 0);
                }
            }
            if (mario.isJumping()) {
                mario.move(0, mario.mSpeedY++);
            }
        }
        if (mario.getY() > 400) {
            //2 ?????????????????????????????????
            if (!mario.isDead()) {
                mario.setDead(true);
                mario.setSpeedY(-16);
            } else {
                lifeNumber--;
                gameState = LIFT_COUNTER;
            }
        }


    }

    private void init() {
        isTransferReady = false;
        isTransferDone = true;
        inited = false;
        threadRunning = false;
        maioDieSoundPlayed = false;
        time = 300;
        enemyShown1 = false;
        enemyShown2 = false;
        enemyShown3 = false;
        isEnemyShown4 = false;
        isEnemyShown5 = false;

        mPaint = new Paint();
        mPaint.setTextSize(20);//????????????
        mPaint.setColor(Color.WHITE);//????????????
        mPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/Bit8.ttf"));//??????????????????
        score = sp.getInt("hiscore", 0);//??????????????????
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //????????????????????????????????????????????????????????????????????????????????????
                while (threadRunning && time > 0) {
                    SystemClock.sleep(1000);
                    isTransferReady = false;
                    time--;
                    if (time == 100) {
                        mySoundPool.play(mySoundPool.getHurryUpSound());

                    }
                }
            }
        });

        //??????????????????
        aBitmap = getBitmap("button/a.png");
        bBitmap = getBitmap("button/b.png");
        leftBitmap = getBitmap("button/left.png");
        rightBitmap = getBitmap("button/right.png");
        downBitmap = getBitmap("button/down.png");
        gameOverBitmap = getBitmap("logo/gameover.png");
        marioBitmap = getBitmap("mario/small/mario_01.png");
        logoBitmap = getBitmap("logo/logo.jpg");
        finishBitmap = getBitmap("logo/finish.jpg");
        Bitmap mushroomBitmap = getBitmap("item/mushroom.png");
        coinBitmap = getBitmap("item/coin/coin_00.png");
        Bitmap cannonBitmap = getBitmap("enemy/cannon.png");
        enemyBulletBitmap = getBitmap("enemy/enemy_bullet.png");
        Bitmap pipeBitmap = getBitmap("brick/pipe.png");

        cannons = new ArrayList<>();
        turtles = new ArrayList<>();
        List<Bitmap> marioSmallBitmaps = new ArrayList<>();
        ArrayList<Bitmap> marioFireInvBitmaps = new ArrayList<>();
        ArrayList<Bitmap> marioSmallInvBitmaps = new ArrayList<>();
        ArrayList<Bitmap> marioBigInvincibleBitmaps = new ArrayList<>();
        commonBricks = new ArrayList<>();
        List<Bitmap> commonbrickbitmaps = new ArrayList<>();
        bricks = new ArrayList<>();
        items = new ArrayList<>();
        List<Bitmap> marioBigBitmaps = new ArrayList<>();
        List<Bitmap> marioFireBitmaps = new ArrayList<>();
        List<Bitmap> flowerBitmaps = new ArrayList<>();
        List<Bitmap> brokenBitmaps = new ArrayList<>();
        List<Bitmap> pipeBitmaps = new ArrayList<>();
        fireBitmaps = new ArrayList<>();
        cannonBitmaps = new ArrayList<>();
        pipes = new ArrayList<>();
        //???????????????????????????
        for (int i = 0; i < 7; i++) {
            marioBigBitmaps.add(getBitmap("mario/big/mario_0" + i + ".png"));
            marioSmallBitmaps.add(getBitmap("mario/small/mario_0" + i + ".png"));
            marioFireBitmaps.add(getBitmap("mario/fire/mario_0" + i + ".png"));
            marioFireInvBitmaps.add(getBitmap("mario/fire/mario_inv_0" + i + ".png"));
            marioSmallInvBitmaps.add(getBitmap("mario/small/mario_inv_0" + i + ".png"));
            marioBigInvincibleBitmaps.add(getBitmap("mario/big/mario_inv_0" + i + ".png"));
        }

        pipeBitmaps.add(pipeBitmap);

        for (int i = 0; i < 10; i++) {
            brokenBitmaps.add(getBitmap(String.format(Locale.CHINA,
                    "brick/broken/broken_%02d.png", i)));
        }

        chestuntBitmaps = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            chestuntBitmaps.add(getBitmap("enemy/chestunt/chestunt_0" + i + ".png"));
        }
        chestunts = new ArrayList<>();
        List<Bitmap> coinBitmaps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            coinBitmaps.add(getBitmap(String.format(Locale.CHINA,
                    "item/coin/coin_%02d.png", i)));
        }
        for (int i = 0; i < 4; i++) {
            flowerBitmaps.add(getBitmap("item/flower/flower_0" + i + ".png"));
            fireBitmaps.add(getBitmap("item/fire/fire_0" + i + ".png"));
        }
        cannonBitmaps.add(cannonBitmap);
        List<Bitmap> brickBitmaps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            brickBitmaps.add(getBitmap(String.format(Locale.CHINA, "brick/brick_%02d.png", i)));
        }
        turtleBitmaps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            turtleBitmaps.add(getBitmap(String.format(Locale.CHINA, "enemy/turtle/turtle_%02d" +
                    ".png", i)));
        }

        List<Bitmap> starBitmaps = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            starBitmaps.add(getBitmap(String.format(Locale.CHINA, "item/star/item_star_%02d.png",
                    i)));
        }

        int[][] pipePositions = {
                {37, 7}, {62, 7}
        };

        for (int i = 0; i < 2; i++) {
            Pipe pipe = new Pipe(80, 80, pipeBitmaps);
            pipe.setVisiable(true);
            if (i == 0) {
                pipe.setTransfer(true);
            }
            pipe.setPosition(pipePositions[i][0] * 40, pipePositions[i][1] * 40);
            pipes.add(pipe);
        }

        //??????????????????????????????
        int[][] itemBrickPositions = {
                {3, 3}, {3, 6}, {56, 6}, {12, 5}
        };
        //??????????????????
        int[][] coinsPostions = {
                {10, 0}, {10, 1}, {10, 2}, {10, 3}, {10, 4}, {11, 2}, {12, 0}, {12, 1}, {12, 2}, {12, 3}, {12, 4},
                {14, 0}, {14, 1}, {14, 2}, {14, 3}, {14, 4}, {15, 0}, {16, 0}, {15, 2}, {16, 2}, {15, 4}, {16, 4},
                {18, 0}, {18, 1}, {18, 2}, {18, 3}, {18, 4}, {19, 4}, {20, 4},
                {22, 0}, {22, 1}, {22, 2}, {22, 3}, {22, 4}, {23, 4}, {24, 4},
                {26, 1}, {26, 2}, {26, 3}, {28, 1}, {28, 2}, {28, 3}, {27, 0}, {27, 4},
                {36, 5}, {37, 5}, {38, 5}, {39, 5}, {40, 5}, {41, 5}, {42, 5}, {43, 5}

        };
        //????????????
        for (int[] coinposition : coinsPostions) {
            Coin coin = new Coin(40, 40, coinBitmaps);
            coin.setVisiable(true);
            coin.setPosition(coinposition[0] * 40, coinposition[1] * 40);
            items.add(coin);
        }
        //????????????????????????
        int[][] commonBrickPostions = {
                {2, 6}, {4, 6}, {29, 7}, {30, 7}, {32, 6}, {34, 5}, {36, 3}, {43, 3}, {36, 4},
                {37, 4}, {38, 4}, {39, 4}, {40, 4}, {41, 4}, {42, 4}, {43, 4}, {11, 5}, {13, 5},
                {14, 5}, {21, 6}, {22, 6}, {50, 6}, {51, 6}, {52, 6}, {53, 6}, {54, 6}

        };


        //????????????
        Brick brick = new Brick(40, 40, brickBitmaps);
        brick.setPosition(40 * itemBrickPositions[0][0], 40 * itemBrickPositions[0][1]);
        brick.createItem(true, flowerBitmaps, ItemType.FLOWER);
        items.add(brick.getItemSprite());
        brick.setVisiable(true);
        bricks.add(brick);
        Brick brick2 = new Brick(40, 40, brickBitmaps);
        brick2.setPosition(40 * itemBrickPositions[1][0], 40 * itemBrickPositions[1][1]);

        brick2.createItem(true, mushroomBitmap, ItemType.MUSHROOM);
        items.add(brick2.getItemSprite());
        brick2.setVisiable(true);
        bricks.add(brick2);

        Brick brick3 = new Brick(40, 40, brickBitmaps);
        brick3.setPosition(40 * itemBrickPositions[2][0], 40 * itemBrickPositions[2][1]);
        brick3.createItem(true, starBitmaps, ItemType.STAR);
        items.add(brick3.getItemSprite());
        brick3.setVisiable(true);
        bricks.add(brick3);

        Brick brick4 = new Brick(40, 40, brickBitmaps);
        brick4.setPosition(40 * itemBrickPositions[3][0], 40 * itemBrickPositions[3][1]);
        brick4.createItem(true, flowerBitmaps, ItemType.FLOWER);
        items.add(brick4.getItemSprite());
        brick4.setVisiable(true);
        bricks.add(brick4);


        commonbrickbitmaps.add(getBitmap("brick/commonBrick.png"));

        for (int[] position : commonBrickPostions) {
            CommonBrick commonBrick = new CommonBrick(40, 40, commonbrickbitmaps);
            commonBrick.setVisiable(true);
            Broken broken = new Broken(187, 187, brokenBitmaps);
            commonBrick.setBroken(broken);
            commonBrick.setCanBroken(false);
            commonBrick.setPosition(40 * position[0], 40 * position[1]);
            commonBricks.add(commonBrick);
        }

        rectF = new RectF();
        //???????????????
        this.mario = new Mario(32, 32, marioSmallBitmaps);
        this.mario.setVisiable(true);
        this.mario.setPosition(0, 330);
        List<List<Bitmap>> bitmapsList = new ArrayList<>();
        bitmapsList.add(marioSmallBitmaps);
        bitmapsList.add(marioBigBitmaps);
        bitmapsList.add(marioFireBitmaps);
        bitmapsList.add(marioSmallInvBitmaps);
        bitmapsList.add(marioBigInvincibleBitmaps);
        bitmapsList.add(marioFireInvBitmaps);
        this.mario.setBitmapsList(bitmapsList);

        //???????????????
        int map_peng01[][] = getMapArray();
        int map_cover[][] = getMapCoverArray();
        Bitmap mapBitmap = getBitmap("map/map1.png");
        tiledLayerPeng01 = new TiledLayer(mapBitmap, 100, 12, 40, 40);
        tiledLayer_cover = new TiledLayer(mapBitmap, 100, 12, 40, 40);
        tiledLayerPeng01.setTiledCell(map_peng01);
        tiledLayer_cover.setTiledCell(map_cover);
        if (firstRun) {
            gameState = LOGO;
            firstRun = false;
        } else {
            gameState = GAMING;
        }
        inited = true;


    }

    private void myDraw() {
        try {
            lockCanvas = holder.lockCanvas();
            lockCanvas.save();
            lockCanvas.scale(scaleX, scaleY);
            lockCanvas.drawColor(Color.parseColor("#3786ef"));
            switch (gameState) {
                case LOGO: {
                    lockCanvas.drawBitmap(logoBitmap, 0, 0, null);
                    break;
                }
                case GAME_OVER: {
                    lockCanvas.drawColor(Color.BLACK);
                    lockCanvas.drawBitmap(gameOverBitmap,
                            400 - gameOverBitmap.getWidth() / 2,
                            250 - gameOverBitmap.getHeight() / 2, null);
                    break;
                }
                case FINISH: {
                    lockCanvas.drawBitmap(finishBitmap, 0, 0, null);
                    break;
                }
                case LIFT_COUNTER: {
                    lockCanvas.drawColor(Color.BLACK);
                    lockCanvas.drawBitmap(marioBitmap, 400 - marioBitmap.getWidth(),
                            250 - marioBitmap.getHeight(), null);
                    lockCanvas.drawText(String.format(Locale.CHINA, "X %02d", lifeNumber),
                            440 - marioBitmap.getWidth(),
                            285 - marioBitmap.getHeight(), mPaint);
                    break;
                }
                case TRANSFER:
                case GAMING: {
                    tiledLayer_cover.draw(lockCanvas);
                    tiledLayerPeng01.draw(lockCanvas);
                    spritesDraw(bricks, lockCanvas);
                    spritesDraw(commonBricks, lockCanvas);
                    spritesDraw(chestunts, lockCanvas);
                    spritesDraw(cannons, lockCanvas);
                    spritesDraw(turtles, lockCanvas);
                    spritesDraw(items, lockCanvas);
                    mario.draw(lockCanvas);
                    spritesDraw(pipes, lockCanvas);

                    lockCanvas.drawBitmap(aBitmap, 600, 420, null);
                    lockCanvas.drawBitmap(bBitmap, 640, 360, null);
                    lockCanvas.drawBitmap(leftBitmap, 0, 360, null);
                    lockCanvas.drawBitmap(rightBitmap, 120, 360, null);
                    lockCanvas.drawBitmap(downBitmap, 60, 420, null);
                    //??????????????????
                    if (scoreString != null) {
                        lockCanvas.drawText(scoreString, mario.getX() + 25, mario.getY() - 20 - 3 * delay, mPaint);
                        if (delay++ == 6) {
                            scoreString = null;
                            delay = 0;
                        }
                    }
                    if (mario.getBullets() != null) {
                        int useableBulletCount = mario.getUseableBulletCount();
                        lockCanvas.drawText("Bullets Ready:" + useableBulletCount, 500, 30, mPaint);
                        if (mario.isInvincible()) {
                            lockCanvas.drawText("Invincible Time:" + mario.getInvincibleTime(), 500,
                                    60, mPaint);
                            if (isTransferReady) {
                                lockCanvas.drawText("Transfer Ready", 500, 90, mPaint);
                            }

                        } else {
                            if (mario.isZeroDamage()) {
                                lockCanvas.drawText("ZeroDamage Time:" + mario.getZeroDamageTime(), 500,
                                        60, mPaint);
                                if (isTransferReady) {
                                    lockCanvas.drawText("Transfer Ready", 500, 90, mPaint);
                                }
                            } else {
                                if (isTransferReady) {
                                    lockCanvas.drawText("Transfer Ready", 500, 60, mPaint);
                                }
                            }

                        }
                    } else {
                        if (mario.isInvincible()) {
                            lockCanvas.drawText("Invincible Time:" + mario.getInvincibleTime(), 500,
                                    30, mPaint);
                            if (isTransferReady) {
                                lockCanvas.drawText("Transfer Ready", 500, 60, mPaint);
                            }

                        } else {
                            if (mario.isZeroDamage()) {
                                lockCanvas.drawText("ZeroDamage Time:" + mario.getZeroDamageTime(), 500,
                                        30, mPaint);
                                if (isTransferReady) {
                                    lockCanvas.drawText("Transfer Ready", 500, 60, mPaint);
                                }
                            } else {
                                if (isTransferReady) {
                                    lockCanvas.drawText("Transfer Ready", 500, 30, mPaint);
                                }
                            }

                        }
                    }
                    //???????????????
                    lockCanvas.drawText(String.format(Locale.CHINA, "SCORE:%06d", score), 30, 30, mPaint);
                    if (mario.getBullets() != null) {
                        for (int i = 0; i < mario.getBullets().size(); i++) {
                            mario.getBullets().get(i).draw(lockCanvas);
                        }
                    }
                    //???????????????
                    lockCanvas.drawBitmap(coinBitmap, 200, 0, null);
                    lockCanvas.drawText(String.format(Locale.CHINA, "x %02d", coinNumber), 240,
                            30, mPaint);
                    //??????????????????
                    lockCanvas.drawText(String.format(Locale.CHINA, "TIME:%03d", time), 700, 30, mPaint);
                    break;
                }
            }
            lockCanvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lockCanvas != null) {
                holder.unlockCanvasAndPost(lockCanvas);
            }
        }
    }

    /**
     * ????????????????????????
     */
    private void myCollision() {
        //?????????????????????
        sptiteCollisionSprite(mario, commonBricks);
        //?????????????????????
        sptiteCollisionSprite(mario, bricks);

        sptiteCollisionSprite(mario, pipes);

        //???????????????
        sptiteCollisionSprite(mario, cannons);
        //?????????????????????
        sptitesCollisionSprites(items, bricks);
        //???????????????????????????
        sptitesCollisionSprites(items, commonBricks);
        sptitesCollisionSprites(items, pipes);

        //?????????????????????
        sptitesCollisionSprites(cannons, bricks);
        sptitesCollisionSprites(cannons, commonBricks);
        sptitesCollisionSprites(cannons, pipes);
        //?????????????????????
        sptitesCollisionSprites(turtles, bricks);
        //?????????????????????
        sptitesCollisionSprites(turtles, commonBricks);
        sptitesCollisionSprites(turtles, pipes);
        //???????????????
        sptitesCollisionSprites(chestunts, bricks);
        sptitesCollisionSprites(chestunts, commonBricks);
        sptitesCollisionSprites(chestunts, pipes);
        //?????????????????????
        sptitesCollisionSprites(mario.getBullets(), bricks);
        //?????????????????????
        sptitesCollisionSprites(mario.getBullets(), commonBricks);
        sptitesCollisionSprites(mario.getBullets(), pipes);
        turtleShellCollisionWith(turtles);
        turtleShellCollisionWith(chestunts);

        if (!mario.isDead()) {
            //?????????????????????
            marioCollisionWith(chestunts);
            marioCollisionWith(turtles);
            marioCollisionWith(items);
            marioCollisionWith(pipes);
            //???????????????????????????
            if (cannons != null) {
                for (int w = 0; w < cannons.size(); w++) {
                    List<Sprite> bullets = ((Cannon) cannons.get(w)).getBullets();
                    marioCollisionWith(bullets);
                }
            }
        }
    }


    /**
     * ?????????????????????
     * ??????
     * ??????
     * ????????????
     */
    private void collisionWithMap() {
        //????????????
        if (!mario.isDead()) {
            spriteCollisionMap(mario);
            spritesCollisionMap(mario.getBullets());
        }
        //????????????
        spritesCollisionMap(chestunts);
        spritesCollisionMap(turtles);
        spritesCollisionMap(cannons);
        //?????????????????????
        spritesCollisionMap(items);
    }

    //??????
    //???????????????
    //???????????????
    //???????????????


    private void turtleShellCollisionWith(List<Sprite> sprites) {
        if (sprites != null && turtles != null) {
            for (int i = 0; i < turtles.size(); i++) {
                Sprite shell = turtles.get(i);
                if (shell.isVisiable() && shell.isDead()) {
                    turtleShellCollisionWith(shell, sprites);
                }
            }
        }
    }

    private void turtleShellCollisionWith(Sprite shell, List<Sprite> sprites) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                turtleShellCollisionWith(shell, sprites.get(i));
            }

        }
    }

    /**
     * ????????????
     *
     * @param shell  ??????
     * @param sprite ????????????
     */
    private void turtleShellCollisionWith(Sprite shell, Sprite sprite) {
        if (shell.isVisiable() && shell.isRunning() && !sprite.isDead()) {
            if (shell.collisionWith(sprite)) {
                if (sprite instanceof Turtle) {
                    if (!((Turtle) sprite).isZeroDamage()) {
                        if (((Turtle) sprite).isCanFly()) {
                            ((Turtle) sprite).setCanFly(false);
                            ((Turtle) sprite).setZeroDamage(true);
                            updateScore(100);
                        } else {
                            mySoundPool.play(mySoundPool.getHitEnemySound());
                            ((Turtle) sprite).setOverturn(true);
                            if (shell.getX() > sprite.getX()) {
                                sprite.setMirror(false);
                            } else {
                                sprite.setMirror(true);
                            }
                            sprite.setSpeedY(-10);
                            sprite.setDead(true);
                            updateScore(100);
                        }
                    }
                } else {
                    mySoundPool.play(mySoundPool.getHitEnemySound());
                    ((Enemy) sprite).setOverturn(true);
                    if (shell.getX() > sprite.getX()) {
                        sprite.setMirror(false);
                    } else {
                        sprite.setMirror(true);
                    }
                    sprite.setSpeedY(-10);
                    sprite.setDead(true);
                    updateScore(100);
                }


            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param sprites ????????????
     */
    private void marioCollisionWith(List<Sprite> sprites) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                Sprite sprite = sprites.get(i);
                marioCollisionWith(sprite);
                if (!(sprite instanceof ItemSprite)) {
                    marioBulletCollisionWithEnemy(sprite);
                }
            }
        }
    }


    /**
     * ?????????????????????
     *
     * @param sprite ????????????
     */
    private void marioCollisionWith(Sprite sprite) {
        if (mario.collisionWith(sprite)) {
            if (sprite instanceof Pipe) {
                if (((Pipe) sprite).isTransfer()) {
                    isTransferReady = true;
                }

            } else
                //???????????????
                if (sprite instanceof ItemSprite) {
                    //???????????????
                    if (sprite instanceof EnemyBullet) {
                        if (mario.isInvincible()) {
                            updateScore(100);
                            sprite.setVisiable(false);
                        } else if (mario.isJumping() && mario.siteCollisionWith(sprite, DOWN) &&
                                !sprite.isDead()) {
                            mySoundPool.play(mySoundPool.getHitEnemySound());
                            //?????????????????????100??????
                            updateScore(100);
                            mario.setSpeedY(-10);
                            sprite.setVisiable(false);
                        } else {
                            if (!mario.isZeroDamage() && sprite.isVisiable()) {
                                int status = mario.getStatus();
                                if (status != 0) {
                                    mario.shapeShift(status - 1);
                                    mySoundPool.play(mySoundPool.getHurtSound());
                                    mario.setZeroDamage(true);
                                } else {
                                    mario.setDead(true);
                                    mario.setSpeedY(-16);
                                }
                            }
                        }
                    } else {
                        int sound = mySoundPool.getItemSound();
                        int status = mario.getStatus();
                        if (sprite instanceof Mushroom) {
                            mySoundPool.play(mySoundPool.getItemSound());
                            if (status == 0) {
                                mario.shapeShift(1);
                            }
                        }
                        if (sprite instanceof Flower) {
                            //???
                            if (status != 2) {
                                //????????????????????????
                                mario.shapeShift(2);
                                //????????????
                                List<Sprite> bullets = new ArrayList<>();
                                for (int j = 0; j < 5; j++) {
                                    Bullet bullet = new Bullet(16, 16, fireBitmaps);
                                    if (mario.isMirror()) {
                                        bullet.setMirror(false);
                                    } else {
                                        bullet.setMirror(true);
                                    }
                                    bullets.add(bullet);
                                }
                                mario.setBullets(bullets);
                            }
                        }
                        if (sprite instanceof Star) {
                            //??????????????????????????????????????????
                            mario.setInvincible(true);
                        }
                        if (sprite instanceof Coin) {
                            coinNumber++;
                            sound = mySoundPool.getCoinSound();
                        }
                        mySoundPool.play(sound);
                        sprite.setVisiable(false);
                        updateScore(100);
                    }


                } else {
                    //???????????????
                    if (!sprite.isDead()) {
                        if (mario.isInvincible()) {
                            //????????????????????????????????????
                            if (sprite instanceof Turtle) {
                                //????????????????????????????????????????????????
                                if (!((Turtle) sprite).isZeroDamage()) {
                                    if (((Turtle) sprite).isCanFly()) {
                                        ((Turtle) sprite).setCanFly(false);
                                        sprite.setSpeedY(0);
                                        ((Turtle) sprite).setZeroDamage(true);

                                        updateScore(100);
                                        mario.setSpeedY(-10);
                                        mario.setJumping(true);
                                    } else {
                                        ((Enemy) sprite).setOverturn(true);
                                        sprite.setDead(true);
                                        mySoundPool.play(mySoundPool.getHitEnemySound());
                                        updateScore(100);
                                        mario.setSpeedY(-10);
                                        mario.setJumping(true);
                                    }
                                }
                            } else {
                                ((Enemy) sprite).setOverturn(true);
                                sprite.setSpeedY(-10);
                                sprite.setDead(true);
                                mySoundPool.play(mySoundPool.getHitEnemySound());
                                //?????????????????????100??????
                                updateScore(100);
                            }
                        } else if (mario.isJumping()) {
                            if (sprite instanceof Turtle) {
                                //????????????????????????????????????????????????
                                if (!((Turtle) sprite).isZeroDamage()) {
                                    if (((Turtle) sprite).isCanFly()) {
                                        ((Turtle) sprite).setCanFly(false);
                                        sprite.setSpeedY(-3);
                                        ((Turtle) sprite).setZeroDamage(true);
                                        updateScore(100);
                                        mario.setSpeedY(-10);
                                    } else {
                                        sprite.setDead(true);
                                        mySoundPool.play(mySoundPool.getHitEnemySound());
                                        updateScore(100);
                                    }
                                }
                            } else {
                                sprite.setDead(true);
                                mySoundPool.play(mySoundPool.getHitEnemySound());
                                //?????????????????????100??????
                                updateScore(100);
                                mario.setSpeedY(-10);
                            }
                        } else {
                            if (!mario.isZeroDamage()) {
                                int status = mario.getStatus();
                                if (status != 0) {
                                    mario.shapeShift(status - 1);
                                    mySoundPool.play(mySoundPool.getHurtSound());
                                    mario.setZeroDamage(true);
                                } else {
                                    mario.setDead(true);
                                    mario.setSpeedY(-16);
                                }
                            }
                        }
                        //?????????
                    } else if (sprite instanceof Turtle) {
                        if (!sprite.isRunning()) {
                            if (mario.getX() < sprite.getX()) {
                                sprite.setMirror(true);
                                sprite.setRunning(true);
                            } else {
                                sprite.setMirror(false);
                                sprite.setRunning(true);
                            }
                            mario.setJumping(true);
                            mario.setSpeedY(-16);
                        } else {
                            if (!mario.isZeroDamage() && !mario.isJumping()) {
                                sprite.setVisiable(false);
                                int status = mario.getStatus();
                                if (status != 0) {
                                    mario.shapeShift(status - 1);
                                    mySoundPool.play(mySoundPool.getHurtSound());
                                    mario.setZeroDamage(true);
                                } else {
                                    mario.setDead(true);
                                    mario.setSpeedY(-16);
                                }
                            }
                        }
                    }
                }
        }
    }


    private void marioBulletCollisionWithEnemy(Sprite sprite) {
        List<Sprite> bullets = mario.getBullets();
        if (bullets != null) {
            for (int i = 0; i < bullets.size(); i++) {
                Sprite sprite1 = bullets.get(i);
                //????????????????????????????????????
                if (sprite1.isVisiable() && sprite1.collisionWith(sprite) && !(sprite instanceof
                        EnemyBullet) && !sprite.isDead()) {
                    //????????????????????????????????????????????????
                    if (sprite instanceof Turtle) {
                        if (!((Turtle) sprite).isZeroDamage()) {
                            if (((Turtle) sprite).isCanFly()) {
                                ((Turtle) sprite).setCanFly(false);
                                ((Turtle) sprite).setZeroDamage(true);
                                updateScore(100);
                            } else {
                                sprite.setDead(true);
                                ((Turtle) sprite).setOverturn(true);
                                if (sprite1.getX() > sprite.getX()) {
                                    sprite.setMirror(false);
                                } else {
                                    sprite.setMirror(true);
                                }
                                sprite.setSpeedY(-10);
                                mySoundPool.play(mySoundPool.getHitEnemySound());
                                updateScore(100);
                            }
                        }
                        sprite1.setVisiable(false);
                    } else {
                        mySoundPool.play(mySoundPool.getHitEnemySound());
                        sprite1.setVisiable(false);
//                        sprite.setVisiable(false);
//                        sprite.setDead(true);
                        ((Enemy) sprite).setOverturn(true);
                        if (sprite1.getX() > sprite.getX()) {
                            sprite.setMirror(false);
                        } else {
                            sprite.setMirror(true);
                        }
                        sprite.setSpeedY(-10);
                        sprite.setDead(true);
                        updateScore(100);
                    }
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param sprites ????????????
     */

    private void spritesCollisionMap(List<Sprite> sprites) {
        if (sprites != null) {
            for (int i = 0; i < sprites.size(); i++) {
                spriteCollisionMap(sprites.get(i));
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param sprite ????????????
     */
    private void spriteCollisionMap(Sprite sprite) {
        if (sprite.isVisiable() && !sprite.isDead()) {
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, UP_LEFT) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, UP_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, UP_RIGHT)) {
                if (sprite instanceof Bullet) {
                    sprite.setVisiable(false);
                } else {
                    sprite.setSpeedY(Math.abs(sprite.getSpeedY()));
                }
            }
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, DOWN_LEFT) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, DOWN_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, DOWN_RIGHT)) {
                //??????????????????
                if (sprite instanceof Turtle) {
                    if (((Turtle) sprite).isCanFly()) {
                        sprite.setJumping(true);
                        sprite.setSpeedY(-10);
                    } else {
                        sprite.setJumping(false);
                        int footY = sprite.getY() + sprite.getHeight();
                        int newY = footY / tiledLayerPeng01.getHeight()
                                * tiledLayerPeng01.getHeight()
                                - sprite.getHeight();
                        sprite.setPosition(sprite.getX(), newY);
                    }
                    //??????????????????
                } else if (sprite instanceof Bullet || sprite instanceof Star) {
                    sprite.setSpeedY(-10);
                    sprite.setJumping(true);
                } else {
                    sprite.setJumping(false);
                    //????????????
                    //??????????????????
                    int footY = sprite.getY() + sprite.getHeight();
                    //??????
                    int newY = footY / tiledLayerPeng01.getHeight()
                            * tiledLayerPeng01.getHeight()
                            - sprite.getHeight();
                    sprite.setPosition(sprite.getX(), newY);
                }
            }
            //????????????
            else if (!sprite.isJumping()) {
                sprite.setJumping(true);
                sprite.setSpeedY(0);
            }
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, LEFT_UP) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, LEFT_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, LEFT_DOWN)) {
                if (sprite instanceof Bullet) {
                    sprite.setVisiable(false);
                } else {
                    sprite.setMirror(true);
                    sprite.move(4, 0);
                }
            }
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_UP) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_DOWN)) {
                if (sprite instanceof Bullet) {
                    sprite.setVisiable(false);
                } else {
                    sprite.setMirror(false);
                    sprite.move(-4, 0);
                }
            }
            //?????????
        } else if (sprite.isVisiable() && sprite.isDead() && sprite instanceof Turtle) {
            if (!((Turtle) sprite).isOverturn()) {
                //????????????
                if (sprite.siteCollisionWith(tiledLayerPeng01, DOWN_LEFT) ||
                        sprite.siteCollisionWith(tiledLayerPeng01, DOWN_MIDDLE) ||
                        sprite.siteCollisionWith(tiledLayerPeng01, DOWN_RIGHT)) {
                    //??????????????????
                    sprite.setJumping(false);
                    //????????????
                    //??????????????????
                    int footY = sprite.getY() + sprite.getHeight();
                    //??????
                    int newY = footY / tiledLayerPeng01.getHeight()
                            * tiledLayerPeng01.getHeight()
                            - sprite.getHeight();
                    sprite.setPosition(sprite.getX(), newY);
                }
            }
            //????????????
            else if (!sprite.isJumping()) {
                sprite.setJumping(true);
                sprite.setSpeedY(0);
            }
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, LEFT_UP) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, LEFT_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, LEFT_DOWN)) {
                sprite.setMirror(true);
                sprite.move(10, 0);

            }
            //????????????
            if (sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_UP) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_MIDDLE) ||
                    sprite.siteCollisionWith(tiledLayerPeng01, RIGHT_DOWN)) {
                sprite.setMirror(false);
                sprite.move(-10, 0);

            }

        }
    }


    /**
     * ????????????
     *
     * @param sprites0 ???????????????????????????
     * @param sprites1 ??????????????????
     */


    private void sptitesCollisionSprites(List<Sprite> sprites0, List<Sprite> sprites1) {
        if (sprites0 != null) {
            for (int i = 0; i < sprites0.size(); i++) {
                sptiteCollisionSprite(sprites0.get(i), sprites1);
            }
        }
    }


    /**
     * ????????????
     *
     * @param sprite0 ?????????????????????
     * @param sprites ??????????????????
     */

    private void sptiteCollisionSprite(Sprite sprite0, List<Sprite> sprites) {
        if (sprites != null) {
            for (Sprite sprite1 : sprites) {
                sptiteCollisionSprite(sprite0, sprite1);
            }
        }
    }

    /**
     * ????????????
     *
     * @param sprite0 ?????????????????????
     * @param sprite1 ????????????
     */
    private void sptiteCollisionSprite(Sprite sprite0, Sprite sprite1) {
        //??????????????????????????????
        if (sprite0.isVisiable() && !sprite0.isDead() && sprite1.isVisiable() && !sprite1.isDead()) {
            //0????????????1
            if (sprite0.siteCollisionWith(sprite1, DOWN)) {
                //??????????????????
                if (sprite0 instanceof Turtle) {
                    sprite0.setJumping(true);
                    sprite0.setSpeedY(-10);
                } else
                    //????????????????????? ???????????????
                    if (sprite0 instanceof Enemy && !(sprite0 instanceof Cannon) && sprite1 instanceof
                            Brick && sprite1.isJumping()) {
                        ((Enemy) sprite0).setOverturn(true);
                        sprite0.setSpeedY(-10);
                        sprite0.setDead(true);
                        updateScore(100);
                    } else
                        //????????????????????? ???????????????
                        if (sprite0 instanceof ItemSprite && sprite1 instanceof CommonBrick && sprite1.isJumping()) {
                            sprite0.setSpeedY(-10);
                            sprite0.setJumping(true);

                        } else
                            //??????????????????????????????
                            if (sprite0 instanceof Bullet || sprite0 instanceof Star) {
                                sprite0.setSpeedY(-10);
                                sprite0.setJumping(true);
                            } else {
                                sprite0.setJumping(false);
                                //????????????
                                //??????????????????
                                int footY = sprite0.getY() + sprite0.getHeight();
                                //??????
                                int newY = footY / tiledLayerPeng01.getHeight()
                                        * tiledLayerPeng01.getHeight()
                                        - sprite0.getHeight();
                                sprite0.setPosition(sprite0.getX(), newY);
                            }
            }
            if (sprite0.siteCollisionWith(sprite1, Site.UP)) {
                //???????????????
                if (sprite0 instanceof Mario && sprite0.isJumping()) {
                    if (sprite1 instanceof CommonBrick) {
                        sprite0.setSpeedY(Math.abs(sprite0.getSpeedY()));
                        if (!sprite1.isJumping()) {
                            if (mario.getStatus() != 0) {
                                ((CommonBrick) sprite1).setCanBroken(true);
                                sprite1.setSpeedY(-4);
                                sprite1.setJumping(true);
                                mySoundPool.play(mySoundPool.getBrokenSound());
                            } else {
                                sprite1.setSpeedY(-4);
                                sprite1.setJumping(true);
                                mySoundPool.play(mySoundPool.getCannotBreakSound());
                            }
                        }
                    } else if (sprite1 instanceof Brick) {
                        sprite0.setSpeedY(Math.abs(sprite0.getSpeedY()));
                        if (((Brick) sprite1).hasItem()) {
                            mySoundPool.play(mySoundPool.getHitBrickSound());
                            sprite1.setSpeedY(-4);
                            sprite1.setJumping(true);
                        }
                    }
                } else {
                    if (sprite0 instanceof Bullet) {
                        sprite0.setVisiable(false);
                    } else {
                        sprite0.setSpeedY(Math.abs(sprite0.getSpeedY()));
                    }
                }

            }
            if (sprite0.siteCollisionWith(sprite1, LEFT)) {
                if (sprite0 instanceof Bullet) {
                    sprite0.setVisiable(false);
                } else {
                    sprite0.setMirror(true);
                    sprite0.move(4, 0);
                }
            }
            if (sprite0.siteCollisionWith(sprite1, RIGHT)) {
                if (sprite0 instanceof Bullet) {
                    sprite0.setVisiable(false);
                } else {
                    sprite0.setMirror(false);
                    sprite0.move(-4, 0);
                }
            }
            //?????????
        } else if (sprite0.isVisiable() && sprite0.isDead() && sprite1.isVisiable() &&
                !sprite1.isDead() && sprite0 instanceof Turtle && !(sprite1 instanceof Turtle)) {
            Log.e(TAG, "sptiteCollisionSprite: ????????????");
            //0????????????1
            if (sprite0.siteCollisionWith(sprite1, DOWN)) {
                sprite0.setJumping(false);
                //????????????
                //??????????????????
                int footY = sprite0.getY() + sprite0.getHeight();
                //??????
                int newY = footY / tiledLayerPeng01.getHeight()
                        * tiledLayerPeng01.getHeight()
                        - sprite0.getHeight();
                sprite0.setPosition(sprite0.getX(), newY);
            }
            if (sprite0.siteCollisionWith(sprite1, LEFT)) {
                sprite0.setMirror(true);
                sprite0.move(10, 0);
            }
            if (sprite0.siteCollisionWith(sprite1, RIGHT)) {

                sprite0.setMirror(false);
                sprite0.move(-10, 0);
            }
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
