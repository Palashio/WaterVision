package kcps.impurivision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DisplayActivity extends AppCompatActivity {

    private int imgNum = 0;
    private Bitmap bmp1, bmp2, bmp3, bmp4, bmp5, bmp6;
    private byte[] bytes3, bytes4, bytes5, bytes6;
    private int[][] writeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        new doWork().execute();
    }

    private class doWork extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            final ImageView img = findViewById(R.id.displayImg);
            img.setImageBitmap(bmp1);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgNum = ((imgNum + 1) % 6);
                    if (imgNum == 0) {
                        img.setImageBitmap(bmp1);
                    } else if (imgNum == 1) {
                        img.setImageBitmap(bmp2);
                    } else if (imgNum == 2) {
                        img.setImageBitmap(bmp3);
                    } else if (imgNum == 3 ){
                        img.setImageBitmap(bmp4);
                    } else if (imgNum == 4) {
                        img.setImageBitmap(bmp5);
                    } else {
                        img.setImageBitmap(bmp6);
                    }
                }
            });
            final Button button = findViewById(R.id.button);
            button.setEnabled(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button.setBackgroundColor(Color.RED);
                    new DownloadTask(0).execute(bytes3, bytes4, bytes5, bytes6);
                }
            });
        }


        private class DownloadTask extends AsyncTask<byte[], Void, byte[][]> {

            private int n;

            public DownloadTask(int n) {
                this.n = n;
            }

            @Override
            protected void onPostExecute(byte[][] bytes) {
                if (n < 3) {
                    new DownloadTask(n+1).execute(bytes);
                } else {
                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Impurivision");
                    Toast t = Toast.makeText(DisplayActivity.this, "Done " + mediaStorageDir.listFiles().length, Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 30);
                    t.show();
                }

            }

            @Override
            protected byte[][] doInBackground(byte[]... bytes) {
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Impurivision");
                mediaStorageDir.mkdirs();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                String type = (n == 0) ? "init" : (n == 1) ? "post" : (n == 2) ? "diff" : "clean";
                File pictureFile =  new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + "_" + type + ".jpg");;
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    try {
                        fos.write(bytes[n]);
                    } catch (IOException e) {} finally {
                        try {
                            fos.close();
                        } catch (IOException e) {}
                    }
                } catch (FileNotFoundException e) {}
                return bytes;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            byte[] bmpArr1 = CameraActivity.bmp1;
            byte[] bmpArr2 = CameraActivity.bmp2;
            bmp1 = BitmapFactory.decodeByteArray(bmpArr1, 0, bmpArr1.length);
            bmp2 = BitmapFactory.decodeByteArray(bmpArr2, 0, bmpArr2.length);
            int[][] pmap1 = bitmapToPixelmap(bmp1);
            int[][] pmap2 = bitmapToPixelmap(bmp2);
            int[][][] diffAndClean = pixelmapDifference(pmap1, pmap2);
            int[][] pmap3 = diffAndClean[0];
            int[][] pmap4 = diffAndClean[1];
            bmp3 = pixelmapToBitmap(pmap1);
            bmp4 = pixelmapToBitmap(pmap2);
            bmp5 = pixelmapToBitmap(pmap3);
            bmp6 = pixelmapToBitmap(pmap4);
            bytes3 = bitmapToBytes(bmp3);
            bytes4 = bitmapToBytes(bmp4);
            bytes5 = bitmapToBytes(bmp5);
            bytes6 = bitmapToBytes(bmp6);
            writeMap = pmap4;
            return null;
        }
    }

    private byte[] bitmapToBytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private int[][][] pixelmapDifference(int[][] init, int[][] post) {
        int[][] diff = new int[init.length][init[0].length];
        int[][] clean = new int[init.length][init[0].length];
        for (int x = 0; x < diff.length; x++) {
            for (int y = 0; y < diff[0].length; y++) {
                if (inRadius(x, y)) {

                    int colorPost = post[x][y];
                    int rPost = Color.red(colorPost);
                    int gPost = Color.green(colorPost);
                    int bPost = Color.blue(colorPost);

                    int colorInit = init[x][y];
                    int rInit = Color.red(colorInit);
                    int gInit = Color.green(colorInit);
                    int bInit = Color.blue(colorInit);

                    int rDiff = rPost - rInit;
                    int gDiff = gPost - gInit;
                    int bDiff = bPost - bInit;

                    diff[x][y] = Color.rgb(rDiff, gDiff, bDiff);
                    int extreme = 0;
                    boolean[] diffs = {rDiff < 0, gDiff < 0, bDiff < 0};
                    for (boolean componentDiff : diffs) {
                        if (componentDiff) extreme++;
                    }
                    if (extreme == 1 || extreme == 2) {
                        clean[x][y] = Color.TRANSPARENT;
                    } else {
                        clean[x][y] = diff[x][y];
                    }

                }
            }
        }
        return new int[][][]{diff, clean};
    }

    private boolean inRadius(int x, int y) {
        int h = bmp1.getWidth() / 2;
        int k = bmp1.getHeight() / 2;
        int r = bmp1.getWidth() / 4; // not sure why this works
        int d2 = (h - x) * (h - x) + (y - k) * (y - k);
        int r2 = r * r;
        return d2 < r2;
    }

    private Bitmap pixelmapToBitmap(int[][] pixelmap) {
        int width = pixelmap.length;
        int height = pixelmap[0].length;
        int[] pixels = new int[width * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i + j * width] = pixelmap[i][j];
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private int[][] bitmapToPixelmap(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[][] map = new int[width][height];
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (inRadius(x, y)) {
                    map[x][y] = bmp.getPixel(x, y);
                } else {
                    map[x][y] = Color.TRANSPARENT;
                }
            }
        }
        return map;
    }

}
