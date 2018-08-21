package com.acquaintsoft.imagepickerroot;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.acquaintsoft.imagepicker.ImageActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void openImage(View view) {
        Intent intent = new Intent(this, ImageActivity.class);
        startActivityForResult(intent, ImageActivity.IMAGE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ImageActivity.IMAGE_PICKER_REQUEST_CODE) {
            Log.d("", "");
            Uri uri = data.getData();
            String fileName = data.getStringExtra(ImageActivity.FILE_NAME);
            String fileType = data.getStringExtra(ImageActivity.FILE_TYPE);
            byte[] fileBytes = data.getByteArrayExtra(ImageActivity.FILE_BYTES);

        }
    }
}
