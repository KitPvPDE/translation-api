package net.kitpvp.network.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kitpvp.json.JsonConfig;
import net.kitpvp.json.JsonReader;
import net.kitpvp.network.translation.format.TranslationFormat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
        super(parent);
        this.init(classpath);
    }

    public PropertyLocaleManager(String classpath) throws IOException{
        super();
        this.init(classpath);
    }

    @Override
    public List<Locale> getLoadedLocales() {
        return this.loaded;
    }

    private void initLocale(String language, String country, String version, InputStream inputStream) throws IOException {
        Locale locale = country == null ? new Locale(language) : new Locale(language, country);
        Properties properties = new Properties();
        properties.load(inputStream);

        this.languages.put(locale, new HashMap<>());
        for(String entry : properties.stringPropertyNames()) {
            String value = properties.getProperty(entry);

            if(value == null)
                continue;

            this.languages.get(locale).put(entry, new TranslationFormat(value, locale));
        }

        this.loaded.add(locale);
        System.out.println("Loaded Locale " + locale + " version " + version + " (" + this.languages.get(locale).size() + " keys)");
    }

    private void init(String classpath) throws IOException {
        try (InputStream languageStream = this.getClass().getResourceAsStream(classpath + "/language.json")) {
            if(languageStream == null)
                throw new FileNotFoundException(classpath + "/language.json");
            JsonArray array = JsonReader.readToJson(languageStream).getAsJsonArray();

            for(JsonElement element : array) {
                String language = JsonConfig.readString(element, null, "language");
                String country = JsonConfig.readString(element, null, "country");
                String file = JsonConfig.readString(element, null, "file");
                String version = JsonConfig.readString(element, null, "version");

                try (InputStream inputStream = this.getClass().getResourceAsStream(classpath + "/" + file)){
                    this.initLocale(language, country, version, inputStream);
                }
            }
        }
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

    }
}
