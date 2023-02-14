// TimeSeriesValue - results from getTimeseriesValues service

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KiWIS "getTimeseriesValues" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TimeSeriesValue {
	/**
	 * "timestamp"
	 *
	 * Use a String and parse to a DateTime when adding to the TS object.
	 */
	@JsonProperty("timestamp")
	private String timestamp = "";

	/**
	 * "value"
	 *
	 * Use a String and parse to a double when adding to the TS object.
	 */
	@JsonProperty("value")
	private String value = null;

	/**
	 * "Quality Code"
	 */
	//@JsonProperty("")
	private String qualityCode = null;

	/**
	 * "Interpolation Type" as number
	 */
	//@JsonProperty("")
	private int interpolationTypeNum = -1;

	/**
	 * "Interpolation Type" as enumeration
	 */
	//@JsonProperty("")
	private InterpolationType interpolationType= InterpolationType.UNKNOWN;

	/**
	 * Default constructor used by Jackson.
	 */
	public TimeSeriesValue () {
	}

	/**
	 * Return the interpolation type enumeration.
	 * @return the interpolation type enumeration.
	 */
	public InterpolationType getInterpolationType () {
		return this.interpolationType;
	}

	/**
	 * Return the interpolation type number.
	 * @return the interpolation type number.
	 */
	public int getInterpolationTypeNum () {
		return this.interpolationTypeNum;
	}

	/**
	 * Return the quality code.
	 * @return the quality code.
	 */
	public String getQualityCode () {
		return this.qualityCode;
	}

	/**
	 * Return the timestamp.
	 * @return the timestamp.
	 */
	public String getTimestamp () {
		return this.timestamp;
	}

	/**
	 * Return the value.
	 * @return the value.
	 */
	public String getValue () {
		return this.value;
	}

	/**
	 * Set the interpolation type.
	 * @param interpolationTypeNum interpolation type as a number (e.g., 102).
	 */
	public void setInterpolationType ( int interpolationTypeNum ) {
		// Set the numeric interpolation type.
		this.interpolationTypeNum = interpolationTypeNum;
		// Also set the enumeration.
		this.interpolationType = InterpolationType.valueOf(this.interpolationTypeNum);
	}

	/**
	 * Set the interpolation type.
	 * @param interpolationType interpolation type as an enumeration
	 */
	public void setInterpolationType ( InterpolationType interpolationType ) {
		// Set the enumeration.
		this.interpolationType = interpolationType;
		// Set the numeric interpolation type only if one number is in the array.
		if ( interpolationType.getTypeNums().length == 1 ) {
			this.interpolationTypeNum = interpolationType.getTypeNums()[0];
		}
	}

	/**
	 * Set the quality code.
	 * @param quality code.
	 */
	public void setQualityCode ( String qualityCode ) {
		this.qualityCode = qualityCode;
	}

	/**
	 * Set the timestamp.
	 * @param timestamp.
	 */
	public void setTimestamp ( String timestamp ) {
		this.timestamp = timestamp;
	}


	/**
	 * Set the value.
	 * @param value.
	 */
	public void setValue ( String value ) {
		this.value = value;
	}

}