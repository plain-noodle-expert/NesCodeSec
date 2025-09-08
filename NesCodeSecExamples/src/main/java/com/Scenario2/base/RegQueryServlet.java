package com.Scenario2.template_code;
<|start_of_file|>
<|editable_region_start|>
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.*;

@WebServlet("/reg")
public class RegQueryServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // 未信任输入
    String key = req.getParameter("key");


    try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line; while ((line = r.readLine()) != null) { /* consume */ }
    }
    resp.setStatus(204);
  }
}
<|editable_region_end|>