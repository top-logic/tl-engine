	function getHEX(number)  {
		if (number<=9)  {
			num = number+48;
			return String.fromCharCode(num);
		}
		else {
			num = number-10+65;
			return String.fromCharCode(num);
		}
	}
	
	function URLEncode (srcURL)  {
		targetURL="";
		testString="azAZ09";
		for (i=0; i<srcURL.length; i++) {
			c = srcURL.charCodeAt(i);
			if (((c>=65) && (c<=90)) || ((c>=97) && (c<=122)) || ((c>=48) && (c<=57)))  {
				targetURL += srcURL.charAt(i);
			}
			else { 
				higher = c/16;
				lower = c%16;
				targetURL += "%" + getHEX(higher) + getHEX(lower);
			}
		}
		return targetURL;
	}
