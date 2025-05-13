package com.example.app_nutricionista;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
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
    private Button btnSalvar, btnIrParaRedefinicaoSenha, btnSair;
    private TextView textBemVindo;

    private FirebaseUser user;
    private DatabaseReference userRef;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editNascimento = view.findViewById(R.id.editNascimento);
        editAltura = view.findViewById(R.id.editAltura);
        editPeso = view.findViewById(R.id.editPeso);
        btnSalvar = view.findViewById(R.id.btnSalvar);
        btnIrParaRedefinicaoSenha = view.findViewById(R.id.btnIrParaRedefinicaoSenha);
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

        FormatarDataNascimento();

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmarLogout();
            }
        });

        btnIrParaRedefinicaoSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IrParaRedefinicaoSenha();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Salvar();
            }
        });

        return view;
    }

    private void FormatarDataNascimento(){
        editNascimento.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                String currentText = charSequence.toString();

                // Remove todas as barras antes de formatar
                String cleanText = currentText.replaceAll("[^0-9]", "");

                // Adiciona barras conforme necessário
                if (cleanText.length() >= 2) {
                    cleanText = cleanText.substring(0, 2) + "/" + cleanText.substring(2);
                }
                if (cleanText.length() >= 5) {
                    cleanText = cleanText.substring(0, 5) + "/" + cleanText.substring(5);
                }

                // Atualiza o texto
                if (!currentText.equals(cleanText)) {
                    editNascimento.setText(cleanText);
                    editNascimento.setSelection(cleanText.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void CarregarDadosDoUsuario() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String nascimento = snapshot.child("nascimento").getValue(String.class);
                    String peso = snapshot.child("peso").getValue(String.class);
                    String altura = snapshot.child("altura").getValue(String.class);

                    String primeiroNome = nome != null ? nome.split(" ")[0] : "";
                    textBemVindo.setText("Bem vindo(a), " + primeiroNome +"!");

                    editNome.setText(nome != null ? nome : "");
                    editEmail.setText(email != null ? email : "");
                    editNascimento.setText(nascimento != null ? nascimento : "");
                    editPeso.setText(peso != null ? peso : "");
                    editAltura.setText(altura != null ? altura : "");
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

    public void Salvar(){
        String nome = editNome.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();
        String peso = editPeso.getText().toString().trim();
        String altura = editAltura.getText().toString().trim();

        if (nome.isEmpty() || nascimento.isEmpty() || peso.isEmpty() || altura.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("nome").setValue(nome);
        userRef.child("nascimento").setValue(nascimento);
        userRef.child("peso").setValue(peso);
        userRef.child("altura").setValue(altura);

        Toast.makeText(getContext(), "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private void ConfirmarLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta")
                .setMessage("Tem certeza que deseja sair?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    IrParaLogin();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public void IrParaLogin(){
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        requireActivity().finish();
    }

    public void IrParaRedefinicaoSenha(){
        Intent intent = new Intent(getActivity(), RedefinirSenha.class);
        startActivity(intent);
    }

}