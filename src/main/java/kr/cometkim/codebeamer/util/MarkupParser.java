package kr.cometkim.codebeamer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by comet on 2016-07-25.
 */
public class MarkupParser {
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\\\|\n|\r\n");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern MODIFICATION_TAG_PATTERN = Pattern.compile("<span class=\"diff-added-inline\">|<span class=\"diff-deleted-inline\">|<\\/span>");

    public static String replaceLineBreak(String origin, String replacement) {
        return replaceByPattern(LINE_BREAK_PATTERN, origin, replacement, false);
    }

    public static String replaceLineBreaks(String origin, String replacement){
        return replaceByPattern(LINE_BREAK_PATTERN, origin, replacement, true);
    }

    public static String replaceHtmlTag(String origin, String replacement){
        return replaceByPattern(HTML_TAG_PATTERN, origin, replacement, false);
    }

    public static String replaceHtmlTags(String origin, String replacement){
        return replaceByPattern(HTML_TAG_PATTERN, origin, replacement, true);
    }

    public static String replaceModificationTags(String origin, String replacement){
        return replaceByPattern(MODIFICATION_TAG_PATTERN, origin, replacement, true);
    }

    private static String replaceByPattern(Pattern pattern, String origin, String replacement, boolean global){
        String replaced = origin;
        Matcher matcher = pattern.matcher(origin);
        if(matcher.find()){
            replaced = global ? matcher.replaceAll(replacement) : matcher.replaceFirst(replacement);
        }
        return replaced;
    }
}
