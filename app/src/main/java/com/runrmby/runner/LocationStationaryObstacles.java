package com.runrmby.runner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by Mark on 3/14/2017.
 */

public class LocationStationaryObstacles {

    //No touchFollower, just footprintsR where fingers touch

    //boolean distanceMode;
    private float courseDistance = 30000f;  //Currently an arbitrary distance to the finish line.

    private Obstacles cone;
    private int coneResId = R.drawable.cone_xxhdpi;
    private int coneXScale = 74;
    private int coneYScale = 104;
    private int maxNumCones = 40;
    private float distBetweenCones = 500f;   //Initialized in resetVariables()
    private float coneXSpeed = 0f;
    private float coneYSpeed = 0f;

//    private Obstacles downTree;
//    private int downTreeResId = R.drawable.mossy_log_xxhdpi;
//    private int downTreeXScale = 464;
//    private int downTreeYScale = 105;
//    private int maxNumDownTrees = 4;
//    private float distBetweenDownTrees = 10000f;
//    private float downTreeXSpeed = 0f;
//    private float downTreeYSpeed = 0f;
//
//    private Obstacles downTree2;
//    private int downTree2ResId = R.drawable.down_tree_xxhdpi;
//    private int downTree2XScale = 865;
//    private int downTree2YScale = 198;
//    private int maxNumDownTrees2 = 2;
//    private float distBetweenDownTrees2 = 25000f;
//    private float downTree2XSpeed = 0f;
//    private float downTree2YSpeed = 0f;

    private Obstacles truck;
    private int truckResId = R.drawable.race_car_xxhdpi;
    private int truckXScale = 279;
    private int truckYScale = 487;
    private int maxNumTrucks = 1;
    private float distBetweenTrucks = 29000f;
    private float truckXSpeed = 0f;
    private float truckYSpeed = 0f;

    boolean racerTrigger = false;
//
//    private Obstacles crowd;
//    private int crowdResId = R.drawable.dude;
//    private int crowdXScale = 122;
//    private int crowdYScale = 154;
//    private int maxNumCrowds = 0;
//    private float distBetweenCrowds = 2500f;
//    private float crowdXSpeed = 3f;
//    private float crowdYSpeed = 10f;
//
//    private Obstacles car;
//    private int carResId = R.drawable.race_car_xxhdpi;
//    private int carXScale = 279;
//    private int carYScale = 487;
//    private int maxNumCars = 1;
//    private float distBetweenCars = 50000f;
//    private float carXSpeed = 0f;
//    private float carYSpeed = 18f;

    private Obstacles homingOb;
    private int homingObResID = R.drawable.dude_xxhdpi;
    private int homingObXScale = 172;
    private int homingObYScale = 204;
    private int homingObWidth;
    private int homingObHeight;
    private int homingObMaxNum = 1;
    private float homingObDistBetween = 75000f;
    private float homingObXSpeed = 0f;
    private float homingObYSpeed = 0f;
    private float homingObHomingSpeed = 0.02f;

    private float increaseDifficultyDistance = 1500f;
    private float toNextDiffIncrease;
    private int difficultly;
    //float homingObHomingSpeed;

    private Obstacles coins;
    private int coinsResId = R.drawable.coin;
    private int coinScale = 75;
    private int maxNumCoins = 4;
    private float distBetweenCoins = 1200f;
    private float coinsHorizontalSpeed = 0f;
    private float coinsVerticalSpeed = 0f;

//    private Bitmap touchFollower;
//    private int touchFollowerHeight;
//    private int touchFollowerWidth;
//    private int tFXScale = 115;
//    private int tFYScale = 213;
//    private float tFX;          //current x coordinate of touchFollower
//    private float tFY;          //current y coordinate of touchFollower
//    private float tFXOffset;
//    private float tFYOffset;
    private float touchDownX;   //desired x coordinate of touchFollower.
    private float touchDownY;   //desired y coordinate of touchFollower.
//    private float tFSpeed = 0.15f;

    private Obstacles footprintsR;
    private int footprintsRImageResId = R.drawable.right_foot_yellow_xxhdpi;
    private int footprintsHeight;
    private int footprintsWidth;
    private int footprintsXScale = 80;//100;
    private int footprintsYScale = 160;//230;
    private int maxNumFootprints = 1;
//    private float fRTempX;
//    private float fRTempY;

    private Obstacles footprintsL;
    private int footprintsLImageResId = R.drawable.left_foot_yellow_xxhdpi;
//    private float fLTempX;
//    private float fLTempY;

    private Matrix matrixRotateClockwise = new Matrix();
    private Matrix matrixRotateCounterClockwise = new Matrix();
    private Boolean tFRotated = false;

