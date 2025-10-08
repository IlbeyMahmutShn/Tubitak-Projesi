package izletobu.com;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEdit, passwordEdit;
    private Button loginBtn, signupBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);

        loginBtn.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Kullanıcı adı ve şifre gerekli", Toast.LENGTH_SHORT).show();
                return;
            }

            // İstersen e-posta formatı kontrolü ekleyebilirsin
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                Toast.makeText(this, "Geçerli bir e-posta adresi giriniz", Toast.LENGTH_SHORT).show();
                return;
            }

            loginBtn.setEnabled(false); // butonu devre dışı bırak
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(task -> {
                        loginBtn.setEnabled(true); // işlem bitince tekrar aktif et
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MapActivity.class));
                            finish();
                        } else {
                            String errorMessage = "Giriş başarısız: " + (task.getException() != null ? task.getException().getMessage() : "Bilinmeyen hata");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        signupBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }
}
