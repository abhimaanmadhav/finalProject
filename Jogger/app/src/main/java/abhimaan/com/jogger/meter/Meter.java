/**
 *
 */
package abhimaan.com.jogger.meter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;

import abhimaan.com.jogger.R;

/**
 * @author Abhimaan Madhav
 */
public class Meter extends View
{

    /**
     * @param context
     */

    Paint paint, bgPaint, distancePaint;
    RectF rectF;
    final int START = 120;
    final int END = 300;
    final int MAX_PROGRESS = 100;
    final float factor = END / MAX_PROGRESS;
    private double progress = 0,
            tempProgress = 0;
    private final long delay = 50;
    private int radius;
    private PointF centerPoint;
    private boolean running = false;
    Handler handler;

    private final int CIRCLE_THICKNESS = 10;
    Context context;
    DecimalFormat f;


    public Meter(Context context)
        {
            super(context);
            this.context = context;
            init();

        }


    public Meter(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            this.context = context;
            init();

        }


    public Meter(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);
            this.context = context;
            init();

        }


    @Override
    protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            canvas.drawArc(rectF, START, END, false, bgPaint);
            canvas.drawArc(rectF, START, (float) (tempProgress * factor), false,
                    paint);
            canvas.drawText(Integer.toString((int) tempProgress) + "%", getWidth() / 2,
                    getHeight() / 2, distancePaint);
//            PointF pointF = PointOnCircle((float) (START + (tempProgress * factor)));
//            canvas.drawBitmap(thumbBitmap, pointF.x - (thumbBitmap.getWidth() / 2),
//                    pointF.y - (thumbBitmap.getHeight() / 2), paint);
        }


    private PointF PointOnCircle(float angleInDegrees)
        {
            // Convert from degrees to radians via multiplication by PI/180
            float x = (float) (radius * Math.cos(angleInDegrees * Math.PI / 180F))
                    + centerPoint.x;
            float y = (float) (radius * Math.sin(angleInDegrees * Math.PI / 180F))
                    + centerPoint.y;
            return new PointF(x, y);
        }


    /**
     *
     */
    void init()
        {
            f = new DecimalFormat("##.00");
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(convertToDp(CIRCLE_THICKNESS));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary,
                    null));
            bgPaint = new Paint();
            bgPaint.setAntiAlias(true);
            bgPaint.setStyle(Style.STROKE);
            bgPaint.setStrokeWidth(convertToDp(CIRCLE_THICKNESS));
            bgPaint.setColor(Color.parseColor("#00BFFF"));
            bgPaint.setStrokeCap(Paint.Cap.ROUND);
            distancePaint = new Paint();
            distancePaint.setTextAlign(Align.CENTER);
            distancePaint.setColor(Color.parseColor("#00BFFF"));
            distancePaint.setAntiAlias(true);
            distancePaint.setTextSize(textSize(18));
            distancePaint.setStyle(Style.STROKE);
            handler = new Handler();
        }


    void measurment()
        {
            int dimension;
            if (getHeight() < getWidth())
                {
                    dimension = getHeight();
                } else
                {
                    dimension = getWidth();
                }
            radius = (dimension / 2) - convertToDp(CIRCLE_THICKNESS);
            rectF = new RectF((getWidth() / 2) - (radius), (getHeight() / 2)
                    - (radius), (getWidth() / 2) + (radius), (getHeight() / 2)
                    + (radius));
            centerPoint = new PointF(getWidth() / 2, getHeight() / 2);

        }


    private int convertToDp(int pixelSize)
        {
            final float scale = getResources().getDisplayMetrics().density;
            int value = (int) (pixelSize * scale);
            return value;

        }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom)
        {
            super.onLayout(changed, left, top, right, bottom);
            init();
            measurment();
        }


    public void setProgress(float progress, boolean animated)
        {
            progress = progress % 101;
            this.progress = progress;
            if (animated & !running)
                {
                    tempProgress = 0;
                    running = true;
                    handler.postDelayed(new MeterRunnable(), delay);
                } else
                {
                    //tODO
                    tempProgress = progress;
                    invalidate();
                }

        }


    @Override
    protected void onDetachedFromWindow()
        {
            handler.removeCallbacksAndMessages(null);
            super.onDetachedFromWindow();
        }


    class MeterRunnable implements Runnable
    {

        @Override
        public void run()
            {
                if (running && tempProgress < progress)
                    {
                        tempProgress = tempProgress + 2;
                        if (tempProgress > progress)
                            {
                                tempProgress = progress;
                                invalidate();
                            } else
                            {
                                //TODO
//                                tempactual = (tempProgress * MAX_SPEED) / 100;
                                invalidate();
                                handler.postDelayed(new MeterRunnable(), delay);
                            }
                    } else
                    {
                        tempProgress = progress;
                        running = false;
                        invalidate();
                    }
            }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            measurment();
        }


    int textSize(int textSizeSP)
        {
            return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    textSizeSP, getResources().getDisplayMetrics()));
        }

}