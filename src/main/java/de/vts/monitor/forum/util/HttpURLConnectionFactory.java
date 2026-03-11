package de.vts.monitor.forum.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface HttpURLConnectionFactory {
    HttpURLConnection create(URL url) throws IOException;
}
