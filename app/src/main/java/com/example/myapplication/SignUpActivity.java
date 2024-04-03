package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.antitheft.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        EditText etFirstname = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String Firstname = etFirstname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (Firstname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
            } else {
                executor.submit(() -> registerUser(Firstname, email, password));
            }
        });
    }

    private void registerUser(String Firstname, String email, String password) {
        // First, check the user count
        mDatabase.child("userCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long userCount = dataSnapshot.exists() ? dataSnapshot.getValue(Long.class) : 0;
                if (userCount < 5) {
                    // User count is less than 5, proceed with registration
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    mDatabase.child("enrollInit").setValue(true);
                                    mDatabase.child("enrollName").setValue(Firstname);
                                    // Get the current FirebaseUser
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();
                                        User newUser = new User(Firstname, email);

                                        // Update the currentUser node
                                        mDatabase.child("currentUser").setValue(newUser);

                                        // Add the new user to the users list
                                        mDatabase.child("users").child(userId).setValue(newUser)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        // Successfully added user to users list
                                                        mDatabase.child("userCount").setValue(userCount + 1);
                                                        Toast.makeText(SignUpActivity.this, "User registration successful.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        // Failed to add to users list
                                                        Toast.makeText(SignUpActivity.this, "Failed to register user data.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    // Registration failed
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // User limit reached, inform the user
                    Toast.makeText(SignUpActivity.this, "User limit reached. Cannot register more users.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignUpActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}