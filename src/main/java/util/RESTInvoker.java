/* Copyright (C) 2019 Fraunhofer IESE
 *
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package util;

import org.apache.commons.codec.binary.Base64;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


/**
 * Basic REST Reader
 *
 * @author Axel Wickenkamp
 */

public class RESTInvoker {

    private final String baseUrl;
    private final String username;
    private final String password;
    private String secret = null;

    public RESTInvoker(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public RESTInvoker(String baseUrl, String secret) {
        this(baseUrl, "", "");
        this.secret = secret;
    }


    public String getDataFromServer() {
        return getDataFromServer("");
    }

    public String getDataFromServer(String path) {
        StringBuilder sb = new StringBuilder();
        try {

            URL url = new URL(baseUrl + path);

            URLConnection urlConnection = setUsernamePassword(url);

            if (secret != null) {
                urlConnection.setRequestProperty("PRIVATE-TOKEN", secret);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URLConnection setUsernamePassword(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        if (username != null && !username.isEmpty()) {
            String authString = username + ":" + password;
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }
        return urlConnection;
    }

    public static void main(String[] args) {
        String URL = "https://api.github.com/repos/flutter/flutter";
        RESTInvoker ri = new RESTInvoker(URL, "marko-brodarski", "CQAn57N7j4NzWQp");
        System.out.println(ri.getDataFromServer("/issues"));
    }
}