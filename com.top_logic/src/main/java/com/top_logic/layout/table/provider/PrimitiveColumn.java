/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.text.Format;
import java.util.Comparator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.BooleanComparatorNullAsFalse;
import com.top_logic.basic.col.BooleanComparatorNullSafe;
import com.top_logic.basic.col.NullSafeComparator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.DocumentDownloadRenderer;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.basic.DispatchingRenderer;
import com.top_logic.layout.form.control.FirstLineRenderer;
import com.top_logic.layout.form.control.TextPopupControl;
import com.top_logic.layout.form.tag.CheckboxInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.BooleanLabelProvider;
import com.top_logic.layout.provider.BooleanLabelProviderNullAsFalse;
import com.top_logic.layout.provider.BooleanResourceProvider;
import com.top_logic.layout.provider.BooleanResourceProviderNullAsFalse;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.filter.AllOperatorsProvider;
import com.top_logic.layout.table.filter.ComparableTableFilterProvider;
import com.top_logic.layout.table.filter.ComparisonOperatorsProvider;
import com.top_logic.layout.table.filter.DateComparator;
import com.top_logic.layout.table.filter.DateTableFilterProvider;
import com.top_logic.layout.table.filter.FloatValueOperatorsProvider;
import com.top_logic.layout.table.filter.GenericFormatProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider.Config;
import com.top_logic.layout.table.filter.NumberComparator;
import com.top_logic.layout.table.filter.NumberTableFilterProvider;
import com.top_logic.layout.table.filter.SimpleBooleanFilterProvider;
import com.top_logic.layout.table.filter.TristateBooleanFilterProvider;
import com.top_logic.layout.table.filter.WrapperValueExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.FormattedValueExcelRenderer;
import com.top_logic.util.TLCollator;

