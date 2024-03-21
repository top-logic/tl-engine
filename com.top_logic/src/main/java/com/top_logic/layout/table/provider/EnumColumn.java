/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.list.FastListElementCollectionComparator;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.provider.SelectControlProvider;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.table.filter.DefaultClassificationTableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.util.TLCollator;

/**
 * {@link ColumnInfo} configuring a column displaying {@link TLEnumeration} contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumColumn extends ReferenceColumn {

	/**
	 * Creates a {@link EnumColumn}.
	 * 
	 * @param type
	 *        The content type.
	 * @param accessor
	 *        Accessor for values of the given types.
	 */
	public EnumColumn(TLTypeContext type, ResKey headerI18NKey, DisplayMode visibility,
			Accessor accessor) {
		super(type, headerI18NKey, visibility, accessor);
	}

	@Override
	protected
	void setComparators(ColumnConfiguration column) {
		if (isMultiple()) {
			column.setComparator(new FastListElementCollectionComparator(new TLCollator()));
			column.setDescendingComparator(null);
		} else {
			// Note: Even single selection is encoded into lists.
			column.setComparator(new FastListElementCollectionComparator(new TLCollator()));
			column.setDescendingComparator(null);
		}
	}

	@Override
	protected
	void setFilterProvider(ColumnConfiguration column) {
		Set<TLType> types = getTypeContext().getConcreteTypes();
		List<TLEnumeration> classifications = new ArrayList<>(types.size());
		for (TLType enumeration : types) {
			classifications.add((TLEnumeration) enumeration);
		}
		DefaultClassificationTableFilterProvider provider =
			new DefaultClassificationTableFilterProvider(classifications, isMultiple(), false);
		column.setFilterProvider(provider);
	}

	@Override
	ColumnInfo joinReference(ReferenceColumn other) {
		if (other instanceof EnumColumn) {
			return super.joinReference(other);
		} else {
			return other.join(this);
		}
	}

	@Override
	protected ReferenceColumn createColumn(TLTypeContext type) {
		return new EnumColumn(type, getHeaderI18NKey(), getVisibility(), getAccessor());
	}

	@Override
	protected ControlProvider getControlProvider() {
		ClassificationPresentation presentation = getClassificationPresentation();
		switch (presentation) {
			case DROP_DOWN:
				if (isMultiple()) {
					/* Do not display multiple selection with select control. The reason is that the
					 * displayed box with the options would destroy the table layout. */
					return SelectionControlProvider.SELECTION_INSTANCE;
				} else {
					if (isMandatory()) {
						return SelectControlProvider.INSTANCE_WITHOUT_CLEAR;
					} else {
						return SelectControlProvider.INSTANCE;
					}
				}
			case CHECKLIST:
				// TODO Ticket #20011: Display checklists as in forms.
				return SelectionControlProvider.SELECTION_INSTANCE;
			case POP_UP:
				return SelectionControlProvider.SELECTION_INSTANCE;
			case RADIO:
				return (field, style) -> newChoiceControl(field, Orientation.VERTICAL);
			case RADIO_INLINE:
				return (field, style) -> newChoiceControl(field, Orientation.HORIZONTAL);
		}
		throw ClassificationPresentation.noSuchEnum(presentation);
	}

	private ChoiceControl newChoiceControl(Object field, Orientation orientation) {
		ChoiceControl choice = new ChoiceControl((SelectField) field);
		choice.setOrientation(orientation);
		return choice;
	}

	private ClassificationPresentation getClassificationPresentation() {
		return TLAnnotations.getClassificationPresentation(
			TLAnnotations.getAnnotation(getTypeContext(), ClassificationDisplay.class), isMultiple());
	}

}