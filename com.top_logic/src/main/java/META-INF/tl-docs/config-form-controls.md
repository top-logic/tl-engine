---
description: Read before choosing or changing the input control of a configuration property in a configuration form (classic EditorFactory or React ConfigControlService) - the meaning annotations @MultiLine, @CommonMark, @Code with CodeLanguage, @Html, @Inline, @OptionLabels, @Encrypted and @Options, the <annotation-controls> and <type-controls> / <providers> maps, the SPIs AnnotationControlProvider and ConfigAnnotationControlProvider, and defining a meaning annotation of your own (e.g. @ApplicationRelativeURL).
order: 50
---

# Controls of configuration forms

A configuration form displays the properties of a `ConfigurationItem`. Which input control a
property gets is not written at the property. The property states what its value *means* through
an annotation (its text spans several lines, it is HTML, it is XML source), and each form stack maps
that meaning to a control in its configuration. The same annotation therefore works in the classic
UI and in the React UI, and a module that brings a better editor (a code editor, a WYSIWYG editor)
takes over all properties of that meaning by overriding one map entry.

The two form stacks:

| Stack | Builds the form | Configuration |
|---|---|---|
| Classic UI | `EditorFactory` (`com.top_logic`) | `EditorFactory$Config` in `<configs>` |
| React UI | `ConfigControlService` (`com.top_logic.layout.configedit`) | the service's `<instance>` |

## How the control is chosen

### Classic UI (`EditorFactory`)

1. `@Encrypted` always gets the password input. No map can choose another control, so no module
   can display a secret in the clear.
2. `<annotation-controls>`: the first entry whose annotation the property carries and whose
   provider chooses a control. Annotations are found on the property, on its value type, and in
   the form's `<customizations>`. This also applies to a field created by a `@PropertyEditor`.
3. `<type-controls>`: the entry for the property type, else for its nearest superclass, else for
   the first interface in breadth-first order. This also applies to a field created by a
   `@PropertyEditor`.
4. The control of the editor that creates the field: a text line, a checkbox, a date chooser, a
   select for an enum or `@Options`, the color chooser for a `Color`. A single select whose options
   form a tree or whose option count exceeds `selection-dialog-threshold` (default 20) is displayed
   with a selection dialog.

### React UI (`ConfigControlService`)

1. `@Encrypted` always gets the password input. No map can choose another control, so no module
   can display a secret in the clear.
2. `<annotation-controls>`: the first entry whose annotation the property (or its value type)
   carries and whose provider creates a control.
3. A property edited by selecting (`@Options`, an enum) gets the searchable dropdown, for one or
   several values.
4. `<providers>`: the entry for the exact Java type of the property value.
5. `<formats>`: the entry for the property's value provider class or one of its superclasses
   (e.g. a time-of-day `Date` gets the time picker).
6. The built-in widget for the Java type, otherwise a text input through the property's format.

An annotation chooses a control, but not the form the value is edited in: the control edits the
value as the remaining steps hold it (typed, as selected options, or as format text). A provider
that cannot edit the value in that form returns `null`, and the next step decides.

## Meaning annotations

All in `com.top_logic.layout.form.values.edit.annotation` unless noted, usable at the property or
at its value type; `@Inline` and `@Encrypted` only at the property.

| Annotation | Meaning | Classic UI | React UI |
|---|---|---|---|
| `@MultiLine(rows)` | The text spans several lines. | text area | text area |
| `@CommonMark` | The text is CommonMark; implies `@MultiLine`. | text area, formatted when read-only | same |
| `@Code(language)` | The text is source code in a `CodeLanguage` (`PLAIN`, `XML`, `HTML`, `JSON`, `CSS`, `JAVASCRIPT`, `MARKDOWN`). | code editor if `tl-layout-codeedit` is present, else text area | code editor if `tl-layout-react-codeedit` is present, else text area |
| `@Html(features)` | The text is HTML; `features` names a feature set of the editor (`""` = standard, `macro` = with source view for embedded TL-Script). | WYSIWYG editor if `tl-layout-wysiwyg` is present, else HTML source | WYSIWYG editor if `tl-layout-react-wysiwyg` is present, else HTML source |
| `@Inline` | A nested item is shown without a group of its own: no heading, no type selector, no remove button. Only for `ITEM` properties edited as nested form. | properties inline | properties inline |
| `@OptionLabels(labelProvider)` | The `LabelProvider` for the options and the value. | labels of the select; read-only value of a property that cannot be set | same |
| `@Encrypted` (`com.top_logic.basic.config.annotation`) | The value is a secret. | password input (always, see above) | password input (always, see above) |
| `@Options(fun, mapping)` | The value is chosen from options. | dropdown, selection dialog above the threshold or for tree options | searchable dropdown |

