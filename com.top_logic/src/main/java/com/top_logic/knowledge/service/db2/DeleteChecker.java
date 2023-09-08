/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.access.DeleteConstraint;
import com.top_logic.model.annotate.persistency.TLDeleteConstraints;
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm for applying annotated {@link DeleteConstraint}s to potentially large sets of objects
 * to be deleted.
 * 
 * @see DeleteChecker#DeleteChecker(Iterable)
 * @see #checkAll()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteChecker {
	private Iterable<? extends TLObject> _deletions;

	private final Map<TLStructuredType, Collection<? extends TLStructuredType>> _concreteTypes = new HashMap<>();

	private final Map<TLStructuredType, List<DeleteConstraint>> _localConstraints = new HashMap<>();

	private final Map<TLStructuredType, Consumer<TLObject>> _checks = new HashMap<>();

	/**
	 * Creates a {@link DeleteChecker} that is able to check all given objects.
	 *
	 * @param deletions
	 *        All objects to analyze.
	 * @see #checkAll()
	 */
	public DeleteChecker(Iterable<? extends TLObject> deletions) {
		_deletions = deletions;
		analyzeTypes();
		createChecks();
	}

	private void analyzeTypes() {
		for (TLObject deleted : _deletions) {
			TLObject obj = deleted;
			TLStructuredType type = obj.tType();
			if (type == null || !type.tValid()) {
				// In the process of setting up the system, table types have not yet been created,
				// therefore, they cannot be returned from legacy objects. If an object is being
				// deleted together with it's type, the type also can no longer be accessed.
				continue;
			}
			if (_concreteTypes.containsKey(type)) {
				continue;
			}
			Collection<? extends TLStructuredType> generalizations = relevantGeneralizations(type);
			_concreteTypes.put(type, generalizations);
		}
	}

	private Collection<? extends TLStructuredType> relevantGeneralizations(TLStructuredType type) {
		if (type.getModelKind() == ModelKind.CLASS) {
			return relevantGeneralizations((TLClass) type);
		} else {
			return Collections.singleton(type);
		}
	}

	private Collection<? extends TLStructuredType> relevantGeneralizations(TLClass type) {
		LinkedHashSet<TLStructuredType> result = new LinkedHashSet<>();
		fillRelevantGeneralizations(result, type);
		return result;
	}

	private void fillRelevantGeneralizations(LinkedHashSet<TLStructuredType> result, TLClass type) {
		if (result.contains(type)) {
			return;
		}
		result.add(type);

		if (!isOverride(type)) {
			for (TLClass generalization : type.getGeneralizations()) {
				fillRelevantGeneralizations(result, generalization);
			}
		}
	}

	private boolean isOverride(TLType specialization) {
		TLDeleteConstraints annotation = annotation(specialization);
		if (annotation == null) {
			return false;
		}
		return annotation.getOverride();
	}

	/**
	 * Invokes the check for all objects passed to the constructor.
	 */
	public void checkAll() {
		for (TLObject deleted : _deletions) {
			check(deleted);
		}
	}

	private void check(TLObject obj) {
		TLStructuredType type = obj.tType();
		if (type == null || !type.tValid()) {
			// In the process of setting up the system, table types have not yet been created,
			// therefore, they cannot be returned from legacy objects. If an object is being
			// deleted together with it's type, the type also can no longer be accessed.
			return;
		}
		Consumer<TLObject> check = _checks.get(type);
		check.accept(obj);
	}

	private void createChecks() {
		for (Entry<TLStructuredType, Collection<? extends TLStructuredType>> entry : _concreteTypes.entrySet()) {
			Collection<? extends TLStructuredType> generalizations = entry.getValue();
			buildLocalConstraints(generalizations);
			_checks.put(entry.getKey(), toCheck(constraints(generalizations)));
		}
	}

	private Consumer<TLObject> toCheck(List<DeleteConstraint> constraints) {
		return (obj) -> {
			{
				Optional<ResKey> veto = obj.tDeleteVeto();
				if (veto.isPresent()) {
					throw new TopLogicException(veto.get());
				}
			}
			for (DeleteConstraint constraint : constraints) {
				Optional<ResKey> veto = constraint.getDeleteVeto(obj);
				if (veto.isPresent()) {
					throw new TopLogicException(veto.get());
				}
			}
		};
	}

	private List<DeleteConstraint> constraints(Collection<? extends TLStructuredType> generalizations) {
		List<DeleteConstraint> result = new ArrayList<>();
		for (TLStructuredType type : generalizations) {
			result.addAll(_localConstraints.get(type));
		}
		return result;
	}

	private void buildLocalConstraints(Collection<? extends TLStructuredType> generalizations) {
		for (TLStructuredType type : generalizations) {
			if (_localConstraints.containsKey(type)) {
				continue;
			}
			_localConstraints.put(type, createLocalConstraints(type));
		}
	}

	private List<DeleteConstraint> createLocalConstraints(TLStructuredType type) {
		if (type == null) {
			return Collections.emptyList();
		}

		TLDeleteConstraints annotation = annotation(type);
		if (annotation == null) {
			return Collections.emptyList();
		}

		List<DeleteConstraint> constraints = TypedConfiguration.getInstanceList(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, annotation.getValue());
		return constraints;
	}

	private TLDeleteConstraints annotation(TLType type) {
		return type.getAnnotation(TLDeleteConstraints.class);
	}
}