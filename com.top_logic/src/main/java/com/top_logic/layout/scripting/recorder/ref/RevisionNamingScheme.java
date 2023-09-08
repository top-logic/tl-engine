/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.recorder.ref.value.RevisionName;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for technical {@link Revision} objects.
 * 
 * @see RevisionName
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevisionNamingScheme extends AbstractModelNamingScheme<Revision, RevisionName> {

	@Override
	public Class<RevisionName> getNameClass() {
		return RevisionName.class;
	}

	@Override
	public Class<Revision> getModelClass() {
		return Revision.class;
	}

	@Override
	public Revision locateModel(ActionContext context, RevisionName name) {
		return HistoryUtils.getRevision(asCommitNumber(name.getCommitNumber()));
	}

	@Override
	protected void initName(RevisionName name, Revision model) {
		name.setCommitNumber(asCommitNumberName(model.getCommitNumber()));
	}

	/**
	 * Special handling for <code>null</code>.
	 * 
	 * @see RevisionName#getCommitNumber()
	 * @see #asCommitNumberName(long)
	 */
	private long asCommitNumber(Long commitNumberName) {
		if (commitNumberName == null) {
			return Revision.CURRENT_REV;
		} else {
			return commitNumberName.longValue();
		}
	}

	/**
	 * Special handling for {@link Revision#CURRENT_REV}.
	 * 
	 * @see RevisionName#getCommitNumber()
	 * @see #asCommitNumber(Long)
	 */
	private Long asCommitNumberName(long commitNumber) {
		if (commitNumber == Revision.CURRENT_REV) {
			return null;
		} else {
			return Long.valueOf(commitNumber);
		}
	}

}
