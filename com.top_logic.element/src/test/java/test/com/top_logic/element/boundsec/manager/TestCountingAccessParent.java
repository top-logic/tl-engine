/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ParentCheck;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link AccessParentDefinition} of the tests behaving exactly like the definition it wraps, but
 * counting how often the access decision asks the access parents of an object.
 *
 * <p>
 * The count tells the work an access check does independently of the speed of the machine: each
 * {@link AccessParentFunction#allows(TLObject, BoundCommandGroup, ParentCheck) decision} on an
 * object and each {@link AccessParentFunction#resolveAll(TLObject, BoundCommandGroup) lookup} of
 * its access parents is counted once.
 * </p>
 */
public class TestCountingAccessParent extends AbstractConfiguredInstance<TestCountingAccessParent.Config>
		implements AccessParentDefinition {

	private static final AtomicLong INVOCATIONS = new AtomicLong();

	/**
	 * Configuration of {@link TestCountingAccessParent}.
	 */
	@TagName("test-counting")
	public interface Config extends PolymorphicConfiguration<TestCountingAccessParent> {

		/**
		 * The definition counted, a single one.
		 */
		@Name("definitions")
		@DefaultContainer
		List<PolymorphicConfiguration<? extends AccessParentDefinition>> getDefinitions();

	}

	/**
	 * Creates a {@link TestCountingAccessParent} from configuration.
	 */
	public TestCountingAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Resets the count of invocations.
	 */
	public static void resetInvocations() {
		INVOCATIONS.set(0);
	}

	/**
	 * The number of invocations since the last {@link #resetInvocations()}.
	 */
	public static long invocations() {
		return INVOCATIONS.get();
	}

	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		context.error("A wrapped access parent is resolved when the access rights are loaded.");
		return null;
	}

	@Override
	public AccessParentFunction resolveWith(InstantiationContext context, TLClass type, CandidateResolver candidates) {
		List<PolymorphicConfiguration<? extends AccessParentDefinition>> definitions = getConfig().getDefinitions();
		if (definitions.size() != 1) {
			context.error("A counted access parent wraps exactly one definition.");
			return null;
		}
		AccessParentFunction function = candidates.resolveCandidate(context, type, definitions.get(0));
		return function == null ? null : new CountingRelation(function);
	}

	/**
	 * {@link AccessParentFunction} counting the decisions and lookups of the relation it wraps.
	 *
	 * @param relation
	 *        The relation deciding.
	 */
	public record CountingRelation(AccessParentFunction relation) implements AccessParentFunction {

		@Override
		public TLObject resolve(TLObject object) {
			return relation.resolve(object);
		}

		@Override
		public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
			INVOCATIONS.incrementAndGet();
			return relation.resolveAll(object, operation);
		}

		@Override
		public Iterable<TLObject> resolveDraft(TLObject draft, BoundCommandGroup operation) {
			INVOCATIONS.incrementAndGet();
			return relation.resolveDraft(draft, operation);
		}

		@Override
		public boolean delegatesCreation() {
			return relation.delegatesCreation();
		}

		@Override
		public boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
			INVOCATIONS.incrementAndGet();
			return relation.allows(object, operation, check);
		}

		@Override
		public boolean isDisjunctive() {
			return relation.isDisjunctive();
		}

		@Override
		public Set<TLClass> getParentTypes(TLClass type) {
			return relation.getParentTypes(type);
		}

		@Override
		public ResKey getLabel() {
			return relation.getLabel();
		}

		@Override
		public boolean delegates(BoundCommandGroup operation) {
			return relation.delegates(operation);
		}

		@Override
		public Set<BoundCommandGroup> getOperations() {
			return relation.getOperations();
		}

		@Override
		public boolean delegatesAll() {
			return relation.delegatesAll();
		}

		/**
		 * The wrapped relation, so that the counting is invisible to the access decision.
		 */
		@Override
		public AccessParentFunction getRelation() {
			return relation.getRelation();
		}

	}

}
