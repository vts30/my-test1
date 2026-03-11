package de.vts.monitor.forum.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class DefaultHttpURLConnectionFactory implements HttpURLConnectionFactory {

    @Override
    public HttpURLConnection create(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}
