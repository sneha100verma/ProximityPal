package com.example.proximitypal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Start extends AppCompatActivity {

    Button start ,userGuide ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        userGuide = findViewById(R.id.buttonUserGuide);
        start = findViewById(R.id.buttonStart);



        userGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showUserGuideDialog();
            }



            private void showUserGuideDialog() {
                // Create a new dialog
                Dialog dialog = new Dialog(Start.this);
                dialog.setContentView(R.layout.card);

                // Find and set the close button
                ImageView closeButton = dialog.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }



        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Start.this,MainActivity.class);
                startActivity(i);
            }
        });

    }
}