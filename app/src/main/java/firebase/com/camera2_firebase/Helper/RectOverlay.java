package firebase.com.camera2_firebase.Helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class RectOverlay extends GraphicOverlay.Graphic {

    private int RECT_COLOR = Color.RED;
    private  float STROKE_WIDTH = 4.0f;
    private Paint rectPaint;
    private  GraphicOverlay graphicOverlay;
    private Rect rect;
    private String faceName;

    public RectOverlay(GraphicOverlay graphicOverlay, Rect rect, String name) {
        super(graphicOverlay);
        faceName = name;
        rectPaint = new Paint();
        rectPaint.setColor(RECT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        this.graphicOverlay = graphicOverlay;
        this.rect = rect;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        double namelen = faceName.length() * 15;
        RectF rectF = new RectF(rect);
        rectF.left = translateX(rectF.left);
        rectF.right = translateX(rectF.right);
        rectF.top = translateY(rectF.top);
        rectF.bottom = translateY(rectF.bottom);

        canvas.drawRect(rectF, rectPaint);
        int posy = (int)(rectF.bottom+50);
        int posx = (int)(((rectF.right) - ((rectF.right - rectF.left)/2)) - namelen);

        Paint paint = new Paint();
        paint = rectPaint;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(50);
        canvas.drawText(faceName, posx, posy, paint);
    }
}
