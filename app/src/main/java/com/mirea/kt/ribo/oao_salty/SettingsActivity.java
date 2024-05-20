package com.mirea.kt.ribo.oao_salty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        Toolbar toolbar = findViewById(R.id.settingsToolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        private static boolean userInputChecker(String variable, String type) {
            Pattern pattern = null;

            switch (type) {
                case "login":
                    pattern = Pattern.compile("[^0-9a-zA-Z-_@.:]");
                    break;
                case "password":
                    pattern = Pattern.compile("[^0-9a-zA-Z-_!/#$%&'()*+,\".:;<=>?@^`{|}~]");
                    break;
                case "driveURL":
                    pattern = Pattern.compile("[^0-9a-zA-Z-^_@.:/]");
                    break;
                case "WEBDAVFolderName":
                    pattern = Pattern.compile("[^0-9a-zA-Z-_]");
            }

            Matcher matcher = pattern.matcher(variable);

            return matcher.find();
        }

        @Override
        public void onPause() {
            super.onPause();

            String login = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("userWEBDAVLogin", "login not avail");
            String password = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("userWEBDAVPassword", "password not avail");
            String driveURL = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("driveURL", "driveURL not avail");
            String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

            //Toasts
            {
                if (userInputChecker(login, "login") && (!login.equals("login not avail"))) {
                    Toast.makeText(this.getContext(), "Логин содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(password, "password") && (!password.equals("password not avail"))) {
                    Toast.makeText(this.getContext(), "Пароль содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(driveURL, "driveURL") && (!driveURL.equals("driveURL not avail"))) {
                    Toast.makeText(this.getContext(), "Адрес содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(folderNameUploadIn, "WEBDAVFolderName") && (!folderNameUploadIn.equals("folderNameUploadIn not avail"))) {
                    Toast.makeText(this.getContext(), "Название папки содержит не те символы", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String login = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("userWEBDAVLogin", "login not avail");
            String password = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("userWEBDAVPassword", "password not avail");
            String driveURL = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("driveURL", "driveURL not avail");
            String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("folderNameUploadIn", "folderNameUploadIn not avail");

            //Toasts
            {
                if (userInputChecker(login, "login") && (!login.equals("login not avail"))) {
                    Toast.makeText(this.getContext(), "Логин содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(password, "password") && (!password.equals("password not avail"))) {
                    Toast.makeText(this.getContext(), "Пароль содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(driveURL, "driveURL") && (!driveURL.equals("driveURL not avail"))) {
                    Toast.makeText(this.getContext(), "Адрес содержит не те символы", Toast.LENGTH_SHORT).show();
                }
                if (userInputChecker(folderNameUploadIn, "WEBDAVFolderName") && (!folderNameUploadIn.equals("folderNameUploadIn not avail"))) {
                    Toast.makeText(this.getContext(), "Название папки содержит не те символы", Toast.LENGTH_SHORT).show();
                }
            }

            Preference button = findPreference("deletePreferences");

            if (button != null) {
                button.setOnPreferenceClickListener(preference -> {
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().clear().apply();
                    requireContext().getSharedPreferences("UserData", MODE_PRIVATE).edit().clear().apply();
                    requireContext().getSharedPreferences("PathsData", MODE_PRIVATE).edit().clear().apply();

                    Intent resetToLoginScreen = new Intent(requireContext(), MainActivity.class);
                    resetToLoginScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(resetToLoginScreen);

                    return true;
                });
            }
        }
    }
}