`@MultiLine` on a property that also carries `@Code`, `@Html` or `@CommonMark` gives the number of
rows of the text area used when no dedicated editor is present.

## Value-type mappings

A mapping by value type covers every property of that type without an annotation. The engine
contributes:

- Classic UI, `com.top_logic.model.search`: TL-Script (`Expr`) and script functions
  (`ScriptFunction0` to `ScriptFunction4`) are edited in the TL-Script code editor.
- React UI, `com.top_logic.layout.configedit`: `ThemeImage` (icon select), `Color` (color input);
  `com.top_logic.model.search.react`: `Expr` (TL-Script editor).

## Contributing or overriding a mapping

A module adds or replaces entries in its own `*.config.xml`. Entries are keyed by the annotation
or type, and the configuration files of all modules are merged in module order, so a module above
another overrides its entry, e.g. a code editor module replaces the text-area entry for `@Code`.
The order of `<annotation-controls>` decides for a property that carries several of the
annotations. In both stacks, an entry for an annotation already configured is updated in place and
keeps its position; an entry for a new annotation is appended. `config:position` (`begin`, `end`,
`before`, `after` with `config:reference`) places or moves an entry elsewhere, and
`config:operation="remove"` drops one.

Classic UI:

```xml
<configs>
	<config config:interface="com.top_logic.layout.form.values.edit.EditorFactory$Config">
		<annotation-controls>
			<annotation-control annotation="com.top_logic.layout.form.values.edit.annotation.Code">
				<impl class="com.top_logic.layout.codeedit.control.CodeEditorProvider"/>
			</annotation-control>
		</annotation-controls>
		<type-controls>
			<type-control type="com.top_logic.model.search.expr.config.dom.Expr">
				<impl class="com.top_logic.model.search.ui.TLScriptCodeEditorControl$CP"/>
			</type-control>
		</type-controls>
	</config>
</configs>
```

An `<annotation-control>` takes an `AnnotationControlProvider` (`com.top_logic.layout.form.values.edit`);
a `<type-control>` takes a `ControlProvider` (`com.top_logic.layout.form.template`).

React UI:

```xml
<services>
	<config service-class="com.top_logic.layout.configedit.ConfigControlService">
		<instance>
			<annotation-controls>
				<annotation-control annotation="com.top_logic.layout.form.values.edit.annotation.Code">
					<impl class="com.top_logic.layout.react.codeedit.CodeConfigProvider"/>
				</annotation-control>
			</annotation-controls>
			<providers>
				<provider type="com.top_logic.model.search.expr.config.dom.Expr">
					<impl class="com.top_logic.model.search.react.TLScriptConfigControlProvider"/>
				</provider>
			</providers>
		</instance>
	</config>
</services>
```

An `<annotation-control>` takes a `ConfigAnnotationControlProvider`, a `<provider>` a
`ConfigControlProvider` (both `com.top_logic.layout.configedit`).

### The SPIs

- `AnnotationControlProvider<A>.getControlProvider(editorFactory, property, annotation)` returns
  the `ControlProvider` for the field. Look up further annotations of the property through
  `EditorFactory.getAnnotation(property, type)`, so that form customizations count.
- `ConfigAnnotationControlProvider<A>.createControl(context, model, annotation)` returns the
  `ReactControl`. `ConfigControlService.holdsText(model)` tells whether the value is held as text.

Both return `null` to decline: the property is then displayed as if it did not carry the
annotation, and the next entry or step decides.

## A meaning annotation of your own

When a module has properties of a meaning the engine does not know, it defines an annotation for
that meaning in the module and maps it there. Example from `com.top_logic.service.openapi.server`:
the base URL of the OpenAPI server is a path relative to the application URL, and a form shows the
full URL as link.

```java
/** States that a string property holds a URL path relative to the URL of the application. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("application-relative-url")
public @interface ApplicationRelativeURL {
	// Pure marker.
}

public class ApplicationRelativeURLProvider implements AnnotationControlProvider<ApplicationRelativeURL> {
	private static final ControlProvider LINK = new DisplayFullPathTemplate();

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			ApplicationRelativeURL annotation) {
		return LINK;
	}
}
```

The property is annotated (`@ApplicationRelativeURL String getBaseURL();`) and the module's
`tl-service-openapi-server.conf.config.xml` adds the `<annotation-control>` entry. Rules:

- Name the annotation after what the value *is*, not after the control. The control is the
  decision of the form stack and may differ between the stacks.
- Give it a `@TagName`, so it can also be added through a form's `<customizations>`.
- Map it in every stack the module serves. A stack without an entry displays the property with its
  default control.
- Keep the annotation in the lowest module that owns the meaning, and the provider in the module
  that owns the control.
