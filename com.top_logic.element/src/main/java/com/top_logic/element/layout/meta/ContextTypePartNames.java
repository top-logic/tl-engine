/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.provider.PartNamesOptionProvider;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Option provider offering the attributes of the type in whose context the edited attribute is
 * defined.
 *
 * <p>
 * The edited attribute itself is not offered. Options are {@link ColumnOption}s, so a property
 * using this provider stores technical names through a {@link ColumnOptionMapping} and displays
 * labels through a {@link ColumnOptionLabelProvider}.
 * </p>
 *
 * <p>
 * In contrast to {@link PartNamesOptionProvider}, which offers the attributes of the edited
 * attribute's value type, this provider offers the sibling attributes of the edited attribute. It
 * is the option provider for annotations that refer to other attributes of the same object, such as
 * the additional attributes participating in a uniqueness constraint.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContextTypePartNames extends Function0<Collection<ColumnOption>> {

	private final TLStructuredType _contextType;

	private final String _editedName;

	/**
	 * Creates a {@link ContextTypePartNames}.
	 */
	@CalledByReflection
	public ContextTypePartNames(DeclarativeFormOptions options) {
		ConfigurationItem formModel = options.get(DeclarativeFormBuilder.FORM_MODEL);

		if (formModel instanceof TLStructuredTypePartFormBuilder.EditModel) {
			TLStructuredTypePartFormBuilder.EditModel editModel =
				(TLStructuredTypePartFormBuilder.EditModel) formModel;

			_contextType = editModel.getContextType();
			PartModel partModel = editModel.getPartModel();
			_editedName = partModel == null ? null : partModel.getName();
		} else {
			_contextType = contextType(formModel);
			_editedName = null;
		}
	}

	/**
	 * The type owning the model part the given form edits, or <code>null</code> if the form does
	 * not edit a model part.
	 */
	private static TLStructuredType contextType(ConfigurationItem formModel) {
		if (!(formModel instanceof FullQualifiedName)) {
			return null;
		}
		String qualifiedName = ((FullQualifiedName) formModel).getFullQualifiedName();
		if (StringServices.isEmpty(qualifiedName)) {
			return null;
		}
		TLModelPart modelPart;
		try {
			modelPart = TLModelUtil.resolveModelPart(qualifiedName);
		} catch (TopLogicException ex) {
			// A part being created has no resolvable name yet.
			return null;
		}
		if (modelPart instanceof TLStructuredTypePart) {
			return ((TLStructuredTypePart) modelPart).getOwner();
		}
		if (modelPart instanceof TLStructuredType) {
			return (TLStructuredType) modelPart;
		}
		return null;
	}

	@Override
	public Collection<ColumnOption> apply() {
		if (_contextType == null) {
			return Collections.emptyList();
		}

		List<TLStructuredTypePart> parts = new ArrayList<>();
		for (TLStructuredTypePart part : _contextType.getAllParts()) {
			if (part.getName().equals(_editedName)) {
				continue;
			}
			if (!accept(part)) {
				continue;
			}
			parts.add(part);
		}
		return TableUtil.createColumnOptions(parts);
	}

	/**
	 * Whether the given attribute is offered as option.
	 */
	protected boolean accept(TLStructuredTypePart part) {
		return true;
	}

	/**
	 * {@link ContextTypePartNames} offering only attributes with a single value.
	 *
	 * <p>
	 * To-many attributes are excluded, so this provider serves annotations that expect an attribute
	 * with exactly one value, such as an attribute participating in a uniqueness constraint.
	 * </p>
	 */
	public static class SingleValued extends ContextTypePartNames {

		/**
		 * Creates a {@link SingleValued}.
		 */
		@CalledByReflection
		public SingleValued(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected boolean accept(TLStructuredTypePart part) {
			return !part.isMultiple();
		}

	}

}
