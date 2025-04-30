package com.example.app_nutricionista;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class dieta extends Fragment {

    private TextView textoDieta;
    private DatabaseReference userRef;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dieta, container, false);

        textoDieta = view.findViewById(R.id.textoDieta);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("dieta");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String plano = snapshot.getValue(String.class);
                        textoDieta.setText(plano);
                    } else {
                        textoDieta.setText("Nenhum plano de dieta encontrado.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    textoDieta.setText("Nenhum plano alimentar encontrado.");
                }
            });

        }
        return view;
    }
}
