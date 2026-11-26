/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * {@link AccessParentDefinition} of the tests combining its candidates by AND: an operation is
 * allowed only when it is allowed on a parent of every candidate.
 *
 * <p>
 * The engine offers no such combinator; it shows that an application can add one by overriding
 * {@link AccessParentFunction#allows(TLObject, BoundCommandGroup, ParentCheck)}.
 * </p>
 */
public class TestAllOfAccessParent extends AbstractConfiguredInstance<TestAllOfAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link TestAllOfAccessParent}.
	 */
	@TagName("test-all-of")
	public interface Config extends PolymorphicConfiguration<TestAllOfAccessParent> {

		/**
		 * The definitions that all must allow an operation.
		 */
		@Name("candidates")
		@DefaultContainer
		List<PolymorphicConfiguration<? extends AccessParentDefinition>> getCandidates();

	}

	/**
	 * Creates a {@link TestAllOfAccessParent} from configuration.
	 */
	public TestAllOfAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		context.error("A combination of access parents is resolved when the access rights are loaded.");
		return null;
	}

	@Override
	public AccessParentFunction resolveWith(InstantiationContext context, TLClass type, CandidateResolver candidates) {
		List<AccessParentFunction> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends AccessParentDefinition> candidate : getConfig().getCandidates()) {
			AccessParentFunction function = candidates.resolveCandidate(context, type, candidate);
			if (function == null) {
				return null;
			}
			result.add(function);
		}
		return new AllOfRelation(result);
	}

	/**
	 * {@link AccessParentFunction} allowing an operation when every candidate allows it.
	 *
	 * @param candidates
	 *        The relations that all must allow an operation.
	 */
	public record AllOfRelation(List<AccessParentFunction> candidates) implements AccessParentFunction {

		@Override
		public TLObject resolve(TLObject object) {
			for (AccessParentFunction candidate : candidates) {
				TLObject parent = candidate.resolve(object);
				if (parent != null) {
					return parent;
				}
			}
			return null;
		}

		@Override
		public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
			List<TLObject> result = new ArrayList<>();
			for (AccessParentFunction candidate : candidates) {
				candidate.resolveAll(object, operation).forEach(result::add);
			}
			return result;
		}

		@Override
		public boolean delegates(BoundCommandGroup operation) {
			return candidates.stream().anyMatch(candidate -> candidate.delegates(operation));
		}

		@Override
		public boolean allows(TLObject object, BoundCommandGroup operation, ParentCheck check) {
			boolean decided = false;
			for (AccessParentFunction candidate : candidates) {
				if (!candidate.delegates(operation)) {
					continue;
				}
				if (!candidate.allows(object, operation, check)) {
					return false;
				}
				decided = true;
			}
			return decided;
		}

		@Override
		public boolean isDisjunctive() {
			return false;
		}

		@Override
		public Set<TLClass> getParentTypes(TLClass type) {
			Set<TLClass> result = new HashSet<>();
			for (AccessParentFunction candidate : candidates) {
				Set<TLClass> types = candidate.getParentTypes(type);
				if (types == null) {
					return null;
				}
				result.addAll(types);
			}
			return result;
		}

		@Override
		public ResKey getLabel() {
			return ResKey.text("all of " + candidates);
		}

	}

}
