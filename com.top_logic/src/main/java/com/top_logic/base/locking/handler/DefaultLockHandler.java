/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link LockHandler} dispatching to the application-wide {@link LockService}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Application-wide lock definition")
public class DefaultLockHandler<C extends DefaultLockHandler.Config<?>> extends AbstractConfiguredLockHandler<C> {

	private final LockService _service = LockService.getInstance();

	/**
	 * Configuration options for {@link DefaultLockHandler}.
	 */
	public interface Config<I extends DefaultLockHandler<?>> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the abstract operation performed by this component.
		 * 
		 * <p>
		 * The operation identifies an application-wide configured lock strategy configured in the
		 * {@link LockService}.
		 * </p>
		 * 
		 * @see LockService#createLock(String, Object...)
		 */
		@Name("lockOperation")
		@NonNullable
		@StringDefault(Token.DEFAULT_OPERATION)
		@Options(fun = AllLockOperations.class)
		String getLockOperation();

		/**
		 * @see #getLockOperation()
		 */
		void setLockOperation(String value);

		/**
		 * Option provider for {@link Config#getLockOperation()}.
		 */
		class AllLockOperations extends Function0<List<String>> {
			@Override
			public List<String> apply() {
				List<String> result = new ArrayList<>(LockService.getInstance().getOperations());
				result.sort(String::compareTo);
				return result;
			}

		}
	}

	/**
	 * Creates a {@link DefaultLockHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultLockHandler(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The {@link LockService} to use.
	 */
	protected final LockService getService() {
		return _service;
	}

	@Override
	protected Lock createLock(Object model) {
		return getService().createLock(getConfig().getLockOperation(), model);
	}

	/**
	 * Creates an instance of {@link DefaultLockHandler}.
	 *
	 * @param operation
	 *        See {@link LockService#createLock(String, Object...)}.
	 * @return The new instance.
	 */
	public static LockHandler newInstance(String operation) {
		Config<?> config = TypedConfiguration.newConfigItem(Config.class);
		config.setLockOperation(operation);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

}
