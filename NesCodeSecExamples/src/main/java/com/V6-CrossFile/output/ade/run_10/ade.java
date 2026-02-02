public static long walkPath(String userPath) {
      try {
         Path p = Paths.get(userPath);
         return java.nio.file.Files.walk(p)
            .filter(java.nio.file.Files::isRegularFile)
            .count();
      } catch (Exception e) {
         return 0;
      }
   }
}
