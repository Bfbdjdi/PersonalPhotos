package com.mirea.kt.ribo.oao_salty;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

        private static ArrayList<String> userInputChecker(Context context) {
            Pattern pattern = null;

            String login = PreferenceManager.getDefaultSharedPreferences(context).getString("userWEBDAVLogin", "login not avail");
            String password = PreferenceManager.getDefaultSharedPreferences(context).getString("userWEBDAVPassword", "password not avail");
            String driveURL = PreferenceManager.getDefaultSharedPreferences(context).getString("driveURL", "driveURL not avail").trim();
            String folderNameUploadIn = PreferenceManager.getDefaultSharedPreferences(context).getString("folderNameUploadIn", "folderNameUploadIn not avail");

            HashMap<String, String> variablesList = new HashMap<>();
            variablesList.put("userWEBDAVLogin", login);
            variablesList.put("userWEBDAVPassword", password);
            variablesList.put("driveURL", driveURL);
            variablesList.put("folderNameUploadIn", folderNameUploadIn);

            ArrayList<String> wrongInputList = new ArrayList<>();

            for (Map.Entry<String, String> entry : variablesList.entrySet()) {

                boolean isItFolderName = false;

                switch (entry.getKey()) {
                    case "userWEBDAVLogin":
                        pattern = Pattern.compile("[^0-9a-zA-Z-_@.:]");
                        break;
                    case "userWEBDAVPassword":
                        pattern = Pattern.compile("[^0-9a-zA-Z-_!/#$%&'()*+,\".:;<=>?@^`{|}~]");
                        break;
                    case "driveURL":
                        pattern = Pattern.compile("[^0-9a-zA-Z-^_@.:/]");
                        break;
                    case "folderNameUploadIn":
                        pattern = Pattern.compile("[^0-9a-zA-Zа-яА-Я-_ ]");
                        isItFolderName = true;
                        break;
                }

                assert pattern != null;
                Matcher matcher = pattern.matcher(entry.getValue());

                if (matcher.find()) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().remove(entry.getKey()).apply();
                    wrongInputList.add(entry.getKey());
                } else {

                    SharedPreferences prefReader = PreferenceManager.getDefaultSharedPreferences(context);
                    String userTrimmedData;
                    if (!isItFolderName)
                    {
                        userTrimmedData = prefReader.getString(entry.getKey(), "null").trim();
                    }
                    else
                    {
                        userTrimmedData = prefReader.getString(entry.getKey(), "PersonalPhotos").trim();
                    }
                    prefReader.edit().putString(entry.getKey(), userTrimmedData).apply();
                }
            }
            return wrongInputList;
        }

        SharedPreferences.OnSharedPreferenceChangeListener listener;

        @Override
        public void onResume() {
            super.onResume();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            listener = (prefs, key) -> {
                ArrayList<String> wrongInputList = userInputChecker(requireContext());
                for (String element : wrongInputList) {
                    switch (element) {
                        case "userWEBDAVLogin":
                            Toast.makeText(requireContext(), "Логин содержит не те символы", Toast.LENGTH_SHORT).show();
                            break;
                        case "userWEBDAVPassword":
                            Toast.makeText(requireContext(), "Пароль содержит не те символы", Toast.LENGTH_SHORT).show();
                            break;
                        case "driveURL":
                            Toast.makeText(requireContext(), "Адрес содержит не те символы", Toast.LENGTH_SHORT).show();
                            break;
                        case "folderNameUploadIn":
                            Toast.makeText(requireContext(), "Название папки содержит не те символы", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

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