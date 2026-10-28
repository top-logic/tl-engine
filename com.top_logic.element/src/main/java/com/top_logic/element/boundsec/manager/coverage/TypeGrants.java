/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The grants on a single type as an editor of them presents them: which role holds which
 * operation on the objects of the type and on their attributes, and what a change of a grant
 * stores.
 *
 * <p>
 * Only what has an effect is offered: the {@link #getRoles() roles} a user can hold on an object
 * of the type, and the {@link #getOperations() operations} applying to it. A role that is granted
 * an operation on the type but cannot be held there gets a row as well, marked
 * {@link #isIneffective(Row) ineffective}, so that its grant can be revoked. An attribute is offered
 * with the operations {@link SimpleBoundCommandGroup#READ} and {@link SimpleBoundCommandGroup#WRITE}
 * only, the operations an attribute grant decides.
 * </p>
 *
 * <p>
 * A grant is changed in the stored configuration through the {@link SecurityDefinitionEditor} and
 * takes effect when the stored definition is applied. Until then a {@link Cell} shows the state the
 * stored rules lead to, marked as {@link Cell#pending() pending} where it differs from the one in
 * effect. The states are computed from the rules of the underlying configuration layers and the
 * stored rules of the type and of its attributes; a stored change of a module or of a
 * generalization shows once it is applied. A change restoring what the underlying layers decide
 * stores no rule at all.
 * </p>
 *
 * <p>
 * A type with an {@link #getAccessParent() access parent} and a type that is
 * {@link #isExempt() exempt} from access control have no grants of their own: they have no rows.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeGrants {

	/**
	 * The operations an attribute grant decides.
	 */
	public static final List<BoundCommandGroup> ATTRIBUTE_OPERATIONS =
		List.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE);

	/**
	 * A row of the grants: a role, either on the objects of the type or on one of their attributes.
	 *
	 * @param attribute
	 *        The attribute the row is about, <code>null</code> for the objects of the type.
	 * @param role
	 *        The role the row is about.
	 */
	public record Row(TLStructuredTypePart attribute, BoundedRole role) {

		/**
		 * Creates a {@link Row}.
		 */
		public Row {
			Objects.requireNonNull(role);
		}

	}

	/**
	 * The state of a single grant: whether a role holds an operation.
	 *
	 * @param applies
	 *        Whether the operation can be granted in the row at all. All other properties are
	 *        <code>false</code> or empty for an operation that cannot.
	 * @param restricted
	 *        Whether the operation is decided by grants in the row. An attribute without a grant of
	 *        the operation is not restricted: the rights on the object decide. A row of the type
	 *        is always restricted.
	 * @param granted
	 *        Whether the role holds the operation, directly or through an implying operation.
	 * @param impliedBy
	 *        The operations on the type through which the role holds the operation, empty when it
	 *        holds it directly or not at all. A grant held through an implying operation cannot be
	 *        changed in the row of the implied operation.
	 * @param impliedByAttributes
	 *        The operations on attributes through which the role holds the operation, with the
	 *        attributes it holds them on: a grant on an attribute implies
	 *        {@link SimpleBoundCommandGroup#DISCOVER} on the object. Empty for any other operation.
	 *        Such a grant is changed on the attributes.
	 * @param pending
	 *        Whether the stored rules change the state in effect, so that the state shown takes
	 *        effect only when the stored definition is applied.
	 */
	public record Cell(boolean applies, boolean restricted, boolean granted, Set<BoundCommandGroup> impliedBy,
			Map<BoundCommandGroup, List<TLStructuredTypePart>> impliedByAttributes, boolean pending) {

		/** The {@link Cell} of an operation that cannot be granted in a row. */
		public static final Cell NOT_APPLICABLE =
			new Cell(false, false, false, Collections.emptySet(), Collections.emptyMap(), false);

		/**
		 * Whether the grant can be changed in its row.
		 */
		public boolean editable() {
			return applies && impliedBy.isEmpty() && impliedByAttributes.isEmpty();
		}

	}

	private final SecurityCoverageAnalysis _analysis;

	private final ModelAccessRights _rights;

	private final SecurityDefinitionEditor _editor;

	private final TLClass _type;

	private final List<BoundedRole> _roles;

	private final List<BoundedRole> _ineffectiveRoles;

	private final List<BoundCommandGroup> _operations;

	private final List<TLStructuredTypePart> _attributes;

	/** The stored rules of the type and of its attributes, by the name of their entry. */
	private final Map<String, List<AccessRule>> _storedRules = new HashMap<>();

	/**
	 * The rules of the underlying configuration layers, by the name of their entry, read on
	 * demand.
	 */
	private Map<String, List<AccessRule>> _baseRules;

	/**
	 * Creates {@link TypeGrants}.
	 *
	 * @param analysis
	 *        The analysis of the access definition in effect, answering which roles can be held on
	 *        the type and which places check an operation.
	 * @param rights
	 *        The grants in effect.
	 * @param editor
	 *        The editor storing changed grants.
	 * @param type
	 *        The type whose grants are edited.
	 */
	public TypeGrants(SecurityCoverageAnalysis analysis, ModelAccessRights rights, SecurityDefinitionEditor editor,
			TLClass type) {
		_analysis = Objects.requireNonNull(analysis);
		_rights = Objects.requireNonNull(rights);
		_editor = Objects.requireNonNull(editor);
		_type = Objects.requireNonNull(type);
		boolean ownGrants = getAccessParent() == null && !isExempt();
		_roles = ownGrants ? analysis.getHoldableRoles(type) : Collections.emptyList();
		_operations = ownGrants ? operations(rights.getOperations(type)) : Collections.emptyList();
		_ineffectiveRoles = ownGrants ? ineffectiveRoles() : Collections.emptyList();
		_attributes = ownGrants ? attributes(type) : Collections.emptyList();
	}

	/**
	 * The roles granted an operation on the type, in effect or by a stored rule, that a user cannot
	 * hold on it, ordered by their names.
	 */
	private List<BoundedRole> ineffectiveRoles() {
		Set<BoundedRole> granted = new HashSet<>();
		for (BoundCommandGroup operation : _operations) {
			granted.addAll(_analysis.getGrantedRoles(_type, operation));
		}
		try {
			for (AccessRule rule : storedRules(TLModelUtil.qualifiedName(_type))) {
				if (!(rule instanceof AccessRevoke)) {
					granted.addAll(roles(rule));
				}
			}
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("The stored access definition cannot be read.", ex);
		}
		granted.removeAll(_roles);
		List<BoundedRole> result = new ArrayList<>(granted);
		result.sort(Comparator.comparing(BoundedRole::getName));
		return Collections.unmodifiableList(result);
	}

	/**
	 * The operations applying to a type: the built-in operations in their order, followed by the
	 * declared ones ordered by their names.
	 */
	private static List<BoundCommandGroup> operations(ApplicableOperations applicable) {
		List<BoundCommandGroup> result = new ArrayList<>();
		for (BoundCommandGroup builtIn : ApplicableOperations.BUILT_IN) {
			if (applicable.appliesTo(builtIn)) {
				result.add(builtIn);
			}
		}
		List<BoundCommandGroup> declared = new ArrayList<>();
		for (BoundCommandGroup operation : applicable.getOperations()) {
			if (!ApplicableOperations.isBuiltIn(operation)) {
				declared.add(operation);
			}
		}
		declared.sort(Comparator.comparing(BoundCommandGroup::getID));
		result.addAll(declared);
		return Collections.unmodifiableList(result);
	}

	/**
	 * The attributes of the type that an attribute grant applies to, ordered by their names.
	 */
	private static List<TLStructuredTypePart> attributes(TLClass type) {
		List<TLStructuredTypePart> result = new ArrayList<>(type.getAllParts());
		result.sort(Comparator.comparing(TLStructuredTypePart::getName));
		return Collections.unmodifiableList(result);
	}

	/**
	 * The type whose grants these are.
	 */
	public TLClass getType() {
		return _type;
	}

	/**
	 * The access parent the type delegates to, <code>null</code> when the type decides for itself.
	 *
	 * <p>
	 * A type delegating to an access parent has no grants of its own.
	 * </p>
	 */
	public AccessParentFunction getAccessParent() {
		return _rights.getAccessParent(_type);
	}

	/**
	 * Whether the type needs no grants: it is excluded from access control, or it is used by the
	 * application's code only.
	 */
	public boolean isExempt() {
		return _rights.isWithoutSecurity(_type) || _rights.isInternal(_type);
	}

	/**
	 * The roles a user can hold on an object of the type.
	 *
	 * @see SecurityCoverageAnalysis#getHoldableRoles(TLClass)
	 */
	public List<BoundedRole> getRoles() {
		return _roles;
	}

	/**
	 * The roles granted an operation on the type that a user cannot hold on it: their grants
	 * never take effect.
	 *
	 * @see SecurityCoverageAnalysis#canHold(TLClass, BoundedRole)
	 */
	public List<BoundedRole> getIneffectiveRoles() {
		return _ineffectiveRoles;
	}

	/**
	 * Whether the given row is about a role that cannot be held on the type, so that its grants
	 * never take effect and are offered for revocation only.
	 */
	public boolean isIneffective(Row row) {
		return row.attribute() == null && _ineffectiveRoles.contains(row.role());
	}

	/**
	 * The operations applying to the type: the built-in operations first, the declared ones after
	 * them.
	 */
	public List<BoundCommandGroup> getOperations() {
		return _operations;
	}

	/**
	 * The attributes of the type, ordered by their names.
	 */
	public List<TLStructuredTypePart> getAttributes() {
		return _attributes;
	}

	/**
	 * All rows: one per {@link #getRoles() role} on the objects of the type, one per
	 * {@link #getIneffectiveRoles() ineffective role} on them, followed by one per role on each
	 * {@link #getAttributes() attribute}.
	 */
	public List<Row> getRows() {
		List<Row> result = new ArrayList<>();
		for (BoundedRole role : _roles) {
			result.add(new Row(null, role));
		}
		for (BoundedRole role : _ineffectiveRoles) {
			result.add(new Row(null, role));
		}
		for (TLStructuredTypePart attribute : _attributes) {
			for (BoundedRole role : _roles) {
				result.add(new Row(attribute, role));
			}
		}
		return result;
	}

	/**
	 * The places that may check the given operation on an object of the type.
	 *
	 * @see SecurityCoverageAnalysis#getUses(TLClass, BoundCommandGroup)
	 */
	public List<OperationUse> getUses(BoundCommandGroup operation) {
		return _analysis.getUses(_type, operation);
	}

	/**
	 * The state of the grant of the given operation in the given row.
	 *
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public Cell getCell(Row row, BoundCommandGroup operation) throws ConfigurationException {
		if (row.attribute() == null) {
			return typeCell(row.role(), operation);
		}
		return attributeCell(row.attribute(), row.role(), operation);
	}

	private Cell typeCell(BoundedRole role, BoundCommandGroup operation) throws ConfigurationException {
		if (!_operations.contains(operation)) {
			return Cell.NOT_APPLICABLE;
		}
		boolean direct = storedTypeRoles(operation).contains(role);
		Set<BoundCommandGroup> impliedBy = new LinkedHashSet<>();
		for (BoundCommandGroup implying : implying(operation)) {
			if (storedTypeRoles(implying).contains(role)) {
				impliedBy.add(implying);
			}
		}
		Map<BoundCommandGroup, List<TLStructuredTypePart>> impliedByAttributes = new LinkedHashMap<>();
		if (SimpleBoundCommandGroup.DISCOVER.equals(operation)) {
			// A grant on an attribute implies Discover on the object as well.
			for (BoundCommandGroup attributeOperation : ATTRIBUTE_OPERATIONS) {
				for (TLStructuredTypePart attribute : _attributes) {
					Set<BoundedRole> granted = storedAttributeRoles(attribute, attributeOperation);
					if (granted != null && granted.contains(role)) {
						impliedByAttributes.computeIfAbsent(attributeOperation, unused -> new ArrayList<>())
							.add(attribute);
					}
				}
			}
		}
		boolean granted = direct || !impliedBy.isEmpty() || !impliedByAttributes.isEmpty();
		boolean effective = _rights.getAllowedRoles(_type, operation).contains(role);
		return new Cell(true, true, granted, Collections.unmodifiableSet(impliedBy),
			Collections.unmodifiableMap(impliedByAttributes), granted != effective);
	}

	/**
	 * The operations a role holding one of them also holds the given one through.
	 *
	 * <p>
	 * {@link SimpleBoundCommandGroup#DISCOVER} is implied by every other operation on the type.
	 * </p>
	 */
	private List<BoundCommandGroup> implying(BoundCommandGroup operation) {
		if (SimpleBoundCommandGroup.DISCOVER.equals(operation)) {
			return _operations.stream().filter(other -> !other.equals(operation)).toList();
		}
		Set<BoundCommandGroup> implying = _rights.getOperations(_type).getImplying(operation);
		return _operations.stream()
			.filter(other -> !other.equals(operation) && implying.contains(other))
			.toList();
	}

	/**
	 * The roles granted the given operation on the type directly, once the stored rules of the
	 * type are applied.
	 *
	 * <p>
	 * The rules of the type's entry - those of the underlying configuration layers followed by
	 * the stored ones - are applied to what the type inherits from its generalizations and its
	 * module, as the {@link com.top_logic.model.security.SecurityConfigurationService} does when
	 * the stored definition is applied. The result therefore does not depend on which stored rules
	 * are in effect already.
	 * </p>
	 */
	private Set<BoundedRole> storedTypeRoles(BoundCommandGroup operation) throws ConfigurationException {
		Set<BoundedRole> roles = new HashSet<>(_rights.getInheritedRoles(_type, operation));
		roles = apply(roles, baseRules(typeEntry()), operation);
		roles = apply(roles, storedRules(typeEntry()), operation);
		return roles == null ? Collections.emptySet() : roles;
	}

	private String typeEntry() {
		return TLModelUtil.qualifiedName(_type);
	}

	private Cell attributeCell(TLStructuredTypePart attribute, BoundedRole role, BoundCommandGroup operation)
			throws ConfigurationException {
		if (!ATTRIBUTE_OPERATIONS.contains(operation)) {
			return Cell.NOT_APPLICABLE;
		}
		Set<BoundedRole> effective = effectiveAttributeRoles(attribute, operation);
		Set<BoundedRole> stored = storedAttributeRoles(attribute, operation);
		boolean restricted = stored != null;
		boolean granted = restricted && stored.contains(role);
		boolean pending = !Objects.equals(effective == null ? null : effective.contains(role),
			stored == null ? null : stored.contains(role));
		return new Cell(true, restricted, granted, Collections.emptySet(), Collections.emptyMap(), pending);
	}

	/**
	 * The roles granted the given operation on the given attribute in effect, <code>null</code>
	 * when the attribute is not restricted.
	 */
	private Set<BoundedRole> effectiveAttributeRoles(TLStructuredTypePart attribute, BoundCommandGroup operation) {
		if (!_rights.hasGrant(attribute, operation)) {
			return null;
		}
		return _rights.getAllowedRoles(attribute, operation);
	}

	/**
	 * The roles granted the given operation on the given attribute once the stored rules of the
	 * attribute are applied, <code>null</code> when the attribute is not restricted then.
	 *
	 * <p>
	 * The rules of the underlying configuration layers and the stored ones are applied in order,
	 * as for the {@link #storedTypeRoles(BoundCommandGroup) type}; an attribute inherits nothing.
	 * </p>
	 */
	private Set<BoundedRole> storedAttributeRoles(TLStructuredTypePart attribute, BoundCommandGroup operation)
			throws ConfigurationException {
		String entry = SecurityDefinitionEditor.entryName(attribute);
		return apply(apply(null, baseRules(entry), operation), storedRules(entry), operation);
	}

	/**
	 * The rules the underlying configuration layers define for the entry of the given name.
	 */
	private List<AccessRule> baseRules(String name) throws ConfigurationException {
		if (_baseRules == null) {
			_baseRules = _editor.baseRules();
		}
		return _baseRules.getOrDefault(name, Collections.emptyList());
	}

	/**
	 * Applies the given rules of the given operation to the given roles, as the
	 * {@link com.top_logic.model.security.SecurityConfigurationService} applies a rule sequence.
	 *
	 * @param roles
	 *        The roles granted the operation before, <code>null</code> when it is not granted at
	 *        all. Modified.
	 * @return The roles granted the operation after the rules, <code>null</code> when a revocation
	 *         without roles dropped the operation.
	 */
	private static Set<BoundedRole> apply(Set<BoundedRole> roles, List<AccessRule> rules,
			BoundCommandGroup operation) {
		Set<BoundedRole> result = roles;
		for (AccessRule rule : rules) {
			if (rule.getOperation() == null || !operation.getID().equals(rule.getOperation().id())) {
				continue;
			}
			Set<BoundedRole> ruleRoles = roles(rule);
			if (!(rule instanceof AccessRevoke)) {
				if (result == null) {
					result = new HashSet<>();
				}
				result.addAll(ruleRoles);
			} else if (rule.getRoles().isEmpty()) {
				result = null;
			} else if (result != null) {
				result.removeAll(ruleRoles);
			}
		}
		return result;
	}

	private static Set<BoundedRole> roles(AccessRule rule) {
		Set<BoundedRole> result = new HashSet<>();
		for (RoleConfig config : rule.getRoles()) {
			BoundedRole role = BoundedRole.getRoleByName(config.getName());
			if (role != null) {
				result.add(role);
			}
		}
		return result;
	}

	private List<AccessRule> storedRules(String name) throws ConfigurationException {
		List<AccessRule> result = _storedRules.get(name);
		if (result == null) {
			result = _editor.storedRules(name);
			_storedRules.put(name, result);
		}
		return result;
	}

	/**
	 * Grants the given operation in the given row, or revokes it.
	 *
	 * <p>
	 * The change is stored and takes effect when the stored definition is applied. A role holding
	 * the operation through an implying one keeps it: such a grant is changed on the implying
	 * operation. Revoking an operation from the last role it is granted to on an attribute lifts
	 * the restriction of the attribute, so that the rights on the object decide again, instead of
	 * denying the attribute to everybody.
	 * </p>
	 *
	 * @param row
	 *        The row of the grant.
	 * @param operation
	 *        The operation, applying in the row.
	 * @param granted
	 *        Whether to grant the operation.
	 * @throws IOException
	 *         When the stored configuration cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void setGranted(Row row, BoundCommandGroup operation, boolean granted)
			throws IOException, ConfigurationException {
		Cell cell = getCell(row, operation);
		if (!cell.editable()) {
			throw new IllegalArgumentException("The grant of " + operation.getID() + " to " + row.role().getName()
				+ " cannot be changed on " + TLModelUtil.qualifiedName(_type) + ".");
		}
		TLStructuredTypePart attribute = row.attribute();
		BoundedRole role = row.role();
		// What the underlying configuration layers decide is not repeated by a stored rule.
		if (attribute == null) {
			_editor.dropStoredGrant(_type, operation, role);
			_storedRules.clear();
			if (storedTypeRoles(operation).contains(role) != granted) {
				_editor.setGranted(_type, operation, role, granted);
			}
		} else if (!granted && isLastRole(attribute, role, operation)) {
			_editor.dropStoredRules(attribute, operation);
			_storedRules.clear();
			if (storedAttributeRoles(attribute, operation) != null) {
				_editor.dropAttributeGrants(attribute, operation);
			}
		} else {
			_editor.dropStoredGrant(attribute, operation, role);
			_storedRules.clear();
			Set<BoundedRole> remaining = storedAttributeRoles(attribute, operation);
			if ((remaining != null && remaining.contains(role)) != granted) {
				_editor.setGranted(attribute, operation, role, granted);
			}
		}
		_storedRules.clear();
	}

	private boolean isLastRole(TLStructuredTypePart attribute, BoundedRole role, BoundCommandGroup operation)
			throws ConfigurationException {
		Set<BoundedRole> stored = storedAttributeRoles(attribute, operation);
		return stored != null && stored.size() == 1 && stored.contains(role);
	}

}
