/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.ToolResults;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceMetadata;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceResponse;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceTypeInfo;
import com.top_logic.tl.ai.core.tools.model.messages.InstancesResponse;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.Resources;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Tool for accessing TopLogic instance data from the knowledge base.
 *
 * <p>
 * This tool provides methods for listing instances of a type with pagination and retrieving
 * detailed information about individual instances. All methods return JSON-formatted strings for
 * consumption by AI agents.
 * </p>
 *
 * <p>
 * Both methods reach the knowledge base with the run's identity, so both are filtered by the run's
 * {@link ReadFilter}: an instance the principal may not read is absent from the listing and absent
 * from the total count, and an unreadable attribute of a readable instance is absent from its
 * description. This class holds no serialiser of its own - it used to hold three, which is how the
 * filtering came to be missing here in the first place.
 * </p>
 *
 * <p>
 * Both results are additionally {@link ReadFilter#marked(String, String) marked} as business data.
 * An attribute value is free text some other end user typed into a shared record, so it is the same
 * kind of input as a fetched web page and carries the same tags. The marking sits on the success
 * response only - a refusal is this application's own sentence, not something it disclosed.
 * </p>
 *
 * @see #listInstances(String, String, List, Integer, String, ToolExecutionContext)
 * @see #getInstance(List, ToolExecutionContext)
 * @see ReadFilter
 */
public class InstanceBrowserTool {

	/**
	 * Configuration for {@link InstanceBrowserTool}.
	 */
	public interface Config extends PolymorphicConfiguration<InstanceBrowserTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link InstanceBrowserTool}.
	 */
	public InstanceBrowserTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link InstanceBrowserTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public InstanceBrowserTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Default number of instances per page.
	 *
	 * <p>
	 * Small on purpose (#616): a listing is for looking at a few instances, and a model that pages
	 * through all of them to find the largest one floods its context with rows a query would have
	 * reduced to one on the server.
	 * </p>
	 */
	static final int DEFAULT_LIMIT = 20;

	/** Maximum number of instances per page. */
	static final int MAX_LIMIT = 200;

	/** How many identifiers one {@code getInstance} call reads at most. */
	static final int MAX_IDS = 50;

	/**
	 * Lists instances of a specific type with pagination support.
	 *
	 * <p>
	 * Returns a JSON object containing the instances of the page - each with its identifier, its
	 * label, its own type and the requested attribute values - the total count, and a cursor for
	 * retrieving the next page. The cursor is an opaque string that should be passed to subsequent
	 * calls to retrieve more results.
	 * </p>
	 *
	 * <p>
	 * The attributes are what spares a model a {@code getInstance} per row (#616): asked for
	 * {@code name, status}, a listing of twenty rows is one call rather than twenty-one.
	 * </p>
	 *
	 * @param moduleName
	 *        The module containing the type.
	 * @param typeName
	 *        The type name.
	 * @param attributes
	 *        Names of the attributes to include per instance; empty or {@code null} for none. A name
	 *        the instance's type does not have, or that the acting account may not read, is left out.
	 * @param limit
	 *        Maximum number of instances to return (default: 20, max: 200).
	 * @param cursor
	 *        Pagination cursor from previous response (null/empty for first page).
	 * @param context
	 *        Injected execution context, supplying the read filter of the run.
	 * @return JSON object with the page's instances as {@code label [id:TID]} plus pagination
	 *         metadata, marked as business data; or error JSON, unmarked, if the type was not found.
	 */
	@ToolMethod(description = "Get a paginated list of instances for a specific type, to look at a few of them. Each entry has the instance's id, label and type, plus the values of the 'attributes' you name - name the ones you need instead of calling getInstance per instance. Only instances you may read are listed, and the total count is the number of those. Never page through instances to select, sort, count or aggregate them: use query (to state a result) or present (to show objects) with a TL-Script expression. Page size: limit (default 20, max 200); cursor from the previous response for the next page.", mode = ToolMode.READ, group = ToolGroups.DATA_READ, marking = AnswerMarking.TOOL)
	public String listInstances(
			@P("The module containing the type") String moduleName,
			@P("The name of the type") String typeName,
			@P(value = "Names of attributes to include for every instance, e.g. [\"name\", \"status\"]", required = false) List<String> attributes,
			@P(value = "Maximum number of instances to return (default: 20, max: 200)", required = false) Integer limit,
			@P(value = "Cursor for pagination (null/empty for first page, use nextCursor from previous response)", required = false) String cursor,
			ToolExecutionContext context) {

		Map<String, Object> args = new HashMap<>();
		args.put("moduleName", moduleName);
		args.put("typeName", typeName);
		args.put("attributes", attributes);
		args.put("limit", limit);
		args.put("cursor", cursor);
		return ToolResults.invoke(InstanceBrowserTool.class, "listInstances()", args,
			"Error accessing instances: ",
			json -> ReadFilter.marked(moduleName + ":" + typeName, json),
			() -> doListInstances(moduleName, typeName, attributes, limit, cursor, ReadFilter.of(context)));
	}

	private InstanceListResponse doListInstances(String moduleName, String typeName, List<String> attributes,
			Integer limit, String cursor, ReadFilter readFilter) throws IOException {
		// Validate parameters
		if (moduleName == null || moduleName.isEmpty()) {
			throw new IllegalArgumentException("Module name is required");
		}
		if (typeName == null || typeName.isEmpty()) {
			throw new IllegalArgumentException("Type name is required");
		}

		// Validate and normalize limit
		int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
		if (effectiveLimit <= 0) {
			throw new IllegalArgumentException("Limit must be positive, got: " + effectiveLimit);
		}
		if (effectiveLimit > MAX_LIMIT) {
			effectiveLimit = MAX_LIMIT; // Clamp to maximum
		}

		// Decode cursor to offset
		int offset = PaginationCursor.decode(cursor);

		// Get the type using MetaElementUtil
		TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
		if (type == null) {
			throw new IllegalArgumentException(
				"Type '" + typeName + "' not found in module '" + moduleName + "'");
		}

		// Every instance the principal may read, and no other. Filtered before the count is taken and
		// before the page is cut: a total taken over the unfiltered list would disclose how many
		// instances exist, and a page cut before the filter would return short pages whose length says
		// the same thing.
		List<TLObject> allInstances =
			readFilter.readable(MetaElementUtil.getAllInstancesOf(type, TLObject.class));
		int totalCount = allInstances.size();

		// Apply pagination
		List<TLObject> pageInstances = allInstances.stream()
			.skip(offset)
			.limit(effectiveLimit)
			.collect(Collectors.toList());

		// Calculate next cursor
		int nextOffset = offset + pageInstances.size();
		String nextCursor = (nextOffset < totalCount) ? PaginationCursor.encode(nextOffset) : null;
		boolean hasMore = nextOffset < totalCount;

		// Build response using data class
		InstanceListResponse result = InstanceListResponse.create();
		result.setModule(moduleName);
		result.setType(typeName);
		result.setTotalCount(totalCount);
		result.setLimit(effectiveLimit);
		result.setOffset(offset);

		// Identifier, label and type per instance: the identifier is what the model names the object
		// by in its next call, the label is what tells two of them apart, the type says what it is -
		// a subtype of the listed type included. All of them are business data, disclosed here only
		// because the filter above already said this instance may be seen.
		Set<String> wanted = attributeNames(attributes);
		for (TLObject instance : pageInstances) {
			ReadFilter.shown(instance);
			InstanceEntry entry = InstanceEntry.create()
				.setId(ReadFilter.idOf(instance))
				.setLabel(MetaLabelProvider.INSTANCE.getLabel(instance));
			TLStructuredType instanceType = instance.tType();
			if (instanceType != null) {
				entry.setType(TLModelUtil.qualifiedName(instanceType));
			}
			if (!wanted.isEmpty()) {
				// The same per-attribute read decisions getInstance makes, then cut to what was asked.
				for (Map.Entry<String, Object> attribute : readFilter.attributesOf(instance).entrySet()) {
					if (wanted.contains(attribute.getKey())) {
						entry.putAttribute(attribute.getKey(), serialize(attribute.getValue()));
					}
				}
			}
			result.addInstance(entry);
		}

		// Pagination metadata
		result.setNextCursor(nextCursor);
		result.setHasMore(hasMore);
		if (hasMore) {
			result.setHint("There are " + totalCount + " instances. Do not page through them to select, sort,"
				+ " count or aggregate: put that into a TL-Script expression for query (to state the result)"
				+ " or present (to show the objects), and the server computes it.");
		}

		return result;
	}

	/**
	 * Utility class for encoding and decoding pagination cursors.
	 */
	private static final class PaginationCursor {

		/**
		 * Encodes an offset into a Base64 cursor string.
		 *
		 * @param offset
		 *        The offset to encode (must be non-negative).
		 * @return Base64-encoded cursor string.
		 * @throws IllegalArgumentException
		 *         if offset is negative.
		 */
		static String encode(int offset) {
			if (offset < 0) {
				throw new IllegalArgumentException("Offset must be non-negative: " + offset);
			}
			return Base64.getEncoder().encodeToString(
				String.valueOf(offset).getBytes(StandardCharsets.UTF_8)
			);
		}

		/**
		 * Decodes a Base64 cursor string into an offset.
		 *
		 * @param cursor
		 *        The cursor to decode (null or empty means offset 0).
		 * @return The decoded offset.
		 * @throws IllegalArgumentException
		 *         if cursor format is invalid or offset is negative.
		 */
		static int decode(String cursor) {
			if (cursor == null || cursor.isEmpty()) {
				return 0;
			}
			try {
				byte[] decoded = Base64.getDecoder().decode(cursor);
				int offset = Integer.parseInt(new String(decoded, StandardCharsets.UTF_8));
				if (offset < 0) {
					throw new IllegalArgumentException("Decoded offset is negative: " + offset);
				}
				return offset;
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Invalid cursor format: " + cursor, ex);
			}
		}
	}

	/**
	 * Gets detailed information about instances by their TIDs.
	 *
	 * <p>
	 * Returns a JSON object with one entry per instance found - its TID, type information, the
	 * attribute values the principal may read, and metadata (container, transient status) - and one
	 * error sentence per identifier that names nothing readable. References to other objects are
	 * represented as structured objects with TIDs, and a referenced object the principal may not read
	 * is left out of them.
	 * </p>
	 *
	 * <p>
	 * A list rather than one identifier (#616): reading five rows used to be five calls, each of
	 * them a turn of the agent loop.
	 * </p>
	 *
	 * @param tids
	 *        The TopLogic identifiers in format "TypeName:id", at most {@link #MAX_IDS}.
	 * @param context
	 *        Injected execution context, supplying the read filter of the run.
	 * @return JSON object with the instances, marked as business data; an identifier that does not
	 *         exist and one the principal may not read get the same error sentence - deliberately,
	 *         see {@link TidResolver#resolve(String, com.top_logic.tl.ai.core.security.ModelSecurity)}.
	 */
	@ToolMethod(description = "Get detailed information about instances by their TIDs, including the attribute values you may read. Pass every id you need in one call (at most 50).", mode = ToolMode.READ, group = ToolGroups.DATA_READ, marking = AnswerMarking.TOOL)
	public String getInstance(@P("The TopLogic identifiers (TIDs) in format 'TypeName:id'") List<String> tids,
			ToolExecutionContext context) {

		Map<String, Object> args = new HashMap<>();
		args.put("tids", tids);
		return ToolResults.invoke(InstanceBrowserTool.class, "getInstance()", args,
			"Error retrieving instance: ",
			json -> ReadFilter.marked("getInstance", json),
			() -> doGetInstances(tids, ReadFilter.of(context)));
	}

	private InstancesResponse doGetInstances(List<String> tids, ReadFilter readFilter) throws IOException {
		Set<String> wanted = new LinkedHashSet<>();
		if (tids != null) {
			for (String tid : tids) {
				if (tid != null && !tid.isBlank()) {
					wanted.add(tid.trim());
				}
			}
		}
		if (wanted.isEmpty()) {
			throw new IllegalArgumentException("Pass at least one TID.");
		}
		if (wanted.size() > MAX_IDS) {
			throw new IllegalArgumentException("At most " + MAX_IDS + " TIDs per call, got " + wanted.size()
				+ ". To show many objects to the user, call present with a query where you have it.");
		}
		InstancesResponse result = InstancesResponse.create();
		for (String tid : wanted) {
			try {
				result.addInstance(doGetInstance(tid, readFilter));
			} catch (IllegalArgumentException ex) {
				// One identifier that names nothing does not hide the ones that do: this is reading,
				// and the model is told exactly which of its ids failed and why.
				result.addError(tid + ": " + ex.getMessage());
			}
		}
		return result;
	}

	/** The requested attribute names, trimmed, without the blank ones. */
	private static Set<String> attributeNames(List<String> attributes) {
		if (attributes == null) {
			return Set.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String name : attributes) {
			if (name != null && !name.isBlank()) {
				result.add(name.trim());
			}
		}
		return result;
	}

	private InstanceResponse doGetInstance(String tid, ReadFilter readFilter) throws IOException {
		// Refuses an object this run may not read, with the message of one that does not exist.
		TLObject tlObject = TidResolver.resolve(tid, readFilter.security());
		ReadFilter.shown(tlObject, tid);

		// Build response using data class
		InstanceResponse result = InstanceResponse.create();
		result.setTid(tid);

		// Type information
		TLStructuredType type = tlObject.tType();
		if (type != null) {
			result.setType(buildTypeInfo(type));
		}

		// The attribute values, each of them a per-attribute Read decision, each of them serialised by
		// the one serialiser that makes those decisions.
		for (Map.Entry<String, Object> attribute : readFilter.attributesOf(tlObject).entrySet()) {
			result.putAttribute(attribute.getKey(), serialize(attribute.getValue()));
		}

		// Additional metadata. The container is another object's identity, so it is disclosed only when
		// that object is readable too - a readable child of a hidden parent must not name the parent.
		InstanceMetadata metadata = InstanceMetadata.create();
		TLObject container = tlObject.tContainer();
		boolean containerVisible = container != null && readFilter.canRead(container);
		metadata.setContainer(containerVisible ? container.tId().toString() : null);
		metadata.setContainerReference(containerVisible && tlObject.tContainerReference() != null
			? tlObject.tContainerReference().getName() : null);
		metadata.setTransient(tlObject.tTransient());
		result.setMetadata(metadata);

		return result;
	}

	/**
	 * Builds type information data object.
	 */
	private static InstanceTypeInfo buildTypeInfo(TLStructuredType type) {
		InstanceTypeInfo typeInfo = InstanceTypeInfo.create();
		typeInfo.setName(type.getName());

		// Module information
		TLModule module = type.getModule();
		if (module != null) {
			typeInfo.setModule(module.getName());

			// Module label if available
			ResKey moduleKey = LabelVisitor.getModuleResourceKey(module);
			Resources resources = Resources.getInstance();
			String moduleLabel = resources.getString(moduleKey, null);
			if (moduleLabel != null && !moduleLabel.isEmpty()) {
				typeInfo.setModuleLabel(moduleLabel);
			}
		}

		// Type label if available
		ResKey typeKey = TLModelNamingConvention.getTypeLabelKey(type);
		Resources resources = Resources.getInstance();
		String typeLabel = resources.getString(typeKey, null);
		if (typeLabel != null && !typeLabel.isEmpty()) {
			typeInfo.setLabel(typeLabel);
		}

		// Model kind
		ModelKind kind = type.getModelKind();
		if (kind != null) {
			typeInfo.setKind(kind.name().toLowerCase());
		}

		return typeInfo;
	}

	/**
	 * Writes an already-filtered value as a JSON string.
	 *
	 * <p>
	 * Takes the JSON-compatible data {@link ReadFilter#attributesOf(TLObject)} produced - primitives,
	 * {@link List}s, {@link Map}s - and never a live {@link TLObject}, which is why it makes no security
	 * decision. The serialisers that did take live objects, and decided nothing, are gone: there is one
	 * of those and it takes the run's rights as an argument.
	 * </p>
	 */
	private static String serialize(Object value) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			writeJsonData(json, value);
		}
		return buffer.toString();
	}

	private static void writeJsonData(JsonWriter json, Object value) throws IOException {
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
				writeJsonData(json, entry.getValue());
			}
			json.endObject();
		} else if (value instanceof Iterable<?> items) {
			json.beginArray();
			for (Object item : items) {
				writeJsonData(json, item);
			}
			json.endArray();
		} else {
			json.value(String.valueOf(value));
		}
	}
}
