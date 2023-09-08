/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.xml.TagWriter;

/**
 * Formatter that translates wiki text to HTML fragments.
 * 
 * <p>
 * The wiki format understood by this formatter is similar to the MediaWiki format. The following
 * formattings are understood:
 * </p>
 * 
 * <dl>
 * <dt>Paragraphs</dt>
 * <dd>Separated by one ore more blank lines.</dd>
 * 
 * <dt>Hard line breaks</dt>
 * <dd>Single line breaks (lines separated by a single newline character but without interleaving
 * blank lines) are translated to hard HTML line breaks using <code>br</code> elements.</dd>
 * 
 * <dt>Unordered lists</dt>
 * <dd>A list item is prefixed with a <code>*</code> or <code>-</code> character. A nested list is
 * marked with multiple list markers at the beginning of the line.
 * 
 * <p>
 * The text
 * </p>
 * 
 * <pre>
 * * A
 * * B
 * ** C
 * ** D
 * </pre>
 * 
 * <p>
 * is interpreted as the list
 * </p>
 * 
 * <ul>
 * <li>A</li>
 * <li>B
 * <ul>
 * <li>C</li>
 * <li>D</li>
 * </ul>
 * </li>
 * </ul>
 * </dd>
 * </dl>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WikiWrite {

	private static final String UL_START = "<ul class=\"wikiText\">";
	private static final String UL_STOP = "</ul>";

	private static final String LI_START = "<li>";
	private static final String LI_STOP = "</li>";

	private static final String P_START = "<p class=\"wikiText\">";
	private static final String P_STOP = "</p>";

	private static final String BR = "<br/>";

	private static final String NL_EXPR = "(?:\\r\\n|\\n|\\r(?!\\n))";

	/**
	 * Pattern matching Linux (<code>\n</code>), Windows (<code>\r\n</code>) and Mac (<code>\r</code>) line breaks.
	 */
	public static final Pattern NEWLINE_PATTERN =
		Pattern.compile(NL_EXPR, Pattern.MULTILINE);

	private static final String SPACE_EXPR = "[ \\t\\x0B\\f]";
	
	private static final Pattern P_PATTERN = 
		Pattern.compile(NL_EXPR + "(?:" + SPACE_EXPR + "*" + NL_EXPR + ")+", Pattern.MULTILINE);
	
	private static final String DEFAULT_LI_PATTERN = "[\\*\\-]";
	private static Pattern LI_PATTERN; 
	static {
	    String pattern;
		try {
			pattern = Configuration.getConfiguration(WikiWrite.class).getValue("liPattern", DEFAULT_LI_PATTERN);
		} catch (ConfigurationError ex) {
			pattern = DEFAULT_LI_PATTERN;
		}
		LI_PATTERN =
			Pattern.compile(SPACE_EXPR + "*" + "(" + pattern + "+" + ")" + SPACE_EXPR + "+", Pattern.MULTILINE);
	}


	/**
	 * Translates the given wiki text into a HTML fragment that is written to
	 * the given writer.
	 */
	public static void wikiWrite(TagWriter out, String value) throws IOException {
		int length = value.length();
		Matcher pMatcher = WikiWrite.P_PATTERN.matcher(value);
		Matcher brMatcher = WikiWrite.NEWLINE_PATTERN.matcher(value);
		Matcher liMatcher = WikiWrite.LI_PATTERN.matcher(value);
		int pStart = 0;
		do {
			int pStop;
			boolean hasP = pMatcher.find(pStart);
			if (hasP) {
				pStop = pMatcher.start();
			} else {
				pStop = length;
			}
	
			if (pStop > pStart) {
				int liDepth = 0;
				out.writeContent(P_START);
				// Have paragraph contents from pStart to pStop.
				int lStart = pStart;
				do {
					int lStop;
					boolean hasL = brMatcher.find(lStart);
					if (hasL) {
						lStop = Math.min(pStop, brMatcher.start());
					} else {
						lStop = pStop;
					}
	
					if (lStart < lStop) {
						// Have line contents from lStart to lStop.
						liMatcher.region(lStart, lStop);
						
						int newLiDepth;
						if (liMatcher.lookingAt()) {
							newLiDepth = liMatcher.group(1).length();
							lStart = liMatcher.end();
						} else {
							newLiDepth = 0;
						}
						
						if (newLiDepth == liDepth && newLiDepth > 0) {
							// No change in depth and within list.
							out.writeContent(LI_STOP);
							out.writeContent(LI_START);
						} else if (newLiDepth < liDepth) {
							// Depth decreased, was in list before.
							while (liDepth > newLiDepth) {
								out.writeContent(LI_STOP);
								out.writeContent(UL_STOP);
								liDepth--;
							}
							if (newLiDepth > 0) {
								out.writeContent(LI_STOP);
								out.writeContent(LI_START);
							}
						} else if (newLiDepth > liDepth) {
							while (liDepth < newLiDepth) {
								out.writeContent(UL_START);
								out.writeContent(LI_START);
								liDepth++;
							}
						} else {
							if (lStart > pStart) {
								// Not first line of paragraph and not first line after list.
								out.writeContent(BR);
							}
						}
						
						out.writeText(value.substring(lStart, lStop));
					}
					
					if (hasL) {
						lStart = Math.min(pStop, brMatcher.end());
					} else {
						lStart = pStop;
					}
				} while (lStart < pStop);
				
				while (liDepth > 0) {
					out.writeContent(LI_STOP);
					out.writeContent(UL_STOP);
					liDepth--;
				}
				out.writeContent(P_STOP);
			}
			
			if (hasP) {
				pStart = pMatcher.end();
			} else {
				pStart = length;
			}
		} while (pStart < length);
	}

}
