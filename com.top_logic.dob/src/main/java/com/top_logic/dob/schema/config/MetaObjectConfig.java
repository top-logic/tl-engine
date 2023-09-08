/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Configuration of an {@link MetaObject}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(DOXMLConstants.META_OBJECT_ELEMENT)
public interface MetaObjectConfig extends DBConfiguration, MetaObjectName {

	/** Name of the tag define the source attribute of an association. */
	String SOURCE_REFERENCE_PROPERTY = "source";

	/** Name of the tag define the destination attribute of an association. */
	String DESTINATION_REFERENCE_PROPERTY = "destination";

	/** Name of the sub tag to configure {@link MOReference} */
	String REFERENCE_ELEMENT_PROPERTY = DOXMLConstants.REFERENCE_ELEMENT;

	/** Name of the property {@link #getAttributes()} */
	String ATTRIBUTES_PROPERTY = DOXMLConstants.MO_ATTRIBUTE_ELEMENT;

	/** Name of the property {@link #isVersioned()} */
	String VERSIONED_PROPERTY = KBXMLConstants.MO_VERSIONED_ATTR;

	/** Name of the property {@link #isUsePKS()} */
	String PKS_PROPERTY = DOXMLConstants.DB_PKS_ATTRIBUTE;

	/** Name of the property {@link #isAbstract()} */
	String ABSTRACT_PROPERTY = DOXMLConstants.ABSTRACT_ATTRIBUTE;

	/** Name of the property {@link #getSuperClass()} */
	String SUPERCLASS_PROPERTY = DOXMLConstants.SUPERCLASS_ATTRIBUTE;

	/**
	 * @see #getAnnotations()
	 */
	String ANNOTATIONS = "annotations";

	/**
	 * The effective name of the table in the DB.
	 */
	@Override
	@Derived(fun = TableDBNameEffective.class, args = {
		@Ref(OBJECT_NAME_ATTRIBUTE), @Ref(DOXMLConstants.DB_NAME_ATTRIBUTE), @Ref(ABSTRACT_PROPERTY)
	})
	String getDBNameEffective();

	/**
	 * Function computing the effective DB name for tables and (abstract) table templates.
	 * 
	 * @see DBConfiguration.DBNameEffective
	 */
	class TableDBNameEffective extends Function3<String, String, String, Boolean> {
		@Override
		public String apply(String name, String dbName, Boolean isAbstract) {
			if (isAbstract) {
				return null;
			}
			if (!StringServices.isEmpty(dbName)) {
				return dbName;
			}
			if (StringServices.isEmpty(name)) {
				return null;
			}
			return SQLH.mangleDBName(name);
		}
	}

	/**
	 * Indexes defined in the represented {@link MOStructure}.
	 * 
	 * @see MOStructure#getIndexes()
	 */
	@EntryTag(DOXMLConstants.MO_INDEX_ELEMENT)
	@Key(IndexConfig.NAME_ATTRIBUTE)
	List<IndexConfig> getIndex();

	/**
	 * The configuration of the primary key.
	 * 
	 * @return The configuration of the primary key.
	 * 
	 * @see MOStructure#getPrimaryKey()
	 */
	PrimaryKeyConfig getPrimaryKey();

	/**
	 * Type of the MetaObject to create.
	 * 
	 * @deprecated Only the value {@link KBXMLConstants#MO_KNOWLEDGE_OBJECT_TYPE_VALUE} is
	 *             supported.
	 */
	@Deprecated
	@Name(DOXMLConstants.MO_TYPE_ATTRIBUTE)
	@StringDefault(KBXMLConstants.MO_KNOWLEDGE_OBJECT_TYPE_VALUE)
	String getObjectType();

	/**
	 * Returns the name of the class this {@link MetaObject} extends.
	 */
	@Name(SUPERCLASS_PROPERTY)
	@Nullable
	String getSuperClass();

	/**
	 * Whether this {@link MetaObject} is abstract, i.e. whether not instances of this
	 * {@link MetaObject} can exists.
	 */
	@Name(ABSTRACT_PROPERTY)
	boolean isAbstract();

	/**
	 * @see DBTableMetaObject#isPKeyStorage()
	 */
	@Name(PKS_PROPERTY)
	boolean isUsePKS();

	/**
	 * Whether the type is a versioned type. If {@link Decision#DEFAULT} then a default from the
	 * global application configuration is used.
	 */
	@Name(VERSIONED_PROPERTY)
	Decision isVersioned();

	/**
	 * Configuration of the attributes in the {@link MOStructure}.
	 */
	@EntryTag(ATTRIBUTES_PROPERTY)
	@Key(AttributeConfig.ATTRIBUTE_NAME_KEY)
	@DefaultContainer
	List<AttributeConfig> getAttributes();

	/**
	 * Generic annotation facility for {@link MetaObjectsConfig}.
	 */
	@Key(MOAnnotation.CONFIGURATION_INTERFACE_NAME)
	Collection<MOAnnotation> getAnnotations();

	/**
	 * Indexed getter for {@link #getAnnotations()}.
	 */
	@Indexed(collection = ANNOTATIONS)
	<T extends MOAnnotation> T getAnnotation(Class<T> annotationType);

}

