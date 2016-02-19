package com.wx.fx;

import com.wx.util.log.LogHelper;

import java.util.Locale;
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

    /**
     * Set the current Locale corresponding to the given language tag.
     *
     * @param languageTag Language to set
     */
    public static void setLocale(String languageTag) {
        setLocale(languageTag, null);
    }

    /**
     * Set the current Locale corresponding to the given language tag if supported, else, sets the first supported
     * Locale.
     *
     * @param languageTag        Language to set
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

        if (supportedLanguages == null) {
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

}
