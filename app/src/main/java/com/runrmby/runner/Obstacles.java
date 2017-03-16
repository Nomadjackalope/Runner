package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.Random;

/**
 * Created by Mark on 2/10/2017.
 * Creates a set of similar obstacles.
 */

public class Obstacles {

    //int obstacleImageResID;
    Bitmap obstacleImage;
    Bitmap rotatedObsImage;
    Bitmap rotatedObsImage2;
    Bitmap rotatedObsImage3;
    int maxNumberOfObstacles;
    int scaleX;
    int scaleY;
    float originalDistanceBetweenObstacles;
    float distanceBetweenObstacles;
    float originalHSpeed;
    float originalVSpeed;
    int windowWidth;
    int windowHeight;
    //float nextObstacleAt;
    float horizontalSpeed;
    float verticalSpeed;
    float originalHomingSpeed;
    float homingSpeed;
    float distanceToNextObstacle;
    int obstacleWidth;
    int obstacleHeight;
    int[] spawnTracker; //0 = not spawned, 1 = spawned, 2 = hit
    float[][] coordinatesArray;
    float[][] speedArray;
    static final int DESTROYED = 0;
    static final int SPAWNED = 1;
    static final int HIT_BY_OBSTACLE = 2;
    static final int TRIGGERED = 3;
    static final int HIT_AND_TRIGGERED = 4;
    Random random = new Random();
    Boolean respawnWithMax;
    int lastSpawnIndex;
    Boolean randomizeParameters;
    Boolean directional;
    Boolean followRoad;

    int[] orientationArray;

    int blinkTime = 10;
    int blinkOnCountDown = blinkTime;
    int blinkOffCountDown = blinkTime;
    static int blinkCyclesToDisappear = 6;
    int[] blinkCyclesToDisappearArray;

    Matrix matrix;

    Context context;

    /**
     * @param randomize allows certain parameters to have variation.
     * //TODO: add two more columns to coordinates array to keep track of horizontal and vertical speed for individual obstacles so that randomizing doesn't affect spawned obstacles, even though that's kind of fun.
     */
    public Obstacles(Context context, int scaleX, int scaleY, int obstacleImageResID, int maxNumberOfObstacles, Boolean respawnWithMax, float distanceBetweenObstacles,
                     float horizontalSpeed, float verticalSpeed, float homingSpeed, int windowWidth, int windowHeight, Boolean randomize, boolean directional, boolean followRoad){
        this.context = context;
        this.obstacleImage = BitmapFactory.decodeResource(context.getResources(), obstacleImageResID, null);
        this.obstacleImage = Bitmap.createScaledBitmap(this.obstacleImage, scaleX, scaleY, true);
        this.maxNumberOfObstacles = maxNumberOfObstacles;
        this.respawnWithMax = respawnWithMax;
        this.originalDistanceBetweenObstacles = distanceBetweenObstacles;
        this.distanceBetweenObstacles = distanceBetweenObstacles;
        this.originalHSpeed = horizontalSpeed;
        this.horizontalSpeed = originalHSpeed;
        this.originalVSpeed = verticalSpeed;
        this.verticalSpeed = originalVSpeed;
        this.originalHomingSpeed = homingSpeed;
        this.homingSpeed = homingSpeed;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        //this.nextObstacleAt = distanceBetweenObstacles;
        this.distanceToNextObstacle = distanceBetweenObstacles;
        this.obstacleWidth = obstacleImage.getWidth();
        this.obstacleHeight = obstacleImage.getHeight();
        this.spawnTracker = new int[maxNumberOfObstacles];
        this.coordinatesArray = new float[maxNumberOfObstacles][2];
        this.speedArray = new float[maxNumberOfObstacles][4];   //[i][0] = x speed, [i][1] = y speed, [i][2] = homing x speed, [i][3] = homing y speed
        this.randomizeParameters = randomize;
        this.lastSpawnIndex = maxNumberOfObstacles - 1;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.directional = directional;
        this.followRoad = followRoad;

        this.orientationArray = new int[maxNumberOfObstacles];

        matrix = new Matrix();
        matrix.postRotate(90);

        rotatedObsImage = Bitmap.createBitmap(obstacleImage, 0, 0, obstacleImage.getWidth(), obstacleImage.getHeight(), matrix, true);
        rotatedObsImage2 = Bitmap.createBitmap(rotatedObsImage, 0, 0, rotatedObsImage.getWidth(), rotatedObsImage.getHeight(), matrix, true);
        rotatedObsImage3 = Bitmap.createBitmap(rotatedObsImage2, 0, 0, rotatedObsImage2.getWidth(), rotatedObsImage2.getHeight(), matrix, true);

        this.blinkCyclesToDisappearArray = new int[maxNumberOfObstacles];
    }

