# TreeElement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a declarative `<tree>` UIElement to the view system that wraps `ReactTreeControl`, using ViewChannels for input data and TL-Script expressions for tree structure derivation.

**Architecture:** `TreeElement` is a `UIElement` with `@TagName("tree")`. Its Config declares input channels (`List<ChannelRef>`), TL-Script expressions (`Expr`) for root derivation, child computation, leaf detection, node support, reverse model lookup, parent navigation, and update node resolution, a `ReactControlProvider` for node content, and an optional selection output channel. Expression compilation happens once in the constructor; per-session wiring (channel resolution, tree model creation, listener registration) happens in `createControl(ViewContext)`.

**Tech Stack:** Java 17, TypedConfiguration, QueryExecutor (TL-Script), ReactTreeControl, DefaultTreeUINodeModel, ViewChannel

**Design doc:** `docs/plans/2026-03-05-tree-element-design.md`

---

### Task 1: Add `setTreeModel()` to ReactTreeControl

`ReactTreeControl` currently has no way to replace its `TreeUIModel` after construction. When input channels change and the root object changes, we need to rebuild the tree. Adding a `setTreeModel()` method (plus `setSelectionModel()`) mirrors what we did for `ReactTableControl.setSelectionListener()`.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java`

**Step 1: Read the file to understand the field layout**

Read `ReactTreeControl.java` and find the `_treeModel` and `_selectionModel` fields (around lines 120-125).

**Step 2: Add `setTreeModel()` method**

Add near the existing setter methods (e.g. near `setSelectionMode()`, around line 180):

```java
/**
 * Replaces the tree model and rebuilds the control state.
 *
 * @param treeModel
 *        The new tree model.
 */
@SuppressWarnings("unchecked")
public void setTreeModel(TreeUIModel<?> treeModel) {
    _treeModel = (TreeUIModel<Object>) treeModel;
    _nodeControlCache.clear();
    buildFullState();
}
```

This requires `buildFullState()` to become package-private (or stay private and be called via the new method). Since `buildFullState()` is already private and called from `setTreeModel()` within the same class, no visibility change is needed.

**Step 3: Add `setSelectionModel()` method**

```java
/**
 * Replaces the selection model and rebuilds the control state.
 *
 * @param selectionModel
 *        The new selection model.
 */
@SuppressWarnings("unchecked")
public void setSelectionModel(SelectionModel<?> selectionModel) {
    _selectionModel = selectionModel;
    buildFullState();
}
```

For this to work, `_treeModel` and `_selectionModel` must NOT be `final`. Check if they are declared `final` and remove the `final` modifier if needed.

**Step 4: Verify build compiles**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react && mvn compile -q`
Expected: BUILD SUCCESS

**Step 5: Commit**

```
Ticket #29108: Add setTreeModel() and setSelectionModel() to ReactTreeControl.
```

---

### Task 2: Write TreeElement Config and Constructor (Test First)

**Files:**
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-tree.view.xml`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestTreeElement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TreeElement.java`

**Step 1: Write test XML**

Create `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-tree.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="rootInput" />
    <channel name="selectedNode" />
  </channels>

  <tree selection="selectedNode">
    <inputs>
      <input channel="rootInput" />
    </inputs>
    <root>input -> $input</root>
    <children>input -> parent -> list($parent, "child1", "child2")</children>
    <isLeaf>input -> node -> $node != $input</isLeaf>
  </tree>
</view>
```

**Step 2: Write the failing test**

