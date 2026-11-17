/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.ObjectFunctions;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Compares ways to compute the composition descendants of an object: TL-Script recipes, the
 * function {@link ObjectFunctions#descendants(TLObject, boolean, boolean)} and a Java walk over the
 * same tree of persistent parts.
 *
 * <p>
 * The test asserts that all ways deliver the same objects. By default, the tree is small (some
 * hundred parts, every kind of reference of the test model is used). With the system property
 * {@value #LARGE_TREE_PROPERTY} set to <code>true</code>, the tree has about ten thousand parts and
 * the run times (median of several runs after a warm-up) are written to the log, so that the ways
 * can be compared on one machine. No run time is asserted.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestCompositionWalkTiming extends AbstractSearchExpressionTest {

	/**
	 * Name of the system property that switches to a tree of about ten thousand parts and to
	 * logging the run times.
	 */
	public static final String LARGE_TREE_PROPERTY = "test.compositionWalk.large";

	private static final String MODULE = "TestCompositionWalkTiming";

	private static final String NAME = "name";

	private static final String NOTE = "note";

	private static final String NOTES = "notes";

	private static final String RELATED = "related";

	private static final String HEAD = "head";

	private static final String MAIN = "main";

	private static final String GROUPS = "groups";

	private static final String ELEMENTS = "elements";

	private static final String LEAVES = "leaves";

	/** Number of composite multi-references of each level (the suffixes 1..n of their names). */
	private static final int REFERENCES_PER_LEVEL_ROOT = 4;

	private static final int REFERENCES_PER_LEVEL = 8;

	/** Number of groups in each composite reference of the root, in the large tree. */
	private static final int LARGE_GROUPS_PER_REFERENCE = 2;

	/** Number of elements in each composite multi-reference of a group, in the large tree. */
	private static final int LARGE_ELEMENTS_PER_REFERENCE = 3;

	/** Number of leaves in each composite multi-reference of an element, in the large tree. */
	private static final int LARGE_LEAVES_PER_REFERENCE = 5;

	/** Every n-th leaf gets a note. */
	private static final int LEAF_NOTE_INTERVAL = 8;

	private static final int RUNS = 15;

	/**
	 * The composite references are computed from the model for the type of every single part.
	 */
	static final String RECIPE_PER_PART = """
		root -> $root.recursion(o ->
		    $o.type()
		      .recursion(c -> $c.get(`tl.model:TLClass#generalizations`))
		      .flatMap(c -> $c.get(`tl.model:TLStructuredType#localParts`))
		      .filter(p -> $p.instanceOf(`tl.model:TLReference`)
		                && $p.get(`tl.model:TLReference#composite`))
		      .flatMap(r -> $o.get($r)),
		  1)
		""";

	/**
	 * The composite references of every class are computed once before the descent.
	 */
	static final String RECIPE_PRECOMPUTED = """
		root -> {
		  refs = all(`tl.model:TLClass`).indexBy(
		    keyFun: c -> $c,
		    mapFun: c -> $c
		      .recursion(g -> $g.get(`tl.model:TLClass#generalizations`))
		      .flatMap(g -> $g.get(`tl.model:TLStructuredType#localParts`))
		      .filter(p -> $p.instanceOf(`tl.model:TLReference`)
		                && $p.get(`tl.model:TLReference#composite`)));
		  $root.recursion(o -> $refs[$o.type()].flatMap(r -> $o.get($r)), 1);
		}
		""";

	/**
	 * The composite references are walked by the TL-Script function.
	 */
	static final String FUNCTION = "root -> $root.objectDescendants()";

	/**
	 * The composite references are walked by the TL-Script function, the result includes the root.
	 */
	static final String FUNCTION_INCLUDING_ROOT = "root -> $root.objectDescendants(includeRoot: true)";

	private final List<TLObject> _outside = new ArrayList<>();

	private int _partCount;

	private int _groupsPerReference;

	private int _elementsPerReference;

	private int _leavesPerReference;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		boolean large = isLargeTree();
		_groupsPerReference = large ? LARGE_GROUPS_PER_REFERENCE : 1;
		_elementsPerReference = large ? LARGE_ELEMENTS_PER_REFERENCE : 1;
		_leavesPerReference = large ? LARGE_LEAVES_PER_REFERENCE : 1;
	}

	private static boolean isLargeTree() {
		return Boolean.getBoolean(LARGE_TREE_PROPERTY);
	}

	public void testSameDescendants() throws Exception {
		TLObject root = createTree();
		try {
			Set<TLObject> expected = javaWalk(root);
			assertEquals(expected, javaWalk(root, true));
			assertEquals(_partCount, expected.size());
			assertFalse(expected.contains(root));
			for (TLObject outside : _outside) {
				assertFalse("Non-composite reference followed.", expected.contains(outside));
			}

			QueryExecutor perPart = QueryExecutor.compile(search(RECIPE_PER_PART));
			QueryExecutor precomputed = QueryExecutor.compile(search(RECIPE_PRECOMPUTED));
			QueryExecutor function = QueryExecutor.compile(search(FUNCTION));
			QueryExecutor functionIncludingRoot = QueryExecutor.compile(search(FUNCTION_INCLUDING_ROOT));

			assertEquals(expected, asSet(perPart.execute(root)));
			assertEquals(expected, asSet(precomputed.execute(root)));
			assertEquals(expected, asSet(function.execute(root)));

			Set<TLObject> expectedIncludingRoot = new HashSet<>(expected);
			expectedIncludingRoot.add(root);
			Collection<?> includingRoot = (Collection<?>) functionIncludingRoot.execute(root);
			assertEquals(expectedIncludingRoot, asSet(includingRoot));
			assertSame(root, includingRoot.iterator().next());
			assertContainerFirst(includingRoot);

			if (isLargeTree()) {
				long java = median(() -> javaWalk(root));
				long javaSecured = median(() -> javaWalk(root, true));
				long a = median(() -> perPart.execute(root));
				long b = median(() -> precomputed.execute(root));
				long d = median(() -> function.execute(root));

				Logger.info("Composition walk over " + expected.size() + " parts (median of " + RUNS
					+ " runs): (a) recipe, references per part: " + a
					+ " ms, (b) recipe, references precomputed: " + b + " ms, (c) Java walk: " + java
					+ " ms, (c') Java walk with access checks: " + javaSecured
					+ " ms, (d) function objectDescendants(): " + d + " ms.", TestCompositionWalkTiming.class);
			}
		} finally {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				root.tDelete();
				for (TLObject outside : _outside) {
					outside.tDelete();
				}
				tx.commit();
			}
		}
	}

	/**
	 * Asserts that each object of the given sequence follows its container.
	 */
	private static void assertContainerFirst(Collection<?> sequence) {
		Set<TLObject> seen = new HashSet<>();
		for (Object element : sequence) {
			TLObject object = (TLObject) element;
			TLObject container = object.tContainer();
			if (container != null) {
				assertTrue("Part before its container: " + object, seen.contains(container));
			}
			seen.add(object);
		}
	}

	/**
	 * The run time of the given computation in milliseconds: the median of {@link #RUNS} runs after a
	 * warm-up run.
	 */
	private static long median(Runnable computation) {
		computation.run();
		long[] times = new long[RUNS];
		for (int n = 0; n < RUNS; n++) {
			long start = System.nanoTime();
			computation.run();
			times[n] = (System.nanoTime() - start) / 1_000_000;
		}
		Arrays.sort(times);
		return times[RUNS / 2];
	}

	/**
	 * Depth-first walk over the composite references, with the composite references cached per
	 * type.
	 */
	static Set<TLObject> javaWalk(TLObject root) {
		return javaWalk(root, false);
	}

	/**
	 * Depth-first walk over the composite references, with the composite references cached per
	 * type, optionally with access checks.
	 *
	 * @param withSecurity
	 *        Whether the walk checks access like
	 *        {@link ObjectFunctions#descendants(TLObject, boolean, boolean)} evaluated with security:
	 *        a composite reference is only followed where the current user may read it, only
	 *        discoverable objects are delivered, and the result is filtered once more like the
	 *        return value of a TL-Script function.
	 */
	static Set<TLObject> javaWalk(TLObject root, boolean withSecurity) {
		Map<TLClass, List<TLReference>> refsByType = new HashMap<>();
		Function<TLClass, List<TLReference>> compositeRefs = type -> {
			List<TLReference> result = new ArrayList<>();
			for (TLStructuredTypePart part : type.getAllParts()) {
				if (part instanceof TLReference ref && ref.isComposite()) {
					result.add(ref);
				}
			}
			return result;
		};

		Set<TLObject> result = new HashSet<>();
		Deque<TLObject> stack = new ArrayDeque<>();
		stack.push(root);
		while (!stack.isEmpty()) {
			TLObject current = stack.pop();
			for (TLReference ref : refsByType.computeIfAbsent((TLClass) current.tType(), compositeRefs)) {
				Object value;
				if (withSecurity) {
					if (!ModelAccessRights.getInstance().isReadAllowed(current, ref)) {
						continue;
					}
					value = SearchExpression.filterSecurity(current.tValue(ref));
				} else {
					value = current.tValue(ref);
				}
				if (value instanceof Collection<?> values) {
					for (Object part : values) {
						if (result.add((TLObject) part)) {
							stack.push((TLObject) part);
						}
					}
				} else if (value != null) {
					if (result.add((TLObject) value)) {
						stack.push((TLObject) value);
					}
				}
			}
		}
		if (withSecurity) {
			@SuppressWarnings("unchecked")
			Set<TLObject> filtered = (Set<TLObject>) SearchExpression.filterSecurity(result);
			return filtered;
		}
		return result;
	}

	private TLObject createTree() {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			for (int n = 0; n < 2; n++) {
				_outside.add(create("Note", "outside" + n));
			}
			_partCount = 0;

			TLObject root = create("Root", "root");
			for (int g = 1; g <= REFERENCES_PER_LEVEL_ROOT; g++) {
				List<TLObject> groups = new ArrayList<>();
				for (int n = 0; n < _groupsPerReference; n++) {
					groups.add(createGroup());
				}
				root.tUpdateByName(GROUPS + g, groups);
			}
			tx.commit();
			return root;
		}
	}

	private TLObject createGroup() {
		TLObject group = createPart("Group");
		group.tUpdateByName(NOTES, List.of(createPart("Note"), createPart("Note")));
		group.tUpdateByName(HEAD, createElement());
		for (int e = 1; e <= REFERENCES_PER_LEVEL; e++) {
			List<TLObject> elements = new ArrayList<>();
			for (int n = 0; n < _elementsPerReference; n++) {
				elements.add(createElement());
			}
			group.tUpdateByName(ELEMENTS + e, elements);
		}
		return group;
	}

	private TLObject createElement() {
		TLObject element = createPart("Element");
		element.tUpdateByName(NOTE, createPart("Note"));
		element.tUpdateByName(RELATED, _outside);
		element.tUpdateByName(MAIN, createLeaf());
		for (int l = 1; l <= REFERENCES_PER_LEVEL; l++) {
			List<TLObject> leaves = new ArrayList<>();
			for (int n = 0; n < _leavesPerReference; n++) {
				leaves.add(createLeaf());
			}
			element.tUpdateByName(LEAVES + l, leaves);
		}
		return element;
	}

	private TLObject createLeaf() {
		TLObject leaf = createPart("Leaf");
		if (_partCount % LEAF_NOTE_INTERVAL == 0) {
			leaf.tUpdateByName(NOTE, createPart("Note"));
		}
		return leaf;
	}

	private TLObject createPart(String typeName) {
		_partCount++;
		return create(typeName, typeName + _partCount);
	}

	private static TLObject create(String typeName, String name) {
		TLClass type = (TLClass) TLModelUtil.findType(model(), MODULE + ":" + typeName);
		TLObject result = ModelService.getInstance().getFactory().createObject(type);
		result.tUpdateByName(NAME, name);
		return result;
	}

	/**
	 * Adds the test model to the application model.
	 */
	static Test setupModel(Test test) {
		return new ThreadContextSetup(test) {
			@Override
			protected void doSetUp() throws Exception {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase()
					.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
					DynamicModelService.extendModel(new AssertProtocol(), ModelService.getApplicationModel(),
						ModelService.getInstance().getFactory(),
						ClassRelativeBinaryContent.withSuffix(TestCompositionWalkTiming.class, "model.xml"));
					tx.commit();
				}
			}

			@Override
			protected void doTearDown() throws Exception {
				// The model stays in the knowledge base of the test.
			}
		};
	}

	public static Test suite() {
		return KBSetup.getKBTest(TestCompositionWalkTiming.class,
			ServiceTestSetup.createStarterFactoryForModules(
				(testCase, suiteName) -> {
					TestSuite suite = new TestSuite(testCase);
					suite.setName(suiteName);
					return setupModel(suite);
				},
				getModules()));
	}

}
