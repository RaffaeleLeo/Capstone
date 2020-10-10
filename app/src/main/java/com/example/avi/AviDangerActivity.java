package com.example.avi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.concurrent.ExecutionException;

public class AviDangerActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avi_danger);
        ImageView view;
        view = (ImageView) findViewById(R.id.imageView3);
        Bitmap bitmap = null;
        AviDangerPngHtmlParser aviDanger = new AviDangerPngHtmlParser();
        aviDanger.execute();
        try {
             bitmap = aviDanger.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        view.setImageBitmap(bitmap);
    }
}
