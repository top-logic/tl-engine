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

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.model.TLObject;
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
	public static String getLink(Wrapper object, String linkText, String goTo) {
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
	private static void writeLink(StringWriter stringWriter, Wrapper targetObject,
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
	public static String getLinkDestination(Wrapper targetObject, String section) {
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
