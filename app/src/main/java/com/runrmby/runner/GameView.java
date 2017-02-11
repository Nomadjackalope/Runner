package com.runrmby.runner;

import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Random;

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

    float velocity;

    Point windowSize;

    // Touches
    FingerPoint activeFinger = new FingerPoint();

    ArrayList<Integer> fingers = new ArrayList<>();

    //------Spawning Obstacles and finishing course--------------------------------------------
    float odometer = 0f;
    //Currently an arbitrary course distance to test.
    float courseDistance = 10000f;
    //Integer courseLength = 10; //Units of background art

    //Currently an arbitrary distance between obstacles to test. TODO: Make it slightly random.
    float distanceBetweenObstacles = 500f;
    float nextObstacleAt = distanceBetweenObstacles;
    float distanceToNextObstacle = distanceBetweenObstacles;
    Bitmap obstacle;
    int obstacleWidth;
    int obstacleHeight;
    int maxNumObstacles = 4;
    Boolean[] obstacleImageArray = new Boolean[maxNumObstacles];
    float[][] obstacleLocationArray = new float[maxNumObstacles][2];

    float distanceBetweenType2Obstacles = 800f;
    float nextType2ObstacleAt = distanceBetweenType2Obstacles;
    float distanceToNextType2Obstacle = distanceBetweenType2Obstacles;
    Bitmap obstacleType2;
    int obstacleType2Width;
    int obstacleType2Height;
    int obstacleType2HorizontalSpeed = 2; //Number of pixels to move horizontally every time draw updates. The implementation might need to be based on real time.
    int maxNumType2Obstacles = 2;
    Boolean[] obstacleType2SpawnArray = new Boolean[maxNumType2Obstacles];
    float[][] obstacleType2LocationArray = new float[maxNumType2Obstacles][2];

    static Boolean obstacleDestroyed = false;
    static Boolean obstacleSpawned = true;
    Random randomNum = new Random();
    //-----------------------------------------------------------------------------------------

    MainActivity mA;

    long gameTimeLeft;
    long gameLengthCountUp = 0; // 1000 = 1 second
    long gameLengthCountDown = 20000;
    long previousTime;

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

        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);

        obstacle = BitmapFactory.decodeResource(this.getResources(), R.drawable.practice3_small, null);
        obstacleWidth = obstacle.getWidth();
        obstacleHeight = obstacle.getHeight();

        obstacleType2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.practice3_small, null);
        obstacleType2Width = obstacleType2.getWidth();
        obstacleType2Height = obstacleType2.getHeight();

        mA = mainActivity;

        resetVariables();

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

        //------------------------Check if obstacles should be destroyed-------------------
        for(int i = 0; i < maxNumObstacles; i++){
            if(obstacleLocationArray[i][1] > background.getHeight()){
                obstacleImageArray[i] = obstacleDestroyed;
            }
        }

        for(int i = 0; i < maxNumType2Obstacles; i++){
            if(obstacleType2LocationArray[i][1] > background.getHeight()){
                obstacleType2SpawnArray[i] = obstacleDestroyed;
            }
        }
        //--------------------------------------------------------------------------------

        // if the user is not touching trigger inertia,
        // if we did it when the users finger was down it would get out of sync
        if(fingers.isEmpty()) {
            // We don't want this running all the time using up cpu
            if(velocity > 1) {
                advanceRoad(velocity); ///0.016f);
                velocity *= 0.75;//0.8f;//0.9f;
            }
        }

        if(fingerMoveDist > 0) {
            advanceRoad(fingerMoveDist);
            fingerMoveDist = 0;
        }

        // Update Time using delta time compared to last time update was run
        gameTimeLeft -= System.currentTimeMillis() - previousTime;
        previousTime = System.currentTimeMillis();

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
            for(int i = 0; i < maxNumObstacles; i++) {
                if(obstacleImageArray[i] == obstacleSpawned) {
                    canvas.drawBitmap(obstacle, obstacleLocationArray[i][0], obstacleLocationArray[i][1], paint);
                }
            }

            for(int i = 0; i < maxNumType2Obstacles; i++) {
                if(obstacleType2SpawnArray[i] == obstacleSpawned) {
                    if(obstacleType2LocationArray[i][0] < background.getWidth()){
                        obstacleType2LocationArray[i][0] += obstacleType2HorizontalSpeed;
                    }
                    canvas.drawBitmap(obstacleType2, obstacleType2LocationArray[i][0], obstacleType2LocationArray[i][1], paint);
                }
            }
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
                for(int i = 0; i < maxNumObstacles; i++) {
                    if (obstacleImageArray[i] == obstacleSpawned) {
                        if (activeFinger.x > obstacleLocationArray[i][0] && activeFinger.x < obstacleLocationArray[i][0] + obstacleWidth) {
                            if (activeFinger.y > obstacleLocationArray[i][1] && activeFinger.y < obstacleLocationArray[i][1] + obstacleHeight) {
                                //Obstacle has been touched.
                                mA.requestGameState(MainActivity.LOSE);
                                System.out.println("HELLO!!");
                            }
                        }
                    }
                }

                for(int i = 0; i < maxNumType2Obstacles; i++) {
                    if (obstacleType2SpawnArray[i] == obstacleSpawned) {
                        if (activeFinger.x > obstacleType2LocationArray[i][0] && activeFinger.x < obstacleType2LocationArray[i][0] + obstacleType2Width) {
                            if (activeFinger.y > obstacleType2LocationArray[i][1] && activeFinger.y < obstacleType2LocationArray[i][1] + obstacleType2Height) {
                                //Obstacle has been touched.
                                mA.requestGameState(MainActivity.LOSE);
                                System.out.println("HELLO!!");
                            }
                        }
                    }
                }
                //---------------------------------------------------------------------------

                //Check if finish line has been reached.
                if(odometer > courseDistance){
                    mA.requestGameState(MainActivity.WIN);
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

        //---------------Spawning Obstacles---------------------------------------------------
        for(int i = 0; i < maxNumObstacles; i++){
            obstacleLocationArray[i][1] += distance;
        }
        //If enough distance has been covered, spawn an obstacle.
        distanceToNextObstacle = nextObstacleAt - odometer;
        if(distanceToNextObstacle <= 0){
            spawnObstacle();
            nextObstacleAt = distanceBetweenObstacles + distanceToNextObstacle + odometer;
        }

        for(int i = 0; i < maxNumType2Obstacles; i++){
            obstacleType2LocationArray[i][1] += distance;
        }
        distanceToNextType2Obstacle = nextType2ObstacleAt - odometer;
        if(distanceToNextType2Obstacle <= 0){
            spawnObstacleType2();
            nextType2ObstacleAt = distanceBetweenType2Obstacles + distanceToNextType2Obstacle + odometer;
        }
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

    //---------Mark attempting to spawn obstacles----------------------------------------------
    public void spawnObstacle(){
//        BitmapFactory.Options ops = new BitmapFactory.Options();
//        ops.inPreferredConfig = Bitmap.Config.RGB_565;
        for(int i = 0; i < maxNumObstacles; i++){
            if(obstacleImageArray[i] == obstacleDestroyed){
                //Set obstacle spawn image.
                obstacleImageArray[i] = obstacleSpawned;
                //Set obstacle spawn location.
                obstacleLocationArray[i][0] = randomNum.nextInt(background.getWidth()-obstacleWidth);
                obstacleLocationArray[i][1] = -obstacleHeight;
                break;
            }
        }
    }

    public void spawnObstacleType2(){
        for(int i = 0; i < maxNumType2Obstacles; i++){
            if(obstacleType2SpawnArray[i] == obstacleDestroyed) {
                obstacleType2SpawnArray[i] = obstacleSpawned;
                obstacleType2LocationArray[i][0] = randomNum.nextInt(background.getWidth() - obstacleType2Width);
                obstacleType2LocationArray[i][1] = -obstacleType2Height;
                break;
            }
        }
    }

    public void resetVariables(){
        odometer = 0f;
        nextObstacleAt = distanceBetweenObstacles;
        distanceToNextObstacle = distanceBetweenObstacles;
        for(int i = 0; i < maxNumObstacles; i++){
            obstacleImageArray[i] = obstacleDestroyed;
        }


        nextType2ObstacleAt = distanceBetweenType2Obstacles;
        distanceToNextType2Obstacle = distanceBetweenType2Obstacles;
        for(int i = 0; i < maxNumType2Obstacles; i++){
            obstacleType2SpawnArray[i] = obstacleDestroyed;
        }

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


    }
}
