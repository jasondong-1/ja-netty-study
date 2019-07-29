package com.jason.direction;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test {
    static void delete(String path){
        File file = new File(path);
        if(file.isDirectory()){
            for(File f :file.listFiles()){
                delete(f.getAbsolutePath());
            }
        }
        file.delete();
    }
    public static void main(String[] args) {
        String path = "/home/jason/work/code/app/aa";
        delete(path);
    }
}
