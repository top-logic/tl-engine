/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import com.google.inject.Inject;

import com.top_logic.base.context.DefaultSessionContext;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapper;
import com.top_logic.util.message.MessageStoreFormat;

/**
 * {@link EventRewriter} migrating the author and commit message of {@link ChangeSet}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevisionUpgrade implements EventRewriter {

	/** Log message before version TL 5.7.0 */
	private static final String PRE_57_LOG_MESSAGE = "*** no message ***";

	private IdMapper _idMapper;

	private final String _noCommitMessage;

	/**
	 * Creates a new {@link RevisionUpgrade}.
	 */
	public RevisionUpgrade() {
		_noCommitMessage = MessageStoreFormat.toString(Messages.NO_COMMIT_MESSAGE);
	}

	@Inject
	void setIdMapper(IdMapper idMapper) {
		_idMapper = idMapper;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		CommitEvent commit = cs.getCommit();
		String author = commit.getAuthor();
		TLID authorId = _idMapper.mapId(StringID.valueOf(author));
		if (authorId != null) {
			commit.setAuthor(DefaultSessionContext.PERSON_ID_PREFIX + IdentifierUtil.toExternalForm(authorId));
		}

		if (PRE_57_LOG_MESSAGE.equals(commit.getLog())) {
			commit.setLog(_noCommitMessage);
		}

		out.write(cs);
	}

}
