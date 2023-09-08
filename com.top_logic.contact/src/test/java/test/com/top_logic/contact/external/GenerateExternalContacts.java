/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.external;

import java.sql.SQLException;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.contact.external.PlainExternalContact;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;

/**
 * Test case generating {@link ExternalContact}s.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GenerateExternalContacts extends BasicTestCase {
	public static final String[] FAMILY_NAMES = { "Müller", "Schmidt",
			"Schneider", "Fischer", "Meyer", "Weber", "Becker", "Wagner",
			"Schulz", "Herrmann", "Schäfer", "Bauer", "Koch", "Richter",
			"Klein", "Wolf", "Schröder", "Neumann", "Zimmermann", "Krüger",
			"Hoffmann", "Braun", "Schmitz", "Schmitt", "Hartmann", "Lange",
			"Krause", "Schmid", "Werner", "Schwarz", "Meier", "Lehmann",
			"Köhler", "Schulze", "Maier", "Walter", "Huber", "Mayer",
			"Kaiser", "Peters", "Weiss", "Möller", "Peter", "Frank", "König",
			"Sommer", "Stein", "Winter", "Berger", "Hansen", "Jung",
			"Schuster", "Gross", "Fuchs", "Scholz", "Keller", "Friedrich",
			"Lorenz", "Baumann", "Beck", "Schubert", "Lang", "Hahn", "Ludwig",
			"Engel", "Vogel", "Simon", "Roth", "Schreiber", "Bergmann",
			"Kraus", "Schumacher", "Winkler", "Berg", "Otto", "Maus",
			"Heinrich", "Günther", "Miller", "Jansen", "Krämer", "Vogt",
			"Jäger", "Busch", "Hofmann", "Paul", "Wurst", "Petersen", "Böhm",
			"Albrecht", "Janssen", "Graf", "Seidel", "Heinz", "Franke",
			"Schulte", "Brandt", "Hermann", "Thomas", "Arnold", "Sauer",
			"Beyer", "Franz", "Mann", "Bach", "Kuhn", "Kramer", "Klaus",
			"Bayer", "Sander", "Kühn", "Lindner", "Pohl", "Voigt", "Breuer",
			"Zimmer", "Jahn", "Ziegler", "Voss", "Kern", "Haas", "Schumann",
			"May", "Ritter", "Langer", "Bender", "Ernst", "Baum", "Seifert",
			"Rose", "Reuter", "Reinhardt", "Thiel", "Pfeiffer", "Arndt",
			"Steiner", "Walther", "Hübner", "Kaufmann", "Kunz", "Gruber",
			"Nowak", "Lutz", "Horn", "Dietrich", "Kruse", "Bär", "Adam",
			"Fritz", "Lenz", "Kraft", "Michel", "Blum", "Wenzel", "Förster",
			"Meissner", "Anders", "Ebert", "Fiedler", "Karl", "Riedel",
			"Mertens", "Nagel", "Hauser", "Eckert", "Gärtner", "Kessler",
			"Schütz", "Kurz", "Wilhelm", "Fröhlich", "Schilling", "Stark",
			"Thiele", "Muster", "Herzog", "Schön", "Hoppe", "Stern", "Grimm",
			"Maurer", "Barth", "Haase", "Bock", "Wilke", "Hesse", "Schenk",
			"Schindler", "Marx", "Ott", "Götz", "Smith", "Hein", "Kunze",
			"Brand", "Witt", "Geiger", "Frey", "Scherer", "Kirchner",
			"Böttcher", "Schrader", "Stahl", "Gerlach", "Freund", "Hirsch",
			"Moser", "Mohr", "Wolter", "Stephan", "Wegner", "Büttner", "Hess",
			"Beckmann", "Schwab", "Yilmaz", "Bachmann", "Hammer", "Schröter",
			"Meister", "Kohl", "Hagen", "Heinze", "Fink", "Behrens", "Böhme",
			"Wendt", "Döring", "Steffen", "Heller", "Menzel", "Reich",
			"Schramm", "Hase", "Rieger", "Beier", "Conrad", "Göbel", "Mai",
			"Schreiner", "Henning", "Klose", "Blume", "Grossmann", "Seitz",
			"Wirth", "Kremer", "Baier", "Held", "Reiter", "Schlüter",
			"Strauss", "Schultz", "Gebhardt", "Krebs", "Lustig", "John",
			"Kopp", "Wild", "Schiller", "Esser", "Siebert", "Jones", "Holz",
			"Martens", "Freitag", "Albert", "Unger", "Körner", "Heine",
			"Noack", "Brinkmann", "Sturm", "Hinz", "Reichert", "Kolb",
			"Ulrich", "Naumann", "Linke", "Burger", "Rothe", "Münch", "Urban",
			"Hanke", "Hennig", "Witte", "Schlegel", "Erdmann", "Hamann",
			"Link", "Will", "Brunner", "Bruns", "Lopez", "Reimann", "Kühne",
			"Buchholz", "Bischoff", "Köster", "Engelhardt", "Sonnenschein",
			"Decker", "Dietz", "Horst", "Zander", "Mayr", "Rudolph", "Ahrens",
			"Ackermann", "Konrad", "Hummel", "Hofer", "Nickel", "Falk",
			"Fricke", "Lee", "Ullrich", "Westphal", "Rauch", "Kirsch", "Blank",
			"Röder", "Pan", "Michael", "Kiefer", "Moritz", "Haupt", "Lux",
			"Albers", "Henkel", "Eder", "Jakob", "Keil", "Preuss", "Rohde",
			"Scharf", "Brückner", "Haag", "Moll", "Lemke", "Krauss",
			"Lindemann", "Nolte", "Bertram", "Rausch", "Geissler", "Koller",
			"Neubauer", "Jürgens", "Wegener", "Metzger", "Giese", "Berndt",
			"Stumpf", "Muller", "Bartsch", "Binder", "Stock", "Brenner",
			"Burkhardt", "Herbst", "Krieger", "Adler", "Heinemann", "Römer",
			"Löffler", "Schüler", "Renner", "Fleischer", "Specht", "Hartung",
			"Frei", "Noll", "Rupp", "Benz", "Hildebrandt", "Fritzsche",
			"Völker", "Krug", "Schade", "Jacob", "Vetter", "Fuhrmann", "Born",
			"Beer", "Hecht", "Christ", "Rodriguez", "Meiser", "Merz", "Probst",
			"Sorglos", "Hausmann", "Bremer", "Sonntag", "Lohmann", "Philipp",
			"Wiese", "Brauer", "Feldmann", "Heise", "Kluge", "Block", "Geier",
			"Bode", "Michels", "Betz", "Endres", "Funk", "Singer", "Hafner",
			"Kroll", "Maass", "Fritsch", "Schaller", "Heuer", "Weller",
			"Seiler", "Korn", "Stoll", "Wahl", "Rau", "Ledig", "Max", "Baur",
			"Bader", "Lauer", "Schöne", "Stefan", "Runge", "Raab", "Alex",
			"Wimmer", "Kunkel", "Weise", "Vollmer", "Jacobs", "Prinz", "Abel",
			"Dörr", "Pape", "Scholl", "Kretschmer", "Bürger", "Weiler",
			"Neugebauer", "Ross", "Thomsen", "Elsner", "Altmann", "Glaser",
			"Radtke", "Metz", "Borchert", "Barthel", "Michaelis", "Sahin",
			"Kellner", "Jordan", "Demir", "Baumgartner", "Eckhardt", "Auer",
			"Dreyer", "Tom", "Schnell", "Zeller", "Reiss", "Dittmann",
			"Stadler", "Eichler", "Lau", "Wichmann", "Clemens", "Hentschel",
			"Mack", "Hoyer", "Stange", "Bartels", "Neu", "Busse", "Langner",
			"Neubert", "Kohler", "Reimer", "Börner", "Hartwig", "Wiegand",
			"Gonzalez", "Diehl", "Niemann", "Bernhardt", "Kara", "Henke",
			"Steinbach", "Riedl", "Markus", "Hering", "Lück", "Paulus",
			"Ries", "Grosse", "Klotz", "Engelmann", "Schweizer", "Lechner",
			"Timm", "Harms", "Knorr", "Winkelmann", "Wittmann", "Haller",
			"Dittrich", "Drews", "Hauck", "Reis", "Funke", "Heck", "Brown",
			"Weiland", "Schmidtke", "Schuler", "Melzer", "Gehrke", "Eggert",
			"Gerber", "Duck", "Sieber", "Pfeffer", "Dieter", "Haus",
			"Johannsen", "Jost", "Scheffler", "Popp", "Junker", "Kempf",
			"Faber", "Reichel", "Biermann", "Finke", "Hager", "Herz", "Grün",
			"Hensel", "Angel", "Starke", "Kuhlmann", "Jensen", "Balzer",
			"Schüssler", "Buchmann", "Oswald", "Böhmer", "Wulf", "Kress",
			"Pilz", "Walz", "Bittner", "Drescher", "Bahr", "Eberhardt",
			"Heilmann", "Pieper", "Seidl", "Weidner", "Lol", "Fernandez",
			"Helbig", "Lehner", "Kühl", "Wirtz", "Daniel", "Rabe", "Hinrichs",
			"Schütte", "Gottschalk", "Dampf", "Gläser", "Augustin",
			"Kowalski", "Schürmann", "Rudolf", "Pietsch", "Kaya",
			"Rademacher", "Junge", "Sattler", "Knoll", "Süss", "Herold",
			"Bäcker", "Martinez", "Heil", "Opitz", "Schirmer", "Merk",
			"Stöhr", "Stenzel", "Rapp", "Forster", "Kaminski", "Eichhorn",
			"Sperling", "Dorn", "Simpson", "Hass", "Bühler", "Man",
			"Schultze", "Rath", "Damm", "Sauter", "Heinen", "Celik",
			"Oppermann", "Kröger", "Berner", "Schaaf", "Grau", "Burmeister",
			"Knopf", "Wiedemann", "Reinhold", "Thieme", "Brandl", "Koslowski",
			"Jeske", "Weigel", "Heuser", "Straub", "Kilian", "Mair", "Ochs",
			"Ruf", "Mauer", "Wiesner", "Kahl", "Walker", "Behr", "Küster",
			"Klar", "Steinert", "Brendel", "Hill", "Stolz", "Böttger",
			"Neuhaus", "Springer", "Schober", "Seeger", "Gerdes", "Blau",
			"Brüggemann", "Grube", "Hohmann", "Dreher", "Wilde", "Uhlig",
			"Bastian", "Fechner", "Janzen", "Marquardt", "Petzold", "Häusler",
			"Enders", "Hagemann", "Faust", "Kummer", "Ehlers", "Mende",
			"Siegel", "Bohn", "Heim", "Rössler", "Alt", "Hellmann", "Paulsen",
			"Höhne", "Garcia", "Mader", "Brockmann", "Jahnke", "Harder",
			"Götze", "Geisler", "Reimers", "Tester", "Kasper", "Sanchez",
			"Schmieder", "Rost", "Küpper", "Fox", "Mahler", "Körber",
			"Gerhardt", "Höfer", "Marks", "Ehrhardt", "Heinrichs", "Buck",
			"Schönfeld", "Hiller", "Glück", "Schlosser", "Henschel", "Klemm",
			"Westermann", "Rüdiger", "Eggers", "Weimer", "Meurer", "Adams",
			"Buch", "Leonhardt", "Hecker", "Rösch", "Maas", "Andreas",
			"Sanders", "Abraham", "Frings", "Dogan", "Lippert", "Wehner",
			"Bosch", "Löwe", "Perez", "Kästner", "Tillmann", "Liebig",
			"Evers", "Steiger", "Hille", "Schiffer", "Bucher", "Schatz",
			"Lukas", "Ruppert", "David", "Wieczorek", "Andres", "Wittig",
			"Loos", "Kleine", "Otte", "Sommerfeld", "Gerhard", "Ullmann",
			"Klinger", "Wüst", "Doll", "Köhn", "Ebel", "Kunert", "Buss",
			"Schütze", "Neuber", "Loch", "Hock", "Schwartz", "Wille",
			"Schweiger", "Wessel", "Andre", "Fleischmann", "Rosenberg",
			"Pfaff", "Büchner", "Brahmst", "Matthes", "Lauterbach", "Rehm",
			"Strauch", "Frenzel", "Dressler", "Oeztürk", "Helm", "Wilms",
			"Sievers", "Reichelt", "Heckmann", "Haug", "Späth", "Kemper",
			"Jacobi", "Schuhmann", "Friedrichs", "Reiner", "Andresen", "Kling",
			"Frisch", "Hamm", "Habermann", "Reitz", "Heider", "Schüller",
			"Reinke", "Schott", "Holl", "Renz", "Kurth", "Salzmann", "Kugler",
			"Heimann", "Ali", "Schreier", "Kersten", "Grund", "Kleinert",
			"Rösler", "Baumeister", "Klee", "Spengler", "Geyer", "Kolbe",
			"Trautmann", "Stamm", "Holzer", "Brück", "Bothe", "Gebauer",
			"Mangold", "Hoch", "Boos", "Alexander", "Hopf", "Buschmann",
			"Zahn", "Erler", "Schell", "Bloch", "Waldmann", "Steinmann",
			"Klatt", "Rossi", "Trapp", "Schwenk", "Taylor", "Hellwig", "Krone",
			"Just", "Brehm", "Appel", "Schleicher", "Groth", "Christian",
			"Rother", "Hildebrand", "Teichmann", "Weil", "Pütz", "Schmit",
			"Fürst", "Lohse", "Gast", "Mühlbauer", "Wolters", "Heitmann",
			"Jeschke", "Heyer", "Steinke", "Obermeier", "Kurt", "Rohr",
			"Münster", "Ewald", "Schick", "Bernhard", "Kreuzer", "Senf",
			"Schweitzer", "Lohr", "Apel", "Zabel", "Zielke", "Marschall",
			"Reinhard", "Anton", "Cordes", "Cramer", "Buchner", "Bell",
			"Feller", "Wedel", "Horstmann", "Morgenstern", "Stangl", "Otten",
			"Jäckel", "Fassbender", "Hoff", "Ramm", "Brauner", "Scheel",
			"Ewert", "Hanisch", "Niemeyer", "Fries", "Grundmann", "Kratz",
			"Preis", "Johnson", "Becher", "Drechsler", "Volk", "Pohlmann",
			"Bruhn", "Messner", "Fritsche", "Rahn", "Stiller", "Hilbert",
			"Schnabel", "Manz", "Knopp", "Heiss", "Höhn", "Meinhardt",
			"Rieck", "Dengler", "Löhr", "Orth", "Strasser", "Oezdemir",
			"Riess", "Theis", "Friese", "Schwarze", "Veith", "Höppner",
			"Backhaus", "Mark", "Bernd", "Gehring", "Dohrmann", "März",
			"Knapp", "Wilken", "Kühnel", "Claus", "Zink", "Kretzschmar",
			"Chris", "Grote", "Hornung", "Aslan", "Dräger", "Wieland",
			"Maler", "Ebner", "Hoffmeister" };

	public static final String[] FIRST_NAMES = { "Anna", "Leonie", "Leoni",
			"Lea", "Leah", "Lena", "Hanna", "Hannah", "Laura", "Lara", "Emily",
			"Emilie", "Sara", "Sarah", "Lilli", "Lilly", "Lili", "Nele",
			"Neele", "Emma", "Mia", "Julia", "Sophie", "Sofie", "Alina",
			"Amelie", "Amely", "Marie", "Lisa", "Louisa", "Luisa", "Maja",
			"Maya", "Johanna", "Lina", "Sofia", "Sophia", "Chiara", "Kiara",
			"Angelina", "Paula", "Leni", "Jule", "Annika", "Annica", "Anika",
			"Charlotte", "Michelle", "Jana", "Vanessa", "Katharina",
			"Catharina", "Clara", "Klara", "Jasmin", "Yasmin", "Emilia",
			"Nina", "Pia", "Marlene", "Marleen", "Marlen", "Emelie", "Emely",
			"Josefine", "Josephine", "Zoe", "Antonia", "Lucie", "Lucy",
			"Celina", "Franziska", "Melina", "Maria", "Sina", "Selina",
			"Pauline", "Finja", "Finnja", "Victoria", "Viktoria", "Melissa",
			"Vivien", "Vivienne", "Fiona", "Isabel", "Isabell", "Isabelle",
			"Eileen", "Aileen", "Ayleen", "Jette", "Amy", "Carolin",
			"Caroline", "Karoline", "Anne", "Greta", "Eva", "Ida", "Natalie",
			"Nathalie", "Merle", "Elena", "Elisa", "Celine", "Fabienne", "Kim",
			"Stella", "Luise", "Louise", "Jolina", "Joelina", "Larissa",
			"Theresa", "Teresa", "Helena", "Mara", "Marah", "Samira", "Carla",
			"Karla", "Miriam", "Annabelle", "Annabell", "Annabel", "Annalena",
			"Milena", "Alicia", "Leticia", "Letizia", "Tabea", "Alexandra",
			"Martha", "Marta", "Elisabeth", "Jessica", "Maike", "Meike",
			"Ella", "Linn", "Lynn", "Kimberly", "Kimberley", "Lia", "Liah",
			"Ronja", "Saskia", "Christina", "Dana", "Magdalena", "Jennifer",
			"Luna", "Matilda", "Mathilda", "Melanie", "Carlotta", "Karlotta",
			"Mariella", "Julie", "Rebecca", "Rebekka", "Carina", "Karina",
			"Frida", "Frieda", "Paulina", "Lana", "Chantal", "Kira", "Kyra",
			"Mira", "Joline", "Joeline", "Nora", "Svenja", "Carolina", "Lenja",
			"Lenya", "Nicole", "Denise", "Isabella", "Josy", "Josie", "Lotta",
			"Svea", "Gina", "Janina", "Linda", "Samantha", "Ann", "Helen",
			"Jenny", "Helene", "Lucia", "Cheyenne", "Chayenne", "Noemi",
			"Xenia", "Olivia", "Alexa", "Patricia", "Patrizia", "Felicitas",
			"Anastasia", "Ellen", "Sonja", "Fenja", "Liv", "Aaliyah", "Romy",
			"Lorena", "Tamara", "Valentina", "Annemarie", "Tessa", "Henriette",
			"Mona", "Joyce", "Luana", "Nelly", "Sidney", "Sydney", "Jasmina",
			"Veronika", "Anouk", "Diana", "Luca", "Luka", "Enya", "Lilian",
			"Marina", "Tara", "Alessia", "Natalia", "Ashley", "Valerie",
			"Leandra", "Cora", "Rosalie", "Selma", "Alisha", "Joanna", "Anja",
			"Evelyn", "Evelin", "Eveline", "Jonna", "Franka", "Joana",
			"Christin", "Kristin", "Judith", "Lene", "Stefanie", "Stephanie",
			"Imke", "Salome", "Ina", "Lavinia", "Maren", "Linnea", "Lotte",
			"Rieke", "Lukas", "Lucas", "Leon", "Luca", "Luka", "Tim", "Timm",
			"Paul", "Jonas", "Finn", "Fynn", "Max", "Niclas", "Niklas", "Luis",
			"Louis", "Felix", "Maximilian", "Philipp", "Philip", "Phillip",
			"Elias", "Jan", "Julian", "Jannick", "Jannik", "Yannic", "Yannick",
			"Yannik", "Noah", "Simon", "Moritz", "Tom", "Ben", "Fabian",
			"David", "Jakob", "Jacob", "Nils", "Niels", "Florian", "Erik",
			"Eric", "Nico", "Niko", "Alexander", "Justin", "Tobias", "Marvin",
			"Marwin", "Kevin", "Lennard", "Lennart", "Johannes", "Linus",
			"Sebastian", "Julius", "Daniel", "Marc", "Mark", "Benjamin",
			"Joel", "Maurice", "Rafael", "Raphael", "Colin", "Collin",
			"Pascal", "Timo", "Nick", "Robin", "Dominic", "Dominik", "Jannis",
			"Yannis", "Janis", "Hannes", "Marlon", "Jason", "Henri", "Henry",
			"Samuel", "Vincent", "Adrian", "Jonathan", "Marcel", "Lars",
			"Lasse", "Anton", "Matthis", "Mattis", "Mathis", "Matis",
			"Michael", "Bastian", "Dennis", "Denis", "Constantin",
			"Konstantin", "Leonard", "Kilian", "Leo", "Malte", "Justus",
			"Maik", "Meik", "Mike", "Ole", "Aaron", "Christian", "Kristian",
			"Mika", "Jeremy", "Jeremie", "Oskar", "Oscar", "Carl", "Karl",
			"Joshua", "Jona", "Jonah", "Phil", "Silas", "Till", "Tyler",
			"Tayler", "Benedikt", "Cedric", "Cedrik", "Jamie", "Jannes",
			"Valentin", "Emil", "Marco", "Marko", "Fabio", "Johann", "Julien",
			"Thomas", "Tomas", "Franz", "Noel", "John", "Luke", "Luc", "Luk",
			"Oliver", "Richard", "Bennet", "Bennett", "Marius", "Maxim",
			"Christoph", "Levin", "Marcus", "Markus", "Nicolas", "Nikolas",
			"Torben", "Thorben", "Gabriel", "Robert", "Andreas", "Henrik",
			"Henrick", "Matthias", "Mattias", "Conner", "Connor", "Patrick",
			"Frederic", "Frederik", "Frederick", "Liam", "Tristan", "Kai",
			"Kay", "Lenny", "Theo", "Dustin", "Laurens", "Laurenz", "Stefan",
			"Stephan", "Arne", "Michel", "Nevio", "René", "Christopher",
			"Sven", "Swen", "Manuel", "Sören", "Finley", "Magnus", "Kjell",
			"Mats", "Laurin", "Ian", "Matti", "Jean", "Lorenz", "Konrad",
			"Lennox", "Martin", "Timon", "Enrico", "Bruno", "Domenic",
			"Domenik", "Steven", "Clemens", "Klemens", "Milan", "Hendrik",
			"Leonardo", "Quentin", "Matteo", "Anthony", "Friedrich", "Leif",
			"Mirco", "Mirko", "Roman", "Janne", "Sandro", "Artur", "Arthur",
			"Leander", "Bela", "Georg", "Hugo", "Melvin", "Leandro", "Adam",
			"Ferdinand", "Chris", "Len", "Lenn", "Mario", "Ruben", "Miguel",
			"Peter", "Sean", "Deniz", "Danny", "Dean", "Mustafa", "Ali",
			"Toni", "Marten", "Devin", "Andre", "Rasmus", "Brian", "Bryan",
			"Damian", "Keno", "Leopold", "Jordan", "Levi", "Ryan" };
	
	public static void createRandomContacts(int count) throws SQLException {
		Random rnd = new Random(42);
		for (int n = 0; n < count; n++) {
			String number = Integer.toString(n);
			String uNumber = "u" + "00000".substring(number.length()) + number;
		
			ExternalContact contact = newRandomContact(rnd, uNumber);
			ExternalContacts.newContact(contact);
		}
	}

	public static ExternalContact newRandomContact(Random rnd, String uNumber) {
		String firstName = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
		String familyName = FAMILY_NAMES[((int) (Math.min(1.0, Math.abs(rnd.nextGaussian() / 3.0)) * (FAMILY_NAMES.length - 1)))];
		String division = "" + ((char) ('A' + rnd.nextInt(26))) + ((char) ('A' + rnd.nextInt(26))) + ((char) ('A' + rnd.nextInt(26)));
		String eMail = ("" + firstName.charAt(0) + '.' + familyName + '@' + division + '.' + "company.com").toLowerCase();
		String phone = "" + ((char) ('1' + rnd.nextInt(9))) + ((char) ('0' + rnd.nextInt(10))) + ((char) ('0' + rnd.nextInt(10))) + ((char) ('0' + rnd.nextInt(10))) + ((char) ('0' + rnd.nextInt(10)));
			
		return new PlainExternalContact(uNumber, firstName, familyName, division, eMail, phone, "test");
	}

    public static Test suite () {
		return KBSetup.getSingleKBTest(GenerateExternalContacts.class);
    }
    
    public void testGenerate() throws SQLException {
    	createRandomContacts(80000);
    	
		KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().commit();
    }
	
}
