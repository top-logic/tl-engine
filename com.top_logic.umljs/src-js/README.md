Setup development environment
=============================

Install npm, it is distributed with Node.js. You can download it from [NPM](https://www.npmjs.com/get-npm).

After npm is installed you need to install all dependencies for umljs. Open a shell and navigate to the source directory, here _src-js_.
Execute the following command to install all umljs dependencies which are mentioned in the __package.json__ file:

`npm install`

Now you should see a new directory **node_modules** which contains all necessary (recursive) dependencies. With

`npm run`

you can start a script which is defined in the _scripts_ section of the JSON object in _package.json_.

In umljs exists the following four scripts:

* _all_ : Executes the _build_ and _open_ script.
* _build_ : Builds compact javascript files containing the whole library. It creates _four_ files, a development and minified production file 	for each _Modeler_ and _Viewer_. _Modeler_ extends _Viewer_ with modeling functionality, i.e. adds a palette, resizing, label support, 	context-pad for shapes, etc. Whereas the _Viewer_ can only display a graph structure.
* _open_ : Opens the _test/test-uml-js.html_ file in your preferred webbrowser.
* _pretest_ : Executes a static javascript code analyzer on the code.

Thus, to build and show a minimal example it is sufficient to enter the following in your shell:

`npm run all`

After you created a _new version_ of umljs you should copy the resulting compact javascript files to _webapp/script/umljs_ to update the corresponding top-logic umljs module.