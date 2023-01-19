// Station - results from getStationList service

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
 * KiWIS "getStationList" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Station {
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
	 * "station_id"
	 */
	@JsonProperty("station_id")
	private String stationId = "";

	/**
	 * "station_latitude"
	 */
	@JsonProperty("station_latitude")
	private String stationLatitude = "";

	/**
	 * "station_longitude"
	 */
	@JsonProperty("station_longitude")
	private String stationLongitude = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Station () {
	}

	/**
	 * Return the station identifier.
	 * @return the station identifier. 
	 */
	public String getStationId () {
		return this.stationId;
	}

	/**
	 * Return the station latitude.
	 * @return the station latitude. 
	 */
	public String getStationLatitude () {
		return this.stationLatitude;
	}
	
	/**
	 * Return the station longitude.
	 * @return the station longitude. 
	 */
	public String getStationLongitude () {
		return this.stationLongitude;
	}

	/**
	 * Return the station name.
	 * @return the station name. 
	 */
	public String getStationName () {
		return this.stationName;
	}
	
	/**
	 * Return the site number.
	 * @return the site number. 
	 */
	public String getStationNo () {
		return this.stationNo;
	}
}
