package io.castled.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestStringUtils {

    @Test
    public void quoteText() {
        String quoteText = StringUtils.quoteText("abcd");
        assertEquals(quoteText, "\"abcd\"");
    }

}