---
description: Read before declaring or connecting channels in a .view.xml - <channels> with <channel> (optionally initialized from other channels with inputs, init, observed-types), <derived-channel> (inputs, expr, reverse, observed-types), how channel scripts see their inputs as named variables (`as`, no lambda header) and <source-channel> (a ChannelSource pushing values from a bus listener, queue or timer), how elements read and write channels, and how a <view-ref> or <open-dialog> shares channels with the view it embeds through <bind channel to>.
order: 15
---

# Channels

A channel is a named value of one view instance. Elements read the object they display from a channel and write what the user selects or enters into one, so channels are the wiring between the elements of a view: a table writes its selection, a form displays the object on that channel, a command takes it as its input. A channel holds one value; a listener of the channel is told about every new value that differs from the old one (`Objects.equals`).

## Declaring channels: `<channels>`

The channels of a view are declared in the `<channels>` section of its `<view>` element (`ViewElement.Config.getChannels()`). Every name is unique within the view. Three kinds exist:

- **`<channel name="…"/>`** (`ValueChannelConfig`) is a writable value. It starts empty and holds whatever an element or an action writes into it last. With an `init` function it is filled from other channels instead, see [below](#a-writable-channel-initialized-from-other-channels-init).
- **`<derived-channel name="…" inputs="…" expr="…"/>`** (`DerivedChannelConfig`) computes its value from other channels with a TL-Script expression. The values of the `inputs` channels are variables of `expr`, named after their channels: with `inputs="a, b"` the expression reads `$a` and `$b`, without declaring them (see [named inputs](#named-inputs-variables-instead-of-parameters)). The value is computed when the view is built and computed again whenever one of the inputs takes a new value; listeners are told only when the result differs from the previous one. A derived channel is read-only: an element writing it fails (`UnsupportedOperationException`) unless the channel has a `reverse` function (below).
- **`<source-channel name="…"><source class="…"/></source-channel>`** (`SourceChannelConfig`) is a writable value that additionally receives what a server-side source publishes, see [below](#channels-fed-by-a-server-side-source-source-channel).

```xml
<channels>
	<channel name="element"/>
	<derived-channel name="photo"
		expr="$element.get(`test.flowchart:FlowNode#image`)"
		inputs="element"
	/>
</channels>
```

The channels are created in declaration order, and a derived channel resolves its inputs when it is created. An input must therefore be declared above the derived channel reading it - a derived channel may read another derived channel declared before it - or be supplied by the view embedding this one (see [`<view-ref>`](#embedding-a-view-view-ref-and-bind) below). A reference to a channel the view does not know fails with "Unknown channel" and lists the channels that are known.

`inputs` takes two notations, here as everywhere in the view layer (`Inputs`): the comma-separated attribute `inputs="selectedCustomer, editMode"` and nested elements `<inputs><input channel="selectedCustomer"/><input channel="editMode"/></inputs>`.

### Named inputs: variables instead of parameters

A script of a channel sees the value of each input as a variable named after the channel. An input whose channel name does not suit the script - a name that is no TL-Script identifier, or one that says less than the script needs - is renamed with `as`, in either notation: `inputs="selection as ticket, editMode"` or `<input channel="selection" as="ticket"/>`; the script then reads `$ticket` and `$editMode`.

```xml
<derived-channel name="title"
	inputs="selection as ticket"
	expr="$ticket.get(`demo.tickets:Ticket#name`)"
/>
```

The script declares no parameters. It is evaluated without positional arguments, so a lambda header (`t -> $t.get(…)`) would make its result a function instead of a value; a script whose top-level expression is a lambda is therefore rejected with a configuration error when the view is loaded, naming the variables the script can use. The same holds for every script of the view layer that sees named values - the scripts of an action chain, the `init` of a channel, the `reverse` of a derived channel, the scripts of an element over its inputs (a table's `<rows>`, an `<object-list>`'s `items`, an `<alert>`'s texts, the `test` of a `<case>`, …) - also for a script that sees no variables at all. The script editor offers the variables for completion after `$`.

A value an element supplies to its script itself - the row of a table, the node of a `<tree>`, the element of an `<object-list>` or a `<kanban-board>`, the entry of a `<calendar>` - is a variable behind the inputs, under a default name the supplying element changes with a `…-as` attribute (`row-as`, `node-as`, `element-as`, `column-as`, `entry-as`; `InputScript`, `Supplies`). An input named like such a value is a configuration error and is renamed with `as`.

### A writable channel initialized from other channels: `init`

A `<channel>` with an `init` function is a writable value that takes its value from other channels - an input field that shows the stored value of the selected object, lets the user enter a new one, and shows the stored value again for the next selection:

```xml
<channels>
	<channel name="project"/>
	<channel name="budget"
		init="$project.get(`tl.demo.projectManagement:ProjectScope#budget`)"
		inputs="project"
	/>
</channels>
```

`inputs` and `init` work like `inputs` and `expr` of a derived channel: the values of the inputs are variables of the function, named after their channels or by their `as` name. The channel (`InitializedViewChannel`) is filled with the result

- when the view is built,
- whenever an input takes a new value - also when the result is the same as before, so a value the user entered for the previous project is replaced,
- while the view is displayed, whenever an object an input holds (or an object of one of the `observed-types`) is changed and the change gives the function a different result. After the budget entered above is stored on the project, the channel shows the stored value; a change of the project that leaves the budget as it is keeps a value entered meanwhile. When the view is displayed again, the function is evaluated again and the channel is filled if the result changed while it was hidden.

Between two fills the channel is an ordinary writable value: an element or action writes it, veto listeners are asked, and listeners hear every new value. A fill asks no veto listener of the channel itself; unsaved changes bound to it block the write of the input instead, like for a derived channel.

Without `inputs`, `init` is evaluated once when the view is built and gives the channel its initial value (`init="10"`); `observed-types` evaluate it again on changes of objects of those types. `inputs` or `observed-types` without `init` are a configuration error. A `<channel>` without `init` behaves as described above: it starts empty.

### Following edits of the input objects: `observed-types`

A derived channel follows more than new input values. While the view declaring it is displayed, it also observes the objects its inputs hold (`ObservingChannel`), so an expression reading an attribute of the input object is recomputed when that attribute is stored, although the input channel keeps pointing to the same object. While the view is not displayed - a tab nobody looks at, a closed dialog - nothing is observed, and the value is recomputed when the view is displayed again.

An expression that reads beyond the input objects - all instances of a type, an attribute of the input's container - names the types whose create, update and delete events recompute it in `observed-types` (comma-separated type names). A derived channel without inputs is otherwise computed once, when the view is built:

```xml
<derived-channel name="ticketCount"
	expr="{ tickets = all(`demo.tickets:Ticket`).size(); if($tickets == 0, null, $tickets); }"
	observed-types="demo.tickets:Ticket"
/>
```

### Writing through a derived channel: `reverse`

A derived channel with a `reverse` function is bidirectional. A value written to it is the variable `$value` of `reverse` - next to the inputs, which `reverse` sees like `expr` -, and the result is written to the *first* input channel; the forward `expr` then recomputes the derived value from the updated input. This is what an element that writes a different representation of a value needs - most of all a URL binding, which writes the text of an address segment into its channel when a link is opened. The tickets demo carries the selected ticket in the address bar by its identifier:

```xml
<channels>
	<channel name="ticket"/>
	<derived-channel name="ticketId"
		expr="objectId($ticket)"
		inputs="ticket"
		reverse="$value.objectResolve(`demo.tickets:Ticket`)"
	/>
</channels>
<param-bindings>
	<bind
		channel="ticketId"
		prefix="ticket"
		route-param="ticket"
	/>
</param-bindings>
```

Selecting a ticket writes `ticket`, `ticketId` follows, and the route parameter shows the identifier; opening a link writes the identifier into `ticketId`, and `reverse` resolves it into the ticket on `ticket`. The bindings of channels to the URL are described in [URL routing](doc:view-layer/navigation#url-routing-what-ends-up-in-the-address-bar).

A form with unsaved input that is bound to a derived channel also blocks the write of the inputs the value is computed from, see [unsaved changes](doc:view-layer/forms#unsaved-changes-are-asked-before-a-channel-write-transitively).

## Channels fed by a server-side source: `<source-channel>`

Some values change without a request of the window: a listener of a message bus receives a message, a queue fills up, a timer ticks. A `<source-channel>` (`SourceChannelConfig`) is a channel such a server-side source writes into. It is a writable value like a `<channel>` - elements and actions may still write it - and additionally holds the latest value its source published. A view that only has to follow commits to the model does not need a source: a `<derived-channel>` with `observed-types` (above) does that.

```xml
<channels>
	<source-channel name="status" update-interval="500">
		<source class="my.app.StatusSource"/>
	</source-channel>
</channels>
```

The source is a Java class implementing `ChannelSource`, configured in the `<source>` element; it is created once from the configuration and serves every view instance declaring the channel. Its single method `subscribe(ViewContext, ChannelSink)` returns a `Registration`:

- **On subscribe, the source publishes its current state** into the `ChannelSink` right away - the channel knows nothing of what happened before - and every later state after that.
- **`ChannelSink.publish(value)` may be called from any thread and never blocks.** The value is written to the channel later, in the sub-session of the request that subscribed and under the window's interaction, so the channel write, the controls updating from it and the updates reaching the browser are serialized against the requests of the session like a command is.
- **The channel holds state, not events.** `update-interval` is the shortest time in milliseconds between two deliveries (default 250, zero delivers every value): a value published while an earlier one is still held back replaces it, and the last value of a burst is always delivered. A source whose consumers must see every message accumulates them into the value itself.
- **The subscription follows the display.** The channel subscribes when the view declaring it is displayed and disposes the registration when the view is hidden - a tab nobody looks at, a closed dialog - or destroyed, exactly like the observation of a derived channel. The source therefore sees one subscription per displayed view instance per window, and one again each time a view is displayed anew. After the registration is disposed, publishing into its sink is harmless and ignored. A source that shares one external connection among its subscribers manages that sharing itself.
- A source whose `subscribe` fails is logged, and the channel stays an ordinary writable value.

Java code that has a `ViewContext` and a channel at hand - an action starting background work, an element of its own - does not need a declared source: a `ChannelPublisher` created on a request thread delivers values into the channel from any thread with the same threading, throttling and conflation; `close()` ends it.

Demo: `com.top_logic.demo.react/src/main/webapp/WEB-INF/views/demo/live-channel-demo.view.xml` with the source `com.top_logic.demo.react.channel.DemoTickerSource`.

## How elements use channels

Elements and commands name channels in their configuration; a channel reference is the plain channel name. The common properties:

- **`input="ch"`** - the object an element displays: `<form input="selectedNode">`, `<text input="element"/>`. The element follows the channel and shows the new value.
- **`selection="ch"`** - what the user selects in a `<table>`, a tree, a `<flow-diagram>`, a `<kanban-board>` or an `<object-list>` is written to the channel, and a value written to the channel by another element is marked as selected (see [selecting rows and nodes](doc:view-layer/tables#selecting-rows-and-nodes)).
- **`inputs="a, b"`** - the channels whose values an element's TL-Script functions read: the `<rows>` of a `<table>`, the `items` of an `<object-list>`, a `<derived-channel>`. The values are read every time a function is applied. The scripts of a channel see them as variables named after the channels or renamed with `as` ([named inputs](#named-inputs-variables-instead-of-parameters)).
- **`inputs="a as x, b"`** on a command or an action of a chain - the channels whose values are *named arguments*, which the scripts of the chain use as variables (`$x`, `$b`) without declaring them; the first input of a command is its main argument, the input of its executability rules: `<generic-command inputs="model">`. See [named arguments](doc:view-layer/commands#a-command-is-a-chain-of-actions-over-named-arguments).
- **`value="ch"`** - a channel bound to an input control in both directions, as with `<value-input value="search"/>`.

Several elements sharing one channel is all a master-detail needs. A table and a form share the selected flow node:

```xml
<table
	selection="selectedNode"
	types="test.flowchart:FlowNode"
>
	<columns>
		<column attribute="name"/>
	</columns>
	<rows>all(`test.flowchart:FlowNode`)</rows>
</table>
<form
	input="selectedNode"
	with-inset="true"
>
	<field attribute="name"/>
</form>
```

## Deleted objects on a channel

A channel a view declares observes the objects it holds while the view is on screen. The channels a dialog, a tile frame, a template instance, a wizard step or an object list creates for their content observe while that content is displayed, exactly like declared ones; Java code creating channels for a control subtree hands them to `ObservingChannels.observeWhileDisplayed` with the root control of that subtree. When one of them is deleted - in another view, by another user - the channel drops it with an ordinary change: a single object becomes `null`, a collection becomes the collection of the remaining members (a set stays a set, even with one or no member left). Every reader hears that change at once - a detail panel empties, a `<derived-channel>` recomputes, a command is disabled. A channel whose view is off screen (a tab nobody looks at, a closed dialog) observes nothing; it validates its value when the view is displayed again and announces a deletion that happened meanwhile then. The drop does not ask the veto listeners: nothing can be saved on an object that is gone.

Which value comes next is up to the selector writing the channel, since only it knows a sensible next selection. A selector takes the drop as the announcement of the deletion, not as a request to clear its selection, and answers it with its next selection when its own update removes the deleted element: a `<tree>` selects the node the deleted object hung in, a `<table>` writes what is left of its selection (see [selecting rows and nodes](doc:view-layer/tables#selecting-rows-and-nodes)).

Where a deleted object still reaches a reader - a channel that is not observed, a value supplied to a command directly - it counts as a missing value: in TL-Script `instanceOf` answers `false` and an attribute access answers the empty value, so a `<derived-channel>` or a `<visible-if>` evaluates to its "nothing" branch. A command counts a deleted input object as no input: `<null-input-disabled/>` disables it, and the model access rule an action brings asks nothing of the object.

## Embedding a view: `<view-ref>` and `<bind>`

`<view-ref view="…"/>` (`ReferenceElement`) displays another `.view.xml` in place; the path is relative to `/WEB-INF/views/`. Every `<view-ref>` creates an instance of the referenced view with a channel scope of its own: the channels it declares belong to this instance, and none of the channels of the enclosing view are visible in it. The same view referenced twice - the card of a repeater, once per element - gets separate channels per instance.

The two views connect only through `<bind channel="…" to="…"/>` (`ChannelBindingConfig`) inside the `<view-ref>`:

- `channel` is the name of a channel the **referenced** view declares,
- `to` is a channel of the **enclosing** view.

A bound channel is not a copy: the channel object of the enclosing view is registered in the referenced view under the name `channel`, and the referenced view's own declaration of that name is skipped. Both views read and write the same channel. A declared channel that is not bound is created by the referenced view as usual and stays private to the instance.

```xml
<view-ref view="tickets/comment.view.xml">
	<bind
		channel="comment"
		to="comment"
	/>
	<bind
		channel="draft"
		to="newComment"
	/>
</view-ref>
```

`tickets/comment.view.xml` declares `comment` and `draft` and computes its derived channels from them; the enclosing view supplies its own `comment` and `newComment` channels for them:

```xml
<channels>
	<channel name="comment"/>
	<channel name="draft"/>
	<derived-channel name="author"
		expr="$comment.get(`demo.tickets:Comment#author`)"
		inputs="comment"
	/>
</channels>
```

The referenced view is loaded when the `<view-ref>` builds its control, not when the enclosing view is parsed. A view can therefore reference itself - each instance gets a scope of its own - as long as the self-reference sits in content that is built on demand (a dialog, a tab, an item of a list); an unconditional self-reference recurses while the control is built. A referenced view that cannot be loaded renders an error placeholder in its place, so the enclosing application stays usable.

A dialog is connected the same way: `<open-dialog dialog-view="…">` creates the dialog view with a scope of its own, writes the value it processes - the default argument of the chain, or the argument its `value` names (see [commands](doc:view-layer/commands#actions-processing-one-value-value)) - into the dialog channel named by `bind-input-to` (`model` by default), and takes `<bind channel="…" to="…"/>` elements with the same meaning as in `<view-ref>`:

```xml
<open-dialog
	bind-input-to="model"
	dialog-view="demo/create-constraint-test.view.xml"
>
	<bind
		channel="selection"
		to="selectedObject"
	/>
</open-dialog>
```
