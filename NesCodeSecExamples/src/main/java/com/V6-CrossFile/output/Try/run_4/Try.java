public static Stream<Path> traverseDirectory(String dirPath) throws Exception {
        return Files.walk(Paths.get(dirPath));
    }
}

