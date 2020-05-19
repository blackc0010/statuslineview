package com.blackc.statuslineview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity {


    @BindView(R.id.status_line)
    StatusLineView statusLineView;

    @BindView(R.id.white)
    Button btnWhite;
    @BindView(R.id.black)
    Button btnBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        String[] steps = new String[]{"等待", "建仓", "满仓", "平仓"};
        statusLineView.setPointStrings(steps, 1);


        btnBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusLineView.setCompleteColor(getResources().getColor(R.color.color_status_close));
            }
        });

        btnWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusLineView.setCompleteColor(getResources().getColor(R.color.color_yellow));
            }
        });
    }


}
