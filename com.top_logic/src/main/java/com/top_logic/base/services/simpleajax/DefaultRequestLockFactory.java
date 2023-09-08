/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.security.SecureRandom;

import com.top_logic.base.services.simpleajax.RequestLock.Options;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.module.ServiceDependencies;

/**
 * Default implementation of {@link RequestLockFactory} using global settings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies(SecureRandomService.Module.class)
public class DefaultRequestLockFactory extends RequestLockFactory {

	/**
	 * Linking {@link Options} to
	 * {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration}.
	 */
	public interface Config extends ServiceConfiguration<RequestLockFactory>, Options {

		/**
		 * @see #getSeedLimit()
		 */
		String SEED_LIMIT = "seed-limit";

		/**
		 * Limit to the maximum initial random request number value.
		 * 
		 * <p>
		 * The initial request number of a {@link RequestLock} created by this factory is guaranteed
		 * not to be greater or equal to this value.
		 * </p>
		 */
		@Name(SEED_LIMIT)
		@IntDefault(10000)
		int getSeedLimit();

	}

	private final OptimizedOptions _defaultOptions;

	private int _seedLimit;

	private final SecureRandom _rnd;

	/**
	 * Creates a {@link DefaultRequestLockFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultRequestLockFactory(InstantiationContext context, Config config) {
		super(context, config);
		_defaultOptions = new OptimizedOptions(config);
		_seedLimit = config.getSeedLimit();
		_rnd = SecureRandomService.getInstance().getRandom();
	}

	/**
	 * The default options for creating new {@link RequestLock} instances.
	 */
	public final Options getDefaultOptions() {
		return _defaultOptions;
	}

	@Override
	public RequestLock createLock() {
		return new RequestLock(_defaultOptions, createInitialRequestNumber());
	}

	/**
	 * A random initial request number.
	 */
	protected int createInitialRequestNumber() {
		return _rnd.nextInt(_seedLimit);
	}

	/**
	 * Optimized {@link Options} implementation.
	 */
	protected static final class OptimizedOptions implements Options {

		/**
		 * @see #getMaxWriters()
		 */
		private final int maxWriters;

		/**
		 * @see #getMaxWaitingReaders()
		 */
		private final int maxWaitingReaders;

		/**
		 * @see #getReorderTimeout()
		 */
		private final long requestTimeout;

		/**
		 * @see #getWriterWaitingTime()
		 */
		private final long writerWaitingTime;

		/**
		 * @see #getReaderWaitingTime()
		 */
		private final long readerWaitingTime;

		/**
		 * Creates an optimized {@link OptimizedOptions} copy from the given {@link Options}.
		 */
		public OptimizedOptions(Options config) {
			this.requestTimeout = config.getReorderTimeout();
			this.writerWaitingTime = config.getWriterWaitingTime();
			this.readerWaitingTime = config.getReaderWaitingTime();
			this.maxWriters = config.getMaxWriters();
			this.maxWaitingReaders = config.getMaxWaitingReaders();
		}

		@Override
		public int getMaxWriters() {
			return maxWriters;
		}

		@Override
		public int getMaxWaitingReaders() {
			return maxWaitingReaders;
		}

		@Override
		public long getReorderTimeout() {
			return requestTimeout;
		}

		@Override
		public long getReaderWaitingTime() {
			return readerWaitingTime;
		}

		@Override
		public long getWriterWaitingTime() {
			return writerWaitingTime;
		}
	}

}
