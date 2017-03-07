package com.runrmby.runner;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
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
    boolean distanceMode = false; //TODO: create selection interface
    float odometer = 0f;
    float courseDistance = 10000f;  //Currently an arbitrary distance to the finish line.
    float courseLeft;               //Distance left for the finish line to reach the bottom of the screen.
    float distRemaining;            //Distance left for touchFollower to reach the finish line.
    Bitmap finishLine;
    //Integer courseLength = 10; //Units of background art

    Obstacles cone;
    int coneResId = R.drawable.cone;
    int coneXScale = 60;
    int coneYScale = 75;
    int maxNumCones = 8;
    float distBetweenCones;   //Initialized in resetVariables()
    float coneXSpeed = 0f;
    float coneYSpeed = 0f;

    Obstacles downTree;
    int downTreeResId = R.drawable.down_tree;
    int downTreeXScale = 189;
    int downTreeYScale = 60;
    int maxNumDownTrees = 3;
    float distBetweenDownTrees;
    float downTreeXSpeed = 0f;
    float downTreeYSpeed = 0f;

    Obstacles truck;
    int truckResId = R.drawable.shitty_truck;
    int truckXScale = 160;
    int truckYScale = 452;
    int maxNumTrucks = 1;
    float distBetweenTrucks;
    float truckXSpeed = 0f;
    float truckYSpeed = 2f;

    Obstacles crowd;
    int crowdResId = R.drawable.dumbppl;
    int crowdXScale = 122;
    int crowdYScale = 154;
    int maxNumCrowds = 1;
    float distBetweenCrowds;
    float crowdXSpeed = 3f;
    float crowdYSpeed = 10f;

    Obstacles car;
    int carResId = R.drawable.pan_2;
    int carXScale = 150;
    int carYScale = 250;
    int maxNumCars = 2;
    float distBetweenCars;
    float carXSpeed = 0f;
    float carYSpeed = 10f;

    Obstacles homingOb;
    int homingObResID = R.drawable.dude;
    int homingObMaxNum = 2;
    float homingObDistBetween;
    float homingObXSpeed = 0f;
    float homingObYSpeed = 0f;

    float increaseDifficultyDistance = 5000f;
    float toNextDiffIncrease;
    int difficultly;
    float homingSpeed;

    Obstacles extraLives;
    int extraLivesResId = R.drawable.lvlup;
    int extraLivesMaxNum = 1;
    float extraLivesDistBetween = 15000f;
    float extraLivesHorizontalSpeed = 3f;
    float extraLivesVerticalSpeed = 0f;

    int livesLeft;
    int collisionsWitnessed;

    //Using obstacle class to spawn footprints.
