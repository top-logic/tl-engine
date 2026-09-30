/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.FontCharSizeMap;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.query.BooleanAttributeFilter;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.DateAttributeFilter;
import com.top_logic.element.meta.query.MinMaxFilter;
import com.top_logic.element.meta.query.NumberAttributeFilter;
import com.top_logic.element.meta.query.StringAttributeFilter;
import com.top_logic.element.meta.query.kbbased.KBBasedWrapperValuedAttributeFilter;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormFieldComparator;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.DefaultTooltipControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.template.ValueWithErrorControlProvider;
import com.top_logic.layout.provider.ImageButtonControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.partition.DatePartitionFunction.DateValueProvider;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MultiAttributeFilter implements InteractiveBuilder<Filter<Object>, ChartContextObserver>,
		Filter<Object>, CollectionFilter, ConfiguredInstance<MultiAttributeFilter.Config> {

	private static final String TABLE_FIELD = "table";

	private static final String ADD_BUTTON = "add";

	private static final String ATTRIBUTE_COLUMN = "attribute";

	private static final String VALUE_COLUMN = "value";

	private static final String REMOVE_COLUMN = "remove";

	static final Property<Object> ROW_OBJECT = TypedAnnotatable.property(Object.class, "rowObject");

	private static final Property<AttributeFormContext> ATTRIBUTE_FORM_CONTEXT =
		TypedAnnotatable.property(AttributeFormContext.class, "AttributeFormContext");

	ControlProvider RANGE_CONTROL_PROVIDER = new ControlProvider() {

		private Document _template = DOMUtil.parseThreadSafe(""
			+ "<span"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+ "<p:field name='" + AttributeFormFactory.SEARCH_FROM_FIELDNAME + "' />"
			+ HTMLConstants.NBSP + "-" + HTMLConstants.NBSP + HTMLConstants.NBSP + HTMLConstants.NBSP
			+ "<p:field name='" + AttributeFormFactory.SEARCH_TO_FIELDNAME + "' />"
			+ "</span>");

		@Override
		public Control createControl(Object model, String style) {
			return new FormGroupControl((FormGroup) model,
				new FormTemplate(ResPrefix.NONE, SelectionControlProvider.SELECTION_INSTANCE, true, _template));
		}
	};

	ControlProvider VALUE_CONTROL_PROVIDER = ValueWithErrorControlProvider.newInstance(SelectionControlProvider.SELECTION_INSTANCE);
	
	private final Config _config;

	private Filter<Object> _filter;

	private List<CollectionFilter> _collectionFilters;

	private Revision _revision;

	/**
	 * Config-interface for {@link MultiAttributeFilter}.
	 */
	public interface Config extends PolymorphicConfiguration<MultiAttributeFilter> {

		/**
		 * <code>META_ELEMENT</code> Attribute name for meta-element-property
		 */
		String META_ELEMENT = "meta-element";

		/**
		 * <code>FILTERS</code> Attribute name for filters-property
		 */
		String FILTERS = "filters";

		/**
		 * <code>FILTERS</code> Attribute name for filters-property
		 */
		String EXCLUDE_ATTRIBUTES = "exclude-attributes";

		/**
		 * Indirect getter for the {@link TLClass} to prevent problems during startup.
		 * 
		 * @return a {@link Provider} for the {@link TLClass} this {@link MultiAttributeFilter}
		 *         is about
		 */
		@Name(META_ELEMENT)
		MetaElementProvider getMetaElement();

		/**
		 * the initially set filters
		 */
		@InstanceFormat
		@Name(FILTERS)
		List<FilterPart> getFilters();

		/**
		 * a list of attribute names that should not be included as filter option
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(EXCLUDE_ATTRIBUTES)
		List<String> getExcludeAttributes();
	}

	/**
	 * Base-class for initially set filters.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static abstract class FilterPart {

		private final Config _config;

		/**
		 * Base-config-interface for {@link FilterPart}.
		 */
		public interface Config extends PolymorphicConfiguration<FilterPart> {
			/**
			 * the name of the attribute this filter is about
			 */
			public String getAttribute();
		}

		/**
		 * Config-constructor for {@link FilterPart}
		 * 
		 * @param context
		 *        - default config-constructor
		 * @param config
		 *        - default config-constructor
		 */
		public FilterPart(InstantiationContext context, FilterPart.Config config) {
			_config = config;
		}

		/**
		 * the {@link Config} of this.
		 */
		protected Config getConfig() {
			return _config;
		}

		/**
		 * the configured attribute for the given type
		 */
		protected TLStructuredTypePart getAttribute(Set<? extends TLClass> types) {
			TLStructuredTypePart result = null;
			for (TLClass type : types) {
				result = type.getPart(getConfig().getAttribute());
				if (result != null) {
					break;
				}
			}
			return result;
		}

		/**
		 * @param ma
		 *        the attribute this filter is about
		 * @param member
		 *        the view to be initialized from the config
		 */
		public abstract void initValues(TLStructuredTypePart ma, FormMember member);
	}

	/**
	 * Base-class for filter-parts with from- and to-fields.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static abstract class FromToFilterPart extends FilterPart {

		/**
		 * Configured classification-filter for {@link MultiAttributeFilter}.
		 */
		public FromToFilterPart(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void initValues(TLStructuredTypePart ma, FormMember member) {
			FormField fromField = ((FormContainer) member).getField(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
			FormField toField = ((FormContainer) member).getField(AttributeFormFactory.SEARCH_TO_FIELDNAME);
			initValues(ma, fromField, toField);
		}

		/**
		 * @param ma
		 *        the attribute this filter is about
		 * @param fromField
		 *        the from-field to be initialized from the config
		 * @param toField
		 *        the to-field to be initialized from the config
		 */
		protected abstract void initValues(TLStructuredTypePart ma, FormField fromField, FormField toField);

	}

	/**
	 * Configured classification-filter for {@link MultiAttributeFilter}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class ClassificationFilter extends FilterPart {

		/**
		 * Config-interface for {@link ClassificationFilter}.
		 */
		public interface Config extends FilterPart.Config {

			@Override
			@ClassDefault(ClassificationFilter.class)
			public Class<ClassificationFilter> getImplementationClass();

			/**
			 * names of the {@link FastListElement}s that are selected
			 */
			@Format(CommaSeparatedStrings.class)
			public List<String> getValues();
		}

		/**
		 * Config-constructor for {@link ClassificationFilter}
		 */
		public ClassificationFilter(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void initValues(TLStructuredTypePart ma, FormMember member) {
			FastList fastList = (FastList) ma.getType();
			List<FastListElement> values = new ArrayList<>();
			for (String name : ((Config) getConfig()).getValues()) {
				FastListElement fle = fastList.getElementByName(name);
				values.add(fle);
			}
			((SelectField) member).setAsSelection(values);
		}

	}

	/**
	 * Configured date-filter for {@link MultiAttributeFilter}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class DateFilter extends FromToFilterPart {

		/**
		 * Config-interface for {@link DateFilter}.
		 */
		public interface Config extends FilterPart.Config {

			/**
			 * the from-value of the date-interval
			 */
			@Format(DateValueProvider.class)
			public Date getFrom();

			/**
			 * the to-value of the date-interval
			 */
			@Format(DateValueProvider.class)
			public Date getTo();

		}

		/**
		 * Config-constructor for {@link DateFilter}
		 */
		public DateFilter(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected Config getConfig() {
			return (Config) super.getConfig();
		}

		@Override
		protected void initValues(TLStructuredTypePart ma, FormField fromField, FormField toField) {
			fromField.initializeField(getConfig().getFrom());
			toField.initializeField(getConfig().getTo());
		}

	}

	/**
	 * Config-constructor for {@link MultiAttributeFilter}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MultiAttributeFilter(InstantiationContext context, Config config) {
		_config = config;
		_filter = TrueFilter.INSTANCE;
	}

	private Set<? extends TLClass> getTypes() {
		return getConfig().getMetaElement().get();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		Set<? extends TLClass> types = getTypes();
		List<TLStructuredTypePart> attributes = filterSupported(GenericModelPreparationBuilder.allParts(types));
		SelectField attField = FormFactory.newSelectField(ATTRIBUTE_COLUMN, attributes, false, false);
		attField.setOptionComparator(new Comparator<TLStructuredTypePart>() {

			@Override
			public int compare(TLStructuredTypePart o1, TLStructuredTypePart o2) {
				String s1 = MetaResourceProvider.INSTANCE.getLabel(o1);
				String s2 = MetaResourceProvider.INSTANCE.getLabel(o2);
				return s1.compareToIgnoreCase(s2);
			}

		});
		AddRowExecutable addExecutable = new AddRowExecutable(attField);
		CommandField addField = FormFactory.newCommandField(ADD_BUTTON, addExecutable);

		addField.setImage(Icons.ADD_BUTTON);
		addField.setNotExecutableImage(Icons.ADD_BUTTON_DISABLED);

		ResPrefix resPrefix = ResPrefix.legacyString(getClass().getSimpleName());
		AttributeFormContext afc = new AttributeFormContext(resPrefix);
		container.set(ATTRIBUTE_FORM_CONTEXT, afc);

		TableConfiguration config = attributeTableConfiguration(container, afc, attField);

		List<String> cols = CollectionUtil.createList(REMOVE_COLUMN, ATTRIBUTE_COLUMN, VALUE_COLUMN);
		ObjectTableModel otm = new ObjectTableModel(cols, config, new ArrayList<>());
		FormTableModel tableModel = new FormTableModel(otm, afc);
		TableField table = FormFactory.newTableField(TABLE_FIELD, tableModel, false);
		addExecutable.setTable(table);
		container.addMember(attField);
		container.addMember(addField);
		container.addMember(table);

		int i = 0;
		for (FilterPart filter : getConfig().getFilters()) {
			TLStructuredTypePart ma = filter.getAttribute(getTypes());
			attField.setAsSingleSelection(ma);
			addExecutable.executeCommand(DefaultDisplayContext.getDisplayContext());
			FormMember member = (FormMember) tableModel.getValueAt(i++, VALUE_COLUMN);
			filter.initValues(ma, member);
		}

		template(container, div(
			fieldsetBox(
				resource(I18NConstants.ATTRIBUTE_FILTER),
				fragment(
					member(ATTRIBUTE_COLUMN),
					text(HTMLConstants.NBSP),
					member(ADD_BUTTON),
					member(TABLE_FIELD)),
				ConfigKey.field(container))));
	}

	private TableConfiguration attributeTableConfiguration(final FormContainer container,
			final AttributeFormContext formContext,
			final SelectField attField) {
		TableConfiguration config = TableConfiguration.table();
		config.setShowTitle(false);
		config.setShowColumnHeader(false);
		config.setShowFooter(false);

		ColumnConfiguration defaultColumn = config.getDefaultColumn();
		defaultColumn.setAccessor(IdentityAccessor.INSTANCE);
		defaultColumn.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
		defaultColumn.setShowHeader(false);

		ColumnConfiguration attributeColumn = config.declareColumn(ATTRIBUTE_COLUMN);
		final FontCharSizeMap charSizeMap = new FontCharSizeMap("Calibri", Font.BOLD, 10);
		attributeColumn.setFieldProvider(new AbstractFieldProvider() {
			@SuppressWarnings("rawtypes")
			@Override
			public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
				String fieldName = getFieldName(aModel, anAccessor, aProperty);
				String label = MetaResourceProvider.INSTANCE.getLabel(aModel);
				String shortLabel = StringServices.cutString(label, 1, 15, charSizeMap);
				if (!Utils.equals(label, shortLabel)) {
					shortLabel = shortLabel + "...";
				}
				StringField result = FormFactory.newStringField(fieldName, shortLabel, true);
				result.setTooltip(label);
				result.setControlProvider(DefaultTooltipControlProvider.INSTANCE);
				return result;
			}
		});
		attributeColumn.setComparator(FormFieldComparator.INSTANCE);
		attributeColumn.setSortable(false);

		ColumnConfiguration removeColumn = config.declareColumn(REMOVE_COLUMN);
		removeColumn.setFieldProvider(new AbstractFieldProvider() {
			@Override
			public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
				String fieldName = getFieldName(aModel, anAccessor, aProperty);
				RemoveRowExecutable result = new RemoveRowExecutable(fieldName, attField);
				TableField table = (TableField) container.getField(TABLE_FIELD);
				result.setTable(table, formContext.getAttributeUpdateContainer());
				result.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
				result.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
				result.set(ROW_OBJECT, aModel);
				return result;
			}
		});
		removeColumn.setControlProvider(ImageButtonControlProvider.INSTANCE);
		removeColumn.setSortable(false);

		ColumnConfiguration valueColumn = config.declareColumn(VALUE_COLUMN);
		valueColumn.setFieldProvider(new FieldProvider() {
			@SuppressWarnings("rawtypes")
			@Override
			public String getFieldName(Object aModel, Accessor anAccessor, String aProperty) {
				return MetaAttributeGUIHelper.getAttributeIDCreate((TLStructuredTypePart) aModel, null);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
				String fieldName = getFieldName(aModel, anAccessor, aProperty);
				TLStructuredTypePart aMA = (TLStructuredTypePart) aModel;
				AttributeUpdate update =
					AttributeUpdateFactory.createAttributeUpdateForSearch(formContext.getAttributeUpdateContainer(),
						aMA.getOwner(), aMA, null, null, null);
				FormMember input = formContext.addFormConstraintForUpdate(update);
				if (input != null) {
					formContext.removeMember(input);
					AttributeFormFactory.setAttributeUpdate(input, update);
					if (AttributeOperations.allowsSearchRange(aMA) && input instanceof FormGroup) {
						input.setControlProvider(RANGE_CONTROL_PROVIDER);
						if (AttributeOperations.isNumberAttribute(aMA)) {
							Double maximum = AttributeOperations.getMaximum(aMA);
							Double minimum = AttributeOperations.getMinimum(aMA);
							if (maximum != null && minimum != null) {
								double abs = Math.abs(maximum.doubleValue());
								abs = Math.max(Math.abs(minimum.doubleValue()), abs);
								int length = String.valueOf(abs).length();
								updateControlProvider(input, AttributeFormFactory.SEARCH_FROM_FIELDNAME, length);
								updateControlProvider(input, AttributeFormFactory.SEARCH_TO_FIELDNAME, length);
							}
						}
					}
					else if (input instanceof FormField) {
						input.setControlProvider(VALUE_CONTROL_PROVIDER);
					}
				}
				else {
					input = FormFactory.newStringField(fieldName, aMA.getName() + " not supported", true);
				}
				return input;
			}

			private void updateControlProvider(FormMember input, String name, final int length) {
				FormMember member = ((FormGroup) input).getMember(name);
				ControlProvider cp = member.getControlProvider();
				if (cp == null && member instanceof ComplexField) {
					ControlProvider ticp = new ControlProvider() {

						@Override
						public Control createControl(Object model, String style) {
							TextInputControl result = new TextInputControl((FormField) model);
							result.setColumns(length);
							return result;
						}

					};
					member.setControlProvider(ticp);
				}
			}
		});
		removeColumn.setSortable(false);

		return config;
	}

	private class AddRowExecutable implements Command {

		private TableField tableField;

		private final SelectField attField;

		public AddRowExecutable(SelectField anAttField) {
			attField = anAttField;
		}

		public void setTable(TableField aTable) {
			tableField = aTable;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			Object value = attField.getSingleSelection();
			if (value != null) {
				FormTableModel tableModel = (FormTableModel) tableField.getTableModel();
				tableModel.addRowObject(value);
				@SuppressWarnings("unchecked")
				List<Object> newOptions = new ArrayList<Object>(attField.getOptions());
				newOptions.remove(value);
				attField.setOptions(newOptions);
				attField.setAsSingleSelection(null);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	private class RemoveRowExecutable extends CommandField {

		private TableField _tableField;

		private final SelectField attField;

		private AttributeUpdateContainer _updateContainer;

		public RemoveRowExecutable(String name, SelectField anAttField) {
			super(name);
			attField = anAttField;
		}

		public void setTable(TableField aTable, AttributeUpdateContainer updateContainer) {
			_tableField = aTable;
			_updateContainer = updateContainer;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			Object rowObject = get(ROW_OBJECT);
			FormTableModel tableModel = (FormTableModel) _tableField.getTableModel();
			tableModel.removeRowObject(rowObject);
			_updateContainer.removeAttributeUpdate((TLStructuredTypePart) rowObject, null);
			@SuppressWarnings("unchecked")
			List<Object> newOptions = new ArrayList<Object>(attField.getOptions());
			newOptions.add(rowObject);
			attField.setOptions(newOptions);
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	private List<TLStructuredTypePart> filterSupported(List<? extends TLStructuredTypePart> metaAttributes) {
		return FilterUtil.filterInto(new ArrayList<>(), new Filter<TLStructuredTypePart>() {

			@Override
			public boolean accept(TLStructuredTypePart ma) {
				if (DisplayAnnotations.isHidden(ma)) {
					return false;
				}
				
				if (getConfig().getExcludeAttributes().contains(ma.getName())) {
					return false;
				}

//				boolean isCalculated = ma instanceof CalculatedPrimitiveMetaAttribute;
				int type = AttributeOperations.getMetaAttributeType(ma);
				switch (type) {
					case LegacyTypeCodes.TYPE_STRING:
					case LegacyTypeCodes.TYPE_DATE:
					case LegacyTypeCodes.TYPE_LONG:
					case LegacyTypeCodes.TYPE_FLOAT:
					case LegacyTypeCodes.TYPE_BOOLEAN:
					case LegacyTypeCodes.TYPE_COMPLEX:
					case LegacyTypeCodes.TYPE_STRING_SET:
					case LegacyTypeCodes.TYPE_HISTORIC_WRAPPER:
						return true;
					case LegacyTypeCodes.TYPE_LIST:
					case LegacyTypeCodes.TYPE_SINGLE_STRUCTURE:
					case LegacyTypeCodes.TYPE_WRAPPER:
					case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
					case LegacyTypeCodes.TYPE_TYPEDSET:
					case LegacyTypeCodes.TYPE_STRUCTURE:
					case LegacyTypeCodes.TYPE_COLLECTION:
//					case LegacyTypeCodes.TYPE_SINGLE_CLASSIFICATION:
					case LegacyTypeCodes.TYPE_CLASSIFICATION:
//						return !isCalculated;
						return true;
					case LegacyTypeCodes.TYPE_DAP:
					case LegacyTypeCodes.TYPE_DAP_FALLB:
					case LegacyTypeCodes.TYPE_DAP_COLLECTION:
					case LegacyTypeCodes.TYPE_BINARY:
					default:
						return false;
				}
			}
		}, metaAttributes);
	}

	private List<CollectionFilter> getFilters(FormContainer container) {
		AttributeFormContext afc = container.get(ATTRIBUTE_FORM_CONTEXT);
		if (!afc.checkAll()) {
			return Collections.emptyList();
		}
		List<CollectionFilter> result = SearchFilterSupport.INSTANCE.getFilters(afc, false);
		if (_revision != null && !_revision.isCurrent()) {
			List<CollectionFilter> translated = new ArrayList<>(result.size());
			for (CollectionFilter filter : result) {
				if (filter instanceof KBBasedWrapperValuedAttributeFilter) {
					KBBasedWrapperValuedAttributeFilter kbFilter = (KBBasedWrapperValuedAttributeFilter) filter;
					TLStructuredTypePart ma = (TLStructuredTypePart) translateValue(kbFilter.getAttribute());
					List<? extends TLObject> wrappers =
						translateCollection(kbFilter.getWrappers());
					translated.add(new KBBasedWrapperValuedAttributeFilter(ma, wrappers, kbFilter.getNegate(), kbFilter
						.isRelevant(), kbFilter.getAccessPath()));
				}
				else if (filter instanceof NumberAttributeFilter) {
					NumberAttributeFilter nFilter = (NumberAttributeFilter) filter;
					TLStructuredTypePart ma = (TLStructuredTypePart) translateValue(nFilter.getAttribute());
					List<Object> list = getStartEndList(nFilter);
					translated.add(new NumberAttributeFilter(ma, list, nFilter.getNegate(), nFilter.isRelevant(),
						nFilter.getAccessPath()));
				}
				else if (filter instanceof DateAttributeFilter) {
					DateAttributeFilter dFilter = (DateAttributeFilter) filter;
					List<Object> list = getStartEndList(dFilter);
					TLStructuredTypePart ma = (TLStructuredTypePart) translateValue(dFilter.getAttribute());
					translated.add(new DateAttributeFilter(ma, list, dFilter.getNegate(), dFilter.isRelevant(),
						dFilter.getAccessPath()));
				}
				else if (filter instanceof StringAttributeFilter) {
					StringAttributeFilter sFilter = (StringAttributeFilter) filter;
					TLStructuredTypePart ma = (TLStructuredTypePart) translateValue(sFilter.getAttribute());
					translated.add(new StringAttributeFilter(ma, sFilter.getPattern(), sFilter.getNegate(), sFilter
						.isRelevant(),
						sFilter.getAccessPath()));
				}
				else if (filter instanceof BooleanAttributeFilter) {
					BooleanAttributeFilter bFilter = (BooleanAttributeFilter) filter;
					TLStructuredTypePart ma = (TLStructuredTypePart) translateValue(bFilter.getAttribute());
					Boolean val = (Boolean) bFilter.getValueMap().get(BooleanAttributeFilter.KEY_VALUE1);
					translated.add(new BooleanAttributeFilter(ma, val, bFilter.getNegate(), bFilter
						.isRelevant(),
						bFilter.getAccessPath()));
				}
				else {
					translated.add(filter);
				}
			}
			result = translated;
		}
		return result;
	}

	private List<Object> getStartEndList(MinMaxFilter filter) {
		Map<?, ?> valueMap = filter.getValueMap();
		Object start = valueMap.get(MinMaxFilter.KEY_VALUE1);
		Object end = valueMap.get(MinMaxFilter.KEY_VALUE2);
		List<Object> list = CollectionUtil.createList(start, end);
		return list;
	}

	private <T> T translateValue(T object) {
		if (object instanceof Collection) {
			return (T) translateCollection((Collection<?>) object);
		}
		else if (object instanceof TLObject) {
			return (T) WrapperHistoryUtils.getWrapper(_revision, (TLObject) object);
		}
		return object;
	}

	private <T> List<T> translateCollection(Collection<T> object) {
		List<T> result = new ArrayList<>(object.size());
		for (T obj : object) {
			T translated = translateValue(obj);
			if (translated != null) {
				result.add(translated);
			}
		}
		return result;
	}

	@Override
	public Filter<Object> build(FormContainer container) {
		ChartContextObserver observer = ChartContextObserver.getObserver(container.getFormContext());
		_revision = observer.get(InteractiveRevisonTransformer.REVISION_NC);
		_collectionFilters = getFilters(container);
		_filter = new Filter<>() {

			@Override
			public boolean accept(Object anObject) {
				try {
					return !filter(Collections.singletonList(anObject)).isEmpty();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

		};
		return this;
	}

	@Override
	public boolean accept(Object anObject) {
		return _filter.accept(anObject);
	}

	@Override
	public int compareTo(Object arg0) {
		return getClass().getName().compareTo(arg0.getClass().getName());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection filter(Collection aCollection) throws NoSuchAttributeException, AttributeException {
		Collection<?> result = aCollection;
		for (CollectionFilter filter : _collectionFilters) {
			result = filter.filter(result);
			if (result.isEmpty()) {
				break;
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getValueMap() {
		throw new UnsupportedOperationException();
	}

}
