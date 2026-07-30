/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.ui.AWTContext;

import de.haumacher.msgbuf.data.ReflectiveDataObject;
import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.Scope;
import de.haumacher.msgbuf.graph.ScopeMixin;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;
import de.haumacher.msgbuf.server.io.ReaderAdapter;
import de.haumacher.msgbuf.server.io.WriterAdapter;

/**
 * Removes business content from a {@link Diagram} captured in a running application, so that the
 * capture can be checked in as a layout test resource.
 *
 * <p>
 * A capture reproduces a layout observed in production (see {@link TestDiagramReplay}), but it also
 * carries the customer's labels and images. This tool replaces that content while keeping every
 * property the layout depends on, so the scrubbed diagram lays out exactly like the original:
 * </p>
 *
 * <ul>
 * <li>Text is replaced character by character — letters by letters, digits by digits, everything
 * else verbatim — so each label keeps its length and its rough glyph widths. Equal texts stay
 * equal, so repeated labels remain repeated.</li>
 * <li>Images are replaced by a placeholder graphic. An image is sized from its declared
 * {@code imgWidth}/{@code imgHeight}, never from its payload, so this is geometrically exact.</li>
 * <li>Tooltips are dropped to a fixed text. They are not laid out at all.</li>
 * </ul>
 *
 * <p>
 * The traversal is reflective, so it covers box types added later without change. Run it as:
 * </p>
 *
 * <pre>
 * mvn -B exec:java -pl com.top_logic.graphic.blocks.server -Dexec.classpathScope=test \
 *     -Dexec.mainClass=test.com.top_logic.graphic.flow.DiagramScrubber \
 *     -Dexec.args="&lt;captured&gt;.json &lt;scrubbed&gt;.json"
 * </pre>
 */
public class DiagramScrubber {

	/**
	 * Properties holding free text that may carry business content.
	 *
	 * <p>
	 * The width of a text is measured during layout, so a replacement must keep its length.
	 * </p>
	 */
	private static final Set<String> TEXT_PROPERTIES = new HashSet<>(Arrays.asList("value"));

	/**
	 * Properties holding text that is never laid out and can be dropped entirely.
	 */
	private static final Set<String> LABEL_PROPERTIES = new HashSet<>(Arrays.asList("text", "tooltip", "label"));

	/**
	 * Properties holding image data.
	 */
	private static final Set<String> IMAGE_PROPERTIES = new HashSet<>(Arrays.asList("href"));

	/**
	 * Replacement for {@link #LABEL_PROPERTIES}.
	 */
	private static final String LABEL_REPLACEMENT = "Tooltip";

	/**
	 * Replacement for {@link #IMAGE_PROPERTIES}: a framed box with a diagonal cross, so that the
	 * area an image occupies stays visible when the scrubbed diagram is inspected.
	 */
	private static final String IMAGE_REPLACEMENT = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(
		("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'>"
			+ "<rect x='1' y='1' width='98' height='98' fill='#eeeeee' stroke='#999999'/>"
			+ "<path d='M 1,1 L 99,99 M 99,1 L 1,99' stroke='#999999' fill='none'/>"
			+ "</svg>").getBytes(StandardCharsets.UTF_8));

	/**
	 * Seed mixed into the per-text replacement, so that the mapping is stable across runs and
	 * machines but not a plain substitution cipher.
	 */
	private static final long SEED = 0x5CB0BBEDL;

	/**
	 * How close a scrubbed text must come to the width of the text it replaces, in CSS pixels.
	 */
	private static final double WIDTH_TOLERANCE_PX = 0.5;

	/**
	 * How many characters to trade at most while matching the width of a single text.
	 */
	private static final int WIDTH_MATCH_ATTEMPTS = 500;

	private final Map<Object, Object> _visited = new IdentityHashMap<>();

	/**
	 * Measures the texts being replaced, so that a replacement can be held to the width of the
	 * original.
	 *
	 * <p>
	 * Mirrors the client, which lays out with the canvas default font.
	 * </p>
	 */
	private final RenderContext _context =
		new AWTContext((float) (RenderContext.DEFAULT_FONT_SIZE_PX * AWTContext.PT_PER_PX));

	/**
	 * Scrubs the diagram read from {@code in} and writes it to {@code out}.
	 */
	public static void scrub(Reader in, Writer out) throws IOException {
		Diagram diagram = Diagram.readDiagram(new DefaultScope(1, 0), new JsonReader(new ReaderAdapter(in)));

		new DiagramScrubber().descend(diagram);

		JsonWriter json = new JsonWriter(new WriterAdapter(out));
		diagram.writeTo(new WriteScope(), json);
		json.flush();
	}

	/**
	 * {@link Scope} that assigns node IDs from scratch.
	 *
	 * <p>
	 * Reading assigns each node the ID it carried in the capture, and that ID cannot be reassigned.
	 * A scope keyed on the node identity rather than on that ID therefore lets the very same graph
	 * be written out again — with {@link DefaultScope} every node would be written as a reference
	 * to itself.
	 * </p>
	 */
	private static final class WriteScope implements ScopeMixin {

		private final Map<Object, SharedGraphNode> _index = new HashMap<>();

		private final Map<SharedGraphNode, Integer> _ids = new IdentityHashMap<>();

		private int _nextId = 1;

