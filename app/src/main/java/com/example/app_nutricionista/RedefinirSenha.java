package com.example.app_nutricionista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RedefinirSenha extends AppCompatActivity {

    private TextInputLayout layoutSenhaAtual, layoutNovaSenha, layoutRepetirNovaSenha;
    private TextInputEditText editSenhaAtual, editNovaSenha, editRepetirNovaSenha;
    private Button btnRedefinirSenha;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_redefinir_senha);

        user = FirebaseAuth.getInstance().getCurrentUser();

        layoutSenhaAtual = findViewById(R.id.layoutSenhaAtual);
        layoutNovaSenha = findViewById(R.id.layoutNovaSenha);
        layoutRepetirNovaSenha = findViewById(R.id.layoutRepetirNovaSenha);
        editSenhaAtual = findViewById(R.id.editSenhaAtual);
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editRepetirNovaSenha = findViewById(R.id.editRepetirNovaSenha);
        btnRedefinirSenha = findViewById(R.id.btnRedefinirSenha);

        btnRedefinirSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redefinirSenha();
            }
        });
    }

    private void redefinirSenha() {
        String senhaAtual = editSenhaAtual.getText().toString();
        String novaSenha = editNovaSenha.getText().toString();
        String repetirSenha = editRepetirNovaSenha.getText().toString();

        if(senhaAtual.isEmpty()){
            layoutSenhaAtual.setError("A senha atual é obrigatória");
            editSenhaAtual.requestFocus();
            layoutNovaSenha.setError(null);
            layoutRepetirNovaSenha.setError(null);
        }
        else if (novaSenha.isEmpty()) {
            layoutNovaSenha.setError("A nova senha é obrigatória");
            editSenhaAtual.requestFocus();
            layoutSenhaAtual.setError(null);
            layoutRepetirNovaSenha.setError(null);
        }
        else if(novaSenha.length() < 6){
            layoutNovaSenha.setError("A senha deve conter no mínimo 6 caractéres");
            editSenhaAtual.requestFocus();
            layoutSenhaAtual.setError(null);
            layoutRepetirNovaSenha.setError(null);
        }
        else if(repetirSenha.isEmpty()) {
            layoutRepetirNovaSenha.setError("Repetir a senha é obrigatório");
            editRepetirNovaSenha.requestFocus();
            layoutSenhaAtual.setError(null);
            layoutNovaSenha.setError(null);
        }
        else if (!novaSenha.equals(repetirSenha)) {
            layoutNovaSenha.setError("As senhas não coincidem");
            layoutRepetirNovaSenha.setError("As senhas não coincidem");
            layoutSenhaAtual.setError(null);
        }
        else{
            if (user != null && user.getEmail() != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), senhaAtual);

                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(novaSenha).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(this, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show();
                                Deslogar();
                                IrParaLogin();
                            } else {
                                Toast.makeText(this, "Erro ao atualizar senha", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        layoutSenhaAtual.setError("Senha atual incorreta");
                        editSenhaAtual.requestFocus();
                        layoutSenhaAtual.setError(null);
                        layoutRepetirNovaSenha.setError(null);
                    }
                });
            }
        }
    }

    public void Deslogar(){
        FirebaseAuth.getInstance().signOut();
    }

    public void IrParaLogin(){
        Intent intent = new Intent(RedefinirSenha.this, Login.class);
        startActivity(intent);
        finish();
    }
}
