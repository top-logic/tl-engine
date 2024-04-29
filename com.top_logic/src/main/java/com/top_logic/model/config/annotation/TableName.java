/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
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
public interface TableName extends TLTypeAnnotation {

	/** Name of the default tag to define a {@link TableName}. */
	String TAG_NAME = "table";

	/**
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * The name of the table in which instances of the annotated {@link TLType} are stored.
	 * 
	 * @see TLAnnotations#getTable(TLType) Looking up the table to store instance of a certain type
	 *      in.
	 */
	@Name(NAME)
	@Mandatory
	@Options(fun = AllTLObjectTables.class, args = @Ref(ANNOTATED))
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Lists all table names that can store dynamically typed {@link TLObject}s.
	 */
	class AllTLObjectTables extends Function1<List<String>, AnnotatedConfig<?>> {
		@Override
		public List<String> apply(AnnotatedConfig<?> arg) {
			if (arg == null) {
				return Collections.emptyList();
			}

			// Quirks to work-around the definition of ClassConfig in tl-element.
			PropertyDescriptor property = arg.descriptor().getProperty("abstract");
			boolean includeAbstract;
			if (property == null) {
				includeAbstract = false;
			} else {
				includeAbstract = ((Boolean) arg.value(property)).booleanValue();
			}

			return AllTables.allTables(PersistentObject.OBJECT_TYPE, includeAbstract);
		}
	}

}

