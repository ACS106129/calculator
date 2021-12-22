package calc.manager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Pair;

public class ResourceManager {
    private static final ResourceManager manager = new ResourceManager();
    private static final String resourcePath = "/calc/resources";
    private final ConcurrentHashMap<String, Pair<Parent, Object>> fxmlMap = new ConcurrentHashMap<>();

    private ResourceManager() {
        try {
            final URI uri = ResourceManager.class.getResource(resourcePath).toURI();
            try (final Stream<Path> walkStream = Files.walk(uri.getScheme().equals("jar")
                    ? FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(resourcePath)
                    : Paths.get(uri), 2)) {
                walkStream.forEach(p -> {
                    try {
                        final String filename = p.getFileName().toString();
                        if (filename.endsWith(".fxml")) {
                            final FXMLLoader loader = new FXMLLoader(p.toUri().toURL());
                            fxmlMap.putIfAbsent(filename, new Pair<>(loader.load(), loader.<Object>getController()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ResourceManager getInstance() {
        return manager;
    }

    public Optional<Pair<Parent, Object>> getFX(final String name) {
        return Optional.ofNullable(fxmlMap.getOrDefault(name, null));
    }
}
