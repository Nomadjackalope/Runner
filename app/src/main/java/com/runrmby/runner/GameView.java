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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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

    Point windowSize;

    FingerPoint finger1 = new FingerPoint();
    FingerPoint finger2 = new FingerPoint();


    public GameView(Context context, Point windowSize) {
        super(context);
        init(windowSize);
    }

    public void init(Point p) {

        this.windowSize = p;
        holder = getHolder();
        paint = new Paint();

        BitmapFactory.Options ops = new BitmapFactory.Options();

        ops.inPreferredConfig = Bitmap.Config.RGB_565;

        background = BitmapFactory.decodeResource(this.getResources(), R.drawable.road, ops);

        System.out.println("GV| bitmap type: " + background.getConfig().name());

        //background = decodeSampledBitmapFromResource(getResources(), R.drawable.road, p.x, p.y);

        background = Bitmap.createScaledBitmap(background, p.x, p.y, true);

        backgroundPositionY2 = -background.getHeight();
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
        //backgroundPositionY += 15; // This should be set by the person's touches
        //backgroundPositionY2 += 15;
        // 15 needs to be the amount that the background not being moved has travelled
        if(backgroundPositionY > background.getHeight()) {
            backgroundPositionY = -background.getHeight() + 15;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + 15;
        }
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

            // Draw all to screen and unlock
            holder.unlockCanvasAndPost(canvas);
        }

    }

    // The first touch is ACITON_DOWN

    // ACTION_POINTER_DOWN is for extra pointers that enter the screen beyond the first


    //

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                // PointerId remains constant. Pointer index is order in touch list
                finger1.id = event.getPointerId(0);
                finger1.isNull = false;


                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                // Change the finger that isn't set yet
                if(finger2.isNull) {
                    if(event.getPointerId(0) != finger1.id) {
                        finger2.id = event.getPointerId(0);
                        finger2.isNull = false;
                    } else if(event.getPointerId(1) != finger1.id) {
                        finger2.id = event.getPointerId(1);
                        finger2.isNull = false;
                    } else {
                        System.out.println("GV| finger2 is null but not set");
                    }
                } else if(finger1.isNull) {
                    if(event.getPointerId(0) != finger2.id) {
                        finger1.id = event.getPointerId(0);
                        finger1.isNull = false;
                    } else if(event.getPointerId(1) != finger2.id) {
                        finger1.id = event.getPointerId(1);
                        finger1.isNull = false;
                    } else {
                        System.out.println("GV| finger1 is null but not set");
                    }
                }else {
                    System.out.println("GV| no is null");
                }

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_POINTER_UP:

                break;

            case MotionEvent.ACTION_UP:
                finger1.isNull = true;
                finger2.isNull = true;
                break;

//            // User touched down
//            case MotionEvent.ACTION_DOWN:
//                System.out.println("GV| action index: " + event.getActionIndex());
//                // Set finger x,y
//                // No more than 2 fingers
//                if(event.getActionIndex() < 2) {
//                    if (getFingerFromIndex(event.getActionIndex()).isNull) {
//                        getFingerFromIndex(event.getActionIndex()).isNull = false;
//                    }
//
//                    getFingerFromIndex(event.getActionIndex()).setXY(event.getX(), event.getY());
//
//                    // Don't run with both fingers, just most recent
//                    setMostRecentFinger(event.getActionIndex());
//                }
//
//
//                // if the finger lands on object do something here
//                break;
//
//            case MotionEvent.ACTION_POINTER_DOWN:
//                System.out.println("GV| action pointer index: " + event.getActionIndex());
//                // Set finger x,y
//                // No more than 2 fingers
//                if(event.getActionIndex() < 2) {
//                    if (getFingerFromIndex(event.getActionIndex()).isNull) {
//                        getFingerFromIndex(event.getActionIndex()).isNull = false;
//                    }
//
//                    getFingerFromIndex(event.getActionIndex()).setXY(event.getX(), event.getY());
//
//                    // Don't run with both fingers, just most recent
//                    setMostRecentFinger(event.getActionIndex());
//                }
//
//                break;
//
//            // User moved finger
//            case MotionEvent.ACTION_MOVE:
//                System.out.println("GV| move index: " + event.getActionIndex());
//                if(event.getActionIndex() == 0) {
//                    if(finger1.mostRecent) {
//                        advanceRoad(event.getY() - finger1.y);
//                        finger1.setXY(event.getX(), event.getY());
//                    }
//                } else if(event.getActionIndex() == 1) {
//                    if(finger2.mostRecent) {
//                        advanceRoad(event.getY() - finger2.y);
//                        finger2.setXY(event.getX(), event.getY());
//                    }
//                }
//                break;
//
//            // User lifted finger up
//            case MotionEvent.ACTION_UP:
//                if(event.getActionIndex() == 0) {
//                    finger1.isNull = true;
//                } else if(event.getActionIndex() == 1) {
//                    finger2.isNull = true;
//                }
//                break;

        }
        return true;
    }

    public void setMostRecentFinger(int index) {
        if(index == 0) {
            finger1.mostRecent = true;
            finger2.mostRecent = false;
        } else if(index == 1) {
            finger1.mostRecent = false;
            finger2.mostRecent = true;
        }
    }

    public void advanceRoad(float distance) {
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;
    }

    public FingerPoint getFingerFromIndex(int index) {
        if(index == 0) {
            return finger1;
        } else if(index == 1) {
            return finger2;
        } else {
            return null;
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
        gameThread = new Thread(this);
        gameThread.start();
    }

    public class FingerPoint {
        float x, y;

        int id;

        // Using this vs null because then we don't create a bunch of these objects
        boolean isNull;

        boolean mostRecent;

        public void setXY(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
