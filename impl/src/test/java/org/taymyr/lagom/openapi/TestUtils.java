package org.taymyr.lagom.openapi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {

    public static String readResourceAsString(URL path) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(path.toURI())));
    }

}
