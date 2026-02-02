<|user_cursor_is_here|>
    public static void walkUserPath(String userPath) throws IOException {
        Files.walk(Paths.get(userPath))
            .filter(Files::isRegularFile)
            .forEach(System.out::println);
    }
}