Create `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestTreeElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.TreeElement;

/**
 * Tests parsing and instantiation of {@link TreeElement}.
 */
public class TestTreeElement extends TestCase {

    /**
     * Tests that a view XML with a {@code <tree>} element can be parsed into configuration.
     */
    public void testParseTreeConfig() throws Exception {
        DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

        Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
            "view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

        BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

        ConfigurationReader reader = new ConfigurationReader(context, descriptors);
        reader.setSource(source);
        ViewElement.Config config = (ViewElement.Config) reader.read();

        context.checkErrors();
        assertNotNull("Config should be parsed", config);

        // The content should be a TreeElement config.
        assertEquals("View should have one content element", 1, config.getContent().size());
        assertTrue("Content should be TreeElement config",
            config.getContent().get(0) instanceof TreeElement.Config);

        TreeElement.Config treeConfig = (TreeElement.Config) config.getContent().get(0);

        // Verify inputs.
        assertEquals("Should have one input", 1, treeConfig.getInputs().size());
        assertEquals("Input channel name", "rootInput", treeConfig.getInputs().get(0).getChannelName());

        // Verify root expression is present (non-null).
        assertNotNull("Root expression should be set", treeConfig.getRoot());

        // Verify children expression is present (non-null).
        assertNotNull("Children expression should be set", treeConfig.getChildren());

        // Verify isLeaf expression is present.
        assertNotNull("IsLeaf expression should be set", treeConfig.getIsLeaf());

        // Verify selection channel.
        assertNotNull("Selection should be set", treeConfig.getSelection());
        assertEquals("Selection channel name", "selectedNode", treeConfig.getSelection().getChannelName());

        // Verify optional expressions are null (not set in test XML).
        assertNull("SupportsNode should not be set", treeConfig.getSupportsNode());
        assertNull("ModelForNode should not be set", treeConfig.getModelForNode());
        assertNull("Parents should not be set", treeConfig.getParents());
        assertNull("NodesToUpdate should not be set", treeConfig.getNodesToUpdate());
    }

    /**
     * Tests that the parsed configuration can be instantiated into a UIElement tree.
     */
    public void testInstantiateTreeElement() throws Exception {
        DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

        Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
            "view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

        BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

        ConfigurationReader reader = new ConfigurationReader(context, descriptors);
        reader.setSource(source);
        ViewElement.Config config = (ViewElement.Config) reader.read();

        UIElement element = context.getInstance(config);
        context.checkErrors();
        assertNotNull("UIElement should be instantiated", element);
        assertTrue("Should be a ViewElement", element instanceof ViewElement);
    }

    /**
     * Test suite requiring the {@link TypeIndex} module.
     */
    public static Test suite() {
        return ServiceTestSetup.createSetup(TestTreeElement.class, TypeIndex.Module.INSTANCE);
    }
}
```

**Step 3: Run test to verify it fails**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestTreeElement -q`
Expected: FAIL (TreeElement class does not exist)

**Step 4: Write TreeElement implementation**

Create `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TreeElement.java`:

```java
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
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactControlProvider;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.ReactTextControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Declarative {@link UIElement} that wraps a {@link ReactTreeControl}.
 *
 * <p>
 * Replaces the legacy {@code TreeComponent} + {@code TreeModelBuilder} pattern. Input data is
 * provided via {@link ViewChannel}s, and the tree structure is derived using TL-Script expressions.
 * </p>
 */
public class TreeElement implements UIElement {

    /**
     * Configuration for {@link TreeElement}.
     */
    @TagName("tree")
    public interface Config extends UIElement.Config {

        @Override
        @ClassDefault(TreeElement.class)
        Class<? extends UIElement> getImplementationClass();

        /** Configuration name for {@link #getInputs()}. */
        String INPUTS = "inputs";

        /** Configuration name for {@link #getRoot()}. */
        String ROOT = "root";

        /** Configuration name for {@link #getChildren()}. */
        String CHILDREN = "children";

        /** Configuration name for {@link #getIsLeaf()}. */
        String IS_LEAF = "isLeaf";

        /** Configuration name for {@link #getSupportsNode()}. */
        String SUPPORTS_NODE = "supportsNode";

        /** Configuration name for {@link #getModelForNode()}. */
        String MODEL_FOR_NODE = "modelForNode";

