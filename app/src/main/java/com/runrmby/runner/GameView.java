package com.runrmby.runner;

//import android.content.res.Resources;
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

    volatile boolean playing = false;

    Canvas canvas;
    Paint paint;

    Bitmap background;
    BitmapFactory.Options ops = new BitmapFactory.Options();
    float backgroundPositionY;
    float backgroundPositionY2;
    int backgroundWidth;
    int backgroundHeight;

    float velocity;

    Point windowSize;

    // Touches
    FingerPoint activeFinger = new FingerPoint();

    ArrayList<Integer> fingers = new ArrayList<>();

//    //------Obstacles and course length------------------------------------------------------
    boolean distanceMode = false; //TODO: create selection interface
    float odometer = 0f;
    float courseDistance;
    float courseLeft;               //Distance left for the finish line to reach the bottom of the screen.
    float distRemaining;            //Distance left for touchFollower to reach the finish line.
    public float yourDistance;
    Bitmap finishLine;

    float toNextDiffIncrease;
    float difficultyIncreaseSeparation;
    int difficultly;

    float sX;
    float sY;

    int coins;
    int steps;
    int livesLeft;
    int collisionsWitnessed;
    LocationNoObstacles levelZero;
    LocationStationaryObstacles levelOne;
    LocationNormalRoad levelTwo;
    LocationCrazyRoad levelThree;
    //-----------------------------------------------------------------------------------------

    boolean handleTouches = false;
    boolean simulatedTouch = false;

    MainActivity mA;
    int locationState;
    boolean fpMode;

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

    int TICKS_PER_SECOND = 25;
    int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    int MAX_FRAMESKIP = 5;
    int loops;
    float interpolation;

    boolean screenInitialized = false;


    public GameView(MainActivity mainActivity, Point windowSize) {
        super(mainActivity.getApplicationContext());
        init(windowSize, mainActivity);
    }

    public void init(Point p, MainActivity mainActivity) {
        mA = mainActivity;
        this.windowSize = p;
        holder = getHolder();
        paint = new Paint();

//        sX = p.x / 1080.0f;
//        sY = p.y / 1920.0f;

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

        background = BitmapFactory.decodeResource(this.getResources(), R.mipmap.road_0, ops);
        //TODO: should initialize a location only when needed, but it takes a while so doing both here because there's only two at the moment.
//        levelZero = new LocationOne(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//        levelOne = new LocationTwo(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//        levelTwo = new LocationThree(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//        switch (mA.locationState){
//            case 0:
////                levelZero = new LocationNoObstacles(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//                background = BitmapFactory.decodeResource(this.getResources(), levelZero.getBackgroundResId(), ops);
//                break;
//            case 1:
////                levelOne = new LocationStationaryObstacles(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//                background = BitmapFactory.decodeResource(this.getResources(), levelOne.getBackgroundResId(), ops);
//                break;
//            case 2:
////                levelTwo = new LocationNormalRoad(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//                background = BitmapFactory.decodeResource(this.getResources(), levelTwo.getBackgroundResId(), ops);
//                break;
//            case 3:
////                levelThree = new LocationCrazyRoad(mA, this, sX, sY, backgroundWidth, backgroundHeight);
//                background = BitmapFactory.decodeResource(this.getResources(), levelThree.getBackgroundResId(), ops);
//                break;
//        }

        //System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        setBackgroundSizePos(p);

        //Initialize finish line.
        finishLine = BitmapFactory.decodeResource(this.getResources(), R.drawable.finish_xxhdpi, null);
//        finishLine = Bitmap.createScaledBitmap(finishLine, (int)(sX * finishLine.getWidth()), (int)(sY * finishLine.getHeight()), true);

//        resetVariables();
    }

    public void setBackgroundSizePos(Point p) {
        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);
        backgroundPositionY = 0;
        backgroundPositionY2 = -background.getHeight();
        //For some reason, these need to be here to get initialized correctly the first time.
        backgroundWidth = background.getWidth();
        backgroundHeight = background.getHeight();
    }

