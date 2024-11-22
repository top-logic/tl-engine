/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.bpe.layout.execution.start;

import java.util.Map;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link StartEventSelector} that calculates a dynamic start event using a function with given
 * context.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class DynamicStartEvent extends AbstractConfiguredInstance<DynamicStartEvent.Config>
		implements StartEventSelector {

	private final ChannelLinking _context;

	private final QueryExecutor _function;

	/**
	 * 
	 * Creates a new {@link DynamicStartEvent}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DynamicStartEvent(InstantiationContext context, Config config) {
		super(context, config);

		_context = context.getInstance(config.getContext());
		_function = QueryExecutor.compile(config.getFunction());
	}

	/**
	 * Configuration options for {@link DynamicStartEvent}.
	 */
	public interface Config extends PolymorphicConfiguration<DynamicStartEvent> {

		/**
		 * @see #getFunction()
		 */
		String FUNCTION = "function";

		/**
		 * @see #getContext()
		 */
		String CONTEXT = "context";

		/**
		 * The specification of the context object to operate on.
		 * 
		 * <p>
		 * The evaluation of the configured {@link ModelSpec} is passed as context object to
		 * {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map) the
		 * command execution}.
		 * </p>
		 * 
		 * <p>
		 * If nothing is configured, the default is to use the model of the context component as
		 * target model for the executed command.
		 * </p>
		 * 
		 * @implNote If nothing is configured, the value of the property is <code>null</code>. For
		 *           in-app component configuration, a <code>null</code> default is more
		 *           appropriate, because it lets look the initial configuration GUI less bloated.
		 */
		@Name(CONTEXT)
		ModelSpec getContext();

		/**
		 * 
		 * The {@link Expr} used to calculate the {@link StartEvent}.
		 * 
		 * <p>
		 * The {@link Expr} uses the context as the only argument.
		 * </p>
		 *
		 *
		 * <p>
		 * A {@link StartEvent} is expected as the return.
		 * </p>
		 */
		@Name(FUNCTION)
		Expr getFunction();
	}

	/**
	 * Returns the {@link StartEvent} calculated by the given {@link Expr}.
	 */
	@Override
	public StartEvent getStartEvent(LayoutComponent component) {
		return (StartEvent) _function.execute(ChannelLinking.eval(component, _context));
	}

}
