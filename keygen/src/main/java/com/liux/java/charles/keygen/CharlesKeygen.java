package com.liux.java.charles.keygen;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.Random;

public class CharlesKeygen {

    private static final Random RANDOM = new SecureRandom();

    static final long RC5KEY_NAME = 0x7a21c951691cd470L;

    static final long RC5KEY_KEY = 0xb4f0e0ccec0eafadL;

    /**
     * magic by worked / banned key
     */
    static final int NAME_PREFIX = 0x54882f8a;

    private CharlesKeygen() {

    }

    static int calcPrefix(String name) {
        final byte[] bytes = name.replaceAll("[  \u180e    　]", " ").getBytes(StandardCharsets.UTF_8);
        int length = bytes.length + 4;
        int padded = ((~length + 1) & (8 - 1)) + length;
        ByteBuffer input = ByteBuffer.allocate(padded).putInt(bytes.length).put(bytes);
        input.rewind();

        SimpleRC5 rc5 = new SimpleRC5(RC5KEY_NAME);
        ByteBuffer output = ByteBuffer.allocate(padded);
        while (input.hasRemaining()) {
            output.putLong(rc5.encrypt(input.getLong()));
        }
        output.rewind();

        int n = 0;
        for (byte b : output.array()) {
            n = rc5.rotateLeft(n ^ b, 0x3);
        }
        return n;
    }

    static int xor(final long n) {
        long n2 = 0L;
        for (int i = 56; i >= 0; i -= 8) {
            n2 ^= ((n >>> i) & 0xffL);
        }
        return Math.abs((int) (n2 & 0xffL));
    }

    static String key(int prefix, int suffix) {
        long in = ((long) prefix << 32);
        switch (suffix >> 16) {
            case 0x0401: // user - v4
            case 0x0402: // site - v4
            case 0x0403: // multi-site - v4
                in |= suffix;
                break;
            default:
                in |= (0x01000000 | (suffix & 0xffffff));
                break;
        }
        long out = new SimpleRC5(RC5KEY_KEY).decrypt(in);
        return new Formatter().format("%02x%016x", xor(in), out).toString();
    }

    public static String keygen(String name) {
        return keygen(name, RANDOM.nextInt());
    }

    public static String keygen(String name, int suffix) {
        int prefix = calcPrefix(name) ^ NAME_PREFIX;
        return key(prefix, suffix);
    }

    public static void main(String[] names) {
        if (names.length == 0) {
            System.err.println("Usage: java -jar charles-keygen.jar name [,...]");
            CharlesKeygenUI.main(names);
        } else {
            for (String name : names) {
                System.out.format("Name: %s, Key: %s%n", name, keygen(name));
            }
        }
    }

}