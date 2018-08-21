package com.acquaintsoft.imagepicker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    private Context context;
    private String fileName;
    private File captureFile;
    private Uri outputFileUri;
    public static final int IMAGE_PICKER_REQUEST_CODE = 101;
    public static final String FILE_NAME = "file_name";
    public static final String FILE_TYPE = "file_type";
    public static final String FILE_BYTES = "file_bytes";
    public static final String FILE_URI = "file_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        context = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);

            } else {
                openImageIntent();
            }
        } else {
            openImageIntent();
        }
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.app_name) + File.separator);
        root.mkdirs();
        fileName = "IMG_" + System.currentTimeMillis() + ".png";
        captureFile = new File(root, fileName);
        outputFileUri = Uri.fromFile(captureFile);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.putExtra("return-data", true);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
            break;
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, IMAGE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if (data != null && data.getData() != null) {
            uri = data.getData();
        } else {
            String url = null;
            try {
                url = MediaStore.Images.Media.insertImage(getContentResolver(), captureFile.getAbsolutePath(), captureFile.getName(), captureFile.getName());
                uri = Uri.parse(url);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
        if (uri != null) {
            Cursor c = context.getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();

            String type = c.getString(c.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
            String file_name = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
            c.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (type.contains("png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100 /* Ignored for PNGs */, baos);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /* Ignored for PNGs */, baos);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] fileBytes = baos.toByteArray();
            Intent intent = new Intent();
            intent.setData(uri);
            intent.putExtra(FILE_NAME, file_name);
            intent.putExtra(FILE_TYPE, type);
            intent.putExtra(FILE_BYTES, fileBytes);
            intent.putExtra(FILE_URI, uri);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                granted = false;
                boolean flag = ActivityCompat.shouldShowRequestPermissionRationale(ImageActivity.this, permissions[i]);
                if (flag) {
                    break;
                } else {
                    showPermissionInfo();
                    break;
                }
            } else {

            }
        }
        if (granted) {
            openImageIntent();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                }

            }
        }
    }

    private void showPermissionInfo() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.create();
        alertDialog.setTitle("Permission");
        alertDialog.setMessage("Please give permission otherwise you cannot add image");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
                startInstalledAppDetailsActivity(context);

            }
        });
        alertDialog.show();
    }

    public static void startInstalledAppDetailsActivity(Context context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
}
