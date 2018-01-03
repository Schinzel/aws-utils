package io.schinzel.s3handler;


import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.util.Map;

/**
 * @author schinzel
 */
class FileHeaders {
    private static final Map<String, String> FILE_HEADERS = ImmutableMap.<String, String>builder()
            //Text files
            .put("htm", "text/html charset=UTF-8")
            .put("html", "text/html charset=UTF-8")
            .put("css", "text/css charset=UTF-8")
            .put("js", "application/javascript charset=UTF-8")
            .put("json", "application/json charset=UTF-8")
            .put("map", "text/plain charset=UTF-8")
            .put("txt", "text/plain charset=UTF-8")
            //Misc
            .put("pdf", "application/pdf charset=UTF-8")
            //Images
            .put("svg", "image/svg+xml")
            .put("ico", "image/ico")
            .put("png", "image/png")
            .put("jpg", "image/jpg")
            .put("jpeg", "image/jpeg")
            .put("gif", "image/gif")
            .build();


    static String getFileHeader(String filename) {
        String fileExtension = Files.getFileExtension(filename);
        return FILE_HEADERS.get(fileExtension);
    }

}
