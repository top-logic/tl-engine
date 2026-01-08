const nunjucks = require("nunjucks");
const fs = require("fs");
const path = require("path");

const templatesDir = "../templates"
const outputFile = "../../src/main/java/com/top_logic/build/doclet/templates.js";

// Zielverzeichnis sicherstellen
fs.mkdirSync(path.dirname(outputFile), { recursive: true });

// Templates kompilieren
const compiled = nunjucks.precompile(templatesDir, {
  include: [
  	"class-template.html",
  	"common-template.html",
  	"index-template.html",
  	"package-template.html",
  	"ref-template.html",
  	"search-template.html"],
  name: (filename) => path.basename(filename)
});

// Ergebnis schreiben
fs.writeFileSync(outputFile, compiled);

console.log("Nunjucks templates compiled to", outputFile);