/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.office.ppt.POIPowerpointUtil;
import com.top_logic.base.office.ppt.SlideReplacement;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.charsize.FontCharSizeMap;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.component.ChartDescription;
import com.top_logic.reporting.flex.chart.component.export.SlideReplacerBuilder.AbstractTextCutter;
import com.top_logic.reporting.flex.chart.component.export.SlideReplacerBuilder.DefaultHeaderTranslator;
import com.top_logic.reporting.flex.chart.component.export.SlideReplacerBuilder.TextCutter;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.MethodCriterion;
import com.top_logic.util.error.TopLogicException;

/**
 * Implementation of {@link ExportManager} for configured charts.
 */
public class ConfiguredChartExportManager extends AbstractExportManager<ConfiguredChartExportManager.Config> {

	/** Column key for generic columns. */
	public static final String GENERIC_COLUMNS = "_generic";

	/**
	 * Enum describing which detail-object should be exported additionally to a chart
	 */
	public enum ExportObjects {
		/**
		 * <code>NONE</code>: No detail-table is exported
		 */
		NONE,
		/**
		 * <code>ROOT</code>: The initial objects (from the {@link ChartTree}-root-node) are
		 * exported
		 */
		ROOT,
		/**
		 * <code>LEAF</code>: The objects from the {@link ChartTree}-leaf-nodes are exported.
		 * May be the same as root-objects but may as well be translated to other objects or
		 * just a sub-set of the root-objects.
		 */
		LEAF
	}

	/**
	 * Config-interface for {@link ConfiguredChartExportManager}.
	 */
	public interface Config extends AbstractExportManager.Config {

		@Override
		@ClassDefault(ConfiguredChartExportManager.class)
		public Class<? extends ConfiguredChartExportManager> getImplementationClass();

		/**
		 * which objects should be exported in detail-tables, see {@link ExportObjects}
		 */
		@FormattedDefault("NONE")
		public ConfiguredChartExportManager.ExportObjects getExportObjects();

		/**
		 * See {@link #getExportObjects()}
		 */
		public void setExportObjects(ConfiguredChartExportManager.ExportObjects objects);

		/**
		 * Whether the chart can be rendered as square chart.
		 * 
		 * @see ChartUtil#writeSquareChart(JFreeChart, int, org.jfree.chart.ChartRenderingInfo)
		 * 
		 * @return true for square chart, false for normal chart (default)
		 */
		@BooleanDefault(false)
		public boolean getExportSquare();

		@Override
		@StringDefault("flex/chart/default.pptx")
		public String getTemplatePath();

		/**
		 * Gets the template file for additional values (table), which will be included in the
		 * {@link #getTemplatePath()} template.
		 * 
		 * <p>
		 * {@link #getAdditionalValuesTemplatePath()} and
		 * {@link #getAdditionalValuesTemplateProvider()} must not both be configured.
		 * </p>
		 * 
		 * @see #getAdditionalValuesTemplateProvider()
		 */
		public String getAdditionalValuesTemplatePath();

		/**
		 * Gets the provider for template file for additional values (table), which will be included
		 * in the {@link #getTemplatePath()} template.
		 * 
		 * <p>
		 * The provider returns the template depending on the export columns.
		 * </p>
		 * 
		 * <p>
		 * {@link #getAdditionalValuesTemplatePath()} and
		 * {@link #getAdditionalValuesTemplateProvider()} must not both be configured.
		 * </p>
		 * 
		 * @see #getAdditionalValuesTemplatePath()
		 */
		PolymorphicConfiguration<? extends AdditionalValuesTemplateProvider> getAdditionalValuesTemplateProvider();

		/**
		 * Gets the columns which shall be exported.
		 * 
		 * There are special interpreted columns possible:<br/>
		 * "_index" - which will export an ascending number in each row<br/>
		 * "_generic" - which will include generic calculated columns of the chart<br/>
		 */
		@FormattedDefault("_index,name,_generic")
		@Format(CommaSeparatedStrings.class)
		public List<String> getColumns();

		/**
		 * Gets the accessor to use in export.
		 */
		@NonNullable
		@InstanceFormat
		@InstanceDefault(WrapperAccessor.class)
		public Accessor<? extends Object> getAccessor();

		/**
		 * Gets the label provider to use in export.
		 */
		@NonNullable
		@InstanceFormat
		@InstanceDefault(MetaLabelProvider.class)
		public LabelProvider getLabelProvider();

		/**
		 * Gets the factory to create text cutter to use in export.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultFlexChartTemplateTextCutterFactory.class)
		TextCutterFactory getTextCutterFactory();

		/**
		 * Gets the comparator to use to sort elements in export. May be <code>null</code> to don't
		 * sort objects.
		 */
		@ItemDefault
		@ImplementationClassDefault(ComparableComparator.class)
		public PolymorphicConfiguration<? extends Comparator<?>> getComparator();

		/**
		 * May be <code>null</code> to don't export additional values.
		 * 
		 * @see AdditionalChartValueProvider
		 */
		@InstanceFormat
		@InstanceDefault(DefaultAdditionalChartValueProvider.class)
		AdditionalChartValueProvider getAdditionalChartValueProvider();

