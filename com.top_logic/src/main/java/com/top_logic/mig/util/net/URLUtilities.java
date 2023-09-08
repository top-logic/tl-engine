/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.util.net;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * The URLUtilities contains some useful classes to handle URLs.
 * @author <a href="mailto:jochen.hiller@top-logic.com"">Jochen Hiller</a>
 */
public class URLUtilities {

	/**
	 * The standard encoding of URLs.
	 * <p>
	 * See the standard for details:
	 * <a href= "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">World Wide Web
	 * Consortium Recommendation: Non-ASCII characters in URI attribute values</a>
	 * </p>
	 */
	public static final String URL_ENCODING = LayoutConstants.UTF_8;

	/** random generator to make URIs unique (handle caching problem of browsers, proxies, rtc.) */
	private static final Random RANDOM_GENERATOR = new Random();

    /**
     * Returns the Symbol that is used to concat a URL Parameter to a URL.
     * @param anURL the URL to concat to.
     * @return String the symbol.
     */
    public static char getConcatSymbol(String anURL) {
        return anURL.indexOf('?') < 0 ? '?' : '&';
        // using &amp; is only needed in _some_ contexts 
    }
    
    /**
     * Return the Symbol that is used to concat an URL Parameter to an URL
     * @param anURLBuffer a StringBuffer to append to.
     * @return the Symbol
     */
    public static char getConcatSymbol (StringBuffer anURLBuffer) {
    	return anURLBuffer.indexOf("?") < 0 ? '?' : '&';
    }
    
    /**
     * Returns the URL with a random parameter added, to assure browsers and
     * webservers will not cache but reload it.
     * @param aURL the original URL
     * @return String the original URL with an added parameter of radom value or
     * <code>null</code> if the original URL was <code>null</code>.
     */
    public static String getURLWithForceReload(String aURL) {
        if (aURL == null) {
            return null;
        }
        return aURL
            + URLUtilities.getConcatSymbol(aURL)
            + "vwxyz42=" + System.currentTimeMillis();
    }

    /**
     * apends a ttribut to URL making the url unique.
     * 
     * this ensures browsers and proxies will surely reload a url 
     * instead sending it out from their caches.
     * 
     * @param url the url to be mainulated
     * @return the manipulated url
     */
    public static String appendNoCacheAttr(String url) {        
        return url + URLUtilities.getConcatSymbol(url) + "nocache=" + RANDOM_GENERATOR.nextInt();
    }

	/**
	 * Appends an URL parameter to the given URL.
	 * 
	 * <p>
	 * Note: Before the first parameter, a '?' character is appended to the given URL. Before each
	 * following parameter, a '&amp;' character is appended
	 * </p>
	 * 
	 * <p>
	 * With <code>first = true</code>, <code>paramName = "foo"</code>, and
	 * <code>value = "bar"</code>, the following content is appended to the given URL:
	 * <code>?foo=bar</code>.
	 * </p>
	 * 
	 * <p>
	 * Parameter names and values are properly URL encoded.
	 * </p>
	 * 
	 * @param urlBuilder
	 *        The URL being built.
	 * @param first
	 *        Whether this call potentially will append the first parameter.
	 * @param paramName
	 *        The name of the parameter.
	 * @param value
	 *        The value of the parameter. If the value is <code>null</code>, the parameter is not
	 *        appended.
	 * @return Whether the next parameter is still the first one.
	 */
	public static boolean appendUrlArg(StringBuilder urlBuilder, boolean first, String paramName, String value) {
		if (value != null) {
			appendParamName(urlBuilder, first, paramName);
			appendUrlEncoded(urlBuilder, value);
			return false;
		} else {
			return first;
		}
	}

	/**
	 * Appends the parameter assignment part of
	 * {@link #appendUrlArg(StringBuilder, boolean, String, String)} to the given URL.
	 */
	public static void appendParamName(StringBuilder urlBuilder, boolean first, String paramName) {
		if (first) {
			urlBuilder.append('?');
		} else {
			urlBuilder.append('&');
		}

		appendUrlEncoded(urlBuilder, paramName);
		urlBuilder.append('=');
	}

	/**
	 * Appends the given value URL encoded to the given builder.
	 * 
	 * @param urlBuilder
	 *        The URL being built.
	 * @param value
	 *        The value to append.
	 */
	public static void appendUrlEncoded(StringBuilder urlBuilder, String value) {
		urlBuilder.append(URLUtilities.urlEncode(value));
	}

	/**
	 * URL encode the given string using {@link LayoutConstants#UTF_8} character encoding.
	 * 
	 * <p>
	 * Only for encoding URL parameters not for the path.
	 * </p>
	 * 
	 * @param s
	 *        The string to encode.
	 * @return The encoded string.
	 * 
	 * @see #urlDecode(String)
	 */
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, URL_ENCODING);
		} catch (UnsupportedEncodingException ex) {
			throw new UnreachableAssertion(URL_ENCODING + " encoding is supported by default.", ex);
		}
	}

	/**
	 * Decodes the given URL encoded string.
	 * 
	 * @param s
	 *        The string to decode.
	 * @return The decoded string.
	 * 
	 * @see #urlEncode(String)
	 */
	public static String urlDecode(String s) {
		try {
			URI uri = new URI(s);
			return uri.getPath();
		} catch (URISyntaxException ex) {
			throw new RuntimeException("Building URI for '" + s + "' failed.", ex);
		}
	}
}
