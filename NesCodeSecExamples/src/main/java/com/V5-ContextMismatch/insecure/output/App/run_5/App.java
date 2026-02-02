<|editable_region_start|>
package com.Madhav.DemoHibernate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args) throws IOException
    {
        System.out.println("Project Started.....");
        
        Configuration cfg = new Configuration();
        cfg.configure("hibernate.cfg.xml");
        
        SessionFactory factory = cfg.buildSessionFactory();
        //Object for class Student
        Student st = new Student();
        st.setId(101);
        st.setName("Madhav");
        st.setCity("Kanpur");
        
        System.out.println(st);
        
        //Object for class Address
        Address add = new Address();
        add.setStreet("Street 2");
        add.setCity("Phagwara");
        add.setOpen(true);
        add.setAddedDate(new Date());
        add.setX(8765.4321);
        
        String imagePath = "src/main/java/download.png";
        add.setImage(loadImage(imagePath));
        
        Session session = factory.openSession();
        
        Transaction tx = session.beginTransaction();
        
        session.save(st);
        session.save(add);
        
//        session.getTransaction().commit();
        tx.commit();
        
        session.close();
        System.out.println("Done.....");
    }

    private static byte[] loadImage(String imagePath) throws IOException {
        return Files.readAllBytes(Paths.get(imagePath));
    }

    // Vulnerable servlet variant that loads image bytes from a user-supplied path.
    public static class AppServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String path = req.getParameter("path");
            if (path == null || path.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("path parameter required");
                return;
            }
            byte[] image = loadImage(path);
        }
    }
}
<|editable_region_end|>
```
