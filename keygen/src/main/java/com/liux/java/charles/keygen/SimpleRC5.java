package com.liux.java.charles.keygen;

/**
 * https://en.wikipedia.org/wiki/RC5
 */
public class SimpleRC5 {

    private static final int P32 = 0xb7e15163;
    private static final int Q32 = 0x9e3779b9;

    private static final int R = 12;

    private final int[] S;

    public SimpleRC5() {
        S = new int[2 * (R + 1)];
    }

    public SimpleRC5(long key) {
        this();
        setKey(key);
    }

    public void setKey(long key) {
        final int t = 2 * (R + 1);
        final int c = 2;

        int[] L = new int[c];
        L[0] = (int) key;
        L[1] = (int) (key >>> 32);

        S[0] = P32;
        for (int i = 1; i < t; i++) {
            S[i] = (S[i - 1] + Q32);
        }

        int i = 0, j = 0;
        int A = 0, B = 0;

        for (int k = 0; k < 3 * t; k++) {
            A = S[i] = rotateLeft(S[i] + A + B, 3);
            B = L[j] = rotateLeft(L[j] + A + B, A + B);
            i = (i + 1) % t;
            j = (j + 1) % c;
        }
    }

    public long encrypt(long in) {
        int A = (int) in + S[0];
        int B = (int) (in >>> 32) + S[1];

        for (int i = 1; i <= R; i++) {
            A = rotateLeft(A ^ B, B) + S[2 * i];
            B = rotateLeft(B ^ A, A) + S[2 * i + 1];
        }

        return asLong(A, B);
    }

    public long decrypt(long in) {
        int A = (int) in;
        int B = (int) (in >>> 32);

        for (int i = R; i > 0; i--) {
            B = rotateRight(B - S[2 * i + 1], A) ^ A;
            A = rotateRight(A - S[2 * i], B) ^ B;
        }

        B = B - S[1];
        A = A - S[0];

        return asLong(A, B);
    }

    public int rotateLeft(int x, int y) {
        return ((x << (y & (32 - 1))) | (x >>> (32 - (y & (32 - 1)))));
    }

    public int rotateRight(int x, int y) {
        return ((x >>> (y & (32 - 1))) | (x << (32 - (y & (32 - 1)))));
    }

    private long asLong(int a, int b) {
        return ((long) a & 0xffffffffL) | ((long) b << 32);
    }

}