    public boolean updateObstacles(float distance, boolean autoSpawn) {
        //Update distance moved.
        if(maxNumberOfObstacles > 0) {
            for (int i = 0; i < maxNumberOfObstacles; i++) {
                if (spawnTracker[i] != DESTROYED) {
                    coordinatesArray[i][1] += distance;
                }
                //If now off screen, set as destroyed.
                if (coordinatesArray[i][1] > windowHeight + obstacleHeight || coordinatesArray[i][1] < -2*obstacleHeight || coordinatesArray[i][0] > windowWidth || coordinatesArray[i][0] < -obstacleWidth) {
                    spawnTracker[i] = DESTROYED;
                    //Could reset coordinates here, but doesn't seem necessary as they are always set when a spawn occurs.
                }
            }

            distanceToNextObstacle -= distance;

            if (autoSpawn) {
                if(spawnObstacle(distanceToNextObstacle, random.nextInt(windowWidth) - obstacleWidth / 2, -obstacleHeight, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean spawnObstacle(float distance, float x, float y, boolean allowOverlap){
        //If enough distance has been covered and not at max spawns, spawn an obstacle.
        if(maxNumberOfObstacles > 0) {
            if (distance <= 0) {
                if (lastSpawnIndex < maxNumberOfObstacles - 1) {
                    lastSpawnIndex++;
                } else {
                    lastSpawnIndex = 0;
                }
                //for(int i = 0; i < maxNumberOfObstacles; i++){
                if (spawnTracker[lastSpawnIndex] == DESTROYED || respawnWithMax) {
                    if(!allowOverlap) {
                        if (wasObstacleTouched(x, y, obstacleWidth, obstacleHeight, false, false, false, true)) {
                            if (lastSpawnIndex > 0) {
                                lastSpawnIndex--;
                            } else {
                                lastSpawnIndex = maxNumberOfObstacles - 1;
                            }
                            return false;
//                            if (random.nextBoolean()) {
//                                x += obstacleWidth;
//                            } else {
//                                x -= obstacleWidth;
//                            }
                        }
                    }
                    spawnTracker[lastSpawnIndex] = SPAWNED;
                    float hS = horizontalSpeed;
                    float vS = verticalSpeed;
//                    coordinatesArray[lastSpawnIndex][0] = x;
//                    coordinatesArray[lastSpawnIndex][1] = y;
//                    speedArray[lastSpawnIndex][0] = horizontalSpeed;
//                    speedArray[lastSpawnIndex][1] = verticalSpeed;
//                    speedArray[lastSpawnIndex][2] = homingSpeed * 0.75f;
//                    speedArray[lastSpawnIndex][3] = homingSpeed;
                    if (!randomizeParameters) {
                        distanceToNextObstacle = distanceBetweenObstacles;
                    } else {
                        //If randomizeParameters selected, vary several parameters for each spawn. !!!THIS AFFECTS MOTION OF ALL SPAWNED OBSTACLES!!!
                        distanceToNextObstacle = distanceBetweenObstacles * (random.nextInt(7) + 1) / 4;
                        hS *= (((random.nextInt(7) + 1 + random.nextFloat())) / 4);
                        vS *= (((random.nextInt(7) + 1 + random.nextFloat())) / 4);
                        if(random.nextBoolean()){
                            hS *= -1;
                        }
                        if(random.nextBoolean()){
                            vS *= -1;
                        }
                    }

                    if(followRoad){
                        //Place approximately within lane.
                        if(x < windowWidth / 2 && x > windowWidth / 2 - obstacleWidth || x > windowWidth - obstacleWidth){
                            x -= obstacleWidth;
                        }

                        //Change speed direction to match correct side of road.
                        if(x < windowWidth / 2){//On left half of screen.
                            if(vS < 0){
                                vS *= -1;
                            }
                        } else {
                            if(vS > 0){
                                vS *= -1;
                            }
                        }
                    }

                    if(directional && (vS < 0 || followRoad && x > windowWidth / 2)){
                        orientationArray[lastSpawnIndex] = 2;
                    }else{
                        orientationArray[lastSpawnIndex] = 0;
                    }

                    if(vS < 0 && random.nextBoolean()){//if obstacle has negative y speed, randomly spawn at bottom of screen
                        y = windowHeight;
                    }


                    if(horizontalSpeed > 0){
                        x = -obstacleWidth;
                        if(vS >= 0) {
                            y = random.nextInt(windowHeight / 2) - obstacleHeight;
                        } else {
                            y = random.nextInt(windowHeight / 2) - obstacleHeight + windowHeight / 2;
                        }
                    } else if (hS < 0){
                        x = windowWidth;
                        if(verticalSpeed >= 0) {
                            y = random.nextInt(windowHeight / 2) - obstacleHeight;
                        } else {
                            y = random.nextInt(windowHeight / 2) - obstacleHeight + windowHeight / 2;
                        }
                    }

                    coordinatesArray[lastSpawnIndex][0] = x;
                    coordinatesArray[lastSpawnIndex][1] = y;
                    speedArray[lastSpawnIndex][0] = hS;
                    speedArray[lastSpawnIndex][1] = vS;
                    speedArray[lastSpawnIndex][2] = homingSpeed * 0.75f;
                    speedArray[lastSpawnIndex][3] = homingSpeed;

                    return true;
                    //break;
                }
                //}
            }
        }
        return false;
    }

    public void destroyObstacle(int obsIndex){
        spawnTracker[obsIndex] = DESTROYED;
        orientationArray[obsIndex] = 0;
        coordinatesArray[obsIndex][0] = -windowWidth;
        coordinatesArray[obsIndex][1] = -windowHeight;
        blinkCyclesToDisappearArray[obsIndex] = blinkCyclesToDisappear;
    }

    public boolean hitObstacle(int obsIndex, boolean isTF, boolean trigger, boolean move, boolean rotate){
        switch (spawnTracker[obsIndex]){
            case DESTROYED:
                return false;
            case SPAWNED:
                if(isTF){
                    spawnTracker[obsIndex] = TRIGGERED;
                } else if(trigger) {
                    spawnTracker[obsIndex] = HIT_AND_TRIGGERED;
                } else{
                    spawnTracker[obsIndex] = HIT_BY_OBSTACLE;
                }
                break;
            case HIT_BY_OBSTACLE:
                if(isTF || trigger){
                    spawnTracker[obsIndex] = HIT_AND_TRIGGERED;
                }
                break;
            case TRIGGERED:
                if(!isTF){
                    spawnTracker[obsIndex] = HIT_AND_TRIGGERED;
                }
                return false;
            case HIT_AND_TRIGGERED:
                return false;
        }
        speedArray[obsIndex][0] = 0;
        speedArray[obsIndex][1] = 0;
        if(!isTF) {
            if(rotate) {
                if (orientationArray[obsIndex] == 3) {
                    orientationArray[obsIndex] = 0;
                } else {
                    orientationArray[obsIndex]++;
                }
            }
            if(move) {
                if (random.nextBoolean()) {
                    coordinatesArray[obsIndex][0] += (float) obstacleWidth;
                } else {
                    coordinatesArray[obsIndex][0] -= (float) obstacleWidth;
                }
            }
        }
        return true;
    }

    public void moveObstacles() {
        //Obstacle movement independent of road.
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] != DESTROYED) {
//                if(homingSpeed != 0){
//                    //TODO: handle homing here
//                }
//                if (coordinatesArray[i][0] < windowWidth) {
                    coordinatesArray[i][0] += speedArray[i][0];
//                }
//                if (coordinatesArray[i][1] < windowHeight) {
                    if(directional && speedArray[i][1] < 0){
                        orientationArray[i] = 2;
                    } else if(directional && speedArray[i][1] > 0){
                        orientationArray[i] = 0;
                    }
                    coordinatesArray[i][1] += speedArray[i][1];
//                }
            }
        }
    }

    public void drawObstacles(Canvas canvas, Paint paint, float interpolation, float velocity) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            float ix = coordinatesArray[i][0];// + (speedArray[i][0] * interpolation);
            float iy = coordinatesArray[i][1];// + ((speedArray[i][1] + velocity) * interpolation);
            if(spawnTracker[i] == SPAWNED || spawnTracker[i] == HIT_BY_OBSTACLE){
                if (orientationArray[i] == 0) {
                    canvas.drawBitmap(obstacleImage, ix, iy, paint);
                } else if (orientationArray[i] == 1) {
                    canvas.drawBitmap(rotatedObsImage, ix, iy, paint);
                } else if (orientationArray[i] == 2) {
                    canvas.drawBitmap(rotatedObsImage2, ix, iy, paint);
                } else if (orientationArray[i] == 3) {
                    canvas.drawBitmap(rotatedObsImage3, ix, iy, paint);
                }
            } else if(spawnTracker[i] == TRIGGERED || spawnTracker[i] == HIT_AND_TRIGGERED){
                //TODO: Figure out how to make transparent or a different look so user can see it's been triggered
                if(blinkOffCountDown > 0){
                    --blinkOffCountDown;
                } else if(blinkOnCountDown > 0) {
                    --blinkOnCountDown;
                    if (orientationArray[i] == 0) {
                        canvas.drawBitmap(obstacleImage, ix, iy, paint);
                    } else if (orientationArray[i] == 1) {
                        canvas.drawBitmap(rotatedObsImage, ix, iy, paint);
                    } else if (orientationArray[i] == 2) {
                        canvas.drawBitmap(rotatedObsImage2, ix, iy, paint);
                    } else if (orientationArray[i] == 3) {
                        canvas.drawBitmap(rotatedObsImage3, ix, iy, paint);
                    }
                } else {
                    blinkOnCountDown = blinkTime;
                    blinkOffCountDown = blinkTime;
                    if(blinkCyclesToDisappearArray[i] > 0){
                        --blinkCyclesToDisappearArray[i];
                    }else{
                        destroyObstacle(i);
                        blinkCyclesToDisappearArray[i] = blinkCyclesToDisappear;
                    }
                }
            }
        }
    }

