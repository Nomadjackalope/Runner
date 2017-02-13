package com.runrmby.runner;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by benjamin on 1/31/17.
 *
 *  Do all game logic here. Ex. spawning obstacles
 *  Time based - the lowest time to finish is the best. Fixed distance. Quicker = better score
 *
 *  // Code used from
 *  http://gamecodeschool.com/android/building-a-simple-game-engine/
 *
 */
public class GameView extends SurfaceView implements Runnable {

    // Game thread
    Thread gameThread = null;

    SurfaceHolder holder;

    volatile boolean playing;

    Canvas canvas;
    Paint paint;

    Bitmap background;
    float backgroundPositionY;
    float backgroundPositionY2;
    int backgroundWidth;
    int backgroundHeight;

    float velocity;

    Point windowSize;

    // Touches
    FingerPoint activeFinger = new FingerPoint();

    ArrayList<Integer> fingers = new ArrayList<>();

    //------Obstacles and course length------------------------------------------------------
    float odometer = 0f;
    //Currently an arbitrary course distance to test.
    float courseDistance = 10000f;
    //Integer courseLength = 10; //Units of background art

    Obstacles obsA;
    int obsAImageResID = R.drawable.practice3_small;
    int obsAMaxNumObs = 4;
    float obsADistBetweenObs = 500f;
    float obsAHorizontalSpeed = 0f;
    float obsAVerticalSpeed = -1f;

    Obstacles obsB;
    int obsBImageResID = R.drawable.practice3_small;
    int obsBMaxNumObs = 2;
    float obsBDistBetweenObs = 600f;
    float obsBHorizontalSpeed = 2f;
    float obsBVerticalSpeed = 0f;

    Obstacles obsC;
    int obsCImageResID = R.drawable.practice3_small;
    int obsCMaxNumObs = 2;
    float obsCDistBetweenObs = 400f;
    float obsCHorizontalSpeed = -2f;
    float obsCVerticalSpeed = 2f;

    Obstacles obsD;
    int obsDImageResID = R.drawable.practice3_small;
    int obsDMaxNumObs = 1;
    float obsDDistBetweenObs = 900f;
    float obsDHorizontalSpeed = 3f;
    float obsDVerticalSpeed = 10f;

    Bitmap touchFollower;
    float tFX;
    float tFY;
    float tFXOffset;
    float tFYOffset;
    //-----------------------------------------------------------------------------------------

    MainActivity mA;

    // Time
    long gameTimeLeft;
    long gameLengthCountUp = 0; // 1000 = 1 second
    long gameLengthCountDown = 20000;
    long previousTime;
    boolean gameRunning = false;

    public Time gameTimer = new Time();

    private static final int GameVersion1 = 0; // Count up
    private static final int GameVersion2 = 1; // Count down
    private int gameVersion;


    public GameView(MainActivity mainActivity, Point windowSize) {
        super(mainActivity.getApplicationContext());
        init(windowSize, mainActivity);
    }

    public void init(Point p, MainActivity mainActivity) {

        this.windowSize = p;
        holder = getHolder();
        paint = new Paint();

        BitmapFactory.Options ops = new BitmapFactory.Options();

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.road, ops);

        System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        setBackgroundSizePos(p);

        //-----------------Initialize obstacles----------------------------------------------------
        obsA = new Obstacles(this.getContext(), obsAImageResID, obsAMaxNumObs, obsADistBetweenObs, obsAHorizontalSpeed, obsAVerticalSpeed, backgroundWidth, backgroundHeight, false);
        obsB = new Obstacles(this.getContext(), obsBImageResID, obsBMaxNumObs, obsBDistBetweenObs, obsBHorizontalSpeed, obsBVerticalSpeed, backgroundWidth, backgroundHeight, false);
        obsC = new Obstacles(this.getContext(), obsCImageResID, obsCMaxNumObs, obsCDistBetweenObs, obsCHorizontalSpeed, obsCVerticalSpeed, backgroundWidth, backgroundHeight, true);
        obsD = new Obstacles(this.getContext(), obsDImageResID, obsDMaxNumObs, obsDDistBetweenObs, obsDHorizontalSpeed, obsDVerticalSpeed, backgroundWidth, backgroundHeight, true);

        //Initialize touch follower.
        touchFollower = BitmapFactory.decodeResource(this.getResources(), R.drawable.practice3_small, null);
        tFXOffset = -touchFollower.getWidth() / 2;
        tFYOffset = -touchFollower.getHeight();
        //-----------------------------------------------------------------------------------------

        mA = mainActivity;

