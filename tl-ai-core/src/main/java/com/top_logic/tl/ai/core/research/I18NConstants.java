/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * I18N constants of a kept research run, which a person reads.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The line under a research report's title.
	 *
	 * @en Research report, {0}
	 * @de Recherchebericht, {0}
	 */
	public static ResKey1 REPORT_META__DATE;

	/**
	 * The status of a research report that does not answer everything.
	 *
	 * @en Partial: the research stopped before it had answered everything.
	 * @de Unvollständig: Die Recherche hat aufgehört, bevor sie alles beantwortet hatte.
	 */
	public static ResKey PARTIAL;

	/**
	 * The heading of the sources a research report cites.
	 *
	 * @en Sources
	 * @de Quellen
	 */
	public static ResKey SOURCES;

	/**
	 * The link to the copy of a web page a research run read.
	 *
	 * @en Archived copy
	 * @de Archivierte Kopie
	 */
	public static ResKey ARCHIVED_COPY;

	/**
	 * When a source of a research report was read.
	 *
	 * @en Read {0}
	 * @de Gelesen am {0}
	 */
	public static ResKey1 READ_AT__DATE;

	/**
	 * The name of a source of a research run whose page or object was deleted.
	 *
	 * @en {0} (deleted)
	 * @de {0} (gelöscht)
	 */
	public static ResKey1 DELETED__NAME;

	/**
	 * The name of a web source of a research run whose archived page was deleted.
	 *
	 * @en Deleted web page
	 * @de Gelöschte Webseite
	 */
	public static ResKey DELETED_PAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