//    Obstacles footprints;
//    int footprintsImageResId = R.drawable.test_obstacle;
//    int footprintsDMaxNumObs = 6;
//    float footprintsDDistBetweenObs = 0f;
//    float footprintsDHorizontalSpeed = 0f;
//    float footprintsDVerticalSpeed = 0f;

    Bitmap touchFollower;
    int touchFollowerHeight;
    int touchFollowerWidth;
    float tFX;          //current x coordinate of touchFollower
    float tFY;          //current y coordinate of touchFollower
    float tFXOffset;
    float tFYOffset;
    float touchDownX;   //desired x coordinate of touchFollower.
    float touchDownY;   //desired y coordinate of touchFollower.

    Matrix matrixRotateClockwise = new Matrix();
    Matrix matrixRotateCounterClockwise = new Matrix();
    Boolean tFRotated = false;

    //Sound effects
    SoundPool soundEffects;
    int badSoundId;
    int goodSoundId;
    int wilhelmScreamId;
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

        float sX = p.x / 1080.0f;
        float sY = p.y / 1920.0f;

        BitmapFactory.Options ops = new BitmapFactory.Options();

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.mtroadcont2, ops);

        //System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        setBackgroundSizePos(p);

        //-----------------Initialize obstacles----------------------------------------------------
        cone = new Obstacles(this.getContext(), (int)(sX *coneXScale), (int)(sY * coneYScale), coneResId, maxNumCones, false, distBetweenCones, coneXSpeed, coneYSpeed, backgroundWidth, backgroundHeight, true, false);
        downTree = new Obstacles(this.getContext(), (int)(sX * downTreeXScale), (int)(sY * downTreeYScale), downTreeResId, maxNumDownTrees, false, distBetweenDownTrees, downTreeXSpeed, downTreeYSpeed, backgroundWidth, backgroundHeight, false, false);
        truck = new Obstacles(this.getContext(), (int)(sX * truckXScale), (int)(sY * truckYScale), truckResId, maxNumTrucks, false, distBetweenTrucks, truckXSpeed, truckYSpeed, backgroundWidth, backgroundHeight, true, true);
        crowd = new Obstacles(this.getContext(), (int)(sX * crowdXScale), (int)(sY * crowdYScale), crowdResId, maxNumCrowds, false, distBetweenCrowds, crowdXSpeed, crowdYSpeed, backgroundWidth, backgroundHeight, true, false);
        car = new Obstacles(this.getContext(), (int)(sX * carXScale), (int)(sY * carYScale), carResId, maxNumCars, false, distBetweenCars, carXSpeed, carYSpeed, backgroundWidth, backgroundHeight, true, true);
        homingOb = new Obstacles(this.getContext(), (int)(sX * 114), (int)(sY * 136), homingObResID, homingObMaxNum, false, homingObDistBetween, homingObXSpeed, homingObYSpeed, backgroundWidth, backgroundHeight, true, false);
        extraLives = new Obstacles(this.getContext(), (int)(sX * 62), (int)(sY * 110), extraLivesResId, extraLivesMaxNum, false, extraLivesDistBetween, extraLivesHorizontalSpeed, extraLivesVerticalSpeed, extraLivesMaxNum, extraLivesMaxNum, true, false);

        //Initialize footprints
