// ParameterType - results from getParameterTypeList service

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
 * KiWIS "getParameterTypeList" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Parameter {
	/**
	 * "parametertype_name"
	 */
	@JsonProperty("parametertype_name")
	private String parameterTypeName = "";
	
	/**
	 * "parametertype_id"
	 */
	@JsonProperty("parametertype_id")
	private String parameterTypeId = "";

	/**
	 * "station_id"
	 */
	@JsonProperty("station_id")
	private String stationId = "";

	/**
	 * "station_name"
	 */
	@JsonProperty("station_name")
	private String stationName = "";

	/**
	 * "station_no"
	 */
	@JsonProperty("station_no")
	private String stationNo = "";

	/**
	 * "stationparameter_name"
	 */
	@JsonProperty("stationparameter_name")
	private String stationParameterName = "";

	/**
	 * "stationparameter_no"
	 */
	@JsonProperty("stationparameter_no")
	private String stationParameterNo = "";
	
	/**
	 * Constructor needed by Jackson.
	 */
	public Parameter ( ) {
	}

	/**
	 * Return the parameter type name.
	 * @return the parameter type name. 
	 */
	public String getParameterTypeName () {
		return this.parameterTypeName;
	}
	
	/**
	 * Return the parameter type number.
	 * @return the parameter type number. 
	 */
	public String getParameterTypeId () {
		return this.parameterTypeId;
	}

	/**
	 * Return the station ID.
	 * @return the station ID. 
	 */
	public String getStationId () {
		return this.stationId;
	}

	/**
	 * Return the station name.
	 * @return the station name. 
	 */
	public String getStationName () {
		return this.stationName;
	}
	
	/**
	 * Return the station number.
	 * @return the station number. 
	 */
	public String getStationNo () {
		return this.stationNo;
	}
	
	/**
	 * Return the station parameter name.
	 * @return the station parameter name. 
	 */
	public String getStationParameterName () {
		return this.stationParameterName;
	}

	/**
	 * Return the station parameter number.
	 * @return the station parameter number. 
	 */
	public String getStationParameterNo () {
		return this.stationParameterNo;
	}
	
}
