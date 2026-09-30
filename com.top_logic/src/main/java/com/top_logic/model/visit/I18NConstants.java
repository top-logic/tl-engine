/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey MODEL_NAME;

	/**
	 * Parameters:
	 * <ul>
	 * <li>Typ des Moduls</li>
	 * <li>Name des Typs</li>
	 * <li>ID des Moduls</li>
	 * <li>Beschreibung</li>
	 * </ul>
	 *
	 * @en <b>{0}:</b> {1}<br/>
	 *     <b>ID:</b> {2}<br/>
	 *     {3}
	 */
	public static ResKey4 MODULE_TOOLTIP;

	/**
	 * Parameters:
	 * <ul>
	 * <li>Typ des Typs</li>
	 * <li>Name des Typs</li>
	 * <li>Name des Moduls</li>
	 * <li>ID des Typs</li>
	 * <li>Beschreibung</li>
	 * </ul>
	 * 
	 * @en <b>{0}:</b> {1} ({2})<br/>
	 *     <b>ID:</b> {3}<br/>
	 *     {4}
	 */
	public static ResKey5 TYPE_TOOLTIP;

	/**
	 * Parameters:
	 * <ul>
	 * <li>Typ des Parts</li>
	 * <li>Name des Parts</li>
	 * <li>Name des Definitionstyps</li>
	 * <li>ID des Parts</li>
	 * <li>Beschreibung</li>
	 * </ul>
	 *
	 * @en <b>{0}:</b> {1} ({2})<br/>
	 *     <b>ID:</b> {3}<br/>
	 *     {4}
	 */
	public static ResKey5 TYPE_PART_TOOLTIP;

	static {
		initConstants(I18NConstants.class);
	}
}