/**
 * {@link ColumnInfo} configuring a column displaying {@link TLPrimitive} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrimitiveColumn extends ColumnInfo {

	private final TLTypeContext _primitiveType;

	private static final Comparator<?> NULL_SAFE_DATE_COMPARATOR = new NullSafeComparator<DateComparator>(DateComparator.INSTANCE, true);
	private static final Comparator<?> NULL_SAFE_DATE_COMPARATOR_DESCENDING = new NullSafeComparator<DateComparator>(DateComparator.INSTANCE_DESCENDING, false);
	private static final Comparator<?> NULL_SAFE_NUMBER_COMPARATOR = new NullSafeComparator<DateComparator>(NumberComparator.INSTANCE, true);
	private static final Comparator<?> NULL_SAFE_NUMBER_COMPARATOR_DESCENDING = new NullSafeComparator<DateComparator>(NumberComparator.INSTANCE_DESCENDING, false);

	private final boolean _tristate;

	private final Kind _primitiveKind;

	/**
	 * Creates a {@link PrimitiveColumn}.
	 * @param primitiveType
	 *        The content type.
	 * @param primitiveKind
	 *        {@link TLPrimitive#getKind()}.
	 */
	public PrimitiveColumn(TLTypeContext primitiveType, Kind primitiveKind, ResKey headerI18NKey, DisplayMode visibility,
			Accessor accessor) {
		super(primitiveType, headerI18NKey, visibility, accessor);
		_primitiveType = primitiveType;
		_primitiveKind = primitiveKind;
		_tristate = _primitiveKind == Kind.TRISTATE;
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		String pattern;
		try {
			pattern = getFormatPattern(_primitiveType);
		} catch (ConfigurationException ex) {
			Logger.error("Type " + _primitiveKind + " has invalid format annotation.", ex, PrimitiveColumn.class);
			pattern = null;
		}
		if (pattern != null) {
			ExcelCellRenderer excelRenderer = new FormattedValueExcelRenderer(pattern);
			column.setExcelRenderer(excelRenderer);
		}
	}

	@Override
	protected
	void setComparators(ColumnConfiguration column) {
		switch (_primitiveKind) {
			case STRING: {
				column.setComparator(new TLCollator());
				column.setDescendingComparator(null);
				break;
			}

			case DATE: {
				column.setComparator(NULL_SAFE_DATE_COMPARATOR);
				column.setDescendingComparator(NULL_SAFE_DATE_COMPARATOR_DESCENDING);
				setFormatLabelProvider(column);

				break;
			}

			case TRISTATE:
			case BOOLEAN: {
				if (_tristate) {
					// Null-safe comparison is necessary, because the value might not yet have been
					// initialized.
					column.setComparator(BooleanComparatorNullSafe.INSTANCE);
					column.setDescendingComparator(BooleanComparatorNullSafe.DESCENDING_INSTANCE);
				} else {
					column.setComparator(BooleanComparatorNullAsFalse.INSTANCE);
					column.setDescendingComparator(BooleanComparatorNullAsFalse.DESCENDING_INSTANCE);
				}
				break;
			}

			case INT:
			case FLOAT: {
				column.addCssClass("tblRight");
				column.setComparator(NULL_SAFE_NUMBER_COMPARATOR);
				column.setDescendingComparator(NULL_SAFE_NUMBER_COMPARATOR_DESCENDING);

				setFormatLabelProvider(column);

				break;
			}

			case BINARY: {
				column.setComparator(null);
				column.setDescendingComparator(null);
				break;
			}
			case CUSTOM:
				// There is no generic comparator for custom attribute type: Value type is unknown.
				break;
		}
	}

	private void setFormatLabelProvider(ColumnConfiguration column) {
		column.setLabelProvider(new FormatLabelProvider(GenericFormatProvider.INSTANCE.getFormat(column)));
	}

	private String getFormatPattern(AnnotationLookup modelPart) throws ConfigurationException {
		switch (_primitiveKind) {
			case INT:
				return DisplayAnnotations.getLongFormatPattern(modelPart);
			case FLOAT:
				return DisplayAnnotations.getFloatFormatPattern(modelPart);
			case DATE:
				return DisplayAnnotations.getDateFormatPattern(modelPart);
			default:
				return null;
		}
	}

	@Override
	protected ControlProvider getControlProvider() {
		switch (_primitiveKind) {
			case TRISTATE:
			case BOOLEAN: {
				TLTypeContext typeContext = getTypeContext();
				{
					BooleanPresentation booleanDisplay = DisplayAnnotations.getBooleanDisplay(typeContext);

					// TODO: This should be routed through the AttributeSettings service after
					// consolidating it in a common module. See BooleanTagProvider in tl-element.
					CheckboxInputTag result = new CheckboxInputTag();
					result.setDisplay(booleanDisplay);
					result.setResetable(!isMandatory() && _tristate);

					return result;
				}
			}
			case BINARY:
			case CUSTOM:
			case DATE:
			case FLOAT:
			case INT:
			case STRING:
				// No special ControlProvider
				return null;
		}
		throw new UnreachableAssertion("Unexpected kind: " + _primitiveKind);
	}

	@Override
	protected
	void setFilterProvider(ColumnConfiguration column) {
		switch (_primitiveKind) {
			case STRING: {
				MultiLine stringDisplay = getAnnotation(MultiLine.class);
				if (stringDisplay != null && stringDisplay.getValue()) {
					Config configItem = TypedConfiguration.newConfigItem(LabelFilterProvider.Config.class);
					PropertyDescriptor property =
						configItem.descriptor().getProperty(AbstractTableFilterProvider.Config.SHOW_OPTION_ENTRIES);
					configItem.update(property, false);
					try {
						column.setFilterProvider(
							new LabelFilterProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
								configItem));
					} catch (ConfigurationException ex) {
						Logger.error("Could not generate table filter provider for column '" + column.getName() + "'",
							ex, this);
					}
				} else {
					column.setFilterProvider(LabelFilterProvider.INSTANCE);
				}
				break;
			}

			case DATE: {
				column.setFilterProvider(new DateTableFilterProvider(false));
				break;
			}

			case TRISTATE:
			case BOOLEAN: {
				column.setFilterProvider(_tristate
					? TristateBooleanFilterProvider.INSTANCE
					: SimpleBooleanFilterProvider.INSTANCE);
				break;
			}

			case INT: {
				column.setFilterProvider(getNumberFilterProvider(AllOperatorsProvider.INSTANCE));
				break;
			}
			case FLOAT: {
				column.setFilterProvider(getNumberFilterProvider(FloatValueOperatorsProvider.INSTANCE));
				break;
			}

			case BINARY: {
				column.setFilterProvider(null);
				break;
			}
			case CUSTOM:
				// There is no generic filter for custom attribute type: Value type is unknown.
				break;
		}
	}

	private ComparableTableFilterProvider getNumberFilterProvider(ComparisonOperatorsProvider operatorProvider) {
		return new NumberTableFilterProvider(false, operatorProvider);
	}

	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		if (getTypeContext().getTypePart() != null) {
			column.setCellExistenceTester(WrapperValueExistenceTester.INSTANCE);
		}
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		switch (_primitiveKind) {
			case STRING: {
				MultiLine stringDisplay = getAnnotation(MultiLine.class);
				if (stringDisplay != null) {
					if (stringDisplay.getValue()) {
						column.setRenderer(FirstLineRenderer.DEFAULT_INSTANCE);
						column.setFullTextProvider(DefaultLabelProvider.INSTANCE);
						column.setEditControlProvider(TextPopupControl.CP.DEFAULT_INSTANCE);
					}
				}
				break;
			}
			case TRISTATE:
			case BOOLEAN: {
				{
					BooleanPresentation booleanDisplay = DisplayAnnotations.getBooleanDisplay(getTypeContext());
					switch (booleanDisplay) {
						case CHECKBOX: {
							if (_tristate) {
								column.setResourceProvider(BooleanResourceProvider.INSTANCE);
							} else {
								column.setResourceProvider(BooleanResourceProviderNullAsFalse.INSTANCE);
							}
							break;
						}

						case RADIO:
						case SELECT: {
							if (_tristate) {
								column.setLabelProvider(BooleanLabelProvider.INSTANCE);
							} else {
								column.setLabelProvider(BooleanLabelProviderNullAsFalse.INSTANCE);
							}
							break;
						}
					}
				}
				break;
			}
			case BINARY:
				column.setRenderer(DocumentDownloadRenderer.INSTANCE);
				break;

			case CUSTOM:
				column.setRenderer(DispatchingRenderer.INSTANCE);
				break;

			case DATE:
			case FLOAT:
			case INT:
				try {
					Format format = DisplayAnnotations.getConfiguredFormat(getTypeContext());
					if (format != null) {
						column.setLabelProvider(new FormatLabelProvider(format));
					}
				} catch (ConfigurationException ex) {
					Logger.error("Cannot instantiate format.", ex, PrimitiveColumn.class);
				}
				break;
		}
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		if (!(other instanceof PrimitiveColumn)) {
			return GenericColumn.joinColumns(this, other);
		}

		return joinPrimitive((PrimitiveColumn) other);
	}

	private ColumnInfo joinPrimitive(PrimitiveColumn other) {
		if (other._primitiveKind != _primitiveKind) {
			return GenericColumn.joinColumns(this, other);
		}

		if (isMandatory() && !other.isMandatory()) {
			return new PrimitiveColumn(_primitiveType, _primitiveKind, getHeaderI18NKey(), getVisibility(),
				getAccessor());
		} else {
			return this;
		}
	}

	private <T extends TLAnnotation> T getAnnotation(Class<T> annotation) {
		return _primitiveType.getAnnotation(annotation);
	}

	private boolean isMandatory() {
		return _primitiveType.isMandatory();
	}

}
