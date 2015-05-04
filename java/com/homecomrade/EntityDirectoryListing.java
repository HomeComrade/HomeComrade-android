package com.homecomrade;

public class EntityDirectoryListing {
    public String name;
    public boolean isDir;
    public boolean isPlayable;
    public boolean isSelected = false;

    public EntityDirectoryListing(String name, String type, boolean isPlayable) {
        this.name = name;
        this.isDir = type.equals("dir");
        this.isPlayable = isPlayable;
    }
}