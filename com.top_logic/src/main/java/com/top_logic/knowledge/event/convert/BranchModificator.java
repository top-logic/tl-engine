/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * The class {@link BranchModificator} is an {@link EventRewriter} which updates {@link BranchEvent}
 * s and attributes which represent a branch.
 * 
 * @see RevisionModificator
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BranchModificator extends RewritingEventVisitor {

	/**
	 * The class {@link BranchAttributes} maps the name of a type to the set of attributes which
	 * represents branch IDs. If there is no such attribute <code>null</code> may be returned.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static interface BranchAttributes extends Mapping<String, Set<String>> {

		/**
		 * A {@link BranchAttributes} which can be used if no type has an attribute which represents
		 * a branch ID.
		 */
		public static final BranchAttributes NO_BRANCH_ATTRIBUTES = new BranchAttributes() {

			/**
			 * Constantly returns <code>null</code>
			 */
			@Override
			public Set<String> map(String input) {
				return null;
			}
		};
	}

	/**
	 * Maps the one branch id to the adopted one. If some key has no mapping then no adoption is
	 * needed.
	 */
	private Map<Long, Long> _idModifications;

	/**
	 * Declaration of branch attributes by type
	 */
	private final BranchAttributes _branchAttrsByType;

	/**
	 * modification value
	 */
	private long _modification = 0L;

	/**
	 * Increases the modification by 1
	 */
	public void increaseBranchID() {
		_modification++;
	}

	/**
	 * Decreases the modification by 1
	 */
	public void decreaseBranchID() {
		_modification--;
	}

	/**
	 * Creates a {@link BranchModificator} which modifies attributes declared by the given
	 * {@link BranchAttributes}
	 * 
	 * @param branchAttributes
	 *        declaration of the attributes which represents branch ID and have to be adopted. must
	 *        not be <code>null</code>
	 */
	public BranchModificator(BranchAttributes branchAttributes) {
		this._branchAttrsByType = branchAttributes;
	}

	@Override
	public Object visitBranch(BranchEvent event, Void arg) {
		if (_modification != 0L) {
			final long branchID = event.getBranchId();
			final long modifiedID = branchID + _modification;
			assert modifiedID > 0: "Modification of " + event + " would result in invalid branch ID " + modifiedID;
			putModified(branchID, modifiedID);

			event.setBranchId(modifiedID);
		}
		final long baseBranchId = event.getBaseBranchId();
		final Long modifiedID = modifyBranchID(baseBranchId);
		event.setBaseBranchId(modifiedID.longValue());
		return super.visitBranch(event, arg);
	}

	private void putModified(final long branchID, final long modifiedID) {
		if (_idModifications == null) {
			_idModifications = new HashMap<>();
		}
		_idModifications.put(branchID, modifiedID);
	}

	private Long getModified(final Long branchID) {
		if (_idModifications == null) {
			return null;
		}
		return _idModifications.get(branchID);
	}

	@Override
	protected Object visitItemEvent(ItemEvent event, Void arg) {
		final ObjectBranchId objectId = event.getObjectId();
		final long branchId = objectId.getBranchId();
		final Long modifyBranchID = modifyBranchID(branchId);
		if (modifyBranchID.longValue() != branchId) {
			final MetaObject objectType = objectId.getObjectType();
			final TLID objectName = objectId.getObjectName();
			final ObjectBranchId modifiedID = new ObjectBranchId(modifyBranchID.longValue(), objectType, objectName);
			event.setObjectId(modifiedID);
		}
		return super.visitItemEvent(event, arg);
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		final String type = event.getObjectType().getName();
		final Map<String, Object> values = event.getValues();
		update(type, values);
		return super.visitItemChange(event, arg);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		final String type = event.getObjectType().getName();
		final Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			// old values may be null
			update(type, oldValues);
		}
		return super.visitUpdate(event, arg);
	}

	/**
	 * Updates the values of an object of the given type, i.e. if some attribute represents a branch
	 * ID it is updated or if some value represents an object (means the value is an
	 * {@link ObjectKey}) it is also updated.
	 * 
	 * @param type
	 *        the type to determine attributes which represents branch attributes.
	 * @param values
	 *        the values of an object of the given type
	 */
	protected void update(final String type, final Map<String, Object> values) {
		final Collection<String> brcAttrNames = _branchAttrsByType.map(type);
		if (brcAttrNames == null) {
			for (Entry<String, Object> entry : values.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof ObjectKey) {
					updateObjectKey(entry, (ObjectKey) value);
				}
			}
		} else {
			for (Entry<String, Object> entry : values.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof ObjectKey) {
					updateObjectKey(entry, (ObjectKey) value);
				} else {
					if (brcAttrNames.contains(entry.getKey())) {
						updateBranchNumber(entry, value);
					}
				}
			}
		}
	}

	/**
	 * Checks that the given value is a {@link Long}, modifies it and updates the {@link Entry}.
	 * 
	 * @param entry
	 *        the entry to update
	 * @param value
	 *        the current value of the given entry
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given value does not represent a branch id
	 * 
	 * @see #modifyBranchID(Long)
	 */
	protected final void updateBranchNumber(final Entry<String, Object> entry, final Object value) {
		final Long brcNumber;
		try {
			brcNumber = (Long) value;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Attribute " + entry.getKey()
				+ " has unexpected value type: " + value + " is not a branch number.", ex);
		}
		entry.setValue(modifyBranchID(brcNumber));
	}

	/**
	 * Updates the value of the {@link Entry}. This is done by transforming the given
	 * {@link ObjectKey} (if necessary). The {@link ObjectKey#getBranchContext()} will be adopted.
	 * 
	 * @param entry
	 *        the entry to update
	 * @param value
	 *        the current value of the given entry
	 * 
	 * @see #modifyBranchID(Long)
	 */
	protected final void updateObjectKey(final Entry<String, Object> entry, final ObjectKey value) {
		final long branchContext = value.getBranchContext();
		final Long modified = modifyBranchID(branchContext);
		if (modified != branchContext) {
			final long historyContext = value.getHistoryContext();
			final MetaObject objectType = value.getObjectType();
			final TLID objectName = value.getObjectName();
			entry.setValue(new DefaultObjectKey(modified, historyContext, objectType, objectName));
		}
	}

	/**
	 * Returns the modified branch id
	 * 
	 * @param branchID
	 *        the id to modify
	 */
	public final Long modifyBranchID(Long branchID) {
		final Long modifiedID = getModified(branchID);
		if (modifiedID == null) {
			return branchID;
		}
		return modifiedID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(BranchModificator.class.getName());
		builder.append('[');
		builder.append("currentModification:").append(_modification);
		builder.append(",mapping:").append(_idModifications);
		builder.append(']');
		return builder.toString();
	}

}