//        footprints = new Obstacles(this.getContext(), (int)(sX * 50), (int)(sY * 50), footprintsImageResId, footprintsDMaxNumObs, true, footprintsDDistBetweenObs, footprintsDHorizontalSpeed, footprintsDVerticalSpeed, backgroundWidth, backgroundHeight, false, false);

        //Initialize touch follower.
        touchFollowerHeight = (int)(sY * 200);
        touchFollowerWidth = (int)(sX * 56);
        touchFollower = BitmapFactory.decodeResource(this.getResources(), R.drawable.practice3_small, null);
        touchFollower = Bitmap.createScaledBitmap(touchFollower, touchFollowerWidth, touchFollowerHeight, true);

        matrixRotateClockwise.postRotate(90);
        matrixRotateCounterClockwise.postRotate(270);

        tFXOffset = -touchFollower.getWidth() / 2;
        tFYOffset = -touchFollower.getHeight();

        //Initialize finish line.
        finishLine = BitmapFactory.decodeResource(this.getResources(), R.drawable.finish, null);
        finishLine = Bitmap.createScaledBitmap(finishLine, (int)(sX * 940), (int)(sY * 200), true);
        //-----------------------------------------------------------------------------------------

        //Sound effects.
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
        soundEffects = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        badSoundId = soundEffects.load(this.getContext(), R.raw.finger_runner_bad_sound, 1);
        goodSoundId = soundEffects.load(this.getContext(), R.raw.finger_runner_good_sound, 1);
        wilhelmScreamId = soundEffects.load(this.getContext(), R.raw.wilhelm_scream, 1);

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
            //--------------Check if an obstacle has run into touchFollower when no fingers are down (only necessary if friendly sprite triggers obstacles)-----------------------------
            if (checkObstaclesTouched(0)) {//Looks like if(false), but false is a passed boolean.
                if (livesLeft == 0) {
                    mA.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
//                    mA.requestGameState(MainActivity.LOSE);
                        }
                    });
                } else {
                    checkObstaclesTouched(2);
                    velocity = 0;
                    touchDownX = tFX - tFXOffset;
                    touchDownY = tFY - tFYOffset;
                    livesLeft--;
                    //TODO: play sound and implement lives display
                    if(!mA.musicMuted) {
                        soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                    }
                    collisionsWitnessed++;
                }
            } else if(extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), 1, true)){
                livesLeft++;
                if(!mA.musicMuted) {
                    soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
                }
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
            if(tFX != touchDownX + tFXOffset) {
                tFX += 0.1 * (touchDownX + tFXOffset - tFX);
            }
            if(tFY != touchDownY + tFYOffset){
                tFY += 0.1 * (touchDownY + tFYOffset - tFY);
            }

            //TODO: Try making an obstacle track down the touch follower.
            for (int i = 0; i < homingObMaxNum; i++) {
                if (homingOb.spawnTracker[i] == 1) {
                    if (homingOb.coordinatesArray[i][0] != tFX) {
                        homingOb.coordinatesArray[i][0] += homingSpeed * 0.75 * (tFX - homingOb.coordinatesArray[i][0]);
                    }
                    if (homingOb.coordinatesArray[i][1] != tFY) {
                        homingOb.coordinatesArray[i][1] += homingSpeed * (tFY - homingOb.coordinatesArray[i][1]);
                    }
                }
            }


            //For the touchFollower crossing the finish line to trigger course completion, use the following line:
            distRemaining = courseLeft - backgroundHeight + tFY + touchFollowerHeight;
            //For a touch past the finish line to trigger course completion, use the following line instead of the previous line:
//            distRemaining = courseLeft - backgroundHeight + touchDownY;

            //Decrease distance between obstacles as odometer increases for distance mode.
            //TODO: create variable for factor. Experiment with values.
            if(distanceMode){
                if(toNextDiffIncrease <= 0){
                    difficultly++;
                    float factor = (difficultly + 4f) / (difficultly + 5f);
                    distBetweenCones *= factor;
                    cone.setDistanceBetweenObstacles(distBetweenCones);
                    distBetweenDownTrees *= factor;
                    downTree.setDistanceBetweenObstacles(distBetweenDownTrees);
                    distBetweenTrucks *= factor;
                    truck.setDistanceBetweenObstacles(distBetweenTrucks);
                    distBetweenCones *= factor;
                    crowd.setDistanceBetweenObstacles(distBetweenCrowds);
                    distBetweenCars *= factor;
                    car.setDistanceBetweenObstacles(distBetweenCars);
                    homingObDistBetween *= factor;
                    homingOb.setDistanceBetweenObstacles(homingObDistBetween);

                    homingSpeed *= (difficultly + 5f) / (difficultly + 4f);

//                    extraLives.setDistanceBetweenObstacles(extraLivesDistBetween / factor); //TODO: currently would happen before first extra life reached.

                    toNextDiffIncrease = increaseDifficultyDistance;
                }
            }


            //Check if finish line has been reached.
            if(distanceMode == false) {
                if (distRemaining <= 0) {
                    //Dispatch a touch event to check if finish line crossed so that MainActivity isn't called in this thread.
                    mA.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
                        }
                    });
                }
            }

            //Move obstacles(any movement independent from the road).
            cone.moveObstacles();
            downTree.moveObstacles();
            truck.moveObstacles();
            crowd.moveObstacles();
            car.moveObstacles();
            homingOb.moveObstacles();
            extraLives.moveObstacles();
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
                if(distanceMode == false) {
                    gameTimer.changeTime(time);
                    mA.timer.setText(gameTimer.getTimeForDisplay());
                } else {
                    //Display distance instead of time for distance mode.
                    mA.timer.setText(String.valueOf(odometer + backgroundHeight - tFY) + "\nLives: " + String.valueOf(livesLeft) + "\nCollisions Witnessed: " + String.valueOf(collisionsWitnessed));
                }
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

            //Draw finish line.
            if(distanceMode == false) {
                if (courseLeft < backgroundHeight) {   //Don't check using distRemaining because that can vary without the road advancing.
                    canvas.drawBitmap(finishLine, -5f, backgroundHeight - courseLeft - finishLine.getHeight(), paint);
                }
            }

            //---------------------Draw obstacles---------------------------------------------
