package com.wx.fx;

import com.wx.fx.util.BundleWrapper;
import com.wx.properties.PropertiesManager;
import com.wx.util.log.LogHelper;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Utility class to set the default language. This offers the possibility to restrain the language to a set of supported
 * languages.
 * <p>
 * Created on 15/01/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Lang {

    private static final Logger LOG = LogHelper.getLogger(Lang.class);

    private static String langResource;

    /**
     * Set the current Locale corresponding to the given language tag.
     *
     * @param languageTag Language to set
     */
    public static void setLocale(String languageTag) {
        setLocale(Objects.requireNonNull(languageTag), (Locale[]) null);
    }

    /**
     * Set the current Locale corresponding to the given language tag if supported, else, sets the first supported
     * Locale.
     *
     * @param languageTag        Language to set (or null to try and set the current language)
     * @param supportedLanguages Supported Locales
     */
    public static void setLocale(String languageTag, Locale... supportedLanguages) {
        Locale lang = languageTag == null ?
                Locale.getDefault() :
                Locale.forLanguageTag(languageTag);

        if (lang == null) {
            LOG.warning("No Locale found for language tag " + languageTag + ", setting default");
            lang = Locale.getDefault();
        }

        if (supportedLanguages == null || supportedLanguages.length == 0) {
            LOG.finest("Setting language " + lang.getDisplayName());
            Locale.setDefault(lang);
            return;
        }

        for (Locale supported : supportedLanguages) {
            if (lang.getLanguage().equals(supported.getLanguage())) {
                LOG.finest("Setting language " + lang.getDisplayName());
                Locale.setDefault(lang);
                return;
            }
        }

        lang = supportedLanguages[0];
        Locale.setDefault(lang);
        LOG.severe("Language not supported, setting default: " + lang);
    }

    /**
     * Set the name of the language resource to use.
     *
     * @param resourceName Name of the language resource to use
     */
    public static void initLanguageResource(String resourceName) {
        Lang.langResource = resourceName;
    }

    /**
     * Get a language property.
     *
     * @param key    Key of the property
     * @param params Parameters to substitute in the resource
     *
     * @return An optional String associated with that key
     */
    public static Optional<String> getOptionalString(String key, Object... params) {
        return getLang().getString(key, params);
    }

    /**
     * Get a language property. If the property does not exist, an {@code IllegalArgumentException} is thrown.
     *
     * @param key    Key of the property
     * @param params Parameters to substitute in the resource
     *
     * @return String associated with that key
     */
    public static String getString(String key, Object... params) {
        return getOptionalString(key, params)
                .orElseThrow(() -> new IllegalArgumentException("Missing language resource key: " + key));
    }


    /**
     * Get the language resource bundle as read-only {@link PropertiesManager}.
     *
     * @return The language resource bundle of this app
     */
    private static PropertiesManager getLang() {
        return new PropertiesManager(new BundleWrapper(getBundle()));
    }

    /**
     * @return The language resource bundle
     */
    public static ResourceBundle getBundle() {
        if (langResource == null) {
            throw new IllegalStateException("Must set resource name first with initLanguageResource");
        }
        return ResourceBundle.getBundle(langResource);
    }
}
