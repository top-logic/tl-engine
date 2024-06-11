/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} for historic references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoricStorage<C extends HistoricStorage.Config<?>> extends AbstractStorage<C> {

	/**
	 * Configuration options for {@link HistoricStorage}.
	 */
	@TagName("historic-storage")
	public interface Config<I extends HistoricStorage<?>> extends AbstractStorage.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Suffix to this attributes name to construct a storage attribute name, in which the branch of
	 * the referenced object is stored.
	 */
	public static final String KO_ATT_SUFFIX_BRANCH = AbstractMOReference.KO_ATT_SUFFIX_BRANCH;

	/**
	 * Suffix to this attributes name to construct a storage attribute name, in which the revision
	 * of the referenced object is stored.
	 * 
	 * @see #KO_ATT_SUFFIX_BRANCH
	 */
	public static final String KO_ATT_SUFFIX_REVISION = AbstractMOReference.KO_ATT_SUFFIX_REVISION;

	/**
	 * Suffix to this attributes name to construct a storage attribute name, in which the object
	 * name of the referenced object is stored.
	 * 
	 * @see #KO_ATT_SUFFIX_BRANCH
	 */
	public static final String KO_ATT_SUFFIX_ID = AbstractMOReference.KO_ATT_SUFFIX_ID;

	/**
	 * Suffix to this attributes name to construct a storage attribute name, in which the type of
	 * the referenced object is stored.
	 * 
	 * @see #KO_ATT_SUFFIX_BRANCH
	 */
	public static final String KO_ATT_SUFFIX_TYPE = AbstractMOReference.KO_ATT_SUFFIX_TYPE;

	/**
	 * Creates a {@link HistoricStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HistoricStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		String theAttBase = attribute.getName();
		try {
			Long theBranchNo = (Long) object.tGetData(theAttBase + KO_ATT_SUFFIX_BRANCH);
			if (theBranchNo == null) { // Early return
				return null;
			}
			Object historyContext = object.tGetData(theAttBase + KO_ATT_SUFFIX_REVISION);
			Revision revision;
			if (historyContext == NextCommitNumberFuture.INSTANCE) {
				// reference to current object was requested before commit occurred
				revision = Revision.CURRENT;
			} else {
				revision = HistoryUtils.getRevision((Long) historyContext);
			}
			TLID theID = (TLID) object.tGetData(theAttBase + KO_ATT_SUFFIX_ID);
			String theType = (String) object.tGetData(theAttBase + KO_ATT_SUFFIX_TYPE);
			Branch theBranch = HistoryUtils.getBranch(theBranchNo.longValue());

			KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
			Wrapper theResult = WrapperFactory.getWrapper(theBranch, revision, theID, theType, theKB);
			return theResult;
		} catch (ClassCastException ex) {
			throw new AttributeException("Misconfigured attributed '" + attribute.getName()
				+ "': storage attribute type(s) invalid.", ex);
		} catch (RuntimeException ex) {
			throw new AttributeException("Problems getting value of attribute '" + attribute.getName() + "'.", ex);
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {

		String theAttBase = attribute.getName();
		if (aValue == null) {
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_BRANCH, null);
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_REVISION, null);
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_ID, null);
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_TYPE, null);
		}
		else {
			Wrapper theWrap = (Wrapper) aValue; // Guaranteed by checkSetValue
			Revision theRev = WrapperHistoryUtils.getRevision(theWrap);
			Object theRevNo;
			if (theRev.isCurrent()) {
				theRevNo = NextCommitNumberFuture.INSTANCE;
			} else {
				theRevNo = theRev.getCommitNumber();
			}

			long theBranchNo = WrapperHistoryUtils.getBranch(theWrap).getBranchId();
			TLID theID = KBUtils.getWrappedObjectName(theWrap);
			String theType = theWrap.tHandle().tTable().getName();

			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_BRANCH, Long.valueOf(theBranchNo));
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_REVISION, theRevNo);
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_ID, theID);
			aMetaAttributed.tSetData(theAttBase + KO_ATT_SUFFIX_TYPE, theType);
		}

		AttributeOperations.touch(aMetaAttributed, attribute);
	}

	@Override
	protected void checkSetValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Check attribute existance
		if (aMetaAttributed != null) {
			AttributeUtil.checkHasAttribute(aMetaAttributed, attribute);
		}

		// Check params
		if (aValue != null && !(aValue instanceof Wrapper)) {
			String type = aValue.getClass().getName();
			throw new IllegalArgumentException("Value is not a Wrapper but " + type + " " + this);
		}
	}

}
