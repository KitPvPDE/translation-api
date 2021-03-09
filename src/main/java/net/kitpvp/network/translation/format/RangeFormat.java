package net.kitpvp.network.translation.format;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Format lists:
 *
 * {1;, ;n-1} and {n}
 *
 * 1..n 1..n 1..n
 *
 */
public class RangeFormat extends Format {

    private final Locale locale;

    public RangeFormat(Locale locale, String pattern) {
        this.locale = locale;
        this.applyPattern(pattern);
    }

    private String pattern;
    private Format[] formats = new Format[2];
    private AbstractRange[] ranges = new AbstractRange[INITIAL_FORMATS];
    private int[] offsets = new int[INITIAL_FORMATS];
    private int maxOffset;

    private static final int INITIAL_FORMATS = 2;
    private static final int FORMAT_NUMBER = 0;
    private static final int FORMAT_DATE   = 1;

    private static final int SEG_RAW = 0;
    private static final int SEG_START = 1;
    private static final int SEG_SEPARATOR = 2;
    private static final int SEG_END = 3;

    private static final Pattern PATTERN_START_INDEX = Pattern.compile("[0-9]+");
    private static final Pattern PATTERN_END_INDEX = Pattern.compile("(n)|(n-[0-9]+)|([0-9]+)");
    private static final Pattern PATTERN_ONLY_INDEX = PATTERN_END_INDEX;

    private static final Object[] EMPTY = new Object[0];

