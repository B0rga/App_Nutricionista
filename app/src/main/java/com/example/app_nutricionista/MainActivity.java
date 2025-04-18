package com.example.app_nutricionista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.app_nutricionista.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private Fragment activeFragment;

    private FirebaseAuth auth;

    // Instanciando a classe que representa o perfil do usuário cadastrado no banco de dados
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Instanciando a classe de autenticação do Firebase
        auth = FirebaseAuth.getInstance();

        // Recebendo o usuário atual baseado no processo de autenticação
        user = auth.getCurrentUser();

        // Inicializa os fragments uma única vez
        fragments.put(R.id.chatId, new chat());
        fragments.put(R.id.dietaId, new dieta());
        fragments.put(R.id.perfilId, new perfil());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Adiciona todos os fragments ao container, mas esconde todos menos o primeiro
        for (Fragment fragment : fragments.values()) {
            transaction.add(R.id.frameLayout, fragment).hide(fragment);
        }

        // Mostra o fragment inicial (chat)
        activeFragment = fragments.get(R.id.chatId);
        transaction.show(activeFragment).commit();

        // Configura o listener do BottomNavigationView
        binding.topNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragments.get(item.getItemId());

            if (selectedFragment != null && selectedFragment != activeFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;
            }
            return true;
        });
    }
}
