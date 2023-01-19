// TimeSeries - results from getTimeseriesList service

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
 * KiWIS "getTimeseriesList" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TimeSeries {
	/**
	 * "catchment_id"
	 */
	@JsonProperty("catchment_id")
	private String catchmentId = "";

	/**
	 * "catchment_name"
	 */
	@JsonProperty("catchment_name")
	private String catchmentName = "";

	/**
	 * "catchment_no"
	 */
	@JsonProperty("catchment_no")
	private String catchmentNo = "";

	/**
	 * "parametertype_id"
	 */
	@JsonProperty("parametertype_id")
	private String parameterTypeId = "";

	/**
	 * "parametertype_name"
	 */
	@JsonProperty("parametertype_name")
	private String parameterTypeName = "";

	/**
	 * "site_id"
	 */
	@JsonProperty("site_id")
	private String siteId = "";

	/**
	 * "site_name"
	 */
	@JsonProperty("site_name")
	private String siteName = "";

	/**
	 * "site_name"
	 */
	@JsonProperty("site_no")
	private String siteNo = "";

	/**
	 * "station_id"
	 */
	@JsonProperty("station_id")
	private String stationId = "";

	/**
	 * "station_latitude"
	 */
	@JsonProperty("station_latitude")
	private Double stationLatitude = null;

	/**
	 * "station_longitude"
	 */
	@JsonProperty("station_longitude")
	private Double stationLongitude = null;

	/**
	 * "station_longname"
	 */
	@JsonProperty("station_longname")
	private String stationLongName = "";

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
	 * "stationparameter_longname"
	 */
	@JsonProperty("stationparameter_longname")
	private String stationParameterLongName = "";
	
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
	 * "ts_id"
	 */
	@JsonProperty("ts_id")
	private String tsId = "";

	/**
	 * "ts_name"
	 */
	@JsonProperty("ts_name")
	private String tsName = "";

	/**
	 * "ts_path"
	 */
	@JsonProperty("ts_path")
	private String tsPath = "";

	/**
	 * "ts_shortname"
	 */
	@JsonProperty("ts_shortname")
	private String tsShortName = "";

	/**
	 * "ts_spacing"
	 */
	@JsonProperty("ts_spacing")
	private String tsSpacing = "";

	/**
	 * "ts_unitname"
	 */
	@JsonProperty("ts_unitname")
	private String tsUnitName = "";

	/**
	 * "ts_unitname_abs"
	 */
	@JsonProperty("ts_unitname_abs")
	private String tsUnitNameAbs = "";

	/**
	 * "ts_unitsymbol"
	 */
	@JsonProperty("ts_unitsymbol")
	private String tsUnitSymbol = "";

	/**
	 * "ts_unitsymbol_abs"
	 */
	@JsonProperty("ts_unitsymbol_abs")
	private String tsUnitSymbolAbs = "";

	/**
	 * "ts_type_id"
	 */
	@JsonProperty("ts_type_id")
	private String tsTypeId = "";

	/**
	 * "ts_type_name"
	 */
	@JsonProperty("ts_type_name")
	private String tsTypeName = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public TimeSeries () {
	}

	/**
	 * Return the catchment identifier.
	 * @return the catchment identifier. 
	 */
	public String getCatchmentId () {
		return this.catchmentId;
	}

	/**
	 * Return the catchment name.
	 * @return the catchment name. 
	 */
	public String getCatchmentName () {
		return this.catchmentName;
	}

	/**
	 * Return the parameter type identifier.
	 * @return the parameter type identifier. 
	 */
	public String getParameterTypeId () {
		return this.parameterTypeId;
	}

	/**
	 * Return the parameter type name.
	 * @return the parameter type name. 
	 */
	public String getParameterTypeName () {
		return this.parameterTypeName;
	}

	/**
	 * Return the site identifier.
	 * @return the site identifier. 
	 */
	public String getSiteId () {
		return this.siteId;
	}

	/**
	 * Return the site name.
	 * @return the site name. 
	 */
	public String getSiteName () {
		return this.siteName;
	}

	/**
	 * Return the site number.
	 * @return the site number. 
	 */
	public String getSiteNo () {
		return this.siteNo;
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
	public Double getStationLatitude () {
		return this.stationLatitude;
	}

	/**
	 * Return the station longitude.
	 * @return the station longitude. 
	 */
	public Double getStationLongitude () {
		return this.stationLongitude;
	}

	/**
	 * Return the station long name.
	 * @return the station long name. 
	 */
	public String getStationLongName () {
		return this.stationLongName;
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
	 * Return the station parameter long name.
	 * @return the station parameter long name. 
	 */
	public String getStationParameterLongName () {
		return this.stationParameterLongName;
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

	/**
	 * Return the time series identifier.
	 * @return the time series identifier. 
	 */
	public String getTsId () {
		return this.tsId;
	}

	/**
	 * Return the time series name.
	 * @return the time series name. 
	 */
	public String getTsName () {
		return this.tsName;
	}

	/**
	 * Return the time series path.
	 * @return the time series path. 
	 */
	public String getTsPath () {
		return this.tsPath;
	}

	/**
	 * Return the time series type ID.
	 * @return the time series type ID. 
	 */
	public String getTsShortName () {
		return this.tsShortName;
	}

	/**
	 * Return the time series spacing (interval).
	 * @return the time series spacing (interval). 
	 */
	public String getTsSpacing () {
		return this.tsSpacing;
	}

	/**
	 * Return the time series type ID.
	 * @return the time series type ID.
	 */
	public String getTsTypeId () {
		return this.tsTypeId;
	}

	/**
	 * Return the time series type name.
	 * @return the time series type name.
	 */
	public String getTsTypeName () {
		return this.tsTypeName;
	}

	/**
	 * Return the time series unit name.
	 * @return the time series unit name. 
	 */
	public String getTsUnitName () {
		return this.tsUnitName;
	}

	/**
	 * Return the time series unit name (abs).
	 * @return the time series unit name (abs). 
	 */
	public String getTsUnitNameAbs () {
		return this.tsUnitNameAbs;
	}

	/**
	 * Return the time series unit symbol.
	 * @return the time series unit symbol. 
	 */
	public String getTsUnitSymbol () {
		return this.tsUnitSymbol;
	}

	/**
	 * Return the time series unit symbol (abs).
	 * @return the time series unit symbol (abs). 
	 */
	public String getTsUnitSymbolAbs () {
		return this.tsUnitSymbolAbs;
	}

}
