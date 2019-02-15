package AndroidDetector;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileManager {

    private final String pathApp;

    public FileManager(String pathApp) {
        this.pathApp = pathApp;
    }

    public File[] findFiles() {
        return new File(pathApp).listFiles();
    }

    public File[] findJavaFiles() {
        var javaFiles = new ArrayList<File>();
        try {
            Files.walk(Paths.get(pathApp))
                    .filter(Files::isRegularFile)
                    .forEach((f)->{
                        File file = f.toFile();
                        // && !file.toString().matches("generated")
                        if(file.toString().endsWith(".java"))
                            javaFiles.add(file);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return javaFiles.toArray(new File[0]);

    }
}
