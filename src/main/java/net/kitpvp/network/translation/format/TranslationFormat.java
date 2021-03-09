package net.kitpvp.network.translation.format;

import java.text.*;
import java.util.Date;
import java.util.Locale;

public class TranslationFormat {

    private static final int INITIAL_FORMATS = 2;
    // Indices for segments
    private static final int SEG_RAW = 0;
    private static final int SEG_INDEX = 1;
    private static final int SEG_TYPE = 2;
    private static final int SEG_MODIFIER = 3;

    // Indices for type keywords
    private static final int TYPE_NULL = 0;
    private static final int TYPE_NUMBER = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_TIME = 3;
    private static final int TYPE_CHOICE = 4;
    private static final int TYPE_RANGE = 5;

    private static final String[] TYPE_KEYWORDS = {
            "",
            "number",
            "date",
            "time",
            "choice",
            "range"
    };

    // Indices for number modifiers
    private static final int MODIFIER_DEFAULT = 0; // common in number and date-time
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_PERCENT = 2;
    private static final int MODIFIER_INTEGER = 3;

    private static final String[] NUMBER_MODIFIER_KEYWORDS = {
            "",
            "currency",
            "percent",
            "integer"
    };

    // Indices for date-time modifiers
    private static final int MODIFIER_SHORT = 1;
    private static final int MODIFIER_MEDIUM = 2;
    private static final int MODIFIER_LONG = 3;
    private static final int MODIFIER_FULL = 4;

    private static final String[] DATE_TIME_MODIFIER_KEYWORDS = {
            "",
            "short",
            "medium",
            "long",
            "full"
    };