    private float velocityFactor = .75f; //Must be less than 1 or else road will advance exponentially.
    private float distanceFactor = 1.00f;  //Must be <= 1 or else road will advance exponentially.
    private float inertiaFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.

    //Sound effects
    private SoundPool soundEffects;
    private int badSoundId;
    private int goodSoundId;
    private int crashSoundId;
    private int crashSound2Id;
    private int crashSound3Id;
    private int crashSound4Id;
    private int crashSound5Id;
    private int truckHornId;
    private int carHornId;
    private int wilhelmScreamId;
    private int driveSoundId;

    private MainActivity mA;
    private GameView gS;
    private float sX;
    private float sY;

    private int backgroundResId = R.mipmap.road_0;
    private int backgroundWidth;
    private int backgroundHeight;

    private float lastY;
    private boolean stepFlag;
    boolean hopping;

    Context context;

    boolean fpMode = false; //If false, using touchFollower character instead of fpMode.

//    boolean timeTrial2Flag = false;

    public LocationStationaryObstacles(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
        this.mA = mA;
        this.gS =  gS;
        this.sX = x;
        this.sY = y;
        this.backgroundWidth = bW;
        this.backgroundHeight = bH;
        this.context = gS.getContext();
        //-----------------Initialize obstacles----------------------------------------------------
        homingObWidth = (int) (sX * homingObXScale);
        homingObHeight = (int) (sY * homingObYScale);

        cone = new Obstacles(context, (int) (sX * coneXScale), (int) (sY * coneYScale), coneResId, maxNumCones, false, distBetweenCones, coneXSpeed, coneYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
//        downTree = new Obstacles(context, (int) (sX * downTreeXScale), (int) (sY * downTreeYScale), downTreeResId, maxNumDownTrees, false, distBetweenDownTrees, downTreeXSpeed, downTreeYSpeed, 0, backgroundWidth, backgroundHeight, false, false, false);
//        downTree2 = new Obstacles(context, (int) (sX * downTree2XScale), (int) (sY * downTree2YScale), downTree2ResId, maxNumDownTrees2, false, distBetweenDownTrees2, downTree2XSpeed, downTree2YSpeed, 0, backgroundWidth, backgroundHeight, false, false, false);
        truck = new Obstacles(context, (int) (sX * truckXScale), (int) (sY * truckYScale), truckResId, maxNumTrucks, false, distBetweenTrucks, truckXSpeed, truckYSpeed, 0, backgroundWidth, backgroundHeight, false, true, false);
//        crowd = new Obstacles(context, (int) (sX * crowdXScale), (int) (sY * crowdYScale), crowdResId, maxNumCrowds, false, distBetweenCrowds, crowdXSpeed, crowdYSpeed, 0, backgroundWidth, backgroundHeight, true, false);
//        car = new Obstacles(context, (int) (sX * carXScale), (int) (sY * carYScale), carResId, maxNumCars, false, distBetweenCars, carXSpeed, carYSpeed, 0, backgroundWidth, backgroundHeight, true, true);
        homingOb = new Obstacles(context, homingObWidth, homingObHeight, homingObResID, homingObMaxNum, false, homingObDistBetween, homingObXSpeed, homingObYSpeed, homingObHomingSpeed, backgroundWidth, backgroundHeight + backgroundHeight, false, false, false);
        coins = new Obstacles(context, (int) (sX * coinScale), (int) (sY * coinScale), coinsResId, maxNumCoins, false, distBetweenCoins, coinsHorizontalSpeed, coinsVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        cone.setMultiSpawn(true);
        truck.setLimitSpawnX(100, backgroundWidth - 100, false);
        truck.setSpawnBottom(true);
        truck.setFlipY(true);

        //Initialize fpMode
        footprintsWidth = (int)(sX * footprintsXScale);
        footprintsHeight = (int)(sY * footprintsYScale);
        footprintsR = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsRImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsR.setRotatedObsImage(R.drawable.right_foot_yellow_transparent_xxhdpi, (int)(sX*242), (int)(sY*556));
//        footprintsR.setBlink(5, 2);
        footprintsL = new Obstacles(context, footprintsWidth, footprintsHeight, footprintsLImageResId, maxNumFootprints, true, 0, 0, 0, 0, backgroundWidth, backgroundHeight, false, false, false);
        footprintsL.setRotatedObsImage(R.drawable.left_foot_yellow_transparent_xxhdpi, (int)(sX*242), (int)(sY*556));
//        footprintsL.setBlink(5,2);

//        //Initialize touch follower.
//        touchFollowerHeight = (int) (sY * tFYScale);
//        touchFollowerWidth = (int) (sX * tFXScale);
//        touchFollower = BitmapFactory.decodeResource(context.getResources(), R.drawable.fatty, null);
//        touchFollower = Bitmap.createScaledBitmap(touchFollower, touchFollowerWidth, touchFollowerHeight, true);

        matrixRotateClockwise.postRotate(90);
        matrixRotateCounterClockwise.postRotate(270);

//        tFXOffset = -touchFollower.getWidth() / 2;
//        tFYOffset = -touchFollower.getHeight();

        //Sound effects.
        setAudio();
    }

    public float getCourseDistance(){
        return this.courseDistance;
    }

//    public float getTFY(){
//        return this.tFY;
//    }
//
//    public int getTouchFollowerHeight(){
//        return this.touchFollowerHeight;
//    }

    public float getIncreaseDifficultyDistance(){
        return this.increaseDifficultyDistance;
    }

    public int getBackgroundResId(){
        return backgroundResId;
    }

    public float getInertiaFactor(){
        return inertiaFactor;
    }

    public float getDistanceFactor(){
        return distanceFactor;
    }

    public float getVelocityFactor(){
        return velocityFactor;
    }

    public float getTouchDownX(){ return touchDownX;}

    public float getTouchDownY(){return touchDownY;}

    public float getFootprintHeight(){return footprintsHeight;}

    public void setBackgroundWidth(int w){
        this.backgroundWidth = w;

    }

    public void setBackgroundHeight(int h){
        this.backgroundHeight = h;
    }

    public void updateTouchDownX(float d) { this.touchDownX += d; }

    public void updateTouchDownY(float d){
        this.touchDownY += d;
    }

    public void spawnFootprint(){
        float x = touchDownX - footprintsWidth/2;
        float y = touchDownY - footprintsHeight/2;
        if(footprintsL.spawnTracker[0] != 2 && footprintsR.spawnTracker[0] != 2 || footprintsL.spawnTracker[0] == 0 && footprintsR.spawnTracker[0] == 0) {//both or no fpMode spawned
//            if(fRTempY == fLTempY){//neither foot ahead (start)
            if (touchDownX < backgroundWidth / 2) {//if touch is on left half of screen, spawn left footprint, otherwise spawn right
                footprintsL.spawnObstacle(0f, x, y, true);
//                fLTempX = footprintsL.coordinatesArray[0][0];
//                fLTempY = footprintsL.coordinatesArray[0][1];
                if (footprintsR.spawnTracker[0] == 1) {
//                        footprintsR.spawnTracker[0] = 2;
//                    fRTempX = footprintsR.coordinatesArray[0][0];
//                    fRTempY = footprintsR.coordinatesArray[0][1];
                    footprintsR.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsR.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsR.hitObstacle(0, false, false, false, true);
                }
            } else {
                footprintsR.spawnObstacle(0f, x, y, true);
//                fRTempX = footprintsR.coordinatesArray[0][0];
//                fRTempY = footprintsR.coordinatesArray[0][1];
                if (footprintsL.spawnTracker[0] == 1) {
//                        footprintsL.spawnTracker[0] = 2;
//                    fLTempX = footprintsL.coordinatesArray[0][0];
//                    fLTempY = footprintsL.coordinatesArray[0][1];
                    footprintsL.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsL.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsL.hitObstacle(0, false, false, false, true);
                }
            }
        } else if (footprintsL.spawnTracker[0] == 1) {//Left foot down.
            if (x < footprintsL.coordinatesArray[0][0] + footprintsWidth) {//Touch is to left of right side of left foot.
                //Spawn left foot.
                footprintsL.spawnObstacle(0f, x, y, true);
//                fLTempX = footprintsL.coordinatesArray[0][0];
//                fLTempY = footprintsL.coordinatesArray[0][1];
                hopping = true;
            } else {//Touch is to right of right side of left foot.
                //Spawn right foot.
                footprintsR.spawnObstacle(0f, x, y, true);
//                fRTempX = footprintsR.coordinatesArray[0][0];
//                fRTempY = footprintsR.coordinatesArray[0][1];
                //"Lift" left foot.
                if (footprintsL.spawnTracker[0] == 1) {
//                        footprintsL.spawnTracker[0] = 2;
//                    fLTempX = footprintsL.coordinatesArray[0][0];
//                    fLTempY = footprintsL.coordinatesArray[0][1];
                    footprintsL.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsL.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsL.hitObstacle(0, false, false, false, true);
                }
                hopping = false;
            }
        } else {//Right foot down.
            if (x < footprintsR.coordinatesArray[0][0] - footprintsWidth) {//Touch is to left of left side of right foot.
                //Spawn left foot.
                footprintsL.spawnObstacle(0f, x, y, true);
//                fLTempX = footprintsL.coordinatesArray[0][0];
//                fLTempY = footprintsL.coordinatesArray[0][1];
                //"Lift" right foot.
                if (footprintsR.spawnTracker[0] == 1) {
//                        footprintsR.spawnTracker[0] = 2;
//                    fRTempX = footprintsR.coordinatesArray[0][0];
//                    fRTempY = footprintsR.coordinatesArray[0][1];
                    footprintsR.coordinatesArray[0][0] -= footprintsWidth / 2;
                    footprintsR.coordinatesArray[0][1] -= footprintsHeight / 2;
                    footprintsR.hitObstacle(0, false, false, false, true);
                }
                hopping = false;
            } else {//Touch is to right of left side of right foot.
                //Spawn right foot.
                footprintsR.spawnObstacle(0f, x, y, true);
//                fRTempX = footprintsR.coordinatesArray[0][0];
//                fRTempY = footprintsR.coordinatesArray[0][1];
                hopping = true;
            }
        }

        if (!mA.musicMuted) {
            soundEffects.play(crashSound5Id, 1f, 1f, 0, 0, 1);
        }
    }


//    public void updateTFY(float d){
//        this.tFY += d;
//    }

    public void updateHomingSpeed(float factor){
        homingOb.homingSpeed += homingObHomingSpeed * factor;
    }

    public void updateHomingObstacle(){
        for (int i = 0; i < homingObMaxNum; i++) {
            if (homingOb.spawnTracker[i] == 1) {
//                if(!gS.fingers.isEmpty()) {
                    if (homingOb.coordinatesArray[i][0] + homingObWidth/2 != touchDownX) {
//                        homingOb.coordinatesArray[i][0] += homingOb.speedArray[i][2] * (touchDownX - homingOb.coordinatesArray[i][0] - homingObWidth/2);
                        homingOb.speedArray[i][0] = homingOb.speedArray[i][2] * (touchDownX - homingOb.coordinatesArray[i][0] - homingObWidth/2);
                    }
                    if (homingOb.coordinatesArray[i][1] + homingObHeight/2 != touchDownY) {
//                        homingOb.coordinatesArray[i][1] += homingOb.speedArray[i][3] * (touchDownY - homingOb.coordinatesArray[i][1] - homingObHeight/2);//homingOb.speedArray[i][3]*(.0001f*(touchDownY - homingOb.coordinatesArray[i][1])*(touchDownY + homingOb.coordinatesArray[i][1]) + 4*(touchDownY - homingOb.coordinatesArray[i][1]));
                        homingOb.speedArray[i][1] = homingOb.speedArray[i][3] * (touchDownY - homingOb.coordinatesArray[i][1] - homingObHeight/2);
                    }
//                }
            }
        }
    }

//    public void updateTouchFollower(){
//        if(tFX != touchDownX + tFXOffset) {
//            tFX += tFSpeed * (touchDownX + tFXOffset - tFX);
//        }
//        if(tFY != touchDownY + tFYOffset){
//            tFY += tFSpeed * (touchDownY + tFYOffset - tFY);
//        }
//        //Prevent touchFollower from disappearing off bottom of screen.
//        if(tFY > backgroundHeight + tFYOffset){
//            tFY = backgroundHeight + tFYOffset;
//        }
//    }

    public void updateDifficulty(int difficultly){
        updateObstacleSeparation();
        this.difficultly = difficultly;
    }

    public void updateObstacleSeparation() {
        //TODO: make factor global
        float factor = (difficultly + 49f) / (difficultly + 50f);
//        downTree.updateDistanceBetweenObstacles(factor);
//        downTree2.updateDistanceBetweenObstacles(factor);
        if(!racerTrigger) {
            truck.updateDistanceBetweenObstacles(factor);
        }
//        distBetweenCrowds *= factor;
//        crowd.setDistanceBetweenObstacles(distBetweenCrowds);
//        distBetweenCars *= factor;
//        car.setDistanceBetweenObstacles(distBetweenCars);
        homingOb.updateDistanceBetweenObstacles(factor);

        factor = 1f / (difficultly + 8f);
        truck.increaseVerticalSpeed(factor);

        factor = (difficultly + 9f) / (difficultly + 10f);
        cone.updateDistanceBetweenObstacles(factor);

//        updateHomingSpeed(1f / (difficultly + 8f));
    }

//    public void resetTF(){
//        tFX = tFXOffset + backgroundWidth/2;
//        tFY = tFYOffset + backgroundHeight;
//    }

    public void setTouchDownX(float x){
        this.touchDownX = x;
    }

    public void setTouchDownY(float y){
        this.touchDownY = y;
        if(fpMode){
            this.lastY = this.touchDownY;
            stepFlag = true;
            spawnFootprint();
        }
    }

    //---------------------Draw obstacles---------------------------------------------
    public void draw(Canvas canvas, Paint paint, float interpolation, float velocity) {

        coins.drawObstacles(canvas, paint, interpolation, velocity);
        cone.drawObstacles(canvas, paint, interpolation, velocity);
//        downTree.drawObstacles(canvas, paint, interpolation, velocity);
//        downTree2.drawObstacles(canvas, paint, interpolation, velocity);
//        crowd.drawObstacles(canvas, paint);
//        car.drawObstacles(canvas, paint);
        homingOb.drawObstacles(canvas, paint, interpolation, velocity);
        truck.drawObstacles(canvas, paint, interpolation, velocity);

        //Draw last so is the top layer.
        if(fpMode) {
            footprintsR.drawObstacles(canvas, paint, interpolation, velocity);
            footprintsL.drawObstacles(canvas, paint, interpolation, velocity);
        }
//        else {
//            //Draw touch follower.
//            canvas.drawBitmap(touchFollower, tFX, tFY, paint);  //TODO: implement interpolation.
//        }
    }

    //--------------Check if an obstacle has run into touchFollower when no fingers are down (only necessary if friendly sprite triggers obstacles)-----------------------------
    public void checkIfObstacleRanIntoTouchFollower(int livesLeft) {
        if (checkObstaclesTouched(true)) {//Looks like if(false), but false is a passed boolean.
            if (livesLeft == 0) {
                mA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gS.simulatedTouch = true;
                        gS.getRootView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1, MotionEvent.ACTION_DOWN, 0f, 0f, 0));
//                    mA.requestGameState(MainActivity.LOSE);
                    }
                });
            } else {
                checkObstaclesTouched(false);
                gS.velocity = 0;
//                if(!fpMode) {
//                    touchDownX = tFX - tFXOffset;
//                    touchDownY = tFY - tFYOffset;
//                }
                gS.livesLeft--;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
            }
        }
