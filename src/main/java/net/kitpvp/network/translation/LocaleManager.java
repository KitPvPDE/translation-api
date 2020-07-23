package net.kitpvp.network.translation;

import lombok.Getter;
import net.kitpvp.mongodbapi.MongoConnector;
import org.bson.Document;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocaleManager {

    @Getter
    private static LocaleManager instance;
    public static final Set<Locale> ACCEPTED = Stream.of(Locale.US, Locale.GERMANY).collect(Collectors.toSet());
    public static final Locale DEFAULT = Locale.US;

    public static LocaleManager createFromDatabase() {
        return (instance = new LocaleManager());
    }

    public static String staticTranslate(String languageKey, Object... args) {
        return staticTranslate(DEFAULT, languageKey, args);
    }

    public static String staticTranslate(Locale locale, String languageKey, Object... args) {
        return getInstance().translate(locale, languageKey, args);
    }

    private Map<Locale, Map<String, MessageFormat>> languages = new HashMap<>();

    private LocaleManager() {
        for(Locale locale : ACCEPTED){
            Map<String, MessageFormat> languageFormats = new HashMap<>();
            for(Document document : MongoConnector.getInstance().getMongoDatabase().getCollection("language_keys_" + locale.toString()).find()){
                try{
                    languageFormats.put(document.getString("_id"), new MessageFormat(document.getString("value").replace("%D", "  ")));
                }catch(Throwable e){
                    System.err.println("Could not parse language format: " + document.getString("value"));
                }
            }

            this.languages.put(locale, languageFormats);
            System.out.println("Loaded locale " + locale + " - " + languageFormats.size() + " language keys");
        }
    }

    public String translate(String translationKey, Object... args) {
        return this.translate(DEFAULT, translationKey, args);
    }

    public String translate(Locale locale, String translationKey, Object... args) {
        try {
            MessageFormat translationFormat = this.findTranslation(locale, translationKey);

            return translationFormat.format(args, new StringBuffer(), new FieldPosition(0)).toString();
        } catch(Throwable throwable) {
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

    public List<Locale> getLoadedLocales() {
        return new ArrayList<>(this.languages.keySet());
    }
}
