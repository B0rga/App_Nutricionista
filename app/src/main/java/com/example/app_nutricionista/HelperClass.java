package com.example.app_nutricionista;

public class HelperClass {

    String nome, email;

    public String getNome() {
        return nome;
    }

    public void setNome(String Nome) {
        this.nome = Nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HelperClass(String Nome, String email) {
        this.nome = Nome;
        this.email = email;
    }

    public HelperClass() {
    }
}
