/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.calendar.CalendarViewControl;
import com.top_logic.layout.react.control.calendar.CalendarViewControl.Granularity;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ExpressionCalendarModel.EventExprs;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
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
	public interface Config extends UIElement.Config, Inputs {

		@Override
		@ClassDefault(CalendarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getObjects()}. */
		String OBJECTS = "objects";

		/** Configuration name for {@link #getEvent()}. */
		String EVENT = "event";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getRangeStart()}. */
		String RANGE_START = "range-start";

		/** Configuration name for {@link #getRangeEnd()}. */
		String RANGE_END = "range-end";

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

		/** Configuration name for {@link #getEntryAs()}. */
		String ENTRY_AS = "entry-as";

		/** Name of the variable holding the business object of an event when no other name is given. */
		String DEFAULT_ENTRY_AS = "entry";

		/** Name of the variable holding the start of an event in an edit. */
		String START_VARIABLE = "start";

		/** Name of the variable holding the end of an event in an edit. */
		String END_VARIABLE = "end";

		/** Name of the variable holding whether a created event spans whole days. */
		String ALL_DAY_VARIABLE = "allDay";

		/** Name of the variable holding the title typed for a created event. */
		String TITLE_VARIABLE = "title";

		/**
		 * TL-Script expression computing the business objects shown as events (a
		 * {@link Collection}).
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names.
		 * </p>
		 */
		@Name(OBJECTS)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getObjects();

		/**
		 * The per-field expressions deriving a calendar event from a business object.
		 */
		@Name(EVENT)
		@Mandatory
		EventConfig getEvent();

		/**
		 * The name of the variable holding the business object of an event in the scripts evaluated
		 * for an event: the fields of the {@link #getEvent() event} and the write-back expressions
		 * {@link #getOnMove()} and {@link #getOnResize()}.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_ENTRY_AS}. With
		 * <code>entry-as="appointment"</code>, the start of an event is computed by
		 * <code>$appointment.get(`my:Appointment#start`)</code>. An input of the same name is a
		 * configuration error and is renamed with <code>as</code>.
		 * </p>
		 */
		@Name(ENTRY_AS)
		@Nullable
		String getEntryAs();

		/**
		 * Optional {@link ViewChannel} to write the selected event's business object to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Optional {@link ViewChannel} the control writes the displayed interval's inclusive start to,
		 * so a supplier can load only the visible window's events. Effective only together with
		 * {@link #getRangeEnd()}.
		 */
		@Name(RANGE_START)
		@Format(ChannelRefFormat.class)
		ChannelRef getRangeStart();

		/**
		 * Optional {@link ViewChannel} the control writes the displayed interval's exclusive end to.
		 * Effective only together with {@link #getRangeStart()}.
		 */
		@Name(RANGE_END)
		@Format(ChannelRefFormat.class)
		ChannelRef getRangeEnd();

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
		 * Write-back expression applied when the user drag-moves an event. Without it, moving is
		 * disabled.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, the business object of the event as the
		 * variable {@value #DEFAULT_ENTRY_AS} (or by the name given with {@value #ENTRY_AS}), and its
		 * new start and end as the variables {@value #START_VARIABLE} and {@value #END_VARIABLE}:
		 * </p>
		 *
		 * <pre>
		 * { $entry.set(`my:Appointment#start`, $start); $entry.set(`my:Appointment#end`, $end); }
		 * </pre>
		 */
		@Name(ON_MOVE)
		@Supplies(value = DEFAULT_ENTRY_AS, renamedBy = ENTRY_AS)
		@Supplies(START_VARIABLE)
		@Supplies(END_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getOnMove();

		/**
		 * Write-back expression applied when the user resizes an event. Without it, resizing is
		 * disabled.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, the business object of the event as the
		 * variable {@value #DEFAULT_ENTRY_AS} (or by the name given with {@value #ENTRY_AS}), and its
		 * new end as the variable {@value #END_VARIABLE}.
		 * </p>
		 */
		@Name(ON_RESIZE)
		@Supplies(value = DEFAULT_ENTRY_AS, renamedBy = ENTRY_AS)
		@Supplies(END_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getOnResize();

		/**
		 * Write-back expression applied when the user selects an empty slot and confirms the title
		 * typed into the calendar, returning the created business object. Without it, creation is
		 * disabled.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, the start and end of the selected slot as the
		 * variables {@value #START_VARIABLE} and {@value #END_VARIABLE}, whether the slot spans whole
		 * days as {@value #ALL_DAY_VARIABLE}, and the typed title as {@value #TITLE_VARIABLE}.
		 * </p>
		 */
		@Name(ON_CREATE)
		@Supplies(START_VARIABLE)
		@Supplies(END_VARIABLE)
		@Supplies(ALL_DAY_VARIABLE)
		@Supplies(TITLE_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getOnCreate();
	}

	/**
	 * The per-field expressions deriving a calendar event from a business object.
	 *
	 * <p>
	 * Each expression sees the values of the calendar's {@link Config#getInputs() inputs} as
	 * variables named after their channels, or by their {@code as} names, and the business object
	 * as the variable {@value Config#DEFAULT_ENTRY_AS}, or by the name the calendar gives it with
	 * {@value Config#ENTRY_AS}:
	 * </p>
	 *
	 * <pre>
	 * &lt;start&gt;$entry.get(`my:Appointment#start`)&lt;/start&gt;
	 * </pre>
	 */
	public interface EventConfig extends ConfigPart {

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

		/** Configuration name for {@link #getColor()}. */
		String COLOR = "color";

		/** Configuration name for {@link #getMovable()}. */
		String MOVABLE = "movable";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/**
		 * Expression computing the event's start time (a date), required.
		 */
		@Name(START)
		@Mandatory
		@NonNullable
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getStart();

		/**
		 * Expression computing the event's end time (a date). Without it, events last one hour.
		 */
		@Name(END)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getEnd();

		/**
		 * Expression computing whether the event spans whole days. Without it, events are timed.
		 */
		@Name(ALL_DAY)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getAllDay();

		/**
		 * Expression computing the event's title.
		 */
		@Name(TITLE)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTitle();

		/**
		 * Expression computing the event's hover tooltip.
		 */
		@Name(TOOLTIP)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();

		/**
		 * Expression computing the event's category key, which selects its color.
		 */
		@Name(CATEGORY)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCategory();

		/**
		 * Expression computing the event's CSS color, taking precedence over the color that the
		 * category key selects. A value of the model type <code>tl.util:Color</code> is accepted
		 * directly.
		 */
		@Name(COLOR)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColor();

		/**
		 * Expression computing whether the event may be moved. Without it, events are movable.
		 */
		@Name(MOVABLE)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getMovable();

		/**
		 * Expression computing whether the event may be resized. Without it, events are resizable.
		 */
		@Name(RESIZABLE)
		@Supplies(value = Config.DEFAULT_ENTRY_AS, renamedBy = Config.ENTRY_AS)
		@ScriptContextVariables(InputScript.Variables.class)
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
		_objectsExecutor = InputScript.compile(config, Config.OBJECTS);

		EventConfig event = config.getEvent();
		_eventExprs = new EventExprs(
			InputScript.compile(event, EventConfig.START),
			InputScript.compile(event, EventConfig.END),
			InputScript.compile(event, EventConfig.ALL_DAY),
			InputScript.compile(event, EventConfig.TITLE),
			InputScript.compile(event, EventConfig.TOOLTIP),
			InputScript.compile(event, EventConfig.CATEGORY),
			InputScript.compile(event, EventConfig.COLOR),
			InputScript.compile(event, EventConfig.MOVABLE),
			InputScript.compile(event, EventConfig.RESIZABLE));

		_onMove = InputScript.compile(config, Config.ON_MOVE);
		_onResize = InputScript.compile(config, Config.ON_RESIZE);
		_onCreate = InputScript.compile(config, Config.ON_CREATE);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(context, _config.getInputs());
		Collection<?> objects = executeObjectsQuery(_objectsExecutor, ChannelInputs.arguments(inputChannels));

		ExpressionCalendarModel model =
			new ExpressionCalendarModel(inputChannels, objects, _eventExprs, _onMove, _onResize, _onCreate);

		CalendarViewControl control = new CalendarViewControl(context, model);
		Granularity granularity = _config.getGranularity();
		control.setGranularity(granularity != null ? granularity : Granularity.WEEK);
		control.setEditable(_config.isEditable());
		control.setWorkingHours(_config.getDayStartHour(), _config.getDayEndHour());
		control.setCssClass(_config.getCssClass());

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
			Registration channelRegistration = selectionChannel.addListener(channelListener);
			control.addCleanupAction(channelRegistration::dispose);
			reapplySelection[0].run();
		}

		// Refresh the objects when observed objects change or an input channel changes.
		QueryExecutor objectsExecutor = _objectsExecutor;
		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			new ArrayList<>(objects),
			args -> new ArrayList<>(executeObjectsQuery(objectsExecutor, args)),
			ObservedTypes.resolve(_config.getObservedTypes()),
			inputChannels,
			elements -> {
				model.setObjects(elements);
				if (reapplySelection[0] != null) {
					reapplySelection[0].run();
				}
			});
		// Observe the model only while the calendar is displayed: a calendar that is not attached - the
		// inactive child of a tab bar, a page nobody looks at - must not react to model changes.
		control.addAttachListener(() -> observer.attach(context.getModelScope()));
		control.addDetachListener(observer::detach);

		// Publish the displayed interval to the range channels. Registered after the observer's attach
		// listener (both fire on attach, in registration order) so that seeding the channels triggers the
		// already-attached observer to load the displayed window's events.
		ChannelRef rangeStartRef = _config.getRangeStart();
		ChannelRef rangeEndRef = _config.getRangeEnd();
		if (rangeStartRef != null && rangeEndRef != null) {
			ViewChannel rangeStartChannel = context.resolveChannel(rangeStartRef);
			ViewChannel rangeEndChannel = context.resolveChannel(rangeEndRef);
			control.addAttachListener(() -> control.setRangeListener((from, to) -> {
				rangeStartChannel.set(from);
				rangeEndChannel.set(to);
			}));
			control.addDetachListener(() -> control.setRangeListener(null));
		}

		return control;
	}

	private static Collection<?> executeObjectsQuery(QueryExecutor objectsExecutor, Object[] channelValues) {
		Object result = objectsExecutor.execute(channelValues);
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

}
