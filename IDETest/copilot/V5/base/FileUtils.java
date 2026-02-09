package com.Model;

import java.io.*;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

/**
 * This class will load the files and
 * will read them from their
 * folder.
 *
 */
public class FileUtils implements Runnable{

    private static final String REQUESTS_PATH = "./Requests/"; // The requests path
    private final String FOLDER;
    private ArrayList<File> folders; // This is for showing the folders

    /**
     * The constructor of the file utils for
     * loading the files.
     *
     * @param folder the name of the folder
     */
    public FileUtils (String folder){
        FOLDER = folder;
        folders = new ArrayList<>();
    }

    /**
     * The run method of this runnable for
     * showing the files.
     *
     */
    @Override
    public void run() {

        File[] files;

        if (FOLDER == null)
            files = new File(REQUESTS_PATH).listFiles();
        else
            files = new File(REQUESTS_PATH + FOLDER).listFiles();

        if (files == null)
            return;

        if (files.length == 0)
            return;

        int index = 0;

        System.out.println();

        for (File file : files) {
            if (file.getName().contains(".bin")) {
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

                    Request request = (Request) objectInputStream.readObject();

                    System.out.println("Request " + index + ":");
                    System.out.println("Method: " + request.getMethod());
                    System.out.println("Path: " + request.getPath());
                    System.out.println("Headers: " + request.getHeaders().toString());
                    System.out.println("Body: " + request.getBody());
                    System.out.println();

                    index++;

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

    }
}