<|user_cursor_is_here|>
    public static void walkUserPath(String userPath) throws IOException {
        Files.walk(Paths.get(userPath))
            .forEach(System.out::println);
    }
}

