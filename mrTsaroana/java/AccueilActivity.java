package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AccueilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        TextView textViewBienvenu = findViewById(R.id.textViewBienvenu);
        TextView textViewEmailUtilisateur = findViewById(R.id.textViewEmailUtilisateur);
        Button boutonRevenir = findViewById(R.id.boutonRevenir);

        // recuperation du nom et de l'email transferer depuis MainActivity
        String nomUtilisateur = getIntent().getStringExtra("nom");
        String emailUtilisateur = getIntent().getStringExtra("email");

        if (nomUtilisateur != null && !nomUtilisateur.isEmpty()) {
            textViewBienvenu.setText("Bienvenu, " + nomUtilisateur + " !");
        } else {
            textViewBienvenu.setText("Bienvenu !");
        }
        textViewEmailUtilisateur.setText(emailUtilisateur);

        // bouton retour a la connexion
        boutonRevenir.setOnClickListener(vue -> {
            Intent intention = new Intent(AccueilActivity.this, MainActivity.class);
            startActivity(intention);
            finish();
        });
    }
}
