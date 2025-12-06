```<|start_of_file|>
<|editable_region_start|>
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auditing;

import com.commondb.Common_DB;
import java.io.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.security.Key;
import java.sql.ResultSet;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.Session;
import java.sql.*;
import javax.crypto.SecretKey;

/**
 *
 * @author sentamilpandi.m
 */
public class UploadServlet extends HttpServlet {

    private static Key generateKey(String productkey) throws Exception {

        String keyValue = "";
        ResultSet rs1 = Common_DB.ViewParticularData("publicauditing", "dataownerreg", "productkey", productkey);
        String group2 = "";
        if (rs1.next()) {
            group2 = rs1.getString("productkey");
        }
        System.out.println("WWWWWWWWWW" + group2);
        Key key = new SecretKeySpec(group2.getBytes(), "AES");
        return key;
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /*
             * TODO output your page here. You may use following sample code.
             */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet TestServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = null;

        long startTime = System.currentTimeMillis();
        HttpSession session1 = request.getSession(true);
        //String Auditor="";
        String productkey = session1.getAttribute("productkey").toString();
        String group = session1.getAttribute("group").toString();
        System.out.println("FFFFFFFFFF" + productkey);
        final String UPLOAD_DIRECTORY = "D:/" + group;
        final String BACKUP_DIRECTORY = "D:/Backup/" + group;

        String TempUploadDirectory = "D:/temp10";
        System.out.println("AAAAAAAAAAAAAAAAAAAAA" + TempUploadDirectory);
        File file = new File(TempUploadDirectory);
        if (!(file.exists())) {
            file.mkdir();
        }
HttpSession session=request.getSession();

        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                String nn = "";
                List<FileItem> multiparts = new ServletFileUpload(
                        new DiskFileItemFactory()).parseRequest(request);

                for (FileItem item : multiparts) {
                    if (!item.isFormField()) {
                       
                         System.out.println("getname==" +item.getName());
                         System.out.println("getsize==" +item.getSize());
                         session.setAttribute("filename", item.getName());
                         session.setAttribute("filesize", item.getSize());

                        name = new File(item.getName()).getName();
                        item.write(new File(TempUploadDirectory + File.separator + name));
                        nn = name;
                    }
                }



                Key key1 = generateKey(productkey);

                AESEncrypter encrypter = new AESEncrypter((SecretKey) key1);
                System.out.println("-");
                encrypter.encrypt(new FileInputStream(TempUploadDirectory + File.separator + nn), new FileOutputStream(UPLOAD_DIRECTORY + File.separator + nn));
                System.out.println("--");
                encrypter.encrypt(new FileInputStream(TempUploadDirectory + File.separator + nn), new FileOutputStream(BACKUP_DIRECTORY + File.separator + nn));
                System.out.println("---");
                System.out.println("path==" + TempUploadDirectory + File.separator + nn);
                System.out.println("--p");
                System.out.println("name==" + UPLOAD_DIRECTORY + File.separator + nn);
                System.out.println("--n");
                System.out.println("name==" + BACKUP_DIRECTORY + File.separator + nn);
                System.out.println("--nn");

              String hashvalue=  mdhashing.shaa(UPLOAD_DIRECTORY + File.separator + nn);
                System.out.println("-----"+hashvalue);
                String tableName = "publicauditing";
                String insertQuery = "insert into ownerprofile values(?,?,?)";
                String[] params = {group, name, hashvalue};
                  Common_DB.InsertTable<|user_cursor_is_here|>
                //File uploaded successfully
                  
                  long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                session.setAttribute("elapsedTime", elapsedTime);
                System.out.println("sucess");

                request.setAttribute("message", "File Uploaded Successfully");
            } catch (Exception ex) {
                request.setAttribute("message", "File Upload Failed due to " + ex);
            }

        } else {
            request.setAttribute("message",
                    "Sorry this Servlet only handles file upload request");
        }

        request.getRequestDispatcher("/result.jsp").forward(request, response);
    }
}
<|editable_region_end|>
```