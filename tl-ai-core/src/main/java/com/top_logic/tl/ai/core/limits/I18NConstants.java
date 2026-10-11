/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;

/**
 * I18N constants for the run limits.
 *
 * <p>
 * What a user is told when their run does not start, or stops early, because a ceiling this
 * installation set was reached. Written for the person in front of the chat: they say what happened
 * and what to do next. A token or run count is never named - it is an administrator's setting in a
 * unit the user cannot relate to, and quoting it invites an argument about a number they cannot
 * change. The money budget is the exception: an amount of money is a figure every user reads, and
 * "12,40 € of 15,00 €" says at once how far over it they are.
 * </p>
 *
 * @see RunLimits
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Refusal shown when the installation is carrying as many runs as it may.
	 *
	 * @en Too many agent runs are in progress right now. Please try again in a moment.
	 * @de Es laufen derzeit zu viele Agentenläufe. Bitte versuchen Sie es in einem Moment erneut.
	 */
	public static ResKey TOO_MANY_RUNS_IN_PROGRESS;

	/**
	 * Refusal shown when the acting account already has as many runs going as it may.
	 *
	 * @en You already have {0} agent runs in progress. Please wait for one of them to finish.
	 * @de Sie haben bereits {0} Agentenläufe in Bearbeitung. Bitte warten Sie, bis einer davon
	 *     abgeschlossen ist.
	 */
	public static ResKey1 TOO_MANY_RUNS_FOR_ACCOUNT__COUNT;

	/**
	 * Refusal shown when the acting account has spent its token budget for the window.
	 *
	 * @en Your agent budget for the last {0} hours is used up. It frees up again as those hours
	 *     pass; an administrator can raise it.
	 * @de Ihr Agenten-Budget für die letzten {0} Stunden ist aufgebraucht. Es wird wieder frei,
	 *     sobald diese Stunden vergangen sind; ein Administrator kann es erhöhen.
	 */
	public static ResKey1 TOKEN_BUDGET_SPENT__HOURS;

	/**
	 * Notice appended to the answer of a run that stopped because the budget ran out while it was
	 * working.
	 *
	 * @en The answer is incomplete: your agent budget for the last {0} hours ran out while this
	 *     run was working.
	 * @de Die Antwort ist unvollständig: Ihr Agenten-Budget für die letzten {0} Stunden war
	 *     während dieses Laufs aufgebraucht.
	 */
	public static ResKey1 RUN_STOPPED_BUDGET_SPENT__HOURS;

	/**
	 * Refusal shown when the acting account has spent its money budget for the window.
	 *
	 * <p>
	 * {0} is the spent amount, {1} the budget, both already formatted in the user's language;
	 * {2} is the window in hours.
	 * </p>
	 *
	 * @en Budget used up: {0} of {1} in the last {2} hours. It frees up again as those hours pass;
	 *     an administrator can raise it.
	 * @de Budget aufgebraucht: {0} von {1} in den letzten {2} Stunden. Es wird wieder frei, sobald
	 *     diese Stunden vergangen sind; ein Administrator kann es erhöhen.
	 */
	public static ResKey3 MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS;

	/**
	 * Notice appended to the answer of a run that stopped because the money budget ran out while it
	 * was working.
	 *
	 * <p>
	 * {0} is the spent amount, {1} the budget, both already formatted in the user's language;
	 * {2} is the window in hours.
	 * </p>
	 *
	 * @en The answer is incomplete. Budget used up: {0} of {1} in the last {2} hours.
	 * @de Die Antwort ist unvollständig. Budget aufgebraucht: {0} von {1} in den letzten {2}
	 *     Stunden.
	 */
	public static ResKey3 RUN_STOPPED_MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS;

	static {
		initConstants(I18NConstants.class);
	}
}
