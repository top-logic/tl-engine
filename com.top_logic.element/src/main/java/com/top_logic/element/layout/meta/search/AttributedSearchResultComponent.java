/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.knowledge.searching.SearchResultBuilder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.component.SupportColumnChange;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;

/**
 * Display results from a search in a table.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedSearchResultComponent extends TableComponent
		implements SupportColumnChange, AttributedSearchResultSetAware {

	public interface Config extends TableComponent.Config {
		@Name(XML_CONFIG_META_ELEMENT)
		@Mandatory
		String getMetaElement();

		@Name(XML_CONFIG_META_ELEMENT_BUILDER)
		PolymorphicConfiguration<SearchModelBuilder> getMetaElementBuilder();

		@Override
		@ItemDefault
		@ImplementationClassDefault(AttributedSearchResultBuilder.class)
		PolymorphicConfiguration<? extends ListModelBuilder> getModelBuilder();
	}

	public static final String XML_CONFIG_META_ELEMENT = "metaElement";

    public static final String XML_CONFIG_META_ELEMENT_BUILDER = "metaElementBuilder";

    private Set<? extends TLClass> metaElements;

    public AttributedSearchResultComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);

        String configuredMEs = someAttrs.getMetaElement();
		if (!configuredMEs.isEmpty()) {
			PolymorphicConfiguration<SearchModelBuilder> metaElementBuilder = someAttrs.getMetaElementBuilder();
			if (metaElementBuilder == null) {
				throw new ConfigurationException("Attribute " + XML_CONFIG_META_ELEMENT_BUILDER + " in component "
					+ getName() + " is mandatory, when " + XML_CONFIG_META_ELEMENT + " is not empty.");
			}
			setMetaElement(configuredMEs, context.getInstance(metaElementBuilder));
		}
    }

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean shouldCheckMissingTypeConfiguration() {
		/* This component registers itself for everything. This check is therefore unnecessary. */
		return false;
	}

	@Override
	protected boolean isChangeHandlingDefault() {
    	return false;
    }

    @Override
	protected ObjectTableModel createApplicationModel() {
    	AttributedSearchResultSet theModel = getSearchResult();
        if (theModel != null) {
            List theColumns = theModel.getResultColumns();
            setColumnNames((String[])theColumns.toArray(new String[theColumns.size()]));
        }
        return super.createApplicationModel();
    }

	@Override
	protected void handleNewModel(Object newModel) {
		resetPersonalColumnConfiguration();
		super.handleNewModel(newModel);
	}

	/**
	 * Clears the column permutation of the search result table so that table columns
	 * are set as specified in search component.
	 */
	protected void resetPersonalColumnConfiguration() {
        PersonalConfiguration.getPersonalConfiguration().setJSONValue(this.getName() + "column", null);
	}

    /**
     * Return the unmodifiable list of supported meta elements.
     *
     * @return    The requested list, never <code>null</code>.
     */
    public Set<? extends TLClass> getMetaElements() {
        return this.metaElements;
    }

    /**
     * Set the meta elements supported by this component.
     *
     * @param    aMetaElement    A comma separated list of meta element names.
     * @param    aBuilder         A search model builder to get the
     */
	protected void setMetaElement(String aMetaElement, SearchModelBuilder aBuilder) {
		List<String> theList = StringServices.toList(aMetaElement, ',');
		Set<TLClass> theResult = new HashSet<>(theList.size());

		for (Iterator<String> theIt = theList.iterator(); theIt.hasNext();) {
			theResult.add(aBuilder.getMetaElement(theIt.next()));
        }

		Set<? extends TLClass> result = Collections.unmodifiableSet(theResult);
		setTypes(result);
    }

	/**
	 * Set the meta elements supported by this component.
	 */
	protected void setTypes(Set<? extends TLClass> types) {
		this.metaElements = types;
	}

	@Override
	public String[] getColumns() {
	    EditableRowTableModel theTableModel = this.getTableModel();
		int theCount = theTableModel.getColumnCount();
	    String[] theColumnNames = new String[theCount];
	    for (int theCol=0; theCol<theCount; theCol++) {
	    	theColumnNames[theCol] = theTableModel.getColumnName(theCol);
	    }

	    return theColumnNames;
	}

	@Override
	public void setColumns(String[] someColumns) {
		if (!ArrayUtil.equals(someColumns, this.getColumnNames())) {
    	    this.setColumnNames(someColumns);
    	    this.invalidate(); // Delete the model
	    }
	}
	
	// End implementation of SupportColumnChange

    /**
     * Search result builder, which takes care of the defined meta element of the calling component.
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class AttributedSearchResultBuilder extends SearchResultBuilder {

		/**
		 * Singleton {@link AttributedSearchResultBuilder} instance.
		 */
		@SuppressWarnings("hiding")
		public static final AttributedSearchResultBuilder INSTANCE = new AttributedSearchResultBuilder();

		protected AttributedSearchResultBuilder() {
			// Singleton constructor.
		}

        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
            boolean theResult = (aModel instanceof AttributedSearchResultSet);

            if (theResult && (aComponent instanceof AttributedSearchResultComponent)) {
                Set<? extends TLClass> theMEs = ((AttributedSearchResultComponent) aComponent).getMetaElements();
                Set<? extends TLClass> types = ((AttributedSearchResultSet) aModel).getTypes();

				theResult = (!types.isEmpty()) && theMEs.containsAll(types);
            }

            return theResult;
        }

        @Override
        public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
            if ((anObject instanceof Wrapper) && (aComponent instanceof AttributedSearchResultComponent)) {
				TLStructuredType type = ((Wrapper) anObject).tType();
				if (type == null) {
					return false;
				}
                AttributedSearchResultSet theModel = ((AttributedSearchResultComponent) aComponent).getSearchResult();

                if (theModel != null) {
					return MetaElementUtil.hasGeneralization(type, theModel.getTypes());
                }
                else { 
                    return false;
                }
            }
            else { 
                return false;
            }
        }
    }


    @Override
    protected void adaptTableConfiguration(TableConfiguration table) {
        super.adaptTableConfiguration(table);

		AttributedSearchResultSet searchResult = getSearchResult();
		if (searchResult == null) {
			return;
		}
		DefaultAttributeFormFactory.configureWithMetaElement(table, searchResult.getTypes());

		removeColumns(table, getColumnsToRemove(getMaster()));
		showColumns(table, getSearchResultColumns(searchResult));

		adaptTableConfigurationToDefaultSortOrder(table);
    }

	private Set<String> getColumnsToRemove(LayoutComponent component) {
		if (component instanceof AttributedSearchComponent) {
			return ((AttributedSearchComponent) component).getExcludeSetForColumns();
		} else {
			return Collections.emptySet();
		}
	}

	private void removeColumns(TableConfiguration table, Set<String> columnNames) {
		for (String columnName : columnNames) {
			table.removeColumn(columnName);
		}
	}

	private Set<String> getSearchResultColumns(AttributedSearchResultSet searchResult) {
		return CollectionUtil.toSet(searchResult.getResultColumns());
	}

	private void showColumns(TableConfiguration table, Set<String> columnNames) {
		for (ColumnConfiguration column : table.getElementaryColumns()) {
			if (columnNames.contains(column.getName())) {
				if (column.getVisibility() != DisplayMode.excluded) {
					column.setVisibility(DisplayMode.visible);
				}
			} else {
				if (column.getVisibility() == DisplayMode.visible) {
					column.setVisibility(DisplayMode.hidden);
				}
			}
		}
	}

	protected void adaptTableConfigurationToDefaultSortOrder(TableConfiguration table) {
		AttributedSearchResultSet model = getSearchResult();
		if (model == null) {
			return;
		}
		List<String> columns = model.getResultColumns();
		if (CollectionUtil.isEmptyOrNull(columns)) {
			String message = "There seems to be a search with no columns. This should not happen.";
			Logger.warn(message, AttributedSearchResultComponent.class);
			return;
		}
		table.setDefaultSortOrder(createDefaultSortConfig(columns));
	}

	protected List<SortConfig> createDefaultSortConfig(List<String> theColumns) {
		return Collections.singletonList(SortConfigFactory.ascending(theColumns.get(0)));
	}

	@Override
	public AttributedSearchResultSet getSearchResult() {
		Object theModel = getModel();
		
		if (theModel instanceof AttributedSearchResultSet) {
			return (AttributedSearchResultSet) theModel;
		}
		else {
			return null;
		}
	}

	/**
	 * Simple supporting class for getting a meta element for a string.
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static final class MetaElementProvider implements SearchModelBuilder {

		/** Only instance of this class. */
		public static final SearchModelBuilder INSTANCE = new MetaElementProvider();

		private MetaElementProvider() {
		}

		@Override
		public TLClass getMetaElement(String aMEType) throws IllegalArgumentException {
			return MetaElementUtil.getMetaElement(aMEType);
		}

		@Override
		public Object getModel(Object businessModel, LayoutComponent aComponent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			throw new UnsupportedOperationException();
		}
	}
}