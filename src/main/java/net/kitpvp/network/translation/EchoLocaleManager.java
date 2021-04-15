package net.kitpvp.network.translation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EchoLocaleManager extends LocaleManager {

    public static final EchoLocaleManager INSTANCE = new EchoLocaleManager();

    @Override
    public String translate(Locale locale, String translationKey, Object... args) {
        return translationKey;
    }
}
