package com.runrmby.runner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;

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
    static int obstacleDestroyed = 0;
    static int obstacleSpawned = 1;
    static int obstacleHit = 2;
    static int obstacleTriggered = 3;
    Random random = new Random();
    Boolean respawnWithMax;
    int lastSpawnIndex;
    Boolean randomizeParameters;
    Boolean directional;

    int[] orientationArray;

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
    }

    public boolean updateObstacles(float distance, boolean autoSpawn) {
        //Update distance moved.
        if(maxNumberOfObstacles > 0) {
            for (int i = 0; i < maxNumberOfObstacles; i++) {
                if (spawnTracker[i] != obstacleDestroyed) {
                    coordinatesArray[i][1] += distance;
                }
                //If now off screen, set as destroyed.
                if (coordinatesArray[i][1] > windowHeight || coordinatesArray[i][1] < -obstacleHeight || coordinatesArray[i][0] > windowWidth || coordinatesArray[i][0] < -obstacleWidth) {
                    spawnTracker[i] = obstacleDestroyed;
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
                if (spawnTracker[lastSpawnIndex] == obstacleDestroyed || respawnWithMax) {
                    if(wasObstacleTouched(x, y, obstacleWidth, obstacleHeight, 0, false)){
                        x += obstacleWidth;
                    }
                    spawnTracker[lastSpawnIndex] = obstacleSpawned;
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
        spawnTracker[obsIndex] = obstacleDestroyed;
        orientationArray[obsIndex] = 0;
    }

    public void hitObstacle(int obsIndex, boolean triggered){
        if(orientationArray[obsIndex] == 3){
            orientationArray[obsIndex] = 0;
        } else {
            orientationArray[obsIndex]++;
        }
        speedArray[obsIndex][0] = 0;
        speedArray[obsIndex][1] = 0;
        if(random.nextBoolean()){
            coordinatesArray[obsIndex][0] += (float)obstacleWidth;
        } else {
            coordinatesArray[obsIndex][0] -= (float)obstacleWidth;
        }
        if(triggered) {
            spawnTracker[obsIndex] = obstacleTriggered;
        } else {
            spawnTracker[obsIndex] = obstacleHit;
        }
    }

    public void moveObstacles() {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] != obstacleDestroyed) {
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
            if (spawnTracker[i] != obstacleDestroyed) {
                if (orientationArray[i] == 0) {
                    canvas.drawBitmap(obstacleImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 1) {
                    canvas.drawBitmap(rotatedObsImage, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 2) {
                    canvas.drawBitmap(rotatedObsImage2, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                } else if (orientationArray[i] == 3) {
                    canvas.drawBitmap(rotatedObsImage3, coordinatesArray[i][0], coordinatesArray[i][1], paint);
                }
            }
        }
    }

    //x and y are view coordinates. Width and height should be 0f if checking a point instead of an area.
    public Boolean wasObstacleTouched(float x, float y, float width, float height, int action, boolean touchFollower) {
        for (int i = 0; i < maxNumberOfObstacles; i++) {
            if (spawnTracker[i] != obstacleDestroyed) {
                if((spawnTracker[i] == obstacleTriggered && touchFollower)) {
                    continue;
                }
                if (orientationArray[i] == 0 || orientationArray[i] == 2) {
                    if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleWidth
                            && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleHeight) {
                        if (action == 0) {
                            //Do nothing
                        } else if (action == 1) {
                            destroyObstacle(i);
                        } else if (action == 2) { //Hit and triggered.
                            hitObstacle(i, true);
                        } else if (action == 3) {  //Hit but not triggered.
                            hitObstacle(i, false);
                        }
                        return true;
                    }
                } else {    //object rotated so width & height flipped.
                    if (x + width > coordinatesArray[i][0] && x < coordinatesArray[i][0] + obstacleHeight
                            && y + height > coordinatesArray[i][1] && y < coordinatesArray[i][1] + obstacleWidth) {
                        if (action == 0) {
                            //Do nothing
                        } else if (action == 1) {
                            destroyObstacle(i);
                        } else if (action == 2) { //Hit and triggered.
                            hitObstacle(i, true);
                        } else if (action == 3) {  //Hit but not triggered.
                            hitObstacle(i, false);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Boolean checkOverlap(int i){
            if(spawnTracker[i] != obstacleDestroyed){
                for(int j = i+1; j < maxNumberOfObstacles; j++){
                    if(spawnTracker[j] != obstacleDestroyed){
                        if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                hitObstacle(i, false);
                                hitObstacle(j, false);
                                return true;
                            }
                        } else if (orientationArray[i] % 2 == 1 && orientationArray[j] % 2 == 0) {
                            if (coordinatesArray[i][0] + obstacleHeight > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleWidth
                                    && coordinatesArray[i][1] + obstacleWidth > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleHeight) {
                                hitObstacle(i, false);
                                hitObstacle(j, false);
                                return true;
                            }
                        } else if (orientationArray[i] % 2 == 0 && orientationArray[j] % 2 == 1) {
                            if (coordinatesArray[i][0] + obstacleWidth > coordinatesArray[j][0] && coordinatesArray[i][0] < coordinatesArray[j][0] + obstacleHeight
                                    && coordinatesArray[i][1] + obstacleHeight > coordinatesArray[j][1] && coordinatesArray[i][1] < coordinatesArray[j][1] + obstacleWidth) {
                                hitObstacle(i, false);
                                hitObstacle(j, false);
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
            spawnTracker[i] = obstacleDestroyed;
        }
        this.lastSpawnIndex = maxNumberOfObstacles - 1;
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

