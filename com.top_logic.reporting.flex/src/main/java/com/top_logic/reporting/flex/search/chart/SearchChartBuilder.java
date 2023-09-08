/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.SeriesDataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.aggregation.AggregationFunction;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder.StandardLabelProvider;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.BarChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.DynamicStackedBarChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.pie.PieChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.time.TimeSeriesChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.xy.SearchXYBarChartBuilder;
import com.top_logic.reporting.flex.chart.config.color.ColorProvider;
import com.top_logic.reporting.flex.chart.config.color.ModifiableColorContext;
import com.top_logic.reporting.flex.chart.config.color.OrderedColorProvider;
import com.top_logic.reporting.flex.chart.config.color.OrderedColorProvider.OrderedColorContext;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.IntervalXYDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver.ChartListener;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver.DimensionListener;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.partition.AbstractAttributeBasedPartition;
import com.top_logic.reporting.flex.chart.config.partition.DateAttributePartition;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.util.KeyCompare;
import com.top_logic.reporting.flex.chart.config.util.ToStringText.NotSetText;
import com.top_logic.util.Resources;

/**
 * {@link InteractiveBuilder} of an {@link JFreeChartBuilder} for the search UI.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class SearchChartBuilder implements JFreeChartBuilder<Dataset>,
		InteractiveBuilder<JFreeChartBuilder<?>, ChartContextObserver> {

	public static final String CONFIG_GROUP = "configGroup";

	public static final String CHART_TYPE_FIELD = "chartType";

	public static final String COLORS_GROUP = "colors";

	public static final String VIEW_GROUP = "view";

	private static final String COLOR_COLUMN = "color";

	private static final String OBJECT_COLUMN = "object";

	private static final String COLORS_TABLE = "table";

	private static final String COLORS_TABLE_GROUP = "inner";

	private final class ColorFieldProvider extends AbstractFieldProvider {
		ModifiableColorContext _colorContext;

		public ColorFieldProvider(ModifiableColorContext colorProvider) {
			_colorContext = colorProvider;
		}

		public ModifiableColorContext getColorContext() {
			return _colorContext;
		}

		@Override
		public FormMember createField(final Object aModel, Accessor anAccessor, String aProperty) {
			String fieldName = getFieldName(aModel, anAccessor, aProperty);
			Color init = getInitialColor(aModel);
			ComplexField field = FormFactory.newComplexField(fieldName, ColorFormat.INSTANCE, init, false);
			field.addValueListener((sender, oldValue, newValue) -> _colorContext.setColor(aModel, (Color) newValue));
			return field;
		}

		private Color getInitialColor(Object aModel) {
			return (Color) _colorContext.getColor(aModel);
		}
	}

	/**
	 * Config-interface for {@link SearchChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<Dataset> {

		@Override
		@ClassDefault(SearchChartBuilder.class)
		public Class<JFreeChartBuilder<Dataset>> getImplementationClass();

	}

	private final Config _config;

	JFreeChartBuilder<?> _builder;

	/**
	 * Config-Constructor for {@link SearchChartBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SearchChartBuilder(InstantiationContext context, Config config) {
		_config = config;
		_builder = null;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * @param builder
	 *        setter for the {@link JFreeChartBuilder} to use when generating a chart
	 */
	public void setChartBuilder(JFreeChartBuilder<?> builder) {
		_builder = builder;
	}

	@Override
	public DatasetBuilder<? extends Dataset> getDatasetBuilder() {
		if (_builder == null) {
			return null;
		}
		return _builder.getDatasetBuilder();
	}

	@Override
	public Class<? extends Dataset> datasetType() {
		return _builder.datasetType();
	}

	@Override
	public JFreeChart createChart(ChartContext context, ChartData chartData) {
		return _builder.createChart(context, chartData);
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		FormGroup group = new FormGroup(VIEW_GROUP, ResPrefix.legacyString(getClass().getSimpleName()));
		container.addMember(group);

		addTypeField(arg, group);
		addColorGroup(group, arg);
		addTypeConfigGroup(group, arg);

		template(group, div(
			verticalBox(
				fieldBox(CHART_TYPE_FIELD),
				contentBox(member(CONFIG_GROUP)),
				contentBox(member(COLORS_GROUP)))));
	}

	private void addTypeConfigGroup(FormGroup container, ChartContextObserver arg) {
		Object model = arg.getModel();
		final Map<String, List<TLStructuredTypePart>> attributes = findAttributes(model);
		LabelProvider provider = new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				List<TLStructuredTypePart> set = attributes.get(object);
				List<String> names = names(set);
				return StringServices.join(names, ", ");
			}

			private List<String> names(List<TLStructuredTypePart> mas) {
				Set<String> set = new HashSet<>();
				for (TLStructuredTypePart metaAttribute : mas) {
					set.add(MetaLabelProvider.INSTANCE.getLabel(metaAttribute));
				}
				List<String> result = new ArrayList<>(set);
				Collections.sort(result);
				return result;
			}
		};
		List<String> options = new ArrayList<>(attributes.keySet());
		final FormGroup group = new FormGroup(CONFIG_GROUP, ResPrefix.legacyString(SearchXYBarChartBuilder.class.getSimpleName()));
		SelectField member = (SelectField) container.getField(CHART_TYPE_FIELD);
		SearchXYBarChartBuilder builder = findBuilder(member.getOptions());
		group.setVisible(member.getSingleSelection() == builder);

		member.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				Object first = CollectionUtil.getFirst(newValue);
				group.setVisible(first == builder);
			}
		});
		IntervalXYDatasetBuilder datasetBuilder = (IntervalXYDatasetBuilder) builder.getDatasetBuilder();
		IntervalXYDatasetBuilder.Config config = datasetBuilder.getConfig();

		SelectField startField = FormFactory.newSelectField("start", options, false, null, false);
		startField.setOptionLabelProvider(provider);
		if (attributes.containsKey(config.getStartAttribute())) {
			startField.setAsSingleSelection(config.getStartAttribute());
		}
		startField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				TypedConfigUtil.setProperty(config, "start-attribute", CollectionUtil.getFirst(newValue));
			}
		});
		SelectField endField = FormFactory.newSelectField("end", options, false, null, false);
		endField.setOptionLabelProvider(provider);
		if (attributes.containsKey(config.getEndAttribute())) {
			endField.setAsSingleSelection(config.getEndAttribute());
		}
		endField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				TypedConfigUtil.setProperty(config, "end-attribute", CollectionUtil.getFirst(newValue));
			}
		});

		group.addMember(startField);
		group.addMember(endField);

		container.addMember(group);

		template(group, div(fieldBox("start"), fieldBox("end")));
	}

	private Map<String, List<TLStructuredTypePart>> findAttributes(Object model) {
		Map<String, List<TLStructuredTypePart>> result = new HashMap<>();
		if (model instanceof AttributedSearchResultSet) {
			Set<? extends TLClass> types = ((AttributedSearchResultSet) model).getTypes();
			for (TLClass me : types) {
				List<TLStructuredTypePart> mas =
					MetaElementUtil.getMetaAttributes(me, LegacyTypeCodes.TYPE_DATE, true, false);
				for (TLStructuredTypePart ma : mas) {
					String name = ma.getName();
					List<TLStructuredTypePart> set = result.get(name);
					if (set == null) {
						set = new ArrayList<>();
						result.put(name, set);
					}
					set.add(ma);
				}
			}
			int size = types.size();
			HashSet<String> keys = new HashSet<>(result.keySet());
			for (String key : keys) {
				List<TLStructuredTypePart> set = result.get(key);
				if (set.size() != size) {
					result.remove(key);
				}
			}
		}
		return result;
	}

	private SearchXYBarChartBuilder findBuilder(List<?> options) {
		for (Object object : options) {
			if (object instanceof SearchXYBarChartBuilder) {
				return (SearchXYBarChartBuilder) object;
			}
		}
		return null;
	}

	private void addTypeField(ChartContextObserver arg, FormGroup group) {
		List<JFreeChartBuilder<?>> options = getBuilderOptions();
		List<?> initialFieldValue = _builder == null ? null : Collections.singletonList(_builder);
		SelectField field =
			FormFactory.newSelectField(CHART_TYPE_FIELD, options, false, initialFieldValue, false, false, null);
		field.setOptionLabelProvider(object -> ((AbstractJFreeChartBuilder<?>) object).getLabel());
		group.addMember(field);

		field.addValueListener(
			(sender, oldValue, newValue) -> _builder = (JFreeChartBuilder<?>) CollectionUtil.getFirst(newValue));
		Observer observer = new Observer(field);
		arg.addDimensionListener(observer);
	}

	private List<JFreeChartBuilder<?>> getBuilderOptions() {
		List<JFreeChartBuilder<?>> options = new ArrayList<>();
		addOption(options, BarChartBuilder.newInstance());
		addOption(options, PieChartBuilder.newInstance());
		addOption(options, TimeSeriesChartBuilder.instance());
		addOption(options, SearchXYBarChartBuilder.newInstance());
		addOption(options, DynamicStackedBarChartBuilder.instance());
		return options;
	}

	private void addOption(List<JFreeChartBuilder<?>> options, JFreeChartBuilder<?> builder) {
		if ((_builder != null) && builder.getClass().equals(_builder.getClass())) {
			builder = _builder;
		}
		options.add(builder);
	}

	private void addColorGroup(final FormContainer container, ChartContextObserver arg) {
		final FormGroup group =
			new FormGroup(COLORS_GROUP, ResPrefix.legacyString(getConfig().getConfigurationInterface().getName()));
		List<String> cols = CollectionUtil.createList(OBJECT_COLUMN, COLOR_COLUMN);

		TableConfiguration table = TableConfigurationFactory.table();
		table.setResPrefix(I18NConstants.COLOR_TABLE);
		table.setColumnCustomization(ColumnCustomization.NONE);
		table.setDefaultFilterProvider(null);
		table.setFilterProvider(null);
		table.setMultiSort(Enabled.never);
		table.setShowFooter(false);
		table.setDefaultSortOrder(Collections.singletonList(SortConfigFactory.ascending(OBJECT_COLUMN)));

		ColumnConfiguration objColumn = table.declareColumn(OBJECT_COLUMN);
		objColumn.setAccessor(IdentityAccessor.INSTANCE);
		objColumn.setLabelProvider(new StandardLabelProvider());

		// Note: When allowing to sort the table, storing the selected colors no longer works,
		// because only the order of colors is stored. It is made sure that the order of rows in the
		// table is reasonable (consistent with the legend of the diagram). Therefore reordering the
		// color table is not required.
		objColumn.setComparator(null);

		objColumn.setFilterProvider(null);

		ModifiableColorContext colorContext = (ModifiableColorContext) getColorProvider().createColorContext();
		
		ColumnConfiguration colorColumn = table.declareColumn(COLOR_COLUMN);
		final ColorFieldProvider colorFieldProvider = new ColorFieldProvider(colorContext);
		colorColumn.setFieldProvider(colorFieldProvider);
		colorColumn.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
		colorColumn.setComparator(null);
		colorColumn.setFilterProvider(null);

		final ObjectTableModel tableModel = new ObjectTableModel(cols, table, Collections.EMPTY_LIST);
		final FormTableModel formTable =
			new FormTableModel(tableModel, new FormGroup(COLORS_TABLE_GROUP,
				ResPrefix.legacyString(getConfig().getConfigurationInterface().getName())));
		TableField tableField = FormFactory.newTableField(COLORS_TABLE, formTable);
		arg.addDimensionListener(new DimensionListener() {

			@Override
			public void onDimensionChange(List<PartitionFunction.Config> partitions,
					List<AggregationFunction.Config> aggregations) {
				colorFieldProvider.getColorContext().reset();
				List<?> colored = getColoredObjects(partitions, aggregations);
				formTable.setRowObjects(colored);
			}

			private List<?> getColoredObjects(List<PartitionFunction.Config> partitions,
					List<AggregationFunction.Config> aggregations) {
				if (aggregations.size() > 1) {
					return aggregations;
				}
				else {
					Object part = CollectionUtil.getLast(partitions);
					if (part instanceof AbstractAttributeBasedPartition.Config) {
						TLStructuredTypePart ma = ((AbstractAttributeBasedPartition.Config) part).getMetaAttribute().get();
						if (ma.getType() instanceof FastList) {
							FastList list = (FastList) ma.getType();
							{
								List<Object> result = new ArrayList<>(list.elements());
								result.add(new NotSetText(ma));
								return result;
							}
						}
					}
				}
				return Collections.emptyList();
			}
		});
		arg.addChartListener(new ChartListener() {

			@Override
			public void onChartDataCreated(Dataset dataSet, ChartTree chartTree) {
				Set<Comparable<?>> keys;
				if (dataSet instanceof SeriesDataset) {
					keys = new HashSet<>();
					SeriesDataset series = (SeriesDataset) dataSet;
					int count = series.getSeriesCount();
					for (int n = 0; n < count; n++) {
						Comparable<?> key = series.getSeriesKey(n);
						if (key instanceof UniqueName) {
							key = ((UniqueName) key).getKey();
						}
						keys.add(key);
					}
				} else {
					keys = getKeysInLevel(0, chartTree);
				}

				Collection<?> allRows = formTable.getAllRows();
				if (!allRows.containsAll(keys)) {
					colorFieldProvider.getColorContext().reset();

					List<Comparable<?>> colored = new ArrayList<>(keys);
					Collections.sort((List) colored, KeyCompare.INSTANCE);
					formTable.setRowObjects(colored);
				}
			}

			private Set<Comparable<?>> getKeysInLevel(int colorLevel, ChartTree tree) {
				Set<Comparable<?>> result = new HashSet<>();
				collectKeysInLevel(colorLevel, tree.getRoot(), result);
				return result;
			}

			private void collectKeysInLevel(int colorLevel, ChartNode node, Set<Comparable<?>> result) {
				if (node.isLeaf()) {
					result.add(node.getKey());
				}
				else {
					for (ChartNode child : node.getChildren()) {
						collectKeysInLevel(colorLevel, child, result);
					}
				}
			}

		});

		group.addMember(tableField);
		container.addMember(group);
	}

	private OrderedColorProvider getColorProvider() {
		ColorProvider storedProvider = getStoredColorProvider();
		if (storedProvider instanceof OrderedColorProvider) {
			return (OrderedColorProvider) storedProvider;
		}
		return OrderedColorProvider.newInstance(Collections.<Paint> emptyList(), storedProvider);
	}

	private ColorProvider getStoredColorProvider() {
		if (_builder == null) {
			return null;
		}
		return _builder.getConfig().getColorProvider();
	}

	@Override
	public JFreeChartBuilder<?> build(FormContainer container) {
		if (_builder == null) {
			return null;
		}
		FormContainer group = container.getContainer(VIEW_GROUP);
		FormContainer colorsGroup = group.getContainer(COLORS_GROUP);
		TableField table = (TableField) colorsGroup.getField(COLORS_TABLE);
		TableModel tableModel = table.getTableModel();
		OrderedColorContext colorContext =
			(OrderedColorContext) ((ColorFieldProvider) tableModel.getTableConfiguration()
				.getDeclaredColumn(COLOR_COLUMN).getFieldProvider())
				.getColorContext();

		((JFreeChartBuilder.Config<?>) _builder.getConfig()).setColorProvider(colorContext);
		return _builder;
	}

	private class Observer implements DimensionListener, Constraint {

		private final SelectField _field;

		private List<PartitionFunction.Config> _partitions;

		private List<AggregationFunction.Config> _aggregations;

		public Observer(SelectField field) {
			_field = field;
			_field.addConstraint(this);
			_partitions = Collections.emptyList();
			_aggregations = Collections.emptyList();
		}

		@Override
		public void onDimensionChange(List<PartitionFunction.Config> partitions,
				List<AggregationFunction.Config> aggregations) {
			_partitions = partitions;
			_aggregations = aggregations;
			_field.check();
		}

		private int calculateSize() {
			int size = _partitions.size();
			if (_aggregations.size() > 1) {
				size++;
			}
			return size;
		}

		@Override
		public boolean check(Object value) throws CheckException {
			AbstractJFreeChartBuilder<?> builder = (AbstractJFreeChartBuilder<?>) CollectionUtil.getFirst(value);
			if (builder == null) {
				return true;
			}
			int size = calculateSize();
			if (size > builder.getMaxDimensions()) {
				throw new CheckException(getMessage(builder,
					I18NConstants.EXCEEDS_MAX_DIMENSIONS__MIN_MIN1_MAX_MAX1_PARTITIONS_AGGREGATIONS));
			}
			else if (size < builder.getMinDimensions()) {
				throw new CheckException(getMessage(builder,
					I18NConstants.UNDERCUTS_MIN_DIMENSIONS__MIN_MIN1_MAX_MAX1_PARTITIONS_AGGREGATIONS));
			}
			if (builder instanceof TimeSeriesChartBuilder) {
				Object first = CollectionUtil.getFirst(_partitions);
				if (!(first instanceof DateAttributePartition.Config)) {
					throw new CheckException(Resources.getInstance().getString(
						I18NConstants.DATE_PARTITION_FIRST));
				}
			}
			return true;
		}

		private String getMessage(AbstractJFreeChartBuilder<?> builder, ResKey messageKey) {
			Resources resources = Resources.getInstance();
			return resources.getMessage(messageKey,
				builder.getMinDimensions(),
				builder.getMinDimensions() - 1,
				builder.getMaxDimensions(),
				builder.getMaxDimensions() - 1,
				_partitions.size(),
				_aggregations.size(),
				builder.getLabel());
		}
	}

}
