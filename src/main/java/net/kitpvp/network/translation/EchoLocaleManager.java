package net.kitpvp.network.translation;

import java.util.Locale;

public class EchoLocaleManager extends LocaleManager {

    public static final EchoLocaleManager INSTANCE = new EchoLocaleManager();

    private EchoLocaleManager() {}

    @Override
    public String translate(Locale locale, String translationKey, Object... args) {
        return translationKey;
    }
}
