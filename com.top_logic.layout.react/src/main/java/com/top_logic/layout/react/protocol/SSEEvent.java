package com.top_logic.layout.react.protocol;

/**
 * Base type for all SSE events sent from server to client.
 */
public interface SSEEvent extends de.haumacher.msgbuf.data.DataObject, de.haumacher.msgbuf.observer.Observable {

	/** Type codes for the {@link com.top_logic.layout.react.protocol.SSEEvent} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.layout.react.protocol.StateEvent}. */
		STATE_EVENT,

		/** Type literal for {@link com.top_logic.layout.react.protocol.PatchEvent}. */
		PATCH_EVENT,

		/** Type literal for {@link com.top_logic.layout.react.protocol.ContentReplacement}. */
		CONTENT_REPLACEMENT,

		/** Type literal for {@link com.top_logic.layout.react.protocol.ElementReplacement}. */
		ELEMENT_REPLACEMENT,

		/** Type literal for {@link com.top_logic.layout.react.protocol.PropertyUpdate}. */
		PROPERTY_UPDATE,

		/** Type literal for {@link com.top_logic.layout.react.protocol.CssClassUpdate}. */
		CSS_CLASS_UPDATE,

		/** Type literal for {@link com.top_logic.layout.react.protocol.FragmentInsertion}. */
		FRAGMENT_INSERTION,

		/** Type literal for {@link com.top_logic.layout.react.protocol.RangeReplacement}. */
		RANGE_REPLACEMENT,

		/** Type literal for {@link com.top_logic.layout.react.protocol.JSSnipplet}. */
		JSSNIPPLET,

		/** Type literal for {@link com.top_logic.layout.react.protocol.I18NCacheInvalidation}. */
		I_18_NCACHE_INVALIDATION,

		/** Type literal for {@link com.top_logic.layout.react.protocol.FunctionCall}. */
		FUNCTION_CALL,
		;

	}

	/** Visitor interface for the {@link com.top_logic.layout.react.protocol.SSEEvent} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.layout.react.protocol.StateEvent}.*/
		R visit(com.top_logic.layout.react.protocol.StateEvent self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.PatchEvent}.*/
		R visit(com.top_logic.layout.react.protocol.PatchEvent self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.ContentReplacement}.*/
		R visit(com.top_logic.layout.react.protocol.ContentReplacement self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.ElementReplacement}.*/
		R visit(com.top_logic.layout.react.protocol.ElementReplacement self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.PropertyUpdate}.*/
		R visit(com.top_logic.layout.react.protocol.PropertyUpdate self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.CssClassUpdate}.*/
		R visit(com.top_logic.layout.react.protocol.CssClassUpdate self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.FragmentInsertion}.*/
		R visit(com.top_logic.layout.react.protocol.FragmentInsertion self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.RangeReplacement}.*/
		R visit(com.top_logic.layout.react.protocol.RangeReplacement self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.JSSnipplet}.*/
		R visit(com.top_logic.layout.react.protocol.JSSnipplet self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.I18NCacheInvalidation}.*/
		R visit(com.top_logic.layout.react.protocol.I18NCacheInvalidation self, A arg) throws E;

		/** Visit case for {@link com.top_logic.layout.react.protocol.FunctionCall}.*/
		R visit(com.top_logic.layout.react.protocol.FunctionCall self, A arg) throws E;

	}

	/** The type code of this instance. */
	TypeKind kind();

	@Override
	public com.top_logic.layout.react.protocol.SSEEvent registerListener(de.haumacher.msgbuf.observer.Listener l);

	@Override
	public com.top_logic.layout.react.protocol.SSEEvent unregisterListener(de.haumacher.msgbuf.observer.Listener l);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.SSEEvent readSSEEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.SSEEvent result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case StateEvent.STATE_EVENT__TYPE: result = com.top_logic.layout.react.protocol.StateEvent.readStateEvent(in); break;
			case PatchEvent.PATCH_EVENT__TYPE: result = com.top_logic.layout.react.protocol.PatchEvent.readPatchEvent(in); break;
			case ContentReplacement.CONTENT_REPLACEMENT__TYPE: result = com.top_logic.layout.react.protocol.ContentReplacement.readContentReplacement(in); break;
			case ElementReplacement.ELEMENT_REPLACEMENT__TYPE: result = com.top_logic.layout.react.protocol.ElementReplacement.readElementReplacement(in); break;
			case PropertyUpdate.PROPERTY_UPDATE__TYPE: result = com.top_logic.layout.react.protocol.PropertyUpdate.readPropertyUpdate(in); break;
			case CssClassUpdate.CSS_CLASS_UPDATE__TYPE: result = com.top_logic.layout.react.protocol.CssClassUpdate.readCssClassUpdate(in); break;
			case FragmentInsertion.FRAGMENT_INSERTION__TYPE: result = com.top_logic.layout.react.protocol.FragmentInsertion.readFragmentInsertion(in); break;
			case RangeReplacement.RANGE_REPLACEMENT__TYPE: result = com.top_logic.layout.react.protocol.RangeReplacement.readRangeReplacement(in); break;
			case JSSnipplet.JSSNIPPLET__TYPE: result = com.top_logic.layout.react.protocol.JSSnipplet.readJSSnipplet(in); break;
			case I18NCacheInvalidation.I_18_NCACHE_INVALIDATION__TYPE: result = com.top_logic.layout.react.protocol.I18NCacheInvalidation.readI18NCacheInvalidation(in); break;
			case FunctionCall.FUNCTION_CALL__TYPE: result = com.top_logic.layout.react.protocol.FunctionCall.readFunctionCall(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
