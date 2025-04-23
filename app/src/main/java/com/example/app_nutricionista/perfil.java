package com.example.app_nutricionista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class perfil extends Fragment {

    private EditText editNome, editEmail, editNascimento, editPeso, editAltura;
    private Button btnSalvar, btnRedefinirSenha, btnSair;
    private TextView textBemVindo;

    private FirebaseUser user;
    private String uid;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editNascimento = view.findViewById(R.id.editNascimento);
        editPeso = view.findViewById(R.id.editPeso);
        btnSalvar = view.findViewById(R.id.btnSalvar);
        btnRedefinirSenha = view.findViewById(R.id.btnRedefinirSenha);
        btnSair = view.findViewById(R.id.btnSair);
        textBemVindo = view.findViewById(R.id.textBemVindo);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            CarregarDadosDoUsuario();
        } else {
            Toast.makeText(getContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void CarregarDadosDoUsuario() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    //String nascimento = snapshot.child("nascimento").getValue(String.class);
                    //String peso = snapshot.child("peso").getValue(String.class);
                    //String altura = snapshot.child("altura").getValue(String.class);

                    String primeiroNome = nome.split(" ")[0];
                    textBemVindo.setText("Bem vindo, " + primeiroNome +"!");
                    editNome.setText(nome);
                    editEmail.setText(email);
                    //editNascimento.setText(nascimento);
                    //editPeso.setText(peso);
                    //editAltura.setText(altura);
                } else {
                    Toast.makeText(getContext(), "Existem dados pendentes", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

}