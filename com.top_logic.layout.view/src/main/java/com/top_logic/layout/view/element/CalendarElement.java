/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.calendar.CalendarViewControl;
import com.top_logic.layout.react.control.calendar.CalendarViewControl.Granularity;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ExpressionCalendarModel.EventExprs;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Declarative {@link UIElement} rendering a model-defined calendar (the {@code <calendar>} tag)
 * through a {@link CalendarViewControl}.
 *
 * <p>
 * The displayed business objects come from a TL-Script expression over the configured input
 * {@link ViewChannel}s; each object's event fields (start, end, title, …) are derived by the
 * per-field expressions of the {@code <event>} sub-element. Drag edits run the configured
 * {@code on-move} / {@code on-resize} / {@code on-create} write-back expressions inside a
 * transaction. The selected event's business object is written to the {@code selection} channel.
 * </p>
 */
public class CalendarElement implements UIElement {

	/**
	 * Configuration for {@link CalendarElement}.
	 */
	@TagName("calendar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(CalendarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getObjects()}. */
		String OBJECTS = "objects";

		/** Configuration name for {@link #getEvent()}. */
		String EVENT = "event";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getGranularity()}. */
		String GRANULARITY = "granularity";

		/** Configuration name for {@link #isEditable()}. */
		String EDITABLE = "editable";

		/** Configuration name for {@link #getDayStartHour()}. */
		String DAY_START_HOUR = "day-start-hour";

		/** Configuration name for {@link #getDayEndHour()}. */
		String DAY_END_HOUR = "day-end-hour";

		/** Configuration name for {@link #getOnMove()}. */
		String ON_MOVE = "on-move";

		/** Configuration name for {@link #getOnResize()}. */
		String ON_RESIZE = "on-resize";

		/** Configuration name for {@link #getOnCreate()}. */
		String ON_CREATE = "on-create";

		/**
		 * References to {@link ViewChannel}s whose values become positional arguments to
		 * {@link #getObjects()}.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the business objects shown as events (a {@link Collection}).
		 */
		@Name(OBJECTS)
		@Mandatory
		@NonNullable
		Expr getObjects();

		/**
		 * The per-field expressions deriving a calendar event from a business object.
		 */
		@Name(EVENT)
		@Mandatory
		EventConfig getEvent();

		/**
		 * Optional {@link ViewChannel} to write the selected event's business object to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
		 * {@link #getObjects() objects}, so the calendar refreshes automatically.
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * The initially displayed granularity.
		 */
		@Name(GRANULARITY)
		Granularity getGranularity();

		/**
		 * Whether the user may edit events by dragging.
		 */
		@Name(EDITABLE)
		@BooleanDefault(true)
		boolean isEditable();

		/**
		 * The first working hour highlighted in the time grid.
		 */
		@Name(DAY_START_HOUR)
		@IntDefault(8)
		int getDayStartHour();

		/**
		 * The hour after the last working hour highlighted in the time grid.
		 */
		@Name(DAY_END_HOUR)
		@IntDefault(18)
		int getDayEndHour();

		/**
		 * Write-back function <code>object, newStart, newEnd -&gt; …</code> applied when the user
		 * drag-moves an event. Without it, moving is disabled.
		 */
		@Name(ON_MOVE)
		Expr getOnMove();

		/**
		 * Write-back function <code>object, newEnd -&gt; …</code> applied when the user resizes an
		 * event. Without it, resizing is disabled.
		 */
		@Name(ON_RESIZE)
		Expr getOnResize();

