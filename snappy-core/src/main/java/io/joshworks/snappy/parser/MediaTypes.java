package io.joshworks.snappy.parser;

import io.undertow.util.HeaderValues;
import io.undertow.util.MimeMappings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by josh on 3/12/17.
 */
public class MediaTypes extends HashSet<MediaTypes.MediaType> {

    private static MimeMappings mimeMappings = MimeMappings.builder().build();

    private static final String CHARSET_SEPARATOR = ";";
    private static final String SUBTYPE_SEPARATOR = "/";
    private static final String WILDCARD = "*";
    private static final MediaType ANY = new MediaType(WILDCARD + SUBTYPE_SEPARATOR + WILDCARD);


    public enum Context {
        PRODUCES, CONSUMES

    }

    public static MediaTypes DEFAULT_CONSUMES = new MediaTypes(Context.CONSUMES, "application/json", "text/plain");
    public static MediaTypes DEFAULT_PRODUCES = new MediaTypes(Context.CONSUMES, "application/json", "text/plain");


    private final Context type;

    private MediaTypes(Context type) {
        this.type = type;
    }

    private MediaTypes(Context type, String... types) {
        this(type);
        for (String mt : types) {
            add(new MediaType(mt));
        }
    }

    public List<String> match(HeaderValues header) {
        Iterator<String> iterator = header.iterator();
        List<String> matches = new ArrayList<>();
        while (iterator.hasNext()) {
            String headerVal = iterator.next();
            MediaType accepts = new MediaType(headerVal);
            headerVal = removeCharset(headerVal);
            if (headerVal != null && this.contains(accepts)) {
                matches.add(headerVal);
            }
        }
        return matches;
    }


    public static String removeCharset(String mediaType) {
        if (mediaType == null || mediaType.isEmpty()) {
            return null;
        }
        String[] split = mediaType.split(CHARSET_SEPARATOR);
        if (split.length == 1) {
            return mediaType;
        }
        mediaType = split[0];
        return mediaType == null ? null : mediaType.trim();

    }

    public static MediaTypes accepts(String... types) {
        return addTo(Context.CONSUMES, types);
    }

    public static MediaTypes produces(String... types) {
        return addTo(Context.PRODUCES, types);
    }

    private static MediaTypes addTo(MediaTypes.Context type, String... types) {
        MediaTypes mediaType = new MediaTypes(type);
        for (String mime : types) {
            mime = parseType(mime);
            if (mime == null) {
                throw new RuntimeException("Invalid mime type " + mime);
            }
            mediaType.add(new MediaType(mime));
        }
        return mediaType;
    }

    private static String parseType(String mime) {
        if (mime.contains("/")) {
            return mime;
        }
        return mimeMappings.getMimeType(mime);
    }

    public Context getContext() {
        return type;
    }

    public static class MediaType {
        public String primary;
        public String subType;
        public String charset;

        public MediaType(String type) {
            String[] splitType = type.split(SUBTYPE_SEPARATOR);
            if (splitType.length == 2) {
                String charset = null;
                if (splitType[1].contains(CHARSET_SEPARATOR)) {
                    charset = splitType[1].split(CHARSET_SEPARATOR)[1].trim();
                    charset = charset.split("=")[1];
                }
                this.primary = splitType[0];
                this.subType = splitType[1];
                this.charset = charset;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediaType mediaType = (MediaType) o;
            if (WILDCARD.equals(mediaType.primary) || WILDCARD.equals(primary)) {
                return true;
            }
            if (!primary.equals(mediaType.primary)) {
                return false;
            }
            if (WILDCARD.equals(mediaType.subType) || WILDCARD.equals(subType)) {
                return true;
            }
            if (subType.equals(mediaType.subType)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = primary != null ? primary.hashCode() : 0;
            result = 31 * result + (subType != null ? subType.hashCode() : 0);
            result = 31 * result + (charset != null ? charset.hashCode() : 0);
            return result;
        }
    }

}
