/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

/**
 * The one place a {@link TLObject} becomes something a language model gets to see.
 *
 * <p>
 * Every conversion of model data into a tool result goes through an instance of this class, and an
 * instance cannot exist without the {@link ModelSecurity} of the run whose principal is being served.
 * That is the whole design: the leaks this closes - {@code getInstance} serialising every attribute of
 * any object addressable by TID, {@code listInstances} reporting unfiltered identities and counts, a
 * TL-Script result rendering {@code _tid}/{@code _type}/{@code _display} for every object it returned -
 * were three tools each having written their own serialiser and each having forgotten. So there is one
 * serialiser, it takes the principal's rights as a constructor argument, and the helpers that used to
 * make forgetting easy no longer exist.
 * </p>
 *
 * <h2>What it enforces</h2>
 *
 * <ul>
 * <li>An object the principal may not read contributes <b>nothing</b>: no identity, no label, no
 * attribute value. It is <em>omitted</em> from a collection rather than replaced by a placeholder,
 * because a placeholder still discloses that it exists and how many of them there are.</li>
 * <li>{@link #attributesOf(TLObject)} additionally respects <b>per-attribute</b> Read: an attribute the
 * principal may not read is left out and the object still renders.</li>
 * <li>Filtering is <b>recursive</b>: through collections, through map keys and values, through arrays
 * and through object-valued attributes, not only at the top level. The traversal deliberately mirrors
 * the engine's own {@link SearchExpression#filterSecurity(Object)}, down to returning the input
 * unchanged when nothing had to be removed.</li>
 * </ul>
 *
 * <h2>What it says about what it discloses</h2>
 *
 * <p>
 * Read rights decide which business data reaches the model; they do not say what that data <em>is</em>.
 * It is text other end users wrote, so every rendering leaving this class is
 * {@link #marked(String, String) marked} with the same untrusted-content envelope a fetched web page
 * gets. The marking is additional to the filtering and replaces none of it.
 * </p>
 *
 * <h2>What it does not claim</h2>
 *
 * <p>
 * <b>Aggregates are not filtered.</b> A count, a sum or a maximum computed <em>inside</em> TL-Script
 * (<code>all(`x`).size()</code>) is a number by the time it arrives here, and no read filter can tell
 * which objects it counted. Per-object read filtering covers identity, labels and attribute values;
 * cardinality is out of reach and accepted as such (issue #291). An administrator who needs a count
 * hidden denies the {@code script} capability, which is the honest control and is already expressible.
 * </p>
 *
 * @see ModelSecurity
 * @see com.top_logic.tl.ai.core.tools.model.TidResolver
 */
public final class ReadFilter {

	/** The key an object's identifier is disclosed under. */
	public static final String TID = "_tid";

	/** The key an object's qualified type name is disclosed under. */
	public static final String TYPE = "_type";

	/** The key an object's display label is disclosed under. */
	public static final String DISPLAY = "_display";

	private final ModelSecurity _security;

	private ReadFilter(ModelSecurity security) {
		_security = Objects.requireNonNull(security, "Model security must not be null.");
	}

	/**
	 * The read filter of the run the given tool call belongs to.
	 *
	 * <p>
	 * There is no permissive fallback. A call that cannot name the principal it is serving would have to
	 * disclose everything or nothing, and either answer would be wrong, so it is refused instead - the
	 * same treatment {@code ViewDocumentTool} and the TL-Script commit gate give a context-less call.
	 * </p>
	 *
	 * @param context
	 *        The context the tool was invoked with.
	 * @throws IllegalStateException
	 *         If the context is not the {@link ToolContext} of an agent run.
	 */
	public static ReadFilter of(ToolExecutionContext context) {
		if (context instanceof ToolContext toolContext) {
			return new ReadFilter(toolContext.modelSecurity());
		}
		throw new IllegalStateException("What a tool may disclose is decided against the run's"
			+ " principal, and this call has no run to ask. Execute the tool with the ToolContext of an"
			+ " agent run.");
	}

