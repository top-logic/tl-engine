/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.persistency.AllPrimitiveApplicationColumns;
import com.top_logic.model.annotate.persistency.AllReferenceColumns;
import com.top_logic.model.annotate.persistency.AllTables;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation of the database table in which instances of the annotated {@link TLType} are stored.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(TableName.TAG_NAME)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@InApp
@DisplayOrder({
	TableName.NAME,
	TableName.CONTAINER,
	TableName.CONTAINER_REFERENCE,
	TableName.CONTAINER_ORDER,
})
public interface TableName extends TLTypeAnnotation {

	/** Name of the default tag to define a {@link TableName}. */
	String TAG_NAME = "table";

	/**
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * Configuration name of {@link #getContainer()}.
	 */
	String CONTAINER = "container";

	/**
	 * Configuration name of {@link #getContainerReference()}.
	 */
	String CONTAINER_REFERENCE = "container-reference";

	/**
	 * Configuration name of {@link #getContainerOrder()}.
	 */
	String CONTAINER_ORDER = "container-order";

	/**
	 * The name of the table in which instances of the annotated {@link TLType} are stored.
	 * 
	 * @see TLAnnotations#getTable(TLType) Looking up the table to store instance of a certain type
	 *      in.
	 */
	@Name(NAME)
	@Mandatory
	@Options(fun = AllTLObjectTables.class)
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Lists all table names that can store dynamically typed {@link TLObject}s.
	 */
	class AllTLObjectTables extends AllTables {
		@Override
		public String getBaseTable() {
			return PersistentObject.OBJECT_TYPE;
		}
	}

	/**
	 * Name of the reference column in the table that stores the container of the object.
	 * 
	 * <p>
	 * If an object of the annotated type has a container object, then the container is stored in
	 * this column. If no column is set, the container association is stored in a generic table.
	 * </p>
	 * 
	 * <p>
	 * If {@link TableName#getContainer()} is set, the column {@link #getContainerReference()} must
	 * also be set.
	 * </p>
	 */
	@Options(fun = AllReferenceColumns.class, args = @Ref(NAME))
	@Nullable
	@Name(CONTAINER)
	String getContainer();

	/**
	 * Name of the reference column in the table that stores the container reference.
	 *
	 * <p>
	 * If an object of the annotated type has a container object which is stored in
	 * {@link #getContainer()}, then the container reference is stored in this column. This value
	 * must be set, if {@link #getContainer()} is set.
	 * </p>
	 */
	@Options(fun = AllReferenceColumns.class, args = @Ref(NAME))
	@Nullable
	@Name(CONTAINER_REFERENCE)
	String getContainerReference();

	/**
	 * Name of the {@link Integer} column that contains the order of the object of the annotated
	 * type in the container object.
	 *
	 * <p>
	 * If an object of the annotated type has a container object which is stored in
	 * {@link #getContainer()} and the {@link #getContainerReference() container reference} is an
	 * ordered reference, then the order is stored in this column. This value must be set, if
	 * {@link #getContainer()} is set.
	 * </p>
	 * 
	 * <p>
	 * {@link #getContainerOrder()} must be set, when the reference is multiple.
	 * </p>
	 */
	@Options(fun = AllPrimitiveApplicationColumns.class, args = @Ref(NAME))
	@Nullable
	@Name(CONTAINER_ORDER)
	String getContainerOrder();

}

