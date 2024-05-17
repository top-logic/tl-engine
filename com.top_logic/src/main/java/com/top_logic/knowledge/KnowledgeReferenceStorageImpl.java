/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.attr.storage.MOReferenceStorageImpl;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Default {@link KIReferenceStorage} implementation for {@link KIReference} based on
 * {@link MOReferenceStorageImpl}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class KnowledgeReferenceStorageImpl extends MOReferenceStorageImpl implements KIReferenceStorage {

	/**
	 * Replacement for <code>null</code> values in mandatory revision and branch column.
	 */
	public static final Long NULL_REPLACEMENT = Long.valueOf(0);

	/**
	 * Value that is stored into the revision column of a mixed reference, when a current reference
	 * is stored.
	 */
	public static final long MIXED_REFERENCE_CURRENT_REPRESENTATION = Revision.CURRENT_REV;

	/**
	 * Dummy value to insert when actually no branch must be written.
	 * 
	 * @see #needsDummyValueForBranch(AbstractMOReference, ObjectKey)
	 */
	// Not null to allow defining column as not-null
	private static final long DUMMY_BRANCH_VALUE = Long.MIN_VALUE;

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object value) throws DataObjectException {
		super.checkAttributeValue(attribute, data, value);
		if (value != null) {
			if (!(value instanceof KnowledgeItem)) {
				throw new IncompatibleTypeException("Attribute '" + attribute.getName()
					+ "' is a reference so it needs an "
					+ KnowledgeItem.class.getName() + " as value, but '" + value + "' was given.");
			}

			KnowledgeItem kiValue = (KnowledgeItem) value;
			KnowledgeItem kiContext = (KnowledgeItem) data;

			checkValueAlive(kiValue);
			checkSameKnowledgeBase(kiContext, kiValue);

			final MetaObject valuesType = kiValue.tTable();
			MOReference reference = ref(attribute);
			final MetaObject modelsTargetType = reference.getMetaObject();

			checkLinkType(kiContext.tTable(), valuesType);

			checkType(ref(attribute), modelsTargetType, valuesType);
			checkHistoryContext(ref(attribute), kiValue);
			checkBranch(ref(attribute), kiContext, kiValue);
		}
	}

	/**
	 * Casts the given {@link MOAttribute} to {@link AbstractMOReference}.
	 * 
	 * @param attribute
	 *        The {@link AbstractMOReference} to cast.
	 * 
	 * @return The given attribute.
	 */
	protected AbstractMOReference ref(MOAttribute attribute) {
		return (AbstractMOReference) attribute;
	}

	private void checkBranch(MOReference attribute, KnowledgeItem context, KnowledgeItem value)
			throws IncompatibleTypeException {
		if (attribute.isBranchGlobal()) {
			return;
		}
		/* A value for a branch local attribute is valid if the value is a current reference and the
		 * branch of the context and the value are the same, or the value is a historic value and it
		 * lives on a previous branch b and the revision of the value is less or equal to the
		 * revision at which branch b '+' 1 was spawned from b. */
		final long contextBranchID = context.getBranchContext();
		final long valueBranchID = value.getBranchContext();
		if (contextBranchID == valueBranchID) {
			// objects live on the same branch, everything is ok.
			return;
		}

		final long valueHistoryContext = value.getHistoryContext();
		if (valueHistoryContext == Revision.CURRENT_REV) {
			// current value but different branches.
			StringBuilder error = new StringBuilder();
			error.append("Value must live in the same branch as the context: branch of value:");
			error.append(value.getBranchContext());
			error.append(", branch of context:");
			error.append(context.getBranchContext());
			throw new IncompatibleTypeException(error.toString());
		}

		// historic value
		final Branch contextBranch = HistoryUtils.getBranch(contextBranchID);
		Branch baseBranch = contextBranch;
		while (true) {
			if (valueHistoryContext <= baseBranch.getBaseRevision().getCommitNumber()) {
				assert baseBranch.getBaseBranch() != null : "Base branch of " + baseBranch
					+ " is null so it is TRUNK but value " + value + " has history context before TRUNK-create date";
				baseBranch = baseBranch.getBaseBranch();
				if (valueBranchID == baseBranch.getBranchId()) {
					break;
				}
			} else {
				StringBuilder error = new StringBuilder();
				error.append("Value ");
				error.append(value);
				error.append(" lives not on the same branch as ");
				error.append(context);
				error.append(" or some previous branch before correponding branch creation.");
				throw new IncompatibleTypeException(error.toString());
			}
		}
	}

	private void checkHistoryContext(MOReference attribute, KnowledgeItem value) throws IncompatibleTypeException {
		switch (attribute.getHistoryType()) {
			case CURRENT:
				if (value.getHistoryContext() != Revision.CURRENT_REV) {
					throw new IncompatibleTypeException("Value must be a current object: value:" + value);
				}
				break;
			default:
				// everything ok
				break;
		}
	}

	private void checkType(MOReference attribute, MetaObject modelsTargetType, MetaObject valuesType)
			throws IncompatibleTypeException {
		if (attribute.isMonomorphic()) {
			if (!Utils.equals(modelsTargetType, valuesType)) {
				StringBuilder error = new StringBuilder();
				error.append("Value must have type '");
				error.append(modelsTargetType);
				error.append("' but has '");
				error.append(valuesType);
				error.append("'");
				throw new IncompatibleTypeException(error.toString());
			}
		} else {
			if (!valuesType.isSubtypeOf(modelsTargetType)) {
				StringBuilder error = new StringBuilder();
				error.append("Value must have subtype of '");
				error.append(modelsTargetType);
				error.append("' but has '");
				error.append(valuesType);
				error.append("'");
				throw new IncompatibleTypeException(error.toString());
			}
		}
	}

	private void checkSameKnowledgeBase(KnowledgeItem context, KnowledgeItem value) throws DataObjectException {
		if (context.getKnowledgeBase() != value.getKnowledgeBase()) {
			throw new DataObjectException("Target object in different KnowledgeBase.");
		}
	}

	private void checkLinkType(MetaObject modelsTargetType, MetaObject valuesType) throws IncompatibleTypeException {
		boolean targetTypeVersioned = ((MOClass) modelsTargetType).isVersioned();
		boolean valueTypeVersioned = ((MOClass) valuesType).isVersioned();
		if (targetTypeVersioned && !valueTypeVersioned) {
			StringBuilder problem = new StringBuilder();
			problem.append("Cannot created versioned link '");
			problem.append(modelsTargetType.getName());
			problem.append("' to unversioned object '");
			problem.append(valuesType.getName());
			problem.append("'.");
			throw new IncompatibleTypeException(problem.toString());
		}
	}

	private void checkValueAlive(KnowledgeItem ki) throws DataObjectException {
		if (!ki.isAlive()) {
			String msg = "Can not set deleted object as reference '" + ki.tId() + "'.";
			throw new DataObjectException(msg);
		}
	}

	/**
	 * Returns a key that represents the object represented by the given key in the given context.
	 * 
	 * <p>
	 * Must be synchronous to
	 * {@link #loadObjectKey(DBHelper, ResultSet, int, AbstractMOReference, ObjectContext)}
	 * </p>
	 * 
	 * @see #loadObjectKey(DBHelper, ResultSet, int, AbstractMOReference, ObjectContext)
	 */
	protected ObjectKey adaptToContext(MOReference attribute, ObjectKey contextKey, ObjectKey cachedKey) {
		final long desiredHistoryContext = contextKey.getHistoryContext();
		final ObjectKey adaptedKey;
		if (cachedKey == null) {
			adaptedKey = null;
		} else {
			switch (attribute.getHistoryType()) {
				case CURRENT:
					/* The reference points to an object in the history context in which the referee
					 * lives. Therefore the key must be adapted to desired context. */
					if (cachedKey.getHistoryContext() != desiredHistoryContext) {
						adaptedKey = newAdaptedKey(desiredHistoryContext, cachedKey);
					} else {
						adaptedKey = cachedKey;
					}
					break;
				case HISTORIC:
					/* The reference points to a fixed history context. This must not be adapted to
					 * the desired context. */
					adaptedKey = cachedKey;
					break;
				case MIXED:
					if (HistoryUtils.isCurrent(cachedKey)) {
						/* if this is a reference containing a current object and someone resolves
						 * the value in a historic context it is necessary to adopt the object key
						 * as the reference must live in the same history context. */
						if (desiredHistoryContext != Revision.CURRENT_REV) {
							if (attribute.isBranchGlobal()) {
								adaptedKey = newAdaptedKey(desiredHistoryContext, cachedKey);
							} else {
								// see AbstractKIReference.DUMMY_BRANCH_VALUE
								long branchContext = contextKey.getBranchContext();
								adaptedKey = newAdaptedKey(branchContext, desiredHistoryContext, cachedKey);
							}
						} else {
							if (attribute.isBranchGlobal()) {
								adaptedKey = cachedKey;
							} else {
								// see AbstractKIReference.DUMMY_BRANCH_VALUE
								long branchContext = contextKey.getBranchContext();
								adaptedKey = newAdaptedKey(branchContext, cachedKey.getHistoryContext(), cachedKey);
							}
						}
					} else {
						/* The reference points to a fixed history context. This must not be adapted
						 * to the desired context. */
						adaptedKey = cachedKey;
					}
					break;
				default:
					throw HistoryType.noSuchType(attribute.getHistoryType());
			}
		}
		return adaptedKey;
	}

	/**
	 * Creates a new {@link ObjectKey} that has represents the same object as the original, but in
	 * the given history context.
	 */
	protected ObjectKey newAdaptedKey(long historyContext, ObjectKey original) {
		long branchContext = original.getBranchContext();
		return newAdaptedKey(branchContext, historyContext, original);
	}

	/**
	 * Creates a new {@link ObjectKey} that has represents the same object as the original, but in
	 * the given history context and the given branch.
	 */
	protected ObjectKey newAdaptedKey(long branchContext, long historyContext, ObjectKey original) {
		MetaObject objectType = original.getObjectType();
		TLID objectName = original.getObjectName();
		return KBUtils.createObjectKey(branchContext, historyContext, objectType, objectName);
	}

	/**
	 * Loads the {@link ObjectKey} of the referenced {@link IdentifiedObject} from the given
	 * database result.
	 * 
	 * <p>
	 * Must be synchronous to {@link #adaptToContext(MOReference, ObjectKey, ObjectKey)}
	 * </p>
	 * 
	 * @see #adaptToContext(MOReference, ObjectKey, ObjectKey)
	 */
	protected ObjectKey loadObjectKey(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			AbstractMOReference attribute, ObjectContext context) throws SQLException {
		TLID objectName = loadId(attribute, dbResult, resultOffset);
		ObjectKey value;
		if (objectName == null) {
			value = null;
		} else {
			long branch = loadBranch(dbResult, resultOffset, attribute, context);
			long historyContext = loadHistoryContext(sqlDialect, dbResult, resultOffset, attribute, context);
			MetaObject type = loadType(sqlDialect, dbResult, resultOffset, attribute, context);
			value = KBUtils.createObjectKey(branch, historyContext, type, objectName);
			value = context.getKnownKey(value);
		}
		return value;
	}

	/**
	 * Loads the identifier of the referenced object.
	 */
	private TLID loadId(AbstractMOReference attribute, ResultSet dbResult, int resultOffset) throws SQLException {
		return readId(attribute.idColumn(), dbResult, resultOffset);
	}

	/**
	 * Loads the branch of the referenced object.
	 * @param attribute
	 *        The {@link AbstractMOReference attribute} to load branch for.
	 * @param context
	 *        The context to get information from, when no branch column exists.
	 */
	private long loadBranch(ResultSet dbResult, int resultOffset, AbstractMOReference attribute, ObjectContext context)
			throws SQLException {
		final long branch;
		if (attribute.hasBranchColumn()) {
			long dbValue = fetchLong(attribute.branchColumn(), dbResult, resultOffset);
			if (dbValue == DUMMY_BRANCH_VALUE) {
				// dummy value in database.
				branch = context.tId().getBranchContext();
			} else {
				branch = dbValue;
			}
		} else {
			branch = context.tId().getBranchContext();
		}
		return branch;
	}

	/**
	 * Loads the type of the referenced object.
	 * 
	 * @param attribute
	 *        The {@link AbstractMOReference attribute} to load type for.
	 * @param context
	 *        The context to get the {@link ObjectContext#getTypeRepository() type repository} to
	 *        resolve the type if attribute is not monomorphic.
	 */
	private MetaObject loadType(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			AbstractMOReference attribute, ObjectContext context) throws SQLException {
		final MetaObject concreteType;
		if (!attribute.isMonomorphic()) {
			final String concreteTypeString =
				(String) readValue(attribute.typeColumn(), sqlDialect, dbResult, resultOffset);
			assert concreteTypeString != null : "Non null objectName but no target type number";
			try {
				concreteType = context.getTypeRepository().getMetaObject(concreteTypeString);
			} catch (UnknownTypeException ex) {
				throw (KnowledgeBaseRuntimeException) new KnowledgeBaseRuntimeException("Attribute '"
					+ attribute.getName()
					+ "' has a non existing type '" + concreteTypeString + "' as value.").initCause(ex);
			}
		} else {
			concreteType = attribute.getTargetType();
		}
		return concreteType;
	}

	/**
	 * Loads the history context of the referenced object.
	 * 
	 * @param attribute
	 *        The {@link AbstractMOReference attribute} to load history context for.
	 * @param context
	 *        The context to get information from, when no revision column exists.
	 * 
	 * @see #adaptToContext(MOReference, ObjectKey, ObjectKey)
	 */
	private long loadHistoryContext(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			AbstractMOReference attribute, ObjectContext context) throws SQLException {
		final long historyContext;
		switch (attribute.getHistoryType()) {
			case CURRENT: {
				/* If reference is load for an historic object, then the reference points to an
				 * object in the same historyContext. In this case no adapting to context is
				 * necessary for that is object. */
				historyContext = context.tId().getHistoryContext();
				break;
			}
			case HISTORIC: {
				assert attribute.hasRevisionColumn() : "Historic references need database representation for history context";
				Long dbValue = (Long) readValue(attribute.revColumn(), sqlDialect, dbResult, resultOffset);
				assert dbValue != null : "No revision whereas object is not null.";
				assert dbValue.longValue() != Revision.CURRENT_REV : "No current object in historic references.";
				historyContext = dbValue.longValue();
				break;
			}
			case MIXED: {
				assert attribute.hasRevisionColumn() : "Historic references need database representation for history context";
				Long dbValue = (Long) readValue(attribute.revColumn(), sqlDialect, dbResult, resultOffset);
				assert dbValue != null : "No revision whereas object is not null.";
				long loadedValue = dbValue.longValue();
				if (loadedValue == MIXED_REFERENCE_CURRENT_REPRESENTATION) {
					historyContext = Revision.CURRENT_REV;
				} else {
					historyContext = loadedValue;
				}
				break;
			}
			default:
				throw HistoryType.noSuchType(attribute.getHistoryType());
		}
		return historyContext;
	}

	@Override
	public ObjectKey getReferenceValueKey(MOReference attribute, DataObject item, Object[] storage, ObjectKey contextKey) {
		ObjectKey key = getReferencedKey(attribute, item, storage);
		return adaptToContext(ref(attribute), contextKey, key);
	}

	/**
	 * Stabilises the cached object to the given history context. When this method is called, it is
	 * ensured that the referenced object is not <code>null</code> and a current object.
	 * 
	 * @param attribute
	 *        The attribute this holding the referenced object.
	 * @param item
	 *        The item holding the given data.
	 * @param storage
	 *        The storage containing data to resolve cached object from.
	 * @param historyContext
	 *        The context to adapt the stored object to.
	 */
	protected abstract void stabilizeCachedValue(MOAttribute attribute, KnowledgeItem item, Object[] storage, long historyContext);

	/**
	 * @param attribute
	 *        The attribute this holding the referenced object.
	 * @param item
	 *        The item holding the reference.
	 * @param storage
	 *        The storage containing data to resolve cached object from.
	 * @return The object key of the currently referenced object.
	 */
	protected abstract ObjectKey getReferencedKey(MOAttribute attribute, DataObject item, Object[] storage);

	/**
	 * Stored the {@link ObjectKey} of the referenced {@link IdentifiedObject} to the database.
	 */
	protected void storeObjectKey(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute,
			KnowledgeItem item, Object[] storage, long currentCommitNumber, ObjectKey cachedKey) throws SQLException {
		if (cachedKey != null) {
			storeNonNullKey(stmtArgs, stmtOffset, attribute, item, storage, currentCommitNumber, cachedKey);
		} else {
			storeNullKey(stmtArgs, stmtOffset, attribute, item);
		}
	}

	private void storeNonNullKey(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute, KnowledgeItem item,
			Object[] storage, long currentCommitNumber, ObjectKey key) throws SQLException {
		storeId(stmtArgs, stmtOffset, attribute, item, key);
		storeType(stmtArgs, stmtOffset, attribute, item, key);
		storeHistoryContext(stmtArgs, stmtOffset, ref(attribute), item, storage, currentCommitNumber, key);
		storeBranch(stmtArgs, stmtOffset, ref(attribute), key);
	}

	private void storeHistoryContext(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute,
			KnowledgeItem item, Object[] storage, long currentCommitNumber, ObjectKey key) {
		switch (attribute.getHistoryType()) {
			case CURRENT:
				assert HistoryUtils.isCurrent(key) : "non historic references must reference to current objects: value:"
					+ getCacheValue(attribute, item, storage);
				break;
			case MIXED:
				if (HistoryUtils.isCurrent(key)) {
					storeLong(attribute.revColumn(), stmtArgs, stmtOffset, MIXED_REFERENCE_CURRENT_REPRESENTATION);
				} else {
					storeLong(attribute.revColumn(), stmtArgs, stmtOffset, key.getHistoryContext());
				}
				break;
			case HISTORIC:
				if (HistoryUtils.isCurrent(key)) {
					storeLong(attribute.revColumn(), stmtArgs, stmtOffset, currentCommitNumber);
					stabilizeCachedValue(attribute, item, storage, currentCommitNumber);
				} else {
					storeLong(attribute.revColumn(), stmtArgs, stmtOffset, key.getHistoryContext());
				}
				break;
		}
	}

	private void storeBranch(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute, ObjectKey key) {
		final long valuesBranchContext = key.getBranchContext();
		if (attribute.hasBranchColumn()) {
			if (needsDummyValueForBranch(attribute, key)) {
				storeLong(attribute.branchColumn(), stmtArgs, stmtOffset, DUMMY_BRANCH_VALUE);
			} else {
				storeLong(attribute.branchColumn(), stmtArgs, stmtOffset, valuesBranchContext);
			}
		}
	}

	/**
	 * The branch column exists for technical reasons. It is actually not necessary for branch local
	 * references, but included for references to historic objects to avoid searching about base
	 * branch borders. Therefore for {@link com.top_logic.dob.meta.MOReference.HistoryType#MIXED}
	 * references there is such a column which needs to be updated when object is branched. As that
	 * is not possible a dummy value is stored and it is transformed to context branch.
	 */
	private boolean needsDummyValueForBranch(AbstractMOReference attribute, ObjectKey key) {
		return attribute.getHistoryType() == HistoryType.MIXED && !attribute.isBranchGlobal()
			&& HistoryUtils.isCurrent(key);
	}

	private void storeType(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute,
			DataObject item, ObjectKey key) throws SQLException {
		final MetaObject modelsTargetType = attribute.getTargetType();
		final MetaObject valuesType = key.getObjectType();
		if (!attribute.isMonomorphic()) {
			assert valuesType.isSubtypeOf(modelsTargetType) : "value must have subtype of '" + modelsTargetType
				+ "' but has '" + valuesType + "'";
			storeObject(attribute.typeColumn(), stmtArgs, stmtOffset, item, valuesType.getName());
		} else {
			assert Utils.equals(modelsTargetType, valuesType) : "value must have type '" + modelsTargetType
				+ "' but has '" + valuesType + "'";
		}
	}

	private void storeId(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute, DataObject item,
			ObjectKey key) throws SQLException {
		storeTLID(attribute.idColumn(), stmtArgs, stmtOffset, item, key.getObjectName());
	}

	private void storeNullKey(Object[] stmtArgs, int stmtOffset, AbstractMOReference attribute,
			DataObject item) throws SQLException {
		storeTLID(attribute.idColumn(), stmtArgs, stmtOffset, item, IdentifierUtil.nullIdForMandatoryDatabaseColumns());
		if (!attribute.isMonomorphic()) {
			storeObject(attribute.typeColumn(), stmtArgs, stmtOffset, item, null);
		}
		if (attribute.hasRevisionColumn()) {
			storeObject(attribute.revColumn(), stmtArgs, stmtOffset, item, NULL_REPLACEMENT);
		}
		if (attribute.hasBranchColumn()) {
			storeObject(attribute.branchColumn(), stmtArgs, stmtOffset, item, NULL_REPLACEMENT);
		}
	}

	/**
	 * Checks whether the value of the given attribute is a current object.
	 */
	private Expression isCurrentCheck(MOReference attribute) {
		switch (attribute.getHistoryType()) {
			case CURRENT:
				return literal(Boolean.TRUE);
			case HISTORIC:
				return literal(Boolean.FALSE);
			case MIXED:
				return isCurrent(referenceTyped(attribute));
		}
		throw HistoryType.noSuchType(attribute.getHistoryType());
	}

	@Override
	public Expression getReferenceInSetExpression(MOReference attribute, MOClass searchType, MetaObject targetType,
			String itemSetParam) {
		MetaObject definedTargetType = attribute.getMetaObject();
		if (attribute.isMonomorphic()) {
			if (!targetType.equals(definedTargetType)) {
				return literal(Boolean.FALSE);
			}
		} else {
			if (!targetType.isSubtypeOf(definedTargetType)) {
				return literal(Boolean.FALSE);
			}
		}
		return inSet(reference(attribute), setParam(itemSetParam));
	}

	@Override
	public Expression getRefererExpression(MOReference attribute, MOClass searchType,
			MetaObject targetType, String parameter) {
		boolean typeCheck = isTypeMatch(attribute, targetType);
		if (!typeCheck) {
			return literal(Boolean.FALSE);
		}

		/* No need to include type expression, because the identifier is unique over all types. */
		Expression matchReference = null;
		matchReference = and(matchReference, getIDCheck(attribute, parameter));
		matchReference = and(matchReference, getHistoryCheck(attribute, searchType, parameter));
		matchReference = and(matchReference, getBranchCheck(ref(attribute), searchType, parameter));
		return matchReference;
	}

	private Expression getIDCheck(MOReference attribute, String parameter) {
		return eqBinary(referenceTyped(attribute, ReferencePart.name), identifier(param(parameter)));
	}

	private Expression getBranchCheck(AbstractMOReference attribute, MOStructure searchType, String parameter) {
		Expression referenceExpr;
		if (!attribute.targetTypeHasBranchColumn()) {
			referenceExpr = literal(TLContext.TRUNK_ID);
		} else if (attribute.hasBranchColumn()) {
			// branch of referenced object is stored within that column
			referenceExpr = referenceTyped(attribute, ReferencePart.branch);
		} else {
			MOAttribute branchAttribute = searchType.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME);
			referenceExpr = attributeTyped(branchAttribute);
		}
		return eqBinary(referenceExpr, branch(param(parameter)));
	}

	private Expression getHistoryCheck(MOReference attribute, MOStructure searchType, String parameter) {
		switch (attribute.getHistoryType()) {
			case CURRENT:
				return newRangeCheck(searchType, parameter);
			case HISTORIC:
				return hasStoredRevision(attribute, parameter);
			case MIXED:
				/* reference is filled with a current object, so the target object must have a
				 * revision which has the correct life range */
				Expression currentDBValue = and(isCurrentCheck(attribute), newRangeCheck(searchType, parameter));
				currentDBValue = and(currentDBValue,
					eqBinary(requestedHistoryContext(), historyContext(param(parameter))));
				/* reference is filled with a historic object, so the target object must have the
				 * stored revision */
				Expression historicDBValue =
					and(not(isCurrentCheck(attribute)), hasStoredRevision(attribute, parameter));
				return or(currentDBValue, historicDBValue);
		}
		throw HistoryType.noSuchType(attribute.getHistoryType());

	}

	/**
	 * Creates expression like:
	 * 
	 * <pre>
	 * rev(&quot;parameter&quot;) == xxx_REV
	 * </pre>
	 */
	private Expression hasStoredRevision(MOReference attribute, String parameter) {
		return eqBinary(revision(param(parameter)), referenceTyped(attribute, ReferencePart.revision));
	}

	/**
	 * Creates an expression that checks that the given parameter is between rev min and rev max
	 * (incl.):
	 * 
	 * <pre>
	 * REV_MIN &lt;= &quot;parameter&quot; &amp;&amp; REV_MAX &gt;= &quot;parameter&quot;
	 * </pre>
	 */
	private Expression newRangeCheck(MOStructure searchType, String parameter) {
		/* current reference could be evaluated within an historic object. In such case an object
		 * references to the given one if the life range contains the revision of the given one. */
		MOAttribute revMaxAttr = searchType.getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		MOAttribute revMinAttr = searchType.getAttributeOrNull(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
		Expression startRange = le(attributeTyped(revMinAttr), revision(param(parameter)));
		Expression stopRange = ge(attributeTyped(revMaxAttr), revision(param(parameter)));
		return and(startRange, stopRange);
	}

	private boolean isTypeMatch(MOReference attribute, MetaObject targetType) {
		MetaObject definedTargetType = attribute.getMetaObject();
		if (attribute.isMonomorphic()) {
			return targetType.equals(definedTargetType);
		} else {
			return targetType.isSubtypeOf(definedTargetType);
		}
	}

	/**
	 * Replaces the cache value with the {@link ObjectKey} of the given {@link IdentifiedObject} .
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to update cache for.
	 * @param oldCacheValue
	 *        The current stored {@link ObjectKey}.
	 */
	protected void updateCache(MOReference attribute, DataObject item, Object[] storage, Object oldCacheValue,
			IdentifiedObject cachedReference) {
		switch (attribute.getHistoryType()) {
			case CURRENT:
				// Update cache always with the key in the history context of the context
				// object. If more than one access in the same history context occurs this is
				// fast. It is no problem to store the key of a potential foreign context
				// because the value is always adapted on access to correct history context.
				setCacheValue(attribute, item, storage, oldCacheValue, cachedReference);
				break;
			case HISTORIC:
				// Update cache such that always the newest object key is cached.
				setCacheValue(attribute, item, storage, oldCacheValue, cachedReference);
				break;
			case MIXED:
				// It can not be decided whether stored value is a current or historic so
				// updating with an (potential) historic value would ruin decision possibility
				break;
		}
	}

	/**
	 * Updates the cache with the key of the given reference.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} for which the the cache value must be set.
	 * @param item
	 *        The item holding the values.
	 * @param storage
	 *        The storage to update cache value.
	 * @param oldCacheValue
	 *        The old cached value.
	 * @param cachedReference
	 *        The object used to resolve new stored cache value.
	 */
	private void setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object oldCacheValue,
			IdentifiedObject cachedReference) {
		ObjectKey internalKey = cachedReference.tId();
		if (oldCacheValue != internalKey) {
			setCacheValue(attribute, item, storage, internalKey);
		}
	}

}
