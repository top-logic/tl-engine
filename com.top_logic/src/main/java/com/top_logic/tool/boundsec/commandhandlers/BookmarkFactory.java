/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Factory class for creating bookmark links for objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BookmarkFactory {

	/**
	 * Generate an external link to one element in the application based on the connection
	 * parameters of a context's request
	 * 
	 * @param targetObject
	 *        The {@link KnowledgeObject} to be displayed, may be <code>null</code>, if the given
	 *        component name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @param displayContext
	 *        The current {@link DisplayContext} for determining the external interface of the
	 *        application.
	 * 
	 * @return The requested link.
	 */
	public static String getBookmarkURL(DisplayContext displayContext, TLObject targetObject, ComponentName componentName) {
		StringBuilder urlBuilder = new StringBuilder(128);
		try {
			LayoutUtils.appendHostURL(displayContext, urlBuilder);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		appendBookmarkURL(urlBuilder, displayContext.getContextPath(), targetObject, componentName);
		return urlBuilder.toString();
	}

	/**
	 * Generate an external link to an element in the application based on configured connection
	 * parameters like {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}.
	 * 
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @param linkText
	 *        The display text of the link, must neither be <code>null</code> nor empty.
	 * @return The requested link.
	 */
	public static String getBookmarkLink(Wrapper targetObject, ComponentName componentName, String linkText) {
		return getBookmarkLink(null, targetObject, componentName, linkText);
	}

	/**
	 * Generate an external link to an element in the application based on configured connection
	 * parameters like {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}.
	 * 
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @param linkText
	 *        The display text of the link, must neither be <code>null</code> nor empty.
	 * @param linkTarget
	 *        The target window of the link, may be <code>null</code>.
	 * @return The requested link.
	 */
	public static String getBookmarkLink(Wrapper targetObject, ComponentName componentName, String linkText, String linkTarget) {
		return getBookmarkLink(null, targetObject, componentName, linkText, linkTarget);
	}

	/**
	 * Generate an external link to an element in the application based on configured connection
	 * parameters like {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}.
	 * 
	 * @param hostSuffix
	 *        The host config suffix. <code>null</code> defaults to an empty string.
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @param linkText
	 *        The display text of the link, must neither be <code>null</code> nor empty.
	 * @return The requested link.
	 */
	public static String getBookmarkLink(String hostSuffix, Wrapper targetObject, ComponentName componentName, String linkText) {
		return getBookmarkLink(hostSuffix, targetObject, componentName, linkText, null);
	}

	/**
	 * Generate an external link to an element in the application based on configured connection
	 * parameters like {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}.
	 * 
	 * @param hostSuffix
	 *        The host config suffix. <code>null</code> defaults to an empty string.
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @param linkText
	 *        The display text of the link, must neither be <code>null</code> nor empty.
	 * @param linkTarget
	 *        The target window of the link, may be <code>null</code>.
	 * @return The requested link.
	 */
	public static String getBookmarkLink(String hostSuffix, Wrapper targetObject, ComponentName componentName,
			String linkText, String linkTarget) {
		StringBuilder buffer = new StringBuilder(128);
		buffer.append("<a");
		if (!StringServices.isEmpty(linkTarget)) {
			buffer.append(" target=\"");
			buffer.append(linkTarget);
			buffer.append('"');
		}
		buffer.append(" href=\"");
		appendBookmarkURL(buffer, hostSuffix, targetObject, componentName);
		buffer.append("\">");
		buffer.append(linkText);
		buffer.append("</a>");
	
		return buffer.toString();
	}

	/**
	 * Generate an URL of an element in the application based on configured connection parameters
	 * like {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}.
	 * 
	 * @param hostSuffix
	 *        The host config suffix. <code>null</code> defaults to an empty string.
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 * @return The requested link.
	 */
	public static String getBookmarkURL(String hostSuffix, Wrapper targetObject, ComponentName componentName) {
		StringBuilder buffer = new StringBuilder();
		appendBookmarkURL(buffer, hostSuffix, targetObject, componentName);
		return buffer.toString();
	}
	
	/**
	 * Append an URL of an element in the application based on configured connection parameters like
	 * {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT} to the given buffer.
	 * 
	 * @param hostSuffix
	 *        The host config suffix. <code>null</code> defaults to an empty string.
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 */
	public static void appendBookmarkURL(StringBuilder buffer, String hostSuffix, Wrapper targetObject,
			ComponentName componentName) {
		AliasManager theAlias = AliasManager.getInstance();
	
		String hostAliasName;
		if (hostSuffix == null) {
			hostAliasName = AliasManager.HOST;
		} else {
			hostAliasName = "%HOST" + hostSuffix + "%";
		}
		String hostAlias = theAlias.replace(hostAliasName);
		String contextPath = theAlias.replace(AliasManager.APP_CONTEXT);
	
		if (StringServices.isEmpty(hostAlias) || contextPath == null) {
			Logger.warn(hostAliasName + " or " + AliasManager.APP_CONTEXT + " is not set in the config file.",
				BookmarkFactory.class);
		}
	
		buffer.append(hostAlias);
		appendBookmarkURL(buffer, contextPath, (TLObject) targetObject, componentName);
	}

	/**
	 * Generate a stable external URL that directly jumps to a given object.
	 * 
	 * <code>
	 * &lt;urlBuilder&gt;/contextPath/layoutPath?id=targetObjectName&type=targetObjectType&component=componentName
	 * </code>
	 * 
	 * @param urlBuilder
	 *        The buffer to append the generated URL to. The builder is initialized with the host of
	 *        the application, e.g. https://apps.top-logic.com:8081
	 * @param contextPath
	 *        The context path of the application.
	 * @param targetObject
	 *        The {@link KnowledgeObject} to be displayed, may be <code>null</code>, if the given
	 *        component name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 */
	private static final void appendBookmarkURL(StringBuilder urlBuilder, String contextPath, TLObject targetObject,
			ComponentName componentName) {
		urlBuilder.append(contextPath);
		urlBuilder.append(ApplicationPages.getInstance().getLayoutServletPath());
	
		appendURLEncodedBookmarkArguments(urlBuilder, targetObject, componentName);
	}

	/**
	 * Append the encoded the URL arguments for a bookmark to the given URL.
	 * 
	 * @param url
	 *        The URL being built.
	 * @param targetObject
	 *        The target object to be displayed, may be <code>null</code>, if the given component
	 *        name is non- <code>null</code>.
	 * @param componentName
	 *        The name of the component to jump to, may be <code>null</code>, which means the
	 *        default component for the given object.
	 */
	public static void appendURLEncodedBookmarkArguments(StringBuilder url, TLObject targetObject,
			ComponentName componentName) {
		boolean first = true;
		if (targetObject != null) {
			// Note: Used in SubSessionHandler to resolve an object later on.
			CommandHandler handler =
				CommandHandlerFactory.getInstance().getHandler(DefaultBookmarkHandler.COMMAND_RESOLVE_BOOKMARK);
			if (handler != null) {
				BookmarkHandler provider = BookmarkService.getInstance().getBookmarkHandler(targetObject);
				first = provider.appendIdentificationArguments(url, first, targetObject);
				
				if (first) {
					Logger.warn(
						"Could not create bookmark URL for object of class '" + targetObject.getClass().getName()
							+ "' using handler '" + handler.getClass().getName() + "'.", BookmarkFactory.class);
				}
			}
		}
		if (componentName != null) {
			URLUtilities.appendUrlArg(url, first, GotoHandler.COMMAND_PARAM_COMPONENT, componentName.qualifiedName());
		}
	}

}
