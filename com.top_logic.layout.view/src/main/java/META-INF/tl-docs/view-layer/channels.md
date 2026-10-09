---
description: Read before declaring or connecting channels in a .view.xml - <channels> with <channel> and <derived-channel> (inputs, expr, reverse, observed-types), how elements read and write channels, and how a <view-ref> or <open-dialog> shares channels with the view it embeds through <bind channel to>.
order: 15
---

# Channels

A channel is a named value of one view instance. Elements read the object they display from a channel and write what the user selects or enters into one, so channels are the wiring between the elements of a view: a table writes its selection, a form displays the object on that channel, a command takes it as its input. A channel holds one value; a listener of the channel is told about every new value that differs from the old one (`Objects.equals`).

## Declaring channels: `<channels>`

The channels of a view are declared in the `<channels>` section of its `<view>` element (`ViewElement.Config.getChannels()`). Every name is unique within the view. Two kinds exist:

- **`<channel name="…"/>`** (`ValueChannelConfig`) is a writable value. It starts empty and holds whatever an element or an action writes into it last.
- **`<derived-channel name="…" inputs="…" expr="…"/>`** (`DerivedChannelConfig`) computes its value from other channels with a TL-Script function. The values of the `inputs` channels are the positional arguments of `expr`, in the order the inputs are listed: with `inputs="a, b"` the function is written `x -> y -> …` and receives the value of `a` as `x` and the value of `b` as `y`. The value is computed when the view is built and computed again whenever one of the inputs takes a new value; listeners are told only when the result differs from the previous one. A derived channel is read-only: an element writing it fails (`UnsupportedOperationException`) unless the channel has a `reverse` function (below).

```xml
<channels>
	<channel name="element"/>
	<derived-channel name="photo"
		expr="n -> $n.get(`test.flowchart:FlowNode#image`)"
		inputs="element"
	/>
</channels>
```

The channels are created in declaration order, and a derived channel resolves its inputs when it is created. An input must therefore be declared above the derived channel reading it - a derived channel may read another derived channel declared before it - or be supplied by the view embedding this one (see [`<view-ref>`](#embedding-a-view-view-ref-and-bind) below). A reference to a channel the view does not know fails with "Unknown channel" and lists the channels that are known.

`inputs` takes two notations, here as everywhere in the view layer (`Inputs`): the comma-separated attribute `inputs="selectedCustomer, editMode"` and nested elements `<inputs><input channel="selectedCustomer"/><input channel="editMode"/></inputs>`.

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

A derived channel with a `reverse` function is bidirectional. A value written to it is passed to `reverse`, and the result is written to the *first* input channel; the forward `expr` then recomputes the derived value from the updated input. This is what an element that writes a different representation of a value needs - most of all a URL binding, which writes the text of an address segment into its channel when a link is opened. The tickets demo carries the selected ticket in the address bar by its identifier:

```xml
<channels>
	<channel name="ticket"/>
	<derived-channel name="ticketId"
		expr="t -> objectId($t)"
		inputs="ticket"
		reverse="id -> $id.objectResolve(`demo.tickets:Ticket`)"
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

## How elements use channels

Elements and commands name channels in their configuration; a channel reference is the plain channel name. The common properties:

- **`input="ch"`** - the object an element displays or a command works on: `<form input="selectedNode">`, `<text input="element"/>`, `<generic-command input="model">`. The element follows the channel and shows the new value.
- **`selection="ch"`** - what the user selects in a `<table>`, a tree or a `<flow-diagram>` is written to the channel, and a value written to the channel by another element is marked as selected (see [selecting rows and nodes](doc:view-layer/tables#selecting-rows-and-nodes)).
- **`inputs="a, b"`** - the channels whose values become the leading arguments of an element's TL-Script functions: the `<rows>` of a `<table>`, the `items` of an `<object-list>`, `<execute-script>`, a `<derived-channel>`. The values are read every time a function is applied.
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
		expr="c -> $c.get(`demo.tickets:Comment#author`)"
		inputs="comment"
	/>
</channels>
```

The referenced view is loaded when the `<view-ref>` builds its control, not when the enclosing view is parsed. A view can therefore reference itself - each instance gets a scope of its own - as long as the self-reference sits in content that is built on demand (a dialog, a tab, an item of a list); an unconditional self-reference recurses while the control is built. A referenced view that cannot be loaded renders an error placeholder in its place, so the enclosing application stays usable.

A dialog is connected the same way: `<open-dialog dialog-view="…">` creates the dialog view with a scope of its own, writes the command's input into the dialog channel named by `bind-input-to` (`model` by default), and takes `<bind channel="…" to="…"/>` elements with the same meaning as in `<view-ref>`:

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