		/**
		 * @see SlideReplacerBuilder
		 */
		@NonNullable
		@ItemDefault
		SlideReplacerBuilder.Config getSlideReplacerBuilder();
	}

	/**
	 * Config-Constructor for {@link ConfiguredChartExportManager}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ConfiguredChartExportManager(InstantiationContext context, Config config) {
		super(context, config);
		PolymorphicConfiguration<? extends AdditionalValuesTemplateProvider> provider =
			config.getAdditionalValuesTemplateProvider();
		String path = config.getAdditionalValuesTemplatePath();
		if (!path.isEmpty() && provider != null) {
			context.error("Can not set both: additional value template path '" + path
				+ "' and additional value template provider '" + provider + "'.");
		}
	}

	private Dimension getExportDimension() {
		int width = 900;
		int height = 560;
		return new Dimension(width, height);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void exportAdditionalValues(LayoutComponent caller, Map<String, Object> valueMap, Map arguments) {
		AbstractChartComponent component = (AbstractChartComponent) caller;

		String imageId = "chart";
		JFreeChart chart = component.getChart();

		if (chart != null) {
			// this.getPostHandler().handle(this, chartContext, chart,
			// false);
			ChartUtil.configurePlotForExport(chart.getPlot(), 12);
			String pathToImage;
			try {
				Dimension dimension = getExportDimension();
				// The chart can be rendered as square chart. That means
				// that the chart is square but
				// without the legend. If a chart should be rendered as
				// square chart the width is used
				// as size.
				if (getConfig().getExportSquare()) {
					pathToImage = ChartUtil.writeSquareChart(chart, dimension.width, null);
				} else {
					pathToImage = ChartUtil.writeChartAsPng(chart, dimension, null);
				}

				BinaryData imageFile = FileManager.getInstance().getDataOrNull(pathToImage);
				if (imageFile != null) {
					valueMap.put(POIPowerpointUtil.PREFIX_PICTURE + imageId.toUpperCase(), imageFile);
				}
				AdditionalChartValueProvider additionalChartValueProvider = getConfig().getAdditionalChartValueProvider();
				if (additionalChartValueProvider != null) {
					additionalChartValueProvider.exportAdditionalValues(valueMap, component, getConfig());
				}
			} catch (IOException ioe) {
				throw new TopLogicException(this.getClass(),
					"Couldn't create the chart for detailed information see the exception message.", ioe);
			}

		}

	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(String handler, String path, ResKey label, ConfiguredChartExportManager.ExportObjects objects) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		if (!StringServices.isEmpty(handler)) {
			item.setExportHandler(handler);
		}
		if (!StringServices.isEmpty(path)) {
			item.setTemplatePath(path);
		}
		if (label != null) {
			item.setDownloadKey(label);
		}
		if (objects != null) {
			item.setExportObjects(objects);
		}
		return item;
	}

	/**
	 * Factory method to create an initialized {@link ConfiguredChartExportManager}.
	 * 
	 * @return a new ConfiguredChartExportManager.
	 */
	public static ConfiguredChartExportManager instance(String handler, String path, ResKey label, ConfiguredChartExportManager.ExportObjects objects) {
		return (ConfiguredChartExportManager) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(item(handler, path, label, objects));
	}

	/**
	 * Provider of values, which shall be part of the export, additional to the chart.
	 * 
	 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
	 */
	public interface AdditionalChartValueProvider {

		/**
		 * @param valueMap
		 *        - the {@link Map}, to which the export result shall be added to
		 * @param component
		 *        - the component, which shall be exported
		 * @param configuration
		 *        - Configuration of the export
		 */
		void exportAdditionalValues(Map<String, Object> valueMap, AbstractChartComponent component,
				Config configuration);

	}
	
	/**
	 * Default implementation of {@link AdditionalChartValueProvider}.
	 * 
	 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
	 */
	public static class DefaultAdditionalChartValueProvider implements AdditionalChartValueProvider {

		/** I18N suffix for header columns in values table. */
		public static final String COLUMN_SUFFIX = "column";

		/**
		 * Static instance of {@link DefaultAdditionalChartValueProvider}
		 */
		public static final AdditionalChartValueProvider INSTANCE = new DefaultAdditionalChartValueProvider();

