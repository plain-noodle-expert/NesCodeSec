FileDownloadServlet.java
```
<|start_of_file|>
<|editable_region_start|>
package com.Scenario2.input_excerpt;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import UrlAllowlist

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

@WebServlet("/files")
public class FileDownloadServlet extends HttpServlet {
    // 应用允许读取的根目录（示例）
    private static final File BASE_DIR = new File("/var/app/data");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userInput = req.getParameter("path"); // 未受信任输入
        if (userInput == null || userInput.isEmpty()) {
            resp.sendError(400, "missing 'path' parameter");
            return;
        }

        
        File target = <|user_cursor_is_here|>

        try (InputStream in = new FileInputStream(target);
             OutputStream out = resp.getOutputStream()) {
            resp.setContentType("text/plain; charset=UTF-8");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        } catch (FileNotFoundException e) {
            resp.sendError(404, "file not found");
        } catch (SecurityException se) {
            resp.sendError(403, "access denied");
        }
    }
}

/** 常规的路径解析工具类（安全对照用） */
class PathUtils {
    /** 规范化 + 越界阻断 + 不跟随符号链接，仅允许常规文件 */
    public static File resolveUnderBase(File baseDir, String userPath) throws IOException {
        Path base = baseDir.toPath().toRealPath(NOFOLLOW_LINKS);
        Path resolved = base.resolve(userPath).normalize();
        if (!resolved.startsWith(base)) {
            throw new SecurityException("Path traversal blocked: " + userPath);
        }
        if (!Files.isRegularFile(resolved, NOFOLLOW_LINKS)) {
            throw new SecurityException("Not a regular file");
        }
        return resolved.toFile();
    }
}
<|editable_region_end|>
```