/*
 * Copyright (C) 2014  Batav B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cafienne.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Simple Utilities to have universal encoding across the breath of the code,
 * instead of each time passing the UTF8 charset
 */
public class URLUtil {
    public static final Charset DEFAULT_CHAR_SET = StandardCharsets.UTF_8;

    /**
     * Simple wrapper around URL encoding. Uses the UTF8 character set, and wraps/ignores
     * the possible exception if this character encoding is not supported.
     *
     * @param path The url path to encode
     */
    public static String encode(String path) {
        return encode(path, DEFAULT_CHAR_SET);
    }

    /**
     * Simple wrapper around URL encoding with the given character set.
     *
     * @param path The url path to encode
     * @param charset The character set to use for the encoding
     */
    public static String encode(String path, Charset charset) {
        return URLEncoder.encode(path, charset);
    }

    /**
     * Simple wrapper around URL decoding. Uses the UTF8 character set, and wraps/ignores
     * the possible exception if this character encoding is not supported.
     *
     * @param path The url path to decode
     */
    public static String decode(String path) {
        return decode(path, DEFAULT_CHAR_SET);
    }

    /**
     * Simple wrapper around URL decoding with the given character set.
     *
     * @param path The url path to decode
     * @param charset The character set to use for the encoding
     */
    public static String decode(String path, Charset charset) {
        return URLDecoder.decode(path, charset);
    }
}
