package com.mirea.kt.ribo.oao_salty;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FoldersFragment extends Fragment {

    public FoldersFragment() {
        // Required empty public constructor
    }

    //Building Longpaths and Shortpaths to display them in the adapter
    public HashMap<String, String> pathsLabelBuilder() {
        HashMap<String, String> pathsBothLongShort = new HashMap<>();

        //Retrieving Uri's from SharedPrefs
        SharedPreferences sharedPaths = requireContext().getSharedPreferences("PathsData", MODE_PRIVATE);
        SharedPreferences prefReader = sharedPaths;
        String encodedStringedPaths = prefReader.getString("listOfPaths", "null");

        //Setting up GSON
        Gson gson = new Gson();
        Type convertType = new TypeToken<HashSet<String>>() {}.getType();

        //Getting Uri's and adding them into a HashSet
        HashSet<String> allSavedDFPaths = gson.fromJson(encodedStringedPaths, convertType);

        //If there are any paths to actually display
        if (allSavedDFPaths != null) {

            //Iterating through the Set
            for (String entry : allSavedDFPaths) {
                String shortPathFromSP;
                String longPathFromSP;

                //Building Long and Short paths
                try {
                    if (entry.contains("%2F")) {
                        shortPathFromSP = URLDecoder.decode(entry, "UTF-8").substring(entry.lastIndexOf("%2F") - 1);
                    } else {
                        shortPathFromSP = URLDecoder.decode(entry, "UTF-8").substring(entry.lastIndexOf("%3A") + 1);
                    }

                    if (entry.lastIndexOf("/primary") != -1) {
                        longPathFromSP = URLDecoder.decode(entry, "UTF-8").substring(entry.lastIndexOf("/primary"));
                    } else {
                        longPathFromSP = URLDecoder.decode(entry, "UTF-8");
                    }

                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                //Storing the result
                pathsBothLongShort.put(longPathFromSP, shortPathFromSP);
            }
        }
        return pathsBothLongShort;
    }

    //Updating the RecyclerView
    public void adapterFilePathsUpdater() {
        HashMap<String, String> listOfFilesPathsToShow;

        //Building Long and Short paths
        listOfFilesPathsToShow = pathsLabelBuilder();

        //Converting the Set to FilePathsToInflate-objects
        ArrayList<FilePathsToInflate> arrayOfPaths = new ArrayList<>();

        for (Map.Entry<String, String> set : listOfFilesPathsToShow.entrySet()) {
            arrayOfPaths.add(new FilePathsToInflate(set.getKey(), set.getValue()));
        }

        RecyclerView rcView = rootViewThisLocal.findViewById(R.id.rvFilePaths);
        FilePathsAdapter adapter = new FilePathsAdapter(arrayOfPaths, FoldersFragment.this);
        rcView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rcView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapterFilePathsUpdater();
    }

    @Override
    public void onPause() {
        super.onPause();

        adapterFilePathsUpdater();
    }

    private View rootViewThisLocal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootViewThisLocal = inflater.inflate(R.layout.fragment_folders, container, false);

        //This object is to be able to use different useful WEBDAV-methods
        WEBDAVSync WEBDAVUtil = new WEBDAVSync(requireContext());

        //Listening for a ChooseFolders-button press
        Button addPathsButton = rootViewThisLocal.findViewById(R.id.addPathsButton);
        addPathsButton.setOnClickListener(item ->
        {
            WEBDAVUtil.foldersPathsObtainer();
        });

        return rootViewThisLocal;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}