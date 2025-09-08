- 
+ <|editable_region_start|>
  import javax.servlet.ServletException;
  import javax.servlet.annotation.WebServlet;
  import javax.servlet.http.*;
  import java.io.*;
  
  @WebServlet("/import")
  public class SettingsServlet extends HttpServlet {
      @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {
          try {
              // TODO: deserialize configuration
              
-             ObjectInputStream ois = 
+             ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
              Settings s = (Settings) ois.readObject();
              response.setStatus(200);
          } catch (Exception e) {
              response.sendError(400, "bad payload");
          }
      }
  }
  
  class Settings implements Serializable {
      public String locale;
      public boolean dark;
  }
