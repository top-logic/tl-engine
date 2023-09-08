/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.storage.ExtIDAttributeStorage;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.SimpleDBAttribute;

/**
 * Attribute storing external ids.
 * 
 * @see ExtID
 * @see ExtIDAttributeStorage
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtIDAttribute extends AbstractMOAttribute {

	/** Suffix for the {@link ExtID#systemId() system id} of the value. */
	public static final String KO_ATT_SUFFIX_TYPE = "_SYS";

	/** Suffix for the {@link ExtID#objectId() object id} of the value. */
	public static final String KO_ATT_SUFFIX_ID = "_OBJ";

	/**
	 * Configuration of an {@link ExtIDAttribute}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(Config.DEFAULT_TAG_NAME)
	public static interface Config extends PrimitiveAttributeConfig {

		/** Default tage name for an {@link ExtIDAttribute}. */
		String DEFAULT_TAG_NAME = "external-id";

		@Override
		@InstanceDefault(ExtIDAttributeStorage.class)
		AttributeStorage getStorage();

		@Override
		@FormattedDefault("Void")
		MetaObject getValueType();

		@Override
		@ClassDefault(ExtIDAttribute.class)
		Class<? extends MOAttribute> getImplementationClass();

	}

	private AttributeStorage _storage;

	private final DBAttribute[] _dbMapping;

	private ExtIDAttribute(String name, MetaObject type) {
		super(name, type);
		_dbMapping = newDBMapping();
	}

	/**
	 * Creates a new {@link ExtIDAttribute}.
	 */
	public ExtIDAttribute(InstantiationContext context, PrimitiveAttributeConfig config) {
		super(context, config);
		setStorage(config.getStorage());
		setMetaObject(config.getValueType());
		_dbMapping = newDBMapping();
	}

	private DBAttribute[] newDBMapping() {
		return new DBAttribute[] { sysIdAttr(), objIdAttr() };
	}

	private SimpleDBAttribute sysIdAttr() {
		return new SimpleDBAttribute(this, MOPrimitive.LONG, SQLH.mangleDBName(getName()) + KO_ATT_SUFFIX_TYPE);
	}

	private SimpleDBAttribute objIdAttr() {
		return new SimpleDBAttribute(this, MOPrimitive.LONG, SQLH.mangleDBName(getName()) + KO_ATT_SUFFIX_ID);
	}

	@Override
	public DBAttribute[] getDbMapping() {
		return _dbMapping;
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
	public MOAttribute copy() {
		ExtIDAttribute copy = new ExtIDAttribute(getName(), typeRef(getMetaObject()));
		copy.initFrom(this);
		copy.setStorage(getStorage());
		return copy;
	}

}