    // Date-time style values corresponding to the date-time modifiers.
    private static final int[] DATE_TIME_MODIFIERS = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL,
    };

    private final String translation;
    private final Locale locale;

    public TranslationFormat( String translation, Locale locale) {
        this.translation = translation;
        this.locale = locale;
        this.applyPattern();
    }

    public final StringBuffer format(Object[] args, StringBuffer buffer) {
        return subformat(args, buffer);
    }

    private String pattern;
    private Format[] formats = new Format[INITIAL_FORMATS];
    private int[] offsets = new int[INITIAL_FORMATS];
    private int[] argumentNumbers = new int[INITIAL_FORMATS];
    private int maxOffset;

    private StringBuffer subformat(Object[] args, StringBuffer appendTo) {
        int lastOffset = 0;
        for (int i = 0; i <= maxOffset; ++i) {
            appendTo.append(pattern, lastOffset, offsets[i]);
            lastOffset = offsets[i];
            int argumentNumber = argumentNumbers[i];
            if (args == null || argumentNumber >= args.length) {
                appendTo.append('{').append(argumentNumber).append('}');
                continue;
            }
            Object obj = args[argumentNumber];
            String arg = null;
            Format subFormatter = null;
            if (obj == null) {
                arg = "null";
            } else if (formats[i] != null) {
                subFormatter = formats[i];
                if (subFormatter instanceof ChoiceFormat) {
                    arg = formats[i].format(obj);
                }
            } else if (obj instanceof Number) {
                // format number if can
                subFormatter = NumberFormat.getInstance(locale);
            } else if (obj instanceof Date) {
                // format a Date if can
                subFormatter = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT, locale);//fix
            } else if (obj instanceof String) {
                arg = (String) obj;
            } else {
                arg = obj.toString();
                if (arg == null) arg = "null";
            }

            // At this point we are in two states, either subFormatter
            // is non-null indicating we should format obj using it,
            // or arg is non-null and we should use it as the value.

            if (subFormatter != null) {
                arg = subFormatter.format(obj);
                formats[i] = subFormatter;
            }
            appendTo.append(arg);
        }
        return appendTo.append(pattern, lastOffset, pattern.length());
    }

    private void applyPattern() {
        StringBuilder[] segments = new StringBuilder[4];
        segments[SEG_RAW] = new StringBuilder();

        int part = SEG_RAW;
        int formatNumber = 0;
        int braceStack = 0;
        boolean inQuote = false;
        maxOffset = -1;

        for (int i = 0; i < this.translation.length(); ++i) {
            char ch = this.translation.charAt(i);
            char lookahead = this.translation.length() > i + 1 ? this.translation.charAt(i + 1) : 0;
            if (part == SEG_RAW) {
                if (ch == '\'') {
                    inQuote = !inQuote;
                } else if (ch == '{' && !inQuote && lookahead != '%') {
                    part = SEG_INDEX;
                    if (segments[SEG_INDEX] == null) {
                        segments[SEG_INDEX] = new StringBuilder();
                    }
                } else {
                    segments[part].append(ch);
                }
            } else {
                if (inQuote) {
                    if (ch == '\'') {
                        inQuote = false;
                    } else {
                        segments[part].append(ch);
                    }
                } else {
                    switch (ch) {
                        case ',':
                            if (part < SEG_MODIFIER) {
                                if (segments[++part] == null) {
                                    segments[part] = new StringBuilder();
                                }
                            } else {
                                segments[part].append(ch);
                            }
                            break;
                        case '{':
                            ++braceStack;
                            segments[part].append(ch);
                            break;
                        case '}':
                            if (braceStack == 0) {
                                part = SEG_RAW;
                                makeFormat(formatNumber, segments);
                                formatNumber++;
                                // throw away other segments
                                segments[SEG_INDEX] = null;
                                segments[SEG_TYPE] = null;
                                segments[SEG_MODIFIER] = null;
                            } else {
                                --braceStack;
                                segments[part].append(ch);
                            }
                            break;
                        case ' ':
                            if (part != SEG_TYPE || segments[SEG_TYPE].length() > 0) {
                                segments[part].append(ch);
                            }
                            break;
                        case '\'':
                        default:
                            if (ch == '\'') {
                                inQuote = true;
                            } else {
                                segments[part].append(ch);
                            }
                            break;
                    }
                }
            }
        }
        if (braceStack == 0 && part != 0) {
            maxOffset = -1;
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
        this.pattern = segments[0].toString();
    }

    private void makeFormat(int offsetNumber,
                            StringBuilder[] textSegments) {
        String[] segments = new String[textSegments.length];
        for (int i = 0; i < textSegments.length; i++) {
            StringBuilder oneseg = textSegments[i];
            segments[i] = (oneseg != null) ? oneseg.toString() : "";
        }

        // get the argument number
        int argumentNumber;
        try {
            argumentNumber = Integer.parseInt(segments[SEG_INDEX]); // always unlocalized!
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("can't parse argument number: "
                    + segments[SEG_INDEX], e);
        }
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number: "
                    + argumentNumber);
        }

        // resize format information arrays if necessary
        if (offsetNumber >= formats.length) {
            int newLength = formats.length * 2;
            Format[] newFormats = new Format[newLength];
            int[] newOffsets = new int[newLength];
            int[] newArgumentNumbers = new int[newLength];
            System.arraycopy(formats, 0, newFormats, 0, maxOffset + 1);
            System.arraycopy(offsets, 0, newOffsets, 0, maxOffset + 1);
            System.arraycopy(argumentNumbers, 0, newArgumentNumbers, 0, maxOffset + 1);
            formats = newFormats;
            offsets = newOffsets;
            argumentNumbers = newArgumentNumbers;
        }
        int oldMaxOffset = maxOffset;
        maxOffset = offsetNumber;
        offsets[offsetNumber] = segments[SEG_RAW].length();
        argumentNumbers[offsetNumber] = argumentNumber;

        // now get the format
        Format newFormat = null;
        if (!segments[SEG_TYPE].isEmpty()) {
            int type = findKeyword(segments[SEG_TYPE], TYPE_KEYWORDS);
            switch (type) {
                case TYPE_NULL:
                    // Type "" is allowed. e.g., "{0,}", "{0,,}", and "{0,,#}"
                    // are treated as "{0}".
                    break;

                case TYPE_NUMBER:
                    switch (findKeyword(segments[SEG_MODIFIER], NUMBER_MODIFIER_KEYWORDS)) {
                        case MODIFIER_DEFAULT:
                            newFormat = NumberFormat.getInstance(locale);
                            break;
                        case MODIFIER_CURRENCY:
                            newFormat = NumberFormat.getCurrencyInstance(locale);
                            break;
                        case MODIFIER_PERCENT:
                            newFormat = NumberFormat.getPercentInstance(locale);
                            break;
                        case MODIFIER_INTEGER:
                            newFormat = NumberFormat.getIntegerInstance(locale);
                            break;
                        default: // DecimalFormat pattern
                            try {
                                newFormat = new DecimalFormat(segments[SEG_MODIFIER],
                                        DecimalFormatSymbols.getInstance(locale));
                            } catch (IllegalArgumentException e) {
                                maxOffset = oldMaxOffset;
                                throw e;
                            }
                            break;
                    }
                    break;

                case TYPE_DATE:
                case TYPE_TIME:
                    int mod = findKeyword(segments[SEG_MODIFIER], DATE_TIME_MODIFIER_KEYWORDS);
                    if (mod >= 0 && mod < DATE_TIME_MODIFIER_KEYWORDS.length) {
                        if (type == TYPE_DATE) {
                            newFormat = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[mod],
                                    locale);
                        } else {
                            newFormat = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[mod],
                                    locale);
                        }
                    } else {
                        // SimpleDateFormat pattern
                        try {
                            newFormat = new SimpleDateFormat(segments[SEG_MODIFIER], locale);
                        } catch (IllegalArgumentException e) {
                            maxOffset = oldMaxOffset;
                            throw e;
                        }
                    }
                    break;

                case TYPE_CHOICE:
                    try {
                        // ChoiceFormat pattern
                        newFormat = new ChoiceFormat(segments[SEG_MODIFIER]);
                    } catch (Exception e) {
                        maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("Choice Pattern incorrect: "
                                + segments[SEG_MODIFIER], e);
                    }
                    break;

                case TYPE_RANGE:
                    try {
                        // RangeFormat pattern
                        newFormat = new RangeFormat(locale, segments[SEG_MODIFIER]);
                    } catch (Exception e) {
                        maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("Range Pattern incorrect: "
                                + segments[SEG_MODIFIER], e);
                    }
                    break;

                default:
                    maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException("unknown format type: " +
                            segments[SEG_TYPE]);
            }
        }
        formats[offsetNumber] = newFormat;
    }

    private static int findKeyword(String s, String[] list) {
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }

        // Try trimmed lowercase.
        String ls = s.trim().toLowerCase(Locale.ROOT);
        if (!ls.equals(s)) {
            for (int i = 0; i < list.length; ++i) {
                if (ls.equals(list[i]))
                    return i;
            }
        }
        return -1;
    }
}