    //x and y are view coordinates. Width and height should be 0f if checking a point instead of an area.
    public Boolean wasObstacleTouched(float x, float y, float width, float height, boolean isTF, boolean trigger, boolean destroy, boolean checkOnly) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] != DESTROYED) {
                if((spawnTracker[i] == TRIGGERED && isTF) || (spawnTracker[i] == HIT_AND_TRIGGERED && isTF)) {
                    continue;
                }
                if (orientationArray[i] == 0 || orientationArray[i] == 2) {
                    if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleWidth
                            && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleHeight) {
                        if(checkOnly){
                            return true;
                        }
                        if(destroy){
                            destroyObstacle(i);
                            return true;
                        } else {
                            return hitObstacle(i, isTF, trigger, true, true);
                        }
                    }
                } else {    //object rotated so width & height flipped.
                    if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleHeight
                            && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleWidth) {
                        if(checkOnly){
                            return true;
                        }
                        if(destroy){
                            destroyObstacle(i);
                            return true;
                        } else {
                            return hitObstacle(i, isTF, trigger, true, true);
                        }
                    }
                }
            }
        }
        return false;
    }

    public int checkOverlap(int i, boolean checkOnly){
            if(spawnTracker[i] != DESTROYED){
                for(int j = 0; j < maxNumberOfObstacles; j++){
                    if(i == j){
                        continue;
                    }
                    if(spawnTracker[j] != DESTROYED){
                        if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                if(!checkOnly) {
                                    hitObstacle(i, false, false, false, true);
                                    hitObstacle(j, false, false, true, true);
                                }
                                return j;
                            }
                        } else if (orientationArray[i] % 2 == 1 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleHeight > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleWidth > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                if(!checkOnly) {
                                    hitObstacle(i, false, false, false, true);
                                    hitObstacle(j, false, false, true, true);
                                }
                                return j;
                            }
                        } else if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 1) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleHeight
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleWidth) {
                                if(!checkOnly) {
                                    hitObstacle(i, false, false, false, true);
                                    hitObstacle(j, false, false, true, true);
                                }
                                return j;
                            }
                        } else {
                            if (coordinatesArray[i][0] + obstacleHeight > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleHeight
                                    && coordinatesArray[i][1] + obstacleWidth > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleWidth) {
                                if(!checkOnly) {
                                    hitObstacle(i, false, false, false, true);
                                    hitObstacle(j, false, false, true, true);
                                }
                                return j;
                            }
                        }
                    }
                }
            }
        return -1;
    }

    public void resetObstacles(){
        this.distanceBetweenObstacles = originalDistanceBetweenObstacles;
        this.distanceToNextObstacle = originalDistanceBetweenObstacles;
        this.horizontalSpeed = this.originalHSpeed;
        this.verticalSpeed = this.originalVSpeed;
        homingSpeed = originalHomingSpeed;
        //this.nextObstacleAt = distanceBetweenObstacles;
        for(int i = 0; i < maxNumberOfObstacles; i++){
//            coordinatesArray[i][0] = 0;
//            coordinatesArray[i][1] = 0;
//            orientationArray[i] = 0;
//            speedArray[i][0] = originalHSpeed;
//            speedArray[i][1] = originalVSpeed;
//            speedArray[i][2] = 0.75f * homingSpeed;
//            speedArray[i][3] = homingSpeed;
            spawnTracker[i] = DESTROYED;

            blinkCyclesToDisappearArray[i] = blinkCyclesToDisappear;
        }
        this.lastSpawnIndex = maxNumberOfObstacles - 1;

        blinkOffCountDown = blinkTime;
        blinkOnCountDown = blinkTime;
    }

    public void resetObstacles(float distanceBetweenObstacles, int windowWidth, int windowHeight){
        resetObstacles();
//        this.originalDistanceBetweenObstacles = distanceBetweenObstacles;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public int getObstacleWidth(){
        return this.obstacleWidth;
    }

    public int getObstacleHeight(){
        return this.obstacleHeight;
    }

    public void setHomingSpeed(float s){
        homingSpeed = s;
    }

    public void setDistanceBetweenObstacles(float d){
//        this.originalDistanceBetweenObstacles = d;
    }

    public void updateDistanceBetweenObstacles(float factor){
        this.distanceBetweenObstacles *= factor;
    }

    public void setRotatedObsImage(int resID, int scaleX, int scaleY){
        this.rotatedObsImage = BitmapFactory.decodeResource(context.getResources(), resID, null);
        this.rotatedObsImage = Bitmap.createScaledBitmap(this.rotatedObsImage, scaleX, scaleY, true);
    }

//    public void setRotatedObsImage2(int resID, int scaleX, int scaleY){
//        this.rotatedObsImage2 = BitmapFactory.decodeResource(context.getResources(), resID, null);
//        this.rotatedObsImage2 = Bitmap.createScaledBitmap(this.rotatedObsImage2, scaleX, scaleY, true);
//    }

    public void setBlink(int count, int cycles){
        this.blinkTime = count;
        this.blinkCyclesToDisappear = cycles;
    }

    public void increaseHorizontalSpeed(float factor){
        this.horizontalSpeed += originalHSpeed * factor;
    }

    public void increaseVerticalSpeed(float factor){
        this.verticalSpeed += originalVSpeed * factor;
    }
}

