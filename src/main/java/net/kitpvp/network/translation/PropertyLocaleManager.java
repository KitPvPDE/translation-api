package net.kitpvp.network.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kitpvp.json.JsonConfig;
import net.kitpvp.json.JsonReader;
import net.kitpvp.network.translation.exception.InvalidTranslationException;
import net.kitpvp.network.translation.exception.MissingTranslationException;
import net.kitpvp.network.translation.format.TranslationFormat;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PropertyLocaleManager extends LocaleManager {

    private final List<Locale> loaded = new ArrayList<>();

    public PropertyLocaleManager(File folder) throws IOException {
        super();
        this.init(folder);
    }

    public PropertyLocaleManager(LocaleManager parent, File folder) throws IOException {
        super(parent);
        this.init(folder);
    }

    public PropertyLocaleManager(LocaleManager parent, String classpath) throws IOException {
        this(parent, PropertyLocaleManager.class, classpath);
    }

    public PropertyLocaleManager(String classpath) throws IOException{
        this(PropertyLocaleManager.class, classpath);
    }

    public PropertyLocaleManager(LocaleManager parent, Class<?> source, String classpath) throws IOException {
        super(parent);
        this.init(source, classpath);
    }

    public PropertyLocaleManager(Class<?> source, String classpath) throws IOException{
        super();
        this.init(source, classpath);
    }

    public void reloadLocaleManager(File folder) throws IOException {
        this.languages.clear();
        this.init(folder);
    }

    @Override
    public List<Locale> getLoadedLocales() {
        return this.loaded;
    }

    @Override
    public String translate(Locale locale, String translationKey, Object... args)
            throws InvalidTranslationException, MissingTranslationException {
        return super.translate(locale, translationKey, args);
    }

    private void initLocale(String language, String country, String version, InputStream inputStream) throws IOException {
        Locale locale = country == null ? new Locale(language) : new Locale(language, country);
        Properties properties = new Properties();
        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(streamReader);

            this.languages.put(locale, new HashMap<>());
            for(String entry : properties.stringPropertyNames()) {
                String value = properties.getProperty(entry);

                if(value == null)
                    continue;

                try {
                    this.languages.get(locale).put(entry, new TranslationFormat(value, locale));
                } catch (IllegalArgumentException cause) {
                    throw new IllegalArgumentException("Could not parse translation key " + entry + ": '" + value + "'", cause);
                }
            }

            this.loaded.add(locale);
        }
    }

    private void init(Class<?> source, String classpath) throws IOException {
        try (InputStream languageStream = source.getResourceAsStream(classpath + "/language.json")) {
            if(languageStream == null)
                throw new FileNotFoundException(classpath + "/language.json");
            JsonArray array = JsonReader.readToJson(languageStream).getAsJsonArray();

            for(JsonElement element : array) {
                String language = JsonConfig.readString(element, null, "language");
                String country = JsonConfig.readString(element, null, "country");
                String file = JsonConfig.readString(element, null, "file");
                String version = JsonConfig.readString(element, null, "version");

                try (InputStream inputStream = source.getResourceAsStream(classpath + "/" + file)){
                    this.initLocale(language, country, version, inputStream);
                }
            }
        }
        this.postInit(classpath);
    }

    private void init(File folder) throws IOException {
        try (InputStream languageStream = FileUtils.openInputStream(new File(folder, "language.json"))) {
            JsonArray array = JsonReader.readToJson(languageStream).getAsJsonArray();

            for(JsonElement element : array) {
                String language = JsonConfig.readString(element, null, "language");
                String country = JsonConfig.readString(element, null, "country");
                File file = new File(folder, JsonConfig.readString(element, null, "file"));
                String version = JsonConfig.readString(element, null, "version");

                try (InputStream inputStream = FileUtils.openInputStream(file)){
                    this.initLocale(language, country, version, inputStream);
                }
            }
        }
        this.postInit(folder.getAbsolutePath());
    }

    private void postInit(String path) {
        StringBuilder builder = new StringBuilder();
        for(Locale locale : this.getLoadedLocales()) {
            if(builder.length() > 0)
                builder.append(" | ");
            builder.append(locale).append(" (").append(this.languages.get(locale).size()).append(" keys)");
        }
        System.out.println("Loaded locales from " + path + ": " + builder);
    }
}
