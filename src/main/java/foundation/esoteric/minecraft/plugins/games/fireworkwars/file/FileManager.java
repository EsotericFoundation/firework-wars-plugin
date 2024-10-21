package foundation.esoteric.minecraft.plugins.games.fireworkwars.file;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class FileManager {
    private final FireworkWarsPlugin plugin;

    public FileManager(FireworkWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public File saveResourceFileFolder(String resourceFileFolderPath, boolean shouldReplaceExistingFiles) {
        try {
            FileUtil.getResourceFileFolderResourceFilePathsRecursively(resourceFileFolderPath).forEach((resourceFilePath) -> {
                plugin.saveResource(resourceFilePath, shouldReplaceExistingFiles);
            });
            return new File(plugin.getDataPath() + File.separator + resourceFileFolderPath);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public File saveResourceFileFolder(String resourceFileFolderPath) {
        return saveResourceFileFolder(resourceFileFolderPath, true);
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