//    // Bitmap processing
//    // http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android#17839663
//    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
//    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        // If image is larger in either dimension than the requested dimension
//        if(height > reqHeight || width > reqWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width
//            while((halfHeight / inSampleSize) >= reqHeight
//                    && (halfWidth / inSampleSize) <= reqWidth) {
//                inSampleSize *=2;
//            }
//        }
//
//        return inSampleSize;
//    }
//
//    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
//                                                         int reqWidth, int reqHeight) {
//
//        // First decode with inJustDecodeBounds=true to check dimensions
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
//
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeResource(res, resId, options);
//    }



    @Override
    public void run() {
        while(playing) {
//            while(mA.initialized) {
                update();

                draw(0);
//            }
        }

//        //http://webcache.googleusercontent.com/search?q=cache:http://www.koonsolo.com/news/dewitters-gameloop/
//        long next_game_tick = System.currentTimeMillis();
//        while( playing ) {
//
//            loops = 0;
//            while( System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) {
//                update();
//
//                next_game_tick += SKIP_TICKS;
//                loops++;
//            }
//            interpolation = ( System.currentTimeMillis() + SKIP_TICKS - next_game_tick ) / ( SKIP_TICKS );
//            draw(interpolation);
//        }
    }

    public void update() {
        // if the user is not touching trigger inertia,
        // if we did it when the users finger was down it would get out of sync
        if(fingers.isEmpty()) {
            // We don't want this running all the time using up cpu
            if(velocity > 1) {
                advanceRoad(velocity); ///0.016f);

                switch (locationState){
                    case 0:
                        velocity *= levelZero.getVelocityFactor();
                        break;
                    case 1:
                        velocity *= levelOne.getVelocityFactor();
                        break;
                    case 2:
                        velocity *= levelTwo.getVelocityFactor();
                        break;
                    case 3:
                        velocity *= levelThree.getVelocityFactor();
                        break;
                }
//                velocity *= 0.75;//0.8f;//0.9f;
            }

            //--------------Check if an obstacle has run into touchFollower when no fingers are down-----------------------------
            switch (locationState){
                case 0:
//                    levelZero.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
                case 1:
                    levelOne.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
                case 2:
//                    levelTwo.checkCollisions();
                    levelTwo.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
                case 3:
//                    levelTwo.checkCollisions();
                    levelThree.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
            }
            //---------------------------------------------------------------------------
        }

        if(fingerMoveDist > 0) {
            advanceRoad(fingerMoveDist);
            fingerMoveDist = 0;
        } else {
            fingerMoveDist = 0;
        }

        if(gameRunning) {
            // Update Time using delta time compared to last time update was run
            gameTimeLeft -= System.currentTimeMillis() - previousTime;
            previousTime = System.currentTimeMillis();

            //Update touch follower position. Could put outside of if(gameRunning) if want to resume movement after game paused while touchFollower is moving
            //...but then you could possibly cheat the timer by pausing right after the start of a big move.-------------------------------
//            if(!fpMode) {
//                switch (locationState) {
//                    case 0:
//                        levelZero.updateTouchFollower();
//                        break;
//                    case 1:
//                        levelOne.updateTouchFollower();
//                        break;
//                    case 2:
//                        levelTwo.updateTouchFollower();
//                        break;
//                    case 3:
//                        levelThree.updateTouchFollower();
//                        break;
//                }
//            }
            switch (locationState){
                case 0:
//                    levelZero.updateHomingObstacle();
                    break;
                case 1:
                    levelOne.updateHomingObstacle();
                    break;
                case 2:
                    levelTwo.updateHomingObstacle();
                    break;
                case 3:
                    levelThree.updateHomingObstacle();
                    break;
            }
            //--------------------------------------------------------------------------------------

            //For the touchFollower crossing the finish line to trigger course completion, use the following:
            switch (locationState){
                case 0:
//                    if(!fpMode) {
//                        distRemaining = courseLeft - backgroundHeight + levelZero.getTFY() + levelZero.getTouchFollowerHeight();
//                    } else {
                        distRemaining = courseLeft - backgroundHeight + levelZero.getTouchDownY() - levelZero.getFootprintHeight()/2;
//                    }
                    break;
                case 1:
//                    if(!fpMode) {
//                        distRemaining = courseLeft - backgroundHeight + levelOne.getTFY() + levelOne.getTouchFollowerHeight();
//                    } else {
                        distRemaining = courseLeft - backgroundHeight + levelOne.getTouchDownY() - levelOne.getFootprintHeight()/2;
//                    }
                    break;
                case 2:
                    distRemaining = courseLeft - backgroundHeight + levelTwo.getTouchDownY() - levelTwo.getFootprintHeight()/2;
                    break;
                case 3:
                    distRemaining = courseLeft - backgroundHeight + levelThree.getTouchDownY() - levelThree.getFootprintHeight()/2;
                    break;
            }
            //For a touch past the finish line to trigger course completion, use the following line instead of the previous:
//            distRemaining = courseLeft - backgroundHeight + touchDownY;

            //----------Increase difficulty during distance mode.-----------------------------------
//            if(distanceMode){
                if(toNextDiffIncrease <= 0){
                    difficultly++;
                    switch (locationState){
                        case 0:
//                            levelZero.updateObstacleSeparation();
                            break;
                        case 1:
                            levelOne.updateDifficulty(difficultly);
//                            levelOne.updateObstacleSeparation();
                            break;
                        case 2:
                            levelTwo.updateDifficulty(difficultly);
//                            levelTwo.updateObstacleSeparation();
                            break;
                        case 3:
                            levelThree.updateDifficulty(difficultly);
//                            levelThree.updateObstacleSeparation();
                            break;
                    }

                    toNextDiffIncrease = difficultyIncreaseSeparation;

                }
//            }
            //--------------------------------------------------------------------------------------

            //Check if finish line has been reached.
            if(distanceMode == false) {
                if (distRemaining <= 0) {
                    //Dispatch a touch event to check if finish line crossed so that MainActivity isn't called in this thread.
                    mA.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            simulatedTouch = true;
                            getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
                        }
                    });
                }
            }

            //------Move obstacles(any movement independent from the road).-----
            switch (locationState){
                case 0:
                    levelZero.move();
                    break;
                case 1:
                    levelOne.move();
                    break;
                case 2:
                    levelTwo.move();
                    break;
                case 3:
                    levelThree.move();
                    break;
            }
            //------------------------------------------------------------------
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
                yourDistance = courseDistance - distRemaining;
                gameTimer.changeTime(time);
                if(!distanceMode) {
//                    mA.timer.setText(gameTimer.getTimeForDisplay());
                    mA.timer.setText(gameTimer.getTimeForDisplay() + "\n" + String.format("%.0f", (yourDistance)/courseDistance*100) + "%\nCoins: " + String.valueOf(coins));
                } else {
                    //Display distance instead of time for distance mode.
//                    mA.timer.setText(String.valueOf(odometer + backgroundHeight - tFY - touchFollowerHeight) + "\nLives: " + String.valueOf(livesLeft));

                    mA.timer.setText(String.format("%.1f", yourDistance) + "\nCoins: " + String.valueOf(coins) + "\nLives: " + String.valueOf(livesLeft));
//                    switch (locationState){
//                        case 0:
////                            yourDistance = odometer + backgroundHeight - levelZero.getTFY() - levelZero.getTouchFollowerHeight();
////                            yourDistance = odometer + backgroundHeight - levelZero.getTouchDownY() - levelZero.getFootprintHeight();
//                            mA.timer.setText(String.format("%.1f", yourDistance) + "\nCoins: " + String.valueOf(coins) + "\nLives: " + String.valueOf(livesLeft));
//                            break;
//                        case 1:
////                            yourDistance = odometer + backgroundHeight - levelOne.getTouchDownY() - levelOne.getFootprintHeight();
//                            mA.timer.setText(String.format("%.1f", yourDistance) + "\nCoins: " + String.valueOf(coins) + "\nLives: " + String.valueOf(livesLeft));
//                            break;
//                        case 2:
////                            yourDistance = odometer + backgroundHeight - levelTwo.getTouchDownY() + levelTwo.getFootprintHeight()/2;
//                            mA.timer.setText(String.format("%.1f", yourDistance) + "\nCoins: " + String.valueOf(coins) + "\nLives: " + String.valueOf(livesLeft));
//                            break;
//                        case 3:
////                            yourDistance = odometer + backgroundHeight - levelThree.getTouchDownY() + levelThree.getFootprintHeight()/2;
//                            mA.timer.setText(String.format("%.1f", yourDistance) + "\nCoins: " + String.valueOf(coins) + "\nLives: " + String.valueOf(livesLeft));
//                            break;
//                    }
                }
            }
        });

    }


    public void draw(float interpolation) {

        if(holder.getSurface().isValid()) {

            // Lock the canvas to draw
            canvas = holder.lockCanvas();

            // Recolor canvas so to not have artifacts
            canvas.drawColor(Color.argb(255,255,0,0));

            //TODO: Interpolation for game loop.
//            float ibackgroundPositionY = backgroundPositionY + (velocity * interpolation);
//            float ibackgroundPositionY2 = backgroundPositionY2 + (velocity * interpolation);

            // Draw road
            canvas.drawBitmap(background, 0, backgroundPositionY, paint);
            canvas.drawBitmap(background, 0, backgroundPositionY2, paint);

            //Draw finish line.
            if(!distanceMode) {
                if (courseLeft < backgroundHeight) {   //Don't check using distRemaining because that can vary without the road advancing.
                    canvas.drawBitmap(finishLine, 0, backgroundHeight - courseLeft - finishLine.getHeight(), paint);
                }
            }

            //---------------------Draw obstacles---------------------------------------------
            switch (locationState){
                case 0:
                    levelZero.draw(canvas, paint, interpolation, velocity);
                    break;
                case 1:
                    levelOne.draw(canvas, paint, interpolation, velocity);
                    break;
                case 2:
                    levelTwo.draw(canvas, paint, interpolation, velocity);
                    break;
                case 3:
                    levelThree.draw(canvas, paint, interpolation, velocity);
                    break;
            }
            //---------------------------------------------------------------------------------

            // Draw all to screen and unlock
            holder.unlockCanvasAndPost(canvas);
        }

    }

    // The first touch is ACITON_DOWN

    // ACTION_POINTER_DOWN is for extra pointers that enter the screen beyond the first

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

                if(!simulatedTouch) {
                    activeFinger.setNew(event.getPointerId(0), event.getX(), event.getY());
                    fingers.add(event.getPointerId(0));

//                touchDownY = activeFinger.y;
//                touchDownX = activeFinger.x;
                    switch (locationState) {
                        case 0:
                            levelZero.setTouchDownX(activeFinger.x);
                            levelZero.setTouchDownY(activeFinger.y);
                            break;
                        case 1:
                            levelOne.setTouchDownX(activeFinger.x);
                            levelOne.setTouchDownY(activeFinger.y);
                            break;
                        case 2:
                            levelTwo.setTouchDownX(activeFinger.x);
                            levelTwo.setTouchDownY(activeFinger.y);
                            break;
                        case 3:
                            levelThree.setTouchDownX(activeFinger.x);
                            levelThree.setTouchDownY(activeFinger.y);
                            break;
                    }
                    steps++;
                } else{
                    simulatedTouch = false;
                }

                //Check if finish line has been reached.
                if(!distanceMode) {
                    if (distRemaining <= 0) {//odometer > courseDistance){
                        mA.setGameState(MainActivity.GAME_WON);
                    }
                }

