package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by Mark on 3/9/2017.
 */

public class LocationOne {

    //boolean distanceMode;
    private float courseDistance = 10000f;  //Currently an arbitrary distance to the finish line.

    private Obstacles cone;
    private int coneResId = R.drawable.cone;
    private int coneXScale = 60;
    private int coneYScale = 75;
    private int maxNumCones = 8;
    private float distBetweenCones;   //Initialized in resetVariables()
    private float coneXSpeed = 0f;
    private float coneYSpeed = 0f;

    private Obstacles downTree;
    private int downTreeResId = R.drawable.down_tree;
    private int downTreeXScale = 189;
    private int downTreeYScale = 60;
    private int maxNumDownTrees = 3;
    private float distBetweenDownTrees;
    private float downTreeXSpeed = 0f;
    private float downTreeYSpeed = 0f;

    private Obstacles truck;
    private int truckResId = R.drawable.shitty_truck;
    private int truckXScale = 160;
    private int truckYScale = 452;
    private int maxNumTrucks = 1;
    private float distBetweenTrucks;
    private float truckXSpeed = 0f;
    private float truckYSpeed = 2f;

    private Obstacles crowd;
    private int crowdResId = R.drawable.dumbppl;
    private int crowdXScale = 122;
    private int crowdYScale = 154;
    private int maxNumCrowds = 1;
    private float distBetweenCrowds;
    private float crowdXSpeed = 3f;
    private float crowdYSpeed = 10f;

    private Obstacles car;
    private int carResId = R.drawable.pan_2;
    private int carXScale = 150;
    private int carYScale = 250;
    private int maxNumCars = 2;
    private float distBetweenCars;
    private float carXSpeed = 0f;
    private float carYSpeed = 10f;

    private Obstacles homingOb;
    private int homingObResID = R.drawable.dude;
    private int homingObXScale = 114;
    private int homingObYScale = 136;
    private int homingObMaxNum = 2;
    private float homingObDistBetween;
    private float homingObXSpeed = 0f;
    private float homingObYSpeed = 0f;

    private float increaseDifficultyDistance = 5000f;
    private float toNextDiffIncrease;
    private int difficultly;
    //float homingSpeed;

    private Obstacles extraLives;
    private int extraLivesResId = R.drawable.lvlup;
    private int extraLivesMaxNum = 1;
    private float extraLivesDistBetween = 15000f;
    private float extraLivesHorizontalSpeed = 3f;
    private float extraLivesVerticalSpeed = 0f;

    private Bitmap touchFollower;
    private int touchFollowerHeight;
    private int touchFollowerWidth;
    private int tFXScale = 56;
    private int tFYScale = 200;
    private float tFX;          //current x coordinate of touchFollower
    private float tFY;          //current y coordinate of touchFollower
    private float tFXOffset;
    private float tFYOffset;
    private float touchDownX;   //desired x coordinate of touchFollower.
    private float touchDownY;   //desired y coordinate of touchFollower.
    private float tFSpeed = 0.1f;

    private Matrix matrixRotateClockwise = new Matrix();
    private Matrix matrixRotateCounterClockwise = new Matrix();
    private Boolean tFRotated = false;

    private float velocityFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.
    private float distanceFactor = 0.75f; //Must be less than 1 or else road will advance exponentially.
    private float inertiaFactor = 1f; //Must be less than 1 or else road will advance exponentially.

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

    private MainActivity mA;
    private GameView gS;
    private float sX;
    private float sY;

    private int backgroundResId = R.drawable.road3;
    private int backgroundWidth;
    private int backgroundHeight;

    Context context;

