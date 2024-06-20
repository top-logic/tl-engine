/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.dob.sql.SimpleDBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * {@link AbstractMOReference} that references {@link KnowledgeItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KIReference extends AbstractMOReference implements MOReferenceInternal {

	/** @see #getStorage() */
	private KIReferenceStorage _storage;

	/**
	 * Creates a new {@link KIReference}.
	 * 
	 * @param name
	 *        see {@link #getName()}
	 * @param targetType
	 *        see {@link #getTargetType()}
	 * 
	 * @see AbstractMOReference#AbstractMOReference(String, MetaObject)
	 */
	private KIReference(String name, MetaObject targetType) {
		super(name, targetType);
	}

	/**
	 * Creates a new {@link KIReference} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link KIReference}.
	 */
	public KIReference(InstantiationContext context, ReferenceAttributeConfig config) {
		super(context, config);
		AttributeStorage storage = config.getStorage();
		if (storage == null) {
			// Not set explicit
			Decision byValue = config.isReferenceByValue();
			if (byValue == Decision.TRUE) {
				storage = ByValueReferenceStorageImpl.INSTANCE;
			} else {
				storage = ByIDReferenceStorageImpl.INSTANCE;
			}
		}
		setStorage(storage);
	}

	/**
	 * Creates a new {@link DBAttribute} for the given
	 * {@link com.top_logic.dob.meta.MOReference.ReferencePart}.
	 */
	@Override
	protected DBAttribute newDBAttribute(ReferencePart part) {
		DBMetaObject dbType = getType(part);
		String dbName = getColumnName(part);
		return new SimpleDBAttribute(this, dbType, dbName, dbType.getDefaultSQLType().binaryParam, isNotNull(part));
	}

	private boolean isNotNull(ReferencePart part) {
		switch (part) {
			case branch:
				return true;
			case name:
				return true;
			case revision:
				return true;
			case type:
				return isMandatory();
		}
		throw ReferencePart.noSuchPart(part);
	}

	@Override
	public KIReferenceStorage getStorage() {
		return _storage;
	}

	@Override
	public MOAttribute copy(String newName, MetaObject newType) {
		KIReference copy = new KIReference(newName, newType);
		copy.initFrom(this);
		copy.setStorage(getStorage());
		return copy;
	}

	@Override
	public void setStorage(AttributeStorage storage) {
		if (!(storage instanceof KIReferenceStorage)) {
			throw new IllegalArgumentException("Attribute '" + getName() + "' needs "
				+ KIReferenceStorage.class.getName() + ": " + storage);
		}
		_storage = (KIReferenceStorage) storage;
	}

	@Override
	protected MOIndex createIndex() {
		return new KIReferenceIndex(this);
	}

	/**
	 * Creates a new {@link KIReference} that holds the {@link IdentifiedObject#tId()
	 * identifier} of the referenced object in cache, i.e. the item itself may be removed by JVM.
	 * 
	 * @param name
	 *        See {@link MOAttribute#getName()}
	 * @param targetType
	 *        See {@link AbstractMOReference#getTargetType()}
	 * 
	 * @see ByIDReferenceStorageImpl
	 */
	public static KIReference referenceById(String name, MetaObject targetType) {
		KIReference referenceById = new KIReference(name, targetType);
		referenceById.setStorage(ByIDReferenceStorageImpl.INSTANCE);
		return referenceById;
	}

	/**
	 * Creates a new {@link KIReference} that holds the {@link IdentifiedObject referenced object}
	 * itself in cache, i.e. the item is not removed by JVM until the referee is removed.
	 * 
	 * @param name
	 *        See {@link MOAttribute#getName()}
	 * @param targetType
	 *        See {@link AbstractMOReference#getTargetType()}
	 * 
	 * @see ByValueReferenceStorageImpl
	 */
	public static KIReference referenceByValue(String name, MetaObject targetType) {
		KIReference referenceByValue = new KIReference(name, targetType);
		referenceByValue.setStorage(ByValueReferenceStorageImpl.INSTANCE);
		return referenceByValue;
	}
}

