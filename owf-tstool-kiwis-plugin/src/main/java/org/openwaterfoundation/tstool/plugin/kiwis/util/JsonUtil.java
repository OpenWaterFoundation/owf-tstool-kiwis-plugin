package org.openwaterfoundation.tstool.plugin.kiwis.util;

import java.util.Map;

/**
 * JSON utilities.
 * @author sam
 *
 */
public class JsonUtil {

	/**
	 * Extract an object from a larger JSON string.
	 * This can be used, for example, to extract a substring from a larger collection object.
	 * This class is not intended to replace other JSON parsing code but is useful to split apart large JSON files
	 * so that parts can be parsed individually.
	 * @param jsonString the full JSON string to process
	 * @param objectName the name of the object to extract
	 * @param includeName whether to include the name in returned result (true) or not (false)
	 * @return the individual object string or null if the object does not exist in the input string.
	 */
	public static String extractObjectString ( String jsonString, String objectName, boolean includeName ) {
		// Find the object in the string
		int posName = jsonString.indexOf ( "\"" + objectName + "\"" );
		int len = jsonString.length();
		if ( posName < 0 ) {
			return null;
		}
		// Have a the location of the object name.
		// Find the colon
		int posStart = jsonString.indexOf ( ":", posName);
		// Determine the object-bounding character, either [ or {
		boolean objectIsArray = false;
		boolean bracketFound = false;
		char c;
		int i;
		posStart = posStart + 1; // Increment past colon
		for ( i = posStart; i < len; i++ ) {
			c = jsonString.charAt(i);
			if ( c == '[' ) {
				objectIsArray = true;
				bracketFound = true;
				break;
			}
			else if ( c == '{' ) {
				objectIsArray = false;
				bracketFound = true;
				break;
			}
		}
		if ( !bracketFound ) {
			// Something wrong with the JSON
			return null;
		}
		// Search for the matching bracket, starting from previous character in the string
		// - increment bracket count until the bracket open and close counts are equal
		int bracketOpenCount = 1;
		int bracketCloseCount = 0;
		int posEnd = -1;
		for ( i = (i + 1); i < len; i++ ) {
			c = jsonString.charAt(i);
			if ( c == '\\' ) {
				// Have an escape character so increment just to make sure brackets are not counted
				++i;
				continue;
			}
			else if ( (c == '[') && objectIsArray ) {
				++bracketOpenCount;
			}
			else if ( (c == ']') && objectIsArray ) {
				++bracketCloseCount;
			}
			else if ( (c == '{') && !objectIsArray ) {
				++bracketOpenCount;
			}
			else if ( (c == '}') && !objectIsArray ) {
				++bracketCloseCount;
			}
			if ( bracketOpenCount == bracketCloseCount ) {
				// Have matched the ending bracket
				posEnd = i;
				break;
			}
		}
		if ( posEnd > 0 ) {
			// Found the closing bracket
			if ( includeName ) {
				return jsonString.substring(posName, (posEnd + 1)).trim();
			}
			else {
				return jsonString.substring(posStart, (posEnd + 1)).trim();
			}
		}
		else {
			// Did not find the closing bracket
			return null;
		}
	}
	
	/**
	 * Return an integer object extracted from a map.
	 * @param map map from which to retrieve objects
	 * @param names array of key names to search for in map (multiple are useful if future-proofing changes to API)
	 * @param allowNull if true, return null if the name is not found
	 */
	public static <T> T getFromMap ( Map<String,Object> map, String [] names, boolean allowNull ) {
		for ( int i = 0; i < names.length; i++ ) {
			Object o = map.get(names[i]);
			if ( o == null ) {
				if ( allowNull ) {
					return null;
				}
				else {
					throw new RuntimeException ( "Map does not contain key and null not allowed" );
				}
			}
			else {
				return (T)o;
			}
		}
		return null;
	}
}