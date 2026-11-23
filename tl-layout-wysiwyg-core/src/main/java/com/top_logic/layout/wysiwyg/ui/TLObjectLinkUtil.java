/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.utility.MaskedValues;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.ObjectNotFound;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkFactory;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkHandler;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for working with {@link TLObject} links.
 * 
 * @author <a href="mailto:dpa@top-logic.com">Diana Pankratz</a>
 */
public class TLObjectLinkUtil {


	/** Parameter to jump to a specific section in the source code of the {@link StructuredText}. */
	private static final String SECTION = "section";

	/** {@link TLObject} link attribute defining the section of a page to go to. */
	public static final String DATA_SECTION = "data-section";

	/** CSS class of TLObject links */
	public static final String TL_OBJECT = "tlObject";

	/** CSS class of the wrapper of TLObject links */
	public static final String TL_OBJECT_WRAPPER = "tlObjectWrapper";

	/**
	 * Attribute of the placeholder that stands for a masked object link, see
	 * {@link #maskLinks(String, Function)}. Its value is the token that names the hidden link, the
	 * empty string where nobody takes the link back.
	 */
	private static final String DATA_MASKED = "data-masked";

	/**
	 * The target object of a TLObject link.
	 * 
	 * @param objectArgument
	 *        The parameters to determine the object.
	 * @return target object.
	 * 
	 * @throws ObjectNotFound
	 *         When informations the arguments should have lead to a valid object but it can not be
	 *         resolved, i.e. the informations are appropriate for some {@link BookmarkHandler} but
	 *         are invalid.
	 */
	public static Wrapper getObject(String objectArgument) throws ObjectNotFound {
		Map<String, Object> objectArguments = getObjectArgumentsMap(objectArgument);
		if (objectArguments == null) {
			return null;
		}
		DefaultBookmarkHandler handler = (DefaultBookmarkHandler) CommandHandlerFactory.getInstance().getHandler(
			DefaultBookmarkHandler.COMMAND_RESOLVE_BOOKMARK);
		return (Wrapper) handler.getBookmarkObject(objectArguments);
	}

	/**
	 * The object that the given link destination names, if the current user may discover it.
	 *
	 * <p>
	 * Serves the controls of the engine that display formatted text: they show and follow an object
	 * link only if this method finds its object, see {@link #maskLinks(String, Function)}.
	 * </p>
	 *
	 * @param href
	 *        The destination of an object link, as {@link #getLinkDestination(TLObject, String)}
	 *        wrote it. May be <code>null</code>.
	 * @return The object, or <code>null</code> if the destination names no object that exists or
	 *         names an object that the current user may not discover. The two cases give the same
	 *         answer, so that the answer does not tell whether a hidden object exists.
	 */
	@FrameworkInternal
	public static Wrapper getDiscoverableObject(String href) {
		if (href == null) {
			return null;
		}
		Wrapper target;
		try {
			target = getObject(href);
		} catch (RuntimeException ex) {
			// The destination names an object that does not exist, or cannot be read at all. In
			// both cases the link leads nowhere, and the reader learns nothing more.
			return null;
		}
		return target == null || DiscoverRight.isMasked(target) ? null : target;
	}

	/**
	 * The given HTML, in which each link to an object that the current user may not discover is
	 * replaced by a placeholder.
	 *
	 * <p>
	 * Applies the rule for a masked element to the object links of a formatted text: the
	 * placeholder <code>&lt;span data-masked="token"&gt;*****&lt;/span&gt;</code> has no label, no
	 * link and no identity of the object. A link whose destination names no existing object is
	 * masked as well, see {@link #getDiscoverableObject(String)}. All other content stays as it is.
	 * </p>
	 *
	 * <p>
	 * Serves the controls and outputs of the engine that send a stored formatted text to a client.
	 * An editor gives each masked link a token and takes the links back with
	 * {@link #restoreLinks(String, Function)} when the text comes back from the client. An output
	 * that does not come back, for example a preview, a label or an export, gives no tokens.
	 * </p>
	 *
	 * @param html
	 *        The HTML source of a formatted text. May be <code>null</code>.
	 * @param tokens
	 *        Gives the token for each masked link, in the order of the text, from the markup of the
	 *        link. <code>null</code> for placeholders without a token.
	 * @return The masked HTML; the given HTML itself if it contains no link to mask.
	 */
	@FrameworkInternal
	public static String maskLinks(String html, Function<String, String> tokens) {
		if (html == null || !html.contains(TL_OBJECT)) {
			return html;
		}
		Document document = parseFragment(html);
		boolean masked = false;
		for (Element link : document.select(ANCHOR + "." + TL_OBJECT)) {
			if (getDiscoverableObject(link.attr(HREF_ATTR)) != null) {
				continue;
			}
			String token = tokens == null ? "" : tokens.apply(link.outerHtml());
			link.replaceWith(new Element(SPAN).attr(DATA_MASKED, token).text(MaskedValues.placeholder()));
			masked = true;
		}
		return masked ? document.body().html() : html;
	}

