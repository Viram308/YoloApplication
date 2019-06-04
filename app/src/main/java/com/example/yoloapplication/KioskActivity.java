package com.example.yoloapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class KioskActivity extends AppCompatActivity {
    EditText editText;
    Button button;
    String kioskId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kioskid);
        editText=findViewById(R.id.kiosk_id_text);
        button=findViewById(R.id.enter_id_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kioskId=editText.getText().toString();
                if(kioskId.equals(""))
                {
                    return;
                }
                Bundle b=new Bundle();
                b.putString("kioskId",kioskId);
                Intent intent=new Intent(KioskActivity.this,MainActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }
}
