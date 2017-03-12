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
    Bitmap finishLine;

    float toNextDiffIncrease;
    float difficultyIncreaseSeparation;
    int difficultly;

    float sX;
    float sY;

    int livesLeft;
    int collisionsWitnessed;
    LocationOne locationOne;
    LocationTwo locationTwo;
    LocationThree locationThree;
    //-----------------------------------------------------------------------------------------

//    private SensorManager sensorMan;
//    private Sensor accelerometer;
//    private float[] mGravity;
//    private float mAccel;
//    private float mAccelCurrent;
//    private float mAccelLast;

    MainActivity mA;
    int locationState;

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
        mA = mainActivity;
        this.windowSize = p;
        holder = getHolder();
        paint = new Paint();

        sX = p.x / 1080.0f;
        sY = p.y / 1920.0f;

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

//        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.road2, ops);
        //TODO: should initialize a location only when needed, but it takes a while so doing both here because there's only two at the moment.
        locationOne = new LocationOne(mA, this, sX, sY, backgroundWidth, backgroundHeight);
        locationTwo = new LocationTwo(mA, this, sX, sY, backgroundWidth, backgroundHeight);
        locationThree = new LocationThree(mA, this, sX, sY, backgroundWidth, backgroundHeight);
        switch (mA.locationState){
            case 0:
//                locationOne = new LocationOne(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                background = BitmapFactory.decodeResource(this.getResources(), locationOne.getBackgroundResId(), ops);
                break;
            case 1:
//                locationTwo = new LocationTwo(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                background = BitmapFactory.decodeResource(this.getResources(), locationTwo.getBackgroundResId(), ops);
                break;
            case 2:
//                locationThree = new LocationThree(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                background = BitmapFactory.decodeResource(this.getResources(), locationThree.getBackgroundResId(), ops);
                break;
        }

        //System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        setBackgroundSizePos(p);


        //Initialize finish line.
        finishLine = BitmapFactory.decodeResource(this.getResources(), R.drawable.finish, null);
        finishLine = Bitmap.createScaledBitmap(finishLine, (int)(sX * 940), (int)(sY * 200), true);


//        sensorMan = (SensorManager) this.mA.getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        mAccel = 0.00f;
//        mAccelCurrent = SensorManager.GRAVITY_EARTH;
//        mAccelLast = SensorManager.GRAVITY_EARTH;


        resetVariables();
    }

    public void setBackgroundSizePos(Point p) {
        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);
        backgroundPositionY = 0;
        backgroundPositionY2 = -background.getHeight();
        //For some reason, these need to be here to get initialized correctly the first time.
        backgroundWidth = background.getWidth();
        backgroundHeight = background.getHeight();
    }

    // Bitmap processing
    // http://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android#17839663
    // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // If image is larger in either dimension than the requested dimension
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

                switch (locationState){
                    case 0:
                        velocity *= locationOne.getVelocityFactor();
                        break;
                    case 1:
                        velocity *= locationTwo.getVelocityFactor();
                        break;
                    case 2:
                        velocity *= locationThree.getVelocityFactor();
                        break;
                }