		/**
		 * Write-back function <code>start, end, allDay -&gt; object</code> applied when the user
		 * selects an empty slot. Without it, creation is disabled.
		 */
		@Name(ON_CREATE)
		Expr getOnCreate();
	}

	/**
	 * The per-field expressions deriving a calendar event from a business object. Each is a
	 * single-argument function receiving the object.
	 */
	public interface EventConfig extends ConfigurationItem {

		/** Configuration name for {@link #getStart()}. */
		String START = "start";

		/** Configuration name for {@link #getEnd()}. */
		String END = "end";

		/** Configuration name for {@link #getAllDay()}. */
		String ALL_DAY = "all-day";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getCategory()}. */
		String CATEGORY = "category";

		/** Configuration name for {@link #getMovable()}. */
		String MOVABLE = "movable";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/**
		 * Function computing the event's start time (a date), required.
		 */
		@Name(START)
		@Mandatory
		@NonNullable
		Expr getStart();

		/**
		 * Function computing the event's end time (a date). Without it, events last one hour.
		 */
		@Name(END)
		Expr getEnd();

		/**
		 * Function computing whether the event spans whole days. Without it, events are timed.
		 */
		@Name(ALL_DAY)
		Expr getAllDay();

		/**
		 * Function computing the event's title.
		 */
		@Name(TITLE)
		Expr getTitle();

		/**
		 * Function computing the event's hover tooltip.
		 */
		@Name(TOOLTIP)
		Expr getTooltip();

		/**
		 * Function computing the event's category key, which selects its color.
		 */
		@Name(CATEGORY)
		Expr getCategory();

		/**
		 * Function computing whether the event may be moved. Without it, events are movable.
		 */
		@Name(MOVABLE)
		Expr getMovable();

		/**
		 * Function computing whether the event may be resized. Without it, events are resizable.
		 */
		@Name(RESIZABLE)
		Expr getResizable();
	}

	private final Config _config;

	private final QueryExecutor _objectsExecutor;

	private final EventExprs _eventExprs;

	private final QueryExecutor _onMove;

	private final QueryExecutor _onResize;

	private final QueryExecutor _onCreate;

	/**
	 * Creates a {@link CalendarElement} from configuration.
	 */
	@CalledByReflection
	public CalendarElement(InstantiationContext context, Config config) {
		_config = config;
		_objectsExecutor = QueryExecutor.compile(config.getObjects());

		EventConfig event = config.getEvent();
		_eventExprs = new EventExprs(
			QueryExecutor.compile(event.getStart()),
			QueryExecutor.compileOptional(event.getEnd()),
			QueryExecutor.compileOptional(event.getAllDay()),
			QueryExecutor.compileOptional(event.getTitle()),
			QueryExecutor.compileOptional(event.getTooltip()),
			QueryExecutor.compileOptional(event.getCategory()),
			QueryExecutor.compileOptional(event.getMovable()),
			QueryExecutor.compileOptional(event.getResizable()));

		_onMove = QueryExecutor.compileOptional(config.getOnMove());
		_onResize = QueryExecutor.compileOptional(config.getOnResize());
		_onCreate = QueryExecutor.compileOptional(config.getOnCreate());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = new ArrayList<>();
		for (ChannelRef ref : _config.getInputs()) {
			inputChannels.add(context.resolveChannel(ref));
		}
		Collection<?> objects = executeObjectsQuery(_objectsExecutor, readChannelValues(inputChannels));

		ExpressionCalendarModel model =
			new ExpressionCalendarModel(objects, _eventExprs, _onMove, _onResize, _onCreate);

		CalendarViewControl control = new CalendarViewControl(context, model);
		Granularity granularity = _config.getGranularity();
		control.setGranularity(granularity != null ? granularity : Granularity.WEEK);
		control.setEditable(_config.isEditable());
		control.setWorkingHours(_config.getDayStartHour(), _config.getDayEndHour());

		// Reflects the selection channel's current value as the calendar's selection; re-applied after
		// data refreshes so a just-created object still gets selected.
		Runnable[] reapplySelection = {null};

		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			boolean[] applyingFromChannel = {false};
			control.setSelectionListener(businessObject -> {
				if (applyingFromChannel[0]) {
					return;
				}
				selectionChannel.set(businessObject);
			});
			reapplySelection[0] = () -> {
				Object value = selectionChannel.get();
				applyingFromChannel[0] = true;
				try {
					control.setSelectedBusinessObject(value);
				} finally {
					applyingFromChannel[0] = false;
				}
			};
			ViewChannel.ChannelListener channelListener = (sender, oldValue, newValue) -> reapplySelection[0].run();
			selectionChannel.addListener(channelListener);
			control.addCleanupAction(() -> selectionChannel.removeListener(channelListener));
			reapplySelection[0].run();
		}

		// Refresh the objects when observed objects change or an input channel changes.
		QueryExecutor objectsExecutor = _objectsExecutor;
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			new ArrayList<>(objects),
			args -> new ArrayList<>(executeObjectsQuery(objectsExecutor, args)),
			resolveObservedTypes(),
			inputChannels,
			elements -> {
				model.setObjects(elements);
				if (reapplySelection[0] != null) {
					reapplySelection[0].run();
				}
			});
		control.addBeforeWriteAction(() -> observer.attach(context.getModelScope()));
		control.addCleanupAction(observer::detach);

		return control;
	}

	private Set<TLStructuredType> resolveObservedTypes() {
		List<TLModelPartRef> refs = _config.getObservedTypes();
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

	private static Collection<?> executeObjectsQuery(QueryExecutor objectsExecutor, Object[] channelValues) {
		Object result = objectsExecutor.execute(channelValues);
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

}
