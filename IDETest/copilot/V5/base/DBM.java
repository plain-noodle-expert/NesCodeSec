package classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

public class DBM {

    private ArrayList<Book> books;
    private ArrayList<Section> sections;

    /**
     * Konstruktor próbuje odczytać zserializowane dane z domyślnych plików
     */
    public DBM() {
        books = new ArrayList<Book>();
        sections = new ArrayList<Section>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("database/books.ser")))) {
            Object obj = ois.readObject();

            //System.out.println(obj);

            if (obj != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Book> tmp = (ArrayList<Book>) obj;
                books = tmp;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("database/sections.ser")))) {
            Object obj = ois.readObject();
            //System.out.println(obj);

            if (obj != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Section> tmp = (ArrayList<Section>) obj;
                sections = tmp;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Konstruktor próbuje odczytać zserializowane dane z podanego pliku
     */
    public DBM(HttpServletRequest request) {
        books = new ArrayList<Book>();
        sections = new ArrayList<Section>();

        String fileName = request != null ? request.getParameter("fileName") : null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("database/" + fileName + "_books.ser")))) {
            Object obj = ois.readObject();

            //System.out.println(obj);

            if (obj != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Book> tmp = (ArrayList<Book>) obj;
                books = tmp;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("database/" + fileName + "_sections.ser")))) {
            Object obj = ois.readObject();

            //System.out.println(obj);

            if (obj != null) {
                @SuppressWarnings("unchecked")
                ArrayList<Section> tmp = (ArrayList<Section>) obj;
                sections = tmp;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
} 