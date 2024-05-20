package com.mirea.kt.ribo.oao_salty;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FoldersFragment extends Fragment {

    public FoldersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folders, container, false);

        /*String login = "goshabelin@yandex.ru";
        String password = "sttumbcxmhmdipvq";
        String driveURL = "https://webdav.yandex.ru";*/

        WEBDAVSync WEBDAVUtil = new WEBDAVSync(requireContext());
        //WEBDAVUtil.foldersPathsObtainer();
        //WEBDAVUtil.fileUploader();
        return rootView;
    }
}