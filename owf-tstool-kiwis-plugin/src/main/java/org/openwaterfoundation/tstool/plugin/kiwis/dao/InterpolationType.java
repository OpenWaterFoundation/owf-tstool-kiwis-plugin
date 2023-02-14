// InterpolationType - enumeration of KiWIS interpolation types

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2022-2023 Open Water Foundation

OWF TSTool KiWIS Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OWF TSTool KiWIS Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OWF TSTool KiWIS Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.kiwis.dao;

import java.util.ArrayList;
import java.util.List;

/**
KiWIS interpolation types, used to determine whether time series value is timestamp for
the start or end of an interval.
*/
public enum InterpolationType {
	/**
	Unknown interpolation type.
	*/
	UNKNOWN ( "Unknown interpolation", new int[] {}, -99999,
		"Unknown interpolation type." ),

	/**
	No interpolation.
	*/
	NO_INTERPOLATION ( "No interpolation", new int[] { 101 }, 0,
		"Spot samples and irregular and sparse readings (such as groundwater readings or staff guage readings).  "
		+ "There is no relation possible between two values." ),

	/**
	No interpolation.
	*/
	NO_INTERPOLATION_0 ( "No interpolation", new int[] { 201 }, 0,
		"Tipping buckets, between two tips a zero total is returned." ),

	/**
	Linear interpolation.
	*/
	LINEAR_INTERPOLATION ( "Linear interpolation", new int[] { 102, 202, 302 }, 0,
		"Instantaneously recorded continuous time series data." ),

	/**
	Constant until next time stamp, indicated by the 3 at the end.
	- 103, 203, 303 are not in documentation and are assumed
	*/
	CONSTANT_UNTIL_NEXT_TIMESTAMP ( "Constant until next time stamp", new int[] { 103, 203, 303, 403, 503, 603, 703}, -1,
		"The interval main time stamp is stored at the beginning of the interval."
		+ " The mean is representative until the next time stamp."),

	/**
	Constant since previous time stamp, indicated by the 4 at the end:
	- 104 is not in documentation but has been verified on the Northern Water system
	- 204, 304 are not in documentation and are assumed
	*/
	// TODO smalers 2023-02-01 need to confirm what 104 is.
	CONSTANT_SINCE_PREVIOUS_TIMESTAMP ( "Constant since previous time stamp", new int[] { 104, 204, 304, 404, 504, 604, 704}, 1,
		"The interval main time stamp is stored at the end of the interval."
		+ " The mean is representative since the previous time stamp."),

	// TODO smalers 2023-01-19 don't know how to handle this.
	/**
	Linear until next time stamp.
	*/
	//LINEAR_UNTIL_NEXT_TIMESTAMP ( "Linear until next time stamp", new int[] { 205 }, -1,
	//	"A value that is stored at the beginning of the interval."
	//	+ " The value itself is assumed to grow linearly until the next value."),

	/**
	Linear since previous time stamp.
	*/
	LINEAR_SINCE_PREVIOUS_TIMESTAMP ( "Linear since previous time stamp", new int[] { 206 }, 1,
		"A value that is stored at the end of the interval."
		+ " The value itself is assumed to grow linearly since the previous value.");

	/**
	The interpolation type name from KiWIS documentation.
	*/
	private String name = null;

	/**
	The usage, from KiWIS documentation.
	*/
	private String usage = null;
	
	/**
	 * The internal KiWIS interpolation types corresponding to the enumeration.
	 */
	private int[] typeNums = null;
	
	/**
	 * The position of the time stamp in regular interval time series.
	 * <ul>
	 * <li> -1 - timestamp in WiSKI is at the beginning of the interval for regular interval time series and needs shifted to the end</li>
	 * <li> 0 - timestamp is instantaneous, used for irregular interval data, does not need shifted</li>
	 * <li> 1- timestamp is at the end of the interval and is OK for regular interval time series</li>
	 * </ul>
	 * Only interpolation types corresponding to -1 will cause a shift in the timestamp to the end of the interval.
	 */
	private int timestampPos = 0;

