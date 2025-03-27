package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Image}.
 */
public class Image_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Image {

	private String _href = "";

	private double _imgWidth = 0.0d;

	private double _imgHeight = 0.0d;

	private com.top_logic.graphic.flow.data.ImageAlign _align = com.top_logic.graphic.flow.data.ImageAlign.X_MID_YMID;

	private com.top_logic.graphic.flow.data.ImageScale _scale = com.top_logic.graphic.flow.data.ImageScale.MEET;

	/**
	 * Creates a {@link Image_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Image#create()
	 */
	public Image_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.IMAGE;
	}

	@Override
	public final String getHref() {
		return _href;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setHref(String value) {
		internalSetHref(value);
		return this;
	}

	/** Internal setter for {@link #getHref()} without chain call utility. */
	protected final void internalSetHref(String value) {
		_listener.beforeSet(this, HREF__PROP, value);
		_href = value;
	}

	@Override
	public final double getImgWidth() {
		return _imgWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setImgWidth(double value) {
		internalSetImgWidth(value);
		return this;
	}

	/** Internal setter for {@link #getImgWidth()} without chain call utility. */
	protected final void internalSetImgWidth(double value) {
		_listener.beforeSet(this, IMG_WIDTH__PROP, value);
		_imgWidth = value;
	}

	@Override
	public final double getImgHeight() {
		return _imgHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setImgHeight(double value) {
		internalSetImgHeight(value);
		return this;
	}

	/** Internal setter for {@link #getImgHeight()} without chain call utility. */
	protected final void internalSetImgHeight(double value) {
		_listener.beforeSet(this, IMG_HEIGHT__PROP, value);
		_imgHeight = value;
	}

	@Override
	public final com.top_logic.graphic.flow.data.ImageAlign getAlign() {
		return _align;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setAlign(com.top_logic.graphic.flow.data.ImageAlign value) {
		internalSetAlign(value);
		return this;
	}

	/** Internal setter for {@link #getAlign()} without chain call utility. */
	protected final void internalSetAlign(com.top_logic.graphic.flow.data.ImageAlign value) {
		if (value == null) throw new IllegalArgumentException("Property 'align' cannot be null.");
		_listener.beforeSet(this, ALIGN__PROP, value);
		_align = value;
	}

	@Override
	public final com.top_logic.graphic.flow.data.ImageScale getScale() {
		return _scale;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setScale(com.top_logic.graphic.flow.data.ImageScale value) {
		internalSetScale(value);
		return this;
	}

	/** Internal setter for {@link #getScale()} without chain call utility. */
	protected final void internalSetScale(com.top_logic.graphic.flow.data.ImageScale value) {
		if (value == null) throw new IllegalArgumentException("Property 'scale' cannot be null.");
		_listener.beforeSet(this, SCALE__PROP, value);
		_scale = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return IMAGE__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			HREF__PROP, 
			IMG_WIDTH__PROP, 
			IMG_HEIGHT__PROP, 
			ALIGN__PROP, 
			SCALE__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case HREF__PROP: return getHref();
			case IMG_WIDTH__PROP: return getImgWidth();
			case IMG_HEIGHT__PROP: return getImgHeight();
			case ALIGN__PROP: return getAlign();
			case SCALE__PROP: return getScale();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case HREF__PROP: internalSetHref((String) value); break;
			case IMG_WIDTH__PROP: internalSetImgWidth((double) value); break;
			case IMG_HEIGHT__PROP: internalSetImgHeight((double) value); break;
			case ALIGN__PROP: internalSetAlign((com.top_logic.graphic.flow.data.ImageAlign) value); break;
			case SCALE__PROP: internalSetScale((com.top_logic.graphic.flow.data.ImageScale) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(HREF__PROP);
		out.value(getHref());
		out.name(IMG_WIDTH__PROP);
		out.value(getImgWidth());
		out.name(IMG_HEIGHT__PROP);
		out.value(getImgHeight());
		out.name(ALIGN__PROP);
		getAlign().writeTo(out);
		out.name(SCALE__PROP);
		getScale().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case HREF__PROP: setHref(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IMG_WIDTH__PROP: setImgWidth(in.nextDouble()); break;
			case IMG_HEIGHT__PROP: setImgHeight(in.nextDouble()); break;
			case ALIGN__PROP: setAlign(com.top_logic.graphic.flow.data.ImageAlign.readImageAlign(in)); break;
			case SCALE__PROP: setScale(com.top_logic.graphic.flow.data.ImageScale.readImageScale(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return IMAGE__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(HREF__ID);
		out.value(getHref());
		out.name(IMG_WIDTH__ID);
		out.value(getImgWidth());
		out.name(IMG_HEIGHT__ID);
		out.value(getImgHeight());
		out.name(ALIGN__ID);
		getAlign().writeTo(out);
		out.name(SCALE__ID);
		getScale().writeTo(out);
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.Image} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.Image readImage_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Image_Impl result = new Image_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case HREF__ID: setHref(in.nextString()); break;
			case IMG_WIDTH__ID: setImgWidth(in.nextDouble()); break;
			case IMG_HEIGHT__ID: setImgHeight(in.nextDouble()); break;
			case ALIGN__ID: setAlign(com.top_logic.graphic.flow.data.ImageAlign.readImageAlign(in)); break;
			case SCALE__ID: setScale(com.top_logic.graphic.flow.data.ImageScale.readImageScale(in)); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Image} type. */
	public static final String IMAGE__XML_ELEMENT = "image";

	/** XML attribute or element name of a {@link #getHref} property. */
	private static final String HREF__XML_ATTR = "href";

	/** XML attribute or element name of a {@link #getImgWidth} property. */
	private static final String IMG_WIDTH__XML_ATTR = "img-width";

	/** XML attribute or element name of a {@link #getImgHeight} property. */
	private static final String IMG_HEIGHT__XML_ATTR = "img-height";

	/** XML attribute or element name of a {@link #getAlign} property. */
	private static final String ALIGN__XML_ATTR = "align";

	/** XML attribute or element name of a {@link #getScale} property. */
	private static final String SCALE__XML_ATTR = "scale";

	@Override
	public String getXmlTagName() {
		return IMAGE__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(HREF__XML_ATTR, getHref());
		out.writeAttribute(IMG_WIDTH__XML_ATTR, Double.toString(getImgWidth()));
		out.writeAttribute(IMG_HEIGHT__XML_ATTR, Double.toString(getImgHeight()));
		out.writeAttribute(ALIGN__XML_ATTR, getAlign().protocolName());
		out.writeAttribute(SCALE__XML_ATTR, getScale().protocolName());
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Image} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Image_Impl readImage_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Image_Impl result = new Image_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case HREF__XML_ATTR: {
				setHref(value);
				break;
			}
			case IMG_WIDTH__XML_ATTR: {
				setImgWidth(Double.parseDouble(value));
				break;
			}
			case IMG_HEIGHT__XML_ATTR: {
				setImgHeight(Double.parseDouble(value));
				break;
			}
			case ALIGN__XML_ATTR: {
				setAlign(com.top_logic.graphic.flow.data.ImageAlign.valueOfProtocol(value));
				break;
			}
			case SCALE__XML_ATTR: {
				setScale(com.top_logic.graphic.flow.data.ImageScale.valueOfProtocol(value));
				break;
			}
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case HREF__XML_ATTR: {
				setHref(in.getElementText());
				break;
			}
			case IMG_WIDTH__XML_ATTR: {
				setImgWidth(Double.parseDouble(in.getElementText()));
				break;
			}
			case IMG_HEIGHT__XML_ATTR: {
				setImgHeight(Double.parseDouble(in.getElementText()));
				break;
			}
			case ALIGN__XML_ATTR: {
				setAlign(com.top_logic.graphic.flow.data.ImageAlign.valueOfProtocol(in.getElementText()));
				break;
			}
			case SCALE__XML_ATTR: {
				setScale(com.top_logic.graphic.flow.data.ImageScale.valueOfProtocol(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