//            footprints.drawObstacles(canvas, paint);

            cone.drawObstacles(canvas, paint);
            downTree.drawObstacles(canvas, paint);
            crowd.drawObstacles(canvas, paint);
            car.drawObstacles(canvas, paint);
            homingOb.drawObstacles(canvas, paint);
            extraLives.drawObstacles(canvas, paint);
            truck.drawObstacles(canvas, paint);

            //Draw touch follower.
            canvas.drawBitmap(touchFollower, tFX, tFY, paint);
            //---------------------------------------------------------------------------------

            // Draw all to screen and unlock
            holder.unlockCanvasAndPost(canvas);
        }

    }

    // The first touch is ACITON_DOWN

    // ACTION_POINTER_DOWN is for extra pointers that enter the screen beyond the first

    //
    boolean handleTouches = true;

    /*
    touchEventList [a,b,c]
    findPointerIndex(b) = 1
    getPointerId(0) = a
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //System.out.println("GV| handleTouches: " + handleTouches);

        if(!handleTouches) {
            return true;
        }

        switch(event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if(!gameRunning) {
                    gameRunning = true;
                    previousTime = System.currentTimeMillis();
                }

                activeFinger.setNew(event.getPointerId(0), event.getX(), event.getY());
                fingers.add(event.getPointerId(0));

                touchDownY = activeFinger.y;
                touchDownX = activeFinger.x;

                //Check if finish line has been reached.
                if(distanceMode == false) {
                    if (distRemaining <= 0) {//odometer > courseDistance){
                        mA.setGameState(MainActivity.GAME_WON);
                    }
                }

//                footprints.spawnObstacle(0f, touchDownX - footprints.getObstacleWidth()/2, touchDownY - footprints.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.

                //--------------Check if an obstacle has been touched-----------------------------
                if (checkObstaclesTouched(2)) {
                    if (livesLeft == 0) {
                        touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateClockwise, true);
                        tFRotated = true;
                        if(!mA.musicMuted) {
                            soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                        }
                        collisionsWitnessed++;
                        mA.setGameState(MainActivity.GAME_LOST);
                    } else {
                        velocity = 0;
                        touchDownX = tFX - tFXOffset;
                        touchDownY = tFY - tFYOffset;
                        --livesLeft;
                        //TODO: play sound and implement lives display
                        if(!mA.musicMuted) {
                            soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                        }
                        collisionsWitnessed++;
                    }
                }else if(extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), 1, true)){
                    livesLeft++;
                    if(!mA.musicMuted) {
                        soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
                    }
                }
                //---------------------------------------------------------------------------

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if(!fingers.contains(event.getPointerId(i))) {
                        activeFinger.id = event.getPointerId(i);
                        activeFinger.setXY(event.getX(event.findPointerIndex(activeFinger.id)),
                                event.getY(event.findPointerIndex(activeFinger.id)));
                        fingers.add(event.getPointerId(i));

                        touchDownY = activeFinger.y;
                        touchDownX = activeFinger.x;
//                        footprints.spawnObstacle(0f, touchDownX - footprints.getObstacleWidth()/2, touchDownY - footprints.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.
                    }

                    //--------------Check if an obstacle has been touched-----------------------------
                    if (checkObstaclesTouched(2)) {
                        if (livesLeft == 0) {
                            touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateClockwise, true);
                            tFRotated = true;
                            if(!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                            }
                            collisionsWitnessed++;
                            mA.setGameState(MainActivity.GAME_LOST);
                        } else {
                            velocity = 0;
                            touchDownX = tFX - tFXOffset;
                            touchDownY = tFY - tFYOffset;
                            --livesLeft;
                            //TODO: play sound and implement lives display
                            if(!mA.musicMuted) {
                                soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                                collisionsWitnessed++;
                            }
                        }
                    }else if(extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), 1, true)){
                        livesLeft++;
                        if(!mA.musicMuted) {
                            soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
                        }
                    }
                    //---------------------------------------------------------------------------
                }


                break;

            case MotionEvent.ACTION_MOVE:
                // PROBLEM: might not be the right finger, x,y becomes off, and you get a glitch
                if(event.findPointerIndex(activeFinger.id) >= 0) {
                    if(gameTimeLeft != 0) {
                        addToFingerMoveDist(event.getY(event.findPointerIndex(activeFinger.id)) - activeFinger.y);
                        activeFinger.y = event.getY(event.findPointerIndex(activeFinger.id));
                        activeFinger.x = event.getX(event.findPointerIndex(activeFinger.id));
                    }
                }
                //--------------Check if an obstacle has been touched-----------------------------
                if (checkObstaclesTouched(2)) {
                    if (livesLeft == 0) {
                        touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateClockwise, true);
                        tFRotated = true;
                        if(!mA.musicMuted) {
                            soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                        }
                        collisionsWitnessed++;
                        mA.setGameState(MainActivity.GAME_LOST);
                    } else {
                        velocity = 0;
                        touchDownX = tFX - tFXOffset;
                        touchDownY = tFY - tFYOffset;
                        --livesLeft;
                        //TODO: play sound and implement lives display
                        if(!mA.musicMuted) {
                            soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                            collisionsWitnessed++;
                        }
                    }
                }else if(extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), 1, true)){
                    livesLeft++;
                    if(!mA.musicMuted) {
                        soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
                    }
                }
                //---------------------------------------------------------------------------
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Figure out what number isn't in pointers
                fingers.remove(Integer.valueOf(event.getPointerId(event.getActionIndex())));
                if(event.getPointerId(event.getActionIndex()) == activeFinger.id) {
                    int f = fingers.get(fingers.size() - 1);
                    if(event.findPointerIndex(f) >= 0) {//TODO: Temporary fix to prevent invalid pointer index from causing crash.
                        activeFinger.setNew(f, event.getX(event.findPointerIndex(f)), event.getY(event.findPointerIndex(f)));
                    }
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
        //System.out.println("GV| addToFingerMoveDist: " + dist);
        fingerMoveDist += dist;
    }

    public void advanceRoad(float distance) {
        // Don't go backward
        if(distance < 0) { return; }

        distance *= 0.75;

        //System.out.println("GV| distance moved: " + distance);
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;

        touchDownY += distance;
        tFY += distance;

        velocity = distance;

        odometer += distance;
        courseLeft -= distance;
        toNextDiffIncrease -= distance;

        //---------------Update Obstacles---------------------------------------------------
        cone.updateObstacles(distance, true);
        downTree.updateObstacles(distance, true);
        truck.updateObstacles(distance, true);
        crowd.updateObstacles(distance, true);
        car.updateObstacles(distance, true);
        homingOb.updateObstacles(distance, true);
        extraLives.updateObstacles(distance, true);

//        footprints.updateObstacles(distance, false);
        //-----------------------------------------------------------------------------

        // Loop backgrounds
        if(backgroundPositionY > background.getHeight()) {
            backgroundPositionY = -background.getHeight() + backgroundPositionY2;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + backgroundPositionY;
        }
    }

    public void pauseGame() {
        System.out.println("GV| pauseGame called");
        handleTouches = false;
        gameRunning = false;
    }

    public void resumeGame() {
        System.out.println("GV| resumeGame called");
        playing = true;
        handleTouches = true;
        gameRunning = false;
        velocity = 0;
//        update();
//        draw();
        //previousTime = System.currentTimeMillis();
    }

    // Call this from activity
    public void pause() {
        pauseGame();
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // Call this from activity
    public void resume() {
        resumeGame();

        gameThread = new Thread(this);
        gameThread.start();
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
    public Boolean checkObstaclesTouched(int action) {
        //First check if active finger is touching obstacle.
//        if(!fingers.isEmpty()) {
//            if (cone.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f, destroy)) {
//                return true;
//            } else if (downTree.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f, destroy)) {
//                return true;
//            } else if (truck.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f, destroy)) {
//                return true;
//            } else if (crowd.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f, destroy)) {
//                return true;
//            } else if (homingOb.wasObstacleTouched(activeFinger.x, activeFinger.y, 0f, 0f, destroy)){
//                return true;
//            }
//        }

        //Now check if touch follower is touching an obstacle.
        if (cone.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (downTree.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (truck.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (crowd.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)) {//(activeFinger.x, activeFinger.y)) {
            return true;
        }else if (car.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)){
            return true;
        } else if (homingOb.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), action, true)){
            return true;
        } else {
//            return false;
//        }

            //Check if trucks have touched other obstacles.
            for (int i = 0; i < maxNumTrucks; i++) {
                if (truck.spawnTracker[i] == 1 || truck.spawnTracker[i] == 2) { //Truck should only destroy obstacles if it's moving.
                    if (truck.speedArray[i][1] != 0) {
                        if(cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 3, false)){
                            collisionsWitnessed++;
                        }
                        if (crowd.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 2, false)) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            collisionsWitnessed++;
                        }
                        if (homingOb.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 2, false)) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            collisionsWitnessed++;
                        }
                        if(car.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 3, false)){
                            truck.hitObstacle(i, false);
                            collisionsWitnessed++;
                        }
                        if(downTree.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 3, false)){
                            collisionsWitnessed++;
                        }
                        extraLives.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, 3, false);
                    }
                }
            }
            //Check if cars have touched other obstacles.
            for (int i = 0; i < maxNumCars; i++){
                if (car.spawnTracker[i] == 1){
                    if(car.speedArray[i][1] != 0){
                        if(cone.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 3, false)){
                            collisionsWitnessed++;
                        }
                        if (crowd.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 2, false)) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            collisionsWitnessed++;
                        }
                        if (homingOb.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 2, false)) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            collisionsWitnessed++;
                        }
                        if(truck.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 3, false)){
                            collisionsWitnessed++;
                        }
                        if(downTree.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 3, false)){
                            collisionsWitnessed++;
                        }
                        extraLives.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, 3, false);
                    }
                }
            }
            for (int i = 0; i < maxNumCrowds; i++){
                if (crowd.spawnTracker[i] == 1){
                    if(crowd.speedArray[i][0] != 0 || crowd.speedArray[i][1] != 0){
                        if(truck.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, 0, false)){
                            crowd.speedArray[i][0] = 0;
                            crowd.speedArray[i][1] = 0;
                        }
                        if(car.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, 0, false)){
                            crowd.speedArray[i][0] = 0;
                            crowd.speedArray[i][1] = 0;
                        }
                    }
                }
            }
            return false;
        }
    }
//---------------------------------------------------------------------------

    public void resetVariables(){
        odometer = 0f;
        courseLeft = courseDistance;
        distRemaining = courseDistance;
        //--------------------------Reset obstacles------------------------------------------
        distBetweenCones = 800f;
        distBetweenDownTrees = 4000f;
        distBetweenTrucks = 3000f;
        distBetweenCrowds = 2500f;
        distBetweenCars = 2000f;
        homingObDistBetween = 7500f;
        cone.resetObstacles(distBetweenCones, backgroundWidth, backgroundHeight);
        downTree.resetObstacles(distBetweenDownTrees, backgroundWidth, backgroundHeight);
        truck.resetObstacles(distBetweenTrucks, backgroundWidth, backgroundHeight);
        crowd.resetObstacles(distBetweenCrowds, backgroundWidth, backgroundHeight);
        car.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        homingOb.resetObstacles(homingObDistBetween, backgroundWidth, backgroundHeight);
        toNextDiffIncrease = increaseDifficultyDistance;
        difficultly = 0;
        homingSpeed = 0.005f; //TODO: experiment with value
        extraLives.resetObstacles(extraLivesDistBetween, backgroundWidth, backgroundHeight);
        if(!distanceMode){
            livesLeft = 0;
        } else {
            livesLeft = 2;
        }
        collisionsWitnessed = 0;

        //Reset touch follower to bottom middle of screen.
        if(tFRotated) {
            touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateCounterClockwise, true);
            tFRotated = false;
        }
        tFX = tFXOffset + backgroundWidth/2;
        tFY = tFYOffset + backgroundHeight;

//        footprints.resetObstacles(footprintsDDistBetweenObs, backgroundWidth, backgroundHeight);
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
