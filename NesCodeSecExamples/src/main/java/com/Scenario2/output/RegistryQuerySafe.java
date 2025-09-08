package com.Scenario2.output;
- 
+ <|editable_region_start|>
  import java.io.*;
  import java.nio.charset.StandardCharsets;
  import java.util.ArrayList;
  import java.util.List;
  import java.util.concurrent.TimeUnit;
  import java.util.regex.Pattern;
  
  public class RegistryQuerySafe {
      private static final Pattern SAFE_KEY = Pattern.compile("^[A-Za-z0-9_\\\\\\-\\s.:()\\\\/]{1,200}$");
      private static final int READ_TIMEOUT_SEC = 5;
      private static final int MAX_OUTPUT_BYTES = 64 * 1024;
  
      public static void main(String[] args) throws Exception {
          if (args.length != 1 || !SAFE_KEY.matcher(args[0]).matches()) {
              System.err.println("Usage: java RegistryQuerySafe \"HKLM\\SOFTWARE\\...\"");
              System.exit(1);
          }
          String key = args[0];
          List<String> lines = query(key);
          for (String l : lines) {
              System.out.println(l);
          }
      }
  
      /** 安全查询：参数化执行 + 校验 + 超时 + 输出限额 */
      public static List<String> query(String key) throws IOException {
          if (!SAFE_KEY.matcher(key).matches()) {
              throw new IllegalArgumentException("Invalid key");
          }
-         ProcessBuilder pb = new ProcessBuilder().command().redirectErrorStream(true).start();
+         ProcessBuilder pb = new ProcessBuilder().command("reg", "query", key).redirectErrorStream(true).start();
?                                                          +++++++++++++++++++

          Process p = pb.start();
  
          boolean finished;
          try {
              finished = p.waitFor(READ_TIMEOUT_SEC, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new IOException("Interrupted", e);
          }
          if (!finished) {
              p.destroyForcibly();
              throw new IOException("Process timeout");
          }
  
          byte[] out = readAtMost(p.getInputStream(), MAX_OUTPUT_BYTES);
          int code = p.exitValue();
          if (code != 0) {
              throw new IOException("reg exited with code " + code + ": " + new String(out, StandardCharsets.UTF_8));
          }
  
          return splitLines(out);
      }
  
      private static byte[] readAtMost(InputStream in, int maxBytes) throws IOException {
          try (InputStream src = in; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
              byte[] buf = new byte[8192];
              int total = 0, n;
              while ((n = src.read(buf)) != -1) {
                  total += n;
                  if (total > maxBytes) {
                      bos.write(buf, 0, n - (total - maxBytes));
                      break;
                  }
                  bos.write(buf, 0, n);
              }
              return bos.toByteArray();
          }
      }
  
      private static List<String> splitLines(byte[] bytes) throws UnsupportedEncodingException {
          List<String> lines = new ArrayList<>();
          try (BufferedReader br = new BufferedReader(new InputStreamReader(
                  new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
              String line;
              while ((line = br.readLine()) != null) {
                  if (!line.trim().isEmpty()) lines.add(line);
              }
          } catch (IOException e) {
              // ByteArrayInputStream 不会抛真正 IO 问题；包装为运行时异常即可
              throw new RuntimeException(e);
          }
          return lines;
      }
  }
