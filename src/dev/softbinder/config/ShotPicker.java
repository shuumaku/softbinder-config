package dev.softbinder.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class ShotPicker {
    private static final int DELAY_MS = 4000;
    private static final ExecutorService worker = Executors.newSingleThreadExecutor();

    private ShotPicker() {
    }

    static void start(final Activity a, String pkg, final EditText param) {
        final Intent target = pkg.isEmpty() ? null : a.getPackageManager().getLaunchIntentForPackage(pkg);
        final String self = a.getComponentName().flattenToShortString();
        Toast.makeText(a, target != null ? "Opening the app; capturing in 4 s..." : "Go to the screen; capturing in 4 s...",
                Toast.LENGTH_LONG).show();
        if (target != null)
            a.startActivity(target);
        worker.execute(() -> {
            byte[] png = null;
            String error = null;
            try {
                Thread.sleep(DELAY_MS);
                png = Root.bytes("screencap -p");
                Root.run("am start -n " + self + " -f 0x20000"); // REORDER_TO_FRONT: same instance, dialog intact
            } catch (IOException e) {
                error = e.getMessage();
            } catch (InterruptedException e) {
                error = "interrupted";
            }
            final byte[] data = png;
            final String err = error;
            a.runOnUiThread(() -> {
                Bitmap bmp = data == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bmp == null) {
                    Toast.makeText(a, "Screenshot failed" + (err != null ? ": " + err : ""), Toast.LENGTH_LONG).show();
                    return;
                }
                show(a, bmp, param);
            });
        });
    }

    private static void show(final Activity a, final Bitmap bmp, final EditText param) {
        final int[] picked = { -1, -1 };
        final View view = new View(a) {
            private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            private float scale, ox, oy;

            @Override
            protected void onMeasure(int wSpec, int hSpec) {
                int w = MeasureSpec.getSize(wSpec);
                int max = (int) (a.getResources().getDisplayMetrics().heightPixels * 0.7f);
                setMeasuredDimension(w, Math.min(max, Math.round(w * bmp.getHeight() / (float) bmp.getWidth())));
            }

            @Override
            protected void onDraw(Canvas c) {
                scale = Math.min(getWidth() / (float) bmp.getWidth(), getHeight() / (float) bmp.getHeight());
                ox = (getWidth() - bmp.getWidth() * scale) / 2;
                oy = (getHeight() - bmp.getHeight() * scale) / 2;
                c.save();
                c.translate(ox, oy);
                c.scale(scale, scale);
                c.drawBitmap(bmp, 0, 0, paint);
                c.restore();
                if (picked[0] >= 0) {
                    float x = ox + picked[0] * scale, y = oy + picked[1] * scale, r = 14 * getResources().getDisplayMetrics().density;
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(0xFFFF0000);
                    c.drawCircle(x, y, r, paint);
                    c.drawLine(x - r * 1.6f, y, x + r * 1.6f, y, paint);
                    c.drawLine(x, y - r * 1.6f, x, y + r * 1.6f, paint);
                }
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_DOWN
                        || e.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = Math.round((e.getX() - ox) / scale), y = Math.round((e.getY() - oy) / scale);
                    if (x >= 0 && y >= 0 && x < bmp.getWidth() && y < bmp.getHeight()) {
                        picked[0] = x;
                        picked[1] = y;
                        invalidate();
                    }
                }
                return true;
            }
        };
        new AlertDialog.Builder(a).setTitle("Touch where to tap").setView(view)
                .setPositiveButton("Use", (d, w) -> {
                    if (picked[0] >= 0)
                        param.setText(picked[0] + " " + picked[1]);
                })
                .setNegativeButton("Cancel", null).show();
    }
}
