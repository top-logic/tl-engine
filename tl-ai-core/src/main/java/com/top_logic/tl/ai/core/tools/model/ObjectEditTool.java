/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.db2.DeleteChecker;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.LockConflictException;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.security.ToolSecurityException;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Changes, creates and deletes single business objects in one call each.
 *
 * <p>
 * <b>What it is for.</b> Changing one value of an object the user has open used to cost a skill
 * load, a handful of TL-Script documentation lookups and a script - nine steps for "set the price to
 * 450" (Gitea #625). These tools take the object's identifier and the new values as structured
 * input, convert each value to its attribute's type on the server ({@link AttributeValues}), check
 * the model's constraints ({@link ObjectConstraints}) and answer with the old and the new values, so
 * the change needs no second read to be confirmed. A set of objects chosen by a condition, or a value
 * computed from other data, remains one script, run by {@code changeData}.
 * </p>
 *
 * <p>
 * <b>An object with its parts.</b> {@code createObject} takes the parts of the new object - the
 * objects of its compositions, nested up to {@value #MAX_DEPTH} levels - in its values, and creates
 * the whole tree in one call (#630). That is one approval instead of one per object, and it is the
 * only way an object with a <em>mandatory</em> composition can be created here at all: the model's
 * constraints are asked once, after the whole tree exists, and a violation anywhere in it undoes all
 * of it. Every part is asked for the right to be created in its container, the question a
 * {@code createObject} call naming that container asks. For a container created in the same call
 * the engine answers that by the container's own creation, which was asked - the same answer one
 * call per part got, since the turn's calls share one transaction.
 * </p>
 *
 * <p>
 * <b>The user's rights, and nothing else.</b> Every call acts as the run's principal:
 * </p>
 * <ul>
 * <li>The object is resolved through {@link TidResolver}, so one the principal may not read is
 * refused like one that does not exist.</li>
 * <li>Write, create and delete are asked of the run's {@link ModelSecurity} - the engine's
 * {@code ModelAccessRights}, deny by default - per object and, for a change, per attribute: a part
 * grant that withholds one attribute refuses that attribute by name. Every check runs before the
 * first write, so a refusal changes nothing and is reported as a failed call that the turn does not
 * have to roll back.</li>
 * <li>A call in a <em>system</em> context is refused outright. The engine grants every right there
 * whoever the run's principal is, and its commit would be recorded under the system's name rather
 * than the user's; a write that cannot be checked or attributed is not made.</li>
 * <li>The approval prompt of a {@link ToolMode#DATA_WRITE} tool comes on top of all this, and the
 * pre-commit guard asks the same questions again for whatever the turn changed.</li>
 * <li>The answer shows only values the principal may read.</li>
 * </ul>
 *
 * <p>
 * <b>A new object's attributes.</b> The engine allows every operation on an object that is not yet
 * committed, because the roles that would decide them are derived at commit time. A new object's
 * initial values are therefore checked the way the application's own create dialogs check them: by
 * {@link ModelSecurity#canSetInitial(TLObject, TLStructuredTypePart)}, which applies an attribute
 * grant where it can be decided before the object exists - on the object its access parent leads
 * to - and lets the right to create the object cover the rest.
 * </p>
 *
 * @see #updateObject(String, Map, ToolContext)
 * @see #createObject(String, Map, String, String, ToolContext)
 * @see #deleteObject(String, ToolContext)
 */
public class ObjectEditTool {

	/**
	 * Configuration for {@link ObjectEditTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ObjectEditTool> {
		// no additional properties
	}

	/** The lock the interactive editors take, so an object a user has open is not written behind them. */
	private static final String LOCK = "editValues";

	/** How every answer that changed something ends. */
	private static final String SAVED = "The change is saved when this turn ends.";

	/** How many levels of parts one {@code createObject} call creates below the object it names. */
	private static final int MAX_DEPTH = 3;

	/**
	 * Creates a new {@link ObjectEditTool}.
	 */
	public ObjectEditTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ObjectEditTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ObjectEditTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	// -- update --

	/**
	 * Sets attributes of one object.
	 *
	 * @param id
	 *        The object, as {@code "TypeName:id"} or a whole {@code label [id:TID]} entry.
	 * @param values
	 *        Attribute name to new value, in the forms {@link AttributeValues} reads.
	 * @param context
	 *        The run's context: its rights and the turn's transaction.
	 * @return The old and new values, or a failed result saying why nothing was changed.
	 */
	@ToolMethod(description = "Change attributes of ONE object you know by its identifier: the object"
		+ " the user has open, selected or referenced, or one a tool answer named. Never search for that"
		+ " object by name. One call sets every attribute in 'values'. Name the attributes as the user"
		+ " did - the server matches a name that differs only in case or spacing, and the label the"
		+ " user sees - so look nothing up first; a name it cannot match is answered with the type's"
		+ " attributes. The server converts each value"
		+ " to the attribute's type: a number, true/false, a date as \"2027-05-14\", an enumeration by"
		+ " its value name, a reference by the identifier of the object it points to, a list for an"
		+ " attribute that holds several values (the list replaces them), null to clear. The answer"
		+ " shows the old and the new values - that IS the confirmation, do not read the object again."
		+ " For objects chosen by a condition, or values calculated from other data, use changeData.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE, marking = AnswerMarking.TOOL)
	public ToolResult updateObject(
			@P("Identifier of the object, as the context or a tool answer gives it: 'TypeName:id'") String id,
			@P("Attribute name -> new value, e.g. {\"price\": 12.5, \"status\": \"ACTIVE\", \"dueDate\":"
				+ " \"2027-05-14\", \"owner\": \"Person:42\"}") Map<String, Object> values,
			ToolContext context) {
		return run("updateObject(" + id + ")", context, security -> doUpdate(id, values, security, context));
	}

	private ToolResult doUpdate(String id, Map<String, Object> values, ModelSecurity security,
			ToolContext context) throws IOException {
		requireValues(values);
		TLObject target = TidResolver.resolve(id, security);
		TLStructuredType type = target.tType();
		Map<TLStructuredTypePart, Object> parts = writableParts(type, values.keySet(), false);

		if (!security.canWrite(target)) {
			throw denied("change", target);
		}
		List<String> withheld = parts.keySet().stream()
			.filter(part -> !security.canWrite(target, part))
			.map(TLStructuredTypePart::getName)
			.toList();
		if (!withheld.isEmpty()) {
			throw new Denied("Permission denied: your account may not change "
				+ quoted(withheld) + " of " + nameOf(target) + ". Nothing was changed. Tell the user;"
				+ " do not try another way to change it.");
		}

		Map<TLStructuredTypePart, Object> converted = convert(values, parts, security);
		Map<TLStructuredTypePart, Object> before = new LinkedHashMap<>();
		for (TLStructuredTypePart part : converted.keySet()) {
			before.put(part, snapshot(target.tValue(part)));
		}
		if (converted.entrySet().stream().allMatch(e -> sameValue(before.get(e.getKey()), e.getValue()))) {
			return ToolResult.of("No change: " + nameOf(target)
				+ " already has these values. Nothing was written.");
		}

		lock(context, target);
		List<String> violations = writing("updateObject", () -> {
			converted.forEach(target::tUpdate);
			List<String> problems = ObjectConstraints.violations(target);
			if (!problems.isEmpty()) {
				before.forEach(target::tUpdate);
			}
			return problems;
		});
		if (!violations.isEmpty()) {
			return refusal("The values break the model's rules, so nothing was changed:\n- "
				+ String.join("\n- ", violations));
		}

		ReadFilter filter = ReadFilter.forSecurity(security);
		Map<String, Object> changes = new LinkedHashMap<>();
		for (TLStructuredTypePart part : converted.keySet()) {
			Map<String, Object> change = new LinkedHashMap<>();
			if (security.canRead(target, part)) {
				change.put("from", AttributeValues.echo(part, before.get(part), filter));
				change.put("to", AttributeValues.echo(part, target.tValue(part), filter));
			} else {
				change.put("to", "(written; your account may not read it)");
			}
			changes.put(part.getName(), change);
		}
		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put("object", ReadFilter.labelled(target));
		answer.put("changed", changes);
		return ToolResult.of("Changed " + converted.size() + (converted.size() == 1 ? " attribute" : " attributes")
			+ ". " + SAVED + "\n" + ReadFilter.marked(ReadFilter.idOf(target), json(answer)));
	}

	// -- create --

	/**
	 * Creates one object, and the parts given with it.
	 *
	 * @param typeName
	 *        The qualified name of a concrete type, {@code "module:Type"}.
	 * @param values
	 *        Attribute name to value, in the forms {@link AttributeValues} reads; for a composition,
	 *        the new parts, see {@link #planPart(TLReference, Object, String, int, ModelSecurity, List)}.
	 * @param container
	 *        The object to create the new one in, or <code>null</code> for a free-standing object.
	 * @param containerReference
	 *        The composition reference of the container to put it in; only needed where the
	 *        container has several that fit.
	 * @param context
	 *        The run's context: its rights and the turn's transaction.
	 * @return The new objects and their values, or a failed result saying why nothing was created.
	 */
	@ToolMethod(description = "Create ONE object of a type and set its attributes in the same call -"
		+ " together with the objects that are part of it. Give every mandatory attribute in 'values';"
		+ " the same names and value forms as updateObject. An attribute that holds the object's parts"
		+ " takes the new parts as {\"type\": \"module:Type\", \"values\": {...}}, a list where it holds"
		+ " several; 'type' only where it holds more than one kind. E.g. {\"name\": \"Summit\","
		+ " \"sessions\": [{\"values\": {\"name\": \"Keynote\"}}, {\"values\": {\"name\": \"Panel\"}}]}."
		+ " Parts nest up to " + MAX_DEPTH + " levels; the whole tree is one call and one approval."
		+ " A refusal names the mandatory attributes the type has. To add a part to an object that"
		+ " exists, name that object as 'container'. The answer gives the identifiers and values of"
		+ " everything created - that IS the confirmation. For values calculated from other data, use"
		+ " changeData.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE, marking = AnswerMarking.TOOL)
	public ToolResult createObject(
			@P("Qualified name of the type: 'module:Type'") String type,
			@P("Attribute name -> value, e.g. {\"name\": \"Example\", \"price\": 12.5, \"status\":"
				+ " \"ACTIVE\"}, and the new parts as described") Map<String, Object> values,
			@P(value = "Identifier of the object the new one is part of, if it is created inside"
				+ " another object", required = false) String container,
			@P(value = "Name of the container's reference to put it in; only needed when the container"
				+ " has several that fit", required = false) String containerReference,
			ToolContext context) {
		return run("createObject(" + type + ")", context,
			security -> doCreate(type, values, container, containerReference, security, context));
	}

	private ToolResult doCreate(String typeName, Map<String, Object> values, String containerId,
			String containerReference, ModelSecurity security, ToolContext context) throws IOException {
		TLClass type = concreteClass(typeName);
		Map<String, Object> given = values == null ? Map.of() : values;
		Map<TLStructuredTypePart, Object> parts = writableParts(type, given.keySet(), true);

		TLObject container = null;
		TLReference place = null;
		if (containerId != null && !containerId.isBlank()) {
			container = TidResolver.resolve(containerId, security);
			place = placeIn(container, type, containerReference);
		}

		if (!security.canCreate(type, container)) {
			throw new Denied("Permission denied: your account may not create objects"
				+ " of type '" + qualified(type) + "'" + (container == null ? "" : " in " + nameOf(container))
				+ ". Nothing was created. Tell the user; do not try another way to create it.");
		}
		if (place != null && !security.canCreate(container, place, type)) {
			throw new Denied("Permission denied: your account may not add to '"
				+ place.getName() + "' of " + nameOf(container) + ". Nothing was created.");
		}

		NewObject tree = plan(type, given, parts, security);

		if (container != null) {
			lock(context, container);
		}
		TLObject parent = container;
		TLReference composition = place;
		Created created = new Created();
		String refused = writing("createObject", () -> created.build(tree, parent, composition, security));
		if (refused != null) {
			return refusal(refused);
		}

		ReadFilter filter = ReadFilter.forSecurity(security);
		String in = container == null ? null : ReadFilter.labelled(container) + " / " + place.getName();
		TLObject root = created.of(tree);
		int count = created.count();
		return ToolResult.of((count == 1 ? "Created 1 object; use its id to change it further. "
			: "Created " + count + " objects; use their ids to change them further. ") + SAVED + "\n"
			+ ReadFilter.marked(ReadFilter.idOf(root), json(created.shown(tree, filter, in))));
	}

	/**
	 * One object a {@code createObject} call is to create, planned in full before the first write.
	 *
	 * @param place
	 *        Where it stands in the call - {@code 'sessions' object 2} - for a refusal; empty for the
	 *        object the call names.
	 * @param type
	 *        Its concrete type.
	 * @param values
	 *        Its attribute values, converted.
	 * @param parts
	 *        The parts to create in it, by the composition they go into, in the order given.
	 */
	private record NewObject(String place, TLClass type, Map<TLStructuredTypePart, Object> values,
			Map<TLReference, List<NewObject>> parts) {
		// Plain record.
	}

	/**
	 * The objects one {@code createObject} call made, in the order it made them.
	 */
	private static final class Created {

		private final Map<NewObject, TLObject> _objects = new IdentityHashMap<>();

		private final List<NewObject> _order = new ArrayList<>();

		/**
		 * Creates the planned tree, asks the model's constraints once it is complete, and takes all of
		 * it back if a part may not be created or a constraint is broken.
		 *
		 * @return Why nothing was created, or <code>null</code> if everything was.
		 */
		String build(NewObject tree, TLObject container, TLReference place, ModelSecurity security) {
			try {
				create(tree, container, place, security);
			} catch (Denied ex) {
				undo();
				return ex.getMessage();
			}
			List<String> violations = new ArrayList<>();
			Set<TLClass> broken = new LinkedHashSet<>();
			for (NewObject planned : _order) {
				for (String violation : ObjectConstraints.violations(of(planned))) {
					violations.add(at(planned.place(), violation));
					broken.add(planned.type());
				}
			}
			if (violations.isEmpty()) {
				return null;
			}
			undo();
			String hints = broken.stream().map(ObjectEditTool::mandatoryHint).filter(hint -> !hint.isEmpty())
				.collect(Collectors.joining("\n"));
			return (count() == 1 ? "The object would break the model's rules, so it was not created:\n- "
				: "The objects would break the model's rules, so none of them was created:\n- ")
				+ String.join("\n- ", violations) + "\n" + hints;
		}

		private void create(NewObject planned, TLObject container, TLReference place, ModelSecurity security) {
			TLObject object = ModelService.getInstance().getFactory().createObject(planned.type(), container);
			_objects.put(planned, object);
			_order.add(planned);
			if (place != null) {
				if (place.isMultiple()) {
					container.tAdd(place, object);
				} else {
					container.tUpdate(place, object);
				}
			}
			// Asked once the object is in its container: an attribute grant of a type with an access
			// parent is decided on the object that parent leads to.
			for (TLStructuredTypePart attribute : planned.values().keySet()) {
				if (!security.canSetInitial(object, attribute)) {
					throw new Denied("Permission denied: your account may not set '" + attribute.getName()
						+ "' of a new '" + qualified(planned.type()) + "'"
						+ (planned.place().isEmpty() ? "" : " (" + planned.place() + ")")
						+ ". Nothing was created. Leave it out, or tell the user.");
				}
			}
			planned.values().forEach(object::tUpdate);
			for (Map.Entry<TLReference, List<NewObject>> parts : planned.parts().entrySet()) {
				TLReference composition = parts.getKey();
				for (NewObject part : parts.getValue()) {
					if (!security.canCreate(part.type(), object)) {
						throw new Denied("Permission denied: your account may not create objects of type '"
							+ qualified(part.type()) + "' (" + part.place() + "). Nothing was created. Tell the"
							+ " user; do not try another way to create it.");
					}
					if (!security.canCreate(object, composition, part.type())) {
						throw new Denied("Permission denied: your account may not add to '" + composition.getName()
							+ "' of the new '" + qualified(planned.type()) + "'. Nothing was created.");
					}
					create(part, object, composition, security);
				}
			}
		}

		/** Deletes what was created, the parts before the object that holds them. */
		private void undo() {
			for (int i = _order.size() - 1; i >= 0; i--) {
				TLObject object = _objects.get(_order.get(i));
				if (object.tValid()) {
					object.tDelete();
				}
			}
		}

		TLObject of(NewObject planned) {
			return _objects.get(planned);
		}

		int count() {
			return _order.size();
		}

		/** What the answer shows of a created object: its id, its type, its values and its parts. */
		Map<String, Object> shown(NewObject planned, ReadFilter filter, String in) {
			TLObject object = of(planned);
			Map<String, Object> values = new LinkedHashMap<>();
			for (TLStructuredTypePart part : planned.values().keySet()) {
				values.put(part.getName(), AttributeValues.echo(part, object.tValue(part), filter));
			}
			for (Map.Entry<TLReference, List<NewObject>> parts : planned.parts().entrySet()) {
				List<Map<String, Object>> shownParts = parts.getValue().stream()
					.map(part -> shown(part, filter, null))
					.toList();
				values.put(parts.getKey().getName(), parts.getKey().isMultiple() ? shownParts
					: shownParts.isEmpty() ? null : shownParts.get(0));
			}
			Map<String, Object> answer = new LinkedHashMap<>();
			answer.put("created", ReadFilter.labelled(object));
			answer.put("type", qualified(planned.type()));
			if (in != null) {
				answer.put("in", in);
			}
			answer.put("values", values);
			return answer;
		}
	}

	/**
	 * The whole tree a call creates, every value converted - or every problem with it at once, so the
	 * corrected call is one call.
	 *
	 * @param parts
	 *        The attributes the call names for the object itself, already checked by
	 *        {@link #writableParts(TLStructuredType, Collection, boolean)}.
	 */
	private static NewObject plan(TLClass type, Map<String, Object> given, Map<TLStructuredTypePart, Object> parts,
			ModelSecurity security) {
		List<String> problems = new ArrayList<>();
		NewObject tree = plan(type, given, parts, "", 0, security, problems);
		if (!problems.isEmpty()) {
			throw new IllegalArgumentException("Nothing was changed:\n- " + String.join("\n- ", problems));
		}
		return tree;
	}

	private static NewObject plan(TLClass type, Map<String, Object> given, Map<TLStructuredTypePart, Object> named,
			String place, int depth, ModelSecurity security, List<String> problems) {
		Map<TLStructuredTypePart, Object> plain = new LinkedHashMap<>();
		Map<TLReference, Object> compositions = new LinkedHashMap<>();
		for (Map.Entry<TLStructuredTypePart, Object> entry : named.entrySet()) {
			if (entry.getKey() instanceof TLReference reference && reference.isComposite()) {
				compositions.put(reference, given.get(entry.getValue()));
			} else {
				plain.put(entry.getKey(), entry.getValue());
			}
		}
		Map<TLStructuredTypePart, Object> values = convert(given, plain, security, place, problems);
		Map<TLReference, List<NewObject>> parts = new LinkedHashMap<>();
		compositions.forEach((reference, value) -> parts.put(reference,
			planParts(reference, value, place, depth, security, problems)));
		return new NewObject(place, type, values, parts);
	}

	/** The new parts given for one composition of an object at the given depth. */
	private static List<NewObject> planParts(TLReference reference, Object value, String place, int depth,
			ModelSecurity security, List<String> problems) {
		List<Object> given = value == null ? List.of()
			: value instanceof Collection<?> many ? new ArrayList<>(many) : List.of(value);
		if (given.isEmpty()) {
			return List.of();
		}
		if (!reference.isMultiple() && given.size() > 1) {
			problems.add(at(place, "'" + reference.getName() + "' holds one object, and " + given.size()
				+ " were given."));
			return List.of();
		}
		if (depth == MAX_DEPTH) {
			problems.add(at(place, "the parts in '" + reference.getName() + "' would be more than " + MAX_DEPTH
				+ " levels below the object created, and one call creates parts at most " + MAX_DEPTH
				+ " levels deep. Create them with another createObject call that names their container."));
			return List.of();
		}
		List<NewObject> result = new ArrayList<>();
		for (int i = 0; i < given.size(); i++) {
			String here = "'" + reference.getName() + "'" + (reference.isMultiple() ? " object " + (i + 1) : "");
			NewObject part = planPart(reference, given.get(i), place.isEmpty() ? here : place + ", " + here,
				depth + 1, security, problems);
			if (part != null) {
				result.add(part);
			}
		}
		return result;
	}

	/**
	 * One new part, as the call gives it: {@code {"type": "module:Type", "values": {...}}}, the type
	 * only where the composition holds more than one kind. A part given as its values alone - neither
	 * key present - is read as those values, which is unambiguous and saves a model the call it would
	 * otherwise spend on being told the form.
	 */
	private static NewObject planPart(TLReference reference, Object item, String place, int depth,
			ModelSecurity security, List<String> problems) {
		if (!(item instanceof Map<?, ?> spec)) {
			problems.add(at(place, "a new object goes into '" + reference.getName() + "' as {\"type\":"
				+ " \"module:Type\", \"values\": {...}}, not as " + (item instanceof String ? "the text '" + item + "'"
					: String.valueOf(item))
				+ ". An object that exists cannot be put there; to change one, use updateObject."));
			return null;
		}
		Object typeName = null;
		Object values = spec;
		if (spec.containsKey("type") || spec.containsKey("values")) {
			List<String> other = spec.keySet().stream().map(String::valueOf)
				.filter(key -> !key.equals("type") && !key.equals("values"))
				.toList();
			if (!other.isEmpty()) {
				problems.add(at(place, "a new object is {\"type\": \"module:Type\", \"values\": {...}}; put "
					+ quoted(other) + " into 'values'."));
				return null;
			}
			typeName = spec.get("type");
			values = spec.get("values");
		}
		if (values != null && !(values instanceof Map<?, ?>)) {
			problems.add(at(place, "'values' of a new object maps attribute names to values, not "
				+ String.valueOf(values) + "."));
			return null;
		}
		TLClass type = partType(reference, typeName, place, problems);
		if (type == null) {
			return null;
		}
		Map<String, Object> given = new LinkedHashMap<>();
		if (values != null) {
			((Map<?, ?>) values).forEach((key, value) -> given.put(String.valueOf(key), value));
		}
		Map<TLStructuredTypePart, Object> named;
		try {
			named = writableParts(type, given.keySet(), true);
		} catch (IllegalArgumentException ex) {
			problems.add(at(place, ex.getMessage()));
			return null;
		}
		return plan(type, given, named, place, depth, security, problems);
	}

	/** The type of a new part: the one named, or the composition's own where it is concrete. */
	private static TLClass partType(TLReference reference, Object name, String place, List<String> problems) {
		TLType target = reference.getType();
		if (name == null || String.valueOf(name).isBlank()) {
			if (target instanceof TLClass concrete && !concrete.isAbstract()) {
				return concrete;
			}
			problems.add(at(place, "'" + reference.getName() + "' holds objects of several kinds; name the kind"
				+ " of each new one as \"type\": " + kinds(target) + "."));
			return null;
		}
		TLClass type;
		try {
			type = concreteClass(String.valueOf(name));
		} catch (IllegalArgumentException ex) {
			problems.add(at(place, ex.getMessage()));
			return null;
		}
		if (!TLModelUtil.isCompatibleType(target, type)) {
			problems.add(at(place, "'" + qualified(type) + "' cannot go into '" + reference.getName()
				+ "', which holds " + kinds(target) + "."));
			return null;
		}
		return type;
	}

	/** The concrete types a composition of the given type takes, for a model to choose from. */
	private static String kinds(TLType target) {
		if (!(target instanceof TLClass type)) {
			return "'" + qualified(target) + "'";
		}
		return TLModelUtil.getConcreteSpecializations(type).stream()
			.map(ObjectEditTool::qualified).sorted().map(name -> "'" + name + "'")
			.collect(Collectors.joining(", "));
	}

	/** A problem with the given place in the call put in front; the object the call names has none. */
	private static String at(String place, String problem) {
		return place.isEmpty() ? problem : "In " + place + ": " + problem;
	}

	private static TLClass concreteClass(String typeName) {
		if (typeName == null || typeName.isBlank()) {
			throw new IllegalArgumentException("No type named. Pass the qualified name 'module:Type'.");
		}
		TLType type;
		try {
			type = TLModelUtil.findType(typeName.trim());
		} catch (TopLogicException ex) {
			throw new IllegalArgumentException("There is no type '" + typeName + "'. Pass the qualified"
				+ " name 'module:Type' as outlineModel or searchModel list it.", ex);
		}
		if (!(type instanceof TLClass tlClass)) {
			throw new IllegalArgumentException("'" + typeName + "' is not a class of objects but a "
				+ type.getModelKind().name().toLowerCase() + "; only classes have instances to create.");
		}
		if (tlClass.isAbstract()) {
			throw new IllegalArgumentException("'" + typeName + "' is abstract and has no instances of its"
				+ " own. Create one of its concrete subtypes: "
				+ TLModelUtil.getConcreteSpecializations(tlClass).stream()
					.filter(c -> c != tlClass).map(ObjectEditTool::qualified).sorted()
					.collect(Collectors.joining(", "))
				+ ".");
		}
		return tlClass;
	}

	/**
	 * The composition reference of the container a new object of the given type goes into.
	 */
	private static TLReference placeIn(TLObject container, TLClass type, String name) {
		List<TLReference> fitting = new ArrayList<>();
		for (TLStructuredTypePart part : container.tType().getAllParts()) {
			if (part instanceof TLReference reference && reference.isComposite() && !reference.isBackwards()
				&& !reference.isDerived() && TLModelUtil.isCompatibleType(reference.getType(), type)) {
				fitting.add(reference);
			}
		}
		if (name != null && !name.isBlank()) {
			String wanted = partName(name);
			return fitting.stream().filter(r -> r.getName().equals(wanted)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(nameOf(container) + " has no reference '"
					+ wanted + "' that holds objects of type '" + qualified(type) + "'"
					+ (fitting.isEmpty() ? "." : "; it has " + quoted(names(fitting)) + ".")));
		}
		if (fitting.isEmpty()) {
			throw new IllegalArgumentException(nameOf(container) + " cannot contain objects of type '"
				+ qualified(type) + "'. Create it without a container, or in the object that contains"
				+ " this type.");
		}
		if (fitting.size() > 1) {
			throw new IllegalArgumentException(nameOf(container) + " holds objects of type '"
				+ qualified(type) + "' in " + quoted(names(fitting))
				+ "; name the one you mean as 'containerReference'.");
		}
		TLReference place = fitting.get(0);
		if (!place.isMultiple() && container.tValue(place) != null) {
			throw new IllegalArgumentException("'" + place.getName() + "' of " + nameOf(container)
				+ " already holds an object. Change or delete that one instead.");
		}
		return place;
	}

	private static String mandatoryHint(TLClass type) {
		List<String> mandatory = type.getAllParts().stream()
			.filter(part -> part.isMandatory() && !part.isDerived())
			.map(TLStructuredTypePart::getName)
			.toList();
		return mandatory.isEmpty() ? "" : "Mandatory attributes of '" + qualified(type) + "': "
			+ quoted(mandatory) + ".";
	}

	// -- delete --

	/**
	 * Deletes one object the way the application deletes it.
	 *
	 * @param id
	 *        The object, as {@code "TypeName:id"} or a whole {@code label [id:TID]} entry.
	 * @param context
	 *        The run's context: its rights and the turn's transaction.
	 * @return What was deleted, or a failed result saying why nothing was.
	 */
	@ToolMethod(description = "Delete ONE object you know by its identifier, the way the application"
		+ " deletes it: the objects it contains are deleted with it, and the application may refuse the"
		+ " deletion. The answer says what was deleted. For objects chosen by a condition, use"
		+ " changeData.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE, marking = AnswerMarking.TOOL)
	public ToolResult deleteObject(
			@P("Identifier of the object, as the context or a tool answer gives it: 'TypeName:id'") String id,
			ToolContext context) {
		return run("deleteObject(" + id + ")", context, security -> doDelete(id, security, context));
	}

	private ToolResult doDelete(String id, ModelSecurity security, ToolContext context) throws IOException {
		TLObject target = TidResolver.resolve(id, security);
		if (!security.canDelete(target)) {
			throw denied("delete", target);
		}
		// The parts it contains go with it, so they are the user's to delete as well. Asked here, by
		// type, because the pre-commit guard would otherwise refuse the whole turn for one of them.
		Set<TLObject> parts = new LinkedHashSet<>();
		collectParts(target, parts);
		Set<String> withheld = parts.stream()
			.filter(part -> !security.canDelete(part))
			.map(part -> qualified(part.tType()))
			.collect(Collectors.toCollection(LinkedHashSet::new));
		if (!withheld.isEmpty()) {
			throw new Denied("Permission denied: deleting " + nameOf(target)
				+ " would also delete objects it contains of type " + quoted(withheld)
				+ ", and your account may not delete those. Nothing was deleted.");
		}

		// The application's own veto, asked before anything moves: the engine asks it only after it
		// has already cleared the references to the deleted objects, so a veto raised by tDelete
		// leaves the turn half-changed and has to roll it back.
		List<TLObject> deletions = new ArrayList<>();
		deletions.add(target);
		deletions.addAll(parts);
		try {
			new DeleteChecker(deletions).checkAll();
		} catch (TopLogicException ex) {
			return refusal("The application does not allow deleting " + nameOf(target) + ": "
				+ english(ex) + " Nothing was deleted.");
		}

		String deleted = ReadFilter.labelled(target);
		String typeName = qualified(target.tType());
		lock(context, target);
		writing("deleteObject", () -> {
			target.tDelete();
			return List.of();
		});

		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put("deleted", deleted);
		answer.put("type", typeName);
		answer.put("containedObjectsDeleted", Integer.valueOf(parts.size()));
		return ToolResult.of("Deleted 1 object" + (parts.isEmpty() ? "" : " and the "
			+ parts.size() + " objects it contained") + ". " + SAVED + "\n"
			+ ReadFilter.marked(id, json(answer)));
	}

	/** Everything the object contains, transitively, through its composition references. */
	private static void collectParts(TLObject object, Set<TLObject> parts) {
		for (TLStructuredTypePart part : object.tType().getAllParts()) {
			if (!(part instanceof TLReference reference) || !reference.isComposite() || reference.isBackwards()
				|| reference.isDerived()) {
				continue;
			}
			Object value = object.tValue(part);
			Collection<?> children = value instanceof Collection<?> many ? many
				: value == null ? List.of() : List.of(value);
			for (Object child : children) {
				if (child instanceof TLObject childObject && parts.add(childObject)) {
					collectParts(childObject, parts);
				}
			}
		}
	}

	// -- shared --

	/** One write operation, given the rights of the run it acts for. */
	@FunctionalInterface
	private interface Operation {
		ToolResult apply(ModelSecurity security) throws IOException;
	}

	/**
	 * Runs one operation, turning every refusal into a failed result.
	 *
	 * <p>
	 * A refusal is raised before the first write, or the operation undid its own write before
	 * refusing, so a failed result here has nothing for the turn to roll back. Anything else escapes:
	 * a failure in the middle of a write leaves the transaction in a state only the turn's rollback
	 * can repair, and the agent loop rolls the turn back for exactly that kind of failure.
	 * </p>
	 */
	private static ToolResult run(String call, ToolContext context, Operation operation) {
		if (ThreadContext.isSystemContext() || TLContext.currentUser() == null) {
			return refusal("This call runs without a user account, in a system context where the"
				+ " application checks no rights and records no author. Data is only changed in the name"
				+ " of the user the run acts for. Nothing was changed.");
		}
		ModelSecurity security = context.modelSecurity();
		try {
			return operation.apply(security);
		} catch (Denied | ToolSecurityException ex) {
			// A configured state, not an operator's problem: info, like every other model-access refusal.
			Logger.info(call + " refused: " + ex.getMessage(), ObjectEditTool.class);
			return refusal(ex.getMessage());
		} catch (IllegalArgumentException ex) {
			return refusal(ex.getMessage());
		} catch (IOException ex) {
			throw new IllegalStateException("Could not render the answer of " + call + ".", ex);
		}
	}

	/**
	 * A missing model access right, said in the words the model relays to the user.
	 *
	 * <p>
	 * Not a {@link ToolSecurityException}: that one names an operation and a type, and these
	 * refusals name the attribute and the object, which is what the user needs to hear.
	 * </p>
	 */
	private static final class Denied extends RuntimeException {

		Denied(String message) {
			super(message);
		}
	}

	/**
	 * Takes the lock the interactive editors take, or refuses: nothing is written yet, so a lock held
	 * by somebody else is a refusal like any other.
	 */
	private static void lock(ToolContext context, TLObject target) {
		try {
			context.transaction().acquireLock(LOCK, target);
		} catch (LockConflictException ex) {
			throw new IllegalArgumentException("Somebody is editing " + nameOf(target)
				+ " right now, so it cannot be changed: " + ex.getMessage() + " Nothing was changed.", ex);
		}
	}

	/**
	 * Runs the part of an operation that writes.
	 *
	 * <p>
	 * A failure in here may leave part of the call written, and a failed tool call that reports a
	 * refusal is one the turn commits around. So whatever escapes is re-thrown as a failure the
	 * agent loop rolls the whole turn back for, never as a refusal.
	 * </p>
	 */
	private static <T> T writing(String call, Supplier<T> write) {
		try {
			return write.get();
		} catch (RuntimeException ex) {
			throw new IllegalStateException(call + " failed while writing, so the changes of this turn are"
				+ " rolled back: " + ex.getMessage(), ex);
		}
	}

	private static ToolResult refusal(String message) {
		return ToolResult.failure(message);
	}

	private static void requireValues(Map<String, Object> values) {
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException("'values' is empty. Pass the attributes to change and"
				+ " their new values, e.g. {\"price\": 12.5}.");
		}
	}

	/**
	 * The attributes named, each one checked for being one this tool may write - in the order given.
	 */
	private static Map<TLStructuredTypePart, Object> writableParts(TLStructuredType type,
			Collection<String> names, boolean creating) {
		Map<TLStructuredTypePart, Object> result = new LinkedHashMap<>();
		List<String> unknown = new ArrayList<>();
		for (String given : names) {
			TLStructuredTypePart part = attribute(type, given);
			if (part == null) {
				unknown.add(given);
				continue;
			}
			checkWritable(type, part, creating);
			if (result.put(part, given) != null) {
				throw new IllegalArgumentException("'" + part.getName() + "' is named twice in 'values'.");
			}
		}
		if (!unknown.isEmpty()) {
			throw new IllegalArgumentException("'" + qualified(type) + "' has no attribute "
				+ quoted(unknown) + ". Its attributes: "
				+ quoted(type.getAllParts().stream().filter(p -> !p.isDerived())
					.map(TLStructuredTypePart::getName).toList())
				+ ".");
		}
		return result;
	}

	/**
	 * The attribute the model named, also when it named it the way the user did.
	 *
	 * <p>
	 * The exact name first; then a name that differs only in case, spaces, hyphens or underscores
	 * ({@code "ticket price"} for {@code ticketPrice}); then the attribute's label in the user's
	 * language or in English. A looser match is taken only when exactly one attribute fits it, so a
	 * guess never picks between two. The answer names the attribute that was written, which makes
	 * the match visible. What this saves is the model-lookup call a model otherwise spends on
	 * learning the spelling of a name the user already said.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         If a loose name fits several attributes.
	 */
	private static TLStructuredTypePart attribute(TLStructuredType type, String given) {
		String name = partName(given);
		TLStructuredTypePart exact = type.getPart(name);
		if (exact != null) {
			return exact;
		}
		String wanted = normalized(name);
		List<TLStructuredTypePart> candidates = type.getAllParts().stream()
			.filter(part -> !part.isDerived())
			.map(TLStructuredTypePart.class::cast)
			.toList();
		TLStructuredTypePart byName = unique(given, candidates.stream()
			.filter(part -> normalized(part.getName()).equals(wanted)).toList());
		if (byName != null) {
			return byName;
		}
		Resources user = Resources.getInstance();
		Resources english = Resources.getInstance(Locale.ENGLISH);
		return unique(given, candidates.stream()
			.filter(part -> wanted.equals(normalized(label(user, part)))
				|| wanted.equals(normalized(label(english, part))))
			.toList());
	}

	private static TLStructuredTypePart unique(String given, List<TLStructuredTypePart> matches) {
		if (matches.size() > 1) {
			throw new IllegalArgumentException("'" + given + "' fits several attributes: "
				+ quoted(matches.stream().map(TLStructuredTypePart::getName).toList())
				+ ". Name the one you mean exactly.");
		}
		return matches.isEmpty() ? null : matches.get(0);
	}

	private static String label(Resources resources, TLStructuredTypePart part) {
		return resources.getString(TLModelI18N.getI18NKey(part), null);
	}

	/** A name or label with case, spaces, hyphens and underscores taken out. */
	private static String normalized(String text) {
		return text == null ? "" : text.replaceAll("[\\s_-]", "").toLowerCase(Locale.ROOT);
	}

	private static void checkWritable(TLStructuredType type, TLStructuredTypePart part, boolean creating) {
		if (part.isDerived() || part.getStorageImplementation().isReadOnly()) {
			throw new IllegalArgumentException("'" + part.getName() + "' of '" + qualified(type)
				+ "' is calculated by the application and cannot be set.");
		}
		if (part instanceof TLReference reference) {
			if (reference.isBackwards()) {
				TLReference opposite = reference.getOpposite();
				throw new IllegalArgumentException("'" + part.getName() + "' is the other side of "
					+ (opposite == null ? "a reference" : "'" + qualified(opposite.getOwner()) + "#"
						+ opposite.getName() + "'")
					+ "; set it there, on the objects it points from.");
			}
			if (reference.isComposite() && !creating) {
				throw new IllegalArgumentException("'" + part.getName() + "' holds the objects contained in"
					+ " this one. Create them with createObject and this object as 'container', and remove"
					+ " them with deleteObject.");
			}
		}
	}

	private static Map<TLStructuredTypePart, Object> convert(Map<String, Object> values,
			Map<TLStructuredTypePart, Object> parts, ModelSecurity security) {
		List<String> problems = new ArrayList<>();
		Map<TLStructuredTypePart, Object> result = convert(values, parts, security, "", problems);
		if (!problems.isEmpty()) {
			// Every wrong value at once, so the corrected call is one call.
			throw new IllegalArgumentException("Nothing was changed:\n- " + String.join("\n- ", problems));
		}
		return result;
	}

	/** Converts every value it can, and adds a problem for each one it cannot. */
	private static Map<TLStructuredTypePart, Object> convert(Map<String, Object> values,
			Map<TLStructuredTypePart, Object> parts, ModelSecurity security, String place, List<String> problems) {
		Map<TLStructuredTypePart, Object> result = new LinkedHashMap<>();
		for (Map.Entry<TLStructuredTypePart, Object> entry : parts.entrySet()) {
			TLStructuredTypePart part = entry.getKey();
			try {
				result.put(part, AttributeValues.convert(part, values.get(entry.getValue()), security));
			} catch (IllegalArgumentException ex) {
				problems.add(at(place, ex.getMessage()));
			}
		}
		return result;
	}

	/** A value as it stood, detached from the object's live collection. */
	private static Object snapshot(Object value) {
		if (value instanceof Set<?> set) {
			return new LinkedHashSet<>(set);
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return value;
	}

	private static boolean sameValue(Object before, Object after) {
		if (before instanceof Collection<?> old && after instanceof Collection<?> now) {
			return before instanceof Set<?> || after instanceof Set<?>
				? new LinkedHashSet<>(old).equals(new LinkedHashSet<>(now))
				: new ArrayList<>(old).equals(new ArrayList<>(now));
		}
		if (before instanceof Collection<?> old && after == null) {
			return old.isEmpty();
		}
		return Objects.equals(before, after);
	}

	private static Denied denied(String operation, TLObject target) {
		return new Denied("Permission denied: your account may not " + operation + " "
			+ nameOf(target) + ". Nothing was changed. Tell the user; do not try another way to "
			+ operation + " it.");
	}

	/**
	 * How a refusal names an object: its label and its type. The label is disclosed only after the
	 * read check passed, like everywhere else it is shown.
	 */
	private static String nameOf(TLObject object) {
		return "'" + MetaLabelProvider.INSTANCE.getLabel(object) + "' (" + qualified(object.tType()) + ")";
	}

	private static String qualified(TLType type) {
		return type == null ? "?" : TLModelUtil.qualifiedName(type);
	}

	/** The local name of an attribute, also when the model wrote it qualified. */
	private static String partName(String name) {
		String trimmed = name.trim();
		int hash = trimmed.lastIndexOf('#');
		return hash < 0 ? trimmed : trimmed.substring(hash + 1);
	}

	private static List<String> names(List<TLReference> references) {
		return references.stream().map(TLReference::getName).toList();
	}

	private static String quoted(Collection<String> names) {
		return names.stream().map(name -> "'" + name + "'").collect(Collectors.joining(", "));
	}

	private static String english(TopLogicException ex) {
		return Resources.getInstance(Locale.ENGLISH).getString(ex.getErrorKey());
	}

	private static String json(Object value) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			write(json, value);
		}
		return buffer.toString();
	}

	private static void write(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String string) {
			json.value(string);
		} else if (value instanceof Number number) {
			json.value(number);
		} else if (value instanceof Boolean bool) {
			json.value(bool);
		} else if (value instanceof Map<?, ?> map) {
			json.beginObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				json.name(String.valueOf(entry.getKey()));
				write(json, entry.getValue());
			}
			json.endObject();
		} else if (value instanceof Iterable<?> items) {
			json.beginArray();
			for (Object item : items) {
				write(json, item);
			}
			json.endArray();
		} else {
			json.value(String.valueOf(value));
		}
	}
}
