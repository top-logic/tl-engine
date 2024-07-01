/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.w3c.dom.Node;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.character.NonClosingProxyWriter;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * Static utility functions related to HTML in some way.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class HTMLUtil {

    public static final String NBSP = HTMLConstants.NBSP;

    private static final String TITLE_TAG_L = "<title>";

    private static final String TITLE_TAG_U = "<TITLE>";
    
    private static final int    TITLE_LEN = TITLE_TAG_L.length();

    /** The separator that separates parameters in an URL-encoded String */
    public static final String ASSIGNMENT_SEP = "=";

    /** The separator that separates name/value pairs in an URL-encoded String.  */
    public static final String PARAM_SEP    = "&";

    /** The separator that separates the base URL from the encoded Parameters */
    public static final String URL_SEP      = "?";

    /** The separator that separates hierarchical names (extension to standard URL-encoding) */
    public static final String STRUCT_SEP   = ".";

	/** constants to be use with class parameters */
	public static final String NOCLASS = null;

	/**
	 * @see #beginSelect(TagWriter, String, String, String, int, boolean)
	 */
	public static final boolean MULTI_SELECT = true;

	/**
	 * @see #beginSelect(TagWriter, String, String, String, int, boolean)
	 */
	public static final boolean SINGLE_SELECT = false;

	/** constants to be use with {@link #writeOption(TagWriter, String, String, boolean)} */
	public static final boolean SELECTED = true;

	/** constants to be use with {@link #beginTd(TagWriter, String, String, int, int)} */
	public static final String TD_ALIGN_NONE = null;

	/** constants to be use with {@link #beginTd(TagWriter, String, String, int, int)} */
	public static final String TD_VALIGN_NONE = null;

	private static final long TIMESTAMP = System.currentTimeMillis();

	/**
	 * Configuration for {@link HTMLUtil}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * Whether script reload must be forced. See {@link Config#getForceScriptReload}.
		 */
		String FORCE_SCRIPT_RELOAD = "forceScriptReload";

		/** Getter for {@link Config#FORCE_SCRIPT_RELOAD}. */
		@Name(FORCE_SCRIPT_RELOAD)
		@BooleanDefault(false)
		boolean getForceScriptReload();
	}

	/**
	 * Getter for the configuration.
	 */
	public static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#FORCE_SCRIPT_RELOAD}.
	 */
	public static boolean forceScriptReload() {
		return getConfig().getForceScriptReload();
	}

	/**
	 * Generates suffix that forces script reload.
	 */
	public static String getReloadSuffix() {
		return forceScriptReload() ? "?t=" + TIMESTAMP : "";
	}

	/**
     * Function to extract a text, until the end of a Tag has been reached.
	 * 
	 * @param    aReader    The reader where some HTML Tag was found.
	 * @param    aLine      Rest of the line where the start of the tag was found.
     * @return   The string from the reader till the end tag has been found.
	 */ 
    public static String findEndTag(BufferedReader aReader, String aLine) throws IOException {
        int           thePos;
        StringBuilder theResult = new StringBuilder(64);

    	do {
        	int theLen = aLine.length();

        	if (theLen > 0) {
                theResult.append(aLine);

                if (Character.isLetterOrDigit(aLine.charAt(theLen - 1))) {
                    theResult.append(' ');
                }
                // So "<title> Blah\nSchwafel"
                // Will give "Blah Schwafel" instead of "Blah Schwafel"
        	}
            aLine = aReader.readLine();

            if (aLine == null) {// no end Tag ?
                return null;
            }

            thePos = aLine.indexOf('<');
	    } while (thePos < 0);
        
        theResult.append(aLine.substring(0, thePos));

        return (theResult.toString().trim());
    }

	/**
	 * Extract the contents of the &lt;title&gt; tag form a (potential) HTML document.
	 * 
	 * @param aReader
	 *        The reader to get the data from.
	 * @return The Title or <code>null</code> in case of an exception or no title could be found.
	 */
	public static String extractTitle(Reader aReader) {
        String         theTitle  = null;
        BufferedReader theReader = (aReader instanceof BufferedReader) ? (BufferedReader) aReader
                                                                       : new BufferedReader(aReader);

 		try {
		   	String theLine = theReader.readLine();

		    while (theTitle == null && theLine != null) {
		        int theStart = theLine.indexOf(TITLE_TAG_L);

		        if (theStart < 0) {
                    theStart = theLine.indexOf(TITLE_TAG_U);
                }

		        if (theStart >= 0) {
	       	        theStart += TITLE_LEN;
	       	        int theEnd = theLine.indexOf('<', theStart);

                    if (theEnd > 0) {
                        return theLine.substring(theStart, theEnd).trim();
                    }
                    else { // Mhh, multi Line title
                        theTitle = findEndTag(theReader, theLine.substring(theStart));
                    }
		        }

		        theLine = theReader.readLine();
		    }
		} 
        catch (IOException ex) {
			// can't help it ...
		}  
        finally {
            theReader = (BufferedReader) StreamUtilities.close(theReader);
        }     
        return (theTitle);
	}
    
    /** 
     * Write a String replacing \n and/or \r by &lt;br&gt;.
     * 
     * @param    aString    The string to be written.
     * @param    aWriter    The writer to write the string to.
     * @throws   IOException    If writing fails for a reason.
     */
    public static void writeBRforNewline(String aString, Writer aWriter) throws IOException {
        int     theLength = aString.length();

        for (int thePos=0; thePos < theLength; thePos++) {
            char theChar = aString.charAt(thePos);

            if (theChar == '\n') {
                    aWriter.write("<br />");
            } else if (theChar == '\r') {
                //ignore as there is always a \n with it
                //well ... it is not, as kha said ...
            } else {
                aWriter.write(theChar);
            }
        }
    }

    /**
     * Replace all \n in the String with &lt;br&gt;
     * and removes combined \r
     * 
     * @param aString the original string
     * @return the string with all (\r)\n replaced (null in case of an io exception).
     */
    public static String encodeBRforNewLine(String aString) {
    	StringWriter theWriter = new StringWriter();
    	try {
			writeBRforNewline(aString, theWriter);
			return theWriter.toString();
		} catch (IOException e) {
			return null;
		}
    }
    
    /**
     * Quotes a string for a JS output.
     *
     * @param aString
     *        the string to quote
     * @return the quoted string
     */
    public static String encodeJS(String aString) {
        if (StringServices.isEmpty(aString)) {
            return ("");
        }
        int length = aString.length();
        StringBuilder sb = new StringBuilder(length + 32);
        for (int i = 0; i < length; i++) {
            char c = aString.charAt(i);
            switch (c) {
            case '"':
            case '\'':
            case '\\':
                sb.append('\\').append(c); break;
            case '\n':
                sb.append("\\n"); break;
            case '\t':
                sb.append("\\t"); break;
            case '\r':
            	sb.append("\\r"); break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 
     * The exact sematics of this function is a mystery, yet to be solved.
     * 
     * @return true when chances are bad that this string will work as an url 
     */
    public static boolean isEvilURL(String anURL) {
        // for now this will do ...
        int len = anURL.length();
        for (int i=0; i < len; i++) {
            char c = anURL.charAt(i);
            if (c == '.')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '/')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '_')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '-')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '#')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '+')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (c == '%')  { // part of filenames and not known to be evil :-)
                continue;
            }
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }   
        return false;
    }

    /**
     * this is just a help method for the parseURLParams. It is used when the
     * a url is of the form: param.subparam=v1 which means the value of
     * the attribute is a structure. This is a recursiv call .. so it can have
     * any depth: p.s1.s2.s3....sn=v
     */
    protected static void addToInnerMap(Map aTable, String stString, String aValue)
    {
        int pos = stString.indexOf(STRUCT_SEP);
        String stName  = stString.substring(0, pos);
        String rest = stString.substring(pos +1, stString.length());
        //check if there is still an inner structure
        // in this case add a map to the inner map etc
        int next = rest.indexOf(STRUCT_SEP);
        if (next < 0) {
            if (aTable.containsKey(stName)) {
                ((Hashtable)aTable.get(stName)).put(rest, aValue);
            }
            else
            {
                Hashtable inside = new Hashtable();
                inside.put(rest, aValue);
                aTable.put(stName, inside);
            }
        }
        else
        {
            if (aTable.containsKey(stName)) {
                Hashtable rec = (Hashtable)aTable.get(stName);
                addToInnerMap(rec, rest, aValue);
            }
            else
            {
                Hashtable aNewInnerMap = new Hashtable();
                aTable.put(stName,aNewInnerMap);
                addToInnerMap (aNewInnerMap, rest, aValue);
            }
        }
    }
        
    /**
	 * Parse a string like <code>xx=yy&amp;zz=kk</code> into a {@link Map} of strings.
	 * 
	 * The parameter list can also indicate an inner structure for the attribute thus if a parameter
	 * of the form: x.y.z=v is encountered, a map with inner maps is returned that reflect the exact
	 * form of the structure.
	 */
    public static Map parseURLParams(String paramList) {
        Map             resultTable = new HashMap();
        StringTokenizer tok         = new StringTokenizer (paramList, PARAM_SEP);
        while (tok.hasMoreTokens()) {
            String  keyValuePair = tok.nextToken ();
            int eqPos = keyValuePair.indexOf(ASSIGNMENT_SEP);
            if (eqPos > 0) {
                String param = keyValuePair.substring(0,eqPos);
                   String value = keyValuePair.substring(eqPos + 1, keyValuePair.length());
                   //check if the attribute is a structure or not
                   //in this case add an inner map and store the attribute value
                   // in the right place
                   if (param.indexOf(STRUCT_SEP) > 0) {
                       addToInnerMap (resultTable, param, value);
                   }
                   else {
                       resultTable.put(param, value);
                   }
            }
        }

        return resultTable;
    }
    
    /** 
     * Extract Parameter aParam from aReq and validate that is is a valid JS-Identifier.
     */
    public static String validJSName(HttpServletRequest aReq, String aParam) throws ServletException {
        String rawValue = aReq.getParameter(aParam);
        int len;
        if (rawValue == null || 0 == (len = rawValue.length())) {
            throw new ServletException("Illegal empty value for '" + aParam + "'");
        }
        if (!Character.isJavaIdentifierStart(rawValue.charAt(0))) {
            throw new ServletException("Illegal value for '" + aParam + "'");
        }
        for (int i = 1; i < len; i++) {
            if (!Character.isJavaIdentifierPart(rawValue.charAt(i)))
                throw new ServletException("Illegal value for '" + aParam + "'");
        }
        return rawValue;
    }

    /**
     * Return the locale defined in the header field "accept-language" of the given request.
     *
     * This field will be filled by the browser automatically.
     * 
     * @param    aRequest    The request to get the locale from, may be <code>null</code>.
     * @return   The found locale or the default locale in case of any error.
     */
    public static Locale getLocale(HttpServletRequest aRequest) {
		return Resources.getAcceptLanguageLocale((aRequest == null) ? null : aRequest.getHeader("accept-language"));
    }

    /**
     * Appends a style part to another style.
     *
     * @param aStyle
     *        the style which shall get extended with another part
     * @param aAdditionalStyle
     *        the part to append to the style
     * @return the given style extended with the given additional style part
     */
    public static String appendStyle(String aStyle, String aAdditionalStyle) {
        if (StringServices.isEmpty(aStyle)) return aAdditionalStyle;
        aStyle = aStyle.trim();
        if (aStyle.charAt(aStyle.length() - 1) != ';') {
            aStyle += "; ";
        }
        return aStyle + aAdditionalStyle + ';';
    }

	/**
	 * Appends the given CSS class to the given buffer of classes.
	 * 
	 * @param cssClasses
	 *        The buffer of CSS classes to add to.
	 * @param newClass
	 *        The new class to add to the given buffer.
	 */
	public static void appendCSSClass(StringBuilder cssClasses, String newClass) {
		if (cssClasses.length() > 0) {
	    	cssClasses.append(' ');
	    }
	    cssClasses.append(newClass);
	}

	/**
	 * Appends the given CSS class to the given stream.
	 * 
	 * @param out
	 *        The stream to write to.
	 * @param cssClass
	 *        The class name to append. In case of empty/null nothing is appended.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void appendCSSClass(Appendable out, String cssClass) throws IOException {
		if (!StringServices.isEmpty(cssClass)) {
			out.append(cssClass);
		}
	}

	/**
	 * Writes a link to a stylesheet file: &lt;link rel=&quot;stylesheet&quot;
	 * type=&quot;text/css&quot; href=&quot;src&quot;/&gt;
	 * 
	 * @param out
	 *        the writer to write to
	 * @param src
	 *        the path to the stylesheet file.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeStylesheetRef(TagWriter out, String contextPath, String src) throws IOException {
		out.beginBeginTag(LINK);
		out.writeAttribute(REL_ATTR, STYLESHEET_REL_VALUE);
		out.writeAttribute(TYPE_ATTR, CSS_TYPE_VALUE);
		{
			out.beginAttribute(HREF_ATTR);
			out.writeAttributeText(contextPath);
			out.writeAttributeText(src);
			out.endAttribute();
		}
		out.endEmptyTag();
	}

	/**
	 * Writes a JavaScript tag with the given <code>src</code> as source to the given
	 * {@link TagWriter}
	 * 
	 * @param writer
	 *        the Tagwriter to write to
	 * @param src
	 *        the source of the {@link HTMLConstants#SCRIPT_REF} tag.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeJavascriptRef(TagWriter writer, String contextPath, String src) throws IOException {
		writeJavaScriptRef(writer, contextPath, src, getReloadSuffix());
	}

	/**
	 * Writes a JavaScript tag with the given <code>src</code> as source to the given
	 * {@link TagWriter}
	 * 
	 * @param writer
	 *        the Tagwriter to write to
	 * @param src
	 *        the source of the {@link HTMLConstants#SCRIPT_REF} tag.
	 * @param reloadSuffix
	 *        The suffix to append to the URL to force reload.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeJavaScriptRef(TagWriter writer, String contextPath, String src, String reloadSuffix)
			throws IOException {
		writePlainJavaScriptRef(writer, contextPath, src, reloadSuffix);
	}

	/**
	 * Writes a JavaScript tag with the given <code>src</code> as source to the given
	 * {@link TagWriter}
	 * 
	 * @param writer
	 *        the Tagwriter to write to
	 * @param src
	 *        the source of the {@link HTMLConstants#SCRIPT_REF} tag.
	 * @param reloadSuffix
	 *        The suffix to append to the URL to force reload.
	 * @param type
	 *        Specifies the type of the script resource, for instance <code>module</code> if native
	 *        javascript modules are used in the given script.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeJavaScriptRef(TagWriter writer, String contextPath, String src, String reloadSuffix,
			String type) throws IOException {
		writePlainJavaScriptRef(writer, contextPath, src, reloadSuffix, type);
	}

	/**
	 * Writes a JavaScript tag with the given <code>src</code> as source to the given
	 * {@link TagWriter}.
	 * 
	 * <p>
	 * Note: In contrast to {@link #writeJavascriptRef(TagWriter, String, String)}, this method does
	 * not append a timestamp to the URL.
	 * </p>
	 * 
	 * @param writer
	 *        the Tagwriter to write to
	 * @param src
	 *        the source of the {@link HTMLConstants#SCRIPT_REF} tag.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writePlainJavaScriptRef(TagWriter writer, String contextPath, String src, String reloadSuffix)
			throws IOException {
		writePlainJavaScriptRef(writer, contextPath, src, reloadSuffix, JAVASCRIPT_TYPE_VALUE);
	}

	/**
	 * Writes a JavaScript tag with the given <code>src</code> as source to the given
	 * {@link TagWriter}.
	 * 
	 * <p>
	 * Note: In contrast to {@link #writeJavascriptRef(TagWriter, String, String)}, this method does
	 * not append a timestamp to the URL.
	 * </p>
	 * 
	 * @param writer
	 *        the Tagwriter to write to
	 * @param src
	 *        the source of the {@link HTMLConstants#SCRIPT_REF} tag.
	 * @param type
	 *        Specifies the type of the script resource, for instance <code>module</code> if native
	 *        javascript modules are used in the given script.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writePlainJavaScriptRef(TagWriter writer, String contextPath, String src, String reloadSuffix,
			String type) throws IOException {
		writer.beginBeginTag(SCRIPT_REF);
		writer.writeAttribute(TYPE_ATTR, type);
		{
			writer.beginAttribute(SRC_ATTR);
			writer.writeAttributeText(contextPath);
			writer.writeAttributeText(src);
			if (forceScriptReload()) {
				writer.writeAttributeText(reloadSuffix);
			}
			writer.endAttribute();
		}
		writer.endBeginTag();
		// empty tag is not allowed in script tag
		writer.endTag(SCRIPT_REF);
	}

	public static void beginForm(TagWriter out, String name, String action, String method) throws IOException {
		HTMLUtil.beginBeginForm(out, name, action, method);
		HTMLUtil.endBeginForm(out);
	}

	public static void beginForm(TagWriter out, String name, String action) throws IOException {
		HTMLUtil.beginForm(out, name, action, HTMLConstants.POST_VALUE);
	}

	public static void beginBeginForm(TagWriter out, String name, String action, String method)
			throws IOException {
		out.beginBeginTag(HTMLConstants.FORM);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.ACTION_ATTR, action);
		out.writeAttribute(HTMLConstants.METHOD_ATTR, method);
	}

	public static void beginBeginForm(TagWriter out, String name, String action) throws IOException {
		HTMLUtil.beginBeginForm(out, name, action, null);
	}

	public static void endBeginForm(TagWriter out) throws IOException {
		out.endBeginTag();
	}

	public static void beginForm(TagWriter out) throws IOException {
		HTMLUtil.beginForm(out, null);
	}

	public static void beginForm(TagWriter out, String name) throws IOException {
		HTMLUtil.beginForm(out, name, null);
	}

	public static void endForm(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.FORM);
	}

	public static void writeInput(TagWriter out, String name, String type, String value) throws IOException {
		HTMLUtil.writeInput(out, name, type, value, 0, 0);
	}

	public static void writeInput(TagWriter out, String name, String type, String value, int size, int maxlength)
			throws IOException {
		HTMLUtil.beginInput(out, name, type, value, size, maxlength);
		HTMLUtil.endInput(out);
	}

	public static void writeInput(TagWriter out, String name, String value, int size, int maxlength)
			throws IOException {
		HTMLUtil.writeInput(out, name, null, value, size, maxlength);
	}

	public static void beginInput(TagWriter out, String name, String type, String value, int size, int maxlength)
			throws IOException {
		out.beginBeginTag(HTMLConstants.INPUT);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, type);
		out.writeAttribute(HTMLConstants.VALUE_ATTR, value);
		if (size != 0) {
			out.writeAttribute(HTMLConstants.SIZE_ATTR, size);
		}
		if (maxlength != 0) {
			out.writeAttribute(HTMLConstants.MAXLENGTH_ATTR, maxlength);
		}
	}

	public static void beginInput(TagWriter out, String name, String value, int size, int maxlength)
			throws IOException {
		HTMLUtil.beginInput(out, name, null, value, size, maxlength);
	}

	public static void endInput(TagWriter out) throws IOException {
		out.endEmptyTag();
	}

	public static void writeHiddenInput(TagWriter out, String name, String value) throws IOException {
		HTMLUtil.writeHiddenInput(out, name, null, value);
	}

	public static void writeHiddenInput(TagWriter out, String name, String id, String value) throws IOException {
		if (value != null) {
			out.beginBeginTag(HTMLConstants.INPUT);
			out.writeAttribute(HTMLConstants.NAME_ATTR, name);
			out.writeAttribute(HTMLConstants.ID_ATTR, id);
			out.writeAttribute(HTMLConstants.TYPE_ATTR, HTMLConstants.HIDDEN_TYPE_VALUE);
			out.writeAttribute(HTMLConstants.VALUE_ATTR, value);
			out.endEmptyTag();
		}
	}

	public static void beginBeginTextarea(TagWriter out, String name, int cols, int rows) throws IOException {
		out.beginBeginTag(HTMLConstants.TEXTAREA);
		
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.COLS_ATTR, cols);
		out.writeAttribute(HTMLConstants.ROWS_ATTR, rows);
	}

	public static void endBeginTextarea(TagWriter out) throws IOException {
		out.endBeginTag();
	}

	public static void beginTextarea(TagWriter out, String name, int cols, int rows) throws IOException {
		HTMLUtil.beginBeginTextarea(out, name, cols, rows);
		HTMLUtil.endBeginTextarea(out);
	}

	public static void endTextarea(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.TEXTAREA);
	}

	public static void writeSubmit(TagWriter out, String name, String value) throws IOException {
		HTMLUtil.beginSubmit(out, name, value);
		out.endEmptyTag();
	}

	public static void beginSubmit(TagWriter out, String name, String value) throws IOException {
		out.beginBeginTag(HTMLConstants.INPUT);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, HTMLConstants.SUBMIT_TYPE_VALUE);
		out.writeAttribute(HTMLConstants.VALUE_ATTR, value);
	}

	public static void endSubmit(TagWriter out) throws IOException {
		out.endEmptyTag();
	}

	public static void beginDiv(TagWriter out, String tagClass) throws IOException {
		HTMLUtil.beginDiv(out, tagClass, null);
	}

	public static void beginDiv(TagWriter out, String tagClass, String tagID) throws IOException {
		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, tagClass);
		out.writeAttribute(HTMLConstants.ID_ATTR, tagID);
		out.endBeginTag();
	}

	public static void endDiv(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.DIV);
	}

	/**
	 * Begins a {@link HTMLConstants#SPAN} element which has the given CSS class.
	 * 
	 * @param out
	 *        {@link TagWriter} to append tag to.
	 * @param tagClass
	 *        {@link HTMLConstants#CLASS_ATTR CSS class} of the new {@link HTMLConstants#SPAN}
	 *        element.
	 */
	public static void beginSpan(TagWriter out, String tagClass) {
		HTMLUtil.beginSpan(out, tagClass, null);
	}

	/**
	 * Begins a {@link HTMLConstants#SPAN} element which has the given CSS class and the given ID.
	 * 
	 * @param out
	 *        {@link TagWriter} to append tag begin to.
	 * @param tagClass
	 *        {@link HTMLConstants#CLASS_ATTR CSS class} of the new {@link HTMLConstants#SPAN}
	 *        element.
	 * @param tagID
	 *        {@link HTMLConstants#ID_ATTR ID} of the new {@link HTMLConstants#SPAN} element.
	 */
	public static void beginSpan(TagWriter out, String tagClass, String tagID) {
		out.beginBeginTag(HTMLConstants.SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, tagClass);
		out.writeAttribute(HTMLConstants.ID_ATTR, tagID);
		out.endBeginTag();
	}

	/**
	 * Ends a {@link HTMLConstants#SPAN} element.
	 * 
	 * @param out
	 *        {@link TagWriter} to append tag end to.
	 */
	public static void endSpan(TagWriter out) {
		out.endTag(HTMLConstants.SPAN);
	}

	public static void beginSelect(TagWriter out, String name) throws IOException {
		HTMLUtil.beginSelect(out, name, 0);
	}

	public static void beginSelect(TagWriter out, String name, String cssClass, int size, boolean multiple) {
		HTMLUtil.beginSelect(out, name, cssClass, null, size, multiple);
	}

	public static void beginSelect(TagWriter out, String name, String cssClass, String width, int size,
			boolean multiple) {
		out.beginBeginTag(HTMLConstants.SELECT);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
		if (width != null) {
			out.writeAttribute(HTMLConstants.STYLE_ATTR, "width:" + width);
		}
		if (size > 0) {
			out.writeAttribute(HTMLConstants.SIZE_ATTR, size);
		}
		if (multiple) {
			out.writeAttribute(HTMLConstants.MULTIPLE_ATTR, HTMLConstants.MULTIPLE_MULTIPLE_VALUE);
		}
		out.endBeginTag();
	}

	public static void beginSelect(TagWriter out, String name, String tagClass) throws IOException {
		HTMLUtil.beginSelect(out, name, tagClass, 0, HTMLUtil.SINGLE_SELECT);
	}

	public static void beginSelect(TagWriter out, String name, int size) throws IOException {
		HTMLUtil.beginSelect(out, name, null, size, HTMLUtil.SINGLE_SELECT);
	}

	public static void beginOption(TagWriter out, String value) throws IOException {
		out.beginBeginTag(HTMLConstants.OPTION);
		out.writeAttribute(HTMLConstants.VALUE_ATTR, value);
		out.endBeginTag();
	}

	public static void writeOption(TagWriter out, int value, String text, int current) throws IOException {
		HTMLUtil.writeOption(out, Integer.toString(value), text, value == current);
	}

	public static void writeOption(TagWriter out, String value, String text, boolean selected)
			throws IOException {
		out.beginBeginTag(HTMLConstants.OPTION);
		out.writeAttribute(HTMLConstants.VALUE_ATTR, value);
		if (selected)  {
			out.writeAttribute(HTMLConstants.SELECTED_ATTR, HTMLConstants.SELECTED_SELECTED_VALUE);
		}
		out.endBeginTag();
		out.writeText(text);
		out.endTag(HTMLConstants.OPTION);
	}

	public static void writeOption(TagWriter out, String value, String text) throws IOException {
		HTMLUtil.writeOption(out, value, text, false);
	}

	public static void endOption(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.OPTION); // end tag will check for correct nesting
	}

	public static void endSelect(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.SELECT);
	}

	public static void beginTable(TagWriter out, String summary, int border) throws IOException {
		out.beginBeginTag(HTMLConstants.TABLE);
		out.writeAttribute(HTMLConstants.SUMMARY_ATTR, summary);
		out.writeAttribute(HTMLConstants.BORDER_ATTR, border);
		out.endBeginTag();
	}

	public static void beginTable(TagWriter out, String summary, String cssClass, int border, int spacing,
			int padding) throws IOException {
		out.beginBeginTag(HTMLConstants.TABLE);
		out.writeAttribute(HTMLConstants.SUMMARY_ATTR, summary);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
		out.writeAttribute(HTMLConstants.BORDER_ATTR, border);
		out.writeAttribute(HTMLConstants.CELLSPACING_ATTR, spacing);
		out.writeAttribute(HTMLConstants.CELLPADDING_ATTR, padding);
		out.endBeginTag();
	}

	public static void beginTable(TagWriter out, String summary) throws IOException {
		HTMLUtil.beginTable(out, summary, 0);
	}

	public static void beginTable(TagWriter out, String id, String name, String summary, String tagClass,
			int border) throws IOException {
		out.beginBeginTag(HTMLConstants.TABLE);
		out.writeAttribute(HTMLConstants.ID_ATTR, id);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.SUMMARY_ATTR, summary);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, tagClass);
		if (border >= 0) {
			out.writeAttribute(HTMLConstants.BORDER_ATTR, border);
		}
		out.endBeginTag();
	}

	public static void beginTable(TagWriter out, String summary, String tagClass, int border)
			throws IOException {
		out.beginBeginTag(HTMLConstants.TABLE);
		out.writeAttribute(HTMLConstants.SUMMARY_ATTR, summary);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, tagClass);
		if (border >= 0) {
			out.writeAttribute(HTMLConstants.BORDER_ATTR, border);
		}
		out.endBeginTag();
	}

	public static void beginTable(TagWriter out, String summary, String tagClass, int border, String aWidthSpec)
			throws IOException {
		out.beginBeginTag(HTMLConstants.TABLE);
		out.writeAttribute(HTMLConstants.SUMMARY_ATTR, summary);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, tagClass);
		out.writeAttribute(HTMLConstants.BORDER_ATTR, border);
		out.writeAttribute(HTMLConstants.WIDTH_ATTR, aWidthSpec);
		out.endBeginTag();
	}

	public static void beginTable(TagWriter out, String summary, String tagClass) throws IOException {
		HTMLUtil.beginTable(out, summary, tagClass, 0);
	}

	public static void endTable(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.TABLE);
	}

	public static void beginTr(TagWriter out) throws IOException {
		out.beginTag(HTMLConstants.TR);
	}

	public static void endTr(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.TR);
	}

	public static void writeEmptyTd(TagWriter out) throws IOException {
		out.beginTag(HTMLConstants.TD);
		out.writeText(HTMLConstants.NBSP);
		out.endTag(HTMLConstants.TD);
	}

	public static void beginTh(TagWriter out) throws IOException {
		out.beginTag(HTMLConstants.TH);
	}

	public static void beginBeginTr(TagWriter out) {
		out.beginBeginTag(TR);
	}

	public static void endBeginTr(TagWriter out) {
		out.endBeginTag();
	}

	public static void beginBeginTable(TagWriter out) {
		out.beginBeginTag(TABLE);
	}

	public static void endBeginTable(TagWriter out) {
		out.endBeginTag();
	}

	public static void writeClassAttribute(TagWriter out, String cssClass) {
		out.writeAttribute(CLASS_ATTR, cssClass);
	}

	public static void beginBeginTd(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.TD);
	}

	public static void endBeginTd(TagWriter out) throws IOException {
		out.endBeginTag();
	}

	public static void beginBeginTh(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.TH);
	}

	public static void endBeginTh(TagWriter out) throws IOException {
		out.endBeginTag();
	}

	public static void endTh(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.TH);
	}

	public static void beginTd(TagWriter out) throws IOException {
		out.beginTag(HTMLConstants.TD);
	}

	public static void beginTd(TagWriter out, String align, String valign, int colspan, int rowspan)
			throws IOException {
		out.beginBeginTag(HTMLConstants.TD);
		out.writeAttribute(HTMLConstants.ALIGN_ATTR, align);
		out.writeAttribute(HTMLConstants.VALIGN_ATTR, valign);
		if (colspan > 1) {
			out.writeAttribute(HTMLConstants.COLSPAN_ATTR, colspan);
		}
		if (rowspan > 1) {
			out.writeAttribute(HTMLConstants.ROWSPAN_ATTR, rowspan);
		}
		out.endBeginTag();
	}

	public static void beginTd(TagWriter out, int colspan) throws IOException {
		HTMLUtil.beginTd(out, null, null, colspan, 0);
	}

	public static void endTd(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.TD);
	}

	public static void beginA(TagWriter out, String href) throws IOException {
		HTMLUtil.beginA(out, href, null);
	}

	public static void beginA(TagWriter out, String href, String name) throws IOException {
		HTMLUtil.beginA(out, href, name, null);
	}

	public static void beginA(TagWriter out, String href, String name, String target) throws IOException {
		HTMLUtil.beginA(out, href, name, target, null);
	}

	public static void beginA(TagWriter out, String href, String name, String target, String cssClass)
			throws IOException {
		out.beginBeginTag(HTMLConstants.ANCHOR);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
		out.writeAttribute(HTMLConstants.HREF_ATTR, href);
		out.writeAttribute(HTMLConstants.NAME_ATTR, name);
		out.writeAttribute(HTMLConstants.TARGET_ATTR, target);
		out.endBeginTag();
	}

	public static void endA(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.ANCHOR);
	}

	public static void writeBr(TagWriter out) throws IOException {
		out.emptyTag(HTMLConstants.BR);
	}

	public static void writeHr(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.HR);
		out.endEmptyTag();
	}

	/**
	 * Utility to write script content to be executed after the page is completely displayed.
	 * 
	 * @param out
	 *        The writer, must be in state
	 *        {@link com.top_logic.basic.xml.TagWriter.State#ELEMENT_CONTENT}.
	 * @param afterRenderingScript
	 *        The JavaScript source code.
	 */
	public static void writeScriptAfterRendering(TagWriter out, String afterRenderingScript) throws IOException {
		beginScriptAfterRendering(out);
		out.append(afterRenderingScript);
		endScriptAfterRendering(out);
	}

	/**
	 * Utility to begin script content, that shall be executed after the page is completely
	 * displayed.
	 * 
	 * @param out
	 *        The writer, must be in state
	 *        {@link com.top_logic.basic.xml.TagWriter.State#ELEMENT_CONTENT}.
	 */
	public static void beginScriptAfterRendering(TagWriter out) throws IOException {
		out.beginScript();
		if (out.getStack().contains(HTMLConstants.HTML)) {
			out.append("BAL.addEventListener(window, 'load', function() {");
		} else {
			out.append("services.ajax.executeAfterRendering(window, function() {");
		}
	}

	/**
	 * Utility to end script content, that shall be executed after the page is completely displayed.
	 * 
	 * @param out
	 *        The writer.
	 */
	public static void endScriptAfterRendering(TagWriter out) throws IOException {
		out.append("});");
		out.endScript();
	}

	public static void writeJavaScriptContent(TagWriter out, String aLine) throws IOException {
		out.writeIndent();
		out.append(aLine);
	}

	/**
	 * Efficiently write a {@link HTMLConstants#SRC_ATTR} with the contents of the given
	 * {@link DynamicText}.
	 * 
	 * @param context
	 *        Current {@link DisplayContext}.
	 * @param out
	 *        The writer.
	 * @param src
	 *        The value of the {@link HTMLConstants#SRC_ATTR} to write.
	 */
	public static void writeImageSrc(DisplayContext context, TagWriter out, DynamicText src) throws IOException {
		writeAttribute(context, out, SRC_ATTR, src);
	}

	/**
	 * Write attributes required to generate a tooltip on an icon.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The writer.
	 * @param tooltipText
	 *        The plain text (no HTML tags) to display as tooltip (and alternative text for limited
	 *        devices).
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeImageTooltip(DisplayContext context, TagWriter out, String tooltipText) throws IOException {
		// Alternative text.
		out.writeAttribute(ALT_ATTR, tooltipText);

		// The tooltip source.
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, tooltipText);

		// Prevent older browsers from also displaying the alternative text as tooltip (effectively
		// producing two tooltips).
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING);
	}

	/**
	 * Write attributes required to generate a tooltip on an icon.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The writer.
	 * @param tooltipHtml
	 *        The HTML source to display as tooltip.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public static void writeImageTooltipHtml(DisplayContext context, TagWriter out, String tooltipHtml)
			throws IOException {
		// Alternative text.
		out.writeAttribute(ALT_ATTR, StringServices.EMPTY_STRING);

		// The tooltip source.
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltipHtml);

		// Prevent older browsers from also displaying the alternative text as tooltip (effectively
		// producing two tooltips).
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING);
	}

	/**
	 * Efficiently write an attribute with the given name and the given {@link DynamicText} as
	 * contents.
	 * 
	 * @param context
	 *        Current {@link DisplayContext}.
	 * @param out
	 *        The writer.
	 * @param name
	 *        The name of the attribute to write.
	 * @param value
	 *        The value of the {@link HTMLConstants#SRC_ATTR} to write.
	 */
	public static void writeAttribute(DisplayContext context, TagWriter out, String name, DynamicText value)
			throws IOException {
		out.beginAttribute(name);
		value.append(context, out);
		out.endAttribute();
	}

	/**
	 * Efficiently write the given {@link DynamicText} as JavaScript string literal.
	 * 
	 * @param context
	 *        Current {@link DisplayContext}.
	 * @param out
	 *        The writer.
	 * @param value
	 *        The value of the {@link HTMLConstants#SRC_ATTR} to write.
	 */
	public static void writeJsString(DisplayContext context, Appendable out, DynamicText value) throws IOException {
		if (out instanceof TagWriter) {
			writeJsString(context, (TagWriter) out, value);
		} else {
			// There is not way to switch the underlying writer into a mode for stream-encoding
			// JavaScript string literals.
			TagUtil.writeJsString(out, Fragments.toString(context, value));
		}
	}

	/**
	 * Efficiently write the given {@link DynamicText} as JavaScript string literal.
	 * 
	 * @param context
	 *        Current {@link DisplayContext}.
	 * @param out
	 *        The writer.
	 * @param value
	 *        The value of the {@link HTMLConstants#SRC_ATTR} to write.
	 */
	public static void writeJsString(DisplayContext context, TagWriter out, DynamicText value) throws IOException {
		out.beginJsString();
		value.append(context, out);
		out.endJsString();
	}

	/**
	 * true, whether the given node is an element node and belongs to the class of void
	 *         elements, false otherwise.
	 * 
	 * @see #VOID_ELEMENTS
	 */
	public static boolean isVoidElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE && isVoidElement(node.getLocalName());
	}

	/**
	 * true, whether the given node name represents a node, which is an element node and
	 *         belongs to the class of void elements, false otherwise.
	 *
	 * @see #VOID_ELEMENTS
	 */
	public static boolean isVoidElement(String elementName) {
		return HTMLConstants.VOID_ELEMENTS.contains(elementName);
	}

	/**
	 * Writes a javascript command which sends an email to the given email address.
	 * 
	 * @param out
	 *        The {@link Appendable} to write js command to.
	 * @param emailAddress
	 *        The mail address to send mail to.
	 */
	public static void appendMailToJS(Appendable out, String emailAddress) {
		try {
			out.append("BAL.sendMailTo(event, ");
			TagUtil.beginJsString(out);
			TagUtil.writeJsStringContent(out, emailAddress);
			TagUtil.endJsString(out);
			out.append(");");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Returns a javascript command to send an email to the given address.
	 * 
	 * @see HTMLUtil#appendMailToJS(Appendable, String)
	 */
	public static String getMailToJS(String emailAddress) {
		StringBuilder buffer = new StringBuilder();
		HTMLUtil.appendMailToJS(buffer, emailAddress);
		return buffer.toString();
	}

	/**
	 * Writes a {@link HTMLConstants#META meta} attribute which ensures that Internet-Explorer does
	 * not use compatibility mode.
	 * 
	 * <p>
	 * This method must only be called in the {@link HTMLConstants#HEAD head} section of the HTML
	 * document.
	 * </p>
	 * 
	 * @param out
	 *        The {@link TagWriter} to write content to
	 */
	public static void writeEdgeDocumentMode(TagWriter out) {
		out.beginBeginTag(META);
		out.writeAttribute(HTTP_EQUIV_ATTR, "X-UA-Compatible");
		out.writeAttribute(CONTENT_ATTR, "IE=edge");
		out.endEmptyTag();
	}

	/**
	 * Writes a {@link HTMLConstants#TL_DRAG_IMAGE} attribute which provides a custom drag image for
	 * the given object when a drag and drop operation is initiated.
	 * 
	 * <p>
	 * The {@link MetaResourceProvider} is used to render the icon and label for the given object.
	 * </p>
	 * 
	 * @param context
	 *        The context in which the rendering occurs.
	 * @param out
	 *        The {@link TagWriter} to write content to.
	 * @param object
	 *        Object to create the drag preview image for.
	 * 
	 * @see #writeDragImage(DisplayContext, TagWriter, ResourceProvider, Object)
	 */
	public static void writeDragImage(DisplayContext context, TagWriter out, Object object) throws IOException {
		writeDragImage(context, out, MetaResourceProvider.INSTANCE, object);
	}

	/**
	 * Writes a {@link HTMLConstants#TL_DRAG_IMAGE} attribute which provides a custom drag image for
	 * the given object when a drag and drop operation is initiated.
	 * 
	 * @param context
	 *        The context in which the rendering occurs.
	 * @param out
	 *        The {@link TagWriter} to write content to.
	 * @param resourceProvider
	 *        {@link ResourceProvider} to render the icon and label of the dragged object.
	 * @param object
	 *        Object to create the drag preview image for.
	 * 
	 * @see HTMLConstants#TL_DRAG_IMAGE
	 */
	public static void writeDragImage(DisplayContext context, TagWriter out, ResourceProvider resourceProvider,
			Object object) throws IOException {
		out.beginAttribute(HTMLConstants.TL_DRAG_IMAGE);
		writeDragImageContent(context, out, resourceProvider, object);
		out.endAttribute();
	}

	/**
	 * writes the HTML that is used to display the image when the user starts a drag.
	 * 
	 * @param context
	 *        The context in which the rendering occurs.
	 * @param out
	 *        The {@link TagWriter} to write content to.
	 * @param resourceProvider
	 *        {@link ResourceProvider} to render the icon and label of the dragged object.
	 * @param object
	 *        Object to create the drag preview image for.
	 */
	public static void writeDragImageContent(DisplayContext context, TagWriter out, ResourceProvider resourceProvider,
			Object object) throws IOException {
		try (TagWriter writer = new TagWriter(new NonClosingProxyWriter(out))) {
			writer.beginTag(HTMLConstants.SPAN);
			ThemeImage themeImage = resourceProvider.getImage(object, Flavor.DEFAULT);
			if (themeImage != null) {
				XMLTag icon = themeImage.toIcon();
				icon.beginBeginTag(context, writer);
				icon.endBeginTag(context, writer);
				icon.endTag(context, writer);
				writer.write(" ");
			}
			String label = resourceProvider.getLabel(object);
			if (label != null) {
				writer.write(label);
			}
			writer.endTag(HTMLConstants.SPAN);
		}
	}
}