		@Override
		public void exportAdditionalValues(Map<String, Object> valueMap, AbstractChartComponent component,
				Config configuration) {
			ConfiguredChartExportManager.ExportObjects eo = configuration.getExportObjects();

			if (eo != ExportObjects.NONE) {
				ChartTree tree = component.getChartTree();
				final ChartDescription description = new ChartDescription(tree);
				List<String> columns = new ArrayList<>();
				List<String> configuredColumns = configuration.getColumns();
				for (String column : configuredColumns) {
					if (column.equals(GENERIC_COLUMNS)) {
						columns.addAll(description.getColumns());
					}
					else {
						columns.add(column);
					}
				}
				if (columns.isEmpty()) return;

				AdditionalValuesTemplateProvider provider = getAdditionalValuesTemplateProvider(configuration);
				String template = provider.getTemplate(columns);
				if (!exists(template)) {
					Logger.error("Table template '" + template + "' doesn't exist.", DefaultAdditionalChartValueProvider.class);
				}
				else {
					List<Object> objects = getExportObjects(tree, eo);
					PolymorphicConfiguration<? extends Comparator<? extends Object>> comparatorConfig =
						configuration.getComparator();
					if (comparatorConfig != null) {
						@SuppressWarnings("unchecked")
						Comparator<Object> comparator =
							(Comparator<Object>) TypedConfigUtil.createInstance(comparatorConfig);
						Collections.sort(objects, comparator);
					}

					SlideReplacerBuilder.Config builderConfig = configuration.getSlideReplacerBuilder();
					SlideReplacerBuilder builder = SlideReplacerBuilder.instance(builderConfig, template, columns);
					builder.initContext(valueMap);
					final Accessor accessor = configuration.getAccessor();
					builder.setAccessor(new ReadOnlyAccessor<>() {
						@Override
						public Object getValue(Object object, String property) {
							MethodCriterion criterion = description.getMethodCriterion(property);
							if (criterion != null) {
								return criterion.getFunction().calculate(null, Collections.singletonList(object));
							}
							return accessor.getValue(object, property);
						}
					});
					builder.setLabelProvider(configuration.getLabelProvider());
					builder.setHeaderTranslator(new DefaultHeaderTranslator(component.getResPrefix().append(COLUMN_SUFFIX)) {
						@Override
						public String translate(String columnName) {
							TLStructuredTypePart ma = description.getAttributes().get(columnName);
							if (ma != null) {
								return MetaResourceProvider.INSTANCE.getLabel(ma);
							}
							return super.translate(columnName);
						}
					});


					builder.setTextCutter(configuration.getTextCutterFactory().createTextCutter(columns));

					SlideReplacement replacement = builder.create(objects);
					valueMap.put("ADDSLIDES_RISK_ITEM_TABLE", replacement);
				}
			}
		}

		private AdditionalValuesTemplateProvider getAdditionalValuesTemplateProvider(Config configuration) {
			String additionalValuesTemplatePath = configuration.getAdditionalValuesTemplatePath();
			if (!additionalValuesTemplatePath.isEmpty()) {
				return new ConstantAdditionalValuesTemplateProvider(additionalValuesTemplatePath);
			}
			PolymorphicConfiguration<? extends AdditionalValuesTemplateProvider> provider = configuration.getAdditionalValuesTemplateProvider();
			if (provider == null) {
				provider = TypedConfiguration.newConfigItem(DefaultAdditionalValuesTemplatePathProvider.Config.class);
			}
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(provider);
		}

		/**
		 * Whether the {@link SlideReplacerBuilder#INDEX_COLUMN} is the first of the given columns.
		 * 
		 * @param columns
		 *        The columns to check. Must not be <code>null</code>, but may be empty.
		 */
		public static boolean indexColumnFirst(List<String> columns) {
			if (columns.isEmpty()) {
				return false;
			}
			return SlideReplacerBuilder.INDEX_COLUMN.equals(columns.get(0));
		}
		
		private List<Object> getExportObjects(ChartTree tree, ConfiguredChartExportManager.ExportObjects eo) {

			switch (eo) {
				case LEAF:
					return tree.getLeafObjects();
				case ROOT:
					return tree.getRootObjects();
				default:
					throw new UnsupportedOperationException();
			}
		}

		private boolean exists(String name) {
			try {
				DataAccessProxy dap = new DataAccessProxy("webinf://reportTemplates", "ppt");
				dap = dap.getChildProxy(name);
				return dap.exists();
			} catch (Exception ex) {
				// ignore
			}
			return false;
		}

	}

	/**
	 * {@link TextCutter} for the default flex chart templates (table*.pptx).
	 */
	public static class DefaultFlexChartTemplateTextCutter extends AbstractTextCutter {

		/** {@link CharSizeMap} for "Calibri" font. */
		public static final CharSizeMap CALIBRI = new FontCharSizeMap("Calibri", Font.PLAIN, 12);

		private final int colCount;

		private final boolean indexColumnFirst;

		/**
		 * Creates a new {@link DefaultFlexChartTemplateTextCutter}.
		 */
		public DefaultFlexChartTemplateTextCutter(int colCount, boolean indexColumnFirst) {
			this.colCount = colCount;
			this.indexColumnFirst = indexColumnFirst;
		}

		@Override
		public int getMaxChars(int column) {
			if (indexColumnFirst) {
				if (column == 0) {
					return 4;
				}
				switch (colCount) {
					case 2:
						return 57;
					case 3:
						return 51;
					case 4:
						return 33;
					default:
						return 24;
				}
			}
			else {
				switch (colCount) {
					case 1:
						return 56;
					case 2:
						return 55;
					case 3:
						return 35;
					case 4:
						return 26;
					default:
						return 20;
				}
			}
		}

		@Override
		public CharSizeMap getCharSizeMap() {
			return CALIBRI;
		}

	}

}