		@Override
		public int id(SharedGraphNode node) {
			return _ids.getOrDefault(node, 0);
		}

		@Override
		public void initId(SharedGraphNode node, int id) {
			_ids.put(node, id);
		}

		@Override
		public int newId() {
			return _nextId++;
		}

		@Override
		public Map<Object, SharedGraphNode> index() {
			return _index;
		}
	}

	/**
	 * Replaces the business content reachable from the given node.
	 */
	private void descend(ReflectiveDataObject node) {
		if (_visited.put(node, node) != null) {
			// Already seen — the graph has container back-references.
			return;
		}

		Set<String> transientProperties = node.transientProperties();
		for (String property : node.properties()) {
			if (transientProperties.contains(property)) {
				// Not part of the transferred data.
				continue;
			}
			Object value = node.get(property);
			if (value instanceof String) {
				String replacement = replacement(node, property, (String) value);
				if (replacement != null) {
					node.set(property, replacement);
				}
			} else {
				descendValue(value);
			}
		}
	}

	private void descendValue(Object value) {
		if (value instanceof ReflectiveDataObject) {
			descend((ReflectiveDataObject) value);
		} else if (value instanceof List<?>) {
			for (Object entry : (List<?>) value) {
				descendValue(entry);
			}
		} else if (value instanceof Map<?, ?>) {
			for (Object entry : ((Map<?, ?>) value).values()) {
				descendValue(entry);
			}
		}
	}

	/**
	 * The replacement for the given property value, or {@code null} to keep it unchanged.
	 */
	private String replacement(ReflectiveDataObject node, String property, String value) {
		if (IMAGE_PROPERTIES.contains(property)) {
			return IMAGE_REPLACEMENT;
		}
		if (LABEL_PROPERTIES.contains(property)) {
			return LABEL_REPLACEMENT;
		}
		if (TEXT_PROPERTIES.contains(property)) {
			return scrubText(node, value);
		}
		return null;
	}

	/**
	 * Replaces the letters and digits of the given text, keeping its length.
	 *
	 * <p>
	 * Characters that are neither letters nor digits are kept, since they are punctuation or
	 * layout-relevant symbols rather than content. Characters outside the ASCII range are kept as
	 * well: they carry no content on their own, and replacing them could change the text width
	 * (an icon glyph from a symbol font is a single character of arbitrary width).
	 * </p>
	 */
	private String scrubText(ReflectiveDataObject node, String value) {
		Random rnd = new Random(SEED ^ value.hashCode());
		char[] result = new char[value.length()];
		for (int n = 0; n < result.length; n++) {
			result[n] = replaceChar(rnd, value.charAt(n));
		}

		String family = string(node, "fontFamily");
		double size = size(node);
		String weight = string(node, "fontWeight");
		double target = _context.measure(value, family, size, weight).getWidth();

		// The replacement characters do not have the width of the ones they replace, so the text
		// would no longer occupy the space the captured layout gave it. Trade individual characters
		// until the whole text is back at its original width.
		double error = error(result, family, size, weight, target);
		for (int attempt = 0; attempt < WIDTH_MATCH_ATTEMPTS && error > WIDTH_TOLERANCE_PX; attempt++) {
			int pos = rnd.nextInt(result.length);
			char previous = result[pos];
			result[pos] = replaceChar(rnd, value.charAt(pos));
			double candidate = error(result, family, size, weight, target);
			if (candidate < error) {
				error = candidate;
			} else {
				result[pos] = previous;
			}
		}
		return new String(result);
	}

	private double error(char[] text, String family, double size, String weight, double target) {
		return Math.abs(_context.measure(new String(text), family, size, weight).getWidth() - target);
	}

	/**
	 * A replacement for the given character, from the same character class.
	 *
	 * <p>
	 * Anything that is neither an ASCII letter nor a digit is kept: punctuation and symbols carry
	 * no content, and a character outside the ASCII range may be an icon glyph whose width no
	 * replacement would reproduce.
	 * </p>
	 */
	private static char replaceChar(Random rnd, char c) {
		if (c >= 'a' && c <= 'z') {
			return (char) ('a' + rnd.nextInt(26));
		}
		if (c >= 'A' && c <= 'Z') {
			return (char) ('A' + rnd.nextInt(26));
		}
		if (c >= '0' && c <= '9') {
			return (char) ('0' + rnd.nextInt(10));
		}
		return c;
	}

	/**
	 * The font size of the given node, or {@code 0} to measure with the context default.
	 */
	private static double size(ReflectiveDataObject node) {
		if (!node.properties().contains("fontSize")) {
			return 0;
		}
		Object value = node.get("fontSize");
		return value instanceof Number ? ((Number) value).doubleValue() : 0;
	}

	/**
	 * The given property of the given node, or {@code null} if the node has no such property.
	 */
	private static String string(ReflectiveDataObject node, String property) {
		return node.properties().contains(property) ? (String) node.get(property) : null;
	}

	/**
	 * Scrubs the capture named by the first argument into the file named by the second.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Expected <captured>.json and <scrubbed>.json arguments.");
		}
		try (Reader in = new InputStreamReader(Files.newInputStream(Path.of(args[0])), StandardCharsets.UTF_8);
				Writer out = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8)) {
			scrub(in, out);
		}
	}
}
