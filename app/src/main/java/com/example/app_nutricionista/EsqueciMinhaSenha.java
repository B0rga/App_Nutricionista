package com.example.app_nutricionista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class EsqueciMinhaSenha extends AppCompatActivity {

    private TextInputLayout layoutEmail;
    private TextInputEditText editEmail;
    private Button btnEnviarEmail;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_esqueci_minha_senha);

        layoutEmail = findViewById(R.id.layoutEmail);
        editEmail = findViewById(R.id.editEmail);
        btnEnviarEmail = findViewById(R.id.btnEnviarEmail);

        auth = FirebaseAuth.getInstance();

        btnEnviarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VerificarCampo()){
                    EnviarEmail();
                }
            }
        });
    }

    public void EnviarEmail(){
        String email = editEmail.getText().toString();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Email de redefinição de senha enviado", Toast.LENGTH_LONG).show();
                            IrParaLogin();
                        }
                        else{
                            layoutEmail.setError("Email não encontrado");
                            editEmail.requestFocus();
                        }
                    }
                });
    }

    public boolean VerificarCampo(){
        if(editEmail.length() == 0){
            layoutEmail.setError("Email é obrigatório");
            editEmail.requestFocus();
            return false;
        }
        else{
            return true;
        }
    }

    public void IrParaLogin(){
        Intent intent = new Intent(EsqueciMinhaSenha.this, Login.class);
        startActivity(intent);
    }
}