        /** Configuration name for {@link #getParents()}. */
        String PARENTS = "parents";

        /** Configuration name for {@link #getNodesToUpdate()}. */
        String NODES_TO_UPDATE = "nodesToUpdate";

        /** Configuration name for {@link #getCanExpandAll()}. */
        String CAN_EXPAND_ALL = "canExpandAll";

        /** Configuration name for {@link #getSelection()}. */
        String SELECTION = "selection";

        /** Configuration name for {@link #getNodeContent()}. */
        String NODE_CONTENT = "nodeContent";

        /**
         * References to {@link ViewChannel}s whose current values become positional arguments to
         * the expression properties.
         */
        @Name(INPUTS)
        @ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
        List<ChannelRef> getInputs();

        /**
         * TL-Script function computing the root business object.
         *
         * <p>
         * Takes the input channel values as positional arguments and returns the root node.
         * </p>
         */
        @Name(ROOT)
        @Mandatory
        @NonNullable
        Expr getRoot();

        /**
         * TL-Script function computing the children of a given parent node.
         *
         * <p>
         * Takes the input channel values followed by the parent node as last argument. Returns a
         * {@link Collection} of child objects.
         * </p>
         */
        @Name(CHILDREN)
        @Mandatory
        @NonNullable
        Expr getChildren();

        /**
         * Optional TL-Script predicate testing whether a node is a leaf.
         *
         * <p>
         * Takes the input channel values followed by the node as last argument. If not configured,
         * a node is considered a leaf if {@link #getChildren()} returns an empty collection.
         * </p>
         */
        @Name(IS_LEAF)
        Expr getIsLeaf();

        /**
         * Optional TL-Script predicate testing whether an object can be part of this tree.
         *
         * <p>
         * Takes the input channel values followed by the candidate node as last argument.
         * </p>
         */
        @Name(SUPPORTS_NODE)
        Expr getSupportsNode();

        /**
         * Optional TL-Script function for reverse root lookup.
         *
         * <p>
         * Takes the input channel values followed by a node as last argument. Returns the root
         * business object containing the given node.
         * </p>
         */
        @Name(MODEL_FOR_NODE)
        Expr getModelForNode();

        /**
         * Optional TL-Script function returning parent nodes.
         *
         * <p>
         * Takes the input channel values followed by a node as last argument. Returns a
         * {@link Collection} of parent objects.
         * </p>
         */
        @Name(PARENTS)
        Expr getParents();

        /**
         * Optional TL-Script function returning nodes to update when an object changes.
         *
         * <p>
         * Takes the input channel values followed by the changed object as last argument.
         * </p>
         */
        @Name(NODES_TO_UPDATE)
        Expr getNodesToUpdate();

        /**
         * Whether it is safe to offer expand-all functionality.
         */
        @Name(CAN_EXPAND_ALL)
        @BooleanDefault(true)
        boolean getCanExpandAll();

        /**
         * Optional reference to a {@link ViewChannel} to write the selected node(s) to.
         */
        @Name(SELECTION)
        @Format(ChannelRefFormat.class)
        ChannelRef getSelection();

        /**
         * Optional custom renderer for tree node content.
         *
         * <p>
         * If not configured, nodes are rendered as text labels using
         * {@code MetaLabelProvider}.
         * </p>
         */
        @Name(NODE_CONTENT)
        PolymorphicConfiguration<ReactControlProvider> getNodeContent();
    }

    private final Config _config;

    private final QueryExecutor _rootExecutor;

    private final QueryExecutor _childrenExecutor;

    private final ReactControlProvider _nodeContentProvider;

    private final boolean _canExpandAll;

