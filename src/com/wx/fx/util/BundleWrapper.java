package com.wx.fx.util;


import java.util.*;

/**
 * Because a {@link ResourceBundle} is write only, all modifying functions will yield an {@link
 * UnsupportedOperationException}.
 * <p>
 * Created on 15/01/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class BundleWrapper extends AbstractMap<String, String> {

    private transient Set<Entry<String, String>> entrySet;

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
    public Set<String> keySet() {
        return Collections.unmodifiableSet(bundle.keySet());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        if (entrySet == null) {
            entrySet = Collections.unmodifiableSet(new AbstractSet<Entry<String, String>>() {

                @Override
                public Iterator<Entry<String, String>> iterator() {
                    return new Iterator<Entry<String, String>>() {

                        Iterator<String> keyIt = bundle.keySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return keyIt.hasNext();
                        }

                        @Override
                        public Entry<String, String> next() {
                            return new Entry<String, String>() {

                                final String key = keyIt.next();

                                @Override
                                public String getKey() {
                                    return key;
                                }

                                @Override
                                public String getValue() {
                                    return bundle.getString(key);
                                }

                                @Override
                                public String setValue(String value) {
                                    throw new UnsupportedOperationException("ResourceBundle is read-only");
                                }
                            };
                        }
                    };
                }

                @Override
                public int size() {
                    return BundleWrapper.this.size();
                }
            });
        }
        return entrySet;
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

    @Override
    public boolean containsKey(Object key) {
        return bundle.containsKey((String) key);
    }

    @Override
    public int size() {
        return bundle.keySet().size();
    }

    @Override
    public String get(Object key) {
        return bundle.getString((String) key);
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ResourceBundle is read-only");
    }

}
