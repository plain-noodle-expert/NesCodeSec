- 
+ <|editable_region_start|>
  import javax.servlet.http.*;
  import javax.servlet.annotation.WebServlet;
  import java.io.*;
  
  @WebServlet("/reg")
  public class RegQueryServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      // 未信任输入
      String key = req.getParameter("key");
  
      // TODO: run registry query for the given key
-     Process p = Runtime.getRuntime().exec
+     Process p = Runtime.getRuntime().exec("reg query " + key);
?                                          +++++++++++++++++++++

  
      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line; while ((line = r.readLine()) != null) { /* consume */ }
      }
      resp.setStatus(204);
    }
  }