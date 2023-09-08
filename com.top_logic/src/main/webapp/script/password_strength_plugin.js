/* @projectDescription jQuery Password Strength Plugin - A jQuery plugin to provide accessibility functions
 * @author Tane Piper (digitalspaghetti@gmail.com)
 * @version 2.0
 * @website: http://digitalspaghetti.me.uk/digitalspaghetti
 * @license MIT License: http://www.opensource.org/licenses/mit-license.php
 * 
 * === Changelog ===
 * Version 2.1.1 (07/10/2013)
 * Changed UI
 * 
 * Version 2.1 (18/05/2008)
 * Added a jQuery method to add a new rule: jQuery('input[@type=password]').pstrength.addRule(name, method, score, active)
 * Added a jQuery method to change a rule score: jQuery('input[@type=password]').pstrength.changeScore('one_number', 50);
 * Added a jQuery method to change a rules active state: jQuery('input[@type=password]').pstrength.ruleActive('one_number', false);
 * Hide the 'password to short' span if the password is more than the min chars
 * 
 * Version 2.0 (17/05/2008)
 * Completly re-wrote the plugin from scratch.  Plugin now features lamda functions for validation and
 * custom validation rules 
 * Plugin now exists in new digitalspaghetti. namespace to stop any conflits with other plugins.
 * Updated documentation
 * 
 * Version 1.4 (12/02/2008)
 * Added some improvments to i18n stuff from Raffael Luthiger.
 * Version 1.3 (02/01/2008)
 * Changing coding style to more OO
 * Added default messages object for i18n
 * Changed password length score to Math.pow (thanks to Keith Mashinter for this suggestion)
 * Version 1.2 (03/09/2007)
 * Added more options for colors and common words
 * Added common words checked to see if words like 'password' or 'qwerty' are being entered
 * Added minimum characters required for password
 * Re-worked scoring system to give better results
 * Version 1.1 (20/08/2007)
 * Changed code to be more jQuery-like
 * Version 1.0 (20/07/2007)
 * Initial version.
 */