//        else if (fpMode){
            if(coins.wasObstacleTouched(touchDownX - footprintsWidth/2, touchDownY - footprintsHeight/2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
//                gS.livesLeft++;
                gS.coins++;
                if (!mA.musicMuted) {
                    soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
                }
            }
//        } else {
//            if (coins.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, true, false)) {
//                gS.livesLeft++;
//                if (!mA.musicMuted) {
//                    soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
//                }
//            }
//        }
    }

    //--------------Check if an obstacle has been touched-----------------------------
    public void checkIfObstacleWasTouched(int livesLeft) {
        if (checkObstaclesTouched(false)) {
            if (livesLeft == 0) {
//                if(!fpMode) {
//                    touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateClockwise, true);
//                    tFRotated = true;
//                }
                if (!mA.musicMuted) {
                    soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
                mA.setGameState(MainActivity.GAME_LOST);
            } else {
                gS.velocity = 0;
//                if(!fpMode) {
//                    touchDownX = tFX - tFXOffset;
//                    touchDownY = tFY - tFYOffset;
//                }
                --gS.livesLeft;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                    gS.collisionsWitnessed++;
                }
            }
        }
//        else if (fpMode){
            if(coins.wasObstacleTouched(touchDownX - footprintsWidth/2, touchDownY - footprintsHeight/2, footprintsWidth, footprintsHeight, true, true, true, false) != -1) {
//                gS.livesLeft++;
                gS.coins++;
                if (!mA.musicMuted) {
                    soundEffects.play(goodSoundId, 0.5f, 0.5f, 0, 0, 1);
                }
            }
//        } else {
//            if (coins.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, true, false)) {
//                gS.livesLeft++;
//                if (!mA.musicMuted) {
//                    soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
//                }
//            }
//        }
    }

    //---------------Update Obstacles---------------------------------------------------
    public void updateObs(float distance) {
        cone.updateObstacles(distance, true);
//        downTree.updateObstacles(distance, true);
//        downTree2.updateObstacles(distance, true);
        if (truck.updateObstacles(distance, true)) {
            if (!mA.musicMuted && racerTrigger) {
                soundEffects.play(driveSoundId, 0.5f, 0.5f, 1, 0, 2);
            }
        }
        //TODO: give car velocity after certain distance so it's hard to go really far.
        if(gS.odometer > 40000 && !racerTrigger){
            racerTrigger = true;
            truck.verticalSpeed = -24;
            truck.doAutoSpawn(0);
            truck.setDistanceBetweenObstacles(10000);
//            truck.randomizeParameters = true;
        }
//        crowd.updateObstacles(distance, true);
//        if (car.updateObstacles(distance, true)) {
//            if (!mA.musicMuted) {
//                soundEffects.play(carHornId, 0.5f, 0.5f, 0, 0, 1);
//            }
//        }
        homingOb.updateObstacles(distance, true);

//        if(gS.distanceMode) {
            coins.updateObstacles(distance, true);
//        }

        if(fpMode) {
            if (footprintsR.spawnTracker[0] != 2) {//Right foot is down.
                footprintsR.updateObstacles(distance, false);
            } else {//Right foot isn't down.
                if(footprintsR.coordinatesArray[0][0] < touchDownX + footprintsWidth || hopping) {
                    //If feet are crossed, move footprint that's not "down" to begin straightening out.
                    footprintsR.coordinatesArray[0][0] += 0.1 * (touchDownX + footprintsWidth - footprintsR.coordinatesArray[0][0]);
                }
                if(footprintsR.coordinatesArray[0][1] - lastY + footprintsHeight > 10 && stepFlag) {
                    footprintsR.coordinatesArray[0][1] += 0.1 * (lastY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                } else if (!stepFlag){
                    footprintsR.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                } else {
                    stepFlag = false;
                    footprintsR.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsR.coordinatesArray[0][1]);
                }
            }
//            fRTempY += distance;
            if (footprintsL.spawnTracker[0] != 2) {//Left foot is down.
                footprintsL.updateObstacles(distance, false);
            } else {//Left foot isn't down.
                if(footprintsL.coordinatesArray[0][0] > touchDownX - footprintsWidth*3 || hopping){
                    //If feet are crossed, move footprint that's not "down" to begin straightening out.
                    footprintsL.coordinatesArray[0][0] += 0.1 * (touchDownX - footprintsL.coordinatesArray[0][0] - footprintsWidth*3);
                }
                if(footprintsL.coordinatesArray[0][1] - lastY + footprintsHeight > 10 && stepFlag) {
                    footprintsL.coordinatesArray[0][1] += 0.1 * (lastY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                } else if(!stepFlag){
                    footprintsL.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                } else {
                    stepFlag = false;
                    footprintsL.coordinatesArray[0][1] += 0.01 * (touchDownY - footprintsHeight * 2 - footprintsL.coordinatesArray[0][1]);
                }
            }
//            fLTempY += distance;

            //Don't let last footprint go off bottom of screen (otherwise you could sort of cheat if you scrolled fast enough)
            if (gS.fingers.isEmpty()) {
                if (footprintsR.spawnTracker[0] == 0) {
//                if(footprintsL.coordinatesArray[0][1] + footprintsHeight > backgroundHeight){
//                    gS.velocity = 1;
                    gS.velocity *= (backgroundHeight - footprintsL.coordinatesArray[0][1] - footprintsHeight) / backgroundHeight;
//                }
                } else {
//                if(footprintsL.coordinatesArray[0][1] + footprintsHeight > backgroundHeight) {
//                    gS.velocity = 1;
                    gS.velocity *= (backgroundHeight - footprintsR.coordinatesArray[0][1] - footprintsHeight) / backgroundHeight;
//                }
                }
            }
        }
    }

    //Move obstacles(any movement independent from the road).
    public void move(){
        cone.moveObstacles();
//        downTree.moveObstacles();
//        downTree2.moveObstacles();
        truck.moveObstacles();
//        crowd.moveObstacles();
//        car.moveObstacles();
        homingOb.moveObstacles();
        coins.moveObstacles();
    }

    //--------------Check if an obstacle was touched-----------------------------
    public Boolean checkObstaclesTouched(boolean checkOnly) {

        //Collide other obstacles.
        checkCollisions();

        //Now check if touch follower is touching an obstacle.
//        if(!fpMode) {
//            if (cone.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
//                return true;
//            } else if (downTree.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
//                return true;
//            } else if (downTree2.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
//                return true;
//            } else if (truck.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
//                return true;
//            }
////            else if (crowd.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly)) {//(activeFinger.x, activeFinger.y)) {
////                return true;
////            }else if (car.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly)){
////                return true;
////            }
//            else if (homingOb.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){
//                return true;
//            } else {
//                return false;
//            }
//        } else {
            for (int i = 0; i < maxNumFootprints; i++) {
                if (footprintsR.spawnTracker[i] == 1) {
                    if (cone.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
//                    else if (downTree.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
//                        return true;
//                    } else if (downTree2.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
//                        return true;
//                    }
                    else if (truck.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
// else if (crowd.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly)) {
//                        return true;
//                    } else if (car.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly)) {
//                        return true;
//                    }
                    else if (homingOb.wasObstacleTouched(footprintsR.coordinatesArray[i][0], footprintsR.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
                }
                if (footprintsL.spawnTracker[i] == 1) {
                    if (cone.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
//                    else if (downTree.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
//                        return true;
//                    } else if (downTree2.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
//                        return true;
//                    }
                    else if (truck.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
// else if (crowd.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly)) {
//                        return true;
//                    } else if (car.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly)) {
//                        return true;
//                    }
                    else if (homingOb.wasObstacleTouched(footprintsL.coordinatesArray[i][0], footprintsL.coordinatesArray[i][1], footprintsWidth, footprintsHeight, true, true, false, checkOnly) != -1) {
                        return true;
                    }
                }
            }
//        }
        return false;
    }

    public void checkCollisions(){
        //Check if trucks have touched other obstacles.
        for (int i = 0; i < maxNumTrucks; i++) {
            if (truck.spawnTracker[i] == 1 || truck.spawnTracker[i] == 2) {
                if (truck.speedArray[i][1] != 0) { //Truck should only hit obstacles if it's moving.
                    if (cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1) {
                        if (!mA.musicMuted) {
                            soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                        }
                        gS.collisionsWitnessed++;
                    }
//                    if (downTree.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false)) {
//                        if (!mA.musicMuted) {
//                            soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
//                        }
//                        gS.collisionsWitnessed++;
//                    }
//                    if (downTree2.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false)) {
//                        if (!mA.musicMuted) {
//                            soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
//                        }
//                        gS.collisionsWitnessed++;
//                    }
                }
                //Destroy cone if not moving so it doesn't look weird.
                cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, true, false);
//                    if (crowd.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false)) {
//                        if (!mA.musicMuted) {
//                            soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
//                        }
//                        gS.collisionsWitnessed++;
//                    }
                    if (homingOb.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false) != -1) {
                        if (!mA.musicMuted) {
                            soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                        }
                    }
//                    if(car.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false)){
//                        if (!mA.musicMuted) {
//                            soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
//                        }
//                        truck.hitObstacle(i, false, false, false);
//                        gS.collisionsWitnessed++;
//                    }
//                    if(truck.checkOverlap(i)) {
//                        if (!mA.musicMuted) {
//                            soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
//                        }
//                        gS.collisionsWitnessed++;
//                    }
//                    coins.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false);
//                }
            }
        }
////        //Check if cars have touched other obstacles.
////        for (int i = 0; i < maxNumCars; i++){
////            if (car.spawnTracker[i] == 1){
////                if(car.speedArray[i][1] != 0){
////                    if(cone.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    if (crowd.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false)) {
////                        if (!mA.musicMuted) {
////                            soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    if (homingOb.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false)) {
////                        if (!mA.musicMuted) {
////                            soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    if(truck.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    if(downTree.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    if(car.checkOverlap(i)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        gS.collisionsWitnessed++;
////                    }
////                    coins.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false);
////                }
////            }
////        }
////        for (int i = 0; i < maxNumCrowds; i++){
////            if (crowd.spawnTracker[i] == 1){
////                if(crowd.speedArray[i][0] != 0 || crowd.speedArray[i][1] != 0){
////                    if(truck.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, true)){
////                        crowd.speedArray[i][0] *= -1;
////                        crowd.speedArray[i][1] *= -1;
////                        //collisionsWitnessed++; //Crowd can get stuck if a vehicle crashes and gets thrown on top of them.
////                    }
////                    if(car.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, true)){
////                        crowd.speedArray[i][0] *= -1;
////                        crowd.speedArray[i][1] *= -1;
////                        gS.collisionsWitnessed++;
////                    }
////                    if(cone.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, false)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        crowd.speedArray[i][0] *= -1;
////                        crowd.speedArray[i][1] *= -1;
////                        gS.collisionsWitnessed++;
////                    }
////                    if(downTree.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, false)){
////                        if (!mA.musicMuted) {
////                            soundEffects.play(crashSound3Id, 0.5f, 0.5f, 0, 0, 1);
////                        }
////                        crowd.speedArray[i][0] *= -1;
////                        crowd.speedArray[i][1] *= -1;
////                        gS.collisionsWitnessed++;
////                    }
////                }
////            }
////        }
        for (int i = 0; i < homingObMaxNum; i++){
            if (homingOb.spawnTracker[i] == 1){
                if(cone.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1){
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                    }
//                    homingOb.speedArray[i][2] *= 0.99f;
//                    homingOb.speedArray[i][3] *= 0.99f;
                    gS.collisionsWitnessed++;
                }
//                if(downTree.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1) {
//                    if (!mA.musicMuted) {
//                        soundEffects.play(crashSound3Id, 0.5f, 0.5f, 0, 0, 1);
//                    }
////                    homingOb.speedArray[i][2] *= 0.99f;
////                    homingOb.speedArray[i][3] *= 0.99f;
//                    gS.collisionsWitnessed++;
//                }
//                if(downTree2.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1) {
//                    if (!mA.musicMuted) {
//                        soundEffects.play(crashSound3Id, 0.5f, 0.5f, 0, 0, 1);
//                    }
////                    homingOb.speedArray[i][2] *= 0.99f;
////                    homingOb.speedArray[i][3] *= 0.99f;
//                    gS.collisionsWitnessed++;
//                }
                if(truck.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, true) != -1) {
                    if (!mA.musicMuted) {
                        soundEffects.play(crashSoundId, 0.5f, 0.5f, 0, 0, 1);
                    }
                    homingOb.speedArray[i][2] *= 0;
                    homingOb.speedArray[i][3] *= 0;
                    gS.collisionsWitnessed++;
                }
            }
        }
    }

    //--------------------------Reset obstacles------------------------------------------
    public void resetObstacles() {
        cone.resetObstacles(distBetweenCones, backgroundWidth, backgroundHeight);
//        downTree.resetObstacles(distBetweenDownTrees, backgroundWidth, backgroundHeight);
//        downTree2.resetObstacles(distBetweenDownTrees, backgroundWidth, backgroundHeight);
        truck.resetObstacles(distBetweenTrucks, backgroundWidth, backgroundHeight);
        racerTrigger = false;
//        crowd.resetObstacles(distBetweenCrowds, backgroundWidth, backgroundHeight);
//        car.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        homingOb.resetObstacles(homingObDistBetween, backgroundWidth, backgroundHeight);
        coins.resetObstacles(distBetweenCoins, backgroundWidth, backgroundHeight);
        footprintsR.resetObstacles(0f, backgroundWidth, backgroundHeight);
        footprintsL.resetObstacles(0f, backgroundWidth, backgroundHeight);
        toNextDiffIncrease = increaseDifficultyDistance;
        difficultly = 0;
        touchDownX = backgroundWidth / 2;
        touchDownY = backgroundHeight + footprintsHeight / 2;
        lastY = touchDownY;
        hopping = false;

        //Show fpMode at bottom of screen at start of game.
        if(fpMode) {
            footprintsL.spawnObstacle(0f, touchDownX - footprintsWidth * 2, backgroundHeight - footprintsHeight, true);
            footprintsR.spawnObstacle(0f, touchDownX + footprintsWidth, backgroundHeight - footprintsHeight, true);
//            fLTempX = footprintsL.coordinatesArray[0][0];
//            fLTempY = footprintsL.coordinatesArray[0][1];
//            fRTempX = footprintsR.coordinatesArray[0][0];
//            fRTempY = footprintsR.coordinatesArray[0][1];
        }
//        else {
//            //Reset touch follower to bottom middle of screen.
//            if (tFRotated) {
//                touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateCounterClockwise, true);
//                tFRotated = false;
//            }
//            resetTF();
//        }
    }

    public void releaseAudio(){
        if(soundEffects != null) {
            for(int i = 0; i < 10; i++){
                soundEffects.stop(i);
            }
            soundEffects.release();
            soundEffects = null;
        }
    }
//
    public void setAudio(){
        if(soundEffects == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            badSoundId = soundEffects.load(context, R.raw.finger_runner_bad_sound, 1);
            goodSoundId = soundEffects.load(context, R.raw.coin_sound_2, 1);
            crashSound2Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_2, 1);
            crashSound3Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_3, 1);
            crashSound4Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_4, 1);
            crashSound5Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_5, 1);
            truckHornId = soundEffects.load(context, R.raw.finger_runner_bad_truck_horn, 1);
            carHornId = soundEffects.load(context, R.raw.finger_runner_bad_car_horn, 1);
            wilhelmScreamId = soundEffects.load(context, R.raw.wilhelm_scream, 1);
            driveSoundId = soundEffects.load(context, R.raw.car_driving, 1);

//            soundEffects = mA.sfx;
//            badSoundId = mA.badSoundId;
//            goodSoundId = mA.goodSoundId;
//            crashSoundId = mA.crashSoundId;
//            crashSound2Id = mA.crashSound2Id;
//            crashSound3Id = mA.crashSound3Id;
//            crashSound4Id = mA.crashSound4Id;
//            crashSound5Id = mA.crashSound5Id;
//            truckHornId = mA.truckHornId;
//            carHornId = mA.carHornId;
//            wilhelmScreamId = mA.wilhelmScreamId;
        }
    }

    public void setFpMode(boolean m){
        this.fpMode = m;
    }

//    public void setTimeTrial2Flag(boolean f){
//        this.timeTrial2Flag = f;
//    }
}
