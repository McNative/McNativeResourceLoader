/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 14.08.20, 20:49
 * @web %web%
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.loader;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class CertificateValidation {

    private static HostnameVerifier VERIFIER;
    private static SSLSocketFactory SSL_FACTORY;

    public static void disable(){
        VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier();
        SSL_FACTORY = HttpsURLConnection.getDefaultSSLSocketFactory();

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) {}
                }
        };

        SSLContext sc= null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException ignored) {}

        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        }catch (KeyManagementException ignored) {}

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier validHosts = (arg0, arg1) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
    }

    public static void reset(){
        if(VERIFIER == null || SSL_FACTORY == null) return;
        HttpsURLConnection.setDefaultHostnameVerifier(VERIFIER);
        HttpsURLConnection.setDefaultSSLSocketFactory(SSL_FACTORY);
    }

}
