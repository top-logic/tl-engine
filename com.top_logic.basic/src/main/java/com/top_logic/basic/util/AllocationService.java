/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.text.DecimalFormat;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.fun.PercentToFactor;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service for conditionally performing bulk memory allocations for heap-intensive computations.
 * 
 * @see #allocateConditionally(long, ComputationEx2)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllocationService extends ConfiguredManagedClass<AllocationService.Config> {

	/**
	 * Configuration options for {@link AllocationService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AllocationService> {

		/**
		 * @see #getMaxRequestSize()
		 */
		String MAX_REQUEST_SIZE = "max-request-size";

		/**
		 * @see #getReservedSpaceAbsolute()
		 */
		String RESERVED_SPACE_ABSOLUTE = "reserved-space-absolute";

		/**
		 * @see #getReservedSpacePercent()
		 */
		String RESERVED_SPACE_PERCENT = "reserved-space-percent";

		/**
		 * Maximum number of bytes allowed to be allocated for a single bulk user operation.
		 * 
		 * <p>
		 * A value of <code>0</code> means "no limit".
		 * </p>
		 * 
		 * <p>
		 * Due to VM constraints (multiple separate heap areas), it is in general not possible to
		 * allocate all the available memory at once in a continuous chunk. Not limiting the size of
		 * a single operation will lead to costly out of memory errors produced while trying to
		 * allocate an excessive amount of memory at once.
		 * </p>
		 */
		@Name(MAX_REQUEST_SIZE)
		@Format(MemorySizeFormat.class)
		long getMaxRequestSize();
		
		/**
		 * The number of bytes that must not be used for bulk user operations.
		 * 
		 * <p>
		 * The value must be
		 * </p>
		 */
		@Name(RESERVED_SPACE_ABSOLUTE)
		@Format(MemorySizeFormat.class)
		long getReservedSpaceAbsolute();

		/**
		 * Percentage of the {@link Runtime#maxMemory() maximum VM size} that must not be used for
		 * bulk user operations.
		 */
		@Name(RESERVED_SPACE_PERCENT)
		float getReservedSpacePercent();

		/**
		 * {@link #getReservedSpacePercent()} as factor.
		 */
		@Derived(fun = PercentToFactor.class, args = { @Ref(value = RESERVED_SPACE_PERCENT) })
		float getReservedSpaceFactor();
	}

	private static final long BYTES_PER_MB = 1024L * 1024;

	private final Runtime _runtime;

	/**
	 * Creates a {@link AllocationService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AllocationService(InstantiationContext context, Config config) {
		super(context, config);
		_runtime = Runtime.getRuntime();
	}

	/**
	 * The amount of free space assuming the VM was expanded to its {@link Runtime#maxMemory()
	 * maximum amount of heap space}.
	 */
	public long getAvailableMaxMemory() {
		return _runtime.maxMemory() - getUsedMemory();
	}

	/**
	 * The currently used amount of memory in bytes.
	 */
	public long getUsedMemory() {
		return _runtime.totalMemory() - _runtime.freeMemory();
	}

	/**
	 * Performs the given bulk memory allocating {@link ComputationEx2} only if the estimated amount
	 * of memory required is available.
	 * 
	 * @param estimatedSize
	 *        The size in bytes required by the the given allocation {@link ComputationEx2}.
	 * @param allocation
	 *        The memory allocation operation. Note: The given {@link ComputationEx2} must not be
	 *        long-running. It should only allocate the given estimated amount of memory and return
	 *        the allocated memory structure as result.
	 * @return The allocated memory, or <code>null</code>, if allocation is not possible due to
	 *         memory limitations.
	 */
	public <T, E1 extends Throwable, E2 extends Throwable> T allocateConditionally(long estimatedSize,
			ComputationEx2<T, E1, E2> allocation) throws E1, E2 {
		synchronized (this) {
			if (canAllocate(estimatedSize)) {
				try {
					return allocation.run();
				} catch (OutOfMemoryError ex) {
					// Might still happen due to concurrency of allocations not guarded by this
					// service.
					Logger.info(
						"Failed to perform memory-intensive user operation due to limited system resource: Estimated  "
							+ mb(estimatedSize) + "m being allocated by user operation. The system reports "
							+ mb(getAvailableMaxMemory()) + "m to be available.", new Exception("Stack trace", ex),
						AllocationService.class);
				}
			}
		}

		// Skip memory intensive computation.
		return null;
	}

	/**
	 * Checks whether the system has enough memory left if the given amount of memory would be
	 * allocated by a user function.
	 * 
	 * @param size
	 *        the amount of memory a user function needs.
	 * @return <code>true</code>, if there is enough free memory left to acquire the given amount of
	 *         memory without overloading the system
	 */
	private boolean canAllocate(long size) {
		long maxRequestSize = getConfig().getMaxRequestSize();
		if (maxRequestSize > 0 && size > maxRequestSize) {
			Logger.info(
				"Denying memory-intensive user operation beacuse it exceed the maximum allowed size. Estimated size are "
					+ mb(size) + "m of user memory, while the maximum allowed size per request is "
					+ mb(maxRequestSize) + "m.",
				new Exception("Stack trace."), AllocationService.class);
			return false;
		}

		long reservedSpace = getReservedSpace();
		long limit = size + reservedSpace;
		if (getAvailableMaxMemory() < limit) {
			System.gc();

			long availableAfterGC = getAvailableMaxMemory();
			if (availableAfterGC < limit) {
				Logger.info("Denying memory-intensive user operation due to limited system resource: Requested "
					+ mb(size) + "m of user memory, while only " + mb(availableAfterGC)
					+ "m are available in total (" + mb(reservedSpace) + "m are reserved for system purposes).",
					new Exception("Stack trace."), AllocationService.class);
				return false;
			}
		}
		return true;
	}

	private String mb(long size) {
		return new DecimalFormat("0.000").format(((double) size) / BYTES_PER_MB);
	}

	private long getReservedSpace() {
		return Math.max(
			getConfig().getReservedSpaceAbsolute(),
			(long) (getConfig().getReservedSpaceFactor() * _runtime.maxMemory()));
	}

	/**
	 * Access to {@link AllocationService} singleton.
	 */
	public static AllocationService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to {@link AllocationService}.
	 */
	public static final class Module extends TypedRuntimeModule<AllocationService> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AllocationService> getImplementation() {
			return AllocationService.class;
		}

	}

}

