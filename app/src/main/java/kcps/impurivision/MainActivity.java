package kcps.impurivision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random rnd = new Random();
    private boolean incrR = true, incrG = true, incrB = false;
    private Handler h = new Handler();
    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                } else if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    startActivity(new Intent(view.getContext(), CameraActivity.class));
                }
            }
        });

        r = new Runnable() {
            @Override
            public void run() {
                int minR = incrR ? 0 : -2, maxR = incrR ? 2 : 0;
                int minG = incrG ? 0 : -2, maxG = incrG ? 2 : 0;
                int minB = incrB ? 0 : -2, maxB = incrB ? 2 : 0;
                int i = ((ColorDrawable) toolbar.getBackground()).getColor();
                int r = bind(Color.red(i) + rand(minR, maxR), 40, 215);
                int g = bind(Color.green(i) + rand(minG, maxG), 40, 215);
                int b = bind(Color.blue(i) + rand(minB, maxB), 40, 215);
                int c = Color.argb(255, r, g, b);
                int c2 = Color.argb(255, (int)(0.75 * r), (int)(0.75 * g), (int) (0.75 * b));
                toolbar.setBackgroundColor(c);
                getWindow().setStatusBarColor(c2);
                fab.setBackgroundColor(c);
                fab.setRippleColor(c2);
                if (r == 40) incrR = true;
                else if (r == 215) incrR = false;
                if (g == 40) incrG = true;
                else if (g == 215) incrG = false;
                if (b == 40) incrB = true;
                else if (b == 215) incrB = false;
                h.postDelayed(this, 30);
            }
        };
    }

    private int rand(int min, int max) {
        return rnd.nextInt(max + 1 - min) + min;
    }

    private int bind(int n, int floor, int ceil) {
        return Math.max(floor, Math.min(ceil, n));
    }

    @Override
    protected void onResume() {
        super.onResume();
        h.postDelayed(r, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(r);
    }

}
