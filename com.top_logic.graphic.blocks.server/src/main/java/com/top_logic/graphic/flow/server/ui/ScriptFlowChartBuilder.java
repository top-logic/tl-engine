/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.graphic.flow.callback.DiagramHandler;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ModelReferenceChecker;
import com.top_logic.util.model.ModelService;

/**
 * {@link FlowChartBuilder} that can be implemented by TL-Script.
 */
@InApp
public class ScriptFlowChartBuilder extends AbstractConfiguredInstance<ScriptFlowChartBuilder.Config<?>>
		implements FlowChartBuilder {

	/**
	 * Configuration options for {@link ScriptFlowChartBuilder}.
	 */
	public interface Config<I extends ScriptFlowChartBuilder> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getHandlers()
		 */
		String HANDLERS = "handlers";

		/**
		 * Predicate that decides, whether a given model can be displayed as flow chart.
		 */
		@NonNullable
		@FormattedDefault("true")
		Expr getSupportsModel();

		/**
		 * Function creating a flow chart form a given model.
		 */
		@NonNullable
		@FormattedDefault("model -> flowChart()")
		@PropertyEditor(PlainEditor.class)
		@Constraint(value = SyntaxCheck.class, args = @Ref(HANDLERS))
		Expr getCreateChart();

		/**
		 * Specification of interactions with the diagram contents.
		 * 
		 * <p>
		 * The handlers defined here can be bound to diagram elements in the
		 * {@link Config#getCreateChart() create chart script} by referencing them as implicitly
		 * defined variables.
		 * </p>
		 */
		@Name(HANDLERS)
		@Key(HandlerDefinition.NAME_ATTRIBUTE)
		Map<String, HandlerDefinition<? extends DiagramHandler>> getHandlers();

		/**
		 * Common super-interface for configurations of a {@link DiagramHandler}.
		 */
		@Abstract
		interface HandlerDefinition<T extends DiagramHandler>
				extends PolymorphicConfiguration<T>, NamedConfigMandatory {
			/**
			 * Unique name of the defined handler.
			 * 
			 * <p>
			 * The defined handler is available to the {@link Config#getCreateChart() create chart
			 * script} as an implicitly defined variable with that name. When defining a handler
			 * with name <code>onClick</code>, this handler is available to the script as variable
			 * <code>$onClick</code>.
			 * </p>
			 */
			@Override
			String getName();
		}

		/**
		 * Syntax check of {@link Config#getCreateChart()} with implicitly defined parameters.
		 */
		class SyntaxCheck
				extends GenericValueDependency<Expr, Map<String, HandlerDefinition<? extends DiagramHandler>>> {
			/**
			 * Creates a {@link SyntaxCheck}.
			 */
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public SyntaxCheck() {
				super((Class) Expr.class, (Class) Map.class);
			}

			@Override
			protected void checkValue(PropertyModel<Expr> propertyA,
					PropertyModel<Map<String, HandlerDefinition<? extends DiagramHandler>>> propertyB) {
				Expr expr = propertyA.getValue();
				if (expr == null) {
					return;
				}

				// Add implicit parameters.
				Set<String> handlerNames = propertyB.getValue().keySet();
				for (String name : handlerNames) {
					expr = Define.create(name, expr);
				}

				try {
					ModelReferenceChecker.checkModelElements(expr);
				} catch (I18NRuntimeException ex) {
					propertyA.setProblemDescription(ex.getErrorKey());
				}
			}

			@Override
			public boolean isChecked(int index) {
				return index == 0;
			}
		}
	}

	private final QueryExecutor _supportModel;

	private final QueryExecutor _createChart;

	private final Map<String, DiagramHandler> _handlers;

	/**
	 * Creates a {@link ScriptFlowChartBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFlowChartBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
		_handlers = TypedConfiguration.getInstanceMap(context, config.getHandlers());
		_supportModel = QueryExecutor.compile(config.getSupportsModel());

		SearchExpression createChart =
			SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), config.getCreateChart());

		// Inject implicit variables.
		for (Entry<String, DiagramHandler> handler : _handlers.entrySet()) {
			createChart = call(lambda(handler.getKey(), createChart), literal(handler.getValue()));
		}

		_createChart = QueryExecutor.compile(createChart);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return SearchExpression.asBoolean(_supportModel.execute(aModel));
	}

	@Override
	public Diagram getModel(Object businessModel, LayoutComponent aComponent) {
		return (Diagram) _createChart.execute(businessModel);
	}

}
