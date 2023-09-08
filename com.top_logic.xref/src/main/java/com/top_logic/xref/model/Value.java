package com.top_logic.xref.model;

/**
 * The value of an annotation property.
 */
public abstract class Value extends AbstractDataObject {

	/** Type codes for the {@link Value} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link StringValue}. */
		STRING_VALUE,

		/** Type literal for {@link FloatValue}. */
		FLOAT_VALUE,

		/** Type literal for {@link IntValue}. */
		INT_VALUE,

		/** Type literal for {@link ListValue}. */
		LIST_VALUE,

		/** Type literal for {@link AnnotationValue}. */
		ANNOTATION_VALUE,
		;

	}

	/** Visitor interface for the {@link Value} hierarchy.*/
	public interface Visitor<R,A> {

		/** Visit case for {@link StringValue}.*/
		R visit(StringValue self, A arg);

		/** Visit case for {@link FloatValue}.*/
		R visit(FloatValue self, A arg);

		/** Visit case for {@link IntValue}.*/
		R visit(IntValue self, A arg);

		/** Visit case for {@link ListValue}.*/
		R visit(ListValue self, A arg);

		/** Visit case for {@link AnnotationValue}.*/
		R visit(AnnotationValue self, A arg);

	}

	/**
	 * Creates a {@link Value} instance.
	 */
	protected Value() {
		super();
	}

	/** The type code of this instance. */
	public abstract TypeKind kind();

	/** Reads a new instance from the given reader. */
	public static Value readValue(com.top_logic.common.json.gstream.JsonReader in) throws java.io.IOException {
		Value result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case StringValue.STRING_VALUE__TYPE: result = StringValue.readStringValue(in); break;
			case FloatValue.FLOAT_VALUE__TYPE: result = FloatValue.readFloatValue(in); break;
			case IntValue.INT_VALUE__TYPE: result = IntValue.readIntValue(in); break;
			case ListValue.LIST_VALUE__TYPE: result = ListValue.readListValue(in); break;
			case AnnotationValue.ANNOTATION_VALUE__TYPE: result = AnnotationValue.readAnnotationValue(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	@Override
	public final void writeTo(com.top_logic.common.json.gstream.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

	/** The type identifier for this concrete subtype. */
	public abstract String jsonType();

	/** Accepts the given visitor. */
	public abstract <R,A> R visit(Visitor<R,A> v, A arg);


}