	/**
	Construct an enumeration value.
	@param name KiWIS interpolation type name
	@param typeNums KiWIS interpolation type numbers that apply for the enumeration
	@param timestampPos position of the timestamp (0 for beginning of interval, 0 for irregular interval, 1 for end of interval)
	@param usage KiWIS interpolation type usage, from the documentation
	*/
	private InterpolationType(String name, int [] typeNums, int timestampPos, String usage ) {
    	this.name = name;
    	this.usage = usage;
    	this.typeNums = typeNums;
    	this.timestampPos = timestampPos;
	}

	/**
	Get the list of interpolation types, in appropriate order.
	@return the list of interpolation types.
	*/
	public static List<InterpolationType> getChoices() {
    	List<InterpolationType> choices = new ArrayList<>();
    	choices.add ( InterpolationType.NO_INTERPOLATION );
    	choices.add ( InterpolationType.NO_INTERPOLATION_0 );
    	choices.add ( InterpolationType.LINEAR_INTERPOLATION );
    	choices.add ( InterpolationType.CONSTANT_UNTIL_NEXT_TIMESTAMP );
    	choices.add ( InterpolationType.CONSTANT_SINCE_PREVIOUS_TIMESTAMP );
    	//choices.add ( InterpolationType.LINEAR_UNTIL_NEXT_TIMESTAMP );
    	choices.add ( InterpolationType.LINEAR_SINCE_PREVIOUS_TIMESTAMP );
    	return choices;
	}

	/**
	Get the list of interpolation type as strings.
	@return the list of interpolation types as strings.
	@param includeNote Currently not implemented.
	*/
	public static List<String> getChoicesAsStrings( boolean includeNote ) {
    	List<InterpolationType> choices = getChoices();
    	List<String> stringChoices = new ArrayList<>();
    	for ( int i = 0; i < choices.size(); i++ ) {
        	InterpolationType choice = choices.get(i);
        	String choiceString = "" + choice;
        	//if ( includeNote ) {
            //	choiceString = choiceString + " - " + choice.toStringVerbose();
        	//}
        	stringChoices.add ( choiceString );
    	}
    	return stringChoices;
	}
	
	/**
	 * Return the timestamp position:
	 *  0 if irregular
	 *  -1 if at the beginning of the interval
	 *  1 if at the end of the interval
	 */
	public int getTimestampPos () {
		return this.timestampPos;
	}
	
	/**
	 * Return the type numbers corresponding to the interpolation type.
	 */
	public int [] getTypeNums () {
		return this.typeNums;
	}

	/**
	Return the name for the type.
	@return the name.
	*/
	@Override
	public String toString() {
    	return this.name;
	}

	/**
	Return the enumeration value given an integer type.
	@param typeNum the interpolation type integer (e.g., 101)
	@return the enumeration value given an integer type, or UNKNOWN if not matched
	*/
	public static InterpolationType valueOf ( int typeNum ) {
	    // Iterate through the enumeration values.
    	InterpolationType [] values = values();
    	for ( InterpolationType t : values ) {
    		for ( int typeNum0 : t.typeNums ) {
    			if ( typeNum == typeNum0 ) {
    				// Matched the interpolation type number so return.
    				return t;
    			}
        	}
    	}
    	return UNKNOWN;
	}

	/**
	Return the enumeration value given a string name (case-independent).
	@param name the name to match
	@return the enumeration value given a string name (case-independent), or UNKNOWN if not matched.
	*/
	public static InterpolationType valueOfIgnoreCase (String name) {
	    if ( name == null ) {
        	return UNKNOWN;
    	}
    	InterpolationType [] values = values();
    	for ( InterpolationType t : values ) {
        	if ( name.equalsIgnoreCase(t.name) )  {
            	return t;
        	}
    	}
    	return UNKNOWN;
	}

}