    /**
     * Creates a new {@link TreeElement} from configuration.
     *
     * <p>
     * Expressions are compiled once here and shared across all sessions. If services like
     * {@code PersistencyLayer} are not yet active, {@link QueryExecutor#compile(Expr)} returns a
     * {@code DeferredQueryExecutor} that lazily compiles on first execution.
     * </p>
     */
    @CalledByReflection
    public TreeElement(InstantiationContext context, Config config) {
        _config = config;
        _canExpandAll = config.getCanExpandAll();

        _rootExecutor = QueryExecutor.compile(config.getRoot());
        _childrenExecutor = QueryExecutor.compile(config.getChildren());

        ReactControlProvider configuredProvider = context.getInstance(config.getNodeContent());
        _nodeContentProvider = configuredProvider != null ? configuredProvider : defaultContentProvider();
    }

    @Override
    public ViewControl createControl(ViewContext context) {
        // 1. Resolve input channels.
        List<ChannelRef> inputRefs = _config.getInputs();
        List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
        for (ChannelRef ref : inputRefs) {
            inputChannels.add(context.resolveChannel(ref));
        }

        // 2. Execute root query.
        Object[] channelValues = readChannelValues(inputChannels);
        Object rootObject = _rootExecutor.execute(channelValues);

        // 3. Build expression-backed TreeBuilder.
        TreeBuilder<DefaultTreeUINode> treeBuilder = createTreeBuilder(inputChannels);

        // 4. Create DefaultTreeUINodeModel.
        DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(treeBuilder, rootObject);

        // 5. Create SelectionModel.
        DefaultSingleSelectionModel<Object> selectionModel =
            new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

        // 6. Create ReactTreeControl.
        ReactTreeControl treeControl = new ReactTreeControl(treeModel, selectionModel, _nodeContentProvider);

        // 7. Wire selection channel.
        ChannelRef selectionRef = _config.getSelection();
        if (selectionRef != null) {
            ViewChannel selectionChannel = context.resolveChannel(selectionRef);
            selectionModel.addSelectionListener(new SelectionListener<Object>() {
                @Override
                public void notifySelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
                    Set<? extends Object> newSelection = event.getNewSelection();
                    if (newSelection.size() == 1) {
                        Object selected = newSelection.iterator().next();
                        // Write business object, not the tree node wrapper.
                        if (selected instanceof DefaultTreeUINode) {
                            selectionChannel.set(((DefaultTreeUINode) selected).getBusinessObject());
                        } else {
                            selectionChannel.set(selected);
                        }
                    } else if (newSelection.isEmpty()) {
                        selectionChannel.set(null);
                    } else {
                        selectionChannel.set(newSelection);
                    }
                }
            });
        }

        // 8. Wire input channel listeners for tree refresh on change.
        ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
            Object[] newValues = readChannelValues(inputChannels);
            Object newRoot = _rootExecutor.execute(newValues);
            DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(
                createTreeBuilder(inputChannels), newRoot);
            treeControl.setTreeModel(newTreeModel);
        };
        for (ViewChannel channel : inputChannels) {
            channel.addListener(refreshListener);
        }

        return treeControl;
    }

    private TreeBuilder<DefaultTreeUINode> createTreeBuilder(List<ViewChannel> inputChannels) {
        return new TreeBuilder<DefaultTreeUINode>() {
            @Override
            public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
                    DefaultTreeUINode parent, Object userObject) {
                return new DefaultTreeUINode(model, parent, userObject);
            }

            @Override
            public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
                Object[] channelValues = readChannelValues(inputChannels);
                Object[] args = appendArg(channelValues, node.getBusinessObject());
                Object result = _childrenExecutor.execute(args);
                Collection<?> children = toCollection(result);

                List<DefaultTreeUINode> childNodes = new ArrayList<>(children.size());
                for (Object child : children) {
                    DefaultTreeUINode childNode = createNode(node.getModel(), node, child);
                    if (childNode != null) {
                        childNodes.add(childNode);
                    }
                }
                return childNodes;
            }

            @Override
            public boolean isFinite() {
                return _canExpandAll;
            }
        };
    }

    private static Object[] readChannelValues(List<ViewChannel> channels) {
        Object[] values = new Object[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            values[i] = channels.get(i).get();
        }
        return values;
    }

    private static Object[] appendArg(Object[] base, Object extra) {
        Object[] result = new Object[base.length + 1];
        System.arraycopy(base, 0, result, 0, base.length);
        result[base.length] = extra;
        return result;
    }

    private static Collection<?> toCollection(Object result) {
        if (result instanceof Collection<?>) {
            return (Collection<?>) result;
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(result);
    }

    private static ReactControlProvider defaultContentProvider() {
        return model -> new ReactTextControl(MetaLabelProvider.INSTANCE.getLabel(model));
    }
}
```

**Note:** The `defaultContentProvider()` uses `ReactTextControl`. If that class does not exist, check for an alternative like `ReactTextCellControl` or a simple text-rendering ReactControl. Search with: `grep -r "class ReactText" com.top_logic.layout.react/`

**Step 5: Run test to verify it passes**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestTreeElement -q`
Expected: PASS (2 tests)

