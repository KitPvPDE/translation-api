package net.kitpvp.translation;

import net.kitpvp.network.translation.EchoLocaleManager;
import net.kitpvp.network.translation.LocaleManager;
import net.kitpvp.network.translation.PropertyLocaleManager;
import net.kitpvp.network.translation.exception.MissingTranslationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TestPropertyLocales {

    @Test
    public void testPropertyLocaleManager() throws IOException, ExecutionException {
        PropertyLocaleManager localeManager = new PropertyLocaleManager(TestPropertyLocales.class, "/locales");

        Assert.assertEquals("Das ist ein Test! 33",
                localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}",
                localeManager.translate(Locale.CANADA, "translation.test"));

        Assert.assertThrows("Missing translations should throw an exception",
                MissingTranslationException.class, () ->
                        localeManager.translate(LocaleManager.DEFAULT, "translation.missing"));

        Assert.assertEquals("Dieser test wurde gejoinked lul",
                localeManager.translate(new Locale("en", "PT"), "translation.test", "lul"));
    }

    @Test
    public void testPropertyLocaleManagerWithParent() throws IOException, ExecutionException {
        PropertyLocaleManager localeManager = new PropertyLocaleManager(EchoLocaleManager.INSTANCE,
                TestPropertyLocales.class, "/locales");

        Assert.assertEquals("Das ist ein Test! 33", localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}", localeManager.translate(Locale.CANADA, "translation.test"));
    }

    @Test
    public void testPropertyLocaleManagerWithRealParent() throws IOException, ExecutionException {
        PropertyLocaleManager parent = new PropertyLocaleManager(TestPropertyLocales.class, "/fallback");
        PropertyLocaleManager localeManager = new PropertyLocaleManager(parent, TestPropertyLocales.class, "/locales");

        Assert.assertEquals("Das ist ein Test! 33",
                localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}",
                localeManager.translate(Locale.CANADA, "translation.test"));

        Assert.assertEquals("Diese Übersetzung wurde gejoinked lul!",
                localeManager.translate(new Locale("en", "PT"), "translation", "lul"));

        Assert.assertEquals("Improve yer PvP-Skills at 1v1's\nwithout taking damage\n\n§a33 Players".replace('\n', ' '),
                localeManager.translate(new Locale("en", "PT"), "warps.nodamage.description", 33)
        .replace('\n', ' '));
    }
}
