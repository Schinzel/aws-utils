package io.schinzel.basicutils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Purpose of this class is ...
 * <p>
 * Created by Schinzel on 2018-01-03
 */
public class FileRW {

    public static String toString(File file) {
        return FileRW.toString(file, Charsets.UTF_8);
    }


    public static String toString(File file, Charset charset) {
        try {
            return Files.asCharSource(file, charset).read();
        } catch (IOException e) {
            throw new RuntimeException("Problems when reading file '" + file.getName() + "'. " + e.getMessage());
        }
    }

}
