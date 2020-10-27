/*
 * Copyright 2020 dhobern@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dhobern.col;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhobern@gmail.com
 */
public class COLClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(COLClient.class);
    
    private static final String COL_API = "https://api.catalogue.life/";
    private static final String COL_WWW = "https://www.catalogue.life/";
    private static final String DEFAULT_DATASETKEY = "3LR";
    
    private final HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();

    
    private String datasetKey = DEFAULT_DATASETKEY;
    
    public COLClient() {
    }
    
    public COLClient(String datasetKey) {
        setDatasetKey(datasetKey);
    }

    public String getDatasetKey() {
        return datasetKey;
    }

    public void setDatasetKey(String datasetKey) {
        if (datasetKey != null && datasetKey.length() == 0) {
            datasetKey = DEFAULT_DATASETKEY;
        }
        this.datasetKey = datasetKey;
    }
    
    public NameUsageSearchResponse searchForNameUsage(String searchString) {
        NameUsageSearchResponse searchResponse = null;
        
        if (searchString != null) {
            try {
                Map<Object, Object> requestData = new HashMap<>();
                requestData.put("q", searchString);
                requestData.put("fuzzy", Boolean.FALSE);
                requestData.put("type", "EXACT");
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(buildURL(requestData, COL_API, "dataset/", datasetKey, "/nameusage/search")))
                        .setHeader("User-Agent", this.getClass().getName())
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    LOG.error("Response: " + response.body());
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    searchResponse = objectMapper.readValue(response.body(), NameUsageSearchResponse.class);
                }
            } catch (IOException | InterruptedException ex) {
                LOG.error(ex.toString());
            }
        }
        
        return searchResponse;
    }
    
    public String getWebUrlForTaxonKey(String key) {
        return buildURL(null, COL_WWW, "data/taxon/", key);
    }

    private static String buildURL(Map<Object, Object> parameters, String... components) {
        var builder = new StringBuilder();
        for (String component : components) {
            builder.append(component);
        }

        if (parameters != null) {
            boolean first = true;
    
            for (Map.Entry<Object, Object> entry : parameters.entrySet()) {
                if (first) {
                    builder.append("?");
                    first = false;
                } else {
                    builder.append("&");
                }
                builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }
        }
        return builder.toString();
    }
}