    public LocationOne(MainActivity mA, GameView gS, float x, float y, int bW, int bH) {
        this.mA = mA;
        this.gS =  gS;
        this.sX = x;
        this.sY = y;
        this.backgroundWidth = bW;
        this.backgroundHeight = bH;
        this.context = gS.getContext();

        //-----------------Initialize obstacles----------------------------------------------------
        cone = new Obstacles(context, (int) (sX * coneXScale), (int) (sY * coneYScale), coneResId, maxNumCones, false, distBetweenCones, coneXSpeed, coneYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        downTree = new Obstacles(context, (int) (sX * downTreeXScale), (int) (sY * downTreeYScale), downTreeResId, maxNumDownTrees, false, distBetweenDownTrees, downTreeXSpeed, downTreeYSpeed, 0, backgroundWidth, backgroundHeight, false, false, false);
        truck = new Obstacles(context, (int) (sX * truckXScale), (int) (sY * truckYScale), truckResId, maxNumTrucks, false, distBetweenTrucks, truckXSpeed, truckYSpeed, 0, backgroundWidth, backgroundHeight, true, true, false);
        crowd = new Obstacles(context, (int) (sX * crowdXScale), (int) (sY * crowdYScale), crowdResId, maxNumCrowds, false, distBetweenCrowds, crowdXSpeed, crowdYSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);
        car = new Obstacles(context, (int) (sX * carXScale), (int) (sY * carYScale), carResId, maxNumCars, false, distBetweenCars, carXSpeed, carYSpeed, 0, backgroundWidth, backgroundHeight, true, true, false);
        homingOb = new Obstacles(context, (int) (sX * homingObXScale), (int) (sY * homingObYScale), homingObResID, homingObMaxNum, false, homingObDistBetween, homingObXSpeed, homingObYSpeed, 0.005f, backgroundWidth, backgroundHeight, true, false, false);
        extraLives = new Obstacles(context, (int) (sX * 62), (int) (sY * 110), extraLivesResId, extraLivesMaxNum, false, extraLivesDistBetween, extraLivesHorizontalSpeed, extraLivesVerticalSpeed, 0, backgroundWidth, backgroundHeight, true, false, false);

        //Initialize fpMode
//        fpMode = new Obstacles(this.getContext(), (int)(sX * 50), (int)(sY * 50), footprintsImageResId, footprintsDMaxNumObs, true, footprintsDDistBetweenObs, footprintsDHorizontalSpeed, footprintsDVerticalSpeed, backgroundWidth, backgroundHeight, false, false);

        //Initialize touch follower.
        touchFollowerHeight = (int) (sY * tFYScale);
        touchFollowerWidth = (int) (sX * tFXScale);
        touchFollower = BitmapFactory.decodeResource(context.getResources(), R.drawable.practice3_small, null);
        touchFollower = Bitmap.createScaledBitmap(touchFollower, touchFollowerWidth, touchFollowerHeight, true);

        matrixRotateClockwise.postRotate(90);
        matrixRotateCounterClockwise.postRotate(270);

        tFXOffset = -touchFollower.getWidth() / 2;
        tFYOffset = -touchFollower.getHeight();

        //Sound effects.
        setAudio();
    }

    public float getCourseDistance(){
        return this.courseDistance;
    }

    public float getTFY(){
        return this.tFY;
    }

    public int getTouchFollowerHeight(){
        return this.touchFollowerHeight;
    }

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

    public void updateTFY(float d){
        this.tFY += d;
    }

    public void updateHomingSpeed(float factor){
        homingOb.homingSpeed *= factor;
    }

    public void updateHomingObstacle(){
        for (int i = 0; i < homingObMaxNum; i++) {
            if (homingOb.spawnTracker[i] == 1) {
                if (homingOb.coordinatesArray[i][0] != tFX) {
                    homingOb.coordinatesArray[i][0] += homingOb.speedArray[i][2] * (tFX - homingOb.coordinatesArray[i][0]);
                }
                if (homingOb.coordinatesArray[i][1] != tFY) {
                    homingOb.coordinatesArray[i][1] += homingOb.speedArray[i][3] * (tFY - homingOb.coordinatesArray[i][1]);
                }
            }
        }
    }

    public void updateTouchFollower(){
        if(tFX != touchDownX + tFXOffset) {
            tFX += tFSpeed * (touchDownX + tFXOffset - tFX);
        }
        if(tFY != touchDownY + tFYOffset){
            tFY += tFSpeed * (touchDownY + tFYOffset - tFY);
        }
        //Prevent touchFollower from disappearing off bottom of screen.
        if(tFY > backgroundHeight + tFYOffset){
            tFY = backgroundHeight + tFYOffset;
        }
    }

    public void updateObstacleSeparation() {
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

        updateHomingSpeed((difficultly + 5f) / (difficultly + 4f));
    }

    public void resetTF(){
        tFX = tFXOffset + backgroundWidth/2;
        tFY = tFYOffset + backgroundHeight;
    }

    public void setTouchDownX(float x){
        this.touchDownX = x;
    }

    public void setTouchDownY(float y){
        this.touchDownY = y;
    }

    //---------------------Draw obstacles---------------------------------------------
    public void draw(Canvas canvas, Paint paint, float interpolation, float velocity) {
//            fpMode.drawObstacles(canvas, paint);

        cone.drawObstacles(canvas, paint, interpolation, velocity);
        downTree.drawObstacles(canvas, paint, interpolation, velocity);
        crowd.drawObstacles(canvas, paint, interpolation, velocity);
        car.drawObstacles(canvas, paint, interpolation, velocity);
        homingOb.drawObstacles(canvas, paint, interpolation, velocity);
        extraLives.drawObstacles(canvas, paint, interpolation, velocity);
        truck.drawObstacles(canvas, paint, interpolation, velocity);

        //Draw touch follower.
        canvas.drawBitmap(touchFollower, tFX, tFY, paint);  //TODO: implement interpolation
    }

    //--------------Check if an obstacle has run into touchFollower when no fingers are down (only necessary if friendly sprite triggers obstacles)-----------------------------
    public void checkIfObstacleRanIntoTouchFollower(int livesLeft) {
        if (checkObstaclesTouched(true)) {
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
                touchDownX = tFX - tFXOffset;
                touchDownY = tFY - tFYOffset;
                gS.livesLeft--;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
            }
        } else if (extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, true, false) != -1) {
            gS.livesLeft++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
            }
        }
    }

    //--------------Check if an obstacle has been touched-----------------------------
    public void checkIfObstacleWasTouched(int livesLeft) {
        if (checkObstaclesTouched(false)) {
            if (livesLeft == 0) {
                touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateClockwise, true);
                tFRotated = true;
                if (!mA.musicMuted) {
                    soundEffects.play(wilhelmScreamId, 1, 1, 0, 0, 1);
                }
                gS.collisionsWitnessed++;
                mA.setGameState(MainActivity.GAME_LOST);
            } else {
                gS.velocity = 0;
                touchDownX = tFX - tFXOffset;
                touchDownY = tFY - tFYOffset;
                --gS.livesLeft;
                if (!mA.musicMuted) {
                    soundEffects.play(badSoundId, 1, 1, 0, 0, 1);
                    gS.collisionsWitnessed++;
                }
            }
        } else if (extraLives.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, true, false) != -1) {
            gS.livesLeft++;
            if (!mA.musicMuted) {
                soundEffects.play(goodSoundId, 1, 1, 0, 0, 1);
            }
        }
    }

    //---------------Update Obstacles---------------------------------------------------
    public void updateObs(float distance) {
        cone.updateObstacles(distance, true);
        downTree.updateObstacles(distance, true);
        if (truck.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(truckHornId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
        crowd.updateObstacles(distance, true);
        if (car.updateObstacles(distance, true)) {
            if (!mA.musicMuted) {
                soundEffects.play(carHornId, 0.5f, 0.5f, 0, 0, 1);
            }
        }
        homingOb.updateObstacles(distance, true);

        if(gS.distanceMode) {
            extraLives.updateObstacles(distance, true);
        }

//        fpMode.updateObstacles(distance, false);
    }

    //Move obstacles(any movement independent from the road).
    public void move(){
        cone.moveObstacles();
        downTree.moveObstacles();
        truck.moveObstacles();
        crowd.moveObstacles();
        car.moveObstacles();
        homingOb.moveObstacles();
        extraLives.moveObstacles();
    }

    //--------------Check if an obstacle was touched-----------------------------
    public Boolean checkObstaclesTouched(boolean checkOnly) {
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
        if (cone.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (downTree.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (truck.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){//(activeFinger.x, activeFinger.y)) {
            return true;
        } else if (crowd.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1) {//(activeFinger.x, activeFinger.y)) {
            return true;
        }else if (car.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){
            return true;
        } else if (homingOb.wasObstacleTouched(tFX, tFY, touchFollower.getWidth(), touchFollower.getHeight(), true, true, false, checkOnly) != -1){
            return true;
        } else {
//            return false;
//        }

            //Check if trucks have touched other obstacles.
            for (int i = 0; i < maxNumTrucks; i++) {
                if (truck.spawnTracker[i] == 1 || truck.spawnTracker[i] == 2) { //Truck should only destroy obstacles if it's moving.
                    if (truck.speedArray[i][1] != 0) {
                        if(cone.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if (crowd.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false) != -1) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if (homingOb.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, true, false, false) != -1) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if(car.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            truck.hitObstacle(i, false, false, false, true);
                            gS.collisionsWitnessed++;
                        }
                        if(downTree.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        extraLives.wasObstacleTouched(truck.coordinatesArray[i][0], truck.coordinatesArray[i][1], truck.obstacleWidth, truck.obstacleHeight, false, false, false, false);
                    }
                }
            }
            //Check if cars have touched other obstacles.
            for (int i = 0; i < maxNumCars; i++){
                if (car.spawnTracker[i] == 1){
                    if(car.speedArray[i][1] != 0){
                        if(cone.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if (crowd.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false) != -1) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if (homingOb.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, true, false, false) != -1) {
                            if (!mA.musicMuted) {
                                soundEffects.play(wilhelmScreamId, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if(truck.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if(downTree.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound4Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        if(car.checkOverlap(i, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound2Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            gS.collisionsWitnessed++;
                        }
                        extraLives.wasObstacleTouched(car.coordinatesArray[i][0], car.coordinatesArray[i][1], car.obstacleWidth, car.obstacleHeight, false, false, false, false);
                    }
                }
            }
            for (int i = 0; i < maxNumCrowds; i++){
                if (crowd.spawnTracker[i] == 1){
                    if(crowd.speedArray[i][0] != 0 || crowd.speedArray[i][1] != 0){
                        if(truck.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, true) != -1){
                            crowd.speedArray[i][0] *= -1;
                            crowd.speedArray[i][1] *= -1;
                            //collisionsWitnessed++; //Crowd can get stuck if a vehicle crashes and gets thrown on top of them.
                        }
                        if(car.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, true) != -1){
                            crowd.speedArray[i][0] *= -1;
                            crowd.speedArray[i][1] *= -1;
                            gS.collisionsWitnessed++;
                        }
                        if(cone.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            crowd.speedArray[i][0] *= -1;
                            crowd.speedArray[i][1] *= -1;
                            gS.collisionsWitnessed++;
                        }
                        if(downTree.wasObstacleTouched(crowd.coordinatesArray[i][0], crowd.coordinatesArray[i][1], crowd.obstacleWidth, crowd.obstacleHeight, false, false, false, false) != -1){
                            if (!mA.musicMuted) {
                                soundEffects.play(crashSound3Id, 0.5f, 0.5f, 0, 0, 1);
                            }
                            crowd.speedArray[i][0] *= -1;
                            crowd.speedArray[i][1] *= -1;
                            gS.collisionsWitnessed++;
                        }
                    }
                }
            }for (int i = 0; i < homingObMaxNum; i++){
                if (homingOb.spawnTracker[i] == 1){
                    if(cone.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1){
                        if (!mA.musicMuted) {
                            soundEffects.play(crashSound5Id, 0.5f, 0.5f, 0, 0, 1);
                        }
                        homingOb.speedArray[i][2] *= 0.95f;
                        homingOb.speedArray[i][3] *= 0.95f;
                        gS.collisionsWitnessed++;
                    }
                    if(downTree.wasObstacleTouched(homingOb.coordinatesArray[i][0], homingOb.coordinatesArray[i][1], homingOb.obstacleWidth, homingOb.obstacleHeight, false, false, false, false) != -1) {
                        if (!mA.musicMuted) {
                            soundEffects.play(crashSound3Id, 0.5f, 0.5f, 0, 0, 1);
                        }
                        homingOb.speedArray[i][2] *= 0.95f;
                        homingOb.speedArray[i][3] *= 0.95f;
                        gS.collisionsWitnessed++;
                    }
                }
            }
            return false;
        }
    }

    //--------------------------Reset obstacles------------------------------------------
    public void resetObstacles() {
        distBetweenCones = 800f;
        distBetweenDownTrees = 4000f;
        distBetweenTrucks = 3000f;
        distBetweenCrowds = 2500f;
        distBetweenCars = 2000f;
        homingObDistBetween = 7500f;
        homingOb.setHomingSpeed(0.005f);
        //homingSpeed = 0.005f; //TODO: experiment with value
        cone.resetObstacles(distBetweenCones, backgroundWidth, backgroundHeight);
        downTree.resetObstacles(distBetweenDownTrees, backgroundWidth, backgroundHeight);
        truck.resetObstacles(distBetweenTrucks, backgroundWidth, backgroundHeight);
        crowd.resetObstacles(distBetweenCrowds, backgroundWidth, backgroundHeight);
        car.resetObstacles(distBetweenCars, backgroundWidth, backgroundHeight);
        homingOb.resetObstacles(homingObDistBetween, backgroundWidth, backgroundHeight);
        toNextDiffIncrease = increaseDifficultyDistance;
        difficultly = 0;
        extraLives.resetObstacles(extraLivesDistBetween, backgroundWidth, backgroundHeight);

        //Reset touch follower to bottom middle of screen.
        if(tFRotated) {
            touchFollower = Bitmap.createBitmap(touchFollower, 0, 0, touchFollower.getWidth(), touchFollower.getHeight(), matrixRotateCounterClockwise, true);
            tFRotated = false;
        }
        resetTF();
    }

    public void releaseAudio(){
        if(soundEffects != null) {
            soundEffects.release();
            soundEffects = null;
        }
    }

    public void setAudio(){
        if(soundEffects == null) {
//        soundEffects = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            soundEffects = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            badSoundId = soundEffects.load(context, R.raw.finger_runner_bad_sound, 1);
            goodSoundId = soundEffects.load(context, R.raw.finger_runner_good_sound, 1);
            crashSound2Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_2, 1);
            crashSound3Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_3, 1);
            crashSound4Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_4, 1);
            crashSound5Id = soundEffects.load(context, R.raw.finger_runner_crash_sound_5, 1);
            truckHornId = soundEffects.load(context, R.raw.finger_runner_bad_truck_horn, 1);
            carHornId = soundEffects.load(context, R.raw.finger_runner_bad_car_horn, 1);
            wilhelmScreamId = soundEffects.load(context, R.raw.wilhelm_scream, 1);
        }
    }
}
