package net.kitpvp.translation;

import net.kitpvp.network.translation.LocaleManager;
import net.kitpvp.network.translation.format.TranslationFormat;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestTranslation {

    @Test
    public void testNoArguments() {
        TranslationFormat format = new TranslationFormat("This is a message without arguments", LocaleManager.DEFAULT);
        assertEquals("This is a message without arguments", format.format(new Object[0], new StringBuffer()).toString());
    }

    @Test
    public void testPlainArgument() {
        TranslationFormat format = new TranslationFormat("This is a message with one argument ({0})", LocaleManager.DEFAULT);
        assertEquals("This is a message with one argument (replacement)", format.format(new Object[] {"replacement"}, new StringBuffer()).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgument() {
        TranslationFormat format = new TranslationFormat("This is a message with one argument ({-1})", LocaleManager.DEFAULT);
        assertEquals("This is a message with one argument (replacement)", format.format(new Object[] {"replacement"}, new StringBuffer()).toString());
    }

    @Test
    public void testMultipleMissingArguments() {
        TranslationFormat format = new TranslationFormat("This is a {0} with {1} argument ({2})", LocaleManager.DEFAULT);
        assertEquals("This is a replacement with {1} argument ({2})", format.format(new Object[] {"replacement"}, new StringBuffer()).toString());
    }

    @Test
    public void testTooManyArguments() {
        TranslationFormat format = new TranslationFormat("This is a message with one argument ({0})", LocaleManager.DEFAULT);
        assertEquals("This is a message with one argument (1)", format.format(new Object[] {1,2,3,4,5}, new StringBuffer()).toString());
    }

    @Test
    public void testNullArgument() {
        TranslationFormat format = new TranslationFormat("This is a message with one argument ({0})", LocaleManager.DEFAULT);
        assertEquals("This is a message with one argument (null)", format.format(new Object[]{null}, new StringBuffer()).toString());
    }

    @Test
    public void testNullArguments() {
        TranslationFormat format = new TranslationFormat("This is a message with one argument ({0})", LocaleManager.DEFAULT);
        assertEquals("This is a message with one argument ({0})", format.format(null, new StringBuffer()).toString());
    }

    @Test
    public void testColorCodeInFormat() {
        TranslationFormat format = new TranslationFormat("This is a {%m}message{%n} with one argument and color ({%m}{0})", LocaleManager.DEFAULT);
        assertEquals("This is a {%m}message{%n} with one argument and color ({%m}0)", format.format(new Object[]{0}, new StringBuffer()).toString());
    }

    @Test
    public void testRangeFormat(){
        TranslationFormat format = new TranslationFormat("This is a message with a range format used ({0,range,{0;, ;n-1} and {n}})", LocaleManager.DEFAULT);
        assertEquals("This is a message with a range format used (a, b, c, d, e and f)",
                format.format(new Object[]{Arrays.asList("a", "b", "c", "d", "e", "f")}, new StringBuffer()).toString());
    }
}
