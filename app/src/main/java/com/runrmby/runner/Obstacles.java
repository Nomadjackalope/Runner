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
    float distanceBetweenObstacles;
    float originalHSpeed;
    float originalVSpeed;
    int windowWidth;
    int windowHeight;
    //float nextObstacleAt;
    float horizontalSpeed;
    float verticalSpeed;
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

    int[] orientationArray;

    static final int blinkTime = 10;
    int blinkOnCountDown = blinkTime;
    int blinkOffCountDown = blinkTime;
    static final int blinkCyclesToDisappear = 6;
    int[] blinkCyclesToDisappearArray;

    /**
     * @param randomize allows certain parameters to have variation.
     * //TODO: add two more columns to coordinates array to keep track of horizontal and vertical speed for individual obstacles so that randomizing doesn't affect spawned obstacles, even though that's kind of fun.
     */
    public Obstacles(Context context, int scaleX, int scaleY, int obstacleImageResID, int maxNumberOfObstacles, Boolean respawnWithMax, float distanceBetweenObstacles, float horizontalSpeed, float verticalSpeed, float homingSpeed, int windowWidth, int windowHeight, Boolean randomize, boolean directional){
        this.obstacleImage = BitmapFactory.decodeResource(context.getResources(), obstacleImageResID, null);
        this.obstacleImage = Bitmap.createScaledBitmap(this.obstacleImage, scaleX, scaleY, true);
        this.maxNumberOfObstacles = maxNumberOfObstacles;
        this.respawnWithMax = respawnWithMax;
        this.distanceBetweenObstacles = distanceBetweenObstacles;
        this.originalHSpeed = horizontalSpeed;
        this.horizontalSpeed = originalHSpeed;
        this.originalVSpeed = verticalSpeed;
        this.verticalSpeed = originalVSpeed;
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

        this.orientationArray = new int[maxNumberOfObstacles];

        Matrix matrix = new Matrix();
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
                if (coordinatesArray[i][1] > windowHeight || coordinatesArray[i][1] < -obstacleHeight || coordinatesArray[i][0] > windowWidth || coordinatesArray[i][0] < -obstacleWidth) {
                    spawnTracker[i] = DESTROYED;
                    //Could reset coordinates here, but doesn't seem necessary as they are always set when a spawn occurs.
                }
            }

            distanceToNextObstacle -= distance;

            if (autoSpawn) {
                if(spawnObstacle(distanceToNextObstacle, random.nextInt(windowWidth - obstacleWidth), -obstacleHeight)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean spawnObstacle(float distance, float x, float y){
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
                    if(wasObstacleTouched(x, y, obstacleWidth, obstacleHeight, false, false, false, true)){
                        x += obstacleWidth;
                    }
                    spawnTracker[lastSpawnIndex] = SPAWNED;
                    coordinatesArray[lastSpawnIndex][0] = x;
                    coordinatesArray[lastSpawnIndex][1] = y;
                    if (!randomizeParameters) {
                        distanceToNextObstacle = distanceBetweenObstacles;
                    } else {
                        //If randomizeParameters selected, vary several parameters for each spawn. !!!THIS AFFECTS MOTION OF ALL SPAWNED OBSTACLES!!!
                        distanceToNextObstacle = distanceBetweenObstacles * (random.nextInt(7) + 1) / 4;
                        horizontalSpeed = originalHSpeed * (((float)(random.nextInt(15) - 8)) / 4);
                        verticalSpeed = originalVSpeed * ((float)((random.nextInt(15) - 8)) / 4);
                        speedArray[lastSpawnIndex][0] = horizontalSpeed;
                        speedArray[lastSpawnIndex][1] = verticalSpeed;
                        speedArray[lastSpawnIndex][2] = homingSpeed * 0.75f;
                        speedArray[lastSpawnIndex][3] = homingSpeed;
//                        if (random.nextBoolean()) {
//                            horizontalSpeed = -originalHSpeed;
//                        }
//                        if (random.nextBoolean()){
//                            verticalSpeed = -originalVSpeed;
//                        }

                    }
                    if(directional && verticalSpeed < 0){
                        orientationArray[lastSpawnIndex] = 2;
                    }else{
                        orientationArray[lastSpawnIndex] = 0;
                    }
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
    }

    public void hitObstacle(int obsIndex, boolean isTF, boolean trigger){
        switch (spawnTracker[obsIndex]){
            case DESTROYED:
                return;
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
                break;
            case HIT_AND_TRIGGERED:
                break;
        }
        speedArray[obsIndex][0] = 0;
        speedArray[obsIndex][1] = 0;
        if(!isTF) {
            if(orientationArray[obsIndex] == 3){
                orientationArray[obsIndex] = 0;
            } else {
                orientationArray[obsIndex]++;
            }
            if(random.nextBoolean()){
                coordinatesArray[obsIndex][0] += (float)obstacleWidth;
            } else {
                coordinatesArray[obsIndex][0] -= (float)obstacleWidth;
            }
        }
    }

    public void moveObstacles() {
        //Obstacle movement independent of road.
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] != DESTROYED) {
                if (coordinatesArray[i][0] < windowWidth) {
                    coordinatesArray[i][0] += speedArray[i][0];
                }
                if (coordinatesArray[i][1] < windowHeight) {
                    coordinatesArray[i][1] += speedArray[i][1];
                }
            }
        }
    }

    public void drawObstacles(Canvas canvas, Paint paint) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if(spawnTracker[i] == SPAWNED || spawnTracker[i] == HIT_BY_OBSTACLE){
                if (orientationArray[i] == 0) {
                    canvas.drawBitmap(obstacleImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 1) {
                    canvas.drawBitmap(rotatedObsImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 2) {
                    canvas.drawBitmap(rotatedObsImage2, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 3) {
                    canvas.drawBitmap(rotatedObsImage3, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                }
            } else if(spawnTracker[i] == TRIGGERED || spawnTracker[i] == HIT_AND_TRIGGERED){
                //TODO: Figure out how to make transparent or a different look so user can see it's been triggered
                if(blinkOffCountDown > 0){
                    --blinkOffCountDown;
                } else if(blinkOnCountDown > 0) {
                    --blinkOnCountDown;
                    if (orientationArray[i] == 0) {
                        canvas.drawBitmap(obstacleImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                    } else if (orientationArray[i] == 1) {
                        canvas.drawBitmap(rotatedObsImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                    } else if (orientationArray[i] == 2) {
                        canvas.drawBitmap(rotatedObsImage2, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                    } else if (orientationArray[i] == 3) {
                        canvas.drawBitmap(rotatedObsImage3, coordinatesArray[i][0], coordinatesArray[i][1], paint);
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
                        } else {
                            hitObstacle(i, isTF, trigger);
                        }
                        return true;
                    }
                } else {    //object rotated so width & height flipped.
                    if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleHeight
                            && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleWidth) {
                        if(checkOnly){
                            return true;
                        }
                        if(destroy){
                            destroyObstacle(i);
                        } else {
                            hitObstacle(i, isTF, trigger);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Boolean checkOverlap(int i){
            if(spawnTracker[i] != DESTROYED){
                for(int j = i+1; j < maxNumberOfObstacles; j++){
                    if(spawnTracker[j] != DESTROYED){
                        if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                hitObstacle(i, false, false);
                                hitObstacle(j, false, false);
                                return true;
                            }
                        } else if (orientationArray[i] % 2 == 1 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleHeight > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleWidth > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                hitObstacle(i, false, false);
                                hitObstacle(j, false, false);
                                return true;
                            }
                        } else if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 1) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleHeight
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleWidth) {
                                hitObstacle(i, false, false);
                                hitObstacle(j, false, false);
                                return true;
                            }
                        }
                    }
                }
            }
        return false;
    }

    public void resetObstacles(){
        this.distanceToNextObstacle = distanceBetweenObstacles;
        //this.nextObstacleAt = distanceBetweenObstacles;
        for(int i = 0; i < maxNumberOfObstacles; i++){
            coordinatesArray[i][0] = 0;
            coordinatesArray[i][1] = 0;
            orientationArray[i] = 0;
            speedArray[i][0] = horizontalSpeed;
            speedArray[i][1] = verticalSpeed;
            spawnTracker[i] = DESTROYED;

            blinkCyclesToDisappearArray[i] = blinkCyclesToDisappear;
        }
        this.lastSpawnIndex = maxNumberOfObstacles - 1;

        blinkOffCountDown = blinkTime;
        blinkOnCountDown = blinkTime;
    }

    public void resetObstacles(float distanceBetweenObstacles, int windowWidth, int windowHeight){
        resetObstacles();
        this.distanceBetweenObstacles = distanceBetweenObstacles;
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
        this.distanceBetweenObstacles = d;
    }
}