    @Override
    public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
        if(obj instanceof Object[]) {
            return this.subformat((Object[]) obj, toAppendTo);
        } else if(obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            return this.subformat(collection.toArray(new Object[0]), toAppendTo);
        } else {
            return this.subformat(EMPTY, toAppendTo);
        }
    }

    private StringBuffer subformat(Object[] arguments, StringBuffer appendTo) {
        int lastOffset = 0;
        for (int i = 0; i <= maxOffset; ++i) {
            appendTo.append(pattern, lastOffset, offsets[i]);
            lastOffset = offsets[i];
            ranges[i].format(appendTo, arguments);
        }
        appendTo.append(pattern, lastOffset, pattern.length());
        return appendTo;
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        return null;
    }

    private void applyPattern(String pattern) {
        StringBuilder[] segments = new StringBuilder[4];
        segments[SEG_RAW] = new StringBuilder();

        int part = SEG_RAW;
        int formatNumber = 0;
        boolean inQuote = false;
        maxOffset = -1;

        for(int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);
            if(part == SEG_RAW) {
                if(ch == '\'') {
                    inQuote = !inQuote;
                } else if(ch == '{' && !inQuote) {
                    part = SEG_START;
                    if(segments[SEG_START] == null) {
                        segments[SEG_START] = new StringBuilder();
                    }
                } else {
                    segments[part].append(ch);
                }
            } else {
                if(inQuote) {
                    if(ch == '\'') {
                        inQuote = false;
                    } else {
                        segments[part].append(ch);
                    }
                } else {
                    switch (ch) {
                        case ';':
                            if(part < SEG_END) {
                                if(segments[++part] == null) {
                                    segments[part] = new StringBuilder();
                                }
                            } else {
                                if(segments[part] == null) {
                                    segments[part] = new StringBuilder();
                                }
                                segments[part].append(ch);
                            }
                            break;
                        case '}':
                            part = SEG_RAW;
                            makeFormat(formatNumber, segments);
                            formatNumber++;
                            // throw away other segments
                            segments[SEG_END] = null;
                            segments[SEG_SEPARATOR] = null;
                            segments[SEG_START] = null;
                            break;
                        default:
                            segments[part].append(ch);
                            break;
                    }
                }
            }
        }
        if (part != SEG_RAW) {
            maxOffset = -1;
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
        this.pattern = segments[0].toString();
    }

    private void makeFormat(int offsetNumber,
                            StringBuilder[] textSegments)
    {
        String[] segments = new String[textSegments.length];
        for (int i = 0; i < textSegments.length; i++) {
            StringBuilder oneseg = textSegments[i];
            segments[i] = (oneseg != null) ? oneseg.toString() : "";
        }

        // resize format information arrays if necessary
        if (offsetNumber >= formats.length) {
            int newLength = formats.length * 2;
            AbstractRange[] newFormats = new AbstractRange[newLength];
            int[] newOffsets = new int[newLength];
            System.arraycopy(ranges, 0, newFormats, 0, maxOffset + 1);
            System.arraycopy(offsets, 0, newOffsets, 0, maxOffset + 1);
            ranges = newFormats;
            offsets = newOffsets;
        }
        int oldMaxOffset = maxOffset;
        maxOffset = offsetNumber;
        offsets[offsetNumber] = segments[SEG_RAW].length();

        // now get the format
        AbstractRange format;
        if(segments[SEG_END].isEmpty() && segments[SEG_SEPARATOR].isEmpty()) {
            Matcher matcher = PATTERN_ONLY_INDEX.matcher(segments[SEG_START]);
            if(!matcher.matches()) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("illegal start index: "
                        + segments[SEG_START]);
            }

            try {
                int startIndex = Integer.parseInt(segments[SEG_START]);
                format = new LengthLimitedElement(startIndex, false);
            } catch (Exception ignored) {
                if(segments[SEG_START].length() == 1) {
                    format = new LengthLimitedElement(0, true);
                } else {
                    try {
                        int endOffset = Integer.parseInt(segments[SEG_START].substring(2));
                        format = new LengthLimitedElement(endOffset, true);
                    } catch (Exception cause) {
                        maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("illegal end offset: "
                                + segments[SEG_START]);
                    }
                }
            }
        } else {
            if(!PATTERN_START_INDEX.matcher(segments[SEG_START]).matches()) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("illegal start index: "
                        + segments[SEG_START]);
            }
            int startIndex;
            try {
                startIndex = Integer.parseInt(segments[SEG_START]);
            } catch (Exception cause) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("illegal start index: "
                        + segments[SEG_START]);
            }

            Matcher matcher = PATTERN_END_INDEX.matcher(segments[SEG_END]);
            if(!matcher.matches()) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("illegal end index: "
                        + segments[SEG_END]);
            }

            int endIndex;
            try {
                endIndex = Integer.parseInt(segments[SEG_END]);
                format = new FixedRange(startIndex, endIndex, segments[SEG_SEPARATOR]);
            } catch (Exception ignored) {
                if(segments[SEG_END].length() == 1) {
                    format = new LengthLimitedRange(startIndex, 0, segments[SEG_SEPARATOR]);
                } else {
                    try {
                        int endOffset = Integer.parseInt(segments[SEG_END].substring(2));
                        format = new LengthLimitedRange(startIndex, endOffset, segments[SEG_SEPARATOR]);
                    } catch (Exception cause) {
                        maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("illegal end offset: "
                                + segments[SEG_END]);
                    }
                }
            }
        }

        ranges[offsetNumber] = format;
    }

    @RequiredArgsConstructor
    private class FixedRange extends AbstractRange {

        private final int start, end;
        private final String separator;

        @Override
        protected void format(@NotNull StringBuffer appendTo, Object[] array) {
            this.checkIndex(array, this.start);
            this.checkIndex(array, this.end);

            boolean appended = false;
            for(int i = start; i <= end; ++i) {
                Object elem = array[start];
                if(appended) {
                    appendTo.append(this.separator);
                }
                this.format(appendTo, elem);
                appended = true;
            }
        }
    }

    @RequiredArgsConstructor
    private class LengthLimitedElement extends AbstractRange {

        private final int element;
        private final boolean reverse;

        @Override
        protected void format(@NotNull StringBuffer appendTo, Object[] array) {
            if(this.reverse) {
                this.checkIndex(array, array.length - this.element - 1);
                this.format(appendTo, array[array.length - this.element - 1]);
            } else {
                this.checkIndex(array, this.element);
                this.format(appendTo, array[this.element]);
            }
        }
    }

    @RequiredArgsConstructor
    private class LengthLimitedRange extends AbstractRange {

        private final int start, endOffset;
        private final String separator;

        @Override
        protected void format(@NotNull StringBuffer appendTo, Object[] array) {
            this.checkIndex(array, this.start);
            this.checkIndex(array, array.length - this.endOffset - 1);

            boolean appended = false;
            for(int i = start; i <= array.length - endOffset - 1; ++i) {
                Object elem = array[i];
                if(appended) {
                    appendTo.append(this.separator);
                }
                this.format(appendTo, elem);
                appended = true;
            }
        }
    }

    private abstract class AbstractRange {

        protected abstract void format(@NotNull StringBuffer appendTo, Object[] array);

        protected void checkIndex(@NotNull Object[] array, int index) {
            if(index < 0 || index >= array.length)
                throw new IndexOutOfBoundsException("Index out of range: " + index + " in " + Arrays.toString(array));
        }

        protected void format(@NotNull StringBuffer appendTo, @Nullable Object obj) {
            if(obj == null) {
                appendTo.append("null");
            } else if(obj instanceof String) {
                appendTo.append((String) obj);
            } else if(obj instanceof Number) {
                if(formats[FORMAT_NUMBER] == null) {
                    formats[FORMAT_NUMBER] = NumberFormat.getInstance(locale);
                }
                appendTo.append(formats[FORMAT_NUMBER].format(obj));
            } else if(obj instanceof Date) {
                if(formats[FORMAT_DATE] == null) {
                    formats[FORMAT_DATE] = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
                }
                appendTo.append(formats[FORMAT_DATE].format(obj));
            } else {
                String str = obj.toString();
                if(str == null) {
                    appendTo.append("null");
                } else {
                    appendTo.append(str);
                }
            }
        }
    }
}
