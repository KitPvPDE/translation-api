package net.kitpvp.translation;

import net.kitpvp.network.translation.LocaleManager;
import net.kitpvp.network.translation.PropertyLocaleManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

public class PropertyTest {

    @Test
    public void testPropertyLocaleManager() throws IOException {
        PropertyLocaleManager localeManager = new PropertyLocaleManager(PropertyTest.class, "/locales");
        System.out.println(localeManager.getLoadedLocales());

        Assert.assertEquals("Das ist ein Test! 33", localeManager.translate(LocaleManager.DEFAULT, "translation.test", "33"));
        Assert.assertEquals("Das ist ein Test! {0}", localeManager.translate(Locale.CANADA, "translation.test"));
    }
}