        resetVariables();

    }

    public void setBackgroundSizePos(Point p) {
        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);
        backgroundPositionY = 0;
        backgroundPositionY2 = -background.getHeight();
        //For some reason, these need to be here to get initialized correctly the first time.
        backgroundWidth = background.getWidth();
        backgroundHeight = background.getHeight();
        tFX = tFXOffset + backgroundWidth/2;
        tFY = tFYOffset + backgroundHeight;
    }

    // Bitmap processing
    // http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android#17839663
    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // If image is larger in either dimesion than the requested dimension
        if(height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) <= reqWidth) {
                inSampleSize *=2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }



    @Override
    public void run() {
        while(playing) {

            update();

            draw();

        }
    }

    public void update() {
        // if the user is not touching trigger inertia,
        // if we did it when the users finger was down it would get out of sync
        if(fingers.isEmpty()) {
            // We don't want this running all the time using up cpu
            if(velocity > 1) {
                advanceRoad(velocity); ///0.016f);
                velocity *= 0.75;//0.8f;//0.9f;
            }
            //--------------TODO: Check if an obstacle has run into touch follower when no fingers are down-----------------------------
            if (checkObstaclesTouched()){
                mA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis()+100, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
//                    mA.requestGameState(MainActivity.LOSE);
                    }
                });
            }
            //---------------------------------------------------------------------------
        }

        if(fingerMoveDist > 0) {
            advanceRoad(fingerMoveDist);
            fingerMoveDist = 0;
        }

        if(gameRunning) {
            // Update Time using delta time compared to last time update was run
            gameTimeLeft -= System.currentTimeMillis() - previousTime;
            previousTime = System.currentTimeMillis();

            //Update touch follower position. Could put outside of if(gameRunning) if want to resume movement after game paused while touchFollower is moving
            //...but then you could possibly cheat the timer by pausing right after the start of a big move.
            if(tFX != activeFinger.x + tFXOffset) {
                tFX += 0.1 * (activeFinger.x + tFXOffset - tFX);
            }
            if(tFY != activeFinger.y + tFYOffset){
                tFY += 0.1 * (activeFinger.y + tFYOffset - tFY);
            }
        }

        switch (gameVersion) {
            case GameVersion1:
                changeTimerText(-gameTimeLeft);
                break;

            case GameVersion2:
                changeTimerText(gameTimeLeft);
                break;
        }

    }

    public void changeTimerText(final long time) {
        mA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                int minutes = (int) time / 60000;
//                int remainder = (int) time - minutes * 60000;
//                int seconds = (int) remainder / 1000;
//                String secondsString;
//                if(seconds > 10) {
//                    secondsString = String.valueOf(seconds);
//                }else {
//                    secondsString = "0" + seconds;
//                }
//                remainder = remainder - seconds * 1000;
//                int milSec = remainder;
//                String string = minutes + ":" + secondsString + "." + milSec;
//                mA.timer.setText(string);
                gameTimer.changeTime(time);
                mA.timer.setText(gameTimer.getTimeForDisplay());
            }
        });

    }


    public void draw() {

        if(holder.getSurface().isValid()) {

            // Lock the canvas to draw
            canvas = holder.lockCanvas();

            // Recolor canvas so to not have artifacts
            canvas.drawColor(Color.argb(255,255,0,0));

            // Draw road
            canvas.drawBitmap(background, 0, backgroundPositionY, paint);
            canvas.drawBitmap(background, 0, backgroundPositionY2, paint);

            //---------------------Draw obstacles---------------------------------------------
            obsA.drawObstacles(canvas, paint);
            obsB.drawObstacles(canvas, paint);
            obsC.drawObstacles(canvas, paint);
            obsD.drawObstacles(canvas, paint);

            //Draw touch follower.
            canvas.drawBitmap(touchFollower, tFX, tFY, paint);
            //---------------------------------------------------------------------------------

            // Draw all to screen and unlock
            holder.unlockCanvasAndPost(canvas);
        }

    }

    // The first touch is ACITON_DOWN

    // ACTION_POINTER_DOWN is for extra pointers that enter the screen beyond the first

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch(event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                activeFinger.setNew(event.getPointerId(0), event.getX(), event.getY());
                fingers.add(event.getPointerId(0));

                //--------------Check if an obstacle has been touched-----------------------------
                if (checkObstaclesTouched()){
                    mA.requestGameState(MainActivity.LOSE);
                }
                //---------------------------------------------------------------------------

                //Check if finish line has been reached.
                if(odometer > courseDistance){
                    mA.requestGameState(MainActivity.WIN);
                }

                if(!gameRunning) {
                    gameRunning = true;
                    previousTime = System.currentTimeMillis();
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if(!fingers.contains(event.getPointerId(i))) {
                        activeFinger.id = event.getPointerId(i);
                        activeFinger.setXY(event.getX(event.findPointerIndex(activeFinger.id)),
                                event.getY(event.findPointerIndex(activeFinger.id)));
                        fingers.add(event.getPointerId(i));
                    }
//                    //--------------Check if an obstacle has been touched-----------------------------
//                    if (checkObstaclesTouched()){
//                        mA.requestGameState(MainActivity.LOSE);
//                    }
//                    //---------------------------------------------------------------------------
                }


                break;

            case MotionEvent.ACTION_MOVE:
                // PROBLEM: might not be the right finger, x,y becomes off, and you get a glitch
                if(event.findPointerIndex(activeFinger.id) >= 0) {
                    if(gameTimeLeft != 0) {
                        addToFingerMoveDist(event.getY(event.findPointerIndex(activeFinger.id)) - activeFinger.y);
                        activeFinger.y = event.getY(event.findPointerIndex(activeFinger.id));
                    }
                }
                //--------------Check if an obstacle has been touched-----------------------------
                if (checkObstaclesTouched()){
                    mA.requestGameState(MainActivity.LOSE);
                }
                //---------------------------------------------------------------------------
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Figure out what number isn't in pointers
                fingers.remove(Integer.valueOf(event.getActionIndex()));
                if(event.getActionIndex() == activeFinger.id) {
                    int f = fingers.get(fingers.size() - 1);
                    activeFinger.setNew(f, event.getX(event.findPointerIndex(f)), event.getY(event.findPointerIndex(f)));
                }

                break;

            case MotionEvent.ACTION_UP:
                fingers.clear();

                break;

        }
        return true;
    }

    volatile float fingerMoveDist = 0;

    // This gets called from onTouch thread which then lets update call advanceRoad
    //  this should keep two sections of road closer together
    void addToFingerMoveDist(float dist) {
        dist *= 0.75;
        System.out.println("GV| addToFingerMoveDist: " + dist);
        fingerMoveDist += dist;
    }

    public void advanceRoad(float distance) {
        // Don't go backward
        if(distance < 0) { return; }

        distance *= 0.75;

        System.out.println("GV| distance moved: " + distance);
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;

        velocity = distance;

        odometer += distance;

        //---------------Update Obstacles---------------------------------------------------
        obsA.updateObstacles(distance);
        obsB.updateObstacles(distance);
        obsC.updateObstacles(distance);
        obsD.updateObstacles(distance);
        //-----------------------------------------------------------------------------

        //backgroundPositionY += 15; // This should be set by the person's touches
        //backgroundPositionY2 += 15;
        // 15 needs to be the amount that the background not being moved has travelled
        if(backgroundPositionY > background.getHeight()) {
            backgroundPositionY = -background.getHeight() + backgroundPositionY2;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + backgroundPositionY;
        }
    }

    // Call this from activity
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // Call this from activity
    public void resume() {
        playing = true;
        //resetVariables();
        velocity = 0;
        gameThread = new Thread(this);
        gameThread.start();
        previousTime = System.currentTimeMillis();
        gameRunning = false;
    }

    public class FingerPoint {
        float x, y;

        int id;

        // Using this vs null because then we don't create a bunch of these objects
        //boolean isNull = true;

        public void setXY(float x, float y) {
            System.out.println("GV| finger1 y: " + y);
            this.x = x;
            this.y = y;
        }

        public void setNew(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    //--------------Check if an obstacle was touched-----------------------------
    public Boolean checkObstaclesTouched() {
        //First check if active finger is touching obstacle.
        if(!fingers.isEmpty()) {
            if (obsA.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f)) {
                return true;
            } else if (obsB.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f)) {
                return true;
            } else if (obsC.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f)) {
                return true;
            } else if (obsD.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f)) {
                return true;
            }
        }

        //Now check if touch follower is touching an obstacle.
        if (obsA.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight())){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (obsB.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight())){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (obsC.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight())){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (obsD.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight())){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else {
            return false;
        }
    }
//---------------------------------------------------------------------------

    public void resetVariables(){
        odometer = 0f;
        //--------------------------Reset obstacles------------------------------------------
        obsA.resetObstacles();
        obsB.resetObstacles();
        obsC.resetObstacles();
        obsD.resetObstacles();

        //TODO: Reset touch follower to bottom middle of screen. This isn't working the very first time through.
        tFX = tFXOffset + backgroundWidth/2;
        tFY = tFYOffset + backgroundHeight;
        //-----------------------------------------------------------------------------------
        backgroundPositionY = 0;
        backgroundPositionY2 = -background.getHeight();
        fingers.clear();
        velocity = 0;
        fingerMoveDist = 0;

        gameVersion = GameVersion1;

        switch (gameVersion) {
            case GameVersion1:
                gameTimeLeft = gameLengthCountUp;
                break;
            case GameVersion2:
                gameTimeLeft = gameLengthCountDown;
                break;
        }
        previousTime = System.currentTimeMillis();
        gameRunning = false;

    }
}
