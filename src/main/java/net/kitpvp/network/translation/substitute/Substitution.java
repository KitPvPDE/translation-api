package net.kitpvp.network.translation.substitute;

import net.kitpvp.network.translation.LocaleManager;

import java.util.Locale;

public interface Substitution<T> {

    T replace(Locale locale, LocaleManager localeManager);
}
