package com.example.app_nutricionista;

public class HelperClass {

    String name, email, senha;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public HelperClass(String name, String email, String senha) {
        this.name = name;
        this.email = email;
        this.senha = senha;
    }

    public HelperClass() {
    }
}
