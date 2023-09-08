/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Superclass for {@link AbstractDBKnowledgeItem} that can not be modified using the
 * {@link KnowledgeItem} API.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class ImmutableKnowledgeItem extends AbstractDBKnowledgeItem {

	private boolean _persistent;

	/**
	 * Creates a {@link ImmutableKnowledgeItem}.
	 */
	public ImmutableKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

	@Override
	public void delete() throws DataObjectException {
		throw new IllegalStateException("Immutable items can not be deleted.");
	}

	@Override
	public State getState() {
		return _persistent ? State.PERSISTENT : State.NEW;
	}

	@Override
	public boolean isAlive() {
		// Immutable object are always alive.
		return true;
	}

	@Override
	public void checkAlive(DBContext context) throws DeletedObjectAccess {
		// Immutable object are always alive.
	}

	@Override
	public void touch() {
		// Immutable objects can not be locked.
		throw new IllegalStateException("Immutable object '" + this + "' can not be locked.");
	}

	@Override
	protected final Object[] getLocalValues() {
		// no changes, so local values == global values
		return getGlobalValues();
	}

	@Override
	protected Object[] getValuesForInitialisation(DBContext context) {
		return getLocalValues();
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		throw new IllegalStateException("Immutable items are not modifiable.");
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		throw new IllegalStateException("Immutable items are not modifiable.");
	}

	@Override
	public Object getValue(MOAttribute attribute, long revision) {
		// value does not depend on revision, because this item is immutable
		return lookupValue(attribute, getLocalValues(), this);
	}

	@Override
	public Object getAttributeValue(String attrName, long revision) throws NoSuchAttributeException {
		// value does not depend on revision, because this item is immutable
		return getValue(tTable().getAttribute(attrName));
	}

	@Override
	public Object getGlobalAttributeValue(String attribute, long revision) throws NoSuchAttributeException {
		// This object is immutable. No difference between global and local value
		return getAttributeValue(attribute, revision);
	}

	@Override
	public ObjectKey getReferencedKey(MOReference reference, long revision) {
		// value does not depend on revision, because this item is immutable
		return lookupKey(reference, getLocalValues());
	}

	@Override
	public void onLoad(PooledConnection readConnection) {
		_persistent = true;
	}

	@Override
	protected void localCommit(DBContext commitContext) {
		_persistent = true;
	}

	@Override
	protected void localRollback() {
		// no changes so nothing to rollback locally
	}

	@Override
	protected void loadAttributeValues(ResultSet resultSet, int dbOffset) throws SQLException {
		loadAttributeValues(resultSet, dbOffset, getGlobalValues());
	}

}

