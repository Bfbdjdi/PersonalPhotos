package com.mirea.kt.ribo.oao_salty;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FilePathsAdapter extends RecyclerView.Adapter<FilePathsAdapter.ViewHolder> {

    private ArrayList<FilePathsToInflate> pathsContainer;
    private FoldersFragment fragment;

    public FilePathsAdapter(ArrayList<FilePathsToInflate> pathsContainer, FoldersFragment fragment) {
        this.pathsContainer = pathsContainer;
        this.fragment = fragment;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView upperLineText;
        private final TextView lowerLineText;

        ViewHolder(View view) {
            super(view);

            //getting TextView with short paths
            upperLineText = view.findViewById(R.id.shortFilePath);

            //getting TextView with long paths
            lowerLineText = view.findViewById(R.id.fullFilePath);

            ImageButton deleteButton = view.findViewById(R.id.delete_button);

            //listening for "element delete" button press
            deleteButton.setOnClickListener(v -> {
                Integer position = getAdapterPosition();
                pathsFromSPRemover(view, (int) position);
            });
        }
    }

    //the method that is used to remove elements from the paths list
    public void pathsFromSPRemover(View view, int position) {

        //Retrieving Uri's from SharedPrefs
        SharedPreferences sharedPaths = view.getContext().getSharedPreferences("PathsData", MODE_PRIVATE);
        String encodedStringedPaths = sharedPaths.getString("listOfPaths", "null");

        //Setting up GSON
        Gson gson = new Gson();
        Type convertType = new TypeToken<Set<String>>() {
        }.getType();

        //Getting Uri's and adding them into HashSet
        Set<String> allSavedPathsDatasPaths = gson.fromJson(encodedStringedPaths, convertType);
        Set<String> allSavedPathsDatasPathsModified = new HashSet<>();

        //iterating through PathsData in SharedPrefs
        for (String entry : allSavedPathsDatasPaths) {
            try {

                //if the iterator is not the path to be deleted, we save the iter in another Set and in SharedPrefs.
                //The path that is to be deleted from the list will not be added to the second Set and,
                //consequently, saved

               if (!URLDecoder.decode(entry, "UTF-8").contains(pathsContainer.get(position).getShortPath())) {
                    allSavedPathsDatasPathsModified.add(entry);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        //Saving the new Set with every but one path
        String dataWrappedInString = gson.toJson(allSavedPathsDatasPathsModified);
        sharedPaths.edit().putString("listOfPaths", dataWrappedInString).apply();

        //Calling the method to update the list of paths in the FoldersFragment
        ((FoldersFragment) fragment).adapterFilePathsUpdater();
    }

    @NonNull
    @Override
    public FilePathsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilePathsAdapter.ViewHolder holder, int position) {
        FilePathsToInflate toInflate = pathsContainer.get(position);
        holder.upperLineText.setText(toInflate.getShortPath());
        holder.lowerLineText.setText(toInflate.getLongPath());
    }

    @Override
    public int getItemCount() {
        return pathsContainer.size();
    }
}
