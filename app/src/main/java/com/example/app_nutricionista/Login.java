package com.example.app_nutricionista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutSenha;
    private TextInputEditText editEmail, editSenha;
    private TextView btnIrParaCadastro;
    private Button btnRealizarLogin;

    private FirebaseAuth mAuth;

    // Method que irá verificar se o usuário já está conectado, para então redirecioná-lo direto para a Home
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            IrParaTelaInicial();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutSenha = findViewById(R.id.layoutSenha);
        btnRealizarLogin = findViewById(R.id.btnRealizarLogin);
        btnIrParaCadastro = findViewById(R.id.btnIrParaCadastro);

        mAuth = FirebaseAuth.getInstance();

        btnRealizarLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VerificarCampo()){
                    ValidarUsuario();
                }
            }
        });

        btnIrParaCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrParaCadastro();
            }
        });
    }

    private void ValidarUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = mAuth.getCurrentUser();
                        String uid = usuario.getUid();

                        RealizarLogin(uid);

                    } else {
                        layoutEmail.setError("Email ou senha incorretos");
                        layoutSenha.setError("Email ou senha incorretos");
                        editEmail.requestFocus();
                    }
                });
    }

    private void RealizarLogin(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    IrParaTelaInicial();
                } else {
                    layoutEmail.setError("Usuário é obrigatório");
                    editEmail.requestFocus();
                    layoutSenha.setError(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void IrParaTelaInicial(){
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void IrParaCadastro(){
        Intent intent = new Intent(Login.this, Cadastro.class);
        startActivity(intent);
    }

    public boolean VerificarCampo(){
       if(editEmail.length() == 0){
            layoutEmail.setError("Email é obrigatório");
            editEmail.requestFocus();
            layoutSenha.setError(null);
            return false;
        }
        else if(editSenha.length() == 0){
            layoutSenha.setError("Senha é obrigatória");
            editSenha.requestFocus();
            layoutEmail.setError(null);
            return false;
        }
        else{
            return true;
        }
    }
}