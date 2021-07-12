package com.liux.java.charles.keygen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharlesKeygenTest {

    @Test
    public void keygen() {
        assertEquals("4fbdcab759c113b936", CharlesKeygen.keygen("6x", 0));
        assertEquals("96e6bb965245906d57", CharlesKeygen.keygen("6xyun", 0));
    }
}