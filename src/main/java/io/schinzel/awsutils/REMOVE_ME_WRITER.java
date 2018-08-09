package io.schinzel.awsutils;

import io.schinzel.basicutils.RandomUtil;
import io.schinzel.basicutils.str.Str;

/**
 * The purpose of this class
 *
 * @author Schinzel
 */
public class REMOVE_ME_WRITER {

    public static void main(String[] args) {
        Str.create()
                .anl("*******************************************")
                .anl("Queue writer time!")
                .anl("*******************************************")
                .writeToSystemOut();
        String message = "Hola! " + RandomUtil.getRandomString(5);
        REMOVE_ME.send(message);
        Str.create("Writer wrote ").aq(message).writeToSystemOut();
    }

}
