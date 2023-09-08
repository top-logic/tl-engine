/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link DBKnowledgeItem} that supports attributes that are not declared in the type of the item.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class DynamicKnowledgeItem extends DBKnowledgeItem implements DynamicAttributedObject {

	public DynamicKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
	}

	@Override
	public FlexDataManager getFlexDataManager() {
		return super.getFlexDataManager();
	}

	/**
	 * Returns the global dynamic values for the given revision.
	 * 
	 * <p>
	 * Note: Must not be called when item is new, because then there are no global values.
	 * </p>
	 */
	private DynamicValues dynamicValues(long revision) {
		return (DynamicValues) findValues(getRevision(revision));
	}

	/**
	 * Atomically set the global dynamic value to the given updated value if currently no values are
	 * set.
	 * 
	 * @param update
	 *        the new value
	 */
	@Override
	public void initDynamicValues(long dataRevision, FlexData update) {
		initDynamicValues(update, dataRevision);
	}

	private void initDynamicValues(FlexData update, long revision) {
		if (isPersistent()) {
			dynamicValues(revision).initDynamicValues(update);
		}
	}

	@Override
	public boolean needsToBeLoaded(long revision) {
		if (isPersistent()) {
			return dynamicValues(revision).needsToBeLoaded(revision);
		} else {
			return false;
		}
	}

	/**
	 * Returns the dynamic values, visible for all sessions.
	 * 
	 * @param revision
	 *        The revision in which the dynamic values are needed.
	 */
	private FlexData getGlobalDynamicValues(long revision) {
		FlexData dynamicValues;
		if (isPersistent()) {
			dynamicValues = dynamicValues(revision).getDynamicValues();
		} else {
			dynamicValues = NoFlexData.INSTANCE;
		}
		return dynamicValues;
	}

	@Override
	Object getAttributeValue(DBContext modificationContext, String attributeName, long revision,
			ObjectContext objectContext) throws NoSuchAttributeException {
		if (isStaticAttribute(attributeName)) {
			try {
				return super.getAttributeValue(modificationContext, attributeName, revision, objectContext);
			} catch (NoSuchAttributeException ex) {
				throw new UnreachableAssertion("Attribute '" + attributeName + "' is a KO attribute", ex);
			}
		} else {
			checkAlive(modificationContext, revision);
			return getDynamicValue(modificationContext, attributeName, revision);
		}
	}

	private Object getDynamicValue(DBContext modificationContext, String dynamicAttributeName, long revision) {
		FlexData attributeData = this.getDataObjectForRead(modificationContext, revision);
		if (!attributeData.hasAttribute(dynamicAttributeName)) {
			return null;
		}
		return getDynamicValue(attributeData, dynamicAttributeName);
	}

	@Override
	public Object setAttributeValue(String attributeName, Object newValue) throws DataObjectException {
		MOAttribute attribute = tTable().getAttributeOrNull(attributeName);
		if (attribute != null) {
			return setValue(attribute, newValue);
		} else {
			return setDynamicValue(attributeName, newValue, false);
		}
	}

	@Override
	protected void initAttribute(String name, Object[] localValues, Object value) throws DataObjectException {
		MOAttribute attribute = tTable().getAttributeOrNull(name);
		if (attribute != null) {
			super.initAttribute(name, localValues, value);
		} else {
			setDynamicValue(name, value, true);
		}

	}

	/**
	 * Set the given value in the data object representing this instance.
	 * 
	 * @param dynamicAttributeName
	 *        The name of the attribute to be used for storage.
	 * @param newValue
	 *        The value to be stored.
	 * @param attributeInitialising
	 *        Whether the attribute is currently initialising.
	 * 
	 * @see AbstractDBKnowledgeItem#initAttribute(MOAttribute, Object[], Object)
	 */
	private Object setDynamicValue(String dynamicAttributeName, Object newValue, boolean attributeInitialising)
			throws DataObjectException {
		DBContext currentModificationContext = getCurrentDBContext();
		checkLocallyModifiable(currentModificationContext);
		Object oldValue = getDynamicValue(currentModificationContext, dynamicAttributeName, IN_SESSION_REVISION);
		if (CollectionUtil.equals(oldValue, newValue)) {
			return oldValue;
		}
		
		FlexData updatedData = this.getDataObjectForWrite(currentModificationContext);
		Object oldAttributeValue = updatedData.setAttributeValue(dynamicAttributeName, newValue);
		if (currentModificationContext == null) {
			/* This may be the first change. Therefore the context may not be exist before. */
			currentModificationContext = getCurrentDBContext();
			handleAfterDynamicUpdate(currentModificationContext, dynamicAttributeName, oldValue, newValue);
		}
		return oldAttributeValue;
	}

	private void handleAfterDynamicUpdate(DBContext context, String attributeName, Object oldValue, Object newValue) {
		context.notifyDynamicAttributeValueChange(this, attributeName, oldValue, newValue);
	}

	/**
	 * Lookup or load the persistent attribute data.
	 * 
	 * @param revision
	 *        The revision of the data to fetch.
	 * 
	 * @return the persistent state of this object's data without any uncomitted changes. Must not
	 *         be modified.
	 */
	private FlexData lookupPersistentState(long revision) {
		FlexData cachedGlobalState = getGlobalDynamicValues(revision);
		if (cachedGlobalState != null) {
			return cachedGlobalState;
		}
		synchronized (this) {
			// fetch session revision only once.
			revision = getRevision(revision);
			// check whether other thread has already fetched the values.
			cachedGlobalState = getGlobalDynamicValues(revision);
			if (cachedGlobalState != null) {
				return cachedGlobalState;
			}
			FlexData persistentValues = loadPersistentDynamicValues(revision);
			initDynamicValues(persistentValues, revision);
			return getGlobalDynamicValues(revision);
		}
	}

	private FlexData loadPersistentDynamicValues(long revision) {
		FlexData newPersistentData;
		if (!isPersistent()) {
			// new object so no data available.
			newPersistentData = NoFlexData.INSTANCE;
		} else {
			newPersistentData = getFlexDataManager().load(getKnowledgeBase(), tId(), revision, false);
		}
		return newPersistentData;
	}

	/**
	 * Returns the value for the given attribute in the given {@link FlexData}.
	 * 
	 * @param attributeData
	 *        The values to get attribute value from
	 * @param dynamicAttributeName
	 *        The name of the attribute to resolve value for. It must be ensured that the given
	 *        {@link FlexData} has a value for the given attribute.
	 */
	private static Object getDynamicValue(FlexData attributeData, String dynamicAttributeName)
			throws UnreachableAssertion {
		return attributeData.getAttributeValue(dynamicAttributeName);
	}

	/**
	 * Return the data object used for writing the additional attributes.
	 * 
	 * @param modificationContext
	 *        {@link DBContext} for which the {@link FlexData} are demanded. May be
	 *        <code>null</code> if the object is not yet locked.
	 * 
	 * @return The data object holding the data of this instance, never <code>null</code>.
	 */
	private FlexData getDataObjectForWrite(DBContext modificationContext) {
		checkAlive(modificationContext);

		FlexData localDynamicValues = null;
		if (modificationContext != null) {
			FlexData knownLocalValues = modificationContext.getLocalDynamicValues(this);
			if (knownLocalValues != null) {
				localDynamicValues = knownLocalValues;
			}
		} else {
			modificationContext = createDBContext();
		}

		if (localDynamicValues == null) {
			// Touch the object for modification.
			internalTouch(modificationContext);
			localDynamicValues = AbstractFlexData.createMutableCopy(lookupPersistentState(IN_SESSION_REVISION));
			modificationContext.initLocalDynamicValues(this, localDynamicValues);
		}
		return localDynamicValues;
	}

	/**
	 * Return the data object used for reading the additional attributes.
	 * 
	 * @param modificationContext
	 *        Context in which this object is currently modified. May be <code>null</code> in case
	 *        object is not modified.
	 * @param revision
	 *        If no modification context is given, then the data of that revision are returned.
	 * 
	 * @return The data object holding the data of this instance. Not <code>null</code>.
	 */
	private final FlexData getDataObjectForRead(DBContext modificationContext, long revision) {
		FlexData readValues;
		if (modificationContext != null) {
			FlexData localChanges = modificationContext.getLocalDynamicValues(this);
			if (localChanges != null) {
				readValues = localChanges;
			} else {
				readValues = lookupPersistentState(revision);
			}
		} else {
			readValues = lookupPersistentState(revision);
		}
		return readValues;

	}

	@Override
	public Set<String> getAllAttributeNames() {
		Set<String> staticAttributes = super.getAllAttributeNames();
		FlexData dataObjectForRead = getDataObjectForRead(getCurrentDBContext(), IN_SESSION_REVISION);
		staticAttributes.addAll(dataObjectForRead.getAttributes());
		return staticAttributes;
	}

	@Override
	FlexData getLocalDynamicValues(CommitContext commitContext) {
		return ((DBContext) commitContext).getLocalDynamicValues(this);
	}

	@Override
	public ItemChange getChange() {
		ItemChange change = super.getChange();

		DBContext modificationContext = getCurrentDBContext();
		if (!isAlive(modificationContext)) {
			// item is removed. Therefore the global dynamic values must be reported.
			handleDeletion(change);
			return change;
		}

		if (modificationContext == null) {
			// No values have been changed in the the current context, neither KO attributes nor
			// dynamic attributes.
			return change;
		}

		FlexData localDynamicValues = modificationContext.getLocalDynamicValues(this);
		if (localDynamicValues == null) {
			// no dynamic value change modification
			return change;
		}
		FlexData globalDynamicValues = getGlobalDynamicValues(IN_SESSION_REVISION);
		if (change == null) {
			// no ko attribute has changed
			ObjectBranchId objectId = ObjectBranchId.toObjectBranchId(tId());
			change = new ItemUpdate(ChangeInspectable.NO_REVISION, objectId, true);
		}
		Set<String> persistentAttributes = new HashSet<>(globalDynamicValues.getAttributes());
		for (String dynamicAttribute : localDynamicValues.getAttributes()) {
			Object newDynamicValue = getDynamicValue(localDynamicValues, dynamicAttribute);
			boolean attributeIsPersistent = persistentAttributes.remove(dynamicAttribute);
			Object oldDynamicValue;
			if (attributeIsPersistent) {
				oldDynamicValue = getDynamicValue(globalDynamicValues, dynamicAttribute);
			} else {
				oldDynamicValue = null;
			}
			change.setValue(dynamicAttribute, oldDynamicValue, newDynamicValue);
		}
		for (String removedAttribute : persistentAttributes) {
			Object removedValue = getDynamicValue(globalDynamicValues, removedAttribute);
			change.setValue(removedAttribute, removedValue, null);
		}
		return change;
	}

	private void handleDeletion(ItemChange deletion) {
		FlexData persistentDynamicData = lookupPersistentState(IN_SESSION_REVISION);
		for (String attribute : persistentDynamicData.getAttributes()) {
			Object persistentValue = getDynamicValue(persistentDynamicData, attribute);
			deletion.setValue(attribute, persistentValue, null);
		}
	}

	@Override
	protected DynamicValues newValues(long revMin, long revMax, Object[] storage) {
		return new DynamicValuesImpl(revMin, revMax, storage);
	}

	@Override
	protected Values newValues(DBContext commitContext, State currentState) {
		DynamicValues newValues = (DynamicValues) super.newValues(commitContext, currentState);
		newValues.setDynamicValues(getNewDynamicValue(commitContext, currentState));
		return newValues;
	}

	private FlexData getNewDynamicValue(DBContext commitContext, State currentState) throws UnreachableAssertion {
		FlexData localDynamicValues = commitContext.getLocalDynamicValues(this);
		if (localDynamicValues != null) {
			if (((MutableFlexData) localDynamicValues).containsBinaryData()) {
				/* Must re-fetch global state to get stable binary data. */
				return null;
			} else {
				// ensure that data can not be changed.
				return AbstractFlexData.makeImmutable(localDynamicValues);
			}
		} else {
			switch (currentState) {
				case NEW: {
					return NoFlexData.INSTANCE;
				}
				case PERSISTENT: {
					return ((DynamicValues) getValues()).getDynamicValues();
				}
				default: {
					throw new UnreachableAssertion("No such state.");
				}
			}
		}
	}

	@Override
	protected DynamicValues deletedValuesAt(long revision) {
		return new DeletedDynamicValues(revision, revision);
	}

	@Override
	protected DynamicValues deletedValuesFrom(long revision) {
		return new DeletedDynamicValues(revision, Revision.CURRENT_REV);
	}

}
