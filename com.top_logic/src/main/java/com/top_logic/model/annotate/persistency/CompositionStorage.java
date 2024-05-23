/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.annotation.TableName;

/**
 * Annotation for the definition of the storage strategy for composition references.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("composition-storage")
@InApp
@TargetType(value = { TLTypeKind.COMPOSITION })
public interface CompositionStorage extends TLAttributeAnnotation {

	/**
	 * Definition of a storage strategy for a composition reference.
	 */
	@Mandatory
	@DefaultContainer
	Storage getStorage();


	/**
	 * Default {@link Storage} for a composition reference when no explicit
	 * {@link CompositionStorage} annotation is given.
	 */
	static LinkTable defaultCompositionLinkStorage() {
		LinkTable linkTable = TypedConfiguration.newConfigItem(LinkTable.class);
		linkTable.setTable(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
		return linkTable;
	}


	/**
	 * Definition of a storage strategy for a composition reference.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	interface Storage extends EqualityByValue {
		// Marker interface
	}

	/**
	 * The connection between the container and the part is stored in a separate link table.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("link")
	interface LinkTable extends Storage {

		/** Name of the default tag to define a {@link TableName}. */
		String TABLE = "table";

		/**
		 * Name of the {@link MOClass table} in which the composition reference stores the
		 * connection between container and part.
		 */
		@Options(fun = LinkTables.class)
		@Mandatory
		@Name(TABLE)
		String getTable();

		/**
		 * Setter for {@link #getTable()}.
		 */
		void setTable(String value);

	}

	/**
	 * The container is stored inline in the table in which the part is stored.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("in-target")
	@DisplayOrder({
		InTargetTable.CONTAINER,
		InTargetTable.CONTAINER_REFERENCE,
		InTargetTable.CONTAINER_ORDER
	})
	interface InTargetTable extends Storage {

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
		 * Name of the reference column in the table that stores the container of the object.
		 * 
		 * <p>
		 * If an object of the annotated type has a container object, then the container is stored
		 * in this column.
		 * </p>
		 */
		@Mandatory
		@Name(CONTAINER)
		String getContainer();

		/**
		 * Name of the reference column in the table that stores the composite reference.
		 */
		@Mandatory
		@Name(CONTAINER_REFERENCE)
		String getContainerReference();

		/**
		 * Name of the {@link Integer} column that contains the order of the part in the container
		 * object.
		 *
		 * <p>
		 * {@link #getContainerOrder()} must be set, when the reference is multiple and ordered.
		 * </p>
		 */
		@Nullable
		@Name(CONTAINER_ORDER)
		String getContainerOrder();

	}

	/**
	 * The part of the composition is stored inline in the table containing the container.
	 * 
	 * <p>
	 * This storage strategy is only possible, when the composition reference is <i>not</i>
	 * {@link TLReference#isMultiple() multiple}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("in-source")
	@DisplayOrder({
		InSourceTable.PART,
	})
	interface InSourceTable extends Storage {

		/**
		 * Configuration name of {@link #getPart()}.
		 */
		String PART = "part";

		/**
		 * Name of the reference column in the table that stores the part of the composition.
		 */
		@Mandatory
		@Name(PART)
		String getPart();

	}
}
