/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.filter;

import java.util.Collections;

import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.AbstractClassificationTableFilterProvider;
import com.top_logic.layout.table.filter.CollectionLabelFilterProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.SelectableOptionFilterProvider;
import com.top_logic.layout.table.filter.SimpleBooleanFilterProvider;
import com.top_logic.layout.table.filter.SimpleComparableFilterProvider;
import com.top_logic.layout.table.filter.SimpleDateFilterProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Abstract Base class for {@link TableFilterProvider}s that create filters based on the
 * {@link TLStructuredTypePart}s of a given {@link TLClass}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class AttributedTableFilterProvider implements TableFilterProvider {
    
    public AttributedTableFilterProvider() {
	}

	/**
	 * Returns the {@link TLClass} used in the underlying table.
	 */
	protected abstract TLClass getMetaElement();

	/**
	 * Initializes the Filter for the given {@link TableViewModel} and a given column pos, column
	 * name and {@link TLClass}.
	 * @param aME         the {@link TLClass} to use
	 * @param aName       the name of the column the filter shall be initialized for.
	 */
	protected TableFilter createFilterForColumn(TableViewModel aTableModel, TLClass aME, String aName) {
		if (MetaElementUtil.hasMetaAttribute(aME, aName)) {
			try {
				TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(aME, aName);

                int typeCode = AttributeOperations.getMetaAttributeType(theMA);
				switch (typeCode) {
                    case LegacyTypeCodes.TYPE_STRING:
						return this.createStringFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_STRING_SET:
						return this.createStringSetFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_DATE:
						return this.createDateFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_BOOLEAN:
						return this.createBooleanFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_FLOAT:
                    case LegacyTypeCodes.TYPE_LONG:
						return this.createNumberFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
                    case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
                    case LegacyTypeCodes.TYPE_WRAPPER:
                    case LegacyTypeCodes.TYPE_SINGLE_STRUCTURE:
                    case LegacyTypeCodes.TYPE_COMPLEX:
                    case LegacyTypeCodes.TYPE_DAP:
                    case LegacyTypeCodes.TYPE_DAP_FALLB:
						return this.createMetaResourceFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_CLASSIFICATION:
						return this.createClassificationFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_DAP_COLLECTION:
                    case LegacyTypeCodes.TYPE_COLLECTION:
                    case LegacyTypeCodes.TYPE_LIST:
                    case LegacyTypeCodes.TYPE_TYPEDSET:
                    case LegacyTypeCodes.TYPE_STRUCTURE:
						return this.createCollectionMetaResourceFilter(aTableModel, aName, theMA);
                    case LegacyTypeCodes.TYPE_BINARY:
                    case LegacyTypeCodes.TYPE_CALCULATED:
                    default:
						return null;
                }
			}
			catch (NoSuchAttributeException e) {
				throw new UnsupportedOperationException("ME '" + aME + "' has no MA called '" + aName + "'!");
			}
		}
		else {
			return this.initFiltersForColumn(aName, aTableModel);
		}
	}

	@Override
	public TableFilter createTableFilter(TableViewModel aTableModel, String filterPosition) {
		return createFilterForColumn(aTableModel, this.getMetaElement(), filterPosition);
	}

	protected TableFilter initFiltersForColumn(String aName, TableViewModel aTableModel) {
		Logger.warn("No filter defined for column '" + aName + "'!", this);
		return null;
	}

	protected TableFilter createStringFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return LabelFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}

	protected TableFilter createStringSetFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return CollectionLabelFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}
	
	protected TableFilter createMetaResourceFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return LabelFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}

	protected TableFilter createCollectionMetaResourceFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return CollectionLabelFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}
	
	protected TableFilter createDateFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return SimpleDateFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}

	protected TableFilter createBooleanFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return SimpleBooleanFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}
	
	protected TableFilter createClassificationFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return SelectableOptionFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}

	protected TableFilter createNumberFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return createComparableFilter(aTableModel, aName, aMA);
	}
	
	protected TableFilter createComparableFilter(TableViewModel aTableModel, String aName, TLStructuredTypePart aMA) {
		return SimpleComparableFilterProvider.INSTANCE.createTableFilter(aTableModel, aName);
	}

	/**
	 * {@link TableFilterProvider} for {@link TLStructuredTypePart}s of type {@link FastList}.
	 */
	public static class GenericClassificationFilterProvider extends AbstractClassificationTableFilterProvider {

		public GenericClassificationFilterProvider(TLStructuredTypePart attribute) {
			super(Collections.singletonList(AttributeOperations.getClassificationList(attribute)));
		}
		
		@Override
		protected boolean isMulti() {
			return true;
		}
	}
}
