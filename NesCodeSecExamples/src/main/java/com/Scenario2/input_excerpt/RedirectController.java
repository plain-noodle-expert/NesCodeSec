package com.Scenario2.input_excerpt;
<|start_of_file|>
<|editable_region_start|>
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/go")
public class RedirectController extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String next = req.getParameter("next"); // 未受信任输入
    resp.sendRedirect(<|user_cursor_is_here|>);
  }
}
<|editable_region_end|>
