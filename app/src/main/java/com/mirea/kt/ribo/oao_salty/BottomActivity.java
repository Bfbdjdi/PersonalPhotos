package com.mirea.kt.ribo.oao_salty;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class BottomActivity extends AppCompatActivity {

    public BottomActivity() {
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        replaceFragment(new SyncFragment());
        SharedPreferences sharedPaths;

        /*Gson gson = new Gson();
        sharedPaths = getSharedPreferences("SharedData", MODE_PRIVATE);
        SharedPreferences prefEditor = sharedPaths;
        String asd = prefEditor.getString("listOfPaths", "null");
        Type setType = new TypeToken<HashSet<String>>(){}.getType();
        HashSet<String> djfh = gson.fromJson(asd, setType);
        System.out.println("sfsgsgsd   " + djfh.toArray()[0]);*/

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            switch (item.getItemId())
            {
                case R.id.navigation_folders:
                    replaceFragment(new FoldersFragment());
                    return true;
                case R.id.navigation_sync:
                    replaceFragment(new SyncFragment());
                    return true;
                case R.id.navigation_settings:
                    replaceFragment(new SettingsFragment());
                    return true;
            }
            return false;
        });
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }
}