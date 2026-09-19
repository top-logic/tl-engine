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

Issue #227 is the open question of a general read rule for perceivable binaries. When it is
answered, `ScreenshotService.mayRead` is the one method to change - every path asks it.

`TestScreenshotServlet` pins the four answers; `TestScreenshotService` pins the rule itself.

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
