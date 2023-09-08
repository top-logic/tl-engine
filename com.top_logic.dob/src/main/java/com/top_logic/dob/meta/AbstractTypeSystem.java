/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.HashSet;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;

/**
 * Implementation of {@link TypeSystem} API that does not depend on the internal
 * storage of types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTypeSystem implements TypeSystem {

	@Override
	public MetaObject getUnionType(MetaObject t1, MetaObject t2) {
		final Kind t2Kind = t2.getKind();
		final Kind t1Kind = t1.getKind();

		if (t1Kind == Kind.INVALID || t2Kind == Kind.INVALID) {
			return MetaObject.INVALID_TYPE;
		}
		
		switch (t1Kind) {
			case ANY:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return MetaObject.ANY_TYPE;
					default:
						return MetaObject.ANY_TYPE;
				}
			case NULL:
				switch (t2Kind) {
					case ANY:
						return MetaObject.ANY_TYPE;
					case NULL:
						return MetaObject.NULL_TYPE;
					default:
						return t2;
				}
			case alternative:
				switch (t2Kind) {
					case ANY:
						return MetaObject.ANY_TYPE;
					case NULL:
						return t1;
					case alternative:
						return equalsAny(t1, t2);
					case item:
					case primitive:
					case struct:
					case collection:
					case tuple:
					case function: 
					default:
						if (t2.isSubtypeOf(t1)) {
							return t1;
						}
						return MetaObject.ANY_TYPE;
				}
			case item:
				switch (t2Kind) {
					case ANY:
						return MetaObject.ANY_TYPE;
					case NULL:
						return t1;
					case alternative:
						if (t1.isSubtypeOf(t2)) {
							return t2;
						}
						return MetaObject.ANY_TYPE;
					case item:
						if (t1.isSubtypeOf(t2)) {
							return t2;
						}
						if (t2.isSubtypeOf(t1)) {
							return t1;
						}
						return getCommonSupertype((MOClass) t1, (MOClass) t2);
					case primitive:
					case struct:
					case collection:
					case tuple:
					case function: 
					default:
						return MetaObject.ANY_TYPE;
				}
			case primitive:
			case struct:
			case collection:
			case tuple: 
			case function: 
			default:
				switch (t2Kind) {
					case ANY:
						return MetaObject.ANY_TYPE;
					case NULL:
						return t1;
					case alternative:
						if (t1.isSubtypeOf(t2)) {
							return t2;
						}
						return MetaObject.ANY_TYPE;
					case item:
						return MetaObject.ANY_TYPE;
					case primitive:
					case struct:
					case collection:
					case tuple:
					case function: 
					default:
						return equalsAny(t1, t2);
				}
		}
	}

	private MetaObject equalsAny(MetaObject t1, MetaObject t2) {
		return t1.equals(t2) ? t1 : MetaObject.ANY_TYPE;
	}

	@Override
	public MetaObject getIntersectionType(MetaObject t1, MetaObject t2) {
		final Kind t1Kind = t1.getKind();
		final Kind t2Kind = t2.getKind();
		
		if (t1Kind == Kind.INVALID || t2Kind == Kind.INVALID) {
			return MetaObject.INVALID_TYPE;
		}
		
		switch (t1Kind) {
			case ANY:
				switch (t2Kind) {
					case ANY: 
						return MetaObject.ANY_TYPE;
					case NULL:
						return MetaObject.NULL_TYPE;
					default:
						return t2;
				}
			case NULL: {
				switch (t2Kind) {
					case ANY: 
					case NULL:
						return MetaObject.NULL_TYPE;
					case item:
					case primitive:
						return MetaObject.NULL_TYPE;
					default:
						return MetaObject.INVALID_TYPE;
				}
			}
			case alternative: {
				switch (t2Kind) {
					case ANY:
						return t1;
					case NULL:
						return MetaObject.NULL_TYPE;
					case alternative:
						return equalsInvalid(t1, t2);
					case item:
						if (t2.isSubtypeOf(t1)) {
							return t2;
						}
						return MetaObject.INVALID_TYPE;
					default:
						return MetaObject.INVALID_TYPE;
				}
			}
			case item: {
				switch (t2Kind) {
					case ANY:
						return t1;
					case NULL:
						return MetaObject.NULL_TYPE;
					case alternative:
						if (t1.isSubtypeOf(t2)) {
							return t1;
						}
						return MetaObject.INVALID_TYPE;
					case item:
						if (t1.isSubtypeOf(t2)) {
							return t1;
						}
						if (t2.isSubtypeOf(t1)) {
							return t2;
						}
						return MetaObject.INVALID_TYPE;
					default:
						return MetaObject.INVALID_TYPE;
				}
			}
			case primitive:
			case struct:
			case collection:
			case tuple:
			case function:
			default:
				switch (t2Kind) {
					case ANY:
						return t1;
					case NULL:
						if (t1Kind == Kind.primitive) {
							return MetaObject.NULL_TYPE;
						} else {
							return MetaObject.INVALID_TYPE;
						}
					case alternative:
						if (t1.isSubtypeOf(t2)) {
							return t1;
						}
						return MetaObject.INVALID_TYPE;
					default:
						return equalsInvalid(t1, t2);
				}
		}
	}

	private MetaObject equalsInvalid(MetaObject t1, MetaObject t2) {
		return t1.equals(t2) ? t1 : MetaObject.INVALID_TYPE;
	}

	private MetaObject getCommonSupertype(MOClass t1, MOClass t2) {
		if (t1.equals(t2)) {
			// Optimization for the trivial case.
			return t1;
		}
		
		HashSet<MOClass> t1Superclasses = new HashSet<>();
		while (t1 != null) {
			t1Superclasses.add(t1);
			t1 = t1.getSuperclass();
		}
		
		while (t2 != null) {
			if (t1Superclasses.contains(t2)) {
				return t2;
			}
			t2 = t2.getSuperclass();
		}
		
		throw new UnreachableAssertion("Classes without common super class: " + t1 + ", " + t2);
	}

	@Override
	public boolean hasCommonInstances(MetaObject t1, MetaObject t2) {
		final Kind t1Kind = t1.getKind();
		final Kind t2Kind = t2.getKind();
		
		if (t1 == t2) {
			return true;
		}

		if (t1Kind == Kind.INVALID || t2Kind == Kind.INVALID) {
			return false;
		}

		switch (t1Kind) {
			case ANY:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return true;
					default:
						return true;
				}
			case NULL:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return true;
					case primitive:
					case alternative:
					case item:
						return true;
					default:
						return false;
				}
			case collection:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return true;
					case collection:
						MOCollection t1Collection = (MOCollection) t1;
						MOCollection t2Collection = (MOCollection) t2;

						String t1Raw = t1Collection.getRawType();
						String t2Raw = t2Collection.getRawType();
						if (t1Raw.equals(t2Raw) || t1Raw.equals(MOCollection.COLLECTION) || t2Raw.equals(MOCollection.COLLECTION)) {
							MetaObject t1ElementType = t1Collection.getElementType();
							MetaObject t2ElementType = t2Collection.getElementType();
							return t1ElementType.equals(t2ElementType) || t1ElementType.getKind() == Kind.ANY || t2ElementType.getKind() == Kind.ANY;
						} else {
							return false;
						}
					default:
						return false;
				}
			case alternative:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return true;
					case alternative:
					case item:
						return commonInstances(t2, (MOAlternative) t1);
					default:
						return false;
				}
			case item:
				switch (t2Kind) {
					case ANY:
					case NULL:
						return true;
					case alternative:
						return commonInstances(t1, (MOAlternative) t2);
					case item:
						return isSubtypeOfEachOther(t1, t2);
					default:
						return false;
				}
			case primitive:
			case struct:
			case tuple:
			default:
				switch (t2Kind) {
					case ANY:
						return true;
					case NULL:
						return t1Kind == Kind.primitive;
					default:
						return t1.equals(t2);
				}
		}
	}

	private boolean commonInstances(MetaObject t1, MOAlternative t2) {
		for (MetaObject specialisation : t2.getSpecialisations()) {
			if (hasCommonInstances(specialisation, t1)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSubtypeOfEachOther(MetaObject t1, MetaObject t2) {
		return t1.isSubtypeOf(t2) || t2.isSubtypeOf(t1);
	}

	@Override
	public boolean isAssignmentCompatible(MetaObject expectedType, MetaObject givenType) {
		// if one of the given MO types is invalid, return true to avoid follow up errors in
		// surrounding type checks
		if (expectedType.getKind() == Kind.INVALID || givenType.getKind() == Kind.INVALID ) {
			return true;
		}
		return givenType.isSubtypeOf(expectedType);
	}

	@Override
	public boolean isComparableTo(MetaObject t1, MetaObject t2) {
		final Kind t1Kind = t1.getKind();
		
		if (t1Kind == Kind.INVALID || t2.getKind() == Kind.INVALID) {
			return false;
		}

		switch (t1Kind) {
			case ANY:
				return false;
			case NULL:
			case primitive:
			case tuple:
				return t1.equals(t2);
			case collection:
			case struct:
			case item:
			case function:
			default:
				return false;
		}
	}

}
