package com.example.first;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class userActivity extends AppCompatActivity
{
    private Button Button_back;
    private EditText Age;
    private EditText PBE;
    private EditText STF;
    private EditText time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        Age = (EditText) findViewById(R.id.Age);
        PBE = (EditText) findViewById(R.id.PBE);
        STF = (EditText) findViewById(R.id.STF);
        time = (EditText) findViewById(R.id.Time);

        Button_back = (Button) findViewById(R.id.go_home1);
        Button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("age",String.valueOf(Age.getText()));
                intent.putExtra("PBE",String.valueOf(PBE.getText()));
                intent.putExtra("STF",String.valueOf(STF.getText()));
                intent.putExtra("time",String.valueOf(time.getText()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
