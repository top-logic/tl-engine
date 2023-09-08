/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Internal implementation of {@link KnowledgeItemInternal} based on {@link AbstractKnowledgeItem}
 * depending on a given {@link DBKnowledgeBase}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractDBKnowledgeItem extends AbstractKnowledgeItem implements KnowledgeItemInternal {

	/**
	 * Constant to use in attribute value getter with revision to access data in current session
	 * revision.
	 * 
	 * @see #getAttributeValue(String, long)
	 * @see #getValue(MOAttribute, long)
	 * @see #getReferencedKey(MOReference, long)
	 */
	static final long IN_SESSION_REVISION = DBKnowledgeBase.IN_SESSION_REVISION;

	/**
	 * The identity of this object.
	 * 
	 * @see KnowledgeItemInternal#tId()
	 */
	private DBObjectKey _identity;

	/** @see #getKnowledgeBase() */
	private final DBKnowledgeBase _kb;

	/**
	 * Creates a new {@link AbstractDBKnowledgeItem}.
	 * 
	 * @param kb
	 *        see {@link #getKnowledgeBase()}
	 * @param type
	 *        see {@link #tTable()}
	 */
	public AbstractDBKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(type);
		_kb = kb;
	}
	
	@Override
	public DBKnowledgeBase getKnowledgeBase() {
		return _kb;
	}

	/**
	 * Initialises the {@link KnowledgeItemInternal#tId()} of this item.
	 * 
	 * <p>
	 * This method is called before this item becomes part of {@link KnowledgeBase} either after
	 * creation or after the item is loaded from persistent storage.
	 * </p>
	 * 
	 * @param newIdentity
	 *        The new {@link KnowledgeItemInternal#tId() identifier}.
	 */
	protected void initIdentifier(DBObjectKey newIdentity) {
		if (this._identity != null) {
			throw new IllegalStateException("Object identifiers can be initialized only once.");
		}
		_identity = newIdentity;
	}

	/**
	 * Initialises the {@link KnowledgeItem#tId() identifier} for a newly created item.
	 */
	protected final void initIdentifier(TLID id, long branch) {
		initIdentifier(new DBObjectKey(branch, Revision.CURRENT_REV, tTable(), id));
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tId()} instead
	 */
	@Deprecated
	@Override
	public final DBObjectKey getObjectKey() {
		return tId();
	}

	@Override
	public final DBObjectKey tId() {
		assert _identity != null : "Identity has not been initialized.";
		return _identity;
	}

	@Override
	public final MOKnowledgeItem tTable() {
		return (MOKnowledgeItem) super.tTable();
	}

	@Override
	public final ObjectKey getKnownKey(ObjectKey key) {
		return getKnowledgeBase().getCachedKey(key);
	}

	protected MOAttribute revMinAttribute() {
		return tTable().getAttributeOrNull(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
	}

	protected MOAttribute revMaxAttribute() {
		return tTable().getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
	}

	/**
	 * Retrieves the value of the given attribute.
	 * 
	 * @param attribute
	 *        the attribute to getValue from.
	 * @param values
	 *        The cache to get values.
	 * @param objectContext
	 *        The context in which the value is accessed.
	 */
	protected Object lookupValue(MOAttribute attribute, Object[] values, ObjectContext objectContext) {
		return attribute.getStorage().getApplicationValue(attribute, this, objectContext, values);
	}

	@Override
	public ObjectKey lookupKey(MOReference attribute, Object[] values) {
		return attribute.getStorage().getReferenceValueKey(attribute, this, values, tId());
	}

	/**
	 * Whether the attribute is a static in general modifiable attribute.
	 * 
	 * @param attributeName
	 *        The name of the attribute to check
	 */
	protected boolean isStaticAttribute(String attributeName) {
		if (tTable().hasAttribute(attributeName)) {
			return true;
		}
		return false;
	}

	/**
	 * Helper method to initialise the given attribute with the given values.
	 * 
	 * @param attribute
	 *        The {@link MOAttribute} to initialise.
	 * @param values
	 *        The container holding all values for this object.
	 * @param value
	 *        The initial value of the given attribute.
	 * @throws DataObjectException
	 *         Whether the given value is not valid for the given attribute.
	 */
	protected final void initAttribute(MOAttribute attribute, Object[] values, Object value)
			throws DataObjectException {
		// check that initial value can be set, because it may be given from outside of framework.
		AttributeStorage storage = attribute.getStorage();
		storage.checkAttributeValue(attribute, this, value);
		storage.initApplicationValue(attribute, this, this, values, value);
	}

	/**
	 * Creates a new empty storage object that can be used as data holder for this
	 * {@link AbstractDBKnowledgeItem}.
	 */
	protected final Object[] newEmptyStorage() {
		return AbstractDataObject.newStorage(tTable());
	}

	/**
	 * Returns the data which are currently visible.
	 * 
	 * <p>
	 * In contrast to {@link #getGlobalValues()}, the local values contains the modification which
	 * are potentially made in the current {@link DBContext}.
	 * </p>
	 * 
	 * <p>
	 * If the object is unmodified yet, it is possible that the values are the same as the global
	 * values. Therefore the values are <b>not</b> valid for modification.
	 * </p>
	 */
	protected abstract Object[] getLocalValues();

	/**
	 * Returns the data to modify during initialisation of the object. The returned array can be
	 * modified.
	 * 
	 * <p>
	 * This method must only be called during initialisation of the item.
	 * </p>
	 * 
	 * @param context
	 *        {@link DBContext} of the current thread.
	 */
	protected abstract Object[] getValuesForInitialisation(DBContext context);

	/**
	 * Returns the global values in the current session revision.
	 * 
	 * @see #getGlobalValues(long)
	 */
	@Override
	public Object[] getGlobalValues() {
		return getGlobalValues(IN_SESSION_REVISION);
	}

	/**
	 * Returns the data of the object in the given revision without any local modifications.
	 * 
	 * <p>
	 * The result is valid for read access in all threads, but <b>not</b> valid for write access.
	 * </p>
	 * 
	 * @param sessionRevision
	 *        The revision in which the data are requested, or {@value #IN_SESSION_REVISION} if the
	 *        values in session revision is needed.
	 * 
	 * @see #getGlobalValues()
	 */
	protected abstract Object[] getGlobalValues(long sessionRevision);

	/**
	 * Flushes local changes to be visible by anyone.
	 * 
	 * @param commitContext
	 *        The context in which changes occured.
	 */
	protected abstract void localCommit(DBContext commitContext);

	/**
	 * Removes local changes, such that they are also not longer included in
	 * {@link #getLocalValues()}.
	 */
	protected abstract void localRollback();

	/**
	 * Returns the {@link FlexDataManager} valid for the type of this object.
	 */
	protected FlexDataManager getFlexDataManager() {
		return getKnowledgeBase().getFlexDatamanager(tTable());
	}

	/**
	 * Utility method to load the attribute values into the given array.
	 */
	protected final void loadAttributeValues(ResultSet resultSet, int dbOffset, Object[] values) throws SQLException {
		// Load attribute values in the given result array
		ConnectionPool pool = getKnowledgeBase().getConnectionPool();
		for (MOAttribute attribute : tTable().getAttributes()) {
			loadAttribute(pool, resultSet, dbOffset, values, attribute);
		}
	}

	protected final void loadAttribute(ConnectionPool pool, ResultSet resultSet, int dbOffset, Object[] values,
			MOAttribute attribute) throws SQLException {
		attribute.getStorage().loadValue(pool, resultSet, dbOffset, attribute, this, values, this);
	}

	/**
	 * Loads the current database data into the {@link #getGlobalValues()}.
	 */
	protected abstract void loadAttributeValues(ResultSet resultSet, int dbOffset) throws SQLException;

	@Override
	public final ObjectKey getReferencedKey(MOReference reference) {
		return getReferencedKey(reference, IN_SESSION_REVISION);
	}

	@Override
	public ObjectKey getGlobalReferencedKey(MOReference reference) {
		return lookupKey(reference, getGlobalValues());
	}

	@Override
	public final Object getAttributeValue(String attributeName) throws NoSuchAttributeException {
		return getAttributeValue(attributeName, IN_SESSION_REVISION);
	}

	@Override
	public final Object getGlobalAttributeValue(String attribute) throws NoSuchAttributeException {
		return getGlobalAttributeValue(attribute, IN_SESSION_REVISION);
	}

	@Override
	public final Object getValue(MOAttribute attribute) {
		return getValue(attribute, IN_SESSION_REVISION);
	}
	
	/**
	 * Transforms {@link #IN_SESSION_REVISION} to the correct revision. Other values are not
	 * modified.
	 */
	protected long getRevision(long revision) {
		if (revision == IN_SESSION_REVISION) {
			return getKnowledgeBase().getSessionRevision();
		}
		return revision;
	}

	/**
	 * Service method to implement {@link KnowledgeItem#getCreateCommitNumber()}.
	 * 
	 * <p>
	 * Method accessed the value of {@link BasicTypes#REV_CREATE_ATTRIBUTE_NAME}. If the value is
	 * {@link NextCommitNumberFuture}, then the object is not persistent yet and
	 * {@link KnowledgeItem#NO_CREATE_REVISION} is returned.
	 * </p>
	 * 
	 * @param self
	 *        The implementor of {@link #getCreateCommitNumber()}.
	 */
	protected static long getCreateCommitNumber(KnowledgeItem self) {
		try {
			Object createAttributeValue = self.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME);
			if (createAttributeValue == NextCommitNumberFuture.INSTANCE) {
				return NO_CREATE_REVISION;
			} else {
				return ((Long) createAttributeValue).longValue();
			}
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}

	}

}

