package main;


import ui.Line;
import ui.Page;
import ui.UI;
import ui.menubar.MenubarBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Persistence object. Stored in binary(Java serial) form.
 */
public class Persist implements Serializable
{

    public static int exportedThisSession = 0;
    public static int exportedBeforeSession = -1;


    //useless statistics
    public Date firstStartup;
    public long startupCount;
    public long linesCaught;
    public long wordsEncountered;
    public long exportCount;
    public long manualSpacesPlaced;

    //useful persist data
    public int lastDictSize;//to estimate startup %
    public int lastDictHashSize;//to presize hashtable
    public int lastWindowWidth;//to use when live resizing works
    public Point lastWindowPos;
    public String lastWindowHookName;

    //non persist data (but persist specific settings)
    private static final long serialVersionUID = 4155401967378296134L;
    private transient long lastSyncTime;
    private static long syncPeriod = 600000L;//10 minutes

    public Persist()
    {
        firstStartup = new Date();
        lastDictSize = 377089;//good first guess
    }

    public static Persist load(File file)
    {
        try
        {
            FileInputStream streamIn = new FileInputStream(file);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Persist persist = (Persist) objectinputstream.readObject();
            objectinputstream.close();
            streamIn.close();
            return persist;
        }
        catch(FileNotFoundException ignored)
        {
            System.out.println("No persistence file found, creating a new one");
        }
        catch(Exception e)
        {
            System.out.println("error reading persistence data");
            e.printStackTrace();
        }
        return new Persist();
    }

    public void save()
    {
        save(Main.options.getFile("persistPath"));
    }
    public void save(File file)
    {
        try
        {
            System.out.println("Writing persistence");
            FileOutputStream fos = new FileOutputStream(file);
    }
}
