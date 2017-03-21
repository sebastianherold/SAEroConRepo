package net.sf.jabref.logic.formatter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests in addition to the general tests from {@link net.sf.jabref.logic.formatter.FormatterTest}
 */
public class IdentityFormatterTest {

    private IdentityFormatter formatter;

    @Before
    public void setUp() {
        formatter = new IdentityFormatter();
    }

    @Test
    public void formatExample() {
        assertEquals("JabRef", formatter.format(formatter.getExampleInput()));
    }
}