package dev.softbinder.config;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Picks tap coordinates: choose a screenshot with the system image chooser,
 * then touch the spot. Coordinates are scaled to the device's screen when the
 * image is the same shape but a different size.
 */
final class ShotPicker {
    private static final int MAX_BYTES = 32 << 20;
    private static final int MAX_SIDE = 2048;

    private ShotPicker() {
    }

    static void start(final Screen a, final EditText param) {
        Intent pick = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
        a.startForResult(Intent.createChooser(pick, "Choose a screenshot"), data -> {
            if (data == null || data.getData() == null)
                return;
            load(a, data.getData(), param);
        });
    }

    private static void load(final Screen a, final Uri uri, final EditText param) {
        a.io.execute(() -> {
            Bitmap bmp = null;
            int fullW = 0, fullH = 0;
            String error = null;
            try {
                byte[] bytes = read(a, uri);
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);
                fullW = o.outWidth;
                fullH = o.outHeight;
                if (fullW <= 0 || fullH <= 0)
                    throw new IOException("not an image");
                o = new BitmapFactory.Options();
                o.inSampleSize = 1; // defaults to 0
                while (fullW / o.inSampleSize > MAX_SIDE || fullH / o.inSampleSize > MAX_SIDE)
                    o.inSampleSize *= 2;
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o);
                if (bmp == null)
                    throw new IOException("could not decode the image");
            } catch (IOException | RuntimeException e) {
                error = e.getMessage();
            } catch (OutOfMemoryError e) {
                error = "image too large";
            }
            final Bitmap shown = bmp;
            final int w = fullW, h = fullH;
            final String err = error;
            a.runOnUiThread(() -> {
                if (shown == null)
                    a.toast("Cannot open image" + (err != null ? ": " + err : ""));
                else
                    show(a, shown, w, h, param);
            });
        });
    }

    private static byte[] read(Screen a, Uri uri) throws IOException {
        InputStream in = a.getContentResolver().openInputStream(uri);
        if (in == null)
            throw new IOException("cannot read it");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[65536];
            for (int n; (n = in.read(buf)) > 0;) {
                out.write(buf, 0, n);
                if (out.size() > MAX_BYTES)
                    throw new IOException("file is too big");
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    /**
     * Image pixels to screen pixels: unchanged if the sizes match, scaled if
     * the image is the same shape as the screen, otherwise null (don't guess).
     */
    static int[] toScreen(int x, int y, int imgW, int imgH, int scrW, int scrH) {
        if (imgW == scrW && imgH == scrH)
            return new int[] { x, y };
        if (Math.abs(imgW / (double) imgH - scrW / (double) scrH) > 0.02)
            return null;
        return new int[] { Math.min(scrW - 1, Math.round(x * scrW / (float) imgW)),
                Math.min(scrH - 1, Math.round(y * scrH / (float) imgH)) };
    }

    private static void show(final Screen a, final Bitmap bmp, final int imgW, final int imgH, final EditText param) {
        final Point scr = new Point();
        a.getWindowManager().getDefaultDisplay().getRealSize(scr);
        // The point is kept in full-image pixels; the bitmap may be a subsampled copy.
        final int[] picked = { -1, -1 };
        String[] cur = param.getText().toString().trim().split("\\s+");
        if (cur.length == 2 && cur[0].matches("\\d+") && cur[1].matches("\\d+") && imgW == scr.x && imgH == scr.y) {
            picked[0] = Integer.parseInt(cur[0]);
            picked[1] = Integer.parseInt(cur[1]);
        }
        if (picked[0] < 0) {
            picked[0] = imgW / 2;
            picked[1] = imgH / 2;
        }
        final AlertDialog[] dialog = { null };
        final TextView readout = new TextView(a);
        readout.setPadding(Ui.dp(a, 12), Ui.dp(a, 6), Ui.dp(a, 12), Ui.dp(a, 6));
        readout.setTextSize(14);
        readout.setSingleLine();
        final Runnable update = () -> readout.setText("Tap at " + picked[0] + ", " + picked[1]);
        update.run();

        final View view = new View(a) {
            private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            private float scale, ox, oy;

            @Override
            protected void onMeasure(int wSpec, int hSpec) {
                int w = MeasureSpec.getSize(wSpec);
                int max = (int) (a.getResources().getDisplayMetrics().heightPixels * 0.55f);
                setMeasuredDimension(w, Math.min(max, Math.round(w * imgH / (float) imgW)));
            }

            @Override
            protected void onDraw(Canvas c) {
                scale = Math.min(getWidth() / (float) imgW, getHeight() / (float) imgH);
                ox = (getWidth() - imgW * scale) / 2;
                oy = (getHeight() - imgH * scale) / 2;
                c.save();
                c.translate(ox, oy);
                c.scale(imgW * scale / bmp.getWidth(), imgH * scale / bmp.getHeight());
                c.drawBitmap(bmp, 0, 0, paint);
                c.restore();
                if (picked[0] >= 0) {
                    float x = ox + picked[0] * scale, y = oy + picked[1] * scale;
                    float r = 14 * getResources().getDisplayMetrics().density;
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(0xFFFF0000);
                    c.drawCircle(x, y, r, paint);
                    c.drawLine(x - r * 1.6f, y, x + r * 1.6f, y, paint);
                    c.drawLine(x, y - r * 1.6f, x, y + r * 1.6f, paint);
                }
            }

            @Override
            public boolean onKeyDown(int keyCode, KeyEvent e) {
                int dx = 0, dy = 0;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        dx = -1;
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        dx = 1;
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        dy = -1;
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        dy = 1;
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if (dialog[0] != null)
                            dialog[0].getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                        return true;
                    default:
                        return super.onKeyDown(keyCode, e);
                }
                int r = e.getRepeatCount();
                int step = r < 3 ? 1 : r < 10 ? 4 : r < 25 ? 12 : 30;
                picked[0] = Math.max(0, Math.min(imgW - 1, picked[0] + dx * step));
                picked[1] = Math.max(0, Math.min(imgH - 1, picked[1] + dy * step));
                update.run();
                invalidate();
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if (scale == 0)
                    return true;
                int x = Math.round((e.getX() - ox) / scale), y = Math.round((e.getY() - oy) / scale);
                picked[0] = Math.max(0, Math.min(imgW - 1, x));
                picked[1] = Math.max(0, Math.min(imgH - 1, y));
                update.run();
                invalidate();
                return true;
            }
        };

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        box.addView(readout);
        dialog[0] = new AlertDialog.Builder(a).setView(box)
                .setPositiveButton("Use", null)
                .setNegativeButton("Cancel", null).create();
        dialog[0].show();
        // Set after show() so a refused point leaves the picker open.
        dialog[0].getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int[] p = toScreen(picked[0], picked[1], imgW, imgH, scr.x, scr.y);
            if (p == null && picked[0] < scr.x && picked[1] < scr.y) {
                a.toast("Image is " + imgW + "x" + imgH + ", screen is " + scr.x + "x" + scr.y
                        + ": using image pixels");
                p = new int[] { picked[0], picked[1] };
            }
            if (p == null) {
                a.toast("Image is " + imgW + "x" + imgH + "; the screen is " + scr.x + "x" + scr.y
                        + ". Use a screenshot from this phone.");
                return;
            }
            param.setText(p[0] + " " + p[1]);
            param.requestFocus(); // keeps the form scrolled to the field
            dialog[0].dismiss();
        });
        view.requestFocus();
    }
}
