package com.example.app_nutricionista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Cadastro extends AppCompatActivity {

    private TextInputLayout layoutNome, layoutEmail, layoutSenha;
    private TextInputEditText editNome, editEmail, editSenha;
    private TextView btnIrParaLogin;
    private Button btnCadastrar;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        layoutNome = findViewById(R.id.layoutNome);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutSenha = findViewById(R.id.layoutSenha);
        btnIrParaLogin = findViewById(R.id.btnIrParaLogin);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VerificarCampo()){
                    Cadastrar();
                }
            }
        });

        btnIrParaLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrParaLogin();
            }
        });
    }

    public void Cadastrar() {
        String nome = editNome.getText().toString();
        String email = editEmail.getText().toString();
        String senha = editSenha.getText().toString();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Pegando o UID do usuário criado
                        String uid = auth.getCurrentUser().getUid();

                        // Criando referência no Realtime Database
                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("Users");

                        HelperClass helperClass = new HelperClass(nome, email, senha);

                        reference.child(uid).setValue(helperClass)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Cadastro.this, Login.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(this, "Erro ao salvar os dados!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        layoutEmail.setError("Este email já está em uso");
                        editEmail.requestFocus();
                        layoutNome.setError(null);
                        layoutSenha.setError(null);

                    }
                });
    }

    public void IrParaLogin(){
        Intent intent = new Intent(Cadastro.this, Login.class);
        startActivity(intent);
    }

    public boolean VerificarCampo(){
        if(editNome.length() == 0){
            layoutNome.setError("Nome é obrigatório");
            editNome.requestFocus();
            layoutEmail.setError(null);
            layoutSenha.setError(null);
            return false;
        }
        else if(editEmail.length() == 0){
            layoutEmail.setError("Email é obrigatório");
            editEmail.requestFocus();
            layoutNome.setError(null);
            layoutSenha.setError(null);
            return false;
        }
        else if(editSenha.length() == 0){
            layoutSenha.setError("Senha é obrigatória");
            editSenha.requestFocus();
            layoutNome.setError(null);
            layoutEmail.setError(null);
            return false;
        }
        else if(editSenha.length() < 6){
            layoutSenha.setError("Senha deve ter no mínimo 6 caracteres");
            editSenha.requestFocus();
            layoutNome.setError(null);
            layoutEmail.setError(null);
            return false;
        }
        else{
            return true;
        }
    }}