//                fpMode.spawnObstacle(0f, touchDownX - fpMode.getObstacleWidth()/2, touchDownY - fpMode.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.

                //--------------Check if an obstacle has been touched-----------------------------
                switch (locationState){
                    case 0:
                        levelZero.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 1:
                        levelOne.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 2:
                        levelTwo.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 3:
                        levelThree.checkIfObstacleWasTouched(livesLeft);
                        break;
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

//                        touchDownY = activeFinger.y;
//                        touchDownX = activeFinger.x;
                        switch (locationState){
                            case 0:
                                levelZero.setTouchDownX(activeFinger.x);
                                levelZero.setTouchDownY(activeFinger.y);
                                break;
                            case 1:
                                levelOne.setTouchDownX(activeFinger.x);
                                levelOne.setTouchDownY(activeFinger.y);
                                break;
                            case 2:
                                levelTwo.setTouchDownX(activeFinger.x);
                                levelTwo.setTouchDownY(activeFinger.y);
                                break;
                            case 3:
                                levelThree.setTouchDownX(activeFinger.x);
                                levelThree.setTouchDownY(activeFinger.y);
                                break;
                        }
                        steps++;

//                        fpMode.spawnObstacle(0f, touchDownX - fpMode.getObstacleWidth()/2, touchDownY - fpMode.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.
                    }

                    //--------------Check if an obstacle has been touched-----------------------------
                    switch (locationState){
                        case 0:
                            levelZero.checkIfObstacleWasTouched(livesLeft);
                            break;
                        case 1:
                            levelOne.checkIfObstacleWasTouched(livesLeft);
                            break;
                        case 2:
                            levelTwo.checkIfObstacleWasTouched(livesLeft);
                            break;
                        case 3:
                            levelThree.checkIfObstacleWasTouched(livesLeft);
                            break;
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
                switch (locationState){
                    case 0:
                        levelZero.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 1:
                        levelOne.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 2:
                        levelTwo.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 3:
                        levelThree.checkIfObstacleWasTouched(livesLeft);
                        break;
                }
                //---------------------------------------------------------------------------

                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Figure out what number isn't in pointers
                fingers.remove(Integer.valueOf(event.getPointerId(event.getActionIndex())));
                if(event.getPointerId(event.getActionIndex()) == activeFinger.id) {
                    int f = fingers.get(fingers.size() - 1);
                    if(event.findPointerIndex(f) >= 0) {
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
        switch (locationState){
            case 0:
                dist *= levelZero.getDistanceFactor();
                break;
            case 1:
                dist *= levelOne.getDistanceFactor();
                break;
            case 2:
                dist *= levelTwo.getDistanceFactor();
                break;
            case 3:
                dist *= levelThree.getDistanceFactor();
                break;
        }
//        dist *= 0.75;
//        //System.out.println("GV| addToFingerMoveDist: " + dist);

        fingerMoveDist += dist;
    }

    public void advanceRoad(float distance) {
        // Don't go backward
        if(distance < 0) {
            return;
        }

//        if(fingers.isEmpty()){
//            //TODO: limit advance velocity
//            //set max advance speed
//            if (distance > 1) {
//                velocity = distance - 1;
//                distance = 1;
//            }
//        } else {
//            velocity = distance;
//        }

        switch (locationState){
            case 0:
//                if(!fingers.isEmpty()) {
                    //Reduce velocity by factor.
                    velocity *= levelZero.getInertiaFactor();
                    //Reduce distance by factor.
                    distance *= levelZero.getDistanceFactor();
//                }
                //Update touchFollower and obstacles.
                levelZero.updateTouchDownY(distance);
//                levelZero.updateTFY(distance);
                levelZero.updateObs(distance);
                break;
            case 1:
//                if(!fingers.isEmpty()) {
                    //Reduce velocity by factor.
                    velocity *= levelOne.getInertiaFactor();
                    //Reduce distance by factor.
                    distance *= levelOne.getDistanceFactor();
//                }
                //Update touchFollower and obstacles.
                levelOne.updateTouchDownY(distance);
//                levelOne.updateTFY(distance);
                levelOne.updateObs(distance);
                break;
            case 2:
//                if(!fingers.isEmpty()) {
                    //Reduce velocity by factor.
                    velocity *= levelTwo.getInertiaFactor();
                    //Reduce distance by factor.
                    distance *= levelTwo.getDistanceFactor();
//                }
                //Update touchFollower and obstacles.
                levelTwo.updateTouchDownY(distance);
                levelTwo.updateObs(distance);
                break;
            case 3:
//                if(!fingers.isEmpty()) {
                //Reduce velocity by factor.
                velocity *= levelThree.getInertiaFactor();
                //Reduce distance by factor.
                distance *= levelThree.getDistanceFactor();
//                }
                //Update touchFollower and obstacles.
                levelThree.updateTouchDownY(distance);
                levelThree.updateObs(distance);
                break;
        }
//        distance *= 0.75;

        //System.out.println("GV| distance moved: " + distance);
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;

//        touchDownY += distance;
//        tFY += distance;

//        velocity = distance;


        odometer += distance;
        courseLeft -= distance;
        toNextDiffIncrease -= distance;

        // Loop backgrounds
        if(backgroundPositionY > background.getHeight()) {
            backgroundPositionY = -background.getHeight() + backgroundPositionY2;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + backgroundPositionY;
        }
    }

    public void setGameAudio() {
        //Set audio.
        switch (locationState) {
            case 0:
                if (levelZero != null) {
                    levelZero.setAudio();
                }
                break;
            case 1:
                if (levelOne != null) {
                    levelOne.setAudio();
                }
                break;
            case 2:
                if (levelTwo != null) {
                    levelTwo.setAudio();
                }
                break;
            case 3:
                if (levelThree != null) {
                    levelThree.setAudio();
                }
                break;
        }
    }

    public void releaseGameAudio() {
        //Release audio.
        if (levelZero != null) {
            levelZero.releaseAudio();
        }
        if (levelOne != null) {
            levelOne.releaseAudio();
        }
        if (levelTwo != null) {
            levelTwo.releaseAudio();
        }
        if (levelThree != null) {
            levelThree.releaseAudio();
        }
    }


    public void pauseGame() {
        System.out.println("GV| pauseGame called");
        handleTouches = false;
        gameRunning = false;
        velocity = 0;
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
        releaseGameAudio();

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
        if(mA.initialized) {
            resumeGame();
        }

        setGameAudio();

        gameThread = new Thread(this);
        gameThread.start();

//        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
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

    public void resetVariables(){

        odometer = 0f;

        coins = 0;
        steps = 0;

        if(!mA.initialized || !screenInitialized) {
            sX = windowSize.x / 1080.0f;
            sY = windowSize.y / 1920.0f;
            //Scale finish line.
//            int testW = finishLine.getWidth();
//            int testH = finishLine.getHeight();
            finishLine = Bitmap.createScaledBitmap(finishLine, (int) (sX * 1080), (int) (sY * 383), true);
            screenInitialized = true;
        }

        switch (mA.locationState){
            case 0:
                if(levelZero == null){
                    levelZero = new LocationNoObstacles(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
                    releaseGameAudio();
                    levelOne = null;
                    levelTwo = null;
                    levelThree = null;
                    setGameAudio();
//                    background = BitmapFactory.decodeResource(this.getResources(), levelZero.getBackgroundResId(), ops);
//                    setBackgroundSizePos(windowSize);
                }

                fpMode = !mA.timeTrial2Flag;
                levelZero.setFpMode(fpMode);

                courseDistance = levelZero.getCourseDistance();
                difficultyIncreaseSeparation = levelZero.getIncreaseDifficultyDistance();
                levelZero.setBackgroundWidth(backgroundWidth);
                levelZero.setBackgroundHeight(backgroundHeight);
                levelZero.resetObstacles();
                break;
            case 1:
                if(levelOne == null){
                    levelOne = new LocationStationaryObstacles(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
                    releaseGameAudio();
                    levelZero = null;
                    levelTwo = null;
                    levelThree = null;
                    setGameAudio();
//                    background = BitmapFactory.decodeResource(this.getResources(), levelOne.getBackgroundResId(), ops);
//                    setBackgroundSizePos(windowSize);
                }

                fpMode = true;
                levelOne.setFpMode(fpMode);

                courseDistance = levelOne.getCourseDistance();
                difficultyIncreaseSeparation = levelOne.getIncreaseDifficultyDistance();
                levelOne.setBackgroundWidth(backgroundWidth);
                levelOne.setBackgroundHeight(backgroundHeight);
                levelOne.resetObstacles();
                break;
            case 2:
                if(levelTwo == null){
                    levelTwo = new LocationNormalRoad(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
                    releaseGameAudio();
                    levelZero = null;
                    levelOne = null;
                    levelThree = null;
                    setGameAudio();
//                    background = BitmapFactory.decodeResource(this.getResources(), levelTwo.getBackgroundResId(), ops);
//                    setBackgroundSizePos(windowSize);
                }

                fpMode = true;
                levelTwo.setFpMode(fpMode);

                courseDistance = levelTwo.getCourseDistance();
                difficultyIncreaseSeparation = levelTwo.getIncreaseDifficultyDistance();
                levelTwo.setBackgroundWidth(backgroundWidth);
                levelTwo.setBackgroundHeight(backgroundHeight);
                levelTwo.resetObstacles();
                break;
            case 3:
                if(levelThree == null){
                    levelThree = new LocationCrazyRoad(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
                    releaseGameAudio();
                    levelZero = null;
                    levelOne = null;
                    levelTwo = null;
                    setGameAudio();
//                    background = BitmapFactory.decodeResource(this.getResources(), levelThree.getBackgroundResId(), ops);
//                    setBackgroundSizePos(windowSize);
                }

                fpMode = true;
                levelThree.setFpMode(fpMode);

                courseDistance = levelThree.getCourseDistance();
                difficultyIncreaseSeparation = levelThree.getIncreaseDifficultyDistance();
                levelThree.setBackgroundWidth(backgroundWidth);
                levelThree.setBackgroundHeight(backgroundHeight);
                levelThree.resetObstacles();
                break;
        }
        courseLeft = courseDistance;
        distRemaining = courseDistance;
        difficultly = 0;
        toNextDiffIncrease = difficultyIncreaseSeparation;

        yourDistance = 0f;

        if(!distanceMode){
            livesLeft = 0;
        } else {
            livesLeft = 2;
        }
        collisionsWitnessed = 0;

        simulatedTouch = false;

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
