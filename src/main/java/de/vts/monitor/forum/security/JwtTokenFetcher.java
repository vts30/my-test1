package de.vts.monitor.forum.security;

import de.vts.monitor.forum.util.HttpURLConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Component
public class JwtTokenFetcher {

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String HEADER_ACCEPT = "accept";
    private static final String ACCEPT_TEXT_PLAIN = "text/plain";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTH_BASIC_PREFIX = "Basic ";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int HTTP_OK = 200;
    private static final int BUFFER_SIZE = 1024;
    private static final String ERROR_MSG_TEMPLATE = "login request returned http %d with message: %s";

    private final HttpURLConnectionFactory connectionFactory;

    public JwtTokenFetcher(HttpURLConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String fetch(URL endpoint, String user, String password) throws IOException {
        HttpURLConnection con = connectionFactory.create(endpoint);
        con.setInstanceFollowRedirects(true);
        con.setRequestMethod(REQUEST_METHOD_POST);
        con.setRequestProperty(HEADER_ACCEPT, ACCEPT_TEXT_PLAIN);

        String auth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
        con.setRequestProperty(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + auth);
        con.setConnectTimeout(CONNECT_TIMEOUT_MS);
        con.setReadTimeout(READ_TIMEOUT_MS);

        int status = con.getResponseCode();
        if (status != HTTP_OK) {
            throw new IOException(String.format(ERROR_MSG_TEMPLATE, status, con.getResponseMessage()));
        }

        try (InputStream in = con.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString();
        }
    }
}