	/**
	 * The read filter for the given rights.
	 *
	 * <p>
	 * For the callers that already hold a {@link ModelSecurity} - the ones reached from a
	 * {@link com.top_logic.tl.ai.core.agent.SecurityContext} rather than from a tool context - and for
	 * tests, which pass {@link ModelSecurity#UNCHECKED}.
	 * </p>
	 */
	public static ReadFilter forSecurity(ModelSecurity security) {
		return new ReadFilter(security);
	}

	/**
	 * The rights every decision of this filter is made against.
	 */
	public ModelSecurity security() {
		return _security;
	}

	/**
	 * Whether the principal may read the given object at all.
	 */
	public boolean canRead(TLObject obj) {
		return obj == null || _security.canRead(obj);
	}

	/**
	 * The subset of the given objects the principal may read.
	 *
	 * <p>
	 * Apply this <b>before</b> paginating and before reporting a total count: a count taken over the
	 * unfiltered list discloses a cardinality the principal may not see.
	 * </p>
	 */
	public <T extends TLObject> List<T> readable(Collection<T> objects) {
		return _security.filterReadable(objects);
	}

	/**
	 * The given value with everything the principal may not read removed, in the shape it arrived in.
	 *
	 * <p>
	 * Structure-preserving: a {@link TLObject} stays a {@link TLObject}, a {@link List} stays a list, a
	 * {@link Map} stays a map. Use it when the value is handed on to something that renders model
	 * objects itself - {@link #text(String, Object)} does, and so does anything that only needs the value
	 * pruned. Use {@link #disclose(Object)} when the result has to become JSON data.
	 * </p>
	 *
	 * <p>
	 * Returns the argument itself when nothing had to be removed, which is what lets a caller tell "was
	 * filtered" from "was already clean" by identity - the contract of the engine's
	 * {@link SearchExpression#filterSecurity(Object)}, kept so the two behave alike.
	 * </p>
	 *
	 * @param value
	 *        Any value, including <code>null</code>.
	 * @return The filtered value; <code>null</code> for an object the principal may not read.
	 */
	public Object visible(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof TLObject obj) {
			return _security.canRead(obj) ? obj : null;
		}
		if (value instanceof Collection<?> collection) {
			return visibleCollection(collection);
		}
		if (value instanceof Map<?, ?> map) {
			return visibleMap(map);
		}
		if (value.getClass().isArray() && value instanceof Object[] array) {
			// Arrays reach the tool layer only from a script result; the JSON and text renderings both
			// treat them as sequences, so the filtered form is a list.
			return visibleCollection(Arrays.asList(array));
		}
		return value;
	}

	private Object visibleCollection(Collection<?> value) {
		if (value.isEmpty()) {
			return value;
		}
		Collection<Object> filtered = emptyLike(value);
		boolean changed = false;
		for (Object element : value) {
			Object visible = visible(element);
			if (visible == element) {
				// Element (recursively) readable - including an element that was null to begin with.
				filtered.add(element);
			} else {
				if (visible != null) {
					// Something inside the element was removed; the element itself stays.
					filtered.add(visible);
				}
				// Otherwise: an unreadable object, omitted rather than kept as a placeholder.
				changed = true;
			}
		}
		return changed ? filtered : value;
	}

	private static Collection<Object> emptyLike(Collection<?> value) {
		if (value instanceof RandomAccess || !(value instanceof Set<?>)) {
			return new ArrayList<>(value.size());
		}
		return value instanceof LinkedHashSet<?> ? new LinkedHashSet<>() : new HashSet<>();
	}

	private Object visibleMap(Map<?, ?> value) {
		if (value.isEmpty()) {
			return value;
		}
		Map<Object, Object> filtered =
			value instanceof LinkedHashMap<?, ?> ? new LinkedHashMap<>() : new HashMap<>();
		boolean changed = false;
		for (Map.Entry<?, ?> entry : value.entrySet()) {
			Object key = entry.getKey();
			Object entryValue = entry.getValue();
			Object visibleKey = visible(key);
			Object visibleValue = visible(entryValue);
			if (visibleKey == null && key != null) {
				// The entry is keyed by an object the principal may not read; the entry as a whole
				// discloses that the object exists, so it goes.
				changed = true;
				continue;
			}
			if (visibleValue == null && entryValue != null) {
				// Same for a value that is a single unreadable object: dropping the entry rather than
				// mapping the key to null keeps "there is something here you may not see" unsaid.
				changed = true;
				continue;
			}
			if (visibleKey != key || visibleValue != entryValue) {
				changed = true;
			}
			filtered.put(visibleKey, visibleValue);
		}
		return changed ? filtered : value;
	}

	/**
	 * The given value as JSON-compatible data, with everything the principal may not read removed.
	 *
	 * <p>
	 * Primitives stay themselves, collections and arrays become {@link List}s, maps become
	 * {@link Map}s keyed by strings, and a readable {@link TLObject} becomes its
	 * identity - {@link #TID}, {@link #TYPE} and {@link #DISPLAY}. Anything else becomes its
	 * {@code toString()}.
	 * </p>
	 *
	 * @param value
	 *        Any value, including <code>null</code>.
	 * @return JSON-compatible data; <code>null</code> for an object the principal may not read.
	 */
	public Object disclose(Object value) {
		// Filter first, convert second: after the filter nothing left in the graph is unreadable, so the
		// conversion has no security decision to make and cannot get one wrong.
		return asJsonData(visible(value));
	}

	private static Object asJsonData(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			return value;
		}
		if (value instanceof TLObject obj) {
			return identity(obj);
		}
		if (value instanceof Collection<?> collection) {
			List<Object> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				result.add(asJsonData(element));
			}
			return result;
		}
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> result = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				result.put(String.valueOf(entry.getKey()), asJsonData(entry.getValue()));
			}
			return result;
		}
		if (value.getClass().isArray() && value instanceof Object[] array) {
			return asJsonData(Arrays.asList(array));
		}
		return String.valueOf(value);
	}

	/**
	 * The identity a readable object discloses.
	 *
	 * <p>
	 * All three fields are disclosure: the label is business data, and the identifier plus the type
	 * disclose that an object of that type exists. So they are one decision, taken in {@link #visible},
	 * rather than three fields a caller assembles - which is why this is private and why nothing outside
	 * this class writes {@link #TID}, {@link #TYPE} or {@link #DISPLAY}.
	 * </p>
	 */
	private static Map<String, Object> identity(TLObject obj) {
		Map<String, Object> result = new LinkedHashMap<>();
		String id = idOf(obj);
		result.put(TID, id);
		TLStructuredType type = obj.tType();
		result.put(TYPE, type == null ? null : TLModelUtil.qualifiedName(type));
		String label = MetaLabelProvider.INSTANCE.getLabel(obj);
		result.put(DISPLAY, label);
		read(obj, id, label);
		return result;
	}

	/**
	 * Puts an object whose identity the model is shown on the run's reading list.
	 *
	 * <p>
	 * Here, because this is where an object becomes something the model sees: every rendering that
	 * discloses an identifier goes through {@link #identity(TLObject)} or {@link #labelled(TLObject)},
	 * so every read tool registers what it showed without a line of its own, and the answer may point
	 * at it with {@code [label](tl:id)}. Outside a tool call nothing is bound and nothing is recorded.
	 * </p>
	 */
	private static void read(TLObject obj, String id, String label, String... aliases) {
		if (id == null || ReadingListBinding.current() == null) {
			return;
		}
		ReadingListBinding.read(ReadSource.of(CitationKinds.objectKind(obj), id, label), aliases);
	}

	/**
	 * Records that a tool showed the model this object in a rendering of its own - a DTO that writes
	 * the identifier itself, the text of one of its attributes - so that the answer may cite it.
	 *
	 * <p>
	 * A rendering through this class records it already; this is for the tools that only ask this
	 * class whether they may show the object.
	 * </p>
	 *
	 * @param obj
	 *        The object shown; must be one the principal may read.
	 * @param aliases
	 *        Further identifiers the model was shown for it, e.g. the one it asked by.
	 */
	public static void shown(TLObject obj, String... aliases) {
		if (obj == null) {
			return;
		}
		read(obj, idOf(obj), MetaLabelProvider.INSTANCE.getLabel(obj), aliases);
	}

	/**
	 * {@link #shown(TLObject, String...)} for a tool that showed the model a text of the object - a
	 * document body, a stored note - against which a passage the answer quotes can be checked.
	 *
	 * @param obj
	 *        The object shown; must be one the principal may read.
	 * @param text
	 *        What of it the model read.
	 * @param aliases
	 *        Further identifiers the model was shown for it.
	 */
	public static void shownWithText(TLObject obj, String text, String... aliases) {
		String id = idOf(obj);
		if (id == null || ReadingListBinding.current() == null) {
			return;
		}
		ReadingListBinding.read(ReadSource.of(CitationKinds.objectKind(obj), id,
			MetaLabelProvider.INSTANCE.getLabel(obj)).withText(text), aliases);
	}

	/**
	 * The attributes of the given object the principal may read, as JSON-compatible data.
	 *
	 * <p>
	 * An attribute the principal may not read is absent from the result and the object still renders
	 * with the rest: per-attribute Read is a finer statement than per-object Read, and collapsing the
	 * two would either hide a readable object or disclose a hidden attribute.
	 * </p>
	 *
	 * @param obj
	 *        The object to describe.
	 * @return The readable attributes by name, or <code>null</code> if the principal may not read the
	 *         object at all - in which case nothing about it may be reported, not even that it exists.
	 */
	public Map<String, Object> attributesOf(TLObject obj) {
		if (obj == null || !_security.canRead(obj)) {
			return null;
		}
		TLStructuredType type = obj.tType();
		if (type == null) {
			return Map.of();
		}
		Map<String, Object> result = new LinkedHashMap<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (!_security.canRead(obj, part)) {
				continue;
			}
			result.put(part.getName(), disclose(obj.tValue(part)));
		}
		return result;
	}

	/**
	 * The given value rendered as text for the model, with everything the principal may not read
	 * removed first, {@link #marked(String, String) marked} as business data.
	 *
	 * <p>
	 * For the tools whose result is a sentence rather than a JSON document - a configured TL-Script
	 * tool, whose body may return anything at all. Scalars are rendered the way TL-Script renders
	 * them ({@link SearchExpression#asString(Object)}); a model object is rendered by
	 * {@link #labelled(TLObject)}, so that the model reads its label <em>and</em> the identifier it
	 * can hand back to a tool; a collection becomes one line per element, because a run of labels
	 * joined by commas is a single string the model cannot pick an object out of.
	 * </p>
	 *
	 * <p>
	 * A label is business data, so nothing here may see an object the principal may not read - which
	 * is why the filtering happens first and not in the renderer.
	 * </p>
	 *
	 * @param source
	 *        What produced the value, for the envelope's {@code source} attribute - the tool's name,
	 *        the query, the identifier that was asked about. Must not itself be business data: it sits
	 *        in the tool's own framing, outside the read filter's reach.
	 * @param value
	 *        Any value, including <code>null</code>.
	 * @return The marked rendering; never {@code null}.
	 */
	public String text(String source, Object value) {
		return marked(source, asText(visible(value)));
	}

	/**
	 * How a model object is written wherever a tool result is text rather than JSON:
	 * {@code label [id:TID]}.
	 *
	 * <p>
	 * One form, in one place, because the identifier is what makes a tool result actionable: the
	 * model reads an object here and names it again in the next call - {@code getInstance}, a
	 * host's {@code present}, a citation in its answer. A label alone cannot be fed back, and the model would
	 * have to search for the object it was just shown.
	 * </p>
	 *
	 * <p>
	 * The identifier is the object key's external form, the same {@code "TypeName:id"} the JSON
	 * renderings disclose as {@code _tid} and
	 * {@code com.top_logic.tl.ai.core.tools.model.TidResolver} reads back. An object that has none -
	 * a transient one, or a stand-in in a test - renders as its bare label rather than as a broken
	 * identifier.
	 * </p>
	 *
	 * <p>
	 * <b>Disclosure.</b> Both halves are business data: the label is text an end user typed, and the
	 * identifier says that an object of that type exists. So this is only ever called on a value
	 * that has already passed {@link #visible(Object)}.
	 * </p>
	 *
	 * @param obj
	 *        The object to render; must be one the principal may read.
	 * @return The rendering; never {@code null}.
	 */
	public static String labelled(TLObject obj) {
		if (obj == null) {
			return "";
		}
		String id = idOf(obj);
		String label = MetaLabelProvider.INSTANCE.getLabel(obj);
		read(obj, id, label);
		return withId(label, id);
	}

	/**
	 * The {@code label [id:TID]} form itself, for a caller that has the two halves already.
	 *
	 * <p>
	 * Separate from {@link #labelled(TLObject)} so that the form is one string literal in one
	 * place: a host tool that renders objects its own way still writes the identifier the way every
	 * other tool writes it, and {@code TidResolver.objectId} reads exactly this back.
	 * </p>
	 *
	 * @param label
	 *        What to call the object. May be empty.
	 * @param id
	 *        Its identifier, or <code>null</code> for an object that has none - which renders as the
	 *        bare label rather than as a broken identifier.
	 * @return The rendering; never {@code null}.
	 */
	public static String withId(String label, String id) {
		String text = label == null ? "" : label;
		return id == null || id.isEmpty() ? text : text + " [id:" + id + "]";
	}

	/**
	 * The identifier {@link #labelled(TLObject)} writes and the tools read back, or {@code null} for
	 * an object that has none.
	 *
	 * @param obj
	 *        Any object, including <code>null</code>.
	 * @return The external form of the object key, e.g. {@code "GenericObject:5371"}.
	 */
	public static String idOf(TLObject obj) {
		ObjectKey key = obj == null ? null : obj.tId();
		return key == null ? null : key.asString();
	}

	/**
	 * The text rendering behind {@link #text(String, Object)}, on an already filtered value.
	 */
	private static String asText(Object value) {
		if (value instanceof Collection<?> many) {
			if (many.isEmpty()) {
				return "";
			}
			if (many.size() == 1) {
				return asText(many.iterator().next());
			}
			StringBuilder result = new StringBuilder();
			for (Object element : many) {
				if (result.length() > 0) {
					result.append('\n');
				}
				result.append(asText(element));
			}
			return result.toString();
		}
		if (value instanceof TLObject obj) {
			return labelled(obj);
		}
		return SearchExpression.asString(value);
	}

	/**
	 * The given rendering of already-disclosed model data, marked as business data the model must not
	 * take instructions from.
	 *
	 * <p>
	 * <b>Why this is here.</b> Read rights decide <em>which</em> business data the model gets to see;
	 * they say nothing about <em>what it is</em>. And what it is, on the production side, is the
	 * agent's largest untrusted input: the free text in a shared record was written by some other end
	 * user of the application, who is not the principal this run serves. So every rendering that
	 * leaves this class carries the same envelope a fetched web page carries, from the same
	 * {@link UntrustedEnvelope}, and so falls under the standing rule the system prompt states once
	 * per run. It is marked here rather than in each tool for the reason the filtering is here - three
	 * tools each writing their own were three tools each forgetting.
	 * </p>
	 *
	 * <p>
	 * Static because marking takes no security decision: it is the label on what the decisions already
	 * produced, and a caller holding a rendering has, by construction, already asked a filter what it
	 * may contain.
	 * </p>
	 *
	 * @param source
	 *        What produced the payload, for the envelope's {@code source} attribute. Never business
	 *        data - see {@link #text(String, Object)}.
	 * @param rendered
	 *        The disclosed payload, JSON or text. May be {@code null}.
	 * @return The marked payload; never {@code null}.
	 * @see UntrustedEnvelope#MODEL_DATA
	 */
	public static String marked(String source, String rendered) {
		return UntrustedEnvelope.MODEL_DATA.wrap(source, rendered);
	}
}
