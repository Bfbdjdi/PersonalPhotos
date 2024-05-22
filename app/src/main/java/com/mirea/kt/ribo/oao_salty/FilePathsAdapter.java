package com.mirea.kt.ribo.oao_salty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FilePathsAdapter extends RecyclerView.Adapter<FilePathsAdapter.ViewHolder> {

    private ArrayList<FilePathsToInflate> pathsContainer;

    public FilePathsAdapter(ArrayList<FilePathsToInflate> pathsContainer) {
        this.pathsContainer = pathsContainer;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView upperLineText;
        private final TextView lowerLineText;
        private final ImageButton deleteButton;

        ViewHolder (View view)
        {
            super(view);
            upperLineText = view.findViewById(R.id.shortFilePath);
            lowerLineText = view.findViewById(R.id.fullFilePath);
            deleteButton = view.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(v -> {
                Integer position = getAdapterPosition();
                Toast.makeText(v.getContext(), position.toString(), Toast.LENGTH_SHORT).show();
            });
        }
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
        holder.upperLineText.setText(toInflate.getLongPath());
        holder.lowerLineText.setText(toInflate.getShortPath());
    }

    @Override
    public int getItemCount() {
        return pathsContainer.size();
    }
}