**Step 6: Commit**

```
Ticket #29108: TreeElement with config parsing, expression compilation, and channel wiring.
```

---

### Task 3: Verify ReactTreeControl Constructor and TreeBuilder API

The `createControl` wiring uses several APIs. Verify they exist as expected.

**Step 1: Check ReactTreeControl constructor**

Verify `ReactTreeControl(TreeUIModel<?>, SelectionModel<?>, ReactControlProvider)` exists. Search in `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java`.

**Step 2: Check DefaultSingleSelectionModel constructor**

Verify `new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER)` compiles. The constructor signature is `DefaultSingleSelectionModel(SelectionModelOwner owner)` in `com.top_logic/src/main/java/com/top_logic/mig/html/DefaultSingleSelectionModel.java`.

**Step 3: Check ReactTextControl or equivalent**

Search for a simple text-rendering ReactControl. If `ReactTextControl` does not exist, use `ReactTextCellControl` from `com.top_logic.layout.react.control.table` or create a minimal text control.

If `ReactTextControl` doesn't exist and there is no simple text ReactControl, update the `defaultContentProvider()` in `TreeElement` to use whatever text control is available.

**Step 4: Compile and commit if changes were needed**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn compile -q`

```
Ticket #29108: Fix TreeElement API usage after verification.
```

---

### Task 4: Build and Verify Module Compiles End-to-End

**Step 1: Build the full module with tests**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn install -q`
Expected: BUILD SUCCESS (includes test execution)

**Step 2: Fix any compilation or test issues**

Common problems:
- Missing imports
- `ReactTextControl` not found -> switch to available text control
- `SelectionModelOwner` import missing
- `@BooleanDefault` import missing
- `AbstractMutableTLTreeModel` import needs wildcard for generic parameter

**Step 3: Run all module tests to verify nothing is broken**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.view && mvn test -DskipTests=false -Dtest="TestTreeElement,TestTableElement,TestViewElement,TestChannelDeclaration,TestDefaultViewChannel" -q`
Expected: All 16+ tests pass

**Step 4: Commit if fixes were needed**

```
Ticket #29108: Fix build issues in TreeElement.
```

---

### Task 5: Regenerate Messages and Commit Generated Files

The `messages_en.properties` and `messages_de.properties` files are generated during `mvn install` from JavaDoc and `@Label` annotations on Config interfaces.

**Step 1: Verify generated messages**

After the `mvn install` from Task 4, check if new message entries were generated:

Run: `git diff com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties`

New entries should appear for all `TreeElement.Config` properties (inputs, root, children, isLeaf, supportsNode, modelForNode, parents, nodesToUpdate, canExpandAll, selection, nodeContent).

**Step 2: Stage and commit generated files**

```bash
git add com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties
git add com.top_logic.layout.view/src/main/java/META-INF/messages_de.properties
```

```
Ticket #29108: Regenerate messages for TreeElement config properties.
```
