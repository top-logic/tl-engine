/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.KnowledgeBaseConverter;
import com.top_logic.knowledge.event.convert.KnowledgeBaseCopy;
import com.top_logic.knowledge.event.convert.KnowledgeEventConverter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * Base class for test cases that simulate a schema migration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDBKnowledgeBaseMigrationTest extends AbstractDBKnowledgeBaseClusterTest {

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseMigrationTestSetup(self);
	}

	/**
	 * Gets the {@link ChangeSetReader} to get {@link ChangeSet} that describe the history of the
	 * given {@link KnowledgeBase} from the given start revision to the last revision of the
	 * {@link KnowledgeBase}.
	 */
	protected ChangeSetReader getEventsSince(KnowledgeBase kb, Revision startRevision) {
		return getEventsInRange(kb, startRevision, Revision.CURRENT);
	}

	/**
	 * Gets the {@link ChangeSetReader} to get {@link ChangeSet} that describe the history of the
	 * given {@link KnowledgeBase} from the given start revision to the given stop revision.
	 */
	protected ChangeSetReader getEventsInRange(KnowledgeBase kb, Revision startRevision, Revision stopRevision) {
		return kb.getChangeSetReader(ReaderConfigBuilder.createConfig(startRevision, stopRevision));
	}

	/**
	 * Executes migration from {@link #kb()} to {@link #kbNode2()} from the given first revision to
	 * {@link Revision#CURRENT}.
	 * 
	 * @see #doMigration(Revision, Revision)
	 */
	protected void doMigration(Revision firstRevision) {
		doMigration(firstRevision, Revision.CURRENT);
	}

	/**
	 * Executes migration from {@link #kb()} to {@link #kbNode2()} from the given first revision to
	 * the given last revision.
	 * 
	 * @see #doMigration()
	 */
	protected void doMigration(Revision firstRevision, Revision lastRevision) {
		new KnowledgeBaseCopy(kb(), kbNode2()).convert(firstRevision, lastRevision);
		updateNode2();
	}

	/**
	 * Executes complete migration from {@link #kb()} to {@link #kbNode2()}
	 * 
	 * @see #doMigration(Revision, Revision)
	 */
	protected void doMigration() {
		new KnowledgeBaseCopy(kb(), kbNode2()).convert();
		updateNode2();
	}

	/**
	 * Executes migration from {@link #kb()} to {@link #kbNode2()} from the given first revision to
	 * {@link Revision#CURRENT} routing events through the given {@link EventRewriter}.
	 * 
	 * @see #doMigration(List, Revision,Revision)
	 */
	protected void doMigration(List<? extends EventRewriter> rewriters, Revision firstRevision) {
		doMigration(rewriters, firstRevision, Revision.CURRENT);
	}

	/**
	 * Executes migration from {@link #kb()} to {@link #kbNode2()} from the given first revision to
	 * the given last revision routing events through the given {@link EventRewriter}.
	 * 
	 * @see #doMigration(List)
	 */
	protected void doMigration(List<? extends EventRewriter> rewriters, Revision firstRevision, Revision lastRevision) {
		new KnowledgeBaseConverter(kb(), kbNode2(), rewriters).convert(firstRevision, lastRevision);
		updateNode2();
	}

	/**
	 * Executes complete migration from {@link #kb()} to {@link #kbNode2()} routing events through
	 * the given {@link EventRewriter}.
	 * 
	 * @see #doMigration(List, Revision, Revision)
	 */
	protected void doMigration(List<? extends EventRewriter> rewriters) {
		new KnowledgeBaseConverter(kb(), kbNode2(), rewriters).convert();
		updateNode2();
	}

	/**
	 * Translates the given events to be adequate for the given {@link KnowledgeBase} and replay
	 * them.
	 */
	protected void replay(Iterable<ChangeSet> changeSets, DBKnowledgeBase kb) {
		try (EventWriter replayWriter = kb.getReplayWriter()) {
			EventWriter converter = KnowledgeEventConverter.createEventConverter(kb.getMORepository(), replayWriter);
			for (ChangeSet cs : changeSets) {
				converter.write(cs);
			}
		}
	}

	private void updateNode2() {
		try {
			updateSessionRevisionNode2();
		} catch (MergeConflictException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

}
