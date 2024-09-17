/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.layout.basic.ResourceRenderer.*;
import static com.top_logic.layout.provider.LabelResourceProvider.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.control.ColumnDescription;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.provider.DefaultCellClassProvider;
import com.top_logic.layout.table.renderer.CellControlRenderer;
import com.top_logic.layout.table.renderer.UniformCellRenderer;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelExportSupport;
import com.top_logic.tool.export.pdf.PDFRenderer;
import com.top_logic.util.css.CssUtil;

/**
 * Configuration of a table column.
 * 
 * @see TableConfiguration
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ColumnConfiguration extends ColumnBase
		implements TypedAnnotatable, ColumnContainer<ColumnConfiguration> {

	/**
	 * Special {@link Control} style used in table footers.
	 * 
	 * @see ControlProvider#createControl(Object, String)
	 */
	public static final String COLUMN_CONTROL_TYPE_FOOTER = "footer";

	/**
	 * Special {@link Control} style used in table headers.
	 * 
	 * @see ControlProvider#createControl(Object, String)
	 */
	public static final String COLUMN_CONTROL_TYPE_HEADER = "header";

	/**
	 * Default value of {@link #getSortKeyProvider()}.
	 */
	protected static final Mapping<Object, Object> DEFAULT_SORT_KEY_PROVIDER = Mappings.identity();

	/**
	 * Default value of {@link #getFilterProvider()}.
	 */
	protected static final TableFilterProvider DEFAULT_FILTER_PROVIDER = LabelFilterProvider.INSTANCE;

	/**
	 * Default value of {@link #getCellExistenceTester()}.
	 */
	protected static final CellExistenceTester DEFAULT_CELL_EXISTENCE_TESTER = AllCellsExist.INSTANCE;

	/**
	 * Default value of {@link #getResourceProvider()}.
	 */
	public static final ResourceProvider DEFAULT_RESOURCE_PROVIDER = MetaResourceProvider.INSTANCE;

	/**
	 * Default value of {@link #getComparator()}.
	 */
	protected static final Comparator<Object> DEFAULT_COMPARATOR = ComparableComparator.INSTANCE;

	/**
	 * Default value of {@link #getVisibility()}.
	 */
	protected static final DisplayMode DEFAULT_VISIBILITY = DisplayMode.visible;

	/**
	 * Default value of {@link #isShowHeader()}.
	 */
	protected static final boolean DEFAULT_SHOW_HEADER = true;

	/**
	 * Default value of {@link #isSelectable()}.
	 */
	protected static final boolean DEFAULT_SELECTABLE = true;

	/**
	 * Default value of {@link #getCssClassProvider()}.
	 */
	protected static final CellClassProvider DEFAULT_CSS_CLASS_PROVIDER = DefaultCellClassProvider.INSTANCE;

	/**
	 * Creates a {@link ColumnConfiguration}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	static ColumnConfiguration column(String name) {
		return new ColumnDescription(name);
	}

	/**
	 * Map that hold the properties accessed by the {@link TypedAnnotatable} methods.
	 */
	@Inspectable
	private InlineMap<TypedAnnotatable.Property<?>, Object> _properties = InlineMap.empty();

	/**
	 * Visibility of a column.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public enum DisplayMode {
		
		/**
		 * Column is displayed by default.
		 */
		visible(true),
		
		/**
		 * Column is hidden by default but can be chosen by the user.
		 */
		hidden(false),

		/**
		 * Column is {@link #visible} an cannot be removed by the user.
		 */
		mandatory(true),

		/**
		 * Column is hidden by default and cannot be chosen by the user.
		 */
		excluded(false),

		;

		private final boolean _displayed;

		private DisplayMode(boolean displayed) {
			_displayed = displayed;
		}

		/**
		 * Whether a column with this {@link DisplayMode} is actually shown.
		 */
		public boolean isDisplayed() {
			return _displayed;
		}
	}

	@Override
	public final ColumnConfiguration removeColumn(String columnName) {
		return columns().removeColumn(columnName);
	}

	@Override
	public final ColumnConfiguration getDefaultColumn() {
		return columns().getDefaultColumn();
	}

	@Override
	public final ColumnConfiguration getDeclaredColumn(String aName) {
		return columns().getDeclaredColumn(aName);
	}

	@Override
	public final ColumnConfiguration getCol(String aName) {
		return columns().getCol(aName);
	}

	@Override
	public final ColumnConfiguration declareColumn(String columnName) {
		return columns().declareColumn(columnName);
	}

	@Override
	public final Collection<? extends ColumnConfiguration> getDeclaredColumns() {
		return columns().getDeclaredColumns();
	}

	@Override
	public final Map<String, ColumnConfiguration> createColumnIndex() {
		return columns().createColumnIndex();
	}

	@Override
	public final List<ColumnConfiguration> getElementaryColumns() {
		return columns().getElementaryColumns();
	}

	@Override
	public final List<String> getElementaryColumnNames() {
		return columns().getElementaryColumnNames();
	}

	/**
	 * The {@link ColumnContainer} in use.
	 */
	protected abstract ColumnContainer<? extends ColumnConfiguration> columns();

	/**
	 * Shortcut for {@link #setResourceProvider(ResourceProvider)} with the {@link LabelProvider}
	 * converted into a {@link ResourceProvider}.
	 */
	public final void setLabelProvider(LabelProvider labelProvider) {
		setResourceProvider(toResourceProvider(labelProvider));
	}

	/**
	 * The {@link ResourceProvider} for the cell value.
	 * 
	 * @return Never null. The default is the {@link #DEFAULT_RESOURCE_PROVIDER}.
	 */
	public abstract ResourceProvider getResourceProvider();

	/** @see #getResourceProvider() */
	public abstract void setResourceProvider(ResourceProvider resourceProvider);

	/** @see #getResourceProvider() */
	protected abstract void copyResourceProvider(ResourceProvider resourceProvider);

	/**
	 * The {@link Renderer} for the cell value.
	 * 
	 * @see #getCellRenderer()
	 */
	public abstract Renderer<Object> getRenderer();

	/**
	 * @see #getRenderer()
	 */
	public abstract void setRenderer(Renderer<?> renderer);

	/**
	 * @see #getRenderer()
	 */
	protected abstract void copyRenderer(Renderer<Object> renderer);

	/**
	 * The {@link ControlProvider} to use for creating {@link Control}s for rendering application
	 * values in table cells.
	 * 
	 * <p>
	 * The method is a short-cut for configuring a {@link CellControlRenderer}.
	 * </p>
	 * 
	 * @see CellControlRenderer
	 */
	public final void setControlProvider(ControlProvider controlProvider) {
		if (controlProvider != null) {
			setCellRenderer(new CellControlRenderer(controlProvider));
		} else {
			// Imitate the old behavior (where control provider was a separate property) as good as
			// possible.
			setCellRenderer(null);
		}
	}

	/**
	 * {@link ControlProvider} to use, when (optionally) editing a table cell.
	 * 
	 * <p>
	 * In contrast to {@link #setControlProvider(ControlProvider)}, the optional control provider is
	 * not used for regular table display. The optional control provider may be used under certain
	 * circumstances by the framework, when a table cell is required to be edited explicitly. On
	 * example is the selected row in grid display.
	 * </p>
	 */
	public abstract ControlProvider getEditControlProvider();

	/**
	 * @see #getEditControlProvider()
	 */
	public abstract void setEditControlProvider(ControlProvider controlProvider);

	/**
	 * @see #getEditControlProvider()
	 */
	protected abstract void copyEditControlProvider(ControlProvider controlProvider);

	/**
	 * This method sets the column width.
	 * 
	 * @param aWidth
	 *        must be <code>null</code> or usable as HTML 'width'- value.
	 */
	public final void setDefaultColumnWidth(String aWidth) {
		String widthStyle = CssUtil.ensureWidthStyle(aWidth);
		internalSetWidthStyle(widthStyle);
	}

	/**
	 * @see #getDefaultColumnWidth()
	 */
	protected abstract void copyDefaultColumnWidth(String aWidth);

	/**
	 * Checks whether this {@link ColumnConfiguration} is classified by the given classifier.
	 */
	public final boolean isClassifiedBy(String aClassifier) {
		Collection<String> theClassifiers = getClassifiers();
		return theClassifiers == null ? false : theClassifiers.contains(aClassifier);
	}

	public final boolean isDefaultColumnWidthRelative() {
		return CssUtil.isRelativeWidth(getDefaultColumnWidth());
	}

	/**
	 * The {@link Accessor} for this column.
	 */
	public abstract Accessor getAccessor();

	/**
	 * @see #getAccessor()
	 */
	public abstract void setAccessor(Accessor accessor);

	/**
	 * @see #getAccessor()
	 */
	protected abstract void copyAccessor(Accessor accessor);

	/**
	 * The CSS class to use on content cells of this column.
	 * 
	 * @return may be <code>null</code> if no class was set.
	 * 
	 * @see #getCssHeaderClass()
	 */
	public abstract String getCssClass();

	/**
	 * The CSS class of this column's header.
	 * 
	 * @return may be <code>null</code> if no class was set.
	 */
	public abstract String getCssHeaderClass();

	/**
	 * CSS class to use on cells of the column that appear first in this group.
	 * 
	 * <p>
	 * Identical to {@link #getCssClass()} for elementary columns.
	 * </p>
	 * 
	 * @return may be <code>null</code> if no class was set.
	 */
	public abstract String getCssClassGroupFirst();

	/**
	 * @see #getCssClassGroupFirst()
	 */
	public abstract void setCssClassGroupFirst(String cssClass);

	/**
	 * @see #getCssClassGroupFirst()
	 */
	protected abstract void copyCssClassGroupFirst(String cssClass);

	/**
	 * CSS class to use on cells of the column that appear last in this group.
	 * 
	 * <p>
	 * Identical to {@link #getCssClass()} for elementary columns.
	 * </p>
	 * 
	 * @return may be <code>null</code> if no class was set.
	 */
	public abstract String getCssClassGroupLast();

	/**
	 * @see #getCssClassGroupLast()
	 */
	public abstract void setCssClassGroupLast(String cssClass);

	/**
	 * Provider that dynamically creates CSS classes for specific table cells.
	 */
	public abstract CellClassProvider getCssClassProvider();

	/**
	 * @see #getCssClassProvider()
	 */
	public void setCssClassProvider(CellClassProvider provider) {
		if (provider == null) {
			provider = DEFAULT_CSS_CLASS_PROVIDER;
		}
		copyCssClassProvider(provider);
	}

	/**
	 * @see #getCssClassProvider()
	 */
	protected abstract void copyCssClassProvider(CellClassProvider provider);

	/**
	 * @see #getCssClassGroupLast()
	 */
	protected abstract void copyCssClassGroupLast(String cssClass);

	public abstract Comparator getDescendingComparator();

	public abstract Mapping getSortKeyProvider();

	public abstract PreloadContribution getPreloadContribution();

	public final void setPreloadContribution(PreloadContribution value) {
		if (value == null) {
			copyPreloadContribution(EmptyPreloadContribution.INSTANCE);
		} else {
			copyPreloadContribution(value);
		}
	}

	/**
	 * @see #getPreloadContribution()
	 */
	protected abstract void copyPreloadContribution(PreloadContribution value);

	public abstract Comparator getComparator();

	public abstract String getCommandGroup();

	/**
	 * Whether cells of this column can be clicked to select their rows.
	 * 
	 * <p>
	 * The default value is <code>true</code>.
	 * </p>
	 * 
	 * <p>
	 * The option has no effect, if the whole table cannot be selected.
	 * </p>
	 */
	public abstract boolean isSelectable();

	public abstract LabelProvider getFullTextProvider();

	public abstract TableFilterProvider getFilterProvider();
	
	public abstract CellExistenceTester getCellExistenceTester();
	
	public abstract ExcelCellRenderer getExcelRenderer();

	/**
	 * The renderer for cell decorations.
	 * 
	 * @see #getRenderer()
	 */
	public abstract CellRenderer getCellRenderer();

	public abstract String getName();

	public abstract FieldProvider getFieldProvider();

	public abstract String getCellStyle();

	/**
	 * The configured column width.
	 * 
	 * <p>
	 * Note: The actual column width may be overridden by
	 * {@link TableViewModel#getProgrammaticColumnWidth(int)} and the renderer hook
	 * {@link TableRenderer#hookGetColumnWidth(ColumnConfiguration)}.
	 * </p>
	 * 
	 * @return may be <code>null</code>
	 */
	public abstract String getDefaultColumnWidth();

	/**
	 * Classifiers of this column.
	 *
	 * @return The collection of classifiers of this column. May be empty. Must not be modified.
	 * 
	 * @see #setClassifiers(Collection)
	 */
	public abstract Collection<String> getClassifiers();

	/** Adds the given CSS class(es) to this column. */
	public void addCssClass(String cssClass) {
		setCssClass(CssUtil.joinCssClassesUnique(getCssClass(), cssClass));
	}

	/** Removes the given CSS class(es) from this column. */
	public void removeCssClass(String cssClass) {
		setCssClass(CssUtil.removeCssClasses(getCssClass(), cssClass));
	}

	/**
	 * @see #getCssClass()
	 */
	public abstract void setCssClass(String cssClass);

	/**
	 * @see #getCssClass()
	 */
	protected abstract void copyCssClass(String cssClass);

	/**
	 * @see #getCssHeaderClass()
	 */
	public abstract void setCssHeaderClass(String cssClass);

	/** Adds the given CSS class(es) to the {@link #getCssHeaderClass()} of this column. */
	public void addCssHeaderClass(String cssClass) {
		setCssHeaderClass(CssUtil.joinCssClassesUnique(getCssHeaderClass(), cssClass));
	}

	/** Removes the given CSS class(es) from the {@link #getCssHeaderClass()} of this column. */
	public void removeCssHeaderClass(String cssClass) {
		setCssHeaderClass(CssUtil.removeCssClasses(getCssHeaderClass(), cssClass));
	}

	/**
	 * @see #getCssHeaderClass()
	 */
	protected abstract void copyCssHeaderClass(String cssClass);


	public abstract void setDescendingComparator(Comparator descendingComparator);

	/**
	 * @see #getDescendingComparator()
	 */
	protected abstract void copyDescendingComparator(Comparator descendingComparator);

	public abstract void setCommandGroup(String aCommandGroup);

	/**
	 * @see #getCommandGroup()
	 */
	protected abstract void copyCommandGroup(String aCommandGroup);

	/**
	 * Sets the {@link #isSelectable()} property.
	 */
	public abstract void setSelectable(boolean selectable);

	/**
	 * @see #isSelectable()
	 */
	protected abstract void copySelectable(boolean selectable);

	/**
	 * Sets the classifiers for this {@link ColumnConfiguration}.
	 * 
	 * @see #getClassifiers()
	 * @see #isClassifiedBy(String)
	 */
	public abstract void setClassifiers(Collection<String> classifiers);

	/**
	 * @see #getClassifiers()
	 */
	protected abstract void copyClassifiers(Collection<String> classifiers);

	/**
	 * @see #hasExcludedFilterFromSidebar()
	 */
	public abstract void setExcludeFilterFromSidebar(boolean excluded);

	/**
	 * @see #hasExcludedFilterFromSidebar()
	 */
	protected abstract void copyExcludeFilterFromSidebar(boolean excluded);

	/**
	 * true, if the filter of this column shall not be available in filter sidebar, false
	 *         otherwise.
	 */
	public abstract boolean hasExcludedFilterFromSidebar();

	/**
	 * @see #getFilterProvider()
	 */
	public abstract void setFilterProvider(TableFilterProvider aProvider);

	/**
	 * @see #getFilterProvider()
	 */
	protected abstract void copyFilterProvider(TableFilterProvider aProvider);
	
	/**
	 * @see #getCellExistenceTester()
	 */
	public abstract void setCellExistenceTester(CellExistenceTester cellExistenceTester);

	/**
	 * @see #getCellExistenceTester()
	 */
	protected abstract void copyCellExistenceTester(CellExistenceTester cellExistenceTester);
	
	public abstract void setExcelRenderer(ExcelCellRenderer renderer);

	/**
	 * @see ColumnConfiguration#getExcelRenderer()
	 */
	protected abstract void copyExcelRenderer(ExcelCellRenderer renderer);

	public abstract void setFullTextProvider(LabelProvider fullTextProvider);

	/**
	 * @see #getFullTextProvider()
	 */
	protected abstract void copyFullTextProvider(LabelProvider fullTextProvider);

	protected abstract void setFullTextProviderAsDefault(LabelProvider fullTextProvider);

	/**
	 * @see #getCellRenderer()
	 */
	public abstract void setCellRenderer(CellRenderer cellRenderer);

	/**
	 * @see #getCellRenderer()
	 */
	protected abstract void copyCellRenderer(CellRenderer cellRenderer);

	public abstract void setFieldProvider(FieldProvider fieldProvider);

	/**
	 * @see #getFieldProvider()
	 */
	protected abstract void copyFieldProvider(FieldProvider fieldProvider);

	protected abstract void internalSetWidthStyle(String widthStyle);

	/**
	 * This method sets the cellStyle.
	 * 
	 * @param aCellStyle
	 *        The cellStyle to set.
	 */
	public final void setCellStyle(String aCellStyle) {
		String terminatedStyle = CssUtil.terminateStyleDefinition(aCellStyle);
		internalSetCellStyle(terminatedStyle);
	}

	/**
	 * @see #getCellStyle()
	 */
	protected abstract void copyCellStyle(String terminatedStyle);

	protected abstract void internalSetCellStyle(String terminatedStyle);

	/**
	 * Whether this column is displayed by default.
	 * 
	 * <p>
	 * The default value is <code>true</code>.
	 * </p>
	 */
	public final boolean isVisible() {
		return getVisibility().isDisplayed();
	}

	/**
	 * Sets the {@link #isVisible()} property.
	 */
	public final void setVisible(boolean newVisible) {
		if (newVisible) {
			if (!isVisible()) {
				this.setVisibility(DisplayMode.visible);
			}
		} else {
			this.setVisibility(DisplayMode.hidden);
		}
	}

	/**
	 * Whether this column cannot be removed from view.
	 * 
	 * <p>
	 * The default value is <code>true</code>.
	 * </p>
	 */
	public final boolean isMandatory() {
		return getVisibility() == DisplayMode.mandatory;
	}

	/**
	 * Sets the {@link #isMandatory()} property.
	 */
	public final void setMandatory(boolean newMandatory) {
		if (newMandatory) {
			this.setVisibility(DisplayMode.mandatory);
		} else {
			if (isMandatory()) {
				this.setVisibility(DisplayMode.visible);
			}
		}
	}

	public abstract void setVisibility(DisplayMode visibility);

	protected abstract void copyVisibility(DisplayMode visibility);

	public abstract DisplayMode getVisibility();

	public final boolean isSortable() {
		return getComparator() != null;
	}

	public final void setSortable(boolean newSortable) {
		if (newSortable) {
			if (!isSortable()) {
				setComparator(ComparableComparator.INSTANCE);
			}
		} else {
			setComparator(null);
		}
	}

	public final void setSortKeyProvider(Mapping value) {
		Mapping mapping = value == null ? Mappings.identity() : value;
		copySortKeyProvider(mapping);
	}

	protected abstract void copySortKeyProvider(Mapping mapping);

	public final void setComparator(Comparator comparator) {
		if (comparator == null) {
			setDescendingComparator(null);
		}
		copyComparator(comparator);
	}

	protected abstract void copyComparator(Comparator comparator);

	/** @see ColumnConfig#getAdditionalHeaders() */
	public abstract List<HTMLFragmentProvider> getAdditionalHeaders();

	/** @see #getAdditionalHeaders() */
	public abstract void setAdditionalHeaders(List<HTMLFragmentProvider> additionalHeader);

	/** @see #getAdditionalHeaders() */
	protected abstract void copyAdditionalHeaders(List<HTMLFragmentProvider> additionalHeader);

	/** @see ColumnConfig#getPDFRenderer() */
	public abstract PDFRenderer getPDFRenderer();

	/** @see #getPDFRenderer() */
	public abstract void setPDFRenderer(PDFRenderer value);

	/** @see #getPDFRenderer() */
	protected abstract void copyPDFRenderer(PDFRenderer value);

	/**
	 * Creates a copy of this {@link ColumnConfiguration} with the given name, see
	 * {@link #getName()}.
	 */
	public abstract ColumnConfiguration copy(String columnName);

	/**
	 * Copies the values of this {@link ColumnConfiguration} to the given target configuration.
	 */
	public void copyTo(ColumnConfiguration target) {
		target.putAnnotatedProperties(_properties);
	}

	private void putAnnotatedProperties(InlineMap<TypedAnnotatable.Property<?>, Object> properties) {
		_properties = _properties.putAllValues(properties);
	}

	@Override
	public <T> T set(TypedAnnotatable.Property<T> property, T value) {
		T oldValue = get(property);
		_properties = _properties.putValue(property, property.internalize(value));
		return oldValue;
	}

	@Override
	public <T> T get(TypedAnnotatable.Property<T> property) {
		return property.externalize(this, _properties.getValue(property));
	}

	@Override
	public boolean isSet(TypedAnnotatable.Property<?> property) {
		return _properties.hasValue(property);
	}

	@Override
	public <T> T reset(TypedAnnotatable.Property<T> property) {
		T oldValue = get(property);
		_properties = _properties.removeValue(property);
		return oldValue;
	}

	@Override
	protected Map<String, AbstractProperty> getProperties() {
		return PROPERTIES;
	}

	/**
	 * Returns a {@link CellRenderer} that is finally used to render the content of this column on
	 * the client.
	 */
	public CellRenderer finalCellRenderer() {
		CellRenderer cellRenderer = getCellRenderer();
		if (cellRenderer != null) {
			return cellRenderer;
		}
		return new UniformCellRenderer(finalRenderer());
	}

	/**
	 * Returns a {@link Renderer} that is finally used to create a {@link CellRenderer} if none is
	 * configured at this {@link ColumnConfiguration}.
	 * 
	 * @see #finalCellRenderer()
	 */
	public Renderer<Object> finalRenderer() {
		Renderer<Object> customRenderer = getRenderer();
		if (customRenderer == null) {
			customRenderer = newResourceRenderer(getResourceProvider());
		}
		return customRenderer;
	}

	/**
	 * Returns a {@link Renderer} that is finally used to export this column in the given
	 * {@link ExcelExportSupport}.
	 */
	public ExcelCellRenderer finalExcelCellRenderer(ExcelExportSupport excelExport) {
		ExcelCellRenderer renderer = getExcelRenderer();
		if (renderer == null) {
			renderer = excelExport.defaultExcelCellRenderer();
		}
		return renderer;
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, getName());
	}

	static abstract class Property extends AbstractProperty<ColumnConfiguration> {
		protected Property(String configName) {
			super(configName);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Map<String, AbstractProperty> PROPERTIES = TableUtil.index(
		ColumnBase.BASE_PROPERTIES,

		new ColumnConfiguration.Property(ColumnConfig.ACCESSOR) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setAccessor((Accessor) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyAccessor((Accessor) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getAccessor();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CELL_RENDERER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCellRenderer((CellRenderer) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCellRenderer((CellRenderer) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCellRenderer();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.RESOURCE_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setResourceProvider((ResourceProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyResourceProvider((ResourceProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getResourceProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.RENDERER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setRenderer((Renderer) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyRenderer((Renderer) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getRenderer();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.LABEL_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setLabelProvider((LabelProvider) value);
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				throw new UnsupportedOperationException("Must not be called, because this property is just used" +
														"as convenience setter of the cell renderer");
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.FULL_TEXT_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setFullTextProvider((LabelProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyFullTextProvider((LabelProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getFullTextProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.FIELD_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setFieldProvider((FieldProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyFieldProvider((FieldProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getFieldProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CONTROL_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setControlProvider((ControlProvider) value);
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				throw new UnsupportedOperationException("Use '" + ColumnConfig.CELL_RENDERER + "'.");
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CLASSIFIER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setClassifiers((Collection) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyClassifiers((Collection) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getClassifiers();
			}
		},

		new ColumnConfiguration.Property(ColumnConfig.SORTABLE) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setSortable((Boolean) value);
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.isSortable();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.SELECTABLE) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setSelectable((Boolean) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copySelectable((Boolean) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.isSelectable();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.VISIBILITY) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setVisibility((DisplayMode) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyVisibility((DisplayMode) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getVisibility();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.VISIBLE) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setVisible((Boolean) value);
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.isVisible();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.MANDATORY) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setMandatory((Boolean) value);
			}

			@Override
			public boolean isCopyableProperty(ColumnConfiguration self) {
				return false;
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.isMandatory();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.COMMAND_GROUP) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCommandGroup((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCommandGroup((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCommandGroup();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.COMPARATOR) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setComparator((Comparator) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyComparator((Comparator) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getComparator();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.DESCENDING_COMPARATOR) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setDescendingComparator((Comparator) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyDescendingComparator((Comparator) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getDescendingComparator();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CSS_CLASS) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCssClass((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCssClass((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCssClass();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CSS_HEADER_CLASS) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCssHeaderClass((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCssHeaderClass((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCssHeaderClass();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CSS_CLASS_GROUP_FIRST) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCssClassGroupFirst((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCssClassGroupFirst((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCssClassGroupFirst();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CSS_CLASS_GROUP_LAST) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCssClassGroupLast((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCssClassGroupLast((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCssClassGroupLast();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CSS_CLASS_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCssClassProvider((CellClassProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCssClassProvider((CellClassProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCssClassProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.EXCLUDE_FILTER_FROM_SIDEBAR) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setExcludeFilterFromSidebar((boolean) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyExcludeFilterFromSidebar((boolean) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.hasExcludedFilterFromSidebar();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CELL_EXISTENCE_TESTER) {

			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCellExistenceTester((CellExistenceTester) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCellExistenceTester((CellExistenceTester) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCellExistenceTester();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.FILTER_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setFilterProvider((TableFilterProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyFilterProvider((TableFilterProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getFilterProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.EXCEL_RENDERER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setExcelRenderer((ExcelCellRenderer) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyExcelRenderer((ExcelCellRenderer) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getExcelRenderer();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.SORT_KEY_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setSortKeyProvider((Mapping) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copySortKeyProvider((Mapping) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getSortKeyProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.EDIT_CONTROL_PROVIDER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setEditControlProvider((ControlProvider) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyEditControlProvider((ControlProvider) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getEditControlProvider();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.PRELOAD_CONTRIBUTION) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setPreloadContribution((PreloadContribution) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyPreloadContribution((PreloadContribution) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getPreloadContribution();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.COLUMN_WIDTH) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setDefaultColumnWidth((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyDefaultColumnWidth((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getDefaultColumnWidth();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.CELL_STYLE) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setCellStyle((String) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyCellStyle((String) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getCellStyle();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.ADDITIONAL_HEADERS) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setAdditionalHeaders(list((List<HTMLFragmentProvider>) value));
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyAdditionalHeaders(list((List<HTMLFragmentProvider>) value));
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getAdditionalHeaders();
			}
		},
		new ColumnConfiguration.Property(ColumnConfig.PDF_RENDERER) {
			@Override
			public void set(ColumnConfiguration self, Object value) {
				self.setPDFRenderer((PDFRenderer) value);
			}

			@Override
			public void copy(ColumnConfiguration self, Object value) {
				self.copyPDFRenderer((PDFRenderer) value);
			}

			@Override
			public Object get(ColumnConfiguration self) {
				return self.getPDFRenderer();
			}
		}
	);

}
