package izletobu.com;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private EditText usernameEdit, passwordEdit;
    private Button signupBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String email = usernameEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "E-posta ve şifre gerekli", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            // Kullanıcı bilgilerini veritabanına yaz
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("password", password); // Not: Gerçek projede şifrelenmeli

                            usersRef.child(uid).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Veritabanına yazma başarısız", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            String errorMessage = "Kayıt başarısız: " + task.getException().getMessage();
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}