// Create our namespaced object
/*global window */
/*global jQuery */
/*global digitalspaghetti*/
window.digitalspaghetti = window.digitalspaghetti || {};
digitalspaghetti.password = {	
	'defaults' : {
		'passwordFieldId': "passwordField",
		'tlCriteriaMinCount': 0,
		'minChar': 0,
		'colors': ["#f00", "#f60", "#ffd400", "#3c0", "#3f0"],
		'scores': [20, 30, 43, 50],
		'verdicts':	['Weak', 'Normal', 'Medium', 'Strong', 'Very Strong'],
		'raisePower': 1.4,
		'debug': false
	},
	'ruleScores' : {
		'length': 0,
		'matchTLCriteriaCount': 0,
		'lowercase': 1,
		'uppercase': 3,
		'one_number': 3,
		'three_numbers': 5,
		'one_special_char': 3,
		'two_special_char': 5,
		'upper_lower_combo': 2,
		'letter_number_combo': 2,
		'letter_number_char_combo': 2
	},
	'rules' : {
		'matchTLCriteriaCount': true,
		'length': true,
		'lowercase': true,
		'uppercase': true,
		'one_number': true,
		'three_numbers': true,
		'one_special_char': true,
		'two_special_char': true,
		'upper_lower_combo': true,
		'letter_number_combo': true,
		'letter_number_char_combo': true
	},
	'validationRules': {
		'matchTLCriteriaCount': function(word, score) {
			var acceptingWordRules = 0;
			var validationRules = digitalspaghetti.password.validationRules;
			acceptingWordRules += validationRules.lowercase(word, 1) ? 1 : 0;
			acceptingWordRules += validationRules.uppercase(word, 1) ? 1 : 0;
			acceptingWordRules += validationRules.one_number(word, 1) ? 1 : 0;
			acceptingWordRules += validationRules.one_special_char(word, 1) ? 1 : 0;
			
			if(acceptingWordRules < digitalspaghetti.password.options.tlCriteriaMinCount) {
				return Number.NEGATIVE_INFINITY;
			} else {
				return score;
			}
			
		},
		'length': function (word, score) {
			var wordlen = word.length;
			if (wordlen < digitalspaghetti.password.options.minChar) {
				return Number.NEGATIVE_INFINITY;
			} else {
				return Math.pow(wordlen, digitalspaghetti.password.options.raisePower);
			}
		},		
		'lowercase': function (word, score) {
			return word.match(/[a-z]/) && score;
		},
		'uppercase': function (word, score) {
			return word.match(/[A-Z]/) && score;
		},
		'one_number': function (word, score) {
			return word.match(/\d+/) && score;
		},
		'three_numbers': function (word, score) {
			return word.match(/(.*[0-9].*[0-9].*[0-9])/) && score;
		},
		'one_special_char': function (word, score) {
			return word.match(/.[!,@,#,$,%,\^,&,*,?,_,~]/) && score;
		},
		'two_special_char': function (word, score) {
			return word.match(/(.*[!,@,#,$,%,\^,&,*,?,_,~].*[!,@,#,$,%,\^,&,*,?,_,~])/) && score;
		},
		'upper_lower_combo': function (word, score) {
			return word.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/) && score;
		},
		'letter_number_combo': function (word, score) {
			return word.match(/([a-zA-Z])/) && word.match(/([0-9])/) && score;
		},
		'letter_number_char_combo' : function (word, score) {
			return word.match(/([a-zA-Z0-9].*[!,@,#,$,%,\^,&,*,?,_,~])|([!,@,#,$,%,\^,&,*,?,_,~].*[a-zA-Z0-9])/) && score;
		}
	},
	'attachWidget': function (element) {
		digitalspaghetti.password.strengthBar = digitalspaghetti.password.options.passwordFieldId  + "_password-strength-bar";
		digitalspaghetti.password.strengthText = digitalspaghetti.password.options.passwordFieldId  + "_password-strength-text";
		var offset = 1; //px
		var barFrameBorderWidth = 1; //px
		var barFrameWidth = jQuery(element).outerWidth(true) - 2 * barFrameBorderWidth - offset;
		var output = ['<div>'];
		output.push('<div class="password-strength-frame" style="width: ' + barFrameWidth + 'px; border-width: ' + barFrameBorderWidth + 'px;">');
		output.push('<div id="' + digitalspaghetti.password.strengthBar + '"></div>');
		output.push('</div>');
		output.push('<div id="' + digitalspaghetti.password.strengthText + '"></div>');
		output.push('</div>');
		output = output.join('');
		jQuery(element).after(output);
	},
	'debugOutput': function (element) {
		if (typeof console.log === 'function') {
			console.log(digitalspaghetti.password);	
		} else {
			alert(digitalspaghetti.password);
		}
	},
	'addRule': function (name, method, score, active) {
		digitalspaghetti.password.rules[name] = active;
		digitalspaghetti.password.ruleScores[name] = score;
		digitalspaghetti.password.validationRules[name] = method;
		return true;
	},
	'init': function (element, options) {
		digitalspaghetti.password.options = jQuery.extend({}, digitalspaghetti.password.defaults, options);
		digitalspaghetti.password.attachWidget(element);
		jQuery(element).keyup(function () {
			digitalspaghetti.password.calculateScore(jQuery(this).val());
		});
		if (digitalspaghetti.password.options.debug) {
			digitalspaghetti.password.debugOutput();
		}
		
		digitalspaghetti.password.calculateScore(jQuery(element).val());
	},
	'calculateScore': function (word) {
		digitalspaghetti.password.totalscore = 0;
		digitalspaghetti.password.width = 0;
		for (var key in digitalspaghetti.password.validationRules) if (digitalspaghetti.password.rules.hasOwnProperty(key)) {
			if (digitalspaghetti.password.rules[key] === true) {
				var score = digitalspaghetti.password.ruleScores[key];
				var result = digitalspaghetti.password.validationRules[key](word, score);
				if (result) {
					digitalspaghetti.password.totalscore += result;
				}
			}
			if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[0] / 2) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[0];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[0];
				digitalspaghetti.password.width =  "3";
			} else if (digitalspaghetti.password.totalscore < digitalspaghetti.password.options.scores[0]) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[1];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[1];
				digitalspaghetti.password.width =  "13";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[1] / 2) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[1];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[1];
				digitalspaghetti.password.width =  "25";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[1]) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[1];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[1];
				digitalspaghetti.password.width =  "37";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[2] / 2) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[2];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[2];
				digitalspaghetti.password.width =  "50";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[2]) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[2];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[2];
				digitalspaghetti.password.width =  "62";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[3] / 2) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[3];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[3];
				digitalspaghetti.password.width =  "75";
			} else if (digitalspaghetti.password.totalscore <= digitalspaghetti.password.options.scores[3]) {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[3];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[3];
				digitalspaghetti.password.width =  "87";
			} else {
				digitalspaghetti.password.strColor = digitalspaghetti.password.options.colors[4];
				digitalspaghetti.password.strText = digitalspaghetti.password.options.verdicts[4];
				digitalspaghetti.password.width =  "100";
			}
			
			jQuery("#" + digitalspaghetti.password.strengthBar).css({'display': 'block', 'background-color': digitalspaghetti.password.strColor, 'height' : '4px', 'width': digitalspaghetti.password.width + "%"});
			jQuery("#" + digitalspaghetti.password.strengthText).text(digitalspaghetti.password.strText);
		}
	}
};

jQuery.extend(jQuery.fn, {
	'pstrength': function (options) {
		var self = this;
		return this.each(function () {
			digitalspaghetti.password.init(self, options);
		});
	}
});
jQuery.extend(jQuery.fn.pstrength, {
	'addRule': function (name, method, score, active) {
		digitalspaghetti.password.addRule(name, method, score, active);
		return true;
	},
	'changeScore': function (rule, score) {
		digitalspaghetti.password.ruleScores[rule] = score;
		return true;
	},
	'ruleActive': function (rule, active) {
		digitalspaghetti.password.rules[rule] = active;
		return true;
	}
});