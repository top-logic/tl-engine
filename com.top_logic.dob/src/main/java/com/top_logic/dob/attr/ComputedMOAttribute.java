/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AbstractMOAttribute} which has no {@link DBAttribute database mapping}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ComputedMOAttribute extends AbstractMOAttribute {

	private AttributeStorage _storage;

	/**
	 * Creates a new {@link ComputedMOAttribute}.
	 * 
	 * @param name
	 *        see {@link AbstractMOAttribute#AbstractMOAttribute(String, MetaObject)}
	 * @param type
	 *        see {@link AbstractMOAttribute#AbstractMOAttribute(String, MetaObject)}
	 */
	public ComputedMOAttribute(String name, MetaObject type) {
		super(name, type);
	}

	/**
	 * Creates a new {@link ComputedMOAttribute} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ComputedMOAttribute}.
	 */
	public ComputedMOAttribute(InstantiationContext context, PrimitiveAttributeConfig config) {
		super(context, config);
		setMetaObject(config.getValueType());
		setStorage(config.getStorage());
	}

	@Override
	public DBAttribute[] getDbMapping() {
		return DBAttribute.NO_DB_ATTRIBUTES;
	}

	@Override
	public AttributeStorage getStorage() {
		return _storage;
	}

	@Override
	public void setStorage(AttributeStorage storage) {
		_storage = storage;
	}

	@Override
	protected void afterFreeze() {
		super.afterFreeze();
		if (_storage == null) {
			throw new IllegalStateException("No storage implementation set in '" + this + "'");
		}
	}

	@Override
	public MOAttribute copy() {
		ComputedMOAttribute copy = new ComputedMOAttribute(getName(), typeRef(getMetaObject()));
		copy.initFrom(this);
		copy.setStorage(getStorage());
		return copy;
	}

}

