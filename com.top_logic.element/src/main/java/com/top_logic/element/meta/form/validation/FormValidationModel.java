/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.constraint.MandatoryConstraintCheck;
import com.top_logic.element.meta.form.constraint.RangeConstraintCheck;
import com.top_logic.element.meta.form.constraint.SizeConstraintCheck;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.annotate.util.ConstraintCheck.ConstraintType;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.util.Pointer;

/**
 * Central validation coordinator for a form.
 *
 * <p>
 * Manages all overlay objects and their constraints. Created by the view
 * component that renders the form.
 * </p>
 */
public class FormValidationModel implements OverlayLookup {

	/** Map from persistent object to overlay (for edit overlays). */
	private final Map<TLObject, TLObject> _overlaysByEdited = new HashMap<>();

	/** All overlays including create overlays (which have no persistent base). */
	private final List<TLObject> _allOverlays = new ArrayList<>();

	/** All constraint entries, grouped by owning (object, attribute). */
	private final Map<Pointer, List<ConstraintEntry>> _constraintsByOwner = new HashMap<>();

	/**
	 * Reverse dependency map: for each (object, attribute) that is read during
	 * a constraint check, which ConstraintEntries depend on it?
	 */
	private final Map<PointerKey, Set<ConstraintEntry>> _dependencyMap = new HashMap<>();

	/** Global listeners. */
	private final List<ConstraintValidationListener> _listeners = new ArrayList<>();

	/** Validation results per (object, attribute). */
	private final Map<PointerKey, ValidationResult> _validationResults = new HashMap<>();

	/**
	 * Registers an overlay and derives constraints from its type.
	 *
	 * @param overlay
	 *        The overlay object to add.
	 * @param editedBase
	 *        The persistent base object being edited, or {@code null} for create overlays.
	 */
	public void addOverlay(TLObject overlay, TLObject editedBase) {
		_allOverlays.add(overlay);
		if (editedBase != null) {
			_overlaysByEdited.put(editedBase, overlay);
		}
		deriveConstraints(overlay);
		validateAllFor(overlay);
	}

	/**
	 * Removes an overlay and cleans up constraints and dependencies.
	 */
	public void removeOverlay(TLObject overlay) {
		_allOverlays.remove(overlay);
		_overlaysByEdited.values().remove(overlay);

		// Remove all constraint entries owned by this overlay.
		List<ConstraintEntry> removed = new ArrayList<>();
		_constraintsByOwner.entrySet().removeIf(entry -> {
			if (entry.getValue().get(0).getObject() == overlay) {
				removed.addAll(entry.getValue());
				return true;
			}
			return false;
		});
		// Clean dependency map.
		for (ConstraintEntry entry : removed) {
			removeDependencies(entry);
		}

		// Clean validation results for this overlay.
		_validationResults.entrySet().removeIf(e -> e.getKey()._object == overlay);

		// Re-validate constraints that depended on this overlay.
		Set<ConstraintEntry> affected = findAffectedConstraints(overlay);
		revalidate(affected);
	}

	@Override
	public TLObject getExistingOverlay(TLObject object) {
		if (_allOverlays.contains(object)) {
			return object;
		}
		return _overlaysByEdited.get(object);
	}

	@Override
	public Iterable<? extends TLObject> getOverlays() {
		return _allOverlays;
	}

	/**
	 * Returns the validation result for the given overlay and attribute.
	 */
	public ValidationResult getValidation(TLObject overlay, TLStructuredTypePart attribute) {
		ValidationResult result = _validationResults.get(new PointerKey(overlay, attribute));
		return result != null ? result : ValidationResult.VALID;
	}

