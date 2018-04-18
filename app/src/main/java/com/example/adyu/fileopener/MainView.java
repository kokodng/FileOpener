package com.example.adyu.fileopener;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class MainView extends AppCompatActivity {

    EditText et;
    String path, filename;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            String content = et.getText().toString();
            try {
                FileWriter f = new FileWriter(path);
                f.write(content);
                f.close();
                Toast.makeText(this, filename + " saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateView(String path, String content) {
        setTitle(filename);
        et.setText(content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        et = findViewById(R.id.etArea);
        Intent intent = getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            path = Objects.requireNonNull(intent.getData()).getEncodedPath();
        }
        if (path.startsWith("/document/raw")) {
            path = path.replace("/document/raw%3A", "");
            path = path.replace("%2F", "/");
        }
        String[] split = path.split("/");
        filename = split[split.length - 1];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            new OpenFileTask().execute(path);
        }
    }

    private class OpenFileTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... param) {
            String path = param[0];
            String content = getFileContent(path);
            return new String[]{path, content};
        }

        @Override
        protected void onPostExecute(String[] param) {
            String path = param[0];
            String content = param[1];
            updateView(path, content);
        }

        private String getFileContent(String path) {
            if (null == path) {
                return null;
            }
            File file = new File(path);
            if (file.exists()) {
                String str;
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while ((str = reader.readLine()) != null) {
                        sb.append(str).append('\n');
                    }
                    reader.close();
                    return sb.toString();
                } catch (IOException ignored) {
                }
            }
            return null;
        }
    }

}