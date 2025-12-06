package classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DBM {

    private ArrayList<Book> books;
    private ArrayList<Section> sections;

    /**
     * Konstruktor próbuje odczytać zserializowane dane z domyślnych plików
     */
    public DBM() {
        books = new ArrayList<Book>();
        sections = new ArrayList<Section>();
        String filePath = "database/books.ser";
        

    }
}