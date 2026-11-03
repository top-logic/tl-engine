---
description: Read before using variables or TL-Script in an XML import definition (com.top_logic.xio) - this and scope, the var option of object handlers, <with-var> (WithVar) binding an XML attribute, <in-scope> (ScopedImportHandler) with expr, argument-var and scope-var, the *-var options (target-var, value-var, argument-var, input-var, chain-var, language-var, fill-forward-var) and an empty variable name, and the TL-Script expressions of a definition with their arguments (in-scope expr, assign value, case predicate with arguments, custom-linking expr, resolve-existing objects / key / partition, update condition) and the security setting they run with.
order: 50
---

# Variables and scripts

## Variables

Handlers pass objects and values to each other through variables of the import context. A handler that binds a variable binds it for its content only; when the content is done, the previous binding is in effect again. A variable bound in an outer handler is visible in all inner ones, also in handlers applied through a `<ref>`.

| Variable | Bound by |
| --- | --- |
| `this` | The context object of the import at the top level; the object of an object handler (`<object>`, `<object-ref>`, `<forward>`, `<with-result>`) for its content; the known object in an [`<update>` or `<update-known>`](doc:xml-import/existing-data); the result of an `<in-scope>` expression. |
| `scope` | An object handler, to the `this` of its caller, for its content. Not bound at the top level. |
| `var` of an object handler, `<update>` or an `<update-known>` tag | The same object as `this`, under a name that stays valid when inner handlers rebind `this`. |
| `<with-var var="...">` | The value of an XML attribute. |
| `<chain chain-var="...">` | The result of the previous handler of the chain. |

Handlers read variables through options ending in `-var`. Their defaults make the common case work without naming any variable:

| Option | Of | Default |
| --- | --- | --- |
| `target-var` | value handlers (`<property>`, `<reference>`, `<set-translation>`, `<assign>`, ...) | `this` |
| `target-var` / `value-var` | `<linking>` | `scope` / `this` |
| `argument-var` | `<assign>`, `<in-scope>` | `scope` |
| `argument-var` | `<resolve-existing>` | `this` |
| `input-var` / `chain-var` | `<chain>` | `scope` / `scope` |
| `scope-var` | `<in-scope>` | `this` |

An empty value (e.g. `argument-var=""`) means no variable: the handler passes `null` as argument, respectively binds nothing. Reading a variable that is not bound fails the import (e.g. the default `argument-var="scope"` of `<in-scope>` at the top level, where `scope` is not bound).

## `<with-var>`

`<with-var var="..." xml-attribute="..."/>` (`WithVar`) binds the variable `var` to the value of the XML attribute of the current element and applies its content handlers to the element. If the element has no such attribute, or an empty one, the content is applied with the variable unchanged - a binding of an enclosing element stays in effect. This makes an attribute that an inner element may override, such as a default language:

```xml
<tag name="data">
	<with-var var="lang" xml-attribute="lang">
		<dispatch>
			<tag name="item">
				<with-var var="lang" xml-attribute="lang">
					<object model-type="my.module:Item" id-attribute="id">
						<set-translation name="name" xml-attribute="name" language-var="lang"/>
						<dispatch/>
					</object>
				</with-var>
			</tag>
		</dispatch>
	</with-var>
</tag>
```

Whether `<with-var>` consumes the element depends on its content.

## `<in-scope>`

`<in-scope expr="..."/>` (`ScopedImportHandler`) evaluates the TL-Script `expr` and binds the result to `scope-var` (default `this`) for its content. `expr` is a function receiving the value of `argument-var` (default `scope`). The typical use selects the object into which the content is imported, independently of the import context:

```xml
<tag name="items">
	<in-scope argument-var="" expr="`test.dnd#LIBRARY`">
		<dispatch id="all-types">
			<tag name="factory">
				<object model-type="test.dnd:Factory">
					<property name="name" xml-attribute="name"/>
					<linking>
						<list-linking name="test.dnd:Library#elements"/>
					</linking>
					<ref handler-id="all-types"/>
				</object>
			</tag>
		</dispatch>
	</in-scope>
</tag>
```

The `<object>` inside gets the library as `scope`, and the `<linking>` adds the factory to it. `argument-var=""` passes no argument, because the expression is a constant (the singleton `LIBRARY` of the module `test.dnd`) and `scope` is not bound at the top level.

## TL-Script expressions

Several options of a definition are TL-Script expressions. Each one is called with a fixed list of arguments, so it is written as a function with one parameter per argument (`x -> ...`, `c -> p -> ...`); an expression that does not need its arguments may ignore them, see [Passing arguments to TL-Script](doc:tl-script-arguments).

| Option | Arguments | Result |
| --- | --- | --- |
| `expr` of `<in-scope>` | the value of `argument-var` | the new scope object |
| `value` of `<assign>` | the value of `argument-var` | the value to store |
| `predicate` of `<case>` / `<if>` | the values of the variables in `arguments` (comma-separated, default `this`) | a boolean |
| `expr` of `<custom-linking>` | the target and the value of the `<linking>` | ignored |
| `objects` of `<resolve-existing>` | the value of `argument-var`, with a `partition` also the partition key | the existing objects |
| `key` of `<resolve-existing>` | an existing object | its key |
| `partition` of `<resolve-existing>` | an ID that is not known | its partition key |
| `condition` of `<update>` | the known object, then the values of the variables in `arguments` | a boolean |

The expressions may have side effects, e.g. a `<custom-linking>` that adds the value to a reference, or an `objects` expression that counts its evaluations in a test.

Arguments that are [forward references](doc:xml-import/objects#forward-references) are handled differently: a `predicate` waits until all its arguments are defined (see [`<switch-block>`](doc:xml-import/structure#switch-block-by-predicate)), a `<custom-linking>` until target and value are defined, and a `condition` of `<update>` reports an argument that is not yet imported as error and does not update.

### Security

The expressions are evaluated through the `ModelBinding` of the import. Whether they run with the access rights of the current user is its `usesSecurity` setting (`AbstractModelBinding#usesSecurity()`): an import through the `XMLImportCommand` runs with the rights of the definition (the command's execution right decides whether the user may import at all), an import through a `ParseXml` TL-Script function with the rights of the calling script, so that the function is no way around the access checks of the script.
