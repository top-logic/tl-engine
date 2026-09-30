package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttEdge}.
 */
public class GanttEdge_Impl extends de.haumacher.msgbuf.graph.AbstractSharedGraphNode implements com.top_logic.react.flow.data.GanttEdge {

	private String _id = "";

	private transient java.lang.Object _userObject = null;

	private transient java.lang.Object _sourceModel = null;

	private String _sourceItemId = "";

	private com.top_logic.react.flow.data.GanttEndpoint _sourceEndpoint = com.top_logic.react.flow.data.GanttEndpoint.START;

	private transient java.lang.Object _targetModel = null;

	private String _targetItemId = "";

	private com.top_logic.react.flow.data.GanttEndpoint _targetEndpoint = com.top_logic.react.flow.data.GanttEndpoint.START;

	private com.top_logic.react.flow.data.GanttEnforce _enforce = com.top_logic.react.flow.data.GanttEnforce.NONE;

	private String _strokeColor = "#606060";

	private double _strokeWidth = 1.0;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GanttEdge_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GanttEdge_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttEdge_Impl.this, DASHES__PROP);
		}
	};

	private String _violatedStrokeColor = null;

	private double _violatedStrokeWidth = 0.0d;

	private final java.util.List<Double> _violatedDashes = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GanttEdge_Impl.this, VIOLATED_DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GanttEdge_Impl.this, VIOLATED_DASHES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttEdge_Impl.this, VIOLATED_DASHES__PROP);
		}
	};

	/**
	 * Creates a {@link GanttEdge_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttEdge#create()
	 */
	public GanttEdge_Impl() {
		super();
	}

	@Override
	public final String getId() {
		return _id;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_listener.beforeSet(this, ID__PROP, value);
		_id = value;
		_listener.afterChanged(this, ID__PROP);
	}

	@Override
	public final java.lang.Object getUserObject() {
		return _userObject;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	/** Internal setter for {@link #getUserObject()} without chain call utility. */
	protected final void internalSetUserObject(java.lang.Object value) {
		_listener.beforeSet(this, USER_OBJECT__PROP, value);
		_userObject = value;
		_listener.afterChanged(this, USER_OBJECT__PROP);
	}

	@Override
	public final boolean hasUserObject() {
		return _userObject != null;
	}

	@Override
	public final java.lang.Object getSourceModel() {
		return _sourceModel;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setSourceModel(java.lang.Object value) {
		internalSetSourceModel(value);
		return this;
	}

	/** Internal setter for {@link #getSourceModel()} without chain call utility. */
	protected final void internalSetSourceModel(java.lang.Object value) {
		_listener.beforeSet(this, SOURCE_MODEL__PROP, value);
		_sourceModel = value;
		_listener.afterChanged(this, SOURCE_MODEL__PROP);
	}

	@Override
	public final boolean hasSourceModel() {
		return _sourceModel != null;
	}

	@Override
	public final String getSourceItemId() {
		return _sourceItemId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setSourceItemId(String value) {
		internalSetSourceItemId(value);
		return this;
	}

	/** Internal setter for {@link #getSourceItemId()} without chain call utility. */
	protected final void internalSetSourceItemId(String value) {
		_listener.beforeSet(this, SOURCE_ITEM_ID__PROP, value);
		_sourceItemId = value;
		_listener.afterChanged(this, SOURCE_ITEM_ID__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEndpoint getSourceEndpoint() {
		return _sourceEndpoint;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		internalSetSourceEndpoint(value);
		return this;
	}

	/** Internal setter for {@link #getSourceEndpoint()} without chain call utility. */
	protected final void internalSetSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		if (value == null) throw new IllegalArgumentException("Property 'sourceEndpoint' cannot be null.");
		_listener.beforeSet(this, SOURCE_ENDPOINT__PROP, value);
		_sourceEndpoint = value;
		_listener.afterChanged(this, SOURCE_ENDPOINT__PROP);
	}

	@Override
	public final java.lang.Object getTargetModel() {
		return _targetModel;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setTargetModel(java.lang.Object value) {
		internalSetTargetModel(value);
		return this;
	}

	/** Internal setter for {@link #getTargetModel()} without chain call utility. */
	protected final void internalSetTargetModel(java.lang.Object value) {
		_listener.beforeSet(this, TARGET_MODEL__PROP, value);
		_targetModel = value;
		_listener.afterChanged(this, TARGET_MODEL__PROP);
	}

	@Override
	public final boolean hasTargetModel() {
		return _targetModel != null;
	}

	@Override
	public final String getTargetItemId() {
		return _targetItemId;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setTargetItemId(String value) {
		internalSetTargetItemId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetItemId()} without chain call utility. */
	protected final void internalSetTargetItemId(String value) {
		_listener.beforeSet(this, TARGET_ITEM_ID__PROP, value);
		_targetItemId = value;
		_listener.afterChanged(this, TARGET_ITEM_ID__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEndpoint getTargetEndpoint() {
		return _targetEndpoint;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		internalSetTargetEndpoint(value);
		return this;
	}

	/** Internal setter for {@link #getTargetEndpoint()} without chain call utility. */
	protected final void internalSetTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint value) {
		if (value == null) throw new IllegalArgumentException("Property 'targetEndpoint' cannot be null.");
		_listener.beforeSet(this, TARGET_ENDPOINT__PROP, value);
		_targetEndpoint = value;
		_listener.afterChanged(this, TARGET_ENDPOINT__PROP);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttEnforce getEnforce() {
		return _enforce;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setEnforce(com.top_logic.react.flow.data.GanttEnforce value) {
		internalSetEnforce(value);
		return this;
	}

	/** Internal setter for {@link #getEnforce()} without chain call utility. */
	protected final void internalSetEnforce(com.top_logic.react.flow.data.GanttEnforce value) {
		if (value == null) throw new IllegalArgumentException("Property 'enforce' cannot be null.");
		_listener.beforeSet(this, ENFORCE__PROP, value);
		_enforce = value;
		_listener.afterChanged(this, ENFORCE__PROP);
	}

	@Override
	public final String getStrokeColor() {
		return _strokeColor;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setStrokeColor(String value) {
		internalSetStrokeColor(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeColor()} without chain call utility. */
	protected final void internalSetStrokeColor(String value) {
		_listener.beforeSet(this, STROKE_COLOR__PROP, value);
		_strokeColor = value;
		_listener.afterChanged(this, STROKE_COLOR__PROP);
	}

	@Override
	public final double getStrokeWidth() {
		return _strokeWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setStrokeWidth(double value) {
		internalSetStrokeWidth(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeWidth()} without chain call utility. */
	protected final void internalSetStrokeWidth(double value) {
		_listener.beforeSet(this, STROKE_WIDTH__PROP, value);
		_strokeWidth = value;
		_listener.afterChanged(this, STROKE_WIDTH__PROP);
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge addDash(double value) {
		internalAddDash(value);
		return this;
	}

	/** Implementation of {@link #addDash(double)} without chain call utility. */
	protected final void internalAddDash(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDash(double value) {
		_dashes.remove(value);
	}

	@Override
	public final String getViolatedStrokeColor() {
		return _violatedStrokeColor;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setViolatedStrokeColor(String value) {
		internalSetViolatedStrokeColor(value);
		return this;
	}

	/** Internal setter for {@link #getViolatedStrokeColor()} without chain call utility. */
	protected final void internalSetViolatedStrokeColor(String value) {
		_listener.beforeSet(this, VIOLATED_STROKE_COLOR__PROP, value);
		_violatedStrokeColor = value;
		_listener.afterChanged(this, VIOLATED_STROKE_COLOR__PROP);
	}

	@Override
	public final boolean hasViolatedStrokeColor() {
		return _violatedStrokeColor != null;
	}

	@Override
	public final double getViolatedStrokeWidth() {
		return _violatedStrokeWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setViolatedStrokeWidth(double value) {
		internalSetViolatedStrokeWidth(value);
		return this;
	}

	/** Internal setter for {@link #getViolatedStrokeWidth()} without chain call utility. */
	protected final void internalSetViolatedStrokeWidth(double value) {
		_listener.beforeSet(this, VIOLATED_STROKE_WIDTH__PROP, value);
		_violatedStrokeWidth = value;
		_listener.afterChanged(this, VIOLATED_STROKE_WIDTH__PROP);
	}

	@Override
	public final java.util.List<Double> getViolatedDashes() {
		return _violatedDashes;
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge setViolatedDashes(java.util.List<? extends Double> value) {
		internalSetViolatedDashes(value);
		return this;
	}

	/** Internal setter for {@link #getViolatedDashes()} without chain call utility. */
	protected final void internalSetViolatedDashes(java.util.List<? extends Double> value) {
		_violatedDashes.clear();
		_violatedDashes.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttEdge addViolatedDash(double value) {
		internalAddViolatedDash(value);
		return this;
	}

	/** Implementation of {@link #addViolatedDash(double)} without chain call utility. */
	protected final void internalAddViolatedDash(double value) {
		_violatedDashes.add(value);
	}

	@Override
	public final void removeViolatedDash(double value) {
		_violatedDashes.remove(value);
	}

	@Override
	public String jsonType() {
		return GANTT_EDGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			USER_OBJECT__PROP, 
			SOURCE_MODEL__PROP, 
			SOURCE_ITEM_ID__PROP, 
			SOURCE_ENDPOINT__PROP, 
			TARGET_MODEL__PROP, 
			TARGET_ITEM_ID__PROP, 
			TARGET_ENDPOINT__PROP, 
			ENFORCE__PROP, 
			STROKE_COLOR__PROP, 
			STROKE_WIDTH__PROP, 
			DASHES__PROP, 
			VIOLATED_STROKE_COLOR__PROP, 
			VIOLATED_STROKE_WIDTH__PROP, 
			VIOLATED_DASHES__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				USER_OBJECT__PROP, 
				SOURCE_MODEL__PROP, 
				TARGET_MODEL__PROP));
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
			case ID__PROP: return getId();
			case USER_OBJECT__PROP: return getUserObject();
			case SOURCE_MODEL__PROP: return getSourceModel();
			case SOURCE_ITEM_ID__PROP: return getSourceItemId();
			case SOURCE_ENDPOINT__PROP: return getSourceEndpoint();
			case TARGET_MODEL__PROP: return getTargetModel();
			case TARGET_ITEM_ID__PROP: return getTargetItemId();
			case TARGET_ENDPOINT__PROP: return getTargetEndpoint();
			case ENFORCE__PROP: return getEnforce();
			case STROKE_COLOR__PROP: return getStrokeColor();
			case STROKE_WIDTH__PROP: return getStrokeWidth();
			case DASHES__PROP: return getDashes();
			case VIOLATED_STROKE_COLOR__PROP: return getViolatedStrokeColor();
			case VIOLATED_STROKE_WIDTH__PROP: return getViolatedStrokeWidth();
			case VIOLATED_DASHES__PROP: return getViolatedDashes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case USER_OBJECT__PROP: internalSetUserObject((java.lang.Object) value); break;
			case SOURCE_MODEL__PROP: internalSetSourceModel((java.lang.Object) value); break;
			case SOURCE_ITEM_ID__PROP: internalSetSourceItemId((String) value); break;
			case SOURCE_ENDPOINT__PROP: internalSetSourceEndpoint((com.top_logic.react.flow.data.GanttEndpoint) value); break;
			case TARGET_MODEL__PROP: internalSetTargetModel((java.lang.Object) value); break;
			case TARGET_ITEM_ID__PROP: internalSetTargetItemId((String) value); break;
			case TARGET_ENDPOINT__PROP: internalSetTargetEndpoint((com.top_logic.react.flow.data.GanttEndpoint) value); break;
			case ENFORCE__PROP: internalSetEnforce((com.top_logic.react.flow.data.GanttEnforce) value); break;
			case STROKE_COLOR__PROP: internalSetStrokeColor((String) value); break;
			case STROKE_WIDTH__PROP: internalSetStrokeWidth((double) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			case VIOLATED_STROKE_COLOR__PROP: internalSetViolatedStrokeColor((String) value); break;
			case VIOLATED_STROKE_WIDTH__PROP: internalSetViolatedStrokeWidth((double) value); break;
			case VIOLATED_DASHES__PROP: internalSetViolatedDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ID__PROP);
		out.value(getId());
		out.name(SOURCE_ITEM_ID__PROP);
		out.value(getSourceItemId());
		out.name(SOURCE_ENDPOINT__PROP);
		getSourceEndpoint().writeTo(out);
		out.name(TARGET_ITEM_ID__PROP);
		out.value(getTargetItemId());
		out.name(TARGET_ENDPOINT__PROP);
		getTargetEndpoint().writeTo(out);
		out.name(ENFORCE__PROP);
		getEnforce().writeTo(out);
		out.name(STROKE_COLOR__PROP);
		out.value(getStrokeColor());
		out.name(STROKE_WIDTH__PROP);
		out.value(getStrokeWidth());
		out.name(DASHES__PROP);
		out.beginArray();
		for (double x : getDashes()) {
			out.value(x);
		}
		out.endArray();
		if (hasViolatedStrokeColor()) {
			out.name(VIOLATED_STROKE_COLOR__PROP);
			out.value(getViolatedStrokeColor());
		}
		out.name(VIOLATED_STROKE_WIDTH__PROP);
		out.value(getViolatedStrokeWidth());
		out.name(VIOLATED_DASHES__PROP);
		out.beginArray();
		for (double x : getViolatedDashes()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: {
				out.value(getId());
				break;
			}
			case USER_OBJECT__PROP: {
				if (hasUserObject()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case SOURCE_MODEL__PROP: {
				if (hasSourceModel()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case SOURCE_ITEM_ID__PROP: {
				out.value(getSourceItemId());
				break;
			}
			case SOURCE_ENDPOINT__PROP: {
				getSourceEndpoint().writeTo(out);
				break;
			}
			case TARGET_MODEL__PROP: {
				if (hasTargetModel()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case TARGET_ITEM_ID__PROP: {
				out.value(getTargetItemId());
				break;
			}
			case TARGET_ENDPOINT__PROP: {
				getTargetEndpoint().writeTo(out);
				break;
			}
			case ENFORCE__PROP: {
				getEnforce().writeTo(out);
				break;
			}
			case STROKE_COLOR__PROP: {
				out.value(getStrokeColor());
				break;
			}
			case STROKE_WIDTH__PROP: {
				out.value(getStrokeWidth());
				break;
			}
			case DASHES__PROP: {
				out.beginArray();
				for (double x : getDashes()) {
					out.value(x);
				}
				out.endArray();
				break;
			}
			case VIOLATED_STROKE_COLOR__PROP: {
				if (hasViolatedStrokeColor()) {
					out.value(getViolatedStrokeColor());
				} else {
					out.nullValue();
				}
				break;
			}
			case VIOLATED_STROKE_WIDTH__PROP: {
				out.value(getViolatedStrokeWidth());
				break;
			}
			case VIOLATED_DASHES__PROP: {
				out.beginArray();
				for (double x : getViolatedDashes()) {
					out.value(x);
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
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SOURCE_ITEM_ID__PROP: setSourceItemId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SOURCE_ENDPOINT__PROP: setSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint.readGanttEndpoint(in)); break;
			case TARGET_ITEM_ID__PROP: setTargetItemId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_ENDPOINT__PROP: setTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint.readGanttEndpoint(in)); break;
			case ENFORCE__PROP: setEnforce(com.top_logic.react.flow.data.GanttEnforce.readGanttEnforce(in)); break;
			case STROKE_COLOR__PROP: setStrokeColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STROKE_WIDTH__PROP: setStrokeWidth(in.nextDouble()); break;
			case DASHES__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setDashes(newValue);
			}
			break;
			case VIOLATED_STROKE_COLOR__PROP: setViolatedStrokeColor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VIOLATED_STROKE_WIDTH__PROP: setViolatedStrokeWidth(in.nextDouble()); break;
			case VIOLATED_DASHES__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setViolatedDashes(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				out.value(((double) element));
				break;
			}
			case VIOLATED_DASHES__PROP: {
				out.value(((double) element));
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DASHES__PROP: {
				return in.nextDouble();
			}
			case VIOLATED_DASHES__PROP: {
				return in.nextDouble();
			}
			default: return super.readElement(scope, in, field);
		}
	}

}
