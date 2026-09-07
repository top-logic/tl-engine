/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Categorizes what kind of state change a tool makes.
 */
/*
 * READ, USER_SCOPED, DATA_WRITE and STRUCTURAL form one axis: how deeply a tool changes *this*
 * application's own state, from not at all to its schema. They are ordered along it, and the gates
 * that read them - the transaction, the production ban - are about this application's data.
 *
 * EXTERNAL is not a point on that axis. A tool that talks to another system leaves this
 * application's state untouched and reaches something no gate here owns: the remote side commits by
 * itself, so no transaction of ours can undo it, and its data is not our structure, so the
 * production ban on STRUCTURAL says nothing about it. The one consequence that does apply is the
 * one an irreversible outward side effect deserves - a human is asked first.
 *
 * It is therefore last, not somewhere in the middle: the ordering above means something, and
 * inserting a value that is not on that axis into it would claim a severity relation that does not
 * exist. Last is also the safe position - the configuration framework defaults an unset enum
 * property to the *first* constant, and READ has to stay that one so that no omission can turn into
 * a silent grant.
 */
public enum ToolMode {

	/** Tool only reads data. No changes, no approval needed. */
	READ,

	/** Tool makes changes scoped to the current user only (temp views, personal dashboards). */
	USER_SCOPED,

	/** Tool writes shared application data (instances, records). */
	DATA_WRITE,

	/** Tool modifies application structure (model, schema, annotations). */
	STRUCTURAL,

	/** Tool communicates with a system outside this application. */
	EXTERNAL;

	/**
	 * Whether a tool in this mode changes state that this application's own transaction covers.
	 */
	/*
	 * The one question two places have to answer the same way, or they contradict each other: the
	 * agent loop asks it to decide whether a call opens the turn's transaction, and the conversation
	 * log asks it to decide whether a completed call is retracted when that turn rolls back.
	 * Answering "no" in the first and "yes" in the second would tell the model a write was undone
	 * that was never in a transaction to begin with.
	 *
	 * EXTERNAL answers no, for the same reason READ does, though not for the same cause: READ wrote
	 * nothing, EXTERNAL wrote something this application cannot reach. Either way the rollback does
	 * not touch it - and for EXTERNAL that is the honest answer, because the remote effect really did
	 * happen and really does stand.
	 */
	public boolean writesApplicationState() {
		return switch (this) {
			case READ, EXTERNAL -> false;
			case USER_SCOPED, DATA_WRITE, STRUCTURAL -> true;
		};
	}

}
