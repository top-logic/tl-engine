/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests that the displays following their input channels - {@link RowSourceObserver} and
 * {@link ObservableTreeModel} - also follow the persistent objects held by those channels: an edit
 * of such an object, while the channel keeps pointing to it, re-evaluates the display without any
 * observed type.
 *
 * <p>
 * The elements are computed from the first attribute of the object on the input channel, so they
 * change only where that object is followed. The changes are committed to the knowledge base and
 * delivered by a {@link GlobalModelEventForwarder}, as an application session sees them.
 * </p>
 */
public class TestChannelInputObjectObservation extends AbstractDBKnowledgeBaseTest {

	/** The root object of a tree that does not swap its root. */
	private static final String ROOT = "root";

	/** The root object of a tree whose input object names a swap. */
	private static final String SWAPPED_ROOT = "swapped";

	/** The prefix of a first attribute value making the tree swap its root. */
	private static final String SWAP = "swap";

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The channel holding the object the elements are computed from. */
	private DefaultViewChannel _input;

	/** The elements delivered to the sink, in the order they were delivered. */
	private final List<List<String>> _delivered = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = CommittedModelChanges.modelScope();
		_input = new DefaultViewChannel("input");
		_delivered.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		_input = null;
		_scope = null;
		super.tearDown();
	}

	/**
	 * Tests that an edit of the object on an input channel re-evaluates the elements, although the
	 * channel keeps its value and no type is observed.
	 */
	public void testInputObjectChangeReEvaluates() throws Exception {
		BObj in = create("a,b");
		_input.set(in);
		RowSourceObserver<String> observer = observer(List.of("a", "b"));
		observer.attach(_scope);

		change(in, "a,b,c");

		assertEquals(List.of(List.of("a", "b", "c")), _delivered);
	}

	/**
	 * Tests that the observation follows the channel value: an edit of the object the channel no
	 * longer holds re-evaluates nothing, an edit of the object it holds now does.
	 */
	public void testObservationFollowsTheChannelValue() throws Exception {
		BObj b1 = create("a");
		BObj b2 = create("b");
		_input.set(b1);
		RowSourceObserver<String> observer = observer(List.of("a"));
		observer.attach(_scope);

		_input.set(b2);
		_delivered.clear();

		change(b1, "a,x");
		assertEquals("The object dropped from the channel is not followed.", List.of(), _delivered);

		change(b2, "b,y");
		assertEquals("The object now on the channel is followed.", List.of(List.of("b", "y")), _delivered);
	}

	/**
	 * Tests that a detached observer does not follow the object on its input channel.
	 */
	public void testDetachedObserverIgnoresInputObject() throws Exception {
		BObj in = create("a");
		_input.set(in);
		RowSourceObserver<String> observer = observer(List.of("a"));
		observer.attach(_scope);
		observer.detach();

		change(in, "a,b");

		assertEquals(List.of(), _delivered);
	}

	/**
	 * Tests that an input channel holding no persistent object - a string, a transient object - is
	 * followed by its value only: the observation passes such values by.
	 */
	public void testNonPersistentInputValuesAreIgnored() throws Exception {
		BObj unrelated = create("u");
		_input.set(List.of("text", transientObject()));
		List<String> elements = List.of("a");
		RowSourceObserver<String> observer =
			new RowSourceObserver<>(elements, args -> elements, Set.of(), List.of(_input), _delivered::add);
		observer.attach(_scope);

		change(unrelated, "u2");
		assertEquals("No object on the channel is followed.", List.of(), _delivered);

		_input.set("other");
		assertEquals("A new value is followed as before.", List.of(elements), _delivered);
	}

	/**
	 * Tests that an edit of the object on a row-independent input only runs the inputs-changed
	 * handler, as a change of that channel's value does.
	 */
	public void testRowIndependentInputObjectRunsTheHandler() throws Exception {
		BObj in = create("a");
		BObj selected = create("s");
		_input.set(in);
		DefaultViewChannel selection = new DefaultViewChannel("selection");
		selection.set(selected);
		int[] handled = { 0 };
		RowSourceObserver<String> observer = new RowSourceObserver<>(List.of("a"),
			args -> elements(args[0]), Set.of(), List.of(_input, selection), _delivered::add)
				.withRowIndependentInputs(List.of(selection))
				.onInputsChanged(List.of(), () -> handled[0]++);
		observer.attach(_scope);

		change(selected, "s2");

		assertEquals("Nothing is delivered.", List.of(), _delivered);
		assertEquals("The handler runs once.", 1, handled[0]);
	}

	/**
	 * Tests that an edit of the object on a cell input delivers the elements at hand again, as a
	 * change of that channel's value does.
	 */
	public void testCellInputObjectDeliversTheElementsAtHand() throws Exception {
		BObj in = create("a");
		BObj cellObject = create("c");
		_input.set(in);
		DefaultViewChannel cellInput = new DefaultViewChannel("cell");
		cellInput.set(cellObject);
		RowSourceObserver<String> observer = observer(List.of("a")).withCellInputs(List.of(cellInput));
		observer.attach(_scope);

		change(cellObject, "c2");

		assertEquals(List.of(List.of("a")), _delivered);
	}

	/**
	 * Tests that an edit of the object on the input of a tree whose root object stays reconciles the
	 * tree in place: the child list is computed again, and the tree is not replaced.
	 */
	public void testTreeInputObjectChangeReconcilesInPlace() throws Exception {
		BObj in = create("a,b");
		_input.set(in);
		TreeRecorder recorder = new TreeRecorder();
		ObservableTreeModel tree = tree(recorder);
		tree.attach(_scope);

		change(in, "a,b,c");

		assertEquals("The tree is not replaced.", 0, recorder._replaced);
		assertTrue("The display is told that its nodes changed.", recorder._nodesChanged > 0);
		assertEquals(List.of("a", "b", "c"), childObjects(tree));
	}

	/**
	 * Tests that an edit of the object on the input of a tree that makes it yield another root object
	 * builds the tree anew.
	 */
	public void testTreeInputObjectChangeSwapsRoot() throws Exception {
		BObj in = create("a,b");
		_input.set(in);
		TreeRecorder recorder = new TreeRecorder();
		ObservableTreeModel tree = tree(recorder);
		tree.attach(_scope);

		change(in, SWAP + ",x");

		assertEquals("The tree is built anew.", 1, recorder._replaced);
		assertEquals(SWAPPED_ROOT, tree.getTreeModel().getRoot().getBusinessObject());
		assertEquals(List.of(SWAP, "x"), childObjects(tree));
	}

	/**
	 * Tests that a detached tree does not follow the object on its input channel.
	 */
	public void testDetachedTreeIgnoresInputObject() throws Exception {
		BObj in = create("a");
		_input.set(in);
		TreeRecorder recorder = new TreeRecorder();
		ObservableTreeModel tree = tree(recorder);
		tree.attach(_scope);
		tree.detach();

		change(in, "a,b");

		assertEquals(0, recorder._replaced);
		assertEquals(0, recorder._nodesChanged);
	}

	/**
	 * An observer computing the elements from the object on {@link #_input}, delivering into
	 * {@link #_delivered}.
	 */
	private RowSourceObserver<String> observer(List<String> initialElements) {
		return new RowSourceObserver<>(initialElements, args -> elements(args[0]), Set.of(), List.of(_input),
			_delivered::add);
	}

	/**
	 * A tree over {@link #_input}: its root is {@link #ROOT}, or {@link #SWAPPED_ROOT} where the
	 * first attribute of the input object starts with {@link #SWAP}, and the children of the root
	 * are the elements of that attribute.
	 */
	private ObservableTreeModel tree(ObservableTreeModel.Display display) {
		TreeBuilder<DefaultTreeUINode> builder = new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> children = new ArrayList<>();
				if (node.getParent() == null) {
					for (String element : elements(_input.get())) {
						children.add(createNode(node.getModel(), node, element));
					}
				}
				return children;
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		};
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, root(_input.get()));
		treeModel.getRoot().getChildren();
		return new ObservableTreeModel(display, treeModel, args -> root(args[0]), builder, null, Set.of(),
			List.of(_input));
	}

	/**
	 * The root object of the tree for the given input value.
	 */
	private static Object root(Object input) {
		return elements(input).stream().anyMatch(SWAP::equals) ? SWAPPED_ROOT : ROOT;
	}

	/**
	 * The objects of the children of the root of the given tree.
	 */
	private static List<Object> childObjects(ObservableTreeModel tree) {
		List<Object> result = new ArrayList<>();
		for (DefaultTreeUINode child : tree.getTreeModel().getRoot().getChildren()) {
			result.add(child.getBusinessObject());
		}
		return result;
	}

	/**
	 * The elements the given input value names: the comma-separated parts of the first attribute
	 * of a {@link BObj}, none for any other value.
	 */
	private static List<String> elements(Object input) {
		if (input instanceof BObj object) {
			return Arrays.asList(object.getA1().split(","));
		}
		return List.of();
	}

	/**
	 * Creates a committed {@link BObj} and delivers the pending events.
	 */
	private BObj create(String a1) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(a1);
			tx.commit();
		} finally {
			tx.rollback();
		}
		_scope.synthesizeModelEvents();
		return result;
	}

	/**
	 * Commits a new first attribute for the given object and delivers the resulting change.
	 */
	private void change(BObj object, String a1) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(a1);
			tx.commit();
		} finally {
			tx.rollback();
		}
		_scope.synthesizeModelEvents();
	}

	/**
	 * An object that lives only in the display holding it.
	 */
	private static TLObject transientObject() {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.channelInputObjects");
		TLClass type = TLModelUtil.addClass(module, "Input");
		return TransientObjectFactory.INSTANCE.createObject(type, null);
	}

	/**
	 * {@link ObservableTreeModel.Display} recording what it is told.
	 */
	private static class TreeRecorder implements ObservableTreeModel.Display {

		/** How often the display was told that its nodes changed. */
		int _nodesChanged;

		/** How often the tree was replaced. */
		int _replaced;

		@Override
		public void invalidateNode(DefaultTreeUINode node) {
			// Followed by nodesChanged(), which is recorded.
		}

		@Override
		public void nodesChanged() {
			_nodesChanged++;
		}

		@Override
		public void treeReplaced(DefaultTreeUINodeModel treeModel, List<DefaultTreeUINode> opened,
				boolean rootChanged) {
			_replaced++;
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestChannelInputObjectObservation.class);
	}

}
