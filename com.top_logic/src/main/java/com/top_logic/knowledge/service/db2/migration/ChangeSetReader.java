/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Messages;
import com.top_logic.util.message.MessageStoreFormat;

/**
 * Reads a {@link ChangeSet} from the underlying reader.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetReader extends AbstractDumpReader implements com.top_logic.knowledge.event.ChangeSetReader {

	/**
	 * Creates a new {@link ChangeSetReader}.
	 */
	public ChangeSetReader(TypeMapping typeMapper, XMLStreamReader reader) {
		super(typeMapper, reader);
	}

	/**
	 * Reads the current {@link ChangeSet}.
	 */
	@Override
	public ChangeSet read() {
		try {
			ensureLocalName(CHANGE_SET);

			long revision = readLongAttribute(REVISION_ATTR);
			ChangeSet cs = new ChangeSet(revision);

			String author = readAttribute(AUTHOR_ATTR);
			long commitTimeMillis = toDate(readAttribute(DATE_ATTR)).getTime();
			String logMessage = readAttribute(MESSAGE_ATTR);
			if (StringServices.isEmpty(logMessage)) {// may happen for older revisions
				// default log message as it is mandatory now
				logMessage = MessageStoreFormat.toString(Messages.NO_COMMIT_MESSAGE);
			}
			cs.setCommit(new CommitEvent(revision, author, commitTimeMillis, logMessage));

			readTag();
			while (hasStartTag()) {
				String localName = _reader.getLocalName();
				if (CREATION.equals(localName)) {
					ObjectBranchId id = readId();
					if (id == null) {
						XMLStreamUtil.skipToMatchingEndTag(_reader);
					} else {
						ObjectCreation evt = new ObjectCreation(revision, id);
						fillProperties(evt);
						cs.addCreation(evt);
					}
				} else if (UPDATE.equals(localName)) {
					ObjectBranchId id = readId();
					if (id == null) {
						XMLStreamUtil.skipToMatchingEndTag(_reader);
					} else {
						ItemUpdate evt = new ItemUpdate(revision, id, false);
						fillProperties(evt);
						cs.addUpdate(evt);
					}
				} else if (DELETION.equals(localName)) {
					ObjectBranchId id = readId();
					if (id == null) {
						XMLStreamUtil.skipToMatchingEndTag(_reader);
					} else {
						ItemDeletion evt = new ItemDeletion(revision, id);
						fillProperties(evt);
						cs.addDeletion(evt);
					}
				} else if (BRANCH.equals(localName)) {
					long branchId = readLongAttribute(BRANCH_ID_ATTR);
					long baseBranchId = readLongAttribute(BASE_BRANCH_ID_ATTR);
					long baseRevisionNumber = readLongAttribute(BASE_REV_ATTR);
					BranchEvent evt = new BranchEvent(revision, branchId, baseBranchId, baseRevisionNumber);
					readBranchedTypes(evt);
					cs.addBranchEvent(evt);
				} else {
					throw fail("Expecting one of '" + CREATION + "', '" + UPDATE + "', '" + DELETION + "', or '"
						+ BRANCH + "', got: " + localName);
				}

				assertEndTag();
				readTag();
			}
			assertEndTag();
			return cs;
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		} catch (ParseException ex) {
			throw toRuntime(ex);
		}
	}

	private long readLongAttribute(String localName) {
		return Long.parseLong(readAttribute(localName));
	}

	private Date toDate(String value) throws ParseException {
		return (Date) XmlDateTimeFormat.INSTANCE.parseObject(value);
	}

	private void readBranchedTypes(BranchEvent evt) throws XMLStreamException {
		readTag();
		if (hasStartTag()) {
			Set<String> branchedTypes = new HashSet<>();
			while (hasStartTag()) {
				ensureLocalName(TYPE);

				branchedTypes.add(readAttribute(TYPE_NAME_ATTR));

				readEndTag();
				readTag();
			}
			evt.setBranchedTypeNames(branchedTypes);
		}
	}

	@Override
	public void close() {
		try {
			_reader.close();
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		}
	}

}

