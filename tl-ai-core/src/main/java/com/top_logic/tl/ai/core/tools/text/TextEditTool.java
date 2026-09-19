/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.text;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.security.ToolSecurityException;
import com.top_logic.tl.ai.core.text.TextEdits;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Reads one text-valued attribute of one object verbatim, and changes a named passage of it.
 *
 * <p>
 * <b>What it is for.</b> The only way to change a stored text before this was a whole-value write -
 * a TL-Script {@code set} with the complete new value. For a document body or a system prompt that
 * costs the whole value in output tokens per edit, and it puts every untouched paragraph back through
 * the model, where it can come back subtly different. {@link #editText} costs the changed snippet and
 * cannot alter anything outside it, because nothing outside it is ever re-emitted.
 * </p>
 *
 * <p>
 * <b>Read first, then anchor.</b> {@link #readText} hands out the value as it is stored, with line
 * numbers in a gutter so a passage can be named; {@link #editText} takes a passage out of it and puts
 * another one in its place. The pair only works because the read is verbatim: an anchor quoted back
 * from a rendering that had normalised anything would not be the text.
 * </p>
 *
 * <p>
 * <b>Text and String attributes only.</b> Most attributes of a model are not text - references,
 * numbers, dates, enumerations, booleans, binaries, multi-valued properties - and none of them is
 * find-and-replaceable. They are refused by name rather than coerced, see
 * {@link #editableText(TLObject, String)}.
 * </p>
 *
 * <p>
 * <b>Security.</b> The object is resolved through {@link TidResolver}, so an object the run's
 * principal may not read is refused exactly like one that does not exist; the attribute is checked
 * for Read before it is disclosed and for Write before it is changed. The change enrols in the turn's
 * own transaction and is committed by the agent loop, which is where the pre-commit guard sees it a
 * second time and where a constraint that vetoes the commit is reported back to the model.
 * </p>
 *
 * @see TextEdits
 */
public class TextEditTool {

	/**
	 * Configuration for {@link TextEditTool}.
	 */
	public interface Config extends PolymorphicConfiguration<TextEditTool> {
		// no additional properties
	}

	/**
	 * How much of a value {@link #readText} renders when no line range was asked for.
	 *
	 * <p>
	 * Below the agent loop's own {@code MAX_TOOL_RESULT_CHARS}, deliberately: a tool result truncated
	 * by the framework ends in the middle of a line, and a model that anchors on what it was shown
	 * would then quote half a sentence as if it were whole. This tool cuts on a line boundary and says
	 * where it stopped instead.
	 * </p>
	 */
	static final int MAX_READ_CHARS = 16000;

	/** How many lines of context {@link #editText} shows around the change it made. */
	private static final int CONTEXT_LINES = 3;

	/**
	 * How much of the changed region {@link #editText} shows.
	 *
	 * <p>
	 * Seven lines are usually nothing, but a value held on one very long line is seven lines of that
	 * line. The confirmation of an edit must not be what makes the answer too big to send.
	 * </p>
	 */
	private static final int MAX_REGION_CHARS = 4000;

	/** How every answer that did nothing begins, following the convention of the other text tools. */
	private static final String ERROR = "error: ";

	/**
	 * Creates a new {@link TextEditTool}.
	 */
	public TextEditTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link TextEditTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public TextEditTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * The current value of one text attribute, character for character as it is stored.
	 *
	 * @param objectId
	 *        The identifier of the object, in the {@code "TypeName:id"} form every tool result
	 *        prints.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @param fromLine
	 *        The first line to return, 1-based; <code>null</code> for the first.
	 * @param toLine
	 *        The last line to return, 1-based and inclusive; <code>null</code> for the last.
	 * @param context
	 *        Injected execution context, supplying the rights the read is made with.
	 * @return The value with a line-number gutter, marked as business data; or a sentence beginning
	 *         with {@code error: } saying why nothing was read.
	 */
	@ToolMethod(description = "Read one Text or String attribute of one object exactly as it is stored,"
		+ " with a line number in front of every line. Read before you edit: editText matches the"
		+ " anchor you pass against these characters. The 'NNN| ' gutter is NOT part of the value -"
		+ " strip it before you quote a passage back. Use fromLine/toLine to read a part of a long"
		+ " value.", mode = ToolMode.READ, group = ToolGroups.DATA_READ)
	public String readText(
			@P("Identifier of the object, as a tool answer printed it: 'TypeName:id'") String objectId,
			@P("Name of the Text or String attribute to read") String attribute,
			@P(value = "First line to return, 1-based (default: the first)",
				required = false) Integer fromLine,
			@P(value = "Last line to return, 1-based and inclusive (default: the last)",
				required = false) Integer toLine,
			ToolExecutionContext context) {
		// Resolved outside the try on purpose, as ViewDocumentTool does it: a call that cannot name
		// its principal is a programming error, and reporting it to the model as a failed read would
		// have the model retry something no retry can fix.
		ReadFilter readFilter = ReadFilter.of(context);
		try {
			return doReadText(objectId, attribute, fromLine, toLine, readFilter);
		} catch (ToolSecurityException ex) {
			return ERROR + ex.getMessage();
		} catch (IllegalArgumentException ex) {
			return ERROR + ex.getMessage();
		} catch (RuntimeException ex) {
			Logger.error("readText(" + objectId + ", " + attribute + ") failed: " + ex.getMessage(), ex,
				TextEditTool.class);
			return ERROR + "The value could not be read: " + ex.getMessage();
		}
	}

	private String doReadText(String objectId, String attribute, Integer fromLine, Integer toLine,
			ReadFilter readFilter) {
		TLObject target = TidResolver.resolve(objectId, readFilter.security());
		TLStructuredTypePart part = editableText(target, attribute);
		if (!readFilter.security().canRead(target, part)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.READ, typeName(target));
		}
		String value = stored(target, part);
		if (value.isEmpty()) {
			return attribute + " is empty. There is no passage to anchor on; write the first content"
				+ " with an empty 'oldText'.";
		}
		TextGutter.Gutter gutter = TextGutter.render(value, fromLine, toLine, MAX_READ_CHARS);
		if (gutter.text().isEmpty()) {
			return attribute + " has " + gutter.totalLines() + " lines; the range you asked for is"
				+ " outside them.";
		}
		// The framing is this application's own sentence and stays outside the envelope; the value is
		// text some other end user typed and goes inside it.
		return describe(attribute, gutter) + "\n"
			+ ReadFilter.marked(objectId + "#" + attribute, gutter.text());
	}

	/**
	 * What was rendered, said before the rendering so it is read before its content is.
	 */
	private static String describe(String attribute, TextGutter.Gutter gutter) {
		String range = gutter.firstLine() == 1 && gutter.lastLine() == gutter.totalLines()
			? "all " + gutter.totalLines() + (gutter.totalLines() == 1 ? " line" : " lines")
			: "lines " + gutter.firstLine() + " to " + gutter.lastLine() + " of "
				+ gutter.totalLines();
		String rest = gutter.truncated()
			? " Stopped there because the value is long; ask for the next lines with fromLine="
				+ (gutter.lastLine() + 1) + "."
			: "";
		return attribute + ", " + range + ", exactly as stored:" + rest;
	}

	/**
	 * Replaces one passage of one text attribute with another, leaving the rest untouched.
	 *
	 * @param objectId
	 *        The identifier of the object, in the {@code "TypeName:id"} form every tool result
	 *        prints.
	 * @param attribute
	 *        The name of the attribute to change.
	 * @param oldText
	 *        The passage to find, as {@link #readText} showed it.
	 * @param newText
	 *        What to put in its place. Empty deletes the passage.
	 * @param replaceAll
	 *        Whether every occurrence is meant rather than the one; <code>null</code> is
	 *        <code>false</code>.
	 * @param context
	 *        Injected execution context, supplying the rights the change is made with and the turn's
	 *        transaction it enrols in.
	 * @return What was changed and the region it now reads, or a sentence beginning with
	 *         {@code error: } saying why nothing was changed.
	 */
	@ToolMethod(description = "Change one passage of a Text or String attribute without re-emitting the"
		+ " rest of it. Read the value with readText first and copy 'oldText' out of it character for"
		+ " character, without the line-number gutter. 'oldText' must occur exactly once, or the call"
		+ " changes nothing and tells you how often it occurs and where: extend it with surrounding"
		+ " lines until it is unique, or pass replaceAll=true if you mean every occurrence. To add"
		+ " text, anchor on a passage you keep and put it back together with the new text in"
		+ " 'newText'. Never send the whole value.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE)
	public String editText(
			@P("Identifier of the object, as a tool answer printed it: 'TypeName:id'") String objectId,
			@P("Name of the Text or String attribute to change") String attribute,
			@P("The passage to replace, copied verbatim from readText without the gutter") String oldText,
			@P("What to put in its place; empty deletes the passage") String newText,
			@P(value = "Replace every occurrence instead of requiring exactly one (default: false)",
				required = false) Boolean replaceAll,
			ToolContext context) {
		// Outside the try, for the reason readText gives.
		ModelSecurity security = context.modelSecurity();
		try {
			return doEditText(objectId, attribute, oldText, newText, replaceAll, security, context);
		} catch (ToolSecurityException ex) {
			return ERROR + ex.getMessage();
		} catch (IllegalArgumentException ex) {
			return ERROR + ex.getMessage();
		} catch (RuntimeException ex) {
			Logger.error("editText(" + objectId + ", " + attribute + ") failed: " + ex.getMessage(), ex,
				TextEditTool.class);
			return ERROR + "The value could not be changed: " + ex.getMessage();
		}
	}

	private String doEditText(String objectId, String attribute, String oldText, String newText,
			Boolean replaceAll, ModelSecurity security, ToolContext context) {
		// An omitted argument and an empty one mean opposite things here - "you forgot" against
		// "delete the passage" - and a model that omits a required field would otherwise have its
		// passage silently deleted.
		if (oldText == null) {
			throw new IllegalArgumentException("'oldText' is missing. Pass the passage to replace,"
				+ " copied from readText; pass an empty string only to write the first content of a"
				+ " value that is empty.");
		}
		if (newText == null) {
			throw new IllegalArgumentException("'newText' is missing. Pass what should stand there"
				+ " instead; pass an empty string to delete the passage.");
		}
		TLObject target = TidResolver.resolve(objectId, security);
		TLStructuredTypePart part = editableText(target, attribute);

		// Both, and in this order: the object, then the attribute. Model access is attribute-granular,
		// so a principal may hold the object and lack this one property of it. Asked here rather than
		// only at commit time because a refusal the model reads now costs one tool call, while the
		// same refusal in the pre-commit guard rolls the whole turn back - and because a tool that
		// writes without asking is a tool relying on a backstop it cannot see.
		security.requireWrite(target);
		if (!security.canWrite(target, part)) {
			throw new ToolSecurityException(SimpleBoundCommandGroup.WRITE, typeName(target));
		}

		String current = stored(target, part);
		boolean all = replaceAll != null && replaceAll;
		TextEdits.Result edit = all
			? TextEdits.replaceAll(current, oldText, newText)
			: TextEdits.replace(current, oldText, newText);
		if (!edit.applied()) {
			return ERROR + edit.message();
		}
		if (edit.text().equals(current)) {
			return "No change: the value already reads exactly that way. Nothing was written.";
		}

		// The lock bridges with the edit lock of the interactive UI, so an object a user has open is
		// not written behind their back. The turn's transaction is begun by the agent loop before a
		// DATA_WRITE tool runs and committed after the turn, which is where a constraint that refuses
		// the new value is reported back.
		context.transaction().acquireLock("editValues", target);
		target.tUpdate(part, edit.text());

		// The region as it now reads, so the change can be verified without reading the value again.
		// Still business data - most of the excerpt is text this run did not write - so it carries the
		// same envelope the read does.
		String region =
			TextGutter.excerpt(edit.text(), edit.changeOffset(), CONTEXT_LINES, MAX_REGION_CHARS);
		String shown = region.isEmpty()
			? " The changed region is too long to show back; read it with readText if you need it."
			: " It reads, around the first change:\n"
				+ ReadFilter.marked(objectId + "#" + attribute, region);
		return edit.message() + " " + attribute + " now has " + TextGutter.lineCount(edit.text())
			+ " lines." + shown + "\nThe change is committed when this turn ends.";
	}

	/**
	 * The attribute to read or change, if it is one this tool can handle.
	 *
	 * <p>
	 * The refusals name what the attribute actually is. A model that is told "not a text attribute"
	 * and nothing else has no way to tell a typo from a reference from a multi-valued property, and
	 * retries all three.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         If the type has no such part, or the part is not a single-valued text property.
	 */
	static TLStructuredTypePart editableText(TLObject target, String attribute) {
		if (attribute == null || attribute.isBlank()) {
			throw new IllegalArgumentException("No attribute named. Pass the name of the Text or"
				+ " String attribute to work on.");
		}
		TLStructuredType type = target.tType();
		if (type == null) {
			throw new IllegalArgumentException("This object has no type, so it has no attributes.");
		}
		TLStructuredTypePart part = type.getPart(attribute);
		if (part == null) {
			throw new IllegalArgumentException("The type '" + type + "' has no attribute named '"
				+ attribute + "'. Use listTypeParts or getInstance to see which attributes it has.");
		}
		if (part.getModelKind() != ModelKind.PROPERTY) {
			throw new IllegalArgumentException("'" + attribute + "' is a reference to other objects,"
				+ " not a text. It cannot be edited as text; set it to the objects it should point to"
				+ " instead.");
		}
		if (part.isMultiple()) {
			throw new IllegalArgumentException("'" + attribute + "' holds a list of values, not one"
				+ " text. It cannot be edited as text; write the whole list instead.");
		}
		if (!isText(part)) {
			throw new IllegalArgumentException("'" + attribute + "' is of type '" + part.getType()
				+ "', not a text. This tool works on Text and String attributes only; write a value of"
				+ " the right type instead.");
		}
		return part;
	}

	/**
	 * Whether the part holds a plain string.
	 *
	 * <p>
	 * Two questions, because either alone lets something through. The kind admits
	 * {@code tl.core:String} and {@code tl.core:Text} together with an application's own datatypes
	 * built on them, which is the generality this tool is meant to have. The storage mapping then
	 * settles what a value of it actually <em>is</em> in Java: a datatype may declare the string kind
	 * and map to a structured value, and handing such a value to a string edit would store a
	 * {@link String} where the application expects its own type.
	 * </p>
	 */
	private static boolean isText(TLStructuredTypePart part) {
		if (!(part.getType() instanceof TLPrimitive primitive)) {
			return false;
		}
		if (primitive.getKind() != TLPrimitive.Kind.STRING) {
			return false;
		}
		Class<?> stored = primitive.getStorageMapping() == null
			? String.class
			: primitive.getStorageMapping().getApplicationType();
		return stored == null || stored.isAssignableFrom(String.class);
	}

	/**
	 * The stored value as a text; an unset attribute and one set to {@code ""} are the same text.
	 */
	private static String stored(TLObject target, TLStructuredTypePart part) {
		Object value = target.tValue(part);
		if (value == null) {
			return "";
		}
		if (!(value instanceof String text)) {
			// The declared type said string and the stored value is not one, so this application maps
			// that datatype to something of its own. Editing it as text would write a String where the
			// application expects its type, which no caller could see going wrong.
			throw new IllegalArgumentException("'" + part.getName() + "' holds a "
				+ value.getClass().getSimpleName() + " rather than plain text, although its type says"
				+ " otherwise. It cannot be edited as text.");
		}
		return text;
	}

	private static String typeName(TLObject target) {
		TLStructuredType type = target == null ? null : target.tType();
		return type == null ? null : String.valueOf(type);
	}
}
