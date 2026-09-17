/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.ai.chat.view.list.DataListElement.Config;
import com.top_logic.ai.chat.view.list.DataListElement.LineConfig;
import com.top_logic.ai.chat.view.list.DataListElement.LinesConfig;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <data-list>}.
 *
 * <p>
 * Runs the {@code rows} TL-Script expression, serialises each row to
 * {@code {id, primary, secondary?, group?}} and {@code putState}s the array to the {@code TLDataList}
 * React component; re-runs and re-pushes when observed model objects change. A row click arrives as
 * the {@code select} command carrying the row id; the control resolves it to the row object and
 * writes it to the two-way {@code selection} channel (and reflects an incoming channel value as the
 * selected id). The channel holds the {@link TLObject} so a consumer like {@code <chat-thread>} reads
 * the object directly.
 * </p>
 */
public class DataListControl extends ReactControl {

	private final QueryExecutor _rowsExecutor;

	private final List<LineSpec> _lineSpecs = new ArrayList<>();

	private final QueryExecutor _groupExecutor;

	private final List<ViewChannel> _inputChannels = new ArrayList<>();

	private final Set<TLStructuredType> _observedTypes;

	private final ViewChannel _selectionChannel;

	private final ViewChannel _createChannel;

	private long _createTick;

	private final boolean[] _applyingFromChannel = { false };

	/** The row objects of the last serialisation, by their id, for the {@code select} lookup. */
	private Map<String, TLObject> _rowsById = Map.of();

	/** Creates a {@link DataListControl}. */
	public DataListControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLDataList"));

		_rowsExecutor = QueryExecutor.compile(config.getRows());
		_groupExecutor = config.getGroupBy() == null ? null : QueryExecutor.compile(config.getGroupBy());

		LinesConfig linesConfig = config.getLines();
		if (linesConfig != null) {
			for (LineConfig line : linesConfig.getLines()) {
				QueryExecutor value = line.getValue() == null ? null : QueryExecutor.compile(line.getValue());
				String emphasis = line.getEmphasis() == null ? null : line.getEmphasis().getExternalName();
				_lineSpecs.add(new LineSpec(line.getAttribute(), value, emphasis));
			}
		}
		_observedTypes = resolveObservedTypes(config.getObservedTypes());

		for (ChannelRef ref : config.getInputs()) {
			_inputChannels.add(context.resolveChannel(ref));
		}

		putState("search", Boolean.valueOf(config.getSearch()));
		putState("collapsible", Boolean.valueOf(config.getCollapsible()));
		putState("title", labelOf(config.getTitle()));
		putState("emptyText", labelOf(config.getEmptyText()));
		putState("create", Boolean.valueOf(config.getCreate()));
		putState("createLabel", labelOf(config.getCreateLabel()));
		putState("language", currentLanguage());

		ChannelRef createRef = config.getCreateChannel();
		_createChannel = createRef == null ? null : context.resolveChannel(createRef);

		ChannelRef selectionRef = config.getSelection();
		if (selectionRef == null) {
			_selectionChannel = null;
			putState("selectedId", "");
		} else {
			_selectionChannel = context.resolveChannel(selectionRef);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
			_selectionChannel.addListener(listener);
			addCleanupAction(() -> _selectionChannel.removeListener(listener));
		}

