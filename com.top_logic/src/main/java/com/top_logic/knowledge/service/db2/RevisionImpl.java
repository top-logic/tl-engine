/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLStructuredType;
import com.top_logic.util.TLContext;

/**
 * {@link KnowledgeItem} implementation for {@link Revision}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class RevisionImpl extends DBSystemObject implements Revision {

	private long _commitNumber;

	private String _author;

	private String _log;

	private long _date;

	/* package protected */RevisionImpl(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

	/*package protected*/ void initNew(long commitNumber, String author, long date, String log) {
		initValues(commitNumber, author, date, log);
		TLID objectName = LongID.valueOf(commitNumber);
		initIdentifier(objectName, TLContext.TRUNK_ID);
	}

	/**
	 * Initialises the values of this {@link RevisionImpl}.
	 * 
	 * @param commitNumber
	 *        see {@link #getCommitNumber()}
	 * @param author
	 *        see {@link #getAuthor()}
	 * @param date
	 *        see {@link #getDate()}
	 * @param log
	 *        see {@link #getLog()}
	 */
	void initValues(long commitNumber, String author, long date, String log) {
		initCommitNumber(commitNumber);
		initAuthor(author);
		initDate(date);
		initLog(log);
	}

	void initLog(String log) {
		_log = log;
	}

	void initDate(long date) {
		_date = date;
	}

	void initAuthor(String author) {
		_author = author;
	}

	void initCommitNumber(long commitNumber) {
		_commitNumber = commitNumber;
	}

	@Override
	public long getCommitNumber() {
		return _commitNumber;
	}

	@Override
	public String getAuthor() {
		return _author;
	}
	
	@Override
	public String getLog() {
		return _log;
	}
	
	@Override
	public long getDate() {
		return _date;
	}
	
	@Override
	public boolean isCurrent() {
		return false;
	}
	
	@Override
	public String toString() {
		return toString(this);
	}

	static String toString(Revision self) {
		return "r" + self.getCommitNumber();
	}
	
	@Override
	public long getCreateCommitNumber() {
		return getCommitNumber();
	}

	/**
	 * Redeclared to resolve conflict of {@link Revision#tType()} and {@link StaticItem#tType()}.
	 * 
	 * @see com.top_logic.knowledge.service.db2.StaticItem#tType()
	 * @see com.top_logic.knowledge.service.Revision#tType()
	 */
	@Override
	public TLStructuredType tType() {
		return Revision.super.tType();
	}

	/**
	 * The persistent type of cross references from revisions to modified objects within that
	 * revision.
	 */
	public static MOKnowledgeItemImpl newRevisionXRefType() {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(RevisionXref.REVISION_XREF_TYPE_NAME);
		type.setAbstract(false);
		MOKnowledgeItemUtil.setSystem(type, true);
		type.setVersioned(false);
		MOAttributeImpl revRef =
			IdentifierTypes.newRevisionReference(RevisionXref.XREF_REV_ATTRIBUTE, "REV", MOAttribute.IMMUTABLE);
		MOAttributeImpl branchRef = IdentifierTypes.newBranchReference(RevisionXref.XREF_BRANCH_ATTRIBUTE, "BRANCH");
		MOAttributeImpl typeRef = IdentifierTypes.newTypeReference(RevisionXref.XREF_TYPE_ATTRIBUTE, "TYPE");
		try {
			type.addAttribute(revRef);
			type.addAttribute(branchRef);
			type.addAttribute(typeRef);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		type.setPrimaryKey(revRef, branchRef, typeRef);

		return type;
	}

}
