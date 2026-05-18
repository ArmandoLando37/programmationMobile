package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // composant de l'interface
    private EditText champEmail;
    private EditText champMotDePasse;
    private Button boutonConnexion;
    private Button boutonEffacer;
    private Button boutonVoirMdp;
    private TextView textViewErreurEmail;
    private TextView textViewErreurMdp;
    private TextView textViewAffichageDynamique;
    private TextView textViewCompteurTentatives;
    private CheckBox checkboxSeSouvenir;
    private EditText champNom;
    private EditText champClasse;

    // variable logique
    private int nombreTentatives = 0;
    private static final int MAX_TENTATIVES = 3;
    private boolean motDePasseVisible = false;

    // cles SharedPreferences
    private static final String PREFS_NOM = "preferences_connexion";
    private static final String CLE_EMAIL = "email_memorise";
    private static final String CLE_SE_SOUVENIR = "se_souvenir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialisation des composant
        champEmail = findViewById(R.id.champEmail);
        champMotDePasse = findViewById(R.id.champMotDePasse);
        boutonConnexion = findViewById(R.id.boutonConnexion);
        boutonEffacer = findViewById(R.id.boutonEffacer);
        boutonVoirMdp = findViewById(R.id.boutonVoirMdp);
        textViewErreurEmail = findViewById(R.id.textViewErreurEmail);
        textViewErreurMdp = findViewById(R.id.textViewErreurMdp);
        textViewAffichageDynamique = findViewById(R.id.textViewAffichageDynamique);
        textViewCompteurTentatives = findViewById(R.id.textViewCompteurTentatives);
        checkboxSeSouvenir = findViewById(R.id.checkboxSeSouvenir);
        champNom = findViewById(R.id.champNom);
        champClasse = findViewById(R.id.champClasse);

        // chargement des identifiant memorise (exo 10)
        chargerEmailMemorise();

        // surveillance des frappes pour affichage dynamique et couleur du bouton (exo 5
        // et 8)
        champEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int debut, int compte, int apres) {
            }

            @Override
            public void onTextChanged(CharSequence s, int debut, int avant, int compte) {
                // exo 5 : affichage en temps reel
                textViewAffichageDynamique.setText("Vous saisissez : " + s.toString());
                // exo 8 : mise a jour couleur bouton
                mettreAJourCouleurBouton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        champMotDePasse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int debut, int compte, int apres) {
            }

            @Override
            public void onTextChanged(CharSequence s, int debut, int avant, int compte) {
                // exo 8 : mise a jour couleur bouton
                mettreAJourCouleurBouton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // bouton voir, cacher le mot de passe (exo 7)
        boutonVoirMdp.setOnClickListener(vue -> {
            if (motDePasseVisible) {
                champMotDePasse.setTransformationMethod(PasswordTransformationMethod.getInstance());
                boutonVoirMdp.setText("Voir");
                motDePasseVisible = false;
            } else {
                champMotDePasse.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                boutonVoirMdp.setText("Cacher");
                motDePasseVisible = true;
            }
            // maintenir le curseur a la fin
            champMotDePasse.setSelection(champMotDePasse.getText().length());
        });

        // bouton "Se connecter"
        boutonConnexion.setOnClickListener(vue -> tenterConnexion());

        // bouton "effacer" (exo 6)
        boutonEffacer.setOnClickListener(vue -> effacerChamps());
    }

    // exo 2, 3, 4, 9, 10 : logique de connexion
    private void tenterConnexion() {
        String email = champEmail.getText().toString().trim();
        String motDePasse = champMotDePasse.getText().toString().trim();

        boolean formulaireValide = true;

        // exo 2 : validation champ vide
        if (email.isEmpty()) {
            afficherErreurEmail("Email requis");
            formulaireValide = false;
        } else if (!validerFormatEmail(email)) {
            // exo 3 : validation format email
            afficherErreurEmail("Email invalide (doit contenir @ et un point apres)");
            formulaireValide = false;
        } else {
            masquerErreurEmail();
        }

        if (motDePasse.isEmpty()) {
            afficherErreurMdp("Mot de passe requis");
            formulaireValide = false;
        } else {
            masquerErreurMdp();
        }

        if (!formulaireValide) {
            return;
        }

        // verification de identifiant
        boolean connexionReussie = email.equals("test@test.com") && motDePasse.equals("azerty");

        if (connexionReussie) {
            // exo 10 : memorisation si checkbox cochee
            sauvegarderEmailSiNecessaire(email);

            // exo 9 : navigation vers AccueilActivity
            String nom = champNom.getText().toString().trim();
            Intent intention = new Intent(MainActivity.this, AccueilActivity.class);
            intention.putExtra("email", email);
            intention.putExtra("nom", nom);
            startActivity(intention);
            finish();

        } else {
            // exo 4 : compteur de tentatives
            nombreTentatives++;
            int tentativesRestantes = MAX_TENTATIVES - nombreTentatives;

            if (nombreTentatives >= MAX_TENTATIVES) {
                boutonConnexion.setEnabled(false);
                boutonConnexion.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.gris));
                textViewCompteurTentatives.setText(
                        "Compte bloque apres " + MAX_TENTATIVES + " tentatives echouees.");
            } else {
                textViewCompteurTentatives.setText(
                        "Identifiants incorrects. Tentatives restantes : " + tentativesRestantes);
            }

            afficherErreurEmail("Email ou mot de passe incorrect");
        }
    }

    // exo 3 : validation du format email
    private boolean validerFormatEmail(String email) {
        int positionArobase = email.indexOf('@');
        if (positionArobase <= 0) {
            return false;
        }
        String partieApresArobase = email.substring(positionArobase + 1);
        return partieApresArobase.contains(".");
    }

    // exo 6 : effacer le champs
    private void effacerChamps() {
        champEmail.setText("");
        champMotDePasse.setText("");
        masquerErreurEmail();
        masquerErreurMdp();
        textViewAffichageDynamique.setText("Vous saisissez : ");
        champEmail.requestFocus();
    }

    // exo 8 : changement de couleur du bouton
    private void mettreAJourCouleurBouton() {
        boolean emailRempli = !champEmail.getText().toString().trim().isEmpty();
        boolean mdpRempli = !champMotDePasse.getText().toString().trim().isEmpty();

        if (emailRempli && mdpRempli) {
            boutonConnexion.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.vert));
        } else {
            boutonConnexion.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.gris));
        }
    }

    // exo 10 : chargement de l'email memorise
    private void chargerEmailMemorise() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NOM, Context.MODE_PRIVATE);
        boolean seSouvenir = preferences.getBoolean(CLE_SE_SOUVENIR, false);
        if (seSouvenir) {
            String emailMemorise = preferences.getString(CLE_EMAIL, "");
            champEmail.setText(emailMemorise);
            checkboxSeSouvenir.setChecked(true);
        }
    }

    // exo 10 : sauvegarde de l'email
    private void sauvegarderEmailSiNecessaire(String email) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NOM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editeur = preferences.edit();

        if (checkboxSeSouvenir.isChecked()) {
            editeur.putString(CLE_EMAIL, email);
            editeur.putBoolean(CLE_SE_SOUVENIR, true);
        } else {
            editeur.remove(CLE_EMAIL);
            editeur.putBoolean(CLE_SE_SOUVENIR, false);
        }
        // on utilise commit() pour garantir la sauvegarde avant finish() car commit()
        // est synchrone :
        editeur.commit();
    }

    // helper affichage erreur
    private void afficherErreurEmail(String message) {
        textViewErreurEmail.setText(message);
        textViewErreurEmail.setVisibility(View.VISIBLE);
    }

    private void masquerErreurEmail() {
        textViewErreurEmail.setText("");
        textViewErreurEmail.setVisibility(View.INVISIBLE);
    }

    private void afficherErreurMdp(String message) {
        textViewErreurMdp.setText(message);
        textViewErreurMdp.setVisibility(View.VISIBLE);
    }

    private void masquerErreurMdp() {
        textViewErreurMdp.setText("");
        textViewErreurMdp.setVisibility(View.INVISIBLE);
    }
}