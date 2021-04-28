package net.kitpvp.translation;

import net.kitpvp.network.translation.EchoLocaleManager;
import net.kitpvp.network.translation.LocaleManager;
import net.kitpvp.network.translation.PropertyLocaleManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

public class TestPropertyLocales {

    @Test
    public void testPropertyLocaleManager() throws IOException {
        PropertyLocaleManager localeManager = new PropertyLocaleManager(TestPropertyLocales.class, "/locales");
        System.out.println(localeManager.getLoadedLocales());

        Assert.assertEquals("Das ist ein Test! 33", localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}", localeManager.translate(Locale.CANADA, "translation.test"));

        System.out.println(localeManager.translate(new Locale("de", "DE"), "translation.test.second"));
        System.out.println(localeManager.translate(new Locale("en", "US"), "translation.test.second"));
        System.out.println(localeManager.translate(new Locale("en", "US"), "translation.test", "33"));
    }

    @Test
    public void testPropertyLocaleManagerWithParent() throws IOException {
        PropertyLocaleManager localeManager = new PropertyLocaleManager(EchoLocaleManager.INSTANCE, TestPropertyLocales.class, "/locales");
        System.out.println(localeManager.getLoadedLocales());

        Assert.assertEquals("Das ist ein Test! 33", localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}", localeManager.translate(Locale.CANADA, "translation.test"));

        System.out.println(localeManager.translate(new Locale("de", "DE"), "translation.test.second"));
        System.out.println(localeManager.translate(new Locale("en", "US"), "translation.test.second"));
        System.out.println(localeManager.translate(new Locale("en", "US"), "translation.test", "33"));
    }

    @Test
    public void testPropertyLocaleManagerWithRealParent() throws IOException {
        PropertyLocaleManager parent = new PropertyLocaleManager(TestPropertyLocales.class, "/fallback");
        PropertyLocaleManager localeManager = new PropertyLocaleManager(parent, TestPropertyLocales.class, "/locales");
        System.out.println(localeManager.getLoadedLocales());
        System.out.println(localeManager.translate(LocaleManager.DEFAULT, "translation"));
        System.out.println(localeManager.translate(Locale.US, "translation"));

        Assert.assertEquals("Das ist ein Test! 33", localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}", localeManager.translate(Locale.CANADA, "translation.test"));
    }
}
