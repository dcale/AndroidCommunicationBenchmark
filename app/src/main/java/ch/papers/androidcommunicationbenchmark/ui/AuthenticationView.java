package ch.papers.androidcommunicationbenchmark.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.View;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 08/01/16.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class AuthenticationView extends View {
    private final byte[] digest;
    private final List<Point> points = new ArrayList<Point>();
    private final Paint dotPaint = new Paint();
    private final Paint patternPaint = new Paint();

    public AuthenticationView(Context context, byte[] key) {
        super(context);
        byte[] digest = new byte[0];
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(key);
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        this.digest = digest;
        this.dotPaint.setColor(Color.BLACK);
        this.dotPaint.setAntiAlias(true);
        this.dotPaint.setStyle(Paint.Style.FILL);

        this.patternPaint.setColor(Color.GREEN);
        this.patternPaint.setAntiAlias(true);
        this.patternPaint.setStyle(Paint.Style.FILL);
        this.patternPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        this.patternPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        this.patternPaint.setPathEffect(new CornerPathEffect(10));
        this.patternPaint.setDither(true);
        Log.d("sha",toHex(digest));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(this.generateDigestColor());

        int squareSize = Math.min(canvas.getHeight(), canvas.getWidth());

        int cellSize = squareSize / 4;
        int cellPadding = cellSize / 2;
        int circleSize = cellPadding / 8;


        this.patternPaint.setColor(this.getComplementColor(this.generateDigestColor()));
        this.patternPaint.setStrokeWidth(circleSize * 2);

        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                canvas.drawCircle(cellPadding + cellSize * i, cellPadding + cellSize * k, circleSize, dotPaint);
                points.add(new Point(cellPadding + cellSize * i, cellPadding + cellSize * k));
            }
        }

        for (int i = 6; i < 32; i++) {
            if (this.isBitSet((byte)((int)digest[i]&digest[i-1]), i % 8)) {
                final int currentNumber = this.toHalfByteInt(digest[i]);
                final int lastNumber = this.toOtherHalfByteInt(digest[i]);
                canvas.drawLine(this.points.get(lastNumber).x, this.points.get(lastNumber).y, this.points.get(currentNumber).x, this.points.get(currentNumber).y, this.patternPaint);
            }
        }
    }

    private int generateDigestColor() {

        final int baseColor = Color.WHITE;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + this.toUnsignedInt(digest[0])) / 2;
        final int green = (baseGreen + this.toUnsignedInt(digest[1])) / 2;
        final int blue = (baseBlue + this.toUnsignedInt(digest[2])) / 2;

        return Color.rgb(red, green, blue);
    }

    private int getComplementColor(int color) {
        final int baseColor = Color.BLACK;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + this.toUnsignedInt(digest[3])) / 2;
        final int green = (baseGreen + this.toUnsignedInt(digest[4])) / 2;
        final int blue = (baseBlue + this.toUnsignedInt(digest[5])) / 2;

        return Color.rgb(red, green, blue);

/*
        return Color.rgb(255 - Color.red(color),
                255 - Color.green(color),
                255 - Color.blue(color));*/
    }

    private int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    private boolean isBitSet(byte value, int bitIndex) {
        return (value & (1 << bitIndex)) != 0;
    }

    private int toHalfByteInt(byte x) {
        // get low order nibble, 4 bits.
        return ((int) x) & 0xf;
    }

    private int toOtherHalfByteInt(byte x) {
        // get high order nibble, 4 bits and move to low order.
        return ((int) x) >> 4 & 0xf;
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }


}
