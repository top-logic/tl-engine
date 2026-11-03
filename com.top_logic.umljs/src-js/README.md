Setup development environment
=============================

Install npm, it is distributed with Node.js. You can download it from [NPM](https://www.npmjs.com/get-npm).

After npm is installed you need to install all dependencies for umljs. Open a shell and navigate to the source directory, here _src-js_.
Execute the following command to install all umljs dependencies which are mentioned in the __package.json__ file:

`npm install`

Now you should see a new directory **node_modules** which contains all necessary (recursive) dependencies. With

`npm run`

you can start a script which is defined in the _scripts_ section of the JSON object in _package.json_.

The Maven build of the module runs `npm install` and `npm run build` (see the `frontend-maven-plugin` in _pom.xml_), so a regular `mvn install` produces the bundles. They are build products and not under version control.

In umljs exists the following scripts:

* _build_ : Builds compact javascript files containing the whole library into _../src/main/webapp/script/umljs_. It creates _four_ files, a development and minified production file for each _Modeler_ and _Viewer_. _Modeler_ extends _Viewer_ with modeling functionality, i.e. adds a palette, resizing, label support, context-pad for shapes, etc. Whereas the _Viewer_ can only display a graph structure.
* _open_ : Opens the _test/test-uml-js.html_ file in your preferred webbrowser.
* _all_ : Executes the _build_ and _open_ script.

The _build_ script also writes the stylesheet _webapp/style/umljs/diagram-js.css_ with the script _tools/js/scope-css.mjs_ of the engine repository: the header _resources/diagram-js-header.css_ followed by _assets/diagram-js.css_ of the installed _diagram-js_, wrapped in an `@scope` rule (see the header for the reasons), so that the stylesheet only applies inside the UML diagrams.
