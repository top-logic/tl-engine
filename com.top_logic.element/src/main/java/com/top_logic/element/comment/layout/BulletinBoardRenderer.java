/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class BulletinBoardRenderer implements Renderer<String>, HTMLConstants {

	private static final String STRING_WRITER_IO_EXCEPTION = "StringWriter does not throw IOException";

	private static final String PROTOCOL = "protocol";

	public static final BulletinBoardRenderer INSTANCE = new BulletinBoardRenderer();

	private static final String PROTOCOL_PATTERN = "(?<protocol>(?i:http|https|rtsp|ftp|file)://)";

	private static final String DIRECT_LINK = "(?i:www)\\.";

	private static final String LINK_CONTENT = "[-a-zA-Z0-9+&@#/\\\\%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/\\\\%=~_|]";

	private static final String WORD_BOUNDARY = "(?:\\b|$|^)";

	private static final String LINK_PATTERN =
		"(" + "(?:" + PROTOCOL_PATTERN + LINK_CONTENT + ")" + "|" + "(?:" + DIRECT_LINK + LINK_CONTENT + ")" + WORD_BOUNDARY
			+ ")";

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aWriter, String aValue) throws IOException {
		String theContent = aValue;

        theContent = TagUtil.encodeXML(theContent);
        theContent = theContent.replaceAll("[\r]?\n", "<br />");
        theContent = this.handleBB(theContent);
		theContent = handleLinks(theContent);

        aWriter.writeContent(theContent);
    }

    protected String handleBB(String aContent) {
        int theQuoteStart = aContent.indexOf("[quote");
        int theQuoteEnd   = aContent.lastIndexOf("[/quote]");
       
        if ((theQuoteStart >= 0) && theQuoteEnd > theQuoteStart) {
            int    theQuoteStartlength = aContent.substring(theQuoteStart).indexOf("]") + 1;
            int    theQuoteStartEnd    = theQuoteStart + theQuoteStartlength;
            String theQuote            = aContent.substring(theQuoteStart, theQuoteStartEnd);
            String theQuotedAuthorName = this.getAuthorNameFromQuote(theQuote);
            String theFirstPart        = aContent.substring(0, theQuoteStart);
            String theInnerPart        = aContent.substring(theQuoteStartEnd, theQuoteEnd);
            String theEndPart          = aContent.substring(theQuoteEnd + "[/quote]".length());

			StringWriter out = new StringWriter();
			try {
				TagWriter t = new TagWriter(out);

				t.writeContent(theFirstPart);
				t.beginBeginTag(DIV);
				t.writeAttribute(CLASS_ATTR, "quotedComments");
				t.endBeginTag();
				t.beginTag(BOLD);
				t.writeContent(Resources.getInstance().getString(I18NConstants.QUOTETEXT) + " " + theQuotedAuthorName);
				t.endTag(BOLD);
				t.beginBeginTag(TABLE);
				t.writeAttribute(CELLSPACING_ATTR, "0");
				t.writeAttribute(CELLPADDING_ATTR, "10");
				t.writeAttribute(WIDTH_ATTR, "100%");
				t.writeAttribute(BORDER_ATTR, "1");
				t.endBeginTag();
				t.beginTag(TR);
				t.beginTag(TD);
				t.writeContent("  " + theInnerPart);
				t.endTag(TD);
				t.endTag(TR);
				t.endTag(TABLE);
				t.endTag(DIV);
				t.writeContent("      " + theEndPart);

				t.close();
			} catch (IOException ex) {
				throw new UnreachableAssertion(STRING_WRITER_IO_EXCEPTION);
			}

			aContent = this.handleBB(out.toString());
        }
        
        return aContent;
    }
    
    private String getAuthorNameFromQuote(String aQuote) {
        try {
            int theStart = aQuote.indexOf("=") + 1;
            int theEnd   = aQuote.indexOf("]");

            if (theStart > 0 && theEnd > theStart) {
                return aQuote.substring(theStart, theEnd);
            }
        }
        catch(Exception ex) {
            Logger.error("Unable to get Authorname from quote tag", ex, this);
        }

        return "";
    }

	/**
	 * Scans the given text for links (e.g. http://www.google.com) with regular expression and replaces
	 * the links with clickable links.
	 *
	 * @param content
	 *        the content to scan for links
	 * @return the content with the links replaced with clickable links, opening the link in a different
	 *         window
	 */
	protected String handleLinks(String content) {
		int index = 0;
		Pattern pattern = Pattern.compile(LINK_PATTERN);

		StringWriter out = new StringWriter();

		try {
			TagWriter t = new TagWriter(out);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String link = matcher.group();

				t.writeContent(content.substring(index, matcher.start()));
				t.beginBeginTag(ANCHOR);
				t.beginAttribute(HREF_ATTR);

				if (matcher.group(PROTOCOL) == null) {
					t.writeAttributeText(HTTP_VALUE + link);
				} else {
					t.writeAttributeText(link);
				}

				t.endAttribute();
				t.writeAttribute(TARGET_ATTR, BLANK_VALUE);
				t.writeAttribute(CLASS_ATTR, "autoLink");
				t.endBeginTag();
				t.writeContent(link);
				t.endTag(ANCHOR);

				index = matcher.end();
			}

			t.writeContent(content.substring(index));
			t.close();
		} catch (IOException ex) {
			throw new UnreachableAssertion(STRING_WRITER_IO_EXCEPTION);
		}

		return out.toString();
	}

}

