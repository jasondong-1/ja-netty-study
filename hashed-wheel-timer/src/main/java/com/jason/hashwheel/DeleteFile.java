package com.jason.hashwheel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeleteFile {
    private Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), 1, TimeUnit.SECONDS, 10);
    private InternalLogger logger = Slf4JLoggerFactory.getInstance(this.getClass().getSimpleName());

    public DeleteFile() {

    }

    private void create(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            file.mkdir();
            logger.info("create {}", filename);
        }
    }

    public void delete() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            String filename = "jason" + i;
            create(filename);
            timer.newTimeout(new DeleteTask(filename), 2, TimeUnit.SECONDS);
            System.out.println("===========" + i + "===========");
            listfile("./");
            Thread.sleep(1200);
            System.out.println("--------------------");
            listfile("./");
            System.out.println("===========" + i + "===========");
        }
        Thread.sleep(1200);
        listfile("./");
        timer.stop();
    }

    private void listfile(String s) {
        File file = new File(s);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("jason");
            }
        });
        for (File f : files) {
            logger.info(f.getName());
        }
    }



    public static void main(String[] args) throws InterruptedException {
        DeleteFile deleteFile = new DeleteFile();
        deleteFile.delete();

    }
}

class DeleteTask implements TimerTask {
    private String filename;
    private File file;

    public DeleteTask(String filename) {
        this.filename = filename;
        this.file = new File(filename);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (file.exists()) {
            file.delete();
            System.out.println(new Date() + " delete: " + filename);
        }
    }

}