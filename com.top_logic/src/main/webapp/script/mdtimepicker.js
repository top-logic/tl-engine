/* -- DO NOT REMOVE --
 * jQuery MDTimePicker v1.0 plugin
 * 
 * Author: Dionlee Uy
 * Email: dionleeuy@gmail.com
 *
 * Date: Tuesday, August 28 2017
 *
 * @requires jQuery
 * -- DO NOT REMOVE -- */
 if (typeof jQuery === 'undefined') { throw new Error('MDTimePicker: This plugin requires jQuery'); }
+function ($) {
	var MDTP_DATA = "mdtimepicker", HOUR_START_DEG = 120, MIN_START_DEG = 90, END_DEG = 360, HOUR_DEG_INCR = 30, MIN_DEG_INCR = 6;

	var Time = function (hour, minute) {
		this.hour = hour;
		this.minute = minute;
		
		this.format = function(format, is12Hour) {
			var that = this;
						
			return $.trim(format.replace(/(hh|h|HH|H|mm|ss|a)/g, function (e) {
				var hour = that.getHour(is12Hour);
				switch(e.toLowerCase()){
					case 'H': return hour;
					case 'HH': return (hour < 10 ? '0' + parseInt(hour) : parseInt(hour));
					case 'h': return hour;
					case 'hh': return (hour < 10 ? '0' + parseInt(hour) : parseInt(hour));
					case 'mm': return (parseInt(that.minute) < 10 ? '0' + parseInt(that.minute) : parseInt(that.minute));
					case 'ss': return '00';
					case 'a': return is12Hour ? that.getT() : '';
				}
			}));
		};

		this.setHour = function (value) { this.hour = value; };
		this.getHour = function (is12Hour) { return is12Hour ? parseInt(this.hour) === 0 || parseInt(this.hour) === 12 ? 12 : (this.hour % 12) : this.hour; };			
		this.invert = function () {
			if (this.getT() === 'AM') this.setHour(this.getHour() + 12);
			else this.setHour(this.getHour() - 12);
		};
		this.setMinutes = function (value) { this.minute = value; }
		this.getMinutes = function (value) { return this.minute; }
		this.getT = function() { 
			return this.hour < 12 ? 'AM' : 'PM';
		};	
	};

	var MDTimePicker = function (caller, config) {
		var that = this;

		this.visible = false;
		this.activeView = 'hours';
		this.hTimeout = null;
		this.mTimeout = null;
		this.config = config;
		this.input = $(document.getElementById(this.config.inputID));
		this.is12Hour = (((this.config.format).match(/h|hh/g) || []).length > 0);
		this.hourPadding = (((this.config.format).match(/HH|hh/g) || []).length > 0);
		this.time = new Time(0, 0);
		this.selected = new Time(0, 0);		
		this.timepicker = {
			overlay : $('<div class="mdtimepicker" id="'+ this.config.controlID +'-mdtp__overlay"></div>'),
			wrapper : $('<div class="mdtp__wrapper" id="'+ this.config.controlID +'-mdtp__wrapper" style=""></div>'),
			timeHolder : {
				wrapper: $('<section class="mdtp__time_holder"></section>'),
				am: $('<span class="mdtp__am fas fa-sun" alt="AM" data-tooltip="AM" title=""></span>'),
				pm: $('<span class="mdtp__pm fas fa-moon" alt="PM" data-tooltip="PM" title=""></span>'),
				hour: $('<span class="mdtp__time_h">12</span>'),
				dots: $('<span class="mdtp__timedots">:</span>'),
				minute: $('<span class="mdtp__time_m">00</span>'),
				am_pm: $('<span class="mdtp__ampm"></span>')
			},
			clockHolder : {
				wrapper: $('<section class="mdtp__clock_holder"></section>'),
				clock: {
					wrapper: $('<div class="mdtp__clock"></div>'),
					dot: $('<span class="mdtp__clock_dot"></span>'),
					hours: $('<div class="mdtp__hour_holder"></div>'),
					minutes: $('<div class="mdtp__minute_holder"></div>')
				}
			}
		};

		var picker = that.timepicker;

		that.setup(picker).appendTo(services.form.BrowserWindowControl.getPopupDialogAnchor());
		services.form.BrowserWindowControl.showPopupPane();
		
		if(this.is12Hour) {
			picker.timeHolder.am.click(function () { if (that.selected.getT() !== 'AM') that.setT('am'); });
			picker.timeHolder.pm.click(function () { if (that.selected.getT() !== 'PM') that.setT('pm'); });
			picker.timeHolder.hour.click(function () { if (that.activeView !== 'hours') that.switchView('hours'); });
			picker.timeHolder.minute.click(function () { if (that.activeView !== 'minutes') that.switchView('minutes'); });
		}
	
		that.resetSelected();
		that.switchView(that.activeView);
	};

	MDTimePicker.prototype = {
		constructor : MDTimePicker,

		setup : function (picker) {
			if (typeof picker === 'undefined') throw new Error('Expecting a value.');

			var that = this, overlay = picker.overlay, wrapper = picker.wrapper,
				clock = picker.clockHolder, header = picker.timeHolder;


			// Setup hours
			for (var i = 0; i < 12; i++) {
				var value = i + 1, deg = (HOUR_START_DEG + (i * HOUR_DEG_INCR)) % END_DEG;
				
				if(this.is12Hour) {						
					var hour = $('<div class="mdtp__digit rotate-' + deg + '" data-hour="' + value + '"><span class="hour">'+ value +'</span></div>');
					hour.find('span').click(function () {
						var _data = parseInt($(this).parent().data('hour')),
							_selectedT = that.selected.getT(),
							_value = (_data + ((_selectedT === 'PM' && _data < 12) || (_selectedT === 'AM' && _data === 12) ? 12 : 0)) % 24;

						that.setHour(_value);
						that.switchView('minutes');
					});					
				} else {	
					var value24 = value === 12 ? '0' : (value+12),
					valueVisable24 = value === 12 ? '00' : (value+12),
					hour = $('<div class="mdtp__digit rotate-' + deg + '"></div>');
					$('<span class="outer" data-hour="' + value + '">'+ value +'</span>').appendTo(hour);
					$('<span class="inner" data-hour="' + value24 + '">'+ valueVisable24 +'</span>').appendTo(hour);
					
					hour.find('span').click(function () {
						var _data = parseInt($(this).data('hour')),
							_selectedT = that.selected.getT(),
							_value = _data;

						that.setHour(_value);
						that.switchView('minutes');
					});
				}

				clock.clock.hours.append(hour);
			}

			// Setup minutes
			for (var i = 0; i < 60; i++) {
				var min = i < 10 ? '0' + i : i, deg = (MIN_START_DEG + (i * MIN_DEG_INCR)) % END_DEG,
					minute = $('<div class="mdtp__digit rotate-' + deg + '" data-minute="' + i + '"></div>');

				if (i % 5 === 0) minute.addClass('marker').html('<span class="minute">' + min + '</span>');
				else minute.html('<span class="minute minute_small">' + min +'</span>');
				
				minute.find('span').click(function () {
					that.setMinute($(this).parent().data('minute'));
					
					that.setValue(that.selected);
					that.hide();
				});

				clock.clock.minutes.append(minute);
			}

			// Setup header
			header.wrapper.append(header.hour)
				.append(header.hour)
				.append(header.dots)
				.append(header.minute)
				.append(header.am_pm)
				.appendTo(wrapper);
			
			if(this.is12Hour) {
				header.wrapper.append(header.am)
					.append(header.pm)
					.appendTo(wrapper);
			} 
				
			header.wrapper.appendTo(wrapper);
			
			// Setup clock
			clock.clock.wrapper
				.append(clock.clock.dot)
				.append(clock.clock.hours)
				.append(clock.clock.minutes)
				.appendTo(clock.wrapper);
			
			clock.wrapper.appendTo(wrapper);

			wrapper.appendTo(overlay);
						
			return overlay;
		},

		setHour : function (hour) {
			if (typeof hour === 'undefined') throw new Error('Expecting a value.');

			this.selected.setHour(hour);
			var that = this, selectedHour = parseInt(this.selected.getHour(this.is12Hour));			
			this.timepicker.timeHolder.hour.text(this.selected.getHour(this.is12Hour));
			
			this.timepicker.clockHolder.clock.hours.children('div').each(function (idx, div) {
				var el = $(div);
				
				if(that.is12Hour) {
					var val = parseInt(el.data('hour'));
					
					el[val === selectedHour ? 'addClass' : 'removeClass']('active');		
				} else {
					var inner = el.find('.inner'), outer = el.find('.outer'), valInner = inner.data('hour'), valOuter = outer.data('hour');	
																						
					el[valInner === selectedHour || valOuter === selectedHour ? 'addClass' : 'removeClass']('active');
					inner[valInner === selectedHour ? 'addClass' : 'removeClass']('active');			
					outer[valOuter === selectedHour ? 'addClass' : 'removeClass']('active');
				}
			});
		},

		setMinute : function (minute) {
			if (typeof minute === 'undefined') throw new Error('Expecting a value.');

			var selectedMinute = parseInt(minute);			
			this.selected.setMinutes(selectedMinute);
			this.timepicker.timeHolder.minute.text(parseInt(minute) < 10 ? '0' + parseInt(minute) : minute);

			this.timepicker.clockHolder.clock.minutes.children('div').each(function (idx, div) {
				var el = $(div), val = el.data('minute');

				el[parseInt(val) === selectedMinute ? 'addClass' : 'removeClass']('active');
			});
		},

		setT : function (value) {
			if (typeof value === 'undefined') throw new Error('Expecting a value.');

			if (this.selected.getT() !== value.toUpperCase()) this.selected.invert();

			var a = this.selected.getT();
			
			if(this.is12Hour) { this.timepicker.timeHolder.am_pm.text(a); }
			this.timepicker.timeHolder.am[a === 'AM' ? 'addClass' : 'removeClass']('active');
			this.timepicker.timeHolder.pm[a === 'PM' ? 'addClass' : 'removeClass']('active');
		},

		setValue : function (value) {
			if (typeof value === 'undefined') throw new Error('Expecting a value.');

			var time = typeof value === 'string' ? this.parseTime(value, this.config.format) : value;			
			this.time = new Time(time.hour, time.minute);
			var timeString = time.hour+":"+ time.minute;
			
			var formatted = this.getFormattedTime();
			this.input.attr('data-time', formatted);
						
			services.form.TimeInputControl.sendValueParseUpdate(document.getElementById(this.config.inputID), this.config.controlID, formatted, this.config.format);
		},
		
		getValue : function () {			
			if(this.isTimeAcceptable(this.config.initialTime)) {
				var hours = this.normalizeHours(this.config.initialTime);
				var minutes = this.normalizeMinutes(this.config.initialTime);
				
				this.time = new Time(parseInt(hours), parseInt(minutes));	
			} else {
				var time = this.getSystemTime();
				this.time = new Time(time.hour, time.minute);
			}
		},
							
		// correct format? HH:mm
		isTimeAcceptable : function (time) {
			if(time == null || time == "") {
				return false;
			} else {
				var timeSplitted = time.split(":");
			
				if(timeSplitted.length == 2) {
					var hours = this.normalizeHours(time);
					var minute = this.normalizeMinutes(time);
					return this.isTimeLegal(hours, minute);
				} else {
					return false;
				}
			}
		},
		
		// values set and in range?
		isTimeLegal : function (hour, minute) {
			var h = this.isHourLegal(hour);
			var m = this.isMinuteLegal(minute);
			return !isNaN(hour) && !isNaN(minute) && hour != "" && minute != "" && this.isHourLegal(hour) && this.isMinuteLegal(minute);
		},
		
		isHourLegal : function (hour) {
			return (0 <= hour && hour < 24);
		}, 
		
		isMinuteLegal : function (minute) {
			return (0 <= minute && minute < 60);
		}, 
		
		normalizeHours : function (time) {
			var timeSplitted = time.split(":");
			
			if(time.indexOf(" ") != -1) {
				if((time.indexOf("PM") != -1) && timeSplitted[0] < 12) {
					return parseInt(timeSplitted[0])+12;
				} else {
					return parseInt(timeSplitted[0]);
				}
			} else {
				return parseInt(timeSplitted[0]);
			}
		},
		
		normalizeMinutes : function (time) {
			return time.split(":")[1].split(" ")[0];
		},
		
		resetSelected : function () {
			this.setHour(this.time.hour);
			this.setMinute(this.time.minute);
			if(this.is12Hour) {
				this.setT(this.time.getT());
			}
		},

		getFormattedTime : function () {
			return this.time.format(this.config.format, this.is12Hour);
		},

		getSystemTime : function () {
			var now = new Date();

			return new Time (now.getHours(), now.getMinutes());
		},
		
		parseTime : function (time, tFormat) {
			var that = this, format = typeof tFormat === 'undefined' ? that.config.format : tFormat,	
                aLength = (format.match(/a/g) || []).length,
				mLength = (format.match(/m/g) || []).length,
				timeLength = time.length,
				fH = format.toLowerCase().indexOf('h'), lH = format.toLowerCase().lastIndexOf('h'),
				fA = format.toLowerCase().indexOf('a'),
				hour = '', min = '', t = '',
				isPM = (time.toLowerCase().indexOf("pm") >= 0),
				isAM = (time.toLowerCase().indexOf("am") >= 0);	
				
			// Parse hour		
			if (this.hourPadding) {
				hour = time.substr(fH, 2);
			} else {
				var prev = format.substring(fH - 1, fH), next = format.substring(lH + 1, lH + 2);

				if (lH === format.length - 1) {
					hour = time.substring(time.indexOf(prev, fH - 1) + 1, timeLength);
				} else if (fH === 0) {
					hour = time.substring(0, time.indexOf(next, fH));
				} else {
					hour = time.substring(time.indexOf(prev, fH - 1) + 1, time.indexOf(next, fH + 1));
				}
			}
					
			if(this.is12Hour) {
				hour = isPM && parseInt(hour) < 12 ? (parseInt(hour)+12)%24 : hour;
			}		
						
			format = format.replace(/(hh|h|HH|H)/g, hour);

			var fM = format.indexOf('m'), lM = format.lastIndexOf('m'),
				fT = format.toLowerCase().indexOf('a');

			// Parse minute
			var prevM = format.substring(fM - 1, fM), nextM = format.substring(lM + 1, lM + 2);

			if (lM === format.length - 1) {
				min = time.substring(time.indexOf(prevM, fM - 1) + 1, timeLength);
			} else if (fM === 0) {
				min = time.substring(0, 2);
			} else {
				min = time.substr(fM, 2);
			}
							
			// Parse t (am/pm)
			if (!this.is12Hour) t = parseInt(hour) > 11 ? (aLength > 0 ? 'PM' : 'pm') : (aLength > 0 ? 'AM' : 'am');
			else t = time.substr(fT, 1);

			if(this.is12Hour && !isPM && !isAM) { hour = '';	min = ''; }
			
			var outTime = new Time(hour, min);

			if (this.is12Hour && ((isPM && parseInt(hour) < 12) || (!isPM && parseInt(hour) === 12))) {
			    outTime.invert();
			}
			
			return outTime;
		},		
		
		switchView : function (view) {
			var that = this, picker = this.timepicker, anim_speed = 350;

			if (view !== 'hours' && view !== 'minutes') return;

			that.activeView = view;

			picker.clockHolder.clock.hours.addClass('animate');
			if (view === 'hours') picker.clockHolder.clock.hours.removeClass('hidden');

			clearTimeout(that.hTimeout);

			that.hTimeout = setTimeout(function() {
				if (view !== 'hours') picker.clockHolder.clock.hours.addClass('hidden');
				picker.clockHolder.clock.hours.removeClass('animate');
			}, view === 'hours' ? 20 : anim_speed);

			picker.clockHolder.clock.minutes.addClass('animate');
			if (view === 'minutes') picker.clockHolder.clock.minutes.removeClass('hidden');

			clearTimeout(that.mTimeout);

			that.mTimeout = setTimeout(function() {
				if (view !== 'minutes') picker.clockHolder.clock.minutes.addClass('hidden');
				picker.clockHolder.clock.minutes.removeClass('animate');
			}, view === 'minutes' ? 20 : anim_speed);
		},

		show : function () {
			var that = this;
			if ($(that.config.inputID).val() === '') {
				var time = that.getSystemTime();
				this.time = new Time(time.hour, time.minute);
			}
			
			that.resetSelected();
			
			var popupRequestor = document.getElementById(that.config.controlID + "-commandImage");			
			var popupContent = services.ajax.topWindow.document.getElementById(that.config.controlID + "-mdtp__overlay");
						
			return PlaceDialog.placeDialog({ element: popupRequestor, content: popupContent, preferBelow: true});
		},

		hide : function () {
			services.form.BrowserWindowControl.hidePopupPane(services.form.BrowserWindowControl.getPopupDialogAnchor());
		}
	};

	$.fn.mdtimepicker = function (config) {
			var that = this,
				$that = $(this),
				picker = $(this).data(MDTP_DATA);
				options = $.extend({}, $.fn.mdtimepicker.defaults, $that.data(), typeof config === 'object' && config);

			$that.data(MDTP_DATA, (picker = new MDTimePicker($that, options)));
			
			if(typeof config === 'string') picker[config]();
			return picker;
	}

	$.fn.mdtimepicker.defaults = {
		format: 'h:mm a',			// format of the input value
		inputID: null,				// ID of the input field
		controlID: null,			// ID of the control
		initialTime: null			// time of the input field, format for this type -> 'H:mm', 'HH:mm', 'h:mm a' or 'hh:mm a'
	};
	
}(jQuery);