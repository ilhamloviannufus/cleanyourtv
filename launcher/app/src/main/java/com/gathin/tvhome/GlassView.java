package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * The focus highlight: a bold solid-colour outline, no fill, no shadow or glow behind it. One instance
 * floats over the grid and is moved with spring animations between tiles, kids-game "bubble UI" style
 * instead of the earlier frosted-glass slab.
 */
public class GlassView extends View {

    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private float radius;
    /** The line's colour: white normally, a festival's accent (diya gold) or a mode accent (reorder blue). */
    private int rimColor = 0xFFFFFFFF;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 20f * density;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(4f * density);
        rim.setColor(rimColor);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Tints the line; 0 restores plain white. */
    void setAccent(int argb) {
        rimColor = argb == 0 ? 0xFFFFFFFF : (0xFF000000 | (argb & 0xFFFFFF));
        rim.setColor(rimColor);
        invalidate();
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = rim.getStrokeWidth() / 2f;
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawRoundRect(rect, radius, radius, rim);
    }
}
