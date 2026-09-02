/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution.layout;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * I18N constants for the agent activity monitor.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Label of the command requesting cancellation of a running agent run.
	 *
	 * @en Request cancellation
	 * @de Abbruch anfordern
	 * @tooltip Asks the node executing this run to stop it. The run ends as CANCELLED at its next
	 *          agent-loop turn, so this does not take effect instantly.
	 */
	public static ResKey REQUEST_CANCEL;

	/**
	 * Message shown after the cancel marker was written.
	 *
	 * @en Cancellation requested. The run ends at its next turn.
	 * @de Abbruch angefordert. Der Lauf endet mit seinem nächsten Turn.
	 */
	public static ResKey REQUEST_CANCEL_SENT;

	/**
	 * Message shown when there was nothing left to cancel.
	 *
	 * @en The run is no longer running, or a cancellation was already requested.
	 * @de Der Lauf läuft nicht mehr, oder ein Abbruch wurde bereits angefordert.
	 */
	public static ResKey REQUEST_CANCEL_NOT_RUNNING;

	/**
	 * Reason the cancel command is disabled because nothing is selected.
	 *
	 * @en Select a run first.
	 * @de Zuerst einen Lauf auswählen.
	 */
	public static ResKey REQUEST_CANCEL_DISABLED_NO_RUN;

	/**
	 * Reason the cancel command is disabled because the execution store is not running here.
	 *
	 * @en The agent execution store is not active, so a run cannot be cancelled.
	 * @de Der Dienst für Agentenausführungen ist nicht aktiv, ein Lauf kann nicht abgebrochen werden.
	 */
	public static ResKey REQUEST_CANCEL_DISABLED_NO_STORE;

	/**
	 * Reason the cancel command is disabled for a run that has already ended.
	 *
	 * @en The run has already ended.
	 * @de Der Lauf ist bereits beendet.
	 */
	public static ResKey REQUEST_CANCEL_DISABLED_TERMINAL;

	/**
	 * Reason the cancel command is disabled for a run whose cancellation is already pending.
	 *
	 * @en A cancellation has already been requested for this run.
	 * @de Für diesen Lauf wurde bereits ein Abbruch angefordert.
	 */
	public static ResKey REQUEST_CANCEL_DISABLED_PENDING;

	static {
		initConstants(I18NConstants.class);
	}
}
