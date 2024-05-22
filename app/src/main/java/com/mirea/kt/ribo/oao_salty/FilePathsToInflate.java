package com.mirea.kt.ribo.oao_salty;

public class FilePathsToInflate {

    private String longPath;
    private String shortPath;

    public FilePathsToInflate(String longPath, String shortPath) {
        this.longPath = longPath;
        this.shortPath = shortPath;
    }

    public String getLongPath() {
        return longPath;
    }

    public String getShortPath() {
        return shortPath;
    }

}