	/**
	 * Whether all overlays are free of ERROR-type validation results.
	 */
	public boolean isValid() {
		for (TLObject overlay : _allOverlays) {
			TLStructuredType type = (TLStructuredType) overlay.tType();
			if (type == null) continue;
			for (TLStructuredTypePart part : type.getAllParts()) {
				if (!getValidation(overlay, part).isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Adds a global validation listener.
	 */
	public void addConstraintValidationListener(ConstraintValidationListener listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes a previously added global validation listener.
	 */
	public void removeConstraintValidationListener(ConstraintValidationListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Called when a value changes on an overlay. Triggers re-validation of
	 * all constraints that depend on the changed attribute.
	 *
	 * @param overlay
	 *        The overlay whose value changed.
	 * @param attribute
	 *        The attribute whose value changed.
	 */
	public void onValueChanged(TLObject overlay, TLStructuredTypePart attribute) {
		Set<ConstraintEntry> toValidate = new HashSet<>();

		// Constraints that depend on this attribute (cross-field dependencies).
		PointerKey key = new PointerKey(overlay, attribute);
		Set<ConstraintEntry> dependents = _dependencyMap.get(key);
		if (dependents != null) {
			toValidate.addAll(dependents);
		}

		// Constraints owned by this attribute itself.
		Pointer ownerKey = Pointer.create(overlay, attribute);
		List<ConstraintEntry> ownConstraints = _constraintsByOwner.get(ownerKey);
		if (ownConstraints != null) {
			toValidate.addAll(ownConstraints);
		}

		if (!toValidate.isEmpty()) {
			revalidate(toValidate);
		}
	}

	private void deriveConstraints(TLObject overlay) {
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return;

		for (TLStructuredTypePart part : type.getAllParts()) {
			List<ConstraintEntry> entries = new ArrayList<>();

			// Mandatory check.
			if (part.isMandatory()) {
				entries.add(new ConstraintEntry(MandatoryConstraintCheck.INSTANCE, overlay, part));
			}

			// Size check from @TLSize.
			TLSize sizeAnnotation = part.getAnnotation(TLSize.class);
			if (sizeAnnotation != null) {
				int lower = AttributeOperations.getLowerBound(sizeAnnotation);
				int upper = AttributeOperations.getUpperBound(sizeAnnotation);
				if (lower > 0 || upper < Integer.MAX_VALUE) {
					entries.add(new ConstraintEntry(new SizeConstraintCheck(lower, upper), overlay, part));
				}
			}

			// Range check from @TLRange.
			TLRange rangeAnnotation = part.getAnnotation(TLRange.class);
			if (rangeAnnotation != null) {
				Double min = AttributeOperations.getMinimum(rangeAnnotation);
				Double max = AttributeOperations.getMaximum(rangeAnnotation);
				if (min != null || max != null) {
					entries.add(new ConstraintEntry(new RangeConstraintCheck(min, max), overlay, part));
				}
			}

			// Custom constraints from @TLConstraints.
			TLConstraints constraintsAnnotation = part.getAnnotation(TLConstraints.class);
			if (constraintsAnnotation != null) {
				for (var checkConfig : constraintsAnnotation.getConstraints()) {
					ConstraintCheck check = SimpleInstantiationContext
						.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(checkConfig);
					if (check != null) {
						entries.add(new ConstraintEntry(check, overlay, part));
					}
				}
			}

			if (!entries.isEmpty()) {
				_constraintsByOwner.put(Pointer.create(overlay, part), entries);
			}
		}
	}

	private void validateAllFor(TLObject overlay) {
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return;

		Set<ConstraintEntry> all = new HashSet<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			List<ConstraintEntry> entries = _constraintsByOwner.get(Pointer.create(overlay, part));
			if (entries != null) {
				all.addAll(entries);
			}
		}
		revalidate(all);
	}

	/**
	 * Re-validates a set of constraint entries using a fixed-point loop.
	 */
	private void revalidate(Set<ConstraintEntry> toCheck) {
		Set<ConstraintEntry> evaluated = new HashSet<>();
		Set<ConstraintEntry> current = toCheck;

		while (!current.isEmpty()) {
			Set<ConstraintEntry> next = new HashSet<>();

			// Group by (object, attribute) for aggregation.
			Map<PointerKey, List<ConstraintEntry>> grouped = new HashMap<>();
			for (ConstraintEntry entry : current) {
				if (evaluated.contains(entry)) continue;
				PointerKey key = new PointerKey(entry.getObject(), entry.getAttribute());
				grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
			}

			for (var groupEntry : grouped.entrySet()) {
				List<ConstraintEntry> entries = groupEntry.getValue();
				TLObject object = entries.get(0).getObject();
				TLStructuredTypePart attribute = entries.get(0).getAttribute();

				List<ResKey> errors = new ArrayList<>();
				List<ResKey> warnings = new ArrayList<>();
				boolean mandatoryFailed = false;

				for (ConstraintEntry entry : entries) {
					evaluated.add(entry);

					// Mandatory short-circuit: skip further checks if mandatory failed.
					if (mandatoryFailed && !(entry.getCheck() instanceof MandatoryConstraintCheck)) {
						continue;
					}

					// Execute check.
					ResKey result = entry.getCheck().check(object, attribute);

					// Trace dependencies.
					Set<Pointer> newDeps = traceDependencies(entry);
					updateDependencies(entry, newDeps);

					if (result != null) {
						if (entry.getType() == ConstraintType.ERROR) {
							errors.add(result);
							if (entry.getCheck() instanceof MandatoryConstraintCheck) {
								mandatoryFailed = true;
							}
						} else {
							warnings.add(result);
						}
					}
				}

				// Store result internally.
				ValidationResult validationResult;
				if (errors.isEmpty() && warnings.isEmpty()) {
					validationResult = ValidationResult.VALID;
				} else {
					validationResult = new ValidationResult(errors, warnings);
				}

				PointerKey resultKey = new PointerKey(object, attribute);
				ValidationResult previous = _validationResults.put(resultKey, validationResult);
				if (previous == null) previous = ValidationResult.VALID;

				if (!validationResult.equals(previous)) {
					for (ConstraintValidationListener listener : _listeners) {
						listener.onValidationChanged(object, attribute, validationResult);
					}
					// Cascading: if result changed, check if any other constraints
					// depend on this (object, attribute) and haven't been evaluated yet.
					PointerKey changedKey = new PointerKey(object, attribute);
					Set<ConstraintEntry> cascaded = _dependencyMap.get(changedKey);
					if (cascaded != null) {
						for (ConstraintEntry dep : cascaded) {
							if (!evaluated.contains(dep)) {
								next.add(dep);
							}
						}
					}
				}
			}

			current = next;
		}
	}

	private Set<Pointer> traceDependencies(ConstraintEntry entry) {
		Set<Pointer> deps = new HashSet<>();
		entry.getCheck().traceDependencies(
			entry.getObject(), entry.getAttribute(),
			deps::add,
			this);
		return deps;
	}

	private void updateDependencies(ConstraintEntry entry, Set<Pointer> newDeps) {
		// Remove old dependencies.
		removeDependencies(entry);

		// Add new dependencies.
		entry.setDependencies(newDeps);
		for (Pointer dep : newDeps) {
			PointerKey key = new PointerKey(dep.object(), dep.attribute());
			_dependencyMap.computeIfAbsent(key, k -> new HashSet<>()).add(entry);
		}
	}

	private void removeDependencies(ConstraintEntry entry) {
		for (Pointer dep : entry.getDependencies()) {
			PointerKey key = new PointerKey(dep.object(), dep.attribute());
			Set<ConstraintEntry> set = _dependencyMap.get(key);
			if (set != null) {
				set.remove(entry);
				if (set.isEmpty()) {
					_dependencyMap.remove(key);
				}
			}
		}
		entry.setDependencies(Collections.emptySet());
	}

	private Set<ConstraintEntry> findAffectedConstraints(TLObject overlay) {
		Set<ConstraintEntry> affected = new HashSet<>();
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return affected;

		for (TLStructuredTypePart part : type.getAllParts()) {
			PointerKey key = new PointerKey(overlay, part);
			Set<ConstraintEntry> deps = _dependencyMap.get(key);
			if (deps != null) {
				affected.addAll(deps);
			}
		}
		return affected;
	}

	/**
	 * Hashable key for (TLObject, TLStructuredTypePart) pairs used in the
	 * dependency map.
	 */
	private static class PointerKey {
		private final TLObject _object;
		private final TLStructuredTypePart _attribute;

		PointerKey(TLObject object, TLStructuredTypePart attribute) {
			_object = object;
			_attribute = attribute;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PointerKey)) return false;
			PointerKey other = (PointerKey) obj;
			return _object == other._object && _attribute == other._attribute;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(_object) * 31 + System.identityHashCode(_attribute);
		}
	}
}
