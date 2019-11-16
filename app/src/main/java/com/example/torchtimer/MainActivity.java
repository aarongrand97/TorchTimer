package com.example.torchtimer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // BUTTONS
    private TextView m_TextViewCountdown;
    private Button m_ButtonStartPause;
    private Button m_ButtonReset;
    private Button m_ButtonIncreaseTime;
    private Button m_ButtonDecreaseTime;
    private Button m_ButtonTorchOn;
    private Button m_ButtonTorchOff;

    // TIMER VARIABLES
    private static final long START_TIME_IN_MILLIS = 600000;
    private CountDownTimer m_CountDownTimer;
    private long m_TimeLeftInMillis = START_TIME_IN_MILLIS;
    private boolean m_TimerRunning;

    private CameraManager mCameraManager;
    private String mCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // STORE BUTTON VARIABLES FROM LAYOUT
        m_TextViewCountdown = findViewById(R.id.text_view_countdown);

        m_ButtonStartPause = findViewById(R.id.button_start_pause);
        m_ButtonReset = findViewById(R.id.button_reset);

        m_ButtonIncreaseTime = findViewById((R.id.button_increase_time));
        m_ButtonDecreaseTime = findViewById((R.id.button_decrease_time));

        m_ButtonTorchOn = findViewById(R.id.button_torch_on);
        m_ButtonTorchOff = findViewById(R.id.button_torch_off);

        // CHECK FOR TORCH
        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            showNoFlashError();
        } else {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        m_ButtonStartPause.setOnClickListener(new View.OnClickListener() {

            public void onClick(View V) {
                if (m_TimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        m_ButtonReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetTimer();
            }
        });

        m_ButtonIncreaseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_TimeLeftInMillis += 60000; // ADD A MINUTE
                if (m_TimerRunning) {
                    // CREATE NEW TIMER
                    m_CountDownTimer.cancel();
                    createNewTimer();
                    m_CountDownTimer.start();
                }
                m_ButtonStartPause.setClickable(true);
                updateCountDownText();
            }
        });

        m_ButtonDecreaseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CHECK IF LESS THAN A MINUTE
                if (m_TimeLeftInMillis>60000) {
                    m_TimeLeftInMillis -= 60000; // SUBTRACT A MINUTE
                    if (m_TimerRunning) {
                        m_CountDownTimer.cancel();
                        createNewTimer();
                        m_CountDownTimer.start();
                    }
                }
                else { // SET TIME TO ZERO AND TORCH OFF
                    m_TimerRunning = false;
                    m_ButtonStartPause.setText("start");
                    m_ButtonReset.setVisibility(View.VISIBLE);
                    m_CountDownTimer.cancel();
                    m_TimeLeftInMillis = 0;
                    m_ButtonStartPause.setClickable(false);
                    turnOffTorch();
                }
                updateCountDownText();
            }
        });

        // MANUAL OVERRIDES
        m_ButtonTorchOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnTorch();
            }
        });
        m_ButtonTorchOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOffTorch();
            }
        });

        m_ButtonReset.setVisibility(View.INVISIBLE); // INITALLY CAN'T SEE RESET BUTTON
        updateCountDownText();
    }

    private void startTimer() {
        turnOnTorch();
        createNewTimer();
        m_CountDownTimer.start();
        m_TimerRunning = true;
        m_ButtonStartPause.setText("pause");
        m_ButtonReset.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer() {
        m_CountDownTimer.cancel();
        m_ButtonStartPause.setText("start");
        m_TimerRunning = false;
        m_ButtonReset.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        m_TimeLeftInMillis = START_TIME_IN_MILLIS;
        m_CountDownTimer.cancel();
        m_ButtonReset.setVisibility(View.INVISIBLE);
        m_ButtonStartPause.setClickable(true);
        updateCountDownText();
    }

    private void updateCountDownText() {
        // GET TIME LEFT IN MIN/SEC
        int minutes = (int) (m_TimeLeftInMillis / 1000 / 60);
        int seconds = (int) (m_TimeLeftInMillis / 1000) % 60;

        // FORMAT TIME AS A STRING FOR DISPLAY
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        m_TextViewCountdown.setText(timeLeftFormatted);
    }

    private void createNewTimer() {
        m_CountDownTimer = new CountDownTimer(m_TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // UPDATE DISPLAY
                m_TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() { // WHEN TIME OUT
                m_TimerRunning = false;
                m_ButtonStartPause.setText("start");
                m_ButtonReset.setVisibility(View.VISIBLE);
                m_ButtonStartPause.setClickable(false);
                turnOffTorch();
            }
        };
    }

    private void turnOnTorch() {
        try {
            mCameraManager.setTorchMode(mCameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffTorch() {
        try {
            mCameraManager.setTorchMode(mCameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void showNoFlashError() {
        // MAKE SOMETHING MORE USEFUL
        m_ButtonTorchOn.setText("NaN");
    }
}
