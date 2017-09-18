package com.proto.core.event;

/**
 * A Marshaller is capable of marshaling a object to a typed T object, and unmashaling a typed object to object
 *
 * @author wfrancis
 */
public interface Translator<T> {

    public static class TranslationException extends Exception {

        public TranslationException(String message) {
            super(message);
        }

        public TranslationException(String message, Throwable e) {
            super(message, e);
        }
    }

    /**
     * marshal a object
     * @param o object to be marshaled
     * @return marshaled object
     */
    T translate(Object o) throws TranslationException;


    /**
     * unmarshal a object
     * @param o object to be unmarshaled
     * @return unmarshaled object
     */
    Object untranslate(T o) throws TranslationException;

    /**
     * Translate a event key to a subject string
     * @param eventKey eventkey to translate
     * @return a string representing the target subject
     * @throws TranslationException
     */
    String translateEventKey(EventKey eventKey) throws TranslationException;

    /**
     * Translate a subject back to a event key
     */
    EventKey untranslateEventKey(String subject) throws TranslationException;

    /**
     * Version number of this translator
     * @return version number of this translator
     */
    public int getVersion();
}
