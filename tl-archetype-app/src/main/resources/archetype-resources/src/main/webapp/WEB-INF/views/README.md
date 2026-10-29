# Application Views

#if ($moduleType == "fragment")
Place the views (`*.view.xml`) of the React UI here. An application using this fragment links them
from its own views, e.g. from the sidebar items of its `app.view.xml`.
#else
Place the views (`*.view.xml`) of the React UI here. `app.view.xml` extends the application shell
of `tl-layout-view` with a same-path overlay: it names the application and adds the sidebar items
leading to its pages.
#end
