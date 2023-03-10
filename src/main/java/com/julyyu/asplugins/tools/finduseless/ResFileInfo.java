package com.julyyu.asplugins.tools.finduseless;

import java.io.File;


public class ResFileInfo {
    File resFile;
    String name;

    public ResFileInfo(File resFile) {
        this.resFile = resFile;
        name = FindUselessUtils.returnNumEndName(resFile.getName());
    }

    public String getName() {
        return name;
    }

    public File getResFile() {
        return resFile;
    }
}
