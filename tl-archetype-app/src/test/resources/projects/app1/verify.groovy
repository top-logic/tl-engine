// Checks that the generated project contains exactly the files of the user interface ui=react.
// The archetype integration test generates the project into the folder "project" of the test.
def project = new File(basedir, "project/my-app");
def webinf = new File(project, "src/main/webapp/WEB-INF");
def expected = [
	"views/app.view.xml": true,
	"layouts": false,
];
expected.each { path, present ->
	def file = new File(webinf, path);
	if (file.exists() != present) {
		throw new AssertionError((present ? "Missing " : "Unexpected ") + file);
	}
}
if (!new File(project, ".gitignore").isFile()) {
	throw new AssertionError("Missing .gitignore in " + project);
}