//                velocity *= 0.75;//0.8f;//0.9f;
            }

            //--------------Check if an obstacle has run into touchFollower when no fingers are down-----------------------------
            switch (locationState){
                case 0:
                    locationOne.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
                case 1:
                    locationTwo.checkIfObstacleRanIntoTouchFollower(livesLeft);
                    break;
                case 2:
                    locationThree.checkCollisions();
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
            switch (locationState){
                case 0:
                    locationOne.updateTouchFollower();
                    break;
                case 1:
                    locationTwo.updateTouchFollower();
                    break;
                case 2:
                    //do nothing
                    break;
            }
            switch (locationState){
                case 0:
                    locationOne.updateHomingObstacle();
                    break;
                case 1:
                    locationTwo.updateHomingObstacle();
                    break;
                case 2:
                    locationThree.updateHomingObstacle();
                    break;
            }
            //--------------------------------------------------------------------------------------

            //For the touchFollower crossing the finish line to trigger course completion, use the following:
            switch (locationState){
                case 0:
                    distRemaining = courseLeft - backgroundHeight + locationOne.getTFY() + locationOne.getTouchFollowerHeight();
                    break;
                case 1:
                    distRemaining = courseLeft - backgroundHeight + locationTwo.getTFY() + locationTwo.getTouchFollowerHeight();
                    break;
                case 2:
                    distRemaining = courseLeft - backgroundHeight + locationThree.getTouchDownY() - locationThree.getFootprintHeight()/2;
            }
            //For a touch past the finish line to trigger course completion, use the following line instead of the previous:
//            distRemaining = courseLeft - backgroundHeight + touchDownY;

            //----------Increase difficulty during distance mode.-----------------------------------
//            if(distanceMode){
                if(toNextDiffIncrease <= 0){
                    difficultly++;
                    switch (locationState){
                        case 0:
                            locationOne.updateObstacleSeparation();
                            break;
                        case 1:
                            locationTwo.updateObstacleSeparation();
                            break;
                        case 2:
                            locationThree.updateObstacleSeparation();
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
                            getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
                        }
                    });
                }
            }

            //------Move obstacles(any movement independent from the road).-----
            switch (locationState){
                case 0:
                    locationOne.move();
                    break;
                case 1:
                    locationTwo.move();
                    break;
                case 2:
                    locationThree.move();
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
                if(distanceMode == false) {
                    gameTimer.changeTime(time);
//                    mA.timer.setText(gameTimer.getTimeForDisplay());
                    mA.timer.setText(gameTimer.getTimeForDisplay() + "\n" + String.format("%.0f", (courseDistance - distRemaining)/courseDistance*100) + "%");
                } else {
                    //Display distance instead of time for distance mode.
//                    mA.timer.setText(String.valueOf(odometer + backgroundHeight - tFY - touchFollowerHeight) + "\nLives: " + String.valueOf(livesLeft));
                    switch (locationState){
                        case 0:
                            mA.timer.setText(String.format("%.1f", odometer + backgroundHeight - locationOne.getTFY() - locationOne.getTouchFollowerHeight()) + "\nLives: " + String.valueOf(livesLeft));
                            break;
                        case 1:
                            mA.timer.setText(String.format("%.1f", odometer + backgroundHeight - locationTwo.getTFY() - locationTwo.getTouchFollowerHeight()) + "\nLives: " + String.valueOf(livesLeft));
                            break;
                        case 2:
                            mA.timer.setText(String.format("%.1f", odometer + backgroundHeight - locationThree.getTouchDownY() + locationThree.getFootprintHeight()/2) + "\nLives: " + String.valueOf(livesLeft));
                            break;
                    }
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
            switch (locationState){
                case 0:
                    locationOne.draw(canvas, paint);
                    break;
                case 1:
                    locationTwo.draw(canvas, paint);
                    break;
                case 2:
                    locationThree.draw(canvas, paint);
            }
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

//                touchDownY = activeFinger.y;
//                touchDownX = activeFinger.x;
                switch (locationState){
                    case 0:
                        locationOne.setTouchDownX(activeFinger.x);
                        locationOne.setTouchDownY(activeFinger.y);
                        break;
                    case 1:
                        locationTwo.setTouchDownX(activeFinger.x);
                        locationTwo.setTouchDownY(activeFinger.y);
                        break;
                    case 2:
                        locationThree.setTouchDownX(activeFinger.x);
                        locationThree.setTouchDownY(activeFinger.y);
                        locationThree.spawnFootprint();
                        break;
                }

                //Check if finish line has been reached.
                if(distanceMode == false) {
                    if (distRemaining <= 0) {//odometer > courseDistance){
                        mA.setGameState(MainActivity.GAME_WON);
                    }
                }

//                footprints.spawnObstacle(0f, touchDownX - footprints.getObstacleWidth()/2, touchDownY - footprints.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.

                //--------------Check if an obstacle has been touched-----------------------------
                switch (locationState){
                    case 0:
                        locationOne.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 1:
                        locationTwo.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 2:
                        locationThree.checkIfObstacleWasTouched(livesLeft);
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
                                locationOne.setTouchDownX(activeFinger.x);
                                locationOne.setTouchDownY(activeFinger.y);
                                break;
                            case 1:
                                locationTwo.setTouchDownX(activeFinger.x);
                                locationTwo.setTouchDownY(activeFinger.y);
                                break;
                            case 2:
                                locationThree.setTouchDownX(activeFinger.x);
                                locationThree.setTouchDownY(activeFinger.y);
                                locationThree.spawnFootprint();
                                break;
                        }

//                        footprints.spawnObstacle(0f, touchDownX - footprints.getObstacleWidth()/2, touchDownY - footprints.getObstacleHeight()); //Any value <=0 to spawn a footprint. Center x and offset y by height.
                    }

                    //--------------Check if an obstacle has been touched-----------------------------
                    switch (locationState){
                        case 0:
                            locationOne.checkIfObstacleWasTouched(livesLeft);
                            break;
                        case 1:
                            locationTwo.checkIfObstacleWasTouched(livesLeft);
                            break;
                        case 2:
                            locationThree.checkIfObstacleWasTouched(livesLeft);
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
                        locationOne.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 1:
                        locationTwo.checkIfObstacleWasTouched(livesLeft);
                        break;
                    case 2:
//                        locationThree.checkCollisions();
                        locationThree.setTouchDownX(activeFinger.x);
                        locationThree.setTouchDownY(activeFinger.y);
                        locationThree.checkIfObstacleWasTouched(livesLeft);
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
                dist *= locationOne.getDistanceFactor();
                break;
            case 1:
                dist *= locationTwo.getDistanceFactor();
                break;
            case 2:
                dist *= locationThree.getDistanceFactor();
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


        switch (locationState){
            case 0:
                //Reduce velocity by factor.
                velocity = distance * locationOne.getInertiaFactor();
                //Reduce distance by factor.
                distance *= locationOne.getDistanceFactor();
                //Update touchFollowerer and obstacles.
                locationOne.updateTouchDownY(distance);
                locationOne.updateTFY(distance);
                locationOne.updateObs(distance);
                break;
            case 1:
                //Reduce velocity by factor.
                velocity = distance * locationTwo.getInertiaFactor();
                //Reduce distance by factor.
                distance *= locationTwo.getDistanceFactor();
                //Update touchFollower and obstacles.
                locationTwo.updateTouchDownY(distance);
                locationTwo.updateTFY(distance);
                locationTwo.updateObs(distance);
                break;
            case 2:
                //Reduce velocity by factor.
                velocity = distance * locationThree.getInertiaFactor();
                //Reduce distance by factor.
                distance *= locationThree.getDistanceFactor();
                //Update touchFollower and obstacles.
                locationThree.updateTouchDownY(distance);
                locationThree.updateObs(distance);
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


//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            mGravity = event.values.clone();
//            // Shake detection
//            float x = mGravity[0];
//            float y = mGravity[1];
//            float z = mGravity[2];
//            mAccelLast = mAccelCurrent;
//            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
//            float delta = mAccelCurrent - mAccelLast;
//            mAccel = mAccel * 0.9f + delta;
//            // Make this higher or lower according to how much
//            // motion you want to detect
//            if(mAccel > 3){
//                // do something
//                switch (locationState){
//                    case 0:
//                        if(locationOne.getTouchDownX() > 0 && locationOne.getTouchDownX() < windowSize.x) {
//                            locationOne.updateTouchDownX(x);
//                        }
//                        break;
//                    case 1:
//                        if(locationTwo.getTouchDownX() > 0 && locationTwo.getTouchDownX() < windowSize.x){
//                            locationTwo.updateTouchDownX(x);
//                        }
//                }
//            }
//        }
//
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // required method
//    }


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
        pauseGame();
        playing = false;

        //Release audio. TODO: change to switch if only one location is initialized at a time.
        locationOne.releaseAudio();
        locationTwo.releaseAudio();
        locationThree.releaseAudio();
//        switch (locationState){
//            case 0:
//                locationOne.releaseAudio();
//                break;
//            case 1:
//                locationTwo.releaseAudio();
//                break;
//            case 2:
//                locationThree.releaseAudio();
//                break;
//        }

//        sensorMan.unregisterListener(this);

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

        //Set audio. TODO: change to switch if only one location is initialized at a time.
        locationOne.setAudio();
        locationTwo.setAudio();
        locationThree.setAudio();
//        switch (locationState){
//            case 0:
//                locationOne.setAudio();
//                break;
//            case 1:
//                locationTwo.setAudio();
//                break;
//            case 2:
//                locationThree.setAudio();
//                break;
//        }

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

        switch (mA.locationState){
            case 0:
                if(locationOne == null){
                    locationOne = new LocationOne(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
//                    locationTwo.releaseAudio();
//                    locationTwo = null;
//                    locationThree.releaseAudio();
//                    locationThree = null;
                    background = BitmapFactory.decodeResource(this.getResources(), locationOne.getBackgroundResId(), ops);
                    setBackgroundSizePos(windowSize);
                }
                courseDistance = locationOne.getCourseDistance();
                difficultyIncreaseSeparation = locationOne.getIncreaseDifficultyDistance();
                locationOne.setBackgroundWidth(backgroundWidth);
                locationOne.setBackgroundHeight(backgroundHeight);
                locationOne.resetObstacles();
                break;
            case 1:
                if(locationTwo == null){
                    locationTwo = new LocationTwo(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
//                    locationOne.releaseAudio();
//                    locationOne = null;
//                    locationThree.releaseAudio();
//                    locationThree = null;
                    background = BitmapFactory.decodeResource(this.getResources(), locationTwo.getBackgroundResId(), ops);
                    setBackgroundSizePos(windowSize);
                }
                courseDistance = locationTwo.getCourseDistance();
                difficultyIncreaseSeparation = locationTwo.getIncreaseDifficultyDistance();
                locationTwo.setBackgroundWidth(backgroundWidth);
                locationTwo.setBackgroundHeight(backgroundHeight);
                locationTwo.resetObstacles();
                break;
            case 2:
                if(locationThree == null){
                    locationThree = new LocationThree(mA, this, sX, sY, backgroundWidth, backgroundHeight);
                }
                if(locationState != mA.locationState) {
                    locationState = mA.locationState;
//                    locationOne.releaseAudio();
//                    locationOne = null;
//                    locationTwo.releaseAudio();
//                    locationTwo = null;
                    background = BitmapFactory.decodeResource(this.getResources(), locationThree.getBackgroundResId(), ops);
                    setBackgroundSizePos(windowSize);
                }
                courseDistance = locationThree.getCourseDistance();
                difficultyIncreaseSeparation = locationThree.getIncreaseDifficultyDistance();
                locationThree.setBackgroundWidth(backgroundWidth);
                locationThree.setBackgroundHeight(backgroundHeight);
                locationThree.resetObstacles();
                break;
        }
        courseLeft = courseDistance;
        distRemaining = courseDistance;
        toNextDiffIncrease = difficultyIncreaseSeparation;


        if(!distanceMode){
            livesLeft = 0;
        } else {
            livesLeft = 2;
        }
        collisionsWitnessed = 0;


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
