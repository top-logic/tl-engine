/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * I18N constants for the agent package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when an agent or skill name uses the reserved {@code TL_} prefix.
	 *
	 * @en The prefix ''TL_'' is reserved for TopLogic platform definitions: {0}
	 * @de Das Präfix „TL_“ ist für TopLogic-Plattformdefinitionen reserviert: {0}
	 */
	public static ResKey1 RESERVED_PREFIX_ERROR__NAME;

	/**
	 * Error message when an agent or skill name is already in use.
	 *
	 * @en The name ''{0}'' is already in use.
	 * @de Der Name ''{0}'' ist bereits in Gebrauch.
	 */
	public static ResKey1 DUPLICATE_NAME_ERROR__NAME;

	/**
	 * Success message after exporting instances to the data folder.
	 *
	 * @en Instances exported to development environment.
	 * @de Instanzen in die Entwicklungsumgebung übernommen.
	 */
	public static ResKey EXPORT_TO_DEV_ENV_SUCCESS;

	/**
	 * Error message when exporting instances to the data folder fails.
	 *
	 * @en Failed to export instances to development environment.
	 * @de Fehler beim Übernehmen der Instanzen in die Entwicklungsumgebung.
	 */
	public static ResKey EXPORT_TO_DEV_ENV_ERROR;

	/**
	 * Success message after re-importing instances from the data folder, discarding any runtime
	 * edits and restoring the code-baked definition.
	 *
	 * @en Instances reset from development environment.
	 * @de Instanzen aus Entwicklungsumgebung zurückgesetzt.
	 */
	public static ResKey IMPORT_FROM_DEV_ENV_SUCCESS;

	/**
	 * Error message when re-importing instances from the data folder fails (file missing or
	 * unreadable, XML invalid, importer failure).
	 *
	 * @en Failed to reset instances from development environment.
	 * @de Fehler beim Zurücksetzen der Instanzen aus der Entwicklungsumgebung.
	 */
	public static ResKey IMPORT_FROM_DEV_ENV_ERROR;

	/**
	 * Message shown when the import is triggered but no exported data file exists yet.
	 *
	 * @en No development data found. Use "Apply to development environment" first to create the
	 *     export file.
	 * @de Keine Entwicklungsdaten gefunden. Bitte zuerst "In Entwicklungsumgebung übernehmen"
	 *     verwenden, um die Exportdatei zu erzeugen.
	 */
	public static ResKey IMPORT_FROM_DEV_ENV_NO_DATA;

	/**
	 * Error message when the exported data file exists but cannot be parsed.
	 *
	 * @en The development data file is invalid and could not be parsed.
	 * @de Die Entwicklungsdatendatei ist ungültig und konnte nicht gelesen werden.
	 */
	public static ResKey IMPORT_FROM_DEV_ENV_PARSE_ERROR;

	/**
	 * Warning header shown when an import completed but some values could not be imported.
	 *
	 * @en Import completed with problems: {0} issue(s) reported while importing {1} object(s). Some
	 *     data may be incomplete.
	 * @de Import mit Problemen abgeschlossen: {0} Problem(e) beim Importieren von {1} Objekt(en).
	 *     Einige Daten sind möglicherweise unvollständig.
	 */
	public static ResKey2 IMPORT_FROM_DEV_ENV_PARTIAL__PROBLEMS_TOTAL;

	/**
	 * Error message when a configuration names an agent the application does not have.
	 *
	 * @en Unknown agent(s): {0}. Pick the agents from the list.
	 * @de Unbekannte Agenten: {0}. Wählen Sie die Agenten aus der Liste.
	 */
	public static ResKey1 UNKNOWN_AGENTS__NAMES;

	/**
	 * Notice shown in place of an answer when the model ended its turn without one, see
	 * {@link StopReason#NO_ANSWER}.
	 *
	 * @en The agent stopped without an answer. Write "Please continue" to let it carry on.
	 * @de Der Agent hat ohne Antwort aufgehört. Schreiben Sie „Bitte weitermachen“, damit er
	 *     fortfährt.
	 */
	public static ResKey RUN_STOPPED_NO_ANSWER;

	/**
	 * Notice appended to an answer that was cut off at the model's output limit, see
	 * {@link StopReason#OUTPUT_LIMIT}.
	 *
	 * @en The answer is incomplete: it was cut off at the model's output limit. Write "Please
	 *     continue" to get the rest.
	 * @de Die Antwort ist unvollständig: Sie wurde an der Ausgabegrenze des Modells abgeschnitten.
	 *     Schreiben Sie „Bitte weitermachen“, um den Rest zu erhalten.
	 */
	public static ResKey RUN_STOPPED_OUTPUT_LIMIT;

	static {
		initConstants(I18NConstants.class);
	}
}
