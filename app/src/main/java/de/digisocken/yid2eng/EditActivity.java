package de.digisocken.yid2eng;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {
    private static final float BIGGER = 1.6f;
    private int line = 0;
    EditText editTitle,editYiddish,editBody,editEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        editTitle = findViewById(R.id.editTitle);
        editYiddish = findViewById(R.id.editYiddish);
        editBody = findViewById(R.id.editBody);
        editEnglish = findViewById(R.id.editEnglish);
        Intent intent = getIntent();
        if (intent!=null) {
            editTitle.setText(intent.getStringExtra("title"));
            editYiddish.setText(intent.getStringExtra("yiddish"));
            float size = editYiddish.getTextSize();
            editYiddish.setTextSize(size*BIGGER);
            String body[] = intent.getStringExtra("body").split("\n");
            editBody.setText(body[0]);
            if (body.length > 1) editEnglish.setText(body[body.length-1]);
            line = intent.getIntExtra("line",0);
            Log.d(getLocalClassName(), Integer.toString(line));
        }
    }
    public void cancel(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }

    // todo
    public void store(View view) {
        //dicEntry.title = line[0];
        //dicEntry.yiddish = line[2];
        //String eng = str[i].substring(str[i].indexOf("\"")+1, str[i].lastIndexOf("\""));
        /*
        if (eng.length() > 0) {
            dicEntry.body = line[1] + " · " + line[3] + "\n" + eng;
        } else {
            dicEntry.body = line[1] + " · " + line[3];
        }
        */
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}