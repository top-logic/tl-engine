/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link AbstractDumpWriter} dumping {@link ChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetDumper extends AbstractDumpWriter {

	private final boolean _skipUnversioned;

	/**
	 * Creates a new {@link ChangeSetDumper}.
	 * 
	 * @param out
	 *        The writer to dump to.
	 */
	public ChangeSetDumper(TagWriter out) {
		this(out, false);
	}

	/**
	 * Creates a new {@link ChangeSetDumper}.
	 * 
	 * @param out
	 *        The writer to dump to.
	 * @param skipUnversioned
	 *        Whether events for unversiond objects must not be dumped.
	 */
	public ChangeSetDumper(TagWriter out, boolean skipUnversioned) {
		super(out);
		_skipUnversioned = skipUnversioned;
	}

	/**
	 * Writes a single {@link ChangeSet} to the dump file.
	 * 
	 * @param cs
	 *        The {@link ChangeSet} to write.
	 */
	public void writeChangeSet(ChangeSet cs) throws IOException {
		_out.beginBeginTag(CHANGE_SET);
		_out.writeAttribute(REVISION_ATTR, cs.getRevision());
		CommitEvent commit = cs.getCommit();
		_out.writeAttribute(AUTHOR_ATTR, commit.getAuthor());
		_out.writeAttribute(DATE_ATTR, XmlDateTimeFormat.INSTANCE.format(new Date(commit.getDate())));
		_out.writeAttribute(MESSAGE_ATTR, commit.getLog());
		_out.endBeginTag();
		for (BranchEvent branch : cs.getBranchEvents()) {
			dumpBranch(branch);
		}
		for (ItemDeletion deletion : cs.getDeletions()) {
			dumpDeletion(deletion);
		}
		for (ObjectCreation creation : cs.getCreations()) {
			dumpCreation(creation);
		}
		for (ItemUpdate update : cs.getUpdates()) {
			dumpUpdate(update);
		}
		_out.endTag(CHANGE_SET);
	}

	private void dumpBranch(BranchEvent event) {
		_out.beginBeginTag(BRANCH);
		_out.writeAttribute(BRANCH_ID_ATTR, event.getBranchId());
		_out.writeAttribute(BASE_BRANCH_ID_ATTR, event.getBaseBranchId());
		_out.writeAttribute(BASE_REV_ATTR, event.getBaseRevisionNumber());
		_out.endBeginTag();
		Set<String> branchedTypeNames = event.getBranchedTypeNames();
		if (branchedTypeNames != null) {
			for (String typeName : branchedTypeNames) {
				_out.beginBeginTag(TYPE);
				_out.writeAttribute(TYPE_NAME_ATTR, typeName);
				_out.endEmptyTag();
			}
		}
		_out.endTag(BRANCH);
	}

	private void dumpDeletion(ItemDeletion deletion) throws IOException {
		if (skipDump(deletion)) {
			return;
		}
		_out.beginBeginTag(DELETION);
		dumpIdAttribute(deletion.getObjectId());
		_out.endBeginTag();
		dumpValues(deletion.getValues(), null);
		_out.endTag(DELETION);
	}

	private void dumpCreation(ObjectCreation creation) throws IOException {
		if (skipDump(creation)) {
			return;
		}
		ObjectBranchId id = creation.getObjectId();
		_out.beginBeginTag(CREATION);
		dumpIdAttribute(id);
		_out.endBeginTag();
		dumpValues(creation.getValues(), null);
		_out.endTag(CREATION);
	}

	private void dumpUpdate(ItemUpdate update) throws IOException {
		if (skipDump(update)) {
			return;
		}
		ObjectBranchId id = update.getObjectId();
		_out.beginBeginTag(UPDATE);
		dumpIdAttribute(id);
		_out.endBeginTag();
		dumpValues(update.getValues(), update.getOldValues());
		_out.endTag(UPDATE);
	}

	private boolean skipDump(ItemEvent event) {
		if (_skipUnversioned && unversionedType(event)) {
			return true;
		}
		return false;
	}

	private boolean unversionedType(ItemEvent event) {
		MetaObject type = event.getObjectId().getObjectType();
		return type instanceof MOClass && !((MOClass) type).isVersioned();
	}

}

