/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.start;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link StartEventSelector} that uses a constant start event specified by process name and
 * optionally event name.
 */
public class ConstantStartEvent extends AbstractConfiguredInstance<ConstantStartEvent.Config>
		implements StartEventSelector {

	private QueryExecutor _function;

	/**
	 * Configuration options for {@link ConstantStartEvent}.
	 */
	public interface Config extends PolymorphicConfiguration<ConstantStartEvent> {

		/**
		 * @see #getProcessName()
		 */
		public static final String PROCESS_NAME = "process-name";

		/**
		 * The name of the process to start.
		 */
		@Mandatory
		@Name(PROCESS_NAME)
		@Options(fun = AllProcessNames.class)
		String getProcessName();

		/**
		 * The name of the start event.
		 * 
		 * <p>
		 * If not given, the process is expected to only have a single start event, which is used.
		 * </p>
		 */
		@Nullable
		@Options(fun = AllStartEvents.class, args = @Ref(PROCESS_NAME))
		String getStartEventName();

		/**
		 * Function retrieving the names of all processes.
		 */
		class AllProcessNames extends Function0<List<String>> {

			private QueryExecutor _lookup;

			/**
			 * Creates a {@link ConstantStartEvent.Config.AllProcessNames}.
			 */
			public AllProcessNames() throws ConfigurationException {
				_lookup = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("", """
						all(`tl.bpe.bpml:Participant`)
						    .map(p -> $p.get(`tl.bpe.bpml:Participant#name`))
						"""));
			}

			@Override
			public List<String> apply() {
				@SuppressWarnings("unchecked")
				List<String> result = (List<String>) _lookup.execute();
				return result;
			}
		}

		/**
		 * All names of start events of the given process.
		 */
		class AllStartEvents extends Function1<List<String>, String> {

			private QueryExecutor _lookup;

			/**
			 * Creates a {@link ConstantStartEvent.Config.AllProcessNames}.
			 */
			public AllStartEvents() throws ConfigurationException {
				_lookup = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("", """
						process -> all(`tl.bpe.bpml:Participant`)
						    .filter(p -> $p.get(`tl.bpe.bpml:Participant#name`) == $process)
						    .singleElement()
						    .get(`tl.bpe.bpml:Participant#process`)
						    .get(`tl.bpe.bpml:Process#nodes`)
						    .filter(n -> $n.instanceOf(`tl.bpe.bpml:StartEvent`))
						    .map(n -> $n.get(`tl.bpe.bpml:Node#name`))
						"""));
			}

			@Override
			public List<String> apply(String process) {
				if (process == null) {
					return Collections.emptyList();
				}
				@SuppressWarnings("unchecked")
				List<String> result = (List<String>) _lookup.execute(process);
				return result;
			}
		}
	}

	/**
	 * Creates a {@link ConstantStartEvent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstantStartEvent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_function = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("", """
				process -> event -> all(`tl.bpe.bpml:Participant`)
				    .filter(p -> $p.get(`tl.bpe.bpml:Participant#name`) == $process)
				    .singleElement()
				    .get(`tl.bpe.bpml:Participant#process`)
				    .get(`tl.bpe.bpml:Process#nodes`)
				    .filter(n -> $n.instanceOf(`tl.bpe.bpml:StartEvent`))
				    .filter(n -> $event == null || $n.get(`tl.bpe.bpml:Node#name`) == $event)
				    .singleElement()
				"""));
	}

	@Override
	public StartEvent getStartEvent(LayoutComponent component) {
		return (StartEvent) _function.execute(getConfig().getProcessName(), getConfig().getStartEventName());
	}

}
