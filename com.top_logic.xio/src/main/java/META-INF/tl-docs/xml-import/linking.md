---
description: Read before connecting imported objects in an XML import definition (com.top_logic.xio) - <linking> (Linker) with target-var and value-var, the linkings <list-linking> (ListLinking), <ref-linking> (RefLinking) with reverse, and <custom-linking> (CustomLinking) with a TL-Script expr, how the first linking matching the type is chosen, and several linkings or several <linking> handlers for one object.
order: 40
---

# Linking

A created object is not connected to anything by itself. `<linking>` (`Linker`) connects the object in the variable `value-var` (default `this`) to the object in the variable `target-var` (default `scope`) - in the content of an `<object>`, the new object to the object of the enclosing element. How it is connected is decided by the *linkings* inside. `<linking>` does not read or consume the element.

```xml
<object model-type="test.com.top_logic.xio.importer.test1:B" id-attribute="name">
	<linking>
		<list-linking name="test.com.top_logic.xio.importer.test1:A#bs"/>
	</linking>
	<property name="name" xml-attribute="name"/>
	<dispatch/>
</object>
```

## Choosing a linking

A linking names the reference it sets as `name="module:Type#reference"`. It applies only if the object whose reference is set is an instance of `Type`. `<linking>` tries its linkings in order and performs the first one that applies; if none applies, it reports `No matching linking found`. So one definition can link an element into different parents, depending on where it occurs:

```xml
<tag name="floor">
	<object model-type="test.dnd:Floor">
		<property name="name" xml-attribute="name"/>
		<linking>
			<list-linking name="test.dnd:Library#elements"/>
			<list-linking name="test.dnd:Factory#floors"/>
		</linking>
		<ref handler-id="all-types"/>
	</object>
</tag>
```

A `<floor>` directly in the library is added to the library's `elements`, a `<floor>` within a `<factory>` to the factory's `floors`.

To set *several* references, use several `<linking>` handlers - each of them performs one linking:

```xml
<object model-type="test.com.top_logic.xio.importer.test7:A" id-attribute="name" join-duplicates="true">
	<linking>
		<list-linking name="test.com.top_logic.xio.importer.test7:R#set"/>
	</linking>
	<linking>
		<list-linking name="test.com.top_logic.xio.importer.test7:R#list"/>
	</linking>
	<dispatch/>
</object>
```

A `<linking>` without any linking does nothing. A missing target or value object (variable bound to `null`) is an error. If one of the objects is a [forward reference](doc:xml-import/objects#forward-references), the linking is performed when it is defined.

## `<list-linking>`

`<list-linking name="module:Type#reference"/>` (`ListLinking`) adds the value to a multiple reference of the target. An object that is already contained is not added again, so an object imported several times (`join-duplicates`, or an update of a known object) is linked once. An object that is already part of another container cannot be added to a composite reference.

## `<ref-linking>`

`<ref-linking name="module:Type#reference"/>` (`RefLinking`) sets a single-valued reference of the target to the value.

```xml
<tag name="entry">
	<object model-type="test.dnd:Entry">
		<property name="name" xml-attribute="name"/>
		<linking>
			<ref-linking name="test.dnd:Location#entry"/>
		</linking>
		<dispatch/>
	</object>
</tag>
```

## `reverse`

`<list-linking>` and `<ref-linking>` set a reference of the target by default. With `reverse="true"`, they set the reference of the *value* to the target instead - the inner object points to the outer one. The type check then applies to the value. From the BPMN import, where a `<flowNodeRef>` within a `<lane>` names a node that gets its `lane` set:

```xml
<tag name="flowNodeRef">
	<object-ref id-attribute="text()">
		<linking>
			<ref-linking reverse="true" name="tl.bpe.bpml:Node#lane"/>
		</linking>
	</object-ref>
</tag>
```

## Linking other variables

`target-var` and `value-var` connect any two objects in variables, e.g. objects bound by `var` of enclosing handlers:

```xml
<object model-type="tl.bpe.bpml:AnnotationAssociation" id-attribute="id" var="association-link">
	<object-ref id-attribute="sourceRef" var="src" mandatory="true">
		<object-ref id-attribute="targetRef" var="dst" mandatory="true">
			<linking target-var="association-link" value-var="dst">
				<ref-linking name="tl.bpe.bpml:AnnotationAssociation#target"/>
			</linking>
			<linking target-var="association-link" value-var="src">
				<ref-linking name="tl.bpe.bpml:AnnotationAssociation#source"/>
			</linking>
		</object-ref>
	</object-ref>
</object>
```

## `<custom-linking>`

`<custom-linking expr="..."/>` (`CustomLinking`) calls a TL-Script function with two arguments, the target and the value (`reverse` does not exist here), when both are defined. It always applies, so it is the last linking of a `<linking>`, if combined with others. Its expression runs with the security setting of the import, see [Variables and scripts](doc:xml-import/variables#tl-script-expressions).

```xml
<linking>
	<custom-linking expr="parent -> child -> $parent.add(`my.module:Container#elements`, $child)"/>
</linking>
```
