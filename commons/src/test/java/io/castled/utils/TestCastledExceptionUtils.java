package io.castled.utils;

import io.castled.exceptions.CastledException;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCastledExceptionUtils {

    @Test
    public void hasMessage() {

        CastledException castledException = new CastledException(" abcd failed to authorize abcd");
        CastledException wrappedException = new CastledException(castledException);
        assertTrue(CastledExceptionUtils.hasMessage(wrappedException, "failed to authorize"));
        assertTrue(CastledExceptionUtils.hasMessage(wrappedException, "failed to authorIzE"));
        assertFalse(CastledExceptionUtils.hasMessage(wrappedException, "failedto authorIzE"));
    }
}