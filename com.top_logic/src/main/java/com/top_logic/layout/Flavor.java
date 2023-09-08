/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Interface for rendering modifiers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Flavor {

	/**
	 * The default Flavor that is {@link #implies(Flavor) implied} by all other
	 * {@link Flavor}s.
	 */
	public static final Flavor DEFAULT = new Flavor();
	
    /** {@link Flavor} of expanded tree nodes. */
	public static final Flavor EXPANDED = atomic("EXPANDED", DEFAULT);
	
	/** {@link Flavor} of large mime type images. */
	public static final Flavor ENLARGED = atomic("ENLARGED", DEFAULT);
	
    /** {@link Flavor} of mandatory fields. */
	public static final Flavor MANDATORY = atomic("MANDATORY", DEFAULT);
	
    /** {@link Flavor} of disabled fields. */
	public static final Flavor DISABLED = atomic("DISABLED", DEFAULT);
	
    /** {@link Flavor} of immutable fields. */
	public static final Flavor IMMUTABLE = atomic("IMMUTABLE", DISABLED);
	
    /** {@link Flavor} of mandatory disabled fields. */
	public static final Flavor MANDATORY_DISABLED = aggregate(MANDATORY, DISABLED);
	
    /** {@link Flavor} of mandatory immutable fields. */
	public static final Flavor MANDATORY_IMMUTABLE = aggregate(MANDATORY, IMMUTABLE);

	/**
	 * Creates an aggregate {@link Flavor} {@link #implies(Flavor) implying} all
	 * given flavors.
	 */
	public static Flavor aggregate(Flavor... flavors) {
		if (flavors == null || flavors.length == 0) {
			return DEFAULT;
		} else {
			return createAggregate(Arrays.asList(flavors));
		}
	}

	/**
	 * Creates an aggregate {@link Flavor} {@link #implies(Flavor) implying} all
	 * given flavors.
	 */
	public static Flavor aggregate(Collection<Flavor> flavors) {
		if (flavors == null || flavors.size() == 0) {
			return DEFAULT;
		} else {
			return createAggregate(flavors);
		}
	}

	/**
	 * Creates a new atomic {@link Flavor} that always {@link #implies(Flavor)
	 * implies} the given default {@link Flavor}.
	 */
	public static Flavor atomic(String name, Flavor defaultFlavor) {
		assert defaultFlavor != null;
		
		Flavor atom = new Flavor(Collections.singleton(new Atom(name, defaultFlavor)));
		Flavor result = aggregate(atom, defaultFlavor);
		
		return result;
	}

	/**
	 * Root {@link Flavor} without default.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	/*package protected*/
	static class Atom implements Comparable<Atom> {
		private final int id;
		/*package protected*/ final String name;
		/*package protected*/ final Flavor defaultFlavor;
		
		private static int nextId;

		protected Atom(String name, Flavor defaultFlavor) {
			assert defaultFlavor != null;
			
			synchronized (Atom.class) {
				this.id = nextId++;
			}
			
			this.name = name;
			this.defaultFlavor = defaultFlavor;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public int compareTo(Atom other) {
			int thisId = this.id;
			int otherId = other.id;
			
			if (thisId > otherId) {
				return 1;
			} else if (thisId < otherId) {
				return -1;
			} else {
				assert this == other;
				return 0;
			}
		}
	}
	
	/*package protected*/ final Atom[] atoms;

	/**
	 * The defining atoms of a {@link Flavor} is the minimal set of {@link Atom}
	 * s whose closure under the {@link Atom#defaultFlavor default} relation is
	 * the set of the {@link Flavor}s {@link #atoms}.
	 */
	/*package protected*/ final Atom[] definingAtoms;
	
	/**
	 * Declared protected to be able to declare a subclass (for backwards compatibility).
	 */
	protected Flavor() {
		this.atoms = new Atom[0];
		this.definingAtoms = atoms;
	}
	
	private Flavor(Set<Atom> atoms) {
		assert atoms.size() > 0 : "a flavor must contain at least one atom";
		
		this.atoms = computeAtomArray(atoms);
		this.definingAtoms = computeDefiningAtoms(atoms);
	}

	/**
	 * Whether this {@link Flavor} smells like the given {@link Flavor}.
	 * 
	 * @param other
	 *        The concrete flavor that is checked for being contained in this
	 *        context {@link Flavor}.
	 * @return Whether this {@link Flavor} is subsumes the given {@link Flavor}.
	 *         The result must be <code>true</code>, if the given
	 *         {@link Flavor} is the {@link #DEFAULT} flavor.
	 */
	public boolean implies(Flavor other) {
		return containsAllOrdered(this.atoms, other.definingAtoms);
	}

	/*package protected*/ static Flavor createAggregate(Collection<Flavor> flavors) {
		if (flavors.size() == 1) {
			return flavors.iterator().next();
		}
		
		HashSet<Atom> uniqueAtoms = new HashSet<>();
		for (Flavor flavor : flavors) {
			assert flavor != null : "Aggregate flavor must not contain null.";
			addAll(uniqueAtoms, flavor.atoms);
		}
		
		if (uniqueAtoms.size() == 1) {
			// All are equal.
			return flavors.iterator().next();
		} else {
			return new Flavor(uniqueAtoms);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (! (obj instanceof Flavor)) {
			return false;
		}

		return equalsOrdered(atoms, ((Flavor) obj).atoms);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(atoms);
	}
	
	@Override
	public final String toString() {
		StringBuilder result = new StringBuilder();
		try {
			appendTo(result);
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder append does not fail.");
		}
		return result.toString();
	}

	/**
	 * Appends the {@link #toString()} representation to the given
	 * {@link Appendable}.
	 */
	public void appendTo(Appendable result) throws IOException {
		for (Atom atom : definingAtoms) {
			result.append('.');
			result.append(atom.name);
		}
	}

	/**
	 * @see #definingAtoms
	 */
	/*package protected*/ static Atom[] computeDefiningAtoms(Set<Atom> atoms) {
		Set<Atom> definingAtoms = new HashSet<>(atoms);
		for (Atom atom : atoms) {
			removeAll(definingAtoms, atom.defaultFlavor.atoms);
		}
		
		return computeAtomArray(definingAtoms);
	}
	
	private static Atom[] computeAtomArray(Set<Atom> atomSet) {
		Atom[] result = atomSet.toArray(new Atom[atomSet.size()]);
		Collections.sort(Arrays.asList(result));
		return result;
	}
	
	private static <T> void removeAll(Collection<T> definingAtoms, T[] values) {
		for (int n = 0; n < values.length; n++) {
			definingAtoms.remove(values[n]);
		}
	}
	
	private static <T> void addAll(Collection<T> uniqueAtoms, T[] values) {
		for (int n = 0; n < values.length; n++) {
			uniqueAtoms.add(values[n]);
		}
	}
	
	private static <T extends Comparable<T>> boolean equalsOrdered(T[] a1, T[] a2) {
		int cnt = a1.length;
		if (cnt != a2.length) {
			return false;
		}
		
		for (int n = 0; n < cnt; n++) {
			if (a1[n].compareTo(a2[n]) != 0) {
				return false;
			}
		}
		
		return true;
	}
	
	private static <T extends Comparable<T>> boolean containsAllOrdered(T[] a1, T[] a2) {
		int n2 = 0;
		int cnt2 = a2.length;
		if (cnt2 == 0) {
			return true;
		}
		
		int n1 = 0;
		int cnt1 = a1.length;
		if (cnt1 == 0) {
			return false;
		}
		
		T v1 = a1[n1++];
		T v2 = a2[n2++];
		while (true) {
			int comp = v1.compareTo(v2);
			if (comp < 0) {
				if (n1 == cnt1) {
					return false;
				}
				v1 = a1[n1++];
			}
			else if (comp > 0) {
				return false;
			}
			else {
				if (n2 == cnt2) {
					return true;
				}
				if (n1 == cnt1) {
					return false;
				}
				v1 = a1[n1++];
				v2 = a2[n2++];
			}
		}
	}
	
}
