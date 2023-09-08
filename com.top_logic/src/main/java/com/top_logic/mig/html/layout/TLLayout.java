/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.io.OutputStream;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.editor.DatabaseLayoutCache;

/**
 * General application layout part.
 * 
 * <p>
 * The application layout is structurally described by a tree. Each node of the tree corresponds to
 * a part of the layout and is represented by a {@link LayoutComponent} or rather its configuration.
 * </p>
 * 
 * <p>
 * The {@link TLLayout}s are created from reading the corresponding file system XML files or
 * deserializing the database content when, for example, the MainLayout is installed or views are
 * configured in-app.
 * </p>
 * 
 * <p>
 * Instead of writing layouts and component configurations yourself, templates exist to make the
 * work easier. If a layout is created by using such a template, {@link #getTemplateName()} provides
 * the name and {@link #getArguments()} the arguments of the used template, otherwise calling these
 * two methods results in a {@link UnsupportedOperationException}.
 * </p>
 * 
 * @see DatabaseLayoutCache
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TLLayout {

	/**
	 * The corresponding component configuration of this layout.
	 */
	LayoutComponent.Config get() throws ConfigurationException;

	/**
	 * Name of the template from which it origins.
	 * 
	 * @throws UnsupportedOperationException
	 *         if {@link #hasTemplate()} is false.
	 */
	String getTemplateName();

	/**
	 * Values for the template properties.
	 * 
	 * @throws UnsupportedOperationException
	 *         if {@link #hasTemplate()} is false.
	 */
	ConfigurationItem getArguments() throws ConfigurationException;

	/**
	 * True if the layout origins from a template, otherwise false.
	 */
	boolean hasTemplate();

	/**
	 * Writes the layout into the given stream.
	 * 
	 * @param stream
	 *        To write into.
	 * @param isFinal
	 *        Whether the layout must not be enhanced with overlays, when it is de-serialized.
	 * @throws IOException
	 *         if the configuration could not be written.
	 */
	void writeTo(OutputStream stream, boolean isFinal) throws IOException;
}
