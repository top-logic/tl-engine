# React WYSIWYG Editor (TipTap)

## Context

TopLogic's existing WYSIWYG editing uses CKEditor 4 (`ext.com.ckeditor`, version 4.21.0), which reached end-of-life in June 2023. The new React UI layer needs an HTML editor for attributes of type `tl.model.wysiwyg:Html`. This design introduces a TipTap-based React WYSIWYG editor in a dedicated module.

TipTap is built on ProseMirror, is MIT-licensed (compatible with TopLogic's AGPL/BOS dual license), and provides first-class React integration.

## Goals

- Rich-text editing for `tl.model.wysiwyg:Html` attributes in React forms
- Standard formatting: bold, italic, underline, strikethrough, headings, lists, blockquote
- Tables, images (server-upload), links, code blocks, text color
- Server-configurable toolbar (different fields can have different toolbar sets)
- Immutable mode renders plain HTML (no TipTap loaded)
- Debounced value sync to server (300ms)
- Demo integration in `com.top_logic.demo`

## Non-Goals

- Collaborative editing (Y.js) — future enhancement
- Mentions, comments, custom blocks — future enhancement
- Replacement of legacy CKEditor module — coexists
- I18NHtml support — only `tl.model.wysiwyg:Html` for now

## Module Structure

### New Maven Module: `com.top_logic.layout.react.wysiwyg`

- **Artifact:** `tl-layout-react-wysiwyg`
- **Parent:** `tl-parent-core-internal`
- **Dependencies:**
  - `com.top_logic.layout.react` (ReactControl, ReactFormFieldControl, Bridge, SSE, UploadHandler)
  - `com.top_logic.layout.wysiwyg` (StructuredText class for value conversion; lighter than `com.top_logic.model.wysiwyg` which pulls in storage, contact, search)

### npm Dependencies

```json
{
  "dependencies": {
    "@tiptap/react": "^2.10",
    "@tiptap/starter-kit": "^2.10",
    "@tiptap/extension-underline": "^2.10",
    "@tiptap/extension-link": "^2.10",
    "@tiptap/extension-image": "^2.10",
    "@tiptap/extension-table": "^2.10",
    "@tiptap/extension-table-row": "^2.10",
    "@tiptap/extension-table-cell": "^2.10",
    "@tiptap/extension-table-header": "^2.10",
    "@tiptap/extension-code-block-lowlight": "^2.10",
    "@tiptap/extension-color": "^2.10",
    "@tiptap/extension-text-style": "^2.10"
  },
  "devDependencies": {
    "typescript": "^5.7",
    "vite": "^6.0",
    "@vitejs/plugin-react": "^4.3",
    "@types/react": "^19.0.0"
  }
}
```

### Vite Build

- Entry: `react-src/wysiwyg-entry.ts`
- Output: `src/main/webapp/script/tl-react-wysiwyg.js`
- Externalizes `tl-react-bridge` (no duplicate React)
- React shims redirect `react`, `react-dom`, `react/jsx-runtime` to `tl-react-bridge` (same pattern as `com.top_logic.layout.react.chartjs`)
- TipTap extensions are bundled (self-contained module)

### File Layout

```
com.top_logic.layout.react.wysiwyg/
  pom.xml
  package.json
  tsconfig.json
  vite.config.ts
  react-src/
    wysiwyg-entry.ts                    # register('TLWysiwygEditor', ...)
    react-shim.ts                       # Re-export React from tl-react-bridge
    react-dom-shim.ts                   # Re-export ReactDOM from tl-react-bridge
    react-jsx-runtime-shim.ts           # Re-export JSX runtime from tl-react-bridge
    controls/
      TLWysiwygEditor.tsx               # Main component
      WysiwygToolbar.tsx                 # Toolbar component
      WysiwygToolbar.css                # Toolbar styles
      TLWysiwygEditor.css               # Editor styles
  src/
    main/
      java/
        META-INF/
          web-fragment.xml
        com/top_logic/layout/react/wysiwyg/
          ReactWysiwygControl.java      # Java control
          I18NConstants.java            # I18N keys
      webapp/
        WEB-INF/
          conf/
            metaConf.txt                            # Lists config files for module discovery
            tl-layout-react-wysiwyg.conf.config.xml
        script/
          tl-react-wysiwyg.js           # Build output (generated)
```

## DataProvider: Key-Based Image Download

The existing `DataProvider` interface serves a single `BinaryData` per control. The WYSIWYG editor needs to serve multiple images by key. This requires changing `DataProvider` and the `/react-api/data` endpoint.

### DataProvider Interface Change

```java
public interface DataProvider {
    /**
     * Returns the binary data for the given key, or {@code null} if not available.
     *
     * @param key
     *        Identifies the requested data item. Controls with a single item ignore this.
     */
    BinaryData getDownloadData(String key);
}
```

The old `getDownloadData()` (no-arg) is removed. The three existing implementations (`ReactAudioPlayerControl`, `ReactPhotoViewerControl`, `ReactDownloadControl`) are updated to accept and ignore the `key` parameter.

### ReactServlet Change

The `handleDataDownload` method passes the optional `key` query parameter:

```java
String key = request.getParameter("key");
BinaryData data = ((DataProvider) control).getDownloadData(key);
```

### Client-Side: useTLDataUrl

`useTLDataUrl()` stays unchanged — it returns the base URL. The WYSIWYG component appends `&key=<imageKey>` when building `<img src>` URLs:

```typescript
const dataUrl = useTLDataUrl();
const imageUrl = `${dataUrl}&key=${encodeURIComponent(imageKey)}`;
```

### ReactWysiwygControl as DataProvider

```java
public class ReactWysiwygControl extends ReactFormFieldControl
        implements UploadHandler, DataProvider {

    @Override
    public BinaryData getDownloadData(String key) {
        if (_currentValue == null || key == null) {
            return null;
        }
        return _currentValue.getImages().get(key);
    }
}
```

### Image URL Flow (Server → Client)

When sending `value` to the client, the control rewrites `<img src="filename.png">` in the HTML to `<img src="/react-api/data?controlId=...&windowName=...&key=filename.png">`. This is done server-side in `handleModelValueChanged` before calling `putState(VALUE, rewrittenHtml)`.

When receiving HTML back from the client (`valueChanged` command), the control strips the URL prefix back to just the image key, so that the `StructuredText` stores clean `<img src="filename.png">` references.

## React Component: TLWysiwygEditor

### State (from server)

```typescript
interface WysiwygState {
  value: string;              // HTML content
  editable: boolean;          // Edit mode vs. immutable
  toolbar: string[];          // Toolbar buttons to show
  hasError: boolean;
  hasWarnings: boolean;
}
```

### Default Toolbar

```typescript
const DEFAULT_TOOLBAR = [
  'bold', 'italic', 'underline', 'strike',
  '|',
  'heading',
  '|',
  'bulletList', 'orderedList', 'blockquote',
  '|',
  'link', 'image', 'table', 'codeBlock',
  '|',
  'color',
  '|',
  'undo', 'redo'
];
```

The `|` separator renders a visual divider in the toolbar.

### Render Modes

**Editable (`editable: true`):**
- TipTap `EditorProvider` with configured extensions
- `WysiwygToolbar` renders buttons based on `state.toolbar`
- Content changes are debounced (300ms) before sending `valueChanged` command
- Toolbar buttons reflect active marks/nodes (toggle state)

**Immutable (`editable: false`):**
- Renders HTML content in a `<div>` with `dangerouslySetInnerHTML`
- CSS class `tlWysiwygEditor--immutable` for styling
- No TipTap instance loaded — lightweight rendering

### Commands and Uploads

| Mechanism | Arguments | Description |
|-----------|-----------|-------------|
| `valueChanged` (command) | `{ value: string }` | HTML content changed (debounced 300ms) |
| `useTLUpload()` (upload) | FormData with image file | Image file upload via `POST /react-api/upload`, dispatched to `UploadHandler.handleUpload()` |

### Image Upload Flow

1. User clicks image toolbar button → file picker opens
2. File selected → `useTLUpload()` sends FormData to server
3. Server stores image, returns URL via state patch: `{ imageUrl: "..." }`
4. Client receives patch, inserts `<img src="...">` at cursor position
5. `imageUrl` is a one-shot state key (cleared after insertion)

### TipTap Extensions

| Extension | Purpose |
|-----------|---------|
| `StarterKit` | Bold, Italic, Strike, Headings, BulletList, OrderedList, Blockquote, CodeBlock, HorizontalRule, History |
| `Underline` | Underline formatting |
| `Link` | Hyperlinks with URL editing |
| `Image` | Image display (inline) |
| `Table`, `TableRow`, `TableCell`, `TableHeader` | Table editing |
| `Color` + `TextStyle` | Text color |

## Java Control: ReactWysiwygControl

Extends `ReactFormFieldControl`, implements `UploadHandler` for image uploads and `DataProvider` for image downloads. Inherits field value sync, editability tracking, and validation state.

```java
public class ReactWysiwygControl extends ReactFormFieldControl
        implements UploadHandler, DataProvider {

    private static final String TOOLBAR = "toolbar";
    private static final String IMAGE_URL = "imageUrl";

    private StructuredText _currentValue;

    public ReactWysiwygControl(ReactContext context, FieldModel model) {
        super(context, model, "TLWysiwygEditor");
        initToolbar();
    }
}
```

### State Keys

| Key | Type | Description |
|-----|------|-------------|
| `value` | String | HTML content (inherited from ReactFormFieldControl) |
| `editable` | boolean | Edit mode (inherited) |
| `hasError` | boolean | Validation error (inherited) |
| `hasWarnings` | boolean | Validation warnings (inherited) |
| `toolbar` | String[] | Toolbar button identifiers |
| `imageUrl` | String | One-shot: URL of uploaded image |

### Value Handling

The `tl.model.wysiwyg:Html` type stores values as `StructuredText` objects (`com.top_logic.layout.wysiwyg.ui.StructuredText`). The control must convert between `StructuredText` and plain HTML strings:

- **Server → Client**: Extract HTML string from `StructuredText.getSourceCode()`
- **Client → Server**: Wrap HTML string in new `StructuredText`, preserving the existing image map

Override `handleModelValueChanged` and `parseClientValue` for this conversion. The control keeps a reference to the current `StructuredText` so that its image map is preserved across value changes.

### StructuredText Image Handling

`StructuredText` holds both HTML source code and a `Map<String, BinaryData>` of embedded images. The HTML references images by key name in `<img src="...">` tags.

**Image download (existing images):** The control implements `DataProvider` and serves images via `/react-api/data?key=<imageKey>`. See "DataProvider: Key-Based Image Download" section above for details.

**Image upload (new images):** Uploaded images are added to `StructuredText.getImages()` via `addImage(key, binaryData)`. The upload response pushes the image key to the client, which builds the download URL and inserts `<img src="...">` at the cursor position.

**HTML rewriting:** The control rewrites image `src` attributes between the internal format (`src="filename.png"`) and the download URL format (`src="/react-api/data?...&key=filename.png"`) when sending to / receiving from the client.

### Image Upload (UploadHandler)

Image uploads use the `UploadHandler` interface (not `@ReactCommand`), dispatched via `POST /react-api/upload`:

```java
@Override
public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
    for (Part part : parts) {
        String fileName = part.getSubmittedFileName();
        BinaryData imageData = /* wrap Part input stream */;
        _currentValue.addImage(fileName, imageData);
        putState(IMAGE_URL, fileName);
    }
    return HandlerResult.DEFAULT_RESULT;
}
```

The client uses `useTLUpload()` which posts to `/react-api/upload`. The `ReactServlet` dispatches to the control's `handleUpload` method based on the `controlId` parameter.

## Type Mapping: ReactComponentRegistry

The module registers its control for `tl.model.wysiwyg:Html` attributes via configuration overlay:

```xml
<!-- tl-layout-react-wysiwyg.conf.config.xml -->
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <services>
    <config service-class="com.top_logic.layout.react.ReactComponentRegistry">
      <instance>
        <type-mappings>
          <type-mapping type="tl.model.wysiwyg:Html" module="TLWysiwygEditor"/>
        </type-mappings>
      </instance>
    </config>

    <config service-class="com.top_logic.gui.JSFileCompiler">
      <instance>
        <additional-files>
          <file resource="tl-react-wysiwyg.js" type="module" />
        </additional-files>
      </instance>
    </config>
  </services>
</application>
```

This ensures that any form field backed by a `tl.model.wysiwyg:Html` attribute automatically renders the TipTap editor — no per-field configuration needed.

## Demo Integration

### Model Change

Add an Html-typed attribute to an existing demo type. In `DemoTypes.model.xml`, add a `description` property to `DemoTypes:A`:

```xml
<property name="description" type="tl.model.wysiwyg:Html">
  <annotations>
    <sort-order value="200.0"/>
  </annotations>
</property>
```

This attribute will automatically appear in forms for `DemoTypes:A` objects and render as the TipTap editor.

### Demo Dependency

Add `tl-layout-react-wysiwyg` as a dependency in `com.top_logic.demo/pom.xml`.

## Styling

- Editor container: `tlWysiwygEditor` class
- Toolbar: `tlWysiwygToolbar` class
- Immutable view: `tlWysiwygEditor--immutable` class
- Error state: `tlWysiwygEditor--error` class
- Content area inherits TopLogic theme typography
- Toolbar icons use simple SVG or Unicode characters (no icon library dependency)

## Build Integration

The module uses `frontend-maven-plugin` (same pattern as `com.top_logic.layout.react.chartjs`):

1. Install Node v20.10.0
2. Run `npm install`
3. Run `npm run build` (Vite build)
4. Output to `src/main/webapp/script/tl-react-wysiwyg.js`

Maven phase: `generate-resources` (before Java compilation).

## Registration in Engine

Add the module to `tl-parent-engine/pom.xml` module list.
