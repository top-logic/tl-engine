/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Edit model of a model element whose values are of a model type: the values of an attribute are
 * of its value type, the values of a type are of the type itself.
 *
 * <p>
 * An annotation that applies to an attribute and to a type alike - such as the input control of a
 * value - asks its {@link TLAnnotation#getAnnotated() container} for this type, whichever of the two
 * it is attached to. A {@link Ref} whose {@link Step} is typed with this interface resolves on both,
 * and to {@code null} on a container of any other kind.
 * </p>
 */
@Abstract
public interface ValueTypeModel extends ConfigurationItem {

	/**
	 * @see #getResolvedType()
	 */
	String RESOLVED_TYPE = "resolved-type";

	/**
	 * @see #getTypeKind()
	 */
	String TYPE_KIND = "type-kind";

	/**
	 * The type of the values of the edited element, {@code null} where it cannot be resolved.
	 */
	@Name(RESOLVED_TYPE)
	@InstanceFormat
	@Hidden
	TLType getResolvedType();

	/**
	 * The {@link TLTypeKind} of the {@link #getResolvedType() value type}, used to limit the
	 * annotations and implementations offered for the edited element.
	 */
	@Name(TYPE_KIND)
	@Derived(fun = ResolveTypeKind.class, args = @Ref(RESOLVED_TYPE))
	@Hidden
	TLTypeKind getTypeKind();

	/**
	 * Function resolving the {@link TLTypeKind} for a given {@link TLType}.
	 */
	class ResolveTypeKind extends Function1<TLTypeKind, TLType> {
		@Override
		public TLTypeKind apply(TLType type) {
			if (type != null) {
				return TLTypeKind.getTLTypeKind(type);
			}

			return null;
		}
	}

	/**
	 * Function resolving a type by its qualified name in the application model, {@code null} for a
	 * name of no existing type.
	 */
	class ResolveQualifiedType extends Function1<TLType, String> {
		@Override
		public TLType apply(String qualifiedName) {
			if (qualifiedName == null || qualifiedName.isBlank()) {
				return null;
			}
			try {
				return TLModelUtil.findType(qualifiedName);
			} catch (TopLogicException ex) {
				return null;
			}
		}
	}

}
