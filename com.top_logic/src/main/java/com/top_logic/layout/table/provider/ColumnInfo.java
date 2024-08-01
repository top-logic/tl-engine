/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;


import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.security.SecurityAddingTableConfiguration;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.ColumnConfigurator;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfo;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.ui.CssClassProvider;
import com.top_logic.model.annotate.ui.PDFRendererAnnotation;
import com.top_logic.model.annotate.ui.TLCssClass;
import com.top_logic.model.export.ConcatenatedPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * Algorithm providing values for a {@link ColumnConfiguration}.
 * <p>
 * This class and all direct and indirect subclasses have to be immutable, recursively, as they are
 * shared among all {@link Thread}s and sessions via the {@link TableConfigModelInfo}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ColumnInfo implements ColumnConfigurator {

	/**
	 * Property on a {@link ColumnConfiguration} declared by the generic table configuration.
	 * 
	 * <p>
	 * The property yields the {@link TLType content types} the column displays.
	 * </p>
	 */
	public static final Property<TLTypeContext> TYPES = TypedAnnotatable.property(TLTypeContext.class, "types");

	private final TLTypeContext _contentType;

	private Set<PreloadContribution> _preloadContributions = new LinkedHashSet<>();

	private final ResKey _headerI18NKey;

	private final Accessor _accessor;

	private boolean _needSecurity;

	private DisplayMode _visibility;

	private String _staticCss;

	private CellClassProvider _cellClassProvider;

	/**
	 * Creates a {@link ColumnInfo}.
	 * 
	 * @param contentType
	 *        See {@link #getTypeContext()}.
	 * @param headerI18NKey
	 *        See {@link #getHeaderI18NKey()}.
	 * @param accessor
	 *        See {@link #getAccessor()}
	 */
	public ColumnInfo(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility, Accessor accessor) {
		_contentType = contentType;
		_headerI18NKey = headerI18NKey;
		_visibility = visibility;
		_accessor = accessor;

		TLTypePart typePart = contentType.getTypePart();
		if (typePart != null) {
			TLCssClass cssAnnotation = typePart.getAnnotation(TLCssClass.class);
			if (cssAnnotation != null) {
				_staticCss = cssAnnotation.getValue();
				PolymorphicConfiguration<? extends CssClassProvider> dynamicCss = cssAnnotation.getDynamicCssClass();
				if (dynamicCss != null) {
					CssClassProvider provider = TypedConfigUtil.createInstance(dynamicCss);
					_cellClassProvider = CellClassAdapter.wrap(contentType, provider);
				}
			}
		}
	}

	/**
	 * The content type of the cells.
	 */
	public TLTypeContext getTypeContext() {
		return _contentType;
	}

	/**
	 * The {@link DisplayMode} to apply to the column, or <code>null</code> if no changes should be
	 * performed.
	 */
	public DisplayMode getVisibility() {
		return _visibility;
	}

	/**
	 * @see #getVisibility()
	 */
	public void setVisibility(DisplayMode visibility) {
		_visibility = visibility;
	}

	/**
	 * The resource key to use for creating the column label.
	 */
	public ResKey getHeaderI18NKey() {
		return _headerI18NKey;
	}

	/**
	 * The {@link Accessor} to use to get value from row object.
	 */
	public Accessor getAccessor() {
		return _accessor;
	}

	/**
	 * Adapts {@link ColumnConfiguration#getComparator()} and
	 * {@link ColumnConfiguration#getDescendingComparator()}.
	 */
	protected abstract void setComparators(ColumnConfiguration column);

	/**
	 * Adapts {@link ColumnConfiguration#getFilterProvider()}.
	 */
	protected abstract void setFilterProvider(ColumnConfiguration column);
	
	/**
	 * Adapts {@link ColumnConfiguration#getCellExistenceTester()}.
	 */
	protected abstract void setCellExistenceTester(ColumnConfiguration column);
	
	/**
	 * Adapts {@link ColumnConfiguration#getExcelRenderer()}.
	 */
	protected abstract void setExcelRenderer(ColumnConfiguration column);

	/**
	 * Adapts {@link ColumnConfiguration#getCellRenderer()}.
	 */
	protected abstract void setRenderer(ColumnConfiguration column);

	/**
	 * Adapts {@link ColumnConfiguration#getPDFRenderer()}.
	 */
	protected void setPDFRenderer(ColumnConfiguration column) {
		setAnnotatedPDFRenderer(column, getTypeContext());
	}

	@Override
	public final void adapt(ColumnConfiguration column) {
		column.set(TYPES, getTypeContext());
		internalAdapt(column);
	}

	/**
	 * Implementation of {@link #adapt(ColumnConfiguration)}.
	 */
	protected void internalAdapt(ColumnConfiguration column) {
		if (_needSecurity) {
			SecurityAddingTableConfiguration.protectColumn(column);
		}
		if (column.getColumnLabelKey() == null) {
			column.setColumnLabelKey(_headerI18NKey);
		}
		if (_accessor != null) {
			column.setAccessor(_accessor);
		}
		if (column.getComparator() == ComparableComparator.INSTANCE || column.getComparator() == null) {
			setComparators(column);
		}
		if (column.getFilterProvider() == LabelFilterProvider.INSTANCE
			|| column.getFilterProvider() == null) {
			setFilterProvider(column);
		}
		if (column.getCellExistenceTester() == AllCellsExist.INSTANCE
			|| column.getCellExistenceTester() == null) {
			setCellExistenceTester(column);
		}
		if (column.getExcelRenderer() == null) {
			setExcelRenderer(column);
		}
		if (column.getCellRenderer() == null) {
			setRenderer(column);
		}
		if (!_preloadContributions.isEmpty()) {
			column.setPreloadContribution(ConcatenatedPreloadContribution.toPreloadContribution(_preloadContributions));
		}

		ControlProvider controlProvider = getControlProvider();
		if (controlProvider != null) {
			column.setEditControlProvider(controlProvider);
		}
		if (_visibility != null) {
			column.setVisibility(_visibility);
		}
		if (column.getPDFRenderer() == null) {
			setPDFRenderer(column);
		}
		if (_staticCss != null) {
			column.setCssClass(_staticCss);
		}
		if (_cellClassProvider != null) {
			column.setCssClassProvider(_cellClassProvider);
		}
	}

	/**
	 * The control provider for the column, if the column should be edited.
	 */
	protected abstract ControlProvider getControlProvider();

	/**
	 * Adds the given {@link PreloadContribution}s to the {@link PreloadContribution} for the
	 * column.
	 * 
	 * @param contribution
	 *        The contribution to add.
	 * 
	 * @see ColumnConfiguration#getPreloadContribution()
	 * 
	 */
	public void addPreloadContribution(PreloadContribution contribution) {
		_preloadContributions.add(contribution);
	}

	/**
	 * Joins this {@link ColumnInfo} with the given one to produce a {@link ColumnInfo} describing a
	 * column that results from displaying different attributes in the same column.
	 * 
	 * @param other
	 *        The {@link ColumnInfo} describing the column of the other attribute.
	 * @return the joined {@link ColumnInfo} describing the resulting column.
	 */
	public final ColumnInfo join(ColumnInfo other) {
		boolean thisNeedsSecurity = _needSecurity;
		boolean otherNeedsSecurity = other._needSecurity;
		ColumnInfo join = internalJoin(other);
		if (thisNeedsSecurity || otherNeedsSecurity) {
			join.ensureSecurity();
		}
		return join;
	}

	/**
	 * Algorithm for {@link #join(ColumnInfo) joining} {@link ColumnInfo}s.
	 */
	protected abstract ColumnInfo internalJoin(ColumnInfo other);

	/**
	 * Forces the column to configured the represented {@link ColumnConfiguration} with security,
	 * i.e. the values for this column can not be seen by the user if he has not enough access
	 * rights.
	 * 
	 * @see SecurityAddingTableConfiguration
	 */
	public void ensureSecurity() {
		_needSecurity = true;
	}

	/**
	 * Determines the {@link PDFRenderer} annotated at the given {@link TLModelPart}.
	 * 
	 * @param column
	 *        Column to set set {@link PDFRenderer} to.
	 * @param modelPart
	 *        {@link TLModelPart} to get annotation from.
	 */
	protected boolean setAnnotatedPDFRenderer(ColumnConfiguration column, AnnotationLookup modelPart) {
		PDFRendererAnnotation annotation = modelPart.getAnnotation(PDFRendererAnnotation.class);
		if (annotation != null) {
			column.setPDFRenderer(TypedConfigUtil.createInstance(annotation.getImpl()));
			return true;
		} else {
			return false;
		}
	}

}