package net.kitpvp.network.translation;

import lombok.Getter;
import lombok.Setter;
import net.kitpvp.network.translation.exception.InvalidTranslationException;
import net.kitpvp.network.translation.exception.MissingTranslationException;
import net.kitpvp.network.translation.format.TranslationFormat;
import net.kitpvp.network.translation.substitute.Substitution;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class LocaleManager {

    public static final Locale DEFAULT = Locale.GERMANY;
    @Getter
    @Setter
    private static LocaleManager instance = EchoLocaleManager.INSTANCE;

    protected final Map<Locale, Map<String, TranslationFormat>> languages = new HashMap<>();
    protected final LocaleManager parent;

    public LocaleManager(LocaleManager parent) {
        this.parent = parent;
    }

    public LocaleManager() {
        this(null);
    }

    public List<Locale> getLoadedLocales() {
        return new ArrayList<>(this.languages.keySet());
    }

    @Deprecated
    public String translate(String translationKey, Object... args) throws
            InvalidTranslationException, MissingTranslationException {
        return this.translate(DEFAULT, translationKey, args);
    }

    public String translate(Locale locale, String translationKey, Object... args) throws
            InvalidTranslationException, MissingTranslationException {
        try {
            TranslationFormat translationFormat = this.findTranslation(locale, translationKey);
            if (translationFormat == null) {
                if (parent == null)
                    throw new MissingTranslationException(translationKey);
                return parent.translate(locale, translationKey, args);
            }
            return translationFormat.format(this.applySubstitutions(locale, args), new StringBuffer()).toString();
        } catch (MissingTranslationException | InvalidTranslationException cause){
            throw cause;
        } catch (Throwable cause) {
            throw new InvalidTranslationException(translationKey, cause);
        }
    }

    protected final @Nullable TranslationFormat findTranslation(Locale locale, String translationKey) {
        if (!this.languages.containsKey(locale)) {
            return findTranslation(DEFAULT, translationKey);
        }

        if (!this.languages.get(locale).containsKey(translationKey)) {
            if (!locale.equals(DEFAULT))
                return this.findTranslation(DEFAULT, translationKey);
        }

        return this.languages.get(locale).get(translationKey);
    }

    protected Object[] applySubstitutions(Locale locale, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj instanceof Substitution) {
                args[i] = ((Substitution<?>) obj).replace(locale, this);
            }
        }
        return args;
    }
}
