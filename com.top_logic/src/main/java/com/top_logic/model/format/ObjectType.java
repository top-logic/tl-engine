/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.DefaultConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Restricts a configuration property holding a {@link TLObject}, or a collection of them, to objects
 * of a model type.
 *
 * <p>
 * An object of the {@link #value() type} or of one of its specializations is accepted. In a
 * declarative property definition the annotation is written as element {@value #TAG_NAME} with the
 * type name in its attribute {@code value}.
 * </p>
 *
 * <p>
 * The restriction is a constraint: it is checked when the configuration is checked, not when a
 * value is parsed.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ConstraintAnnotation(ObjectType.Factory.class)
@TagName(ObjectType.TAG_NAME)
public @interface ObjectType {

	/**
	 * Tag name of the annotation in a declarative property definition.
	 */
	String TAG_NAME = "object-type";

	/**
	 * Qualified name of the model type (see {@link TLModelPartRef}), e.g. {@code "my.module:MyType"}.
	 */
	@Label("Model type")
	String value();

	/**
	 * {@link ConstraintFactory} for {@link ObjectType} annotations.
	 */
	public class Factory implements ConstraintFactory<ObjectType> {

		/**
		 * Singleton {@link ObjectType.Factory} instance.
		 */
		public static final ObjectType.Factory INSTANCE = new ObjectType.Factory();

		private static final Ref[] NO_REFS = {};

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public List<ConstraintSpec> createConstraint(ObjectType annotation) {
			return Collections.<ConstraintSpec> singletonList(
				new DefaultConstraintSpec(new Algorithm(annotation.value()), NO_REFS, false));
		}

	}

	/**
	 * Checks that a {@link TLObject} value, or each element of a collection of them, is an instance
	 * of the expected model type.
	 */
	public class Algorithm extends ValueConstraint<Object> {

		private final String _typeName;

		/**
		 * Creates an {@link Algorithm}.
		 *
		 * @param typeName
		 *        Qualified name of the expected model type.
		 */
		public Algorithm(String typeName) {
			super(Object.class);
			_typeName = typeName;
		}

		@Override
		protected void checkValue(PropertyModel<Object> propertyModel) {
			Object value = propertyModel.getValue();
			if (value == null) {
				return;
			}
			Collection<?> objects = value instanceof Collection<?> collection ? collection : List.of(value);
			if (objects.isEmpty()) {
				return;
			}
			TLType expectedType;
			try {
				expectedType = TLModelPartRef.ref(_typeName).resolveType();
			} catch (TopLogicException ex) {
				propertyModel.setProblemDescription(ex.getErrorKey());
				return;
			}
			if (expectedType == null) {
				propertyModel.setProblemDescription(I18NConstants.ERROR_NO_SUCH_OBJECT_TYPE__TYPE.fill(_typeName));
				return;
			}
			for (Object element : objects) {
				if (element instanceof TLObject object && !TLModelUtil.isCompatibleInstance(expectedType, object)) {
					propertyModel.setProblemDescription(
						I18NConstants.ERROR_OBJECT_TYPE_MISMATCH__EXPECTED_ACTUAL.fill(_typeName,
							TLModelUtil.qualifiedName(object.tType())));
					return;
				}
			}
		}
	}

}
