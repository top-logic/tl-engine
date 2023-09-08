/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.kafka.knowledge.service.TLSyncRecord;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.service.db2.migration.ChangeSetDumper;

/**
 * The {@link KafkaLogWriter} for {@link TLSyncRecord}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class TLSyncRecordLogWriter implements KafkaLogWriter<TLSyncRecord<ChangeSet>> {

	private static final String TL_SYNC_RECORD = "tl-sync-record";

	private static final String SYSTEM_ID = "system-id";

	private static final String LAST_MESSAGE_REVISION = "last-message-revision";

	private static final String CHANGESET = "changeset";

	private static final String REVISION = "revision";

	private static final String CREATIONS = "creations";

	private static final String UPDATES = "updates";

	private static final String DELETIONS = "deletions";

	private static final String BRANCH_CREATIONS = "branch-creations";

	private static final String COMMIT = "commit";

	private static final String AUTHOR = "author";

	private static final String DATE = "date";

	private static final String KIND = "kind";

	private static final String MESSAGE = "message";

	/** The {@link TLSyncRecordLogWriter} instance. */
	public static final TLSyncRecordLogWriter INSTANCE = new TLSyncRecordLogWriter();

	@Override
	public void writeMetaData(TagWriter output, TLSyncRecord<ChangeSet> message) {
		output.beginTag(TL_SYNC_RECORD);
		{
			KafkaLogUtil.writeTextTag(output, SYSTEM_ID, message.getSystemId());
			KafkaLogUtil.writeTextTag(output, LAST_MESSAGE_REVISION, message.getLastMessageRevision());
			output.beginTag(CHANGESET);
			writeMetaData(output, message.getRecord());
			output.endTag(CHANGESET);
		}
		output.endTag(TL_SYNC_RECORD);
	}

	private void writeMetaData(TagWriter output, ChangeSet changeSet) {
		KafkaLogUtil.writeTextTag(output, REVISION, changeSet.getRevision());
		KafkaLogUtil.writeTextTag(output, CREATIONS, changeSet.getCreations().size());
		KafkaLogUtil.writeTextTag(output, UPDATES, changeSet.getUpdates().size());
		KafkaLogUtil.writeTextTag(output, DELETIONS, changeSet.getDeletions().size());
		KafkaLogUtil.writeTextTag(output, BRANCH_CREATIONS, changeSet.getBranchEvents().size());
		output.beginTag(COMMIT);
		write(output, changeSet.getCommit());
		output.endTag(COMMIT);
	}

	private void write(TagWriter output, CommitEvent commitEvent) {
		KafkaLogUtil.writeTextTag(output, AUTHOR, commitEvent.getAuthor());
		/* The date has to be human readable. */
		KafkaLogUtil.writeTextTag(output, DATE, TimeUtil.toStringEpoche(commitEvent.getDate()));
		KafkaLogUtil.writeTextTag(output, KIND, commitEvent.getKind());
		KafkaLogUtil.writeTextTag(output, MESSAGE, commitEvent.getLog());
		KafkaLogUtil.writeTextTag(output, REVISION, commitEvent.getRevision());
	}

	@Override
	public void writeAllData(TagWriter output, TLSyncRecord<ChangeSet> message) {
		output.beginTag(TL_SYNC_RECORD);
		{
			KafkaLogUtil.writeTextTag(output, SYSTEM_ID, message.getSystemId());
			KafkaLogUtil.writeTextTag(output, LAST_MESSAGE_REVISION, message.getLastMessageRevision());
			writeAllData(output, message.getRecord());
		}
		output.endTag(TL_SYNC_RECORD);
	}

	private void writeAllData(TagWriter output, ChangeSet changeset) {
		/* Don't call "close()" on the ChangeSetDumper: It would close the TagWriter, but the
		 * TagWriter comes from outside and is still used after this method. Additionally, it is not
		 * necessary to close the ChangeSetDumper, as it has no resources that need to be closed. It
		 * uses solely the given TagWriter. */
		@SuppressWarnings("resource")
		ChangeSetDumper changeSetDumper = new ChangeSetDumper(output);
		try {
			changeSetDumper.writeChangeSet(changeset);
		} catch (IOException exception) {
			logError(exception, "Failed to dump changeset. Cause: " + exception.getMessage());
			output.writeText("ERROR: " + exception.getMessage());
		}
	}

	private void logError(IOException exception, String errorMessage) {
		Logger.error(errorMessage, exception, TLSyncRecordLogWriter.class);
	}

}
