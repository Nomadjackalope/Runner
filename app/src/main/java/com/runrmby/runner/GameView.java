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
import android.util.AttributeSet;
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

    float velocity;

    Point windowSize;

    // Touches
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
            backgroundPositionY = -background.getHeight() + backgroundPositionY2;
        }
        if(backgroundPositionY2 > background.getHeight()) {
            backgroundPositionY2 = -background.getHeight() + backgroundPositionY;
        }

        // if the user is not touching trigger inertia,
        // if we did it when the users finger was down it would get out of sync
        if(fingers.isEmpty()) {
            // We don't want this running all the time using up cpu
            if(velocity > 1) {
                advanceRoad(velocity); ///0.016f);
                velocity *= 0.6f;
            }
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

    int pointerIndex;

    FingerPoint activeFinger = new FingerPoint();

    ArrayList<Integer> fingers = new ArrayList<>();

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //final int action = MotionEventCompat.getActionMasked(event);

        switch(event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                activeFinger.setNew(event.getPointerId(0), event.getX(), event.getY());
                fingers.add(event.getPointerId(0));

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
                    advanceRoad(event.getY(event.findPointerIndex(activeFinger.id)) - activeFinger.y);
                    activeFinger.y = event.getY(event.findPointerIndex(activeFinger.id));
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

    public void advanceRoad(float distance) {
        System.out.println("GV| distance: " + distance);
        //distance = distance;
        backgroundPositionY += distance;
        backgroundPositionY2 += distance;

        velocity = distance;
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
}