	/**
	 * The given HTML with each placeholder of {@link #maskLinks(String, Function)} replaced by the
	 * object link that its token names.
	 *
	 * <p>
	 * Serves the editors of the engine: the links that the client received masked come back where
	 * the client keeps their placeholders. A link whose placeholder the user deleted is deleted. A
	 * placeholder whose token names no link is dropped, so that a client cannot bring in a link that
	 * it did not receive.
	 * </p>
	 *
	 * @param html
	 *        The HTML of a formatted text as the client sent it. May be <code>null</code>.
	 * @param links
	 *        Gives the markup of the link that a token names, <code>null</code> for a token that
	 *        names no link.
	 * @return The HTML with the links; the given HTML itself if it contains no placeholder.
	 */
	@FrameworkInternal
	public static String restoreLinks(String html, Function<String, String> links) {
		if (html == null || !html.contains(DATA_MASKED)) {
			return html;
		}
		Document document = parseFragment(html);
		Elements placeholders = document.select(SPAN + "[" + DATA_MASKED + "]");
		if (placeholders.isEmpty()) {
			return html;
		}
		for (Element placeholder : placeholders) {
			String link = links.apply(placeholder.attr(DATA_MASKED));
			if (link != null) {
				placeholder.before(link);
			}
			placeholder.remove();
		}
		return document.body().html();
	}

	private static Document parseFragment(String html) {
		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings().prettyPrint(false);
		return document;
	}

	/**
	 * A map of all key value pairs of the given String.
	 * 
	 * <p>
	 * The pairs are separated by "&" and keys and values are separated by "=".
	 * </p>
	 * 
	 * @param objectArgument
	 *        String containing the key value pairs.
	 * @return Converted key value pairs as {@link Map}.
	 */
	public static Map<String, Object> getObjectArgumentsMap(String objectArgument) {
		int index = objectArgument.lastIndexOf("?");
		if (index == -1) {
			return null;
		}
		String parameters = objectArgument.substring(index + 1);
		Map<String, Object> result = new HashMap<>();
		for (String keyValue : StringServices.split(parameters, '&')) {
			int valueSeparator = keyValue.indexOf('=');
			String key = keyValue.substring(0, valueSeparator);
			String value = keyValue.substring(valueSeparator + 1);
			result.put(URLUtilities.urlDecode(key), URLUtilities.urlDecode(value));
		}
		return result;
	}

	/**
	 * Creates a {@link TLObject} link HTML snippet.
	 * @param object
	 *        The {@link TLObject} to which the link leads.
	 * @param linkText
	 *        The text representing the link.
	 * @param goTo
	 *        Goto id inside of the {@link TLObject} source code.
	 * 
	 * @return {@link String} with HTML Snippet of the link.
	 */
	public static String getLink(TLObject object, String linkText, String goTo) {
		String pageName = MetaResourceProvider.INSTANCE.getLabel(object);
		if (linkText == null) {
			linkText = pageName;
		}
		final ThemeImage image = MetaResourceProvider.INSTANCE.getImage(object, Flavor.DEFAULT);
		StringWriter stringWriter = new StringWriter();

		writeLink(stringWriter, object, goTo, image, linkText);

		return stringWriter.toString();
	}

	/**
	 * Writes the link to a {@link TLObject}
	 * 
	 * @param stringWriter
	 *        {@link StringWriter} to write the link to
	 * @param targetObject
	 *        Target object of the link.
	 * @param section
	 *        Goto id inside of the {@link TLObject} source code.
	 * @param image
	 *        Icon of the {@link TLObject}
	 * @param linkText
	 *        The text representing the link.
	 */
	private static void writeLink(StringWriter stringWriter, TLObject targetObject,
			String section, final ThemeImage image, String linkText) {
		try (TagWriter writer = new TagWriter(stringWriter)) {
			writer.beginBeginTag(ANCHOR);
			writer.writeAttribute(HREF_ATTR, getLinkDestination(targetObject, section));
			writeGotoSection(writer, section);
			writer.beginCssClasses();
			writer.append(TL_OBJECT);
			writer.endCssClasses();
			writer.endBeginTag();
			writer.writeText(linkText);
			writer.endTag(ANCHOR);
		} catch (IOException exception) {
			throw new TopLogicException(I18NConstants.WRITE_LINK_ERROR, exception);
		}
	}


	/**
	 * Creates the {@link HTMLConstants#HREF_ATTR} attribute containing information about the target
	 * object.
	 * 
	 * @param targetObject
	 *        The destination of the link.
	 * @param section
	 *        The parameter containing the section to jump to in the source code. If
	 *        <code>null</code> the parameter will not be added to the attribute href.
	 */
	public static String getLinkDestination(TLObject targetObject, String section) {
		StringBuilder urlBuilder = new StringBuilder();
		BookmarkFactory.appendURLEncodedBookmarkArguments(urlBuilder, targetObject, null);
		URLUtilities.appendUrlArg(urlBuilder, false, SECTION, section);
		return urlBuilder.toString();
	}

	/**
	 * Write a {@link #DATA_SECTION} attribute.
	 * 
	 * @param section
	 *        The section of the page to go to.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	private static void writeGotoSection(TagWriter writer, String section) throws IOException {
		if (section != null) {
			writer.beginAttribute(DATA_SECTION);
			writer.writeAttributeText(section);
			writer.endAttribute();
		}
	}

}
