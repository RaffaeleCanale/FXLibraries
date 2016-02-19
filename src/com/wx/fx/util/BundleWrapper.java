package com.wx.fx.util;

import com.wx.properties.PropertiesMapInterface;

import java.util.ResourceBundle;
import java.util.Set;

/**
 * This implementation wraps a {@link ResourceBundle} in a {@link PropertiesMapInterface}.
 * <p>
 * Because a {@link ResourceBundle} is write only, all modifying functions will yield an {@link
 * UnsupportedOperationException}.
 * <p>
 * Created on 15/01/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class BundleWrapper implements PropertiesMapInterface {

    private final ResourceBundle bundle;

    /**
     * Build a {@code PropertiesMapInterface} from a {@code ResourceBundle}.
     *
     * @param bundle Bundle to wrap
     */
    public BundleWrapper(ResourceBundle bundle) {
        this.bundle = bundle;
    }


    @Override
    public String getProperty(String s) {
        return containsKey(s) ? bundle.getString(s) : null;

    }

    @Override
    public String setProperty(String key, String value) {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

    @Override
    public String removeProperty(String s) {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

    @Override
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return bundle.keySet();
    }
}
