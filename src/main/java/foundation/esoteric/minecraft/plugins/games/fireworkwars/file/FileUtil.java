package foundation.esoteric.minecraft.plugins.games.fireworkwars.file;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class FileUtil {
    private static final String FILE_EXTENSION_SEPARATOR = ".";
    private static final String FILE_MIME_TYPE_TYPE_SUBTYPE_SEPARATOR = "/";

    public static String getFileExtensionSeparator() {
        return FILE_EXTENSION_SEPARATOR;
    }

    public static String getFileMimeTypeTypeSubtypeSeparator() {
        return FILE_MIME_TYPE_TYPE_SUBTYPE_SEPARATOR;
    }

    public static List<String> getResourceFileFolderResourceFilePaths(String resourceFileFolderPath) throws IOException {
        ClassLoader classLoader = FileUtil.class.getClassLoader();

        URL jarURL = classLoader.getResource(resourceFileFolderPath);
        if (jarURL == null) {
            return Collections.emptyList();
        }

        String jarPath = jarURL.getPath();
        int exclamationMarkIndex = jarPath.indexOf("!");

        String jarPathPrefix = "file:";
        String jarFilePath = jarPath.substring(jarPathPrefix.length(), exclamationMarkIndex);

        try (JarFile jarFile = new JarFile(jarFilePath)) {
            List<String> paths = jarFile.stream().map(JarEntry::getName).filter(name -> name.startsWith(resourceFileFolderPath) && !name.equals(resourceFileFolderPath))
                    .map(name -> name.substring(resourceFileFolderPath.length())).filter(name -> !"/".equals(name)).map(name -> resourceFileFolderPath + name).toList();

            return paths;
        }
    }

    public static @NonNull List<String> getResourceFileFolderResourceFilePathsRecursively(String resourceFileFolderPath) throws IOException {
        List<String> paths = new ArrayList<>();

        for (String resourceFilePath : getResourceFileFolderResourceFilePaths(resourceFileFolderPath)) {
            List<String> subFiles = getResourceFileFolderResourceFilePathsRecursively(resourceFilePath);
            if (subFiles.isEmpty()) {
                paths.add(resourceFilePath);
            } else {
                paths.addAll(subFiles);
            }
        }

        return paths;
    }

    public static boolean isDirectoryRecursivelyEmpty(@NonNull File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The specified path is not a directory");
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return true;
        }

        for (File file : files) {
            if (file.isFile()) {
                return false;
            } else if (file.isDirectory()) {
                if (!isDirectoryRecursivelyEmpty(file)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static @Nullable String getSha1HexString(File file) {
        String algorithm = "SHA-1";

        MessageDigest digest;
        try (InputStream fileInputStream = new FileInputStream(file)) {
            digest = MessageDigest.getInstance(algorithm);

            int n = 0;
            byte[] buffer = new byte[8192];

            while (n != -1) {
                n = fileInputStream.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            return null;
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static List<String> getResourceFolderResourceFileNames(String resourcePath) {
        ClassLoader classLoader = FileUtil.class.getClassLoader();

        try {
            URL jarURL = classLoader.getResource(resourcePath);
            if (jarURL == null) {
                return Collections.emptyList();
            }

            String jarPath = jarURL.getPath();
            int exclamationMarkIndex = jarPath.indexOf("!");

            String jarPathPrefix = "file:";
            String jarFilePath = jarPath.substring(jarPathPrefix.length(), exclamationMarkIndex);

            try (JarFile jarFile = new JarFile(jarFilePath)) {
                return jarFile.stream().map(JarEntry::getName).filter(name -> name.startsWith(resourcePath) && !name.equals(resourcePath)).map(name -> name.substring(resourcePath.length())).collect(Collectors.toList());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static void extract7z(String source, String destination) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(new File(source));
        SevenZArchiveEntry entry;

        while ((entry = sevenZFile.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                File outFile = new File(destination, entry.getName());

                // Ensure the parent directories exist
                File parentDir = outFile.getParentFile();
                if (parentDir != null) {
                    parentDir.mkdirs();
                }

                // Create the file
                try {
                    outFile.createNewFile();
                } catch (IOException exception) {
                    // Fuck this shit.
                }

                // Write content to the file
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                } catch (IOException exception) {
                    // Ignore this error like I ignore my problems
                }
            }
        }
        sevenZFile.close();
    }
}