		List<Object> initial = new ArrayList<>(executeRowsQuery(_rowsExecutor, readChannelValues(_inputChannels)));
		serialize(initial);
		if (_selectionChannel != null) {
			applyFromChannel(_selectionChannel.get());
		}

		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initial,
			args -> new ArrayList<>(executeRowsQuery(_rowsExecutor, args)),
			_observedTypes,
			_inputChannels,
			this::serialize);
		addAttachListener(() -> observer.attach(context.getModelScope()));
		addDetachListener(observer::detach);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_selectionChannel != null) {
			applyFromChannel(_selectionChannel.get());
		}
	}

	/**
	 * Selects the row with the given id: resolves it to its object and writes it to the selection
	 * channel.
	 *
	 * @param args
	 *        {@code id} is the row id previously pushed in the {@code rows} state; empty clears the
	 *        selection.
	 */
	@ReactCommandHandler(value = "select", params = {
		@ReactParam(name = "id", type = "string", description = "Row id, or empty to clear.")
	})
	public void select(Map<String, Object> args) {
		if (_applyingFromChannel[0]) {
			return;
		}
		String id = stringArg(args, "id");
		if (_selectionChannel == null) {
			putState("selectedId", id);
			return;
		}
		TLObject object = id.isEmpty() ? null : _rowsById.get(id);
		_selectionChannel.set(object);
		putState("selectedId", object == null ? "" : idOf(object));
	}

	/**
	 * Pulses the create channel so a host command reacts to a "new" click. Each click writes a fresh
	 * value, so a listener fires every time.
	 */
	@ReactCommandHandler(value = "create", params = {})
	public void create(Map<String, Object> args) {
		if (_createChannel != null) {
			_createChannel.set(Long.valueOf(++_createTick));
		}
	}

	private void applyFromChannel(Object value) {
		_applyingFromChannel[0] = true;
		try {
			TLObject object = firstObject(value);
			putState("selectedId", object == null ? "" : idOf(object));
		} finally {
			_applyingFromChannel[0] = false;
		}
	}

	/** Serialises the rows to browser state and re-indexes them by id. */
	private void serialize(Collection<?> rows) {
		Map<String, TLObject> byId = new LinkedHashMap<>();
		List<Map<String, Object>> out = new ArrayList<>();
		for (Object row : rows) {
			if (!(row instanceof TLObject object)) {
				continue;
			}
			String id = idOf(object);
			if (id.isEmpty()) {
				continue;
			}
			byId.put(id, object);
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("id", id);
			entry.put("lines", lines(object));
			if (_groupExecutor != null) {
				entry.put("group", label(_groupExecutor.execute(object)));
			}
			out.add(entry);
		}
		_rowsById = byId;
		putState("rows", out);
	}

	/** The browser id of a row object: its stable knowledge-object name. */
	private static String idOf(TLObject object) {
		if (object == null || object.tHandle() == null) {
			return "";
		}
		return object.tHandle().getObjectName().toExternalForm();
	}

	/** The single {@link TLObject} a channel value names, or {@code null}. */
	private static TLObject firstObject(Object value) {
		if (value instanceof TLObject object) {
			return object;
		}
		if (value instanceof Collection<?> many && many.size() == 1) {
			Object only = many.iterator().next();
			return only instanceof TLObject object ? object : null;
		}
		return null;
	}

	/** The display lines for a row, honouring the configured attributes / values and emphasis. */
	private List<Map<String, Object>> lines(TLObject object) {
		List<Map<String, Object>> lines = new ArrayList<>();
		for (LineSpec spec : _lineSpecs) {
			Object value = spec.executor() != null
				? spec.executor().execute(object)
				: attributeValue(object, spec.attribute());
			Map<String, Object> line = new LinkedHashMap<>();
			line.put("text", label(value));
			if (spec.emphasis() != null) {
				line.put("emphasis", spec.emphasis());
			}
			lines.add(line);
		}
		if (lines.isEmpty()) {
			Map<String, Object> line = new LinkedHashMap<>();
			line.put("text", label(object));
			lines.add(line);
		}
		return lines;
	}

	private static Object attributeValue(TLObject object, String attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		TLStructuredType type = object.tType();
		TLStructuredTypePart part = type == null ? null : type.getPart(attribute);
		return part == null ? null : object.tValue(part);
	}

	/** A display label for an expression result (formats dates, objects and text uniformly). */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	private static String labelOf(ResKey key) {
		return key == null ? "" : Resources.getInstance().getString(key);
	}

	private static String currentLanguage() {
		TLContext tlContext = TLContext.getContext();
		Locale locale = tlContext == null ? null : tlContext.getCurrentLocale();
		return locale == null ? Locale.ENGLISH.getLanguage() : locale.getLanguage();
	}

	private static Set<TLStructuredType> resolveObservedTypes(List<TLModelPartRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int n = 0; n < channels.size(); n++) {
			values[n] = channels.get(n).get();
		}
		return values;
	}

	private static Collection<?> executeRowsQuery(QueryExecutor rowsExecutor, Object[] channelValues) {
		Object result = rowsExecutor.execute(channelValues);
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

	private static String stringArg(Map<String, Object> args, String key) {
		if (args == null) {
			return "";
		}
		Object value = args.get(key);
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}

	/** One configured display line: an attribute or a computed value, with an optional emphasis. */
	private record LineSpec(String attribute, QueryExecutor executor, String emphasis) {
		// A compact holder for a resolved <line> configuration.
	}
}
