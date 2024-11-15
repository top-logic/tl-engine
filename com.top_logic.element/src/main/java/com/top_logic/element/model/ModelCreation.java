/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.ArrayQueue;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLTypePart;

/**
 * Scheduler for the phases of a model instantiation.
 * 
 * <p>
 * Each phase is organized as sequence of {@link Runnable}s that may produces more {@link Runnable}s
 * during execution, which should be executed in a following phase.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelCreation {

	enum Phase {
		/**
		 * The initial setup phase.
		 * 
		 * <p>
		 * This phase is implicitly active, when the setup starts. In this phase, modules and empty
		 * type instances are created. Such creations can happen independently of each other.
		 * </p>
		 */
		START,

		/**
		 * Creation of the type hierarchy.
		 * 
		 * <p>
		 * All types must have been created in the preceding phase, before for each type its
		 * generalizations could be set.
		 * </p>
		 */
		CREATE_TYPE_HIERARCHY,

		/**
		 * Creation of type parts.
		 * 
		 * <p>
		 * All types must have been created and its generalizations should been set, before its
		 * parts could be created.
		 * </p>
		 * 
		 * <p>
		 * The inheritance hierarchy must be known in order to topologically sort the actions for
		 * creating the type parts. This sorting is necessary because an override of a
		 * {@link TLTypePart} can only be created if the underlying type part already exists.
		 * </p>
		 */
		CREATE_PARTS,

		/**
		 * Creations of association ends.
		 * 
		 * <p>
		 * Association ends must be created before references are created, because these use
		 * association ends as their implementation.
		 * </p>
		 */
		CREATE_ASSOCIATION_END,

		/**
		 * Creation of references.
		 * 
		 * <p>
		 * References must be created before creating reference overrides.
		 * </p>
		 */
		CREATE_REFERENCE,

		/**
		 * Creation of reference overrides.
		 * 
		 * <p>
		 * All reference overrides must be created before back references are created, because there
		 * might be back references on overridden references.
		 * </p>
		 */
		CREATE_REFERENCE_OVERRIDE,

		/**
		 * Creation of back references.
		 * 
		 * <p>
		 * Back references must be created before creating back reference overrides
		 * </p>
		 * 
		 * <p>
		 * Back references must be created before finally determining the total order of all
		 * properties of a class.
		 * </p>
		 */
		CREATE_BACK_REFERENCE,

		/**
		 * Creation of property overrides.
		 * 
		 * <p>
		 * All property overrides must be created before back references are created, because there
		 * might be back references on overridden references.
		 * </p>
		 */
		CREATE_BACK_REFERENCE_OVERRIDE,

		/**
		 * Establishing the final property order.
		 */
		REORDER_PROPERTIES,

		/**
		 * When this resolver runs in the context of a model patch apply, this is the phase, where
		 * old model elements are deleted.
		 */
		CLEANUP,

		/**
		 * Creating roles in modules.
		 */
		CREATE_ROLE,

		/**
		 * Creating module singletons.
		 */
		CREATE_SINGLETON,
	}

	private Phase _current = Phase.START;

	private final EnumMap<Phase, Queue<Runnable>> _jobs = new EnumMap<>(Phase.class);

	private InTopologicalSortOrder _createParts;
	
	private InTopologicalSortOrder _createReferenceOverrides;

	private InTopologicalSortOrder _createBackReferenceOverrides;

	/**
	 * Creates a {@link ModelCreation}.
	 */
	public ModelCreation() {
		super();
	}

	/**
	 * Schedules the creation of the models type hierarchy.
	 */
	public void createTypeHierarchy(Runnable step) {
		add(Phase.CREATE_TYPE_HIERARCHY, step);
	}

	/**
	 * Schedules the creation of type parts.
	 * 
	 * <p>
	 * Type part overrides must be sorted topologically with respect to the inheritance hierarchy of
	 * their type, since one override may overwrite another.
	 * </p>
	 */
	public void createParts(TLClass type, Runnable step) {
		if (_createParts == null) {
			_createParts = new InTopologicalSortOrder();

			add(Phase.CREATE_PARTS, _createParts);
		}

		_createParts.add(type, step);
	}

	/**
	 * Schedules an association end creation.
	 */
	public void createAssociationEnd(Runnable step) {
		add(Phase.CREATE_ASSOCIATION_END, step);
	}

	/**
	 * Schedules a reference creation.
	 */
	public void createReference(Runnable step) {
		add(Phase.CREATE_REFERENCE, step);
	}

	/**
	 * Schedules a reference override creation.
	 * 
	 * <p>
	 * Type part overrides must be sorted topologically with respect to the inheritance hierarchy of
	 * their type, since one override may overwrite another.
	 * </p>
	 */
	public void createReferenceOverride(TLClass type, Runnable step) {
		if (_createReferenceOverrides == null) {
			_createReferenceOverrides = new InTopologicalSortOrder();

			add(Phase.CREATE_REFERENCE_OVERRIDE, _createReferenceOverrides);
		}

		_createReferenceOverrides.add(type, step);
	}

	/**
	 * Schedules a backwards reference creation.
	 */
	public void createBackReference(Runnable step) {
		add(Phase.CREATE_BACK_REFERENCE, step);
	}

	/**
	 * Schedules a backwards reference override creation.
	 * 
	 * <p>
	 * Type part overrides must be sorted topologically with respect to the inheritance hierarchy of
	 * their type, since one override may overwrite another.
	 * </p>
	 */
	public void createBackReferenceOverride(TLClass type, Runnable step) {
		if (_createBackReferenceOverrides == null) {
			_createBackReferenceOverrides = new InTopologicalSortOrder();

			add(Phase.CREATE_BACK_REFERENCE_OVERRIDE, _createBackReferenceOverrides);
		}

		_createBackReferenceOverrides.add(type, step);
	}

	/**
	 * Schedules reordering type properties.
	 */
	public void reorderProperties(Runnable step) {
		add(Phase.REORDER_PROPERTIES, step);
	}

	/**
	 * Schedules a cleanup operation.
	 */
	public void cleanup(Runnable step) {
		add(Phase.CLEANUP, step);
	}

	/**
	 * Schedules a role creation.
	 */
	public void createRole(Runnable step) {
		add(Phase.CREATE_ROLE, step);
	}

	/**
	 * Schedules a singleton creation.
	 */
	public void createSingleton(Runnable step) {
		add(Phase.CREATE_SINGLETON, step);
	}

	private void add(Phase phase, Runnable value) {
		if (phase.ordinal() <= _current.ordinal()) {
			throw new IllegalStateException("Phase '" + phase + "' has already started.");
		}
		_jobs.computeIfAbsent(phase, x -> new ArrayQueue<>()).add(value);
	}

	/**
	 * Processes all {@link Runnable}s added to this instance until no more {@link Runnable}s
	 * are produced.
	 * 
	 * @param log
	 *        The logging output of the computation.
	 */
	public void complete(Protocol log) {
		for (Phase phase : Phase.values()) {
			_current = phase;
	
			log.checkErrors();
			Queue<Runnable> job = _jobs.getOrDefault(phase, CollectionUtil.emptyQueue());
			while (!job.isEmpty()) {
				job.poll().run();
			}
		}
	}

	static final class InTopologicalSortOrder implements Runnable {

		Map<TLClass, List<Runnable>> _jobs = new HashMap<>();

		void add(TLClass context, Runnable r) {
			_jobs.computeIfAbsent(context, x -> new ArrayList<>()).add(r);
		}

		@Override
		public void run() {
			List<TLClass> order = CollectionUtil.topsort(c -> c.getGeneralizations(), _jobs.keySet(), false);
			for (TLClass type : order) {
				for (Runnable r : _jobs.get(type)) {
					r.run();
				}
			}
		}
	}

}
