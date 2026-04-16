package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttAxis}.
 */
public class GanttAxis_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttAxis {

	private String _providerId = "";

	private double _rangeMin = 0.0d;

	private double _rangeMax = 0.0d;

	private double _currentZoom = 1.0;

	private double _snapGranularity = 1.0;

	private final java.util.List<com.top_logic.react.flow.data.GanttTick> _currentTicks = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttTick>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttTick element) {
			_listener.beforeAdd(GanttAxis_Impl.this, CURRENT_TICKS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttTick element) {
			_listener.afterRemove(GanttAxis_Impl.this, CURRENT_TICKS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttAxis_Impl.this, CURRENT_TICKS__PROP);
		}
	};

	/**
	 * Creates a {@link GanttAxis_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttAxis#create()
	 */
	public GanttAxis_Impl() {
		super();
	}

	@Override
	public final String getProviderId() {
		return _providerId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setProviderId(String value) {
		internalSetProviderId(value);
		return this;
	}

	/** Internal setter for {@link #getProviderId()} without chain call utility. */
	protected final void internalSetProviderId(String value) {
		_listener.beforeSet(this, PROVIDER_ID__PROP, value);
		_providerId = value;
		_listener.afterChanged(this, PROVIDER_ID__PROP);
	}

	@Override
	public final double getRangeMin() {
		return _rangeMin;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setRangeMin(double value) {
		internalSetRangeMin(value);
		return this;
	}

	/** Internal setter for {@link #getRangeMin()} without chain call utility. */
	protected final void internalSetRangeMin(double value) {
		_listener.beforeSet(this, RANGE_MIN__PROP, value);
		_rangeMin = value;
		_listener.afterChanged(this, RANGE_MIN__PROP);
	}

	@Override
	public final double getRangeMax() {
		return _rangeMax;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setRangeMax(double value) {
		internalSetRangeMax(value);
		return this;
	}

	/** Internal setter for {@link #getRangeMax()} without chain call utility. */
	protected final void internalSetRangeMax(double value) {
		_listener.beforeSet(this, RANGE_MAX__PROP, value);
		_rangeMax = value;
		_listener.afterChanged(this, RANGE_MAX__PROP);
	}

	@Override
	public final double getCurrentZoom() {
		return _currentZoom;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setCurrentZoom(double value) {
		internalSetCurrentZoom(value);
		return this;
	}

	/** Internal setter for {@link #getCurrentZoom()} without chain call utility. */
	protected final void internalSetCurrentZoom(double value) {
		_listener.beforeSet(this, CURRENT_ZOOM__PROP, value);
		_currentZoom = value;
		_listener.afterChanged(this, CURRENT_ZOOM__PROP);
	}

	@Override
	public final double getSnapGranularity() {
		return _snapGranularity;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setSnapGranularity(double value) {
		internalSetSnapGranularity(value);
		return this;
	}

	/** Internal setter for {@link #getSnapGranularity()} without chain call utility. */
	protected final void internalSetSnapGranularity(double value) {
		_listener.beforeSet(this, SNAP_GRANULARITY__PROP, value);
		_snapGranularity = value;
		_listener.afterChanged(this, SNAP_GRANULARITY__PROP);
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GanttTick> getCurrentTicks() {
		return _currentTicks;
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis setCurrentTicks(java.util.List<? extends com.top_logic.react.flow.data.GanttTick> value) {
		internalSetCurrentTicks(value);
		return this;
	}

	/** Internal setter for {@link #getCurrentTicks()} without chain call utility. */
	protected final void internalSetCurrentTicks(java.util.List<? extends com.top_logic.react.flow.data.GanttTick> value) {
		if (value == null) throw new IllegalArgumentException("Property 'currentTicks' cannot be null.");
		_currentTicks.clear();
		_currentTicks.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttAxis addCurrentTick(com.top_logic.react.flow.data.GanttTick value) {
		internalAddCurrentTick(value);
		return this;
	}

	/** Implementation of {@link #addCurrentTick(com.top_logic.react.flow.data.GanttTick)} without chain call utility. */
	protected final void internalAddCurrentTick(com.top_logic.react.flow.data.GanttTick value) {
		_currentTicks.add(value);
	}

	@Override
	public final void removeCurrentTick(com.top_logic.react.flow.data.GanttTick value) {
		_currentTicks.remove(value);
	}

	@Override
	public String jsonType() {
		return GANTT_AXIS__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			PROVIDER_ID__PROP, 
			RANGE_MIN__PROP, 
			RANGE_MAX__PROP, 
			CURRENT_ZOOM__PROP, 
			SNAP_GRANULARITY__PROP, 
			CURRENT_TICKS__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case PROVIDER_ID__PROP: return getProviderId();
			case RANGE_MIN__PROP: return getRangeMin();
			case RANGE_MAX__PROP: return getRangeMax();
			case CURRENT_ZOOM__PROP: return getCurrentZoom();
			case SNAP_GRANULARITY__PROP: return getSnapGranularity();
			case CURRENT_TICKS__PROP: return getCurrentTicks();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PROVIDER_ID__PROP: internalSetProviderId((String) value); break;
			case RANGE_MIN__PROP: internalSetRangeMin((double) value); break;
			case RANGE_MAX__PROP: internalSetRangeMax((double) value); break;
			case CURRENT_ZOOM__PROP: internalSetCurrentZoom((double) value); break;
			case SNAP_GRANULARITY__PROP: internalSetSnapGranularity((double) value); break;
			case CURRENT_TICKS__PROP: internalSetCurrentTicks(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttTick.class, value)); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(PROVIDER_ID__PROP);
		out.value(getProviderId());
		out.name(RANGE_MIN__PROP);
		out.value(getRangeMin());
		out.name(RANGE_MAX__PROP);
		out.value(getRangeMax());
		out.name(CURRENT_ZOOM__PROP);
		out.value(getCurrentZoom());
		out.name(SNAP_GRANULARITY__PROP);
		out.value(getSnapGranularity());
		out.name(CURRENT_TICKS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttTick x : getCurrentTicks()) {
			x.writeTo(scope, out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case PROVIDER_ID__PROP: {
				out.value(getProviderId());
				break;
			}
			case RANGE_MIN__PROP: {
				out.value(getRangeMin());
				break;
			}
			case RANGE_MAX__PROP: {
				out.value(getRangeMax());
				break;
			}
			case CURRENT_ZOOM__PROP: {
				out.value(getCurrentZoom());
				break;
			}
			case SNAP_GRANULARITY__PROP: {
				out.value(getSnapGranularity());
				break;
			}
			case CURRENT_TICKS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttTick x : getCurrentTicks()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PROVIDER_ID__PROP: setProviderId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RANGE_MIN__PROP: setRangeMin(in.nextDouble()); break;
			case RANGE_MAX__PROP: setRangeMax(in.nextDouble()); break;
			case CURRENT_ZOOM__PROP: setCurrentZoom(in.nextDouble()); break;
			case SNAP_GRANULARITY__PROP: setSnapGranularity(in.nextDouble()); break;
			case CURRENT_TICKS__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttTick> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttTick.readGanttTick(scope, in));
				}
				in.endArray();
				setCurrentTicks(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case CURRENT_TICKS__PROP: {
				((com.top_logic.react.flow.data.GanttTick) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CURRENT_TICKS__PROP: {
				return com.top_logic.react.flow.data.GanttTick.readGanttTick(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

}
