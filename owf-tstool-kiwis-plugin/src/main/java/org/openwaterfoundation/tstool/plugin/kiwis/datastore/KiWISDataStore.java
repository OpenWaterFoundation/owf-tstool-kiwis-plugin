// KiWISDataStore - class that implements the KiWISDataStore plugin datastore

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2022 Open Water Foundation

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

package org.openwaterfoundation.tstool.plugin.kiwis.datastore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openwaterfoundation.tstool.plugin.kiwis.PluginMeta;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.Parameter;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.ParameterType;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.QualityCode;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.Site;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.Station;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.TimeSeries;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.TimeSeriesValue;
import org.openwaterfoundation.tstool.plugin.kiwis.ui.KiWIS_TimeSeries_CellRenderer;
import org.openwaterfoundation.tstool.plugin.kiwis.ui.KiWIS_TimeSeries_InputFilter_JPanel;
import org.openwaterfoundation.tstool.plugin.kiwis.ui.KiWIS_TimeSeries_TableModel;
import org.openwaterfoundation.tstool.plugin.kiwis.dto.JacksonToolkit;
import org.openwaterfoundation.tstool.plugin.kiwis.util.WebUtil;

import com.fasterxml.jackson.databind.JsonNode;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

public class KiWISDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Standard request parameters.
	 */
	private final String COMMON_REQUEST_PARAMETERS = "?service=kisters&type=queryServices&datasource=0";

	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	Map<String,Object> pluginProperties = new LinkedHashMap<>();
	
	/**
	 * Global quality codes.
	 */
	List<QualityCode> qualityCodeList = new ArrayList<>();
	
	/**
	 * Global site list.
	 */
	List<Site> siteList = new ArrayList<>();
	
	/**
	 * Global station list.
	 */
	List<Station> stationList = new ArrayList<>();
	
	/**
	 * Global time series catalog, used to streamline creating lists for UI choices.
	 */
	List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

	/**
	 * Global location ID list, used to streamline creating lists for UI choices,
	 * determined when the tscatalogList is read.
	 */
	List<String> locIdList = new ArrayList<>();

	/**
	 * Global time series short name list, used to streamline creating lists for UI choices,
	 * determined when the tscatalogList is read.
	 */
	List<String> tsShortNameList = new ArrayList<>();
	
	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store
	@param description name for the data store
	@param dmi DMI instance to use for the data store.
	*/
	public KiWISDataStore ( String name, String description, URI serviceRootURI, PropList props ) {
		String routine = getClass().getSimpleName() + ".KiWISDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootURI );
	    setProperties ( props );

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation Kisters WISKI (KiWIS) data web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with Kisters WISKI (KiWIS) web services.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}

	/**
 	* Check the database requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore kiwis-northern version >= 1.5.5
 	* @require datastore kiwis-northern ?configproperty propname? == Something
 	* @require datastore kiwis-northern configuration system_id == CO-District-MHFD
 	*
 	* @enabledif datastore nsdataws-mhfd version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "KiWISDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore nsdataws-mhfd version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore nsdataws-mhfd configuration system_id == CO-District-MHFD
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("system_id") ) {
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'system_id' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether KiWIS has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "KiWIS configuration 'system_id' value is not defined in the database." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "KiWIS configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "KiWIS configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2021-07-29 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nsdataws-mhfd version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}
		
	}
	
	/**
	 * Convert the KiWIS ts_spacing, which uses ISO 8601 duration notation, to a TSTool interval.
	 * See: https://en.wikipedia.org/wiki/ISO_8601
	 * Only simple P or PT strings with one duration part is handled.
	 * @param tsSpacing KiWIS 'ts_spacing' value as ISO duration (e.g., P1D for daily).
	 * @return equivalent TSTool interval
	 */
	private String convertSpacingToInterval ( String tsSpacing ) {
		boolean converted = false;
		String interval = null;
		
		String mult = null; // Multiplier.
		char baseChar; // Interval base character in tsSpacing
		if ( (tsSpacing.length() == 4) && tsSpacing.startsWith("PT") ) {
			// Time.
			mult = tsSpacing.substring(2,3);
			baseChar = tsSpacing.charAt(3);
			converted = true;
			switch ( baseChar ) {
				case 'H':
					interval = "Hour";
					break;
				case 'M':
					interval = "Minute";
					break;
				case 'S':
					interval = "Second";
					break;
				default:
					converted = false;
			}
		}
		else if ( (tsSpacing.length() == 3) && tsSpacing.startsWith("P") ) {
			// Date.
			converted = true;
			mult = tsSpacing.substring(1,2);
			baseChar = tsSpacing.charAt(2);
			switch ( baseChar ) {
				case 'Y':
					interval = "Year";
					break;
				case 'M':
					interval = "Month";
					break;
				case 'W':
					// TODO smalers 2023-01-01 need to handle week.
					interval = "Week";
					converted = false;
					break;
				case 'D': interval = "Day";
					break;
				default:
					converted = false;
			}
		}
		if ( converted ) {
			// Return the converted interval.
			return "" + mult + interval;
		}
		else {
			// Return the original spacing.
			return tsSpacing;
		}
	}

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		KiWIS_TimeSeries_InputFilter_JPanel ifp = new KiWIS_TimeSeries_InputFilter_JPanel(this, 4);
		return ifp;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		// First query the database for the specified input.
		List<TimeSeriesCatalog> tsmetaList = readTimeSeriesMeta ( dataType, timeStep, ifp );
		return getTimeSeriesListTableModel(tsmetaList);
	}
	
	/**
	 * Get the list of location identifier (station_no) strings used in the UI.
	 * The list is determined from the cached list of time series catalog.
	 * @param dataType to match, or * or null to return all, should be a value of stationparameter_no
	 * @return a unique sorted list of the location identifiers (station_no)
	 */
	public List<String> getLocIdStrings ( String dataType ) {
		if ( (dataType == null) || dataType.isEmpty() || dataType.equals("*") ) {
			// Return the cached list of all locations.
			return this.locIdList;
		}
		else {
			// Get the list of locations from the cached list of time series catalog
			List<String> locIdList = new ArrayList<>();
			String stationNo = null;
			String stationParameterNo = null;
			boolean found = false;
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
				stationNo = tscatalog.getStationNo();
				stationParameterNo = tscatalog.getStationParameterNo();
				
				if ( !stationParameterNo.equals(dataType) ) {
					// Requested data type does not match.
					continue;
				}

				found = false;
				for ( String locId2 : locIdList ) {
					if ( locId2.equals(stationNo) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					locIdList.add(stationNo);
				}
			}
			Collections.sort(locIdList, String.CASE_INSENSITIVE_ORDER);
			return locIdList;
		}
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(),
                    	entry.getValue());
    	}
		return pluginProperties;
	}

	/**
	 * Return the site list, currently not cached.
	 * @param readData if true, read the quality codes, if false return the global data
	 */
	public List<QualityCode> getQualityCodes ( boolean readData ) throws IOException {
		if ( readData ) {
			return readQualityCodes();
		}
		else {
			return this.qualityCodeList;
		}
	}

	/**
	 * Return the site list, currently not cached.
	 */
	public List<Site> getSiteList () throws IOException {
		return readSiteList();
	}
	
	/**
	 * Return the station list, currently not cached.
	 */
	public List<Station> getStationList () throws IOException {
		return readStationList();
	}

	/**
	 * Return the list of time series catalog.
	 * @param readData if false, return the global cached data, if true read the data and reset in he cache
	 */
	public List<TimeSeriesCatalog> getTimeSeriesCatalog(boolean readData) {
		if ( readData ) {
			String dataTypeReq = null;
			String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		Integer kiwisTsid = null;
    		String kiwisTsPath = null;
			this.tscatalogList = readTimeSeriesCatalog(dataTypeReq, dataIntervalReq, ifp, kiwisTsid, kiwisTsPath);
		}
		return this.tscatalogList;
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType) {
		boolean includeWildcards = true;
		return getTimeSeriesDataIntervalStrings(dataType, includeWildcards);
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * Currently only the wildcard is returned because intervals cannot be used to filter the getTimeseriesList web service
	 * (without additional work).
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType, boolean includeWildcards ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		int pos = dataType.indexOf(" - ");
		if ( pos > 0 ) {
			// Data type includes SHEF code, for example:  WaterLevelRiver - HG
			dataType = dataType.substring(0, pos).trim();
		}
		// Else use the dataType as is.
		List<String> dataIntervals = new ArrayList<>();
		Message.printStatus(2, routine, "Getting interval strings for data type \"" + dataType + "\"");

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end
	
			dataIntervals.add("*");
			if ( dataIntervals.size() > 1 ) {
				// Also add at the end.
				dataIntervals.add(0,"*");
			}
		}

		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the dataTypes.name properties from the stationSummaries web service request.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval) {
		boolean includeWildcards = true;
		return getTimeSeriesDataTypeStrings(dataInterval, includeWildcards );
	}

	/**
	 * Return the list of time series data type strings.
	 * These strings are the same as the parameter type list 'parametertype_name'.
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval, boolean includeWildcards ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesDataTypeStrings";

		boolean useStationParameters = true;
		List<String> dataTypes = new ArrayList<>();
		if ( useStationParameters ) {
			// Read the parameter list and use the stationparameter_no,
			// which is consistent with the 'ts_path' and TSTool TSID.
			List<Parameter> parameterList = null;
			try {
				parameterList = readParameterList();
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading parameter list (" + e + ")." );
				Message.printWarning(3, routine, e );
			}

			// Create the data type list.
			boolean found = false;
			String stationParameterName = null;
			String stationParameterNo = null;
			if ( parameterList != null ) {
				for ( Parameter p : parameterList ) {
					stationParameterName = p.getStationParameterName();
					stationParameterNo = p.getStationParameterNo();
					found = false;
					for ( String dataType : dataTypes ) {
						//if ( stationParameterName.equals(dataType) ) {
						if ( stationParameterNo.equals(dataType) ) {
							found = true;
							break;
						}
					}
					if ( !found ) {
						//Message.printStatus(2, routine, "Adding parameter name \"" + p.getStationParameterName() + "\"");
						//dataTypes.add(p.getStationParameterName());
						dataTypes.add(p.getStationParameterNo());
					}
				}
			}
		}
		else {
			// Read the parameter type list.
			List<ParameterType> parameterTypeList = null;
			try {
				parameterTypeList = readParameterTypeList();
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading parameter type list (" + e + ")." );
				Message.printWarning(3, routine, e );
			}

			// Create the data type list.
			String parameterTypeName = null;
			boolean found = false;
			if ( parameterTypeList != null ) {
				for ( ParameterType pt : parameterTypeList ) {

					parameterTypeName = pt.getParameterTypeName();
					found = false;
					for ( String dataType : dataTypes ) {
						if ( parameterTypeName.equals(dataType) ) {
							found = true;
							break;
						}
					}
					if ( !found ) {
						//Message.printStatus(2, routine, "Adding parameter type name \"" + pt.getParameterTypeName() + "\"");
						dataTypes.add(parameterTypeName);
					}
				}
			}
		}

		// Sort the names.
		Collections.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Add wildcard at the front and end - allows querying all data types for the location:
			// - always add so that people can get a full list
			// - adding at the top makes it easy to explore data without having to scroll to the end

			dataTypes.add("*");
			dataTypes.add(0,"*");
		}

		return dataTypes;
	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel,
		int row ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesIdentifierFromTableModel";
    	KiWIS_TimeSeries_TableModel tm = (KiWIS_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	//String locId = (String)tableModel.getValueAt(row,tm.COL_LOCATION_ID);
    	String source = "KiWIS"; // TODO smalers 2017-05-15 evaluate whether can get an agency ID.
    	String dataType = (String)tableModel.getValueAt(row,tm.COL_DATA_TYPE);
    	String interval = (String)tableModel.getValueAt(row,tm.COL_DATA_INTERVAL);
    	String scenario = "";
    	String inputName = ""; // Only used for files.
    	TSIdent tsid = null;
    	boolean useTsid = false;
		String datastoreName = this.getName();
		String locId = "";
    	if ( useTsid ) {
    		// Use the LocType and ts_id.
   			locId = "ts_id:" + tableModel.getValueAt(row,tm.COL_TS_ID);
    	}
    	else {
    		// Use the station number for the location.
   			locId = "" + tableModel.getValueAt(row,tm.COL_STATION_NO);
    	}
    	try {
    		tsid = new TSIdent(locId, source, dataType, interval, scenario, datastoreName, inputName );
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	return tsid;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return new KiWIS_TimeSeries_CellRenderer ((KiWIS_TimeSeries_TableModel)tableModel);
    }

    /**
     * Get the TableModel used for displaying the time series.
     *
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return new KiWIS_TimeSeries_TableModel(this,(List<TimeSeriesCatalog>)data);
    }

	/**
	 * This version is required by UI components.
 	 * Return the list of time series statistic strings from the cached TimeSeriesCatalog list.
 	 * This is not currently used by KiWIS.
 	 * @param dataType data type string to filter the list of TimeSeriesCatalog.
 	 * If null, blank, or "*" the data type is not considered when determining the list of statistics.
 	 * @param dataInterval data interval to filter the list of TimeSeriesCatalog.
 	 * @param includeWildcards if true, include "*" at front and back of the list
 	 */
	public List<String> getTimeSeriesStatisticStrings(String dataType, String dataInterval, boolean includeWildcards) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesStatisticStrings";
		int pos = dataType.indexOf(" - ");
		if ( pos > 0 ) {
			// Data type includes SHEF code, for example:  WaterLevelRiver - HG
			dataType = dataType.substring(0, pos).trim();
		}
		// Else use the dataType as is.
		Message.printStatus(2, routine, "Getting statistic strings for data type \"" + dataType + "\" and interval \"" + dataInterval + "\"");
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

		// Get the distinct statistic strings.

		List<String> statisticsDistinct = new ArrayList<>();
	
		if ( Message.isDebugOn ) {
	   		Message.printStatus(2, routine, "Time series catalog has " + statisticsDistinct.size() + " distinct statistics.");
		}

		// Sort the statistic strings.

		Collections.sort(statisticsDistinct);

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end
	
			statisticsDistinct.add("*");
			if ( statisticsDistinct.size() > 1 ) {
				statisticsDistinct.add(0,"*");
			}
		}
		return statisticsDistinct;
	}

	/**
	 * Get the list of time series short name strings used in the UI.
	 * The list is determined from the cached list of time series catalog.
	 * @param dataType the data type (stationparameter_no) to match
	 * @param locId the location ID (station_no) to match
	 */
	public List<String> getTsShortNameStrings ( String dataType, String locId ) {
		String routine = getClass().getSimpleName() + "getTsShortNameStrings";
		if ( ((dataType == null) || dataType.isEmpty() || dataType.equals("*")) && ((locId == null) || locId.isEmpty()) ) {
			// Return the cached list of all time series short names.
			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "Returning all " + this.tsShortNameList.size() + " tsShortName for dataType=" + dataType
					+ " locId=" + locId);
			}
			return this.tsShortNameList;
		}
		else {
			// Only return the list of names that match the requested data type and/or location.
			String stationNo;
			String stationParameterNo;
			String tsShortName;
			tsShortNameList = new ArrayList<>();
			boolean found;
			boolean checkLocId = true;
			boolean checkDataType = true;
			if ( (dataType == null) || dataType.isEmpty() || dataType.equals("*") ) {
				checkDataType = false;
			}
			if ( (locId == null) || locId.isEmpty() || locId.equals("*") ) {
				checkLocId = false;
			}
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
				stationNo = tscatalog.getStationNo();
				stationParameterNo = tscatalog.getStationParameterNo();
				tsShortName = tscatalog.getTsShortName();
				
				if ( checkLocId ) {
					if ( !stationNo.equals(locId) ) {
						// Requested location does not match.
						continue;
					}
				}

				if ( checkDataType ) {
					if ( !stationParameterNo.equals(dataType) ) {
						// Requested location does not match.
						continue;
					}
				}

				found = false;
				for ( String tsShortName2 : tsShortNameList ) {
					if ( tsShortName2.equals(tsShortName) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					tsShortNameList.add(tsShortName);
				}
			}
			Collections.sort(tsShortNameList,String.CASE_INSENSITIVE_ORDER);
			if ( Message.isDebugOn ) {
				Message.printStatus(2, routine, "Found " + tsShortNameList.size() + " tsShortName for dataType=" + dataType
					+ " locId=" + locId);
			}
			return tsShortNameList;
		}
	}

	/**
	 * Lookup the quality code text from the numeric key.
	 * @param qualityCodeList list of quality codes
	 * @param key the numeric quality code key (as a string)
	 * @return the text quality code matching the key, the key if not matched, or an empty string if the key is null
	 */
    public String lookupQualityCode ( List<QualityCode> qualityCodeList, String key ) {
   		if ( key == null) {
   			return "";
   		}
   		Integer keyInt = new Integer(key);
    	Integer key2;
    	for ( QualityCode qualityCode : qualityCodeList ) {
    		key2 = qualityCode.getKey();
    		if ( key2.equals(keyInt) ) {
    			return qualityCode.getCode();
    		}
    	}
    	// Not found so return the key as a string.
    	return "" + key;
    }

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return true;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );

		try {
			this.qualityCodeList = readQualityCodes();
			Message.printStatus(2, routine, "Read " + this.qualityCodeList.size() + " quality codes." );
			if ( Message.isDebugOn ) {
				for ( QualityCode qc : this.qualityCodeList ) {
					Message.printStatus(2, routine, "Quality code: " + qc );
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global quality codes (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		try {
			this.siteList = readSiteList();
			Message.printStatus(2, routine, "Read " + this.siteList.size() + " sites." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global site list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		try {
			this.stationList = readStationList();
			Message.printStatus(2, routine, "Read " + this.stationList.size() + " stations." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global station list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// The time series catalog COULD be used more throughout TSTool, such as when reading time series.
		// However, the initial implementation of readTimeSeries reads the list each time.
		// The cached list is used to create choices for the UI in order to ensure fast performance.
		// Therefore the slowdown is only at TSTool startup.
		try {
    		String dataTypeReq = null;
    		String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		Integer kiwisTsid = null;
    		String kiwisTsPath = null;
    		// Read the catalog for all time series.
			this.tscatalogList = readTimeSeriesCatalog(dataTypeReq, dataIntervalReq, ifp, kiwisTsid, kiwisTsPath );
			Message.printStatus(2, routine, "Read " + this.stationList.size() + " time series catalog." );
			
			// Loop through and create the lists of location ID and time series short name used in the ReadKiWIS command editor.
			
			String stationNo;
			String tsShortName;
			this.locIdList = new ArrayList<>();
			this.tsShortNameList = new ArrayList<>();
			boolean found;
			for ( TimeSeriesCatalog tscatalog : this.tscatalogList ) {
				stationNo = tscatalog.getStationNo();
				tsShortName = tscatalog.getTsShortName();
				
				found = false;
				for ( String stationNo2 : this.locIdList ) {
					if ( stationNo2.equals(stationNo) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					this.locIdList.add(stationNo);
				}

				found = false;
				for ( String tsShortName2 : this.tsShortNameList ) {
					if ( tsShortName2.equals(tsShortName) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					this.tsShortNameList.add(tsShortName);
				}
			}

			// Sort the lists.
			Collections.sort(this.locIdList,String.CASE_INSENSITIVE_ORDER);
			Collections.sort(this.tsShortNameList,String.CASE_INSENSITIVE_ORDER);
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series catalog list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
	}

	/**
 	* Read the getParameterList objects.
 	*/
	private List<Parameter> readParameterList() throws IOException {
		String routine = getClass().getSimpleName() + ".readParameterList";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS
			+ "&request=getParameterList&format=objson"
			+ "&returnFields=parametertype_id,parametertype_name,station_name,station_no,stationparameter_name,stationparameter_no";
		Message.printStatus(2, routine, "Reading parameter list from: " + requestUrl);
		List<Parameter> parameterList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				parameterList.add((Parameter)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Parameter.class));
			}
		}
		return parameterList;
	}

	/**
 	* Read the getParameterTypeList objects.
 	*/
	private List<ParameterType> readParameterTypeList() throws IOException {
		String routine = getClass().getSimpleName() + ".readParameterTypeList";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getParameterTypeList&format=objson";
		Message.printStatus(2, routine, "Reading parameter type list from: " + requestUrl);
		List<ParameterType> parameterTypeList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				parameterTypeList.add((ParameterType)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), ParameterType.class));
			}
		}
		return parameterTypeList;
	}

	/**
 	* Read the getQualityCodes objects.  Results look like:
 		{
  	  	   [ {
    			"key" : 0,
    			"code" : "Excellent",
    			"description" : "Data approved as excellent",
    			"color" : "rgba(46, 139, 85, 1.0)"
  			}, {
    			"key" : 20,
    			"code" : "Approved",
    			"description" : "Approved (WMS)",
    			"color" : "rgba(46, 139, 85, 1.0)"
  			},
 	* @return a list of QualityCode.
 	*/
	private List<QualityCode> readQualityCodes() throws IOException {
		String routine = getClass().getSimpleName() + ".readQualityCodes";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getQualityCodes&format=json";
		Message.printStatus(2, routine, "Reading quality codes from: " + requestUrl);
		List<QualityCode> qualityCodeList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				qualityCodeList.add((QualityCode)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), QualityCode.class));
			}
		}
		return qualityCodeList;
	}

	/**
 	* Read the site list objects.
 	*/
	private List<Site> readSiteList() throws IOException {
		String routine = getClass().getSimpleName() + ".readSiteList";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getSiteList&format=objson";
		Message.printStatus(2, routine, "Reading site list from: " + requestUrl);
		List<Site> siteList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				siteList.add((Site)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Site.class));
			}
		}
		return siteList;
	}

	/**
 	* Read the station list objects.
 	*/
	private List<Station> readStationList() throws IOException {
		String routine = getClass().getSimpleName() + ".readStationist";
		String requestUrl = getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getStationList&format=objson";
		Message.printStatus(2, routine, "Reading station list from: " + requestUrl);
		List<Station> stationList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl, arrayName);
		Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			for(int i = 0; i < jsonNode.size(); i++) {
				stationList.add((Station)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), Station.class));
			}
		}
		return stationList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * @param tsid time series identifier.  The location may be stationNumId or stationNumId-pointTagName or
     * 'stationNumId-pointTagName.with.periods'.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	try {
    		return readTimeSeries ( tsid, readStart, readEnd, readData, null );
    	}
    	catch ( Exception e ) {
    		// Throw a RuntimeException since the method interface does not include an exception type.
    		throw new RuntimeException ( e );
    	}
    }

    /**
     * Read a single time series given its time series identifier.
     * @param tsid time series identifier.  The location may be stationNumId or stationNumId-pointTagName or
     * 'stationNumId-pointTagName.with.periods'.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> Not yet implemented.</li>
     * <li> "Debug" - if true, turn on debug for the query</li>
     * </ul>
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd,
    	boolean readData, HashMap<String,Object> readProperties ) throws Exception {
    	//throws IOException {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	
    	TS ts = null;
    	
    	// Create a time series identifier.
    	TSIdent tsident = TSIdent.parseIdentifier(tsid);
    	String locType = tsident.getLocationType();
    	Integer kiwisTsid = null; // KiWIS ts_id, used if location type is used.
    	String kiwisTsPath = null; // KiWIS ts_path, used if location type is NOT used.
    	// Time series catalog for the single matching time series.
 		TimeSeriesCatalog tscatalog = null;
    	if ( locType.equalsIgnoreCase("ts_id") ) {
    		// KiWIS ts_id uniquely identifies the time series:
    		// - the location is like ts_id:ts_id  (where first 5 characters are 'ts_id:'
    		kiwisTsid = new Integer(tsident.getMainLocation());
    		// Read the time series list for the single time series.
    		String dataTypeReq = null;
    		String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		// Read the catalog matching the KiWIS 'ts_id'.
    		List<TimeSeriesCatalog> tslist = readTimeSeriesCatalog(dataTypeReq, dataIntervalReq, ifp, kiwisTsid, kiwisTsPath );
    		if ( tslist.size() == 0 ) {
    			// Did not match any time series.
    			throw new RuntimeException ( "No time series found matching ts_id = " + kiwisTsid );
    		}
    		else if ( tslist.size() > 1 ) {
    			// Matched more than one time series so identifier information is not unique.
    			throw new RuntimeException ( "Matched " + tslist.size() + " time series for ts_id = " + kiwisTsid + ", expecting 1.");
    		}
    		else {
    			// Matched a single time series so can continue:
    			// - ts_id is used below to read data
    			tscatalog = tslist.get(0);
    		}
    	}
    	else {
    		// KiWIS ts_path parts are used in the TSID:
    		// - station_no.stationparamer_no-ts_shortname
    		// - if necessary: station_no.'stationparamer_no'-'ts_shortname'
    		String stationNo = tsident.getLocation();
    		List<String> parts = StringUtil.breakStringList(tsident.getType(), "-", StringUtil.DELIM_ALLOW_STRINGS);
    		String stationParameterNo = parts.get(0);
    		String tsShortName = parts.get(1);
    		// Read the catalog matching the KiWIS 'ts_path'.
    		String dataTypeReq = null;
    		String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		kiwisTsPath = "*/" + stationNo + "/" + stationParameterNo + "/" + tsShortName;
    		List<TimeSeriesCatalog> tslist = readTimeSeriesCatalog(dataTypeReq, dataIntervalReq, ifp, kiwisTsid, kiwisTsPath );
    		if ( tslist.size() == 0 ) {
    			// Did not match any time series.
    			throw new RuntimeException ( "No time series found matching ts_id = " + kiwisTsid );
    		}
    		else if ( tslist.size() > 1 ) {
    			// Matched more than one time series so identifier information is not unique.
    			throw new RuntimeException ( "Matched " + tslist.size() + " time series for ts_id = " + kiwisTsid + ", expecting 1.");
    		}
    		else {
    			// Matched a single time series so can continue.
    			// - ts_id is used below to read data
    			tscatalog = tslist.get(0);
    			kiwisTsid = tscatalog.getTsId();
    		}
    	}

    	// If the interval is not known, set to irregular so data values will be set
    	if ( tsident.getInterval().isEmpty() ) {
    		tsident.setInterval("IrregSecond");
    	}
    	
    	// Create the time series and set properties.

    	try {
    		ts = TSUtil.newTimeSeries(tsident.toString(), true);
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	// Set the properties.
    	tsident = null;
    	String dataTypeReq = null;
    	String dataIntervalReq = null;
    	try {
    		ts.setIdentifier(tsid);
    		tsident = ts.getIdentifier();
    		dataTypeReq = tsident.getType();
    		dataIntervalReq = tsident.getInterval();
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	// The period will be reset below if irregular because data don't exist exactly at boundaries
    	if ( readStart != null ) {
    		ts.setDate1Original(readStart);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the start down to include a full interval.
    			readStart.round(-1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate1(readStart);
    	}
    	if ( readEnd != null ) {
    		ts.setDate2Original(readEnd);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the end up to include a full interval
    			readEnd.round(1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate2(readEnd);
    	}

    	// Set standard properties:
    	// - use station name for the description because the station parameter name seems to be terse
		ts.setDescription(tscatalog.getStationName());
		ts.setDataUnits(tscatalog.getTsUnitSymbol());
		ts.setDataUnitsOriginal(tscatalog.getTsUnitSymbol());
		ts.setMissing(Double.NaN);

		// Set the time series properties.
		setTimeSeriesProperties ( ts, tscatalog );
    	
    	if ( readData ) {
    		// Also read the time series values.
    		List<TimeSeriesValue> timeSeriesValueList = readTimeSeriesValues ( kiwisTsid, kiwisTsPath, readStart, readEnd, readProperties );
    		
    		String dataFlag = null;
    		DateTime date = null;
    		double value;
    		String valueString;
    		int duration = -1;
    		List<QualityCode> qualityCodeList = this.getQualityCodes(false);
    		if ( timeSeriesValueList.size() > 0 ) {
    			// Set the period based on data.
    			ts.setDate1(DateTime.parse(timeSeriesValueList.get(0).getTimestamp()));
    			ts.setDate2(DateTime.parse(timeSeriesValueList.get(timeSeriesValueList.size() - 1).getTimestamp()));

    			// Allocate the time series data array.
    			ts.allocateDataSpace();
    			
    			// Transfer the TimeSeriesValue list to the TS data.
    			
    			Message.printStatus(2,routine, "Transferring " + timeSeriesValueList.size() + " time series values.");
    			for ( TimeSeriesValue tsValue : timeSeriesValueList ) {
    				try {
    					date = DateTime.parse(tsValue.getTimestamp());
    				}
    				catch ( Exception e ) {
    					Message.printWarning(3, routine, "Error parsing date/time: " + tsValue.getTimestamp());
    					continue;
    				}
    				valueString = tsValue.getValue();
    				if ( (valueString != null) && !valueString.isEmpty() ) {
    					try {
    						value = Double.parseDouble(tsValue.getValue());
    					}
    					catch ( NumberFormatException e ) {
    						Message.printWarning(3, routine, "Error parsing " + tsValue.getTimestamp() + " data value: " + tsValue.getValue());
    						continue;
    					}
    					dataFlag = lookupQualityCode(qualityCodeList, tsValue.getQualityCode());
    					ts.setDataValue(date, value, dataFlag, duration);
    				}
    			}
    		}
    	}

    	return ts;
    }

	/**
	 * Read time series catalog, which uses the "/getTimeseriesList" web service query.
	 * @param dataTypeReq Requested data type (e.g., "DischargeRiver") or "*" to read all data types,
	 *        or null to use default of "*".
	 * @param dataIntervalReq Requested data interval (e.g., "IrregSecond") or "*" to read all intervals,
	 *        or null to use default of "*".
	 * @param ifp input filter panel with "where" conditions
	 * @param kiwisTsid the KiWIS 'ts_id' to match, or null to ignore
	 * @param kiwisTsPath the KiWIS 'ts_path' to match, or null to ignore, can have * for the site_no part
	 */
	public List<TimeSeriesCatalog> readTimeSeriesCatalog ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp,
		Integer kiwisTsid, String kiwisTsPath ) {
		String routine = getClass().getSimpleName() + ".readTimeSeriesCatalog";

		// Note that when requesting additional fields with 'returnfields', aLL fields to be returned must be specified,
		// not just additional fields above the default.
		StringBuilder requestUrl = new StringBuilder(
			getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getTimeseriesList&format=objson&returnfields="
				+ "catchment_id,catchment_name,catchment_no,"
				+ "parametertype_id,parametertype_name,"
				+ "site_id,site_name,site_no,"
				+ "station_id,station_longitude,station_longname,station_latitude,station_name,station_no,"
				+ "stationparameter_longname,stationparameter_name,stationparameter_no,"
				+ "ts_id,ts_name,ts_path,ts_shortname,ts_spacing,ts_type_id,ts_type_name,"
				+ "ts_unitname,ts_unitsymbol,ts_unitname_abs,ts_unitsymbol_abs");

		// Add filters for the data type and time step.
		
		if ( (dataTypeReq != null) && !dataTypeReq.isEmpty() && !dataTypeReq.equals("*") ) {
			try {
				requestUrl.append ( "&stationparameter_no=" + URLEncoder.encode(dataTypeReq,StandardCharsets.UTF_8.toString()) );
			}
			catch ( Exception e ) {
				// TODO smalers 2023-01-01 should not happen.
			}
		}

		if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
			// KiWIS does not allow 'ts_spacing' as a query filter.
		}
		
		// Add query parameters based on the input filter:
		// - this includes list type parameters and specific parameters to match database values
		int numFilterWheres = 0; // Number of filter where clauses that are added.
		if ( ifp != null ) {
	        int nfg = ifp.getNumFilterGroups ();
	        InputFilter filter;
	        String ifpWhereClause=""; // A where clause that is being formed.
	        for ( int ifg = 0; ifg < nfg; ifg++ ) {
	            filter = ifp.getInputFilter ( ifg );
	            // Handle list where clauses:
	            // - TODO smalers, 2019-12-14 for now hard-code to same as in the input filter
	            String whereLabel = filter.getWhereInternal2();
	            //Message.printStatus(2, routine, "IFP whereLabel =\"" + whereLabel + "\"");
	            boolean special = false; // TODO smalers 2022-12-26 might add special filters.
	            if ( special ) {
	            }
	            else {
	            	// Specific where clauses like station description.
	            	// This will return null if the where clause is empty.
	            	/*
	            	ifpWhereClause = DMIUtil.getWhereClauseFromInputFilter(this, filter, ifp.getOperator(ifg), true);
	            	if (ifpWhereClause != null) {
	        	    	try {
	        		    	q.addWhereClause(ifpWhereClause);
	        	    	}
	        	    	catch ( Exception e ) {
	        		    	message = "Error adding where clause for input filter (" + e + ").";
	        		    	Message.printWarning(3,routine,message);
	        		    	// Rethrow because don't want inaccurate query results.
	        		    	throw new RuntimeException ( message, e );
	        	    	}
	            	}
	            	*/
	            	// Add the query parameter to the URL.
				    filter = ifp.getInputFilter(ifg);
				    String queryClause = WebUtil.getQueryClauseFromInputFilter(filter,ifp.getOperator(ifg));
				    if ( Message.isDebugOn ) {
				    	Message.printStatus(2,routine,"Filter group " + ifg + " where is: \"" + queryClause + "\"");
				    }
				    if ( queryClause != null ) {
				    	requestUrl.append("&" + queryClause);
				    	++numFilterWheres;
				    }
	            }
	        }
		}
		
		// Add query parameters for the 'ts_id' and the 'ts_path' if specified.
		
		if ( kiwisTsid != null ) {
			requestUrl.append ( "&ts_id=" + kiwisTsid );
		}
		if ( kiwisTsPath != null ) {
			// Encode because the asterisk may be in the path.
			try {
				requestUrl.append ( "&ts_path=" + URLEncoder.encode(kiwisTsPath,StandardCharsets.UTF_8.toString()) );
			}
			catch ( Exception e ) {
				// TODO smalers 2023-01-01 should not happen.
			}
		}
		
		// If no query filters where added, add a wildcard on the 'station_no' so that all time series are returned.
		if ( numFilterWheres == 0 ) {
	    	requestUrl.append("&station_no=*");
		}
		
		Message.printStatus(2, routine, "Reading time series list from: " + requestUrl);
		List<TimeSeries> timeSeriesList = new ArrayList<>();
		String arrayName = null;
		JsonNode jsonNode = null;
		try {
			jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl.toString(), arrayName);
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error reading time series catalog (" + e + ").");
			Message.printWarning(3,routine,e);
		}
		if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
			Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
			for ( int i = 0; i < jsonNode.size(); i++ ) {
				timeSeriesList.add((TimeSeries)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), TimeSeries.class));
			}
		}
		else {
			Message.printStatus(2, routine, "  Read 0 items.");
		}
		
		// Convert the KiWIS TimeSeries objects to TimeSeriesCatalog.
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		String stationParameterNo;
		String tsShortName;
		for ( TimeSeries timeSeries : timeSeriesList ) {
			TimeSeriesCatalog tscatalog = new TimeSeriesCatalog();

			// Standard properties expected by TSTool:
			// - match the KiWIS 'ts_path' as much as possible since it is unique for retrieving time series values
			stationParameterNo = timeSeries.getStationParameterNo();
			tsShortName = timeSeries.getTsShortName();
			if ( (stationParameterNo.indexOf("-") >= 0) || (stationParameterNo.indexOf(".") >= 0) ) {
				stationParameterNo = "'" + stationParameterNo + "'";
			}
			if ( (tsShortName.indexOf("-") >= 0) || (tsShortName.indexOf(".") >= 0) ) {
				tsShortName = "'" + tsShortName + "'";
			}
			tscatalog.setDataType(stationParameterNo + "-" + tsShortName);
			tscatalog.setDataInterval(convertSpacingToInterval(timeSeries.getTsSpacing()));
			tscatalog.setDataUnits(timeSeries.getTsUnitSymbol()); // Symbol = abbreviation?

			// Standard and additional properties returned by the web service (see 'returnFields').
			tscatalog.setCatchmentId( (timeSeries.getCatchmentId() == null) ? null : new Integer(timeSeries.getCatchmentId()));
			tscatalog.setCatchmentName(timeSeries.getCatchmentName());

			tscatalog.setStationId( (timeSeries.getStationId() == null) ? null : new Integer(timeSeries.getStationId()));
			tscatalog.setStationLongName(timeSeries.getStationLongName());
			tscatalog.setStationLatitude(timeSeries.getStationLatitude());
			tscatalog.setStationLongitude(timeSeries.getStationLongitude());
			tscatalog.setStationName(timeSeries.getStationName());
			tscatalog.setStationNo(timeSeries.getStationNo());

			tscatalog.setStationParameterLongName(timeSeries.getStationParameterLongName());
			tscatalog.setStationParameterName(timeSeries.getStationParameterName());
			tscatalog.setStationParameterNo(timeSeries.getStationParameterNo());

			tscatalog.setSiteId( (timeSeries.getSiteId() == null) ? null : new Integer(timeSeries.getSiteId()));
			tscatalog.setSiteName(timeSeries.getSiteName());
			tscatalog.setSiteNo(timeSeries.getSiteNo());
			
			tscatalog.setParameterTypeId( (timeSeries.getParameterTypeId() == null) ? null : new Integer(timeSeries.getParameterTypeId()));
			tscatalog.setParameterTypeName(timeSeries.getParameterTypeName());

			tscatalog.setTsId( (timeSeries.getTsId() == null) ? null : new Integer(timeSeries.getTsId()));
			tscatalog.setTsName(timeSeries.getTsName());
			tscatalog.setTsPath(timeSeries.getTsPath());
			tscatalog.setTsShortName(timeSeries.getTsShortName());
			tscatalog.setTsSpacing(timeSeries.getTsSpacing());
			tscatalog.setTsTypeId( (timeSeries.getTsTypeId() == null) ? null : new Integer(timeSeries.getTsTypeId()));
			tscatalog.setTsTypeName(timeSeries.getTsTypeName());
			tscatalog.setTsUnitName(timeSeries.getTsUnitName());
			tscatalog.setTsUnitNameAbs(timeSeries.getTsUnitNameAbs());
			tscatalog.setTsUnitSymbol(timeSeries.getTsUnitSymbol());
			tscatalog.setTsUnitSymbolAbs(timeSeries.getTsUnitSymbolAbs());
			
			// Save the catalog in the list.
			tscatalogList.add(tscatalog);
		}
		
		return tscatalogList;
	}

    /**
     * Read time series values.
     * @param kiwisTsid the Kisters 'ts_id' when the TSID uses location type.
     * @param kiwisTsPath the Kisters 'ts_path' when the TSID uses parts similar to the 'ts_path'
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> Not yet implemented.</li>
     * </ul>
     * @return the list of time series values, may be an empty list
     */
    public List<TimeSeriesValue> readTimeSeriesValues ( Integer kiwisTsid, String kiwisTsPath, DateTime readStart, DateTime readEnd,
    	HashMap<String,Object> readProperties ) throws Exception {
    	//throws IOException {
    	String routine = getClass().getSimpleName() + ".readTimeSeriesValues";
    	List<TimeSeriesValue> timeSeriesValues = new ArrayList<>();

		// Note that when requesting additional fields with 'returnfields', aLL fields to be returned must be specified,
		// not just additional fields above the default.


		//String format="dajson";
		String format="csv";

		StringBuilder requestUrl = new StringBuilder(
			getServiceRootURI() + COMMON_REQUEST_PARAMETERS + "&request=getTimeseriesValues&format=" + format
				+ "&ts_id=" + kiwisTsid
				+ "&returnfields="
				+ URLEncoder.encode("Timestamp,Value,Quality Code",StandardCharsets.UTF_8.toString()));
		
		// Add where for period to query using ISO format "YYYY-MM-DD hh:mm:ss":
		// - no T between date and time?
		// - must URLencode the string
		
		if ( readStart != null ) {
			//requestUrl.append("&from=" + readStart);
			requestUrl.append("&from=" + URLEncoder.encode(readStart.toString(DateTime.FORMAT_YYYY_MM_DD_HH_mm),StandardCharsets.UTF_8.toString()));
		}
		if ( readEnd != null ) {
			//requestUrl.append("&to=" + readEnd);
			requestUrl.append("&to=" + URLEncoder.encode(readEnd.toString(DateTime.FORMAT_YYYY_MM_DD_HH_mm),StandardCharsets.UTF_8.toString()));
		}
		if ( (readStart == null) && (readEnd == null) ) {
			// Request all data.
			requestUrl.append("&period=complete");
		}
		
		Message.printStatus(2, routine, "Reading time series values from: " + requestUrl);

		// Request the JSON and parse into objects.

		if ( format.equals("csv") ) {
			/* Format is similar to the following:
			 * - semi-colons are used since not encountered in data?
			#ts_id;957010
			#rows;1
			#Timestamp;Value
			2022-12-30T18:00:00.000-07:00;84.88
			*/
			// Read the URI content into a string:
			// - break by newlines and then process
			String outputFile = null;
			StringBuilder outputString = new StringBuilder();
			// Set timeout to 5 minutes.
			int connectTimeout = 300000;
			int readTimeout = 300000;
			// URL-encode the URL so that for example space is replaced by %20
			IOUtil.getUriContent(requestUrl.toString(), outputFile, outputString, connectTimeout, readTimeout);
			BufferedReader reader = new BufferedReader(new StringReader(outputString.toString()));
			String line = null;
			String delim = ";";
			List<String> tokens = null;
			TimeSeriesValue timeSeriesValue = null;
			while ( (line = reader.readLine()) != null ) {
				if ( line.isEmpty() ) {
					// Totally empty line.
					continue;
				}
				else if ( line.trim().isEmpty() ) {
					// Empty after removing whitespace.
					continue;
				}
				else if ( line.charAt(0) == '#' ) {
					// Comment.
					continue;
				}

				// Split the line by semicolons.
				tokens = StringUtil.breakStringList(line, delim, 0);
				// Make sure the requested fields.
				if ( tokens.size() == 3 ) {
					// Create a new value object.
					timeSeriesValue = new TimeSeriesValue();
					// Transfer the values from response to the value object.
					timeSeriesValue.setTimestamp(tokens.get(0));
					timeSeriesValue.setValue(tokens.get(1));
					timeSeriesValue.setQualityCode(tokens.get(2));
					// Add the value object to the list to return.
					timeSeriesValues.add(timeSeriesValue);
				}
			}
		}
		else if ( format.equals("dajson") ) {
			// format=dajson returns the following, which is somewhat difficult to handle so use csv.
			/*
			[ {
			  	"ts_id" : "957010",
			  	"rows" : "1",
			  	"columns" : "Timestamp,Value",
			  	"data" : [ [ "2022-12-30T17:00:00.000-07:00", 78.31 ] ]
				} ]
			*/
			JsonNode jsonNode = null;
			String arrayName = null;
			try {
				jsonNode = JacksonToolkit.getInstance().getJsonNodeFromWebServiceUrl(requestUrl.toString(), arrayName);
			}
			catch ( Exception e ) {
				Message.printWarning(3,routine,"Error reading time series values (" + e + ").");
				Message.printWarning(3,routine,e);
			}
			if ( (jsonNode != null) && (jsonNode.size() > 0) ) {
				Message.printStatus(2, routine, "  Read " + jsonNode.size() + " items.");
				for ( int i = 0; i < jsonNode.size(); i++ ) {
					timeSeriesValues.add((TimeSeriesValue)JacksonToolkit.getInstance().treeToValue(jsonNode.get(i), TimeSeriesValue.class));
				}
			}
			else {
				Message.printStatus(2, routine, "  Read 0 items.");
			}
		}
    	
    	return timeSeriesValues;
    }

    /**
     * Read time series metadata, which results in a query that joins station, station_type, point, point_class, and point_type.
     */
    List<TimeSeriesCatalog> readTimeSeriesMeta ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
    	// Remove note from data type.
	   	int pos = dataTypeReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataTypeReq = dataTypeReq.substring(0, pos);
	   	}
	   	pos = dataIntervalReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataIntervalReq = dataIntervalReq.substring(0, pos).trim();
	   	}
	   	// By default all time series are included in the catalog:
	   	// - the filter panel options can be used to constrain
	   	// - the kiwisTsid and kiwisTsPath are not used here
	   	Integer kiwisTsid = null;
	   	String kiwisTsPath = null;
	    return readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp, kiwisTsid, kiwisTsPath );
	}

    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
    	return "";
    }

    /**
     * Set the time series properties from the TimeSeriesCatalog.
     */
    private void setTimeSeriesProperties ( TS ts, TimeSeriesCatalog tscatalog ) {
    	// Set all the KiWIS properties that are known for the time series.
    	ts.setProperty("catchment_id", tscatalog.getCatchmentId());
    	ts.setProperty("catchment_name", tscatalog.getCatchmentName());
    	ts.setProperty("catchment_no", tscatalog.getCatchmentNo());

    	ts.setProperty("parametertype_id", tscatalog.getParameterTypeId());
    	ts.setProperty("parametertype_name", tscatalog.getParameterTypeName());

    	ts.setProperty("site_id", tscatalog.getSiteId());
    	ts.setProperty("site_name", tscatalog.getSiteName());
    	ts.setProperty("site_no", tscatalog.getSiteNo());

    	ts.setProperty("station_id", tscatalog.getStationId());
    	ts.setProperty("station_latitude", tscatalog.getStationLatitude());
    	ts.setProperty("station_longitude", tscatalog.getStationLongitude());
    	ts.setProperty("station_longname", tscatalog.getStationName());
    	ts.setProperty("station_name", tscatalog.getStationName());
    	ts.setProperty("station_no", tscatalog.getStationNo());

    	ts.setProperty("stationparmeter_longname", tscatalog.getStationParameterLongName());
    	ts.setProperty("stationparmeter_name", tscatalog.getStationParameterName());
    	ts.setProperty("stationparmeter_no", tscatalog.getStationParameterNo());

    	ts.setProperty("ts_id", tscatalog.getTsId());
    	ts.setProperty("ts_name", tscatalog.getTsName());
    	ts.setProperty("ts_path", tscatalog.getTsPath());
    	ts.setProperty("ts_shortname", tscatalog.getTsShortName());
    	ts.setProperty("ts_unitname", tscatalog.getTsUnitName());
    	ts.setProperty("ts_unitname_abs", tscatalog.getTsUnitNameAbs());
    	ts.setProperty("ts_unitsymbol", tscatalog.getTsUnitSymbol());
    	ts.setProperty("ts_unitsymbol_abs", tscatalog.getTsUnitSymbolAbs());

    	ts.setProperty("ts_type_id", tscatalog.getTsTypeId());
    	ts.setProperty("ts_type_name", tscatalog.getTsTypeName());

    	ts.setProperty("ts_spacing", tscatalog.getTsSpacing());
    }

}