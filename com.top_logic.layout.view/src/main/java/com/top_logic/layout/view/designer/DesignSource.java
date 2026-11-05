/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.view.ViewElement;

/**
 * Where a view the View Designer edits is stored: a {@code .view.xml} file
 * ({@link FileDesignSource}) or an attribute of a model object holding a view configuration
 * ({@link AttributeDesignSource}).
 *
 * <p>
 * A {@link DesignSession} keeps a tree per source it opened, and the {@link DesignTreeNode nodes}
 * of a tree know the source they belong to. Two sources standing for the same storage location are
 * {@link Object#equals(Object) equal}, so that the session finds the tree of a source again.
 * </p>
 */
public interface DesignSource {

	/**
	 * The text naming this source to the user: in the breadcrumb of the designer, at the root of
	 * its tree and in messages.
	 */
	String getLabel();

	/**
	 * The view configuration the design tree of this source is built from.
	 *
	 * <p>
	 * The tree edits the configuration in place, until it is {@link #save(String) saved} or the
	 * session is reverted, see {@link #discard()}.
	 * </p>
	 *
	 * @throws ConfigurationException
	 *         If the stored view cannot be read.
	 */
	ViewElement.Config load() throws ConfigurationException;

	/**
	 * Stores the edited view.
	 *
	 * @param content
	 *        The view serialized as XML with the root element {@code <view>}, checked to be
	 *        loadable.
	 * @throws com.top_logic.util.error.TopLogicException
	 *         If the view cannot be stored.
	 */
	void save(String content);

	/**
	 * Forgets the configuration {@link #load() loaded} for editing, so that the next
	 * {@link #load()} reads the stored view anew.
	 */
	void discard();

	/**
	 * An identifier of this source unique among all sources, e.g. to recognize a designer window
	 * opened on it.
	 */
	String getId();

}
