package net.kitpvp.network.translation;

import lombok.Getter;
import lombok.Setter;
import net.kitpvp.network.translation.substitute.Substitution;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocaleManager {

    public static final Set<Locale> ACCEPTED = Stream.of(Locale.US, Locale.GERMANY).collect(Collectors.toSet());
    public static final Locale DEFAULT = Locale.US;

    @Setter
    private static LocaleManager instance = EchoLocaleManager.INSTANCE;

    public static LocaleManager getInstance() {
        return instance == null ? EchoLocaleManager.INSTANCE : instance;
    }

    public static String staticTranslate(String languageKey, Object... args) {
        return staticTranslate(DEFAULT, languageKey, args);
    }

    public static String staticTranslate(Locale locale, String languageKey, Object... args) {
        return getInstance().translate(locale, languageKey, args);
    }

    protected final Map<Locale, Map<String, MessageFormat>> languages = new HashMap<>();

    protected LocaleManager() { }

    public String translate(String translationKey, Object... args) {
        return this.translate(DEFAULT, translationKey, args);
    }

    public String translate(Locale locale, String translationKey, Object... args) {
        try{
            MessageFormat translationFormat = this.findTranslation(locale, translationKey);

            return translationFormat.format(this.applySubstitutions(locale, args), new StringBuffer(), new FieldPosition(0)).toString();
        }catch(Throwable throwable){
            return translationKey;
        }
    }

    private MessageFormat findTranslation(Locale locale, String translationKey) {
        if(!ACCEPTED.contains(locale))
            return this.findTranslation(DEFAULT, translationKey);

        if(!this.languages.containsKey(locale)){
            this.languages.put(locale, new HashMap<>());
        }

        if(!this.languages.get(locale).containsKey(translationKey)){
            if(!locale.equals(DEFAULT))
                return this.findTranslation(DEFAULT, translationKey);
        }

        return this.languages.get(locale).computeIfAbsent(translationKey, MessageFormat::new);
    }

    private Object[] applySubstitutions(Locale locale, Object[] args) {
        for(int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if(obj instanceof Substitution) {
                args[i] = ((Substitution<?>) obj).replace(locale, this);
            }
        }
        return args;
    }

    public List<Locale> getLoadedLocales() {
        return new ArrayList<>(this.languages.keySet());
    }
}
