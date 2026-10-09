/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * The checks {@link ConfigFormModel#apply()} runs before a working copy is allowed to replace the
 * original, and the reporting that puts each failure on the field that caused it.
 *
 * <p>
 * Two kinds of violation are collected, from two different places. A mandatory property with no
 * value is not something {@link ConstraintChecker} reports - the configuration framework only
 * catches that when it reads XML, and nothing here reads XML - so it is checked directly via
 * {@link PropertyDescriptor#isMandatory()}, {@link ConfigurationItem#valueSet(PropertyDescriptor)},
 * and the property's current value: {@link ConfigurationItem#update(PropertyDescriptor, Object)}
 * marks a property as set the moment it is called, whatever value it was called with - so a field
 * the user cleared back to {@code null} or {@code ""} reads as "set" too, and {@code valueSet}
 * alone cannot tell that apart from a value the user actually entered. A constraint violation comes
 * from {@link ConstraintChecker}, which is itself recursive and already names, for every failure,
 * the exact item and property {@link ConfigFieldIndex} is keyed by.
 * </p>
 *
 * <p>
 * Not everything found stands in the way. A constraint declared
 * {@link com.top_logic.basic.config.constraint.annotation.Constraint#asWarning() as a warning} says
 * something about a value without refusing it, and is collected as a {@link Warning} next to the
 * blocking {@link Violation}s: shown at the field it is about, never a reason to refuse
 * {@link ConfigFormModel#apply()}.
 * </p>
 *
 * <p>
 * {@link #check(ConfigurationItem)} is meant to run on the edited part of a configuration, not on
 * the copied root above it: only the edited part was actually changed by the user, and a violation
 * elsewhere in the tree predates the edit and has no field on screen to show it. A constraint that
 * navigates upward out of the edited part still works, because the copy keeps the root above it in
 * place.
 * </p>
 */
public final class ConfigValidation {

	/**
	 * One reason a configuration may not be applied, and where it belongs.
	 *
	 * @param item
	 *        The configuration item the violation was found on.
	 * @param property
	 *        The property of {@link #item()} the violation belongs to.
	 * @param message
	 *        The message describing the violation.
	 * @param missing
	 *        Whether the violation is a mandatory value not given, rather than a value given that
	 *        a constraint rejects.
	 */
	public record Violation(ConfigurationItem item, PropertyDescriptor property, ResKey message, boolean missing) {

		/**
		 * Creates a {@link Violation} of a value given that a constraint rejects.
		 */
		public Violation(ConfigurationItem item, PropertyDescriptor property, ResKey message) {
			this(item, property, message, false);
		}
	}

	/**
	 * One remark about a value that does not stand in the way of applying it, and where it belongs.
	 *
	 * <p>
	 * The same three components a {@link Violation} carries, and a type of its own all the same,
	 * because what the two are for could not be further apart: a {@link Violation} refuses
	 * {@link ConfigFormModel#apply()} and is listed in the {@link Refusal}, a {@link Warning} is
	 * shown next to the value and refuses nothing. A single record with a flag to tell them apart
	 * would leave every list of them to be read twice - once for what blocks and once for what does
	 * not - and one forgotten check would turn a remark into a refusal.
	 * </p>
	 *
	 * @param item
	 *        The configuration item the warning was found on.
	 * @param property
	 *        The property of {@link #item()} the warning belongs to.
	 * @param message
	 *        The message describing what is worth a second look.
	 */
	public record Warning(ConfigurationItem item, PropertyDescriptor property, ResKey message) {
		// Nothing beyond the components.
	}

	/**
	 * Everything {@link #check(ConfigurationItem)} found about a configuration.
	 *
	 * @param violations
	 *        Why the configuration may not be applied, in no particular order. Empty if it may be
	 *        applied as it stands.
	 * @param warnings
	 *        What is worth saying about it anyway, in no particular order. Never a reason to refuse
	 *        anything.
	 */
	public record Findings(List<Violation> violations, List<Warning> warnings) {
		// Nothing beyond the components.
	}

	private ConfigValidation() {
		// Static use only.
	}

	/**
	 * Checks the given configuration item, and everything reachable from it, both for violations
	 * that must block {@link ConfigFormModel#apply()} and for warnings that must not.
	 *
	 * @param item
	 *        The edited part of the configuration to check.
	 * @return What was found. {@link Findings#violations()} empty means the item may be applied as
	 *         it stands, whatever {@link Findings#warnings()} holds.
	 */
	public static Findings check(ConfigurationItem item) {
		List<Violation> violations = new ArrayList<>();
		List<Warning> warnings = new ArrayList<>();
		collectMissingMandatory(item, violations, Collections.newSetFromMap(new IdentityHashMap<>()));
		collectConstraintFailures(item, violations, warnings);
		return new Findings(violations, warnings);
	}

	/**
	 * Puts every finding on the field that {@link ConfigFieldIndex#register(ConfigurationItem,
	 * PropertyDescriptor, ConfigFieldModel) registered} for its item and property.
	 *
	 * @param findings
	 *        What to report, typically {@link #check(ConfigurationItem)}'s result.
	 * @param index
	 *        The index the editor filled while building its fields.
	 * <p>
	 * Reported through {@link ConfigFieldModel#setModelValidationError(ResKey)}, not
	 * {@link ConfigFieldModel#setError(ResKey)}: a violation is a verdict on the configuration, and
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#getInputError() the input error} is
	 * documented to be the other thing entirely - "not produced by a constraint but by the input
	 * control", the record that the field could not read what was typed into it. Sharing one slot
	 * would make the two indistinguishable afterwards, and they must be told apart: a violation is
	 * taken back before every re-check (see {@link ConfigFieldIndex#clearFindings()}), while a
	 * rejected input is the very thing that must survive to keep Apply from discarding it.
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#setRevealed(boolean) Revealing} the
	 * field goes with it, since pressing Apply is exactly the "attempt to submit" that makes a
	 * model-level verdict visible.
	 * </p>
	 *
	 * <p>
	 * A {@link Warning} goes to the field's separate warning channel,
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#setModelValidationWarnings(List)},
	 * which holds the field's complete list rather than a single message - so all warnings about
	 * one field are placed in one call, see {@link #warningsByField(List, ConfigFieldIndex)}.
	 * Revealing the field goes with it for the same reason it does for a violation: the field is
	 * where the remark belongs, and Apply is when it is asked for.
	 * </p>
	 *
	 * @return Whether every {@link Findings#violations() violation} found a field to carry it.
	 *         {@code false} if at least one violation named a property the editor does not render,
	 *         and was therefore not placed anywhere the user can see. The
	 *         {@link Findings#warnings() warnings} have no say in this answer: the caller reads it
	 *         to decide whether a refusal must name what it could not show, and a warning refuses
	 *         nothing - one that found no field is simply not shown.
	 */
	public static boolean report(Findings findings, ConfigFieldIndex index) {
		return report(findings, index, true);
	}

	/**
	 * Puts the given findings on the fields that caused them, revealing a missing value or not.
	 *
	 * @param revealMissing
	 *        Whether to {@link com.top_logic.layout.form.model.AbstractFieldModel#setRevealed(boolean)
	 *        reveal} the field of a {@link Violation#missing() missing} value. Without, such a
	 *        finding is on display only at a field already revealed - one the user has changed, or
	 *        one a refusal has revealed. A value given that a constraint rejects is revealed either
	 *        way: it is a verdict on what is there.
	 *
	 * @see #report(Findings, ConfigFieldIndex)
	 */
	public static boolean report(Findings findings, ConfigFieldIndex index, boolean revealMissing) {
		boolean complete = true;
		for (Violation violation : findings.violations()) {
			ConfigFieldModel field = index.lookup(violation.item(), violation.property());
			if (field == null) {
				complete = false;
			} else {
				field.setModelValidationError(violation.message());
				if (revealMissing || !violation.missing()) {
					field.setRevealed(true);
				}
			}
		}
		Map<ConfigFieldModel, List<ResKey>> warningsByField = warningsByField(findings.warnings(), index);
		for (Map.Entry<ConfigFieldModel, List<ResKey>> entry : warningsByField.entrySet()) {
			ConfigFieldModel field = entry.getKey();
			field.setModelValidationWarnings(entry.getValue());
			field.setRevealed(true);
		}
		return complete;
	}

	/**
	 * Collects the messages of the given warnings per field that displays them, dropping those the
	 * editor renders no field for.
	 *
	 * <p>
	 * Grouped rather than placed one at a time because
	 * {@link com.top_logic.layout.form.model.AbstractFieldModel#setModelValidationWarnings(List)}
	 * replaces the field's whole list: two warnings about the same property - two constraints on it,
	 * or one constraint reporting on it from either end - would otherwise leave only the last one on
	 * screen. A {@link LinkedHashMap} keeps the order the checker found them in, so what is read
	 * under the field does not shuffle from one check to the next; keying it by the field is sound
	 * because a {@link ConfigFieldModel} is compared by identity, which is also what
	 * {@link ConfigFieldIndex} relies on.
	 * </p>
	 */
	private static Map<ConfigFieldModel, List<ResKey>> warningsByField(List<Warning> warnings,
			ConfigFieldIndex index) {
		Map<ConfigFieldModel, List<ResKey>> byField = new LinkedHashMap<>();
		for (Warning warning : warnings) {
			ConfigFieldModel field = index.lookup(warning.item(), warning.property());
			if (field != null) {
				byField.computeIfAbsent(field, any -> new ArrayList<>()).add(warning.message());
			}
		}
		return byField;
	}

	/**
	 * Why an edited configuration may not be handed over yet, or {@code null} if nothing stands in
	 * the way.
	 *
	 * @param message
	 *        What to tell the user.
	 * @param details
	 *        The individual violations behind {@code message}, empty where it stands on its own.
	 */
	public record Refusal(ResKey message, List<ResKey> details) {
		// Fields only.
	}

	/**
	 * Checks an edited configuration again and shows what the check found, without refusing
	 * anything.
	 *
	 * <p>
	 * What a form runs while the user is still editing, so a value is questioned where it is
	 * entered rather than only once Apply is pressed. Takes back the previous findings first, via
	 * {@link ConfigFieldIndex#clearFindings()}, so what is on display is what holds right now: a
	 * finding that still holds is placed again, one the user has meanwhile fixed - possibly by
	 * editing the other end of a cross-item constraint - is gone.
	 * </p>
	 *
	 * <p>
	 * Deliberately blind to the two things {@link #refusalFor(Iterable, ConfigFieldIndex)} refuses
	 * over. An {@link ConfigFieldIndex#pending() unconfirmed entry} is marked as one to confirm or
	 * discard only when the user asks for the configuration to be handed over - while editing, an
	 * entry that was just started is exactly where it is supposed to be, and telling the user off
	 * for it is telling them off for having begun. An
	 * {@link ConfigFieldIndex#hasInputError() unreadable input} keeps Apply from discarding what was
	 * typed, which is nothing this method is in a position to discard; the field that rejected the
	 * input already says so on its own, and the configuration behind it still holds the last value
	 * the field accepted, which is what is checked here.
	 * </p>
	 *
	 * @param edited
	 *        The configuration to check.
	 * @param index
	 *        The fields the configuration is rendered in, to report on.
	 * @return The violations placed, empty if the configuration may be handed over as it stands.
	 *         The {@link Findings#warnings() warnings} are on their fields either way and are not
	 *         part of this answer, since they refuse nothing.
	 */
	public static List<Violation> recheck(ConfigurationItem edited, ConfigFieldIndex index) {
		return recheck(Collections.singletonList(edited), index);
	}

	/**
	 * The same while the user is still editing: a mandatory value not given is put on its field
	 * without revealing it.
	 *
	 * <p>
	 * A field the user has not touched yet - one of an entry just added, say - must not turn red
	 * before the user had a chance to fill it: its finding becomes visible once the user changes
	 * the field, or once a refusal reveals it. A value a constraint rejects is shown at once, at
	 * either end of the constraint, since it is a verdict on a value that is there.
	 * </p>
	 *
	 * @see #recheck(ConfigurationItem, ConfigFieldIndex)
	 */
	public static List<Violation> recheckWhileEditing(ConfigurationItem edited, ConfigFieldIndex index) {
		return recheckWhileEditing(edited, index, Collections.emptySet());
	}

	/**
	 * The same, with properties of the edited item the user interface requires in addition.
	 *
	 * @param mandatory
	 *        Properties of the edited item that must have a value although they do not declare
	 *        it, see {@link FieldDisplay#mandatory()}.
	 *
	 * @see #recheckWhileEditing(ConfigurationItem, ConfigFieldIndex)
	 */
	public static List<Violation> recheckWhileEditing(ConfigurationItem edited, ConfigFieldIndex index,
			Set<PropertyDescriptor> mandatory) {
		return recheck(Collections.singletonList(edited), index, false, mandatory);
	}

	/**
	 * The same for several configurations checked as one, where what is edited is a collection
	 * rather than a single item.
	 *
	 * @see #recheck(ConfigurationItem, ConfigFieldIndex)
	 * @see #refusalFor(Iterable, ConfigFieldIndex)
	 */
	public static List<Violation> recheck(Iterable<? extends ConfigurationItem> edited, ConfigFieldIndex index) {
		return recheck(edited, index, true, Collections.emptySet());
	}

	/**
	 * @param mandatory
	 *        Properties of the edited items that must have a value although they do not declare
	 *        it, see {@link FieldDisplay#mandatory()}.
	 */
	private static List<Violation> recheck(Iterable<? extends ConfigurationItem> edited, ConfigFieldIndex index,
			boolean revealMissing, Set<PropertyDescriptor> mandatory) {
		index.clearFindings();

		List<Violation> violations = new ArrayList<>();
		List<Warning> warnings = new ArrayList<>();
		for (ConfigurationItem item : edited) {
			Findings findings = check(item);
			violations.addAll(findings.violations());
			warnings.addAll(findings.warnings());
			addRequiredByDisplay(item, mandatory, violations);
		}
		// Reported whatever comes of it: a warning is shown at its field and refuses nothing, so a
		// configuration whose only finding is a warning is handed over with the warning on display
		// until the form is rebuilt over the applied value.
		report(new Findings(violations, warnings), index, revealMissing);
		return violations;
	}

	/**
	 * Runs every check that stands between an edited configuration and being handed over, and puts
	 * what it finds on the fields that caused it.
	 *
	 * <p>
	 * One method rather than a sequence each caller repeats, because there are two callers with
	 * nothing else in common - a standalone {@link ConfigFormControl} with its own Apply, and a
	 * configuration edited as one field of a surrounding form - and a rule enforced in only one of
	 * them is worse than no rule at all: the same configuration would be refused or accepted
	 * depending on where it is being edited.
	 * </p>
	 *
	 * <p>
	 * The order matters. An unconfirmed entry whose key is already spoken for carries an input
	 * error of its own, and "an entry could not be read" would then be said about a key that reads
	 * perfectly well and is merely taken.
	 * </p>
	 *
	 * <p>
	 * The check itself, and the placing of what it found, is
	 * {@link #recheck(Iterable, ConfigFieldIndex)} - the very step a form runs while the user is
	 * still editing. What this adds is only what handing the configuration over may refuse over:
	 * an unconfirmed entry, an unreadable input, and the refusal a violation amounts to.
	 * </p>
	 *
	 * @param edited
	 *        The configuration to check.
	 * @param index
	 *        The fields the configuration is rendered in, both to report on and to ask about
	 *        unreadable input and unconfirmed entries.
	 * @return Why the configuration may not be handed over, or {@code null} if nothing stands in the
	 *         way. {@code null} is not "nothing was found": the warnings are placed on their fields
	 *         either way, and only the violations decide the answer.
	 */
	public static Refusal refusalFor(ConfigurationItem edited, ConfigFieldIndex index) {
		return refusalFor(Collections.singletonList(edited), index);
	}

	/**
	 * The same, with properties of the edited item the user interface requires in addition.
	 *
	 * @param mandatory
	 *        Properties of the edited item that must have a value although they do not declare
	 *        it, see {@link FieldDisplay#mandatory()}.
	 *
	 * @see #refusalFor(ConfigurationItem, ConfigFieldIndex)
	 */
	public static Refusal refusalFor(ConfigurationItem edited, ConfigFieldIndex index,
			Set<PropertyDescriptor> mandatory) {
		return refusalFor(Collections.singletonList(edited), index, mandatory);
	}

	/**
	 * The same for several configurations checked as one, where what is edited is a collection
	 * rather than a single item.
	 *
	 * <p>
	 * Each entry is checked on its own rather than the collection's owner being checked as a whole:
	 * where the owner exists only to hold them - the container built around an annotation, say - it
	 * carries requirements of its own that nobody is editing here, and a form must not be blocked
	 * by a value it does not offer a field for.
	 * </p>
	 */
	public static Refusal refusalFor(Iterable<? extends ConfigurationItem> edited, ConfigFieldIndex index) {
		return refusalFor(edited, index, Collections.emptySet());
	}

	private static Refusal refusalFor(Iterable<? extends ConfigurationItem> edited, ConfigFieldIndex index,
			Set<PropertyDescriptor> mandatory) {
		// Also cleared here, not only in the recheck further down: the two refusals in between
		// never reach it, and a finding the previous attempt placed must not outlive them either.
		index.clearFindings();

		List<ConfigPendingEntries.PendingEntry> pending = index.pending();
		if (!pending.isEmpty()) {
			for (ConfigPendingEntries.PendingEntry entry : pending) {
				entry.setKeyFieldError(I18NConstants.ERROR_CONFIRM_OR_DISCARD_ENTRY);
			}
			return new Refusal(I18NConstants.ERROR_ENTRY_NOT_CONFIRMED, Collections.emptyList());
		}
		if (index.hasInputError()) {
			return new Refusal(I18NConstants.ERROR_INPUT_NOT_READABLE, Collections.emptyList());
		}
		List<Violation> violations = recheck(edited, index, true, mandatory);
		if (!violations.isEmpty()) {
			// Every violation is listed, not only those that found no field: the fields are spread
			// over a form taller than the screen, and the list is what says how many there are and
			// what they are without hunting for them. Only the violations: the Refusal is why the
			// configuration was not handed over, and a warning is not part of that answer.
			return new Refusal(I18NConstants.ERROR_CANNOT_APPLY,
				violations.stream().map(Violation::message).toList());
		}
		return null;
	}

	/**
	 * Adds a {@link Violation} for every property of the given item the user interface requires
	 * that has no value, unless a violation of it was found already.
	 *
	 * <p>
	 * A value is missing if it is <code>null</code>, an empty text, or an empty collection: the
	 * user interface asks for a value, and an empty list has no entry.
	 * </p>
	 */
	private static void addRequiredByDisplay(ConfigurationItem item, Set<PropertyDescriptor> mandatory,
			List<Violation> violations) {
		for (PropertyDescriptor property : mandatory) {
			if (item.descriptor().getProperty(property.getPropertyName()) != property) {
				// A property of another item.
				continue;
			}
			if (!isEmpty(item.value(property))) {
				continue;
			}
			boolean found = violations.stream()
				.anyMatch(violation -> violation.item() == item && violation.property() == property);
			if (!found) {
				violations.add(new Violation(item, property,
					I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(property, false)), true));
			}
		}
	}

	/**
	 * Whether the given value is no value: <code>null</code>, an empty text, or an empty
	 * collection.
	 */
	private static boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof String text) {
			return text.isEmpty();
		}
		if (value instanceof Collection<?> collection) {
			return collection.isEmpty();
		}
		if (value instanceof Map<?, ?> map) {
			return map.isEmpty();
		}
		if (value.getClass().isArray()) {
			return Array.getLength(value) == 0;
		}
		return false;
	}

	/**
	 * Walks the given item and every nested item and collection entry reachable through its ITEM,
	 * LIST, ARRAY and MAP properties, adding a {@link Violation} for every mandatory property with
	 * no value.
	 *
	 * @param visited
	 *        Items already walked, guarding against revisiting one reachable twice and against a
	 *        cycle that would otherwise never terminate.
	 */
	private static void collectMissingMandatory(ConfigurationItem item, List<Violation> violations,
			Set<ConfigurationItem> visited) {
		if (item == null || !visited.add(item)) {
			return;
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isMandatory() && isMissing(item, property)) {
				violations.add(new Violation(item, property,
					I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY.fill(Labels.propertyLabel(property, false)), true));
			}
			descendMissingMandatory(item, property, violations, visited);
		}
	}

	/**
	 * Whether the given property of the given item has no value worth keeping, for the purpose of a
	 * {@link PropertyDescriptor#isMandatory() @Mandatory} check.
	 *
	 * <p>
	 * {@link ConfigurationItem#valueSet(PropertyDescriptor)} alone is not enough: it answers
	 * whether {@link ConfigurationItem#update(PropertyDescriptor, Object)} was ever called for the
	 * property, not whether the value it left behind is actually usable. A field the user cleared -
	 * typed over what was there and left it blank - calls {@code update} with {@code null} (or, for
	 * a {@link String} property, whatever the framework normalizes that {@code null} to, typically
	 * {@code ""}) just as much as a field the user filled in calls it with the entered value;
	 * {@code valueSet} reads "set" either way. So this also rejects a {@code null} value outright,
	 * and an empty {@link String} - the only type whose "empty" value is not itself {@code null}.
	 * </p>
	 *
	 * <p>
	 * Deliberately narrow: only a {@code null} value or an empty {@link String} count as missing,
	 * and only for a property this editor actually renders as a field, or as a type selector. A
	 * {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, {@link PropertyKind#MAP}, or monomorphic
	 * {@link PropertyKind#ITEM} property is never flagged here, whatever it holds. The first three
	 * mirror
	 * {@link ConfigFieldModel#isTechnicallyMandatory(PropertyDescriptor)}, which excludes exactly
	 * those kinds because they are "not nullable, but may be empty" - the same rule the classic
	 * declarative form applies. Two reasons this method keeps step with that rule rather than
	 * refusing an empty mandatory collection: it would enforce a requirement the field layer itself
	 * does not, and a collection property has no {@link ConfigFieldModel} of its own - it is
	 * rendered by {@link ConfigListEditorControl}, not as a field - so
	 * {@link #report(Findings, ConfigFieldIndex)} would find nothing in the
	 * {@link ConfigFieldIndex} for it, leaving the user stuck in an edit mode that refuses to close
	 * with nothing on screen to correct.
	 * </p>
	 *
	 * <p>
	 * A monomorphic {@link PropertyKind#ITEM} property is excluded for the second of those two
	 * reasons alone: it has no {@link ConfigFieldModel} either, but renders as a list of at most one
	 * entry, see {@link ConfigItemValue}, and an empty list is a value the configuration can hold.
	 * </p>
	 *
	 * <p>
	 * A polymorphic ITEM property renders a {@link PolymorphicItemControl}, whose type selector is a
	 * {@link com.top_logic.layout.form.model.SimpleSelectFieldModel} the {@link ConfigFieldIndex}
	 * does not carry either. Left unset, a mandatory one is nevertheless missing: it would be
	 * written as an empty element the configuration cannot read back. The refusal lists it, so it
	 * does not point at nothing, and the type selector carries the mandatory marker.
	 * </p>
	 *
	 * <p>
	 * An ITEM property written as text, like a TL-Script expression, is the exception:
	 * {@link ConfigEditorControl} edits an item that
	 * {@link ConfigControlService#hasTextForm(ConfigurationItem, PropertyDescriptor) has a text form}
	 * in a field of its own, so an unset mandatory one is checked like any other field.
	 * </p>
	 *
	 * <p>
	 * The kind exclusion comes first, before {@link ConfigurationItem#valueSet(PropertyDescriptor)}
	 * is consulted at all, and must stay there. {@code valueSet} answers "was this property ever
	 * written to", and a collection nobody has touched yet has not been - it reads as unset while
	 * it is still empty. A {@code valueSet} check ahead of the kind exclusion would therefore
	 * report every empty mandatory collection as missing, refusing Apply over a property that has
	 * no field to carry the refusal. Adding an entry does flip it, so it is the empty case the
	 * ordering is about, not the filled one: a live collection reports its own {@code add} (and
	 * {@code remove}) back to the owning item, which marks the property set - see
	 * {@link com.top_logic.basic.config.ConfigurationChange.Kind#ADD}.
	 * </p>
	 */
	private static boolean isMissing(ConfigurationItem item, PropertyDescriptor property) {
		switch (property.kind()) {
			case LIST:
			case ARRAY:
			case MAP:
				return false;

			case ITEM:
				if (ConfigControlService.hasTextForm(item, property)) {
					// An item written as text is edited in a field of its own, see
					// ConfigEditorControl.
					break;
				}
				if (PolymorphicConfiguration.class.isAssignableFrom(property.getType())) {
					// The type selector of a PolymorphicItemControl chooses the item: none chosen,
					// none given.
					return item.value(property) == null;
				}
				return false;

			default:
				break;
		}
		if (!item.valueSet(property)) {
			return true;
		}
		Object value = item.value(property);
		if (value == null) {
			return true;
		}
		return value instanceof String string && string.isEmpty();
	}

	/**
	 * Recurses {@link #collectMissingMandatory(ConfigurationItem, List, Set)} into the nested
	 * items held by an ITEM, LIST, ARRAY or MAP property, if that is what the given property is.
	 */
	private static void descendMissingMandatory(ConfigurationItem item, PropertyDescriptor property,
			List<Violation> violations, Set<ConfigurationItem> visited) {
		switch (property.kind()) {
			case ITEM:
				collectMissingMandatory((ConfigurationItem) item.value(property), violations, visited);
				break;

			case LIST: {
				List<?> entries = (List<?>) item.value(property);
				if (entries != null) {
					for (Object entry : entries) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			case ARRAY: {
				List<?> entries = PropertyDescriptorImpl.arrayAsList(item.value(property));
				if (entries != null) {
					for (Object entry : entries) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			case MAP: {
				Map<?, ?> entries = (Map<?, ?>) item.value(property);
				if (entries != null) {
					for (Object entry : entries.values()) {
						collectMissingMandatory((ConfigurationItem) entry, violations, visited);
					}
				}
				break;
			}

			default:
				break;
		}
	}

	/**
	 * Runs {@link ConstraintChecker} on the given item, adding a {@link Warning} for every
	 * {@link ConstraintFailure#isWarning() warning} failure it finds and a {@link Violation} for
	 * every other one.
	 *
	 * <p>
	 * A warning is kept, not dropped: a constraint declared
	 * {@link com.top_logic.basic.config.constraint.annotation.Constraint#asWarning() as a warning}
	 * is one whose author wanted the value questioned rather than refused, and dropping it here
	 * leaves the field with nothing at all on it - the constraint would then be written, evaluated,
	 * and silently discarded.
	 * </p>
	 *
	 * <p>
	 * Uses {@link ConstraintChecker#check(ConfigurationItem)}, not one of the logging overloads:
	 * those clear the failure list in a {@code finally} block once they have reported it, so
	 * {@link ConstraintChecker#getFailures()} would read empty afterwards. The check is itself
	 * recursive, so nested items and collection entries are covered without any recursion here.
	 * </p>
	 *
	 * <p>
	 * Takes {@link ConstraintFailure#getConstraintName()} as the message, not
	 * {@link ConstraintFailure#getMessage()}: the latter is the wording written to the server log -
	 * it names the configuration interface, the property, the raw value and the source location -
	 * which is not something to put next to a form field. {@link ConstraintFailure#getConstraintName()}
	 * is what the constraint itself said went wrong, and is what the classic declarative form shows
	 * too (through {@link com.top_logic.basic.config.constraint.algorithm.DefaultPropertyModel#getProblemDescription()},
	 * which is where that key comes from in the first place).
	 * </p>
	 */
	private static void collectConstraintFailures(ConfigurationItem item, List<Violation> violations,
			List<Warning> warnings) {
		ConstraintChecker checker = new ConstraintChecker();
		try {
			checker.check(item);
		} catch (ConfigurationException ex) {
			// Malformed constraint annotations - a programming error in the configuration
			// interface, not something the user editing the form did or can fix. Nothing is
			// added; the constraints that did parse have already been collected.
			Logger.error("Cannot check constraints of '" + item + "'.", ex, ConfigValidation.class);
		}
		for (ConstraintFailure failure : checker.getFailures()) {
			if (failure.isWarning()) {
				warnings.add(
					new Warning(failure.getItem(), failure.getContextProperty(), failure.getConstraintName()));
			} else {
				violations.add(
					new Violation(failure.getItem(), failure.getContextProperty(), failure.getConstraintName()));
			}
		}
	}
}
