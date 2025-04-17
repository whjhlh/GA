package com.whj.generate.utill;


import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * @author whj
 * @date 2025-02-22 下午9:11
 */
public class StringUtil {
    /**
     * 判断是否小写
     *
     * @param input
     * @return
     */
    public static boolean isLowerCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        boolean hasLetters = false;
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isLowerCase(cp)) {
                    return false;
                }
            }
        }
        return hasLetters;
    }

    /**
     * 判断是否大写
     *
     * @param input
     * @return
     */
    public static boolean isUpperCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        boolean hasLetters = false;
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                hasLetters = true;
                if (!Character.isUpperCase(cp)) {
                    return false;
                }
            }
        }
        return hasLetters;
    }

    /**
     * 判断是否混合大小写
     *
     * @param input
     * @return
     */
    public static boolean isMixedCase(final String input) {
        if (input.isEmpty() || input.codePointCount(0, input.length()) < 2) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        for (int i = 0, cp; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (Character.isLetter(cp)) {
                // Don't count the first cp as upper to allow for title case
                if (Character.isUpperCase(cp) && i > 0) {
                    hasUpper = true;
                } else if (Character.isLowerCase(cp)) {
                    hasLower = true;
                }
                if (hasUpper && hasLower) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是首字母大写
     *
     * @param input
     * @return
     */
    public static boolean isTitleCase(final String input) {
        if (input.isEmpty()) {
            return false;
        }
        if (input.codePointCount(0, input.length()) > 1) {
            return isTitleCase(input.codePointAt(0)) && isLowerCase(input.substring(input.offsetByCodePoints(0, 1)));
        } else {
            return isTitleCase(input.codePointAt(0));
        }
    }

    /**
     * 判断是否是首字母大写
     *
     * @param codePoint
     * @return
     */
    public static boolean isTitleCase(int codePoint) {
        // True if is actual title case, or if is upper case and has no separate title case variant.
        return Character.isTitleCase(codePoint)
                || (Character.isUpperCase(codePoint) && Character.toTitleCase(codePoint) == codePoint);
    }

    /**
     * 判断是否是空白
     *
     * @param str
     * @return
     */
    public static boolean isNotBlank(final String str) {
        if (null == str && str.isEmpty()) {
            return false;
        }
        for (int i = 0, cp; i < str.length(); i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            if (!isBlank(String.valueOf(cp))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个字符串是否相等
     */
    public static boolean equals(final String a, final String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }


    /**
     * 替换
     *
     * @param name
     * @param str
     * @param ch
     * @return
     */
    public static String replace(String name, String str, String ch) {
        if (StringUtil.isNotBlank(name) && name.contains(str)) {
            return name.replace(str, ch);
        }
        return name;
    }
}
