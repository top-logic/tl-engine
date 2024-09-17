/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurationContainer;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * A ColumnDescription provides specific information about a single column of a table.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ColumnDescription extends ColumnConfiguration {

	@Inspectable
	private final ColumnConfigurationContainer _columns;

	/**
	 * @see #getAccessor()
	 */
	@Inspectable
	private Accessor accessor;

    /**
     * The field provider allows for providing an edit access to table cells.
     * This class creates the appropriate form member for the underlying value
     */
	@Inspectable
    private FieldProvider fieldProvider;

	/** @see #getResourceProvider() */
	@Inspectable
	private ResourceProvider _resourceProvider = DEFAULT_RESOURCE_PROVIDER;

    /**
	 * @see #getCellRenderer()
	 */
	@Inspectable
    private CellRenderer cellRenderer;

    /**
	 * @see #getRenderer()
	 */
	@Inspectable
	private Renderer<Object> _renderer;

	/**
	 * @see #getPDFRenderer()
	 */
	@Inspectable
	private PDFRenderer _pdfRenderer;

    /**
     * The control provider used to create the appropriate control for the form member
     */
    private ControlProvider headControlProvider;

	@Inspectable
    private String name;

	private Collection<String> _classifiers = Collections.emptySet();

    /** Show the name of the column in the table header. Default is <code>true</code>. */
	private boolean showHeader = DEFAULT_SHOW_HEADER;

    /**
     * @see #isSelectable()
     */
	private boolean selectable = DEFAULT_SELECTABLE;

	/**
	 * @see #isVisible()
	 * @see #isMandatory()
	 */
	private DisplayMode visibility = DEFAULT_VISIBILITY;

    /**
	 * @see #getColumnLabelKey()
	 */
	private ResKey columnLabelKey;
    
	/**
	 * @see #getColumnLabel()
	 */
	@Inspectable
	private String columnLabel;
    
    private String commandGroup;
    
	@Inspectable
	private Comparator<?> comparator = DEFAULT_COMPARATOR;
    
	@Inspectable
    private Comparator<?> descendingComparator;

    // Ticket #2644
    // javac: do not use ? as generic parameter
	private Mapping<Object, Object> sortKeyProvider = DEFAULT_SORT_KEY_PROVIDER;
	
	private ControlProvider editControlProvider;

	private String columnWidth;

	private String cellStyle;

	private String headStyle;

	private boolean _excludeFilterFromSidebar = false;

	@Inspectable
	private TableFilterProvider filterProvider = DEFAULT_FILTER_PROVIDER;
	
	private CellExistenceTester cellExistenceTester = DEFAULT_CELL_EXISTENCE_TESTER;
	
	private ExcelCellRenderer _excelRenderer;

	@Inspectable
	private LabelProvider fullTextProvider;

	private boolean fullTextProviderExplicitlySet;

	private String cssClass;

	private String cssClassGroupFirst;

	private String cssClassGroupLast;

	private CellClassProvider cssClassProvider = DEFAULT_CSS_CLASS_PROVIDER;

	private PreloadContribution preloadContribution = EmptyPreloadContribution.INSTANCE;

	private List<HTMLFragmentProvider> _additionalHeaders;

	/**
	 * Creates a {@link ColumnDescription} with all default values.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	@FrameworkInternal
	public ColumnDescription(String name) {
		this.name = name;
		_columns = new ColumnConfigurationContainer(null);
	}

	@Override
	public ColumnDescription copy(String newName) {
		ColumnDescription column = new ColumnDescription(newName);
		copyTo(column);
		return column;
	}

	@Override
	public void copyTo(ColumnConfiguration target) {
		super.copyTo(target);
		internalCopyTo((ColumnDescription) target);
	}

	private void internalCopyTo(ColumnDescription column) {
		column.copyFrom(this);
		_columns.copyTo(column._columns);
	}

	private void copyFrom(ColumnDescription templateColumn) {
		updateFrom(PropertyCopier.INSTANCE, templateColumn.getSettings());
	}

	@Override
	public void addColumn(ColumnConfiguration columnConfiguration) {
		columns().addColumn(columnConfiguration);
	}

	@Override
	protected ColumnConfigurationContainer columns() {
		return _columns;
	}

	@Override
	public Accessor getAccessor() {
		return accessor;
	}

	@Override
	protected void copyAccessor(Accessor accessor) {
		checkFrozen();
		this.accessor = accessor;
	}

	@Override
	public void setAccessor(Accessor accessor) {
		copyAccessor(accessor);
	}

    @Override
	public String getHeadStyle() {
    	return this.headStyle;
    }

	@Override
	protected void copyHeadStyle(String headStyle) {
		checkFrozen();
		this.headStyle = headStyle;
	}

	@Override
	protected void internalSetHeadStyle(String terminatedStyleDefinition) {
		copyHeadStyle(terminatedStyleDefinition);
	}
    
    @Override
	public String getDefaultColumnWidth() {
    	return this.columnWidth;
    }

	@Override
	protected void copyDefaultColumnWidth(String widthStyle) {
		checkFrozen();
		this.columnWidth = widthStyle;
	}

	@Override
	protected void internalSetWidthStyle(String widthStyle) {
		copyDefaultColumnWidth(widthStyle);
	}
    
    @Override
	public String getCellStyle() {
        return (this.cellStyle);
    }

	@Override
	protected void copyCellStyle(String cellStyle) {
		checkFrozen();
		this.cellStyle = cellStyle;
	}

	@Override
	protected void internalSetCellStyle(String terminatedStyle) {
		copyCellStyle(terminatedStyle);
	}
    
    @Override
	public FieldProvider getFieldProvider() {
        return (this.fieldProvider);
    }
    
	@Override
	protected void copyFieldProvider(FieldProvider fieldProvider) {
		checkFrozen();
		this.fieldProvider = fieldProvider;
	}

	@Override
	public void setFieldProvider(FieldProvider fieldProvider) {
		copyFieldProvider(fieldProvider);
	}

    @Override
	public String getName() {
        return (this.name);
    }

	@Override
	public ResourceProvider getResourceProvider() {
		return _resourceProvider;
	}

	@Override
	protected void copyResourceProvider(ResourceProvider resourceProvider) {
		checkFrozen();
		_resourceProvider = resourceProvider;
	}

	@Override
	public void setResourceProvider(ResourceProvider resourceProvider) {
		copyResourceProvider(resourceProvider);
	}

    @Override
	public Renderer<Object> getRenderer() {
		return _renderer;
	}

	@Override
	protected void copyRenderer(Renderer<Object> renderer) {
		checkFrozen();
		_renderer = renderer;
	}

	@Override
	public void setRenderer(Renderer<?> renderer) {
		copyRenderer(renderer.generic());
	}

	@Override
	public CellRenderer getCellRenderer() {
        return (this.cellRenderer);
    }

	@Override
	protected void copyCellRenderer(CellRenderer cellRenderer) {
		checkFrozen();
		this.cellRenderer = cellRenderer;
	}
    
	@Override
	public void setCellRenderer(CellRenderer cellRenderer) {
		copyCellRenderer(cellRenderer);
	}

	@Override
	public boolean hasExcludedFilterFromSidebar() {
		return _excludeFilterFromSidebar;
	}

	@Override
	public void setExcludeFilterFromSidebar(boolean excluded) {
		copyExcludeFilterFromSidebar(excluded);
	}

	@Override
	protected void copyExcludeFilterFromSidebar(boolean excluded) {
		checkFrozen();
		_excludeFilterFromSidebar = excluded;
	}

    @Override
	public TableFilterProvider getFilterProvider(){
    	return this.filterProvider;
    }
    
	@Override
	protected void copyFilterProvider(TableFilterProvider aProvider) {
		checkFrozen();
		this.filterProvider = aProvider;
	}

	@Override
	public void setFilterProvider(TableFilterProvider aProvider) {
		copyFilterProvider(aProvider);
	}

	@Override
	public ExcelCellRenderer getExcelRenderer() {
		return _excelRenderer;
	}

	@Override
	protected void copyExcelRenderer(ExcelCellRenderer renderer) {
		checkFrozen();
		_excelRenderer = renderer;
	}

	@Override
	public void setExcelRenderer(ExcelCellRenderer renderer) {
		copyExcelRenderer(renderer);
	}

	@Override
	public CellExistenceTester getCellExistenceTester() {
		return cellExistenceTester;
	}

	@Override
	public void setCellExistenceTester(CellExistenceTester cellExistenceTester) {
		checkFrozen();
		copyCellExistenceTester(cellExistenceTester);
	}

	@Override
	protected void copyCellExistenceTester(CellExistenceTester cellExistenceTester) {
		this.cellExistenceTester = cellExistenceTester;
	}

	@Override
	public LabelProvider getFullTextProvider() {
		if (useResourceProviderAsFullTextProvider()) {
			return getResourceProvider();
		}
		return fullTextProvider;
	}

	private boolean useResourceProviderAsFullTextProvider() {
		if (fullTextProvider == null) {
			return true;
		}
		return !fullTextProviderExplicitlySet;
	}

	@Override
	protected void copyFullTextProvider(LabelProvider fullTextProvider) {
		checkFrozen();
		this.fullTextProvider = fullTextProvider;
	}

	@Override
	public void setFullTextProvider(LabelProvider fullTextProvider) {
		checkFrozen();
		fullTextProviderExplicitlySet = true;
		copyFullTextProvider(fullTextProvider);
	}

	@Override
	protected void setFullTextProviderAsDefault(LabelProvider fullTextProvider) {
		if (!fullTextProviderExplicitlySet) {
			copyFullTextProvider(fullTextProvider);
		}
	}
    
    @Override
	public ControlProvider getHeadControlProvider() {
        return (this.headControlProvider);
    }

	@Override
	protected void copyHeadControlProvider(ControlProvider aHeadControlProvider) {
		checkFrozen();
		this.headControlProvider = aHeadControlProvider;
	}

	@Override
	public void setHeadControlProvider(ControlProvider aHeadControlProvider) {
		copyHeadControlProvider(aHeadControlProvider);
    }

    @Override
	public boolean isShowHeader() {
        return (this.showHeader);
    }

	@Override
	protected void copyShowHeader(boolean showHeader) {
		checkFrozen();
		this.showHeader = showHeader;
	}

	@Override
	public void setShowHeader(boolean showHeader) {
		copyShowHeader(showHeader);
    }
    
    @Override
	public boolean isSelectable() {
		return selectable;
	}

	@Override
	protected void copySelectable(boolean selectable) {
		checkFrozen();
		this.selectable = selectable;
	}

	@Override
    public void setSelectable(boolean selectable) {
		copySelectable(selectable);
	}

	@Override
	public String getColumnLabel() {
		return this.columnLabel;
	}

	@Override
	protected void copyColumnLabel(String value) {
		checkFrozen();
		this.columnLabel = value;
	}

	@Override
	public void setColumnLabel(String value) {
		copyColumnLabel(value);
	}

    @Override
	public ResKey getColumnLabelKey() {
        return (this.columnLabelKey);
    }

	@Override
	public void copyColumnLabelKey(ResKey value) {
		checkFrozen();
		this.columnLabelKey = value;
	}

	@Override
    public void setColumnLabelKey(ResKey value) {
		copyColumnLabelKey(value);
    }

    @Override
	public String getCommandGroup() {
        return (this.commandGroup);
    }

	@Override
	protected void copyCommandGroup(String aCommandGroup) {
		checkFrozen();
		this.commandGroup = aCommandGroup;
	}

	@Override
    public void setCommandGroup(String aCommandGroup) {
		copyCommandGroup(aCommandGroup);
    }
    
    @Override
	public Comparator getComparator() {
		return comparator;
	}
    
	@Override
	protected void copyComparator(Comparator comparator) {
		checkFrozen();
		this.comparator = comparator;
	}

    @Override
	public Mapping getSortKeyProvider() {
    	return sortKeyProvider;
    }
    
	@Override
	protected void copySortKeyProvider(Mapping mapping) {
		checkFrozen();
		this.sortKeyProvider = mapping;
	}

	@Override
	public ControlProvider getEditControlProvider() {
		return editControlProvider;
	}

	@Override
	protected void copyEditControlProvider(ControlProvider controlProvider) {
		checkFrozen();
		this.editControlProvider = controlProvider;
	}

	@Override
	public void setEditControlProvider(ControlProvider editControlProvider) {
		copyEditControlProvider(editControlProvider);
	}

	@Override
	public PreloadContribution getPreloadContribution() {
		return this.preloadContribution;
	}

	@Override
	protected void copyPreloadContribution(PreloadContribution value) {
		checkFrozen();
		this.preloadContribution = value;
	}

    @Override
	public Comparator getDescendingComparator() {
		return descendingComparator;
	}
    
	@Override
	protected void copyDescendingComparator(Comparator descendingComparator) {
		checkFrozen();
		this.descendingComparator = descendingComparator;
	}

	@Override
	public void setDescendingComparator(Comparator descendingComparator) {
		copyDescendingComparator(descendingComparator);
	}

	@Override
	public String getCssClass() {
		return this.cssClass;
	}

	@Override
	protected void copyCssClass(String cssClass) {
		checkFrozen();
		this.cssClass = cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		copyCssClass(cssClass);
	}

	@Override
	public String getCssClassGroupFirst() {
		return this.cssClassGroupFirst;
	}
	
	@Override
	protected void copyCssClassGroupFirst(String cssClass) {
		checkFrozen();
		this.cssClassGroupFirst = cssClass;
	}

	@Override
	public void setCssClassGroupFirst(String cssClass) {
		copyCssClassGroupFirst(cssClass);
	}

	@Override
	public String getCssClassGroupLast() {
		return this.cssClassGroupLast;
	}

	@Override
	protected void copyCssClassGroupLast(String cssClass) {
		checkFrozen();
		this.cssClassGroupLast = cssClass;
	}

	@Override
	public void setCssClassGroupLast(String cssClass) {
		copyCssClassGroupLast(cssClass);
	}

	@Override
	public CellClassProvider getCssClassProvider() {
		return cssClassProvider;
	}

	@Override
	protected void copyCssClassProvider(CellClassProvider provider) {
		checkFrozen();
		cssClassProvider = provider;
	}

	@Override
	public void setClassifiers(Collection<String> classifiers) {
		copyClassifiers(classifiers);
	}

	@Override
	protected void copyClassifiers(Collection<String> classifiers) {
		checkFrozen();
		_classifiers = classifiers == null ? Collections.emptySet() : Collections.unmodifiableCollection(classifiers);
	}
	
	@Override
	public Collection<String> getClassifiers() {
		return _classifiers;
	}

	@Override
	public void setVisibility(DisplayMode visibility) {
		copyVisibility(visibility);
	}

	@Override
	protected void copyVisibility(DisplayMode visibility) {
		checkFrozen();
		this.visibility = visibility;
	}

	@Override
	public DisplayMode getVisibility() {
		return visibility;
	}

	@Override
	public List<HTMLFragmentProvider> getAdditionalHeaders() {
		return list(_additionalHeaders);
	}

	@Override
	protected void copyAdditionalHeaders(List<HTMLFragmentProvider> additionalHeader) {
		checkFrozen();
		_additionalHeaders = list(additionalHeader);
	}

	@Override
	public void setAdditionalHeaders(List<HTMLFragmentProvider> additionalHeader) {
		copyAdditionalHeaders(additionalHeader);
	}

	@Override
	public PDFRenderer getPDFRenderer() {
		return _pdfRenderer;
	}

	@Override
	protected void copyPDFRenderer(PDFRenderer value) {
		checkFrozen();
		_pdfRenderer = value;
	}

	@Override
	public void setPDFRenderer(PDFRenderer value) {
		copyPDFRenderer(value);
	}

	@Override
	public void freeze() {
		super.freeze();
		_columns.freeze();
	}

}

