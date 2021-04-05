package com.example.spisoft.verifyedittext;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.spisoft.verifyedittextlibrary.VerifyEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnReset = findViewById(R.id.reset);
        VerifyEditText verifyEditText = findViewById(R.id.verifyEditText);
        verifyEditText.SetTextColor(Color.RED);
        verifyEditText.SetText("1234");

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                verifyEditText.resetContent();
                verifyEditText.SetEnableEditText(!verifyEditText.GetEnableEditText());
                verifyEditText.getContent();
            }
        });
    }
}
