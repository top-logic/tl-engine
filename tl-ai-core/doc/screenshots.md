# Looking at a captured screenshot

`browserScreenshot` drives the session's headless Chromium, captures a PNG and persists it as a
`tl.ai.screenshots:Screenshot`. Until issue #216 that was the end of the story: the answer named the
object's TID, nothing served the bytes and nothing drew them, so the feature was write-only from a
user's point of view. This is the delivery path.

## The route

`ScreenshotServlet`, mapped to `/screenshot/*` by `tl-ai-core`'s `web-fragment.xml`, answers
`GET /screenshot/{id}` with the stored image.

- **`{id}` is the screenshot's own handle**, not its TID: 16 bytes from `SecureRandom`, base64url,
  assigned by `ScreenshotService.createScreenshot`. `ScreenshotService.viewUrl(screenshot)` builds
  the URL, absolute when the deployment declares a `%HOST%` and root-relative otherwise
  (`ApplicationUrl`).
- **Content type** comes from the object (`Screenshot#contentType`), but only within `image/*`;
  anything else is served as `application/octet-stream`, so a stored type that is not an image can
  never be rendered as a document in this application's origin. `X-Content-Type-Options: nosniff`
  says the declared type is the only type.
- **Caching** is `private, max-age=31536000, immutable`. The bytes never change for the life of an
  id, and `private` keeps the answer out of every shared cache - it depends on who asked.

## Who may look

**The owner, or an administrator** - `ScreenshotService.mayRead`. A screenshot is a picture of a
page somebody drove a browser to on their own account: another system's inbox, a customer record, a
login form with a name in it. That is the capturing user's material.

This is deliberately stricter than `/html/{id}`. `GeneratedHtmlPageServlet` answers anonymously
because a published page is meant to be handed around and its unguessable id *is* the capability.
For a screenshot the login is the boundary and the unguessable id is only defence in depth:

| Request | Answer |
|---|---|
| an anonymous session | **401**. An application that lets visitors in gives them a perfectly valid session, and that is not a login |
| no cookie at all | **302** to the start page - `TopLogicServlet`'s one-time cookie probe, which is `private` and `final` in the framework and applies to every route on that base, `/chat/*` included. The request that comes back still without a session is answered **401** |
| signed in, own screenshot | **200** with the image |
| signed in, somebody else's screenshot | **404** |
| signed in, an id that does not exist | **404** - the same answer, so the route is not an oracle for which ids exist |

Neither refusal ever delivers the image, and neither delivers the login page *as* the image: the
302 is a redirect the browser follows to an HTML page, where an `<img>` simply fails to render.

A screenshot captured by an unattended run has no owner and is readable by an administrator only.

`TestScreenshotServlet` pins the four answers; `TestScreenshotService` pins the rule itself.

## The same rule, one layer down

The route is not the only way to a screenshot: `viewDocument` loads any `tl.core:Binary` attribute
of any object into a model's context, addressed by a TID. That path asks the general model-access
layer (`ModelSecurity.canRead` -> the engine's `ModelAccessRights`), which is deny-by-default and
therefore said "administrators only" for a screenshot until somebody configured otherwise. #227
decided what that configuration is, and `tl-ai-core` now ships it in
`WEB-INF/conf/tl-ai-core.binarySecurity.config.xml`:

- a technical role `AiBinaryOwner`, meaning "this object belongs to you";
- two role rules deriving it, per object, from `Screenshot#owner` and `CookiePool#owner` - so an
  object whose owner is empty has no role holder at all, which is the null-owner rule;
- a `<security-config>` granting **Read**, and only Read, to that role on both types.

Administrators and code in a system context bypass the check above all of it, which is where the
"plus administrators" half comes from. So both paths carry the same sentence - **the owner, plus
administrators; nobody but administrators and the system for an owner-less one** - and
`TestPerceivableBinaryReadPolicy` pins it for the model layer, against the shipped configuration
rather than a hand-built policy object.

Two consequences worth knowing:

- An installation that wants a wider rule adds its own role to the grants (or a rule of its own that
  puts `AiBinaryOwner` where it wants it). That widens `viewDocument`; it does not widen
  `/screenshot/{id}`, which asks `ScreenshotService.mayRead` and stays at owner-plus-administrator.
- Write, Delete and Create are not granted to anybody, so they stay with the administrators they
  were already with. What an owner may do with their own screenshot beyond looking at it is a
  question #227 did not answer.

## What the user sees

`browserScreenshot` returns the viewing URL and tells the model, in the tool result, to write it
into the answer as a Markdown image:

```
Saved screenshot "example.com-20260919-104500.png" (48213 bytes) from https://example.com/.
View it at: /screenshot/a7f3b2... - show it to the user by writing exactly this markdown image
into your answer: ![example.com-20260919-104500.png](/screenshot/a7f3b2...).
Screenshot object TID: ...
```

Both rendering paths keep that image, and both are pinned by tests, because an answer is model
output and the filters in front of it are a security boundary (#385):

- the React chat parses with `marked` and filters with `DOMPurify` (`tl-chat/ui/src/markdown.tsx`);
  `.md img` in `styles.css` holds the picture to the width of the column.
- the Java path renders with flexmark and filters with `HtmlSanitizer`, whose allow-list keeps
  `img` with an `http`, `https` or relative `src` and drops a `javascript:` or `data:` one.

## What is not here

There is no table or form over `tl.ai.screenshots:Screenshot`: the admin views are being rebuilt as
React views under issue #452, and the browsing/list view of captured screenshots belongs there.
Screenshots captured before this route existed carry no id and are reachable through the object, not
through a URL; that list view is where they show up.
