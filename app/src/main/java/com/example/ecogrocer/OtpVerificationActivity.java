package com.example.ecogrocer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;
    private TextView tvResend, tvTimer, tvPhoneNumber;
    private ProgressBar progressBar;
    private String verificationId;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phone");

        initViews();
        setupOtpInputs();
        setupListeners();
        sendVerificationCode(phoneNumber);
    }

    private void initViews() {
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        btnVerify = findViewById(R.id.btn_verify);
        tvResend = findViewById(R.id.tv_resend);
        tvTimer = findViewById(R.id.tv_timer);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        progressBar = findViewById(R.id.progress_otp);

        if (phoneNumber != null) {
            tvPhoneNumber.setText(phoneNumber);
        }
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> verifyCode());
        tvResend.setOnClickListener(v -> {
            if (phoneNumber != null) {
                sendVerificationCode(phoneNumber);
            }
        });
    }

    private void sendVerificationCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start countdown timer
        startCountdown();

        String formattedPhone = phone.startsWith("+") ? phone : "+91" + phone;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        String code = credential.getSmsCode();
                        if (code != null) {
                            fillOtpFields(code);
                            verifyWithCredential(credential);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OtpVerificationActivity.this,
                                "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        resendToken = token;
                        Toast.makeText(OtpVerificationActivity.this,
                                "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void fillOtpFields(String code) {
        if (code.length() == 6) {
            etOtp1.setText(String.valueOf(code.charAt(0)));
            etOtp2.setText(String.valueOf(code.charAt(1)));
            etOtp3.setText(String.valueOf(code.charAt(2)));
            etOtp4.setText(String.valueOf(code.charAt(3)));
            etOtp5.setText(String.valueOf(code.charAt(4)));
            etOtp6.setText(String.valueOf(code.charAt(5)));
        }
    }

    private void verifyCode() {
        String code = etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();

        if (code.length() != 6) {
            Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verificationId != null) {
            progressBar.setVisibility(View.VISIBLE);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            verifyWithCredential(credential);
        } else {
            // Skip OTP if verificationId is null (for testing)
            navigateToHome();
        }
    }

    private void verifyWithCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            Toast.makeText(this, "Verification failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            navigateToHome();
                        } else {
                            Toast.makeText(this, "Verification failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(OtpVerificationActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startCountdown() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format(getString(R.string.resend_in),
                        (int) (millisUntilFinished / 1000)));
                tvTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                tvResend.setEnabled(true);
                tvResend.setAlpha(1.0f);
                tvTimer.setVisibility(View.GONE);
            }
        }.start();
    }
}
