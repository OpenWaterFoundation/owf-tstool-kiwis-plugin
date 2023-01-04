// ReadKiWIS_Command - This class initializes, checks, and runs the ReadKiWIS() command.

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2023 Open Water Foundation

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

package org.openwaterfoundation.tstool.plugin.kiwis.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.openwaterfoundation.tstool.plugin.kiwis.datastore.KiWISDataStore;
import org.openwaterfoundation.tstool.plugin.kiwis.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.kiwis.ui.KiWIS_TimeSeries_InputFilter_JPanel;

import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.MissingObjectEvent;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the ReadKiWIS() command.
*/
public class ReadKiWIS_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider
{

/**
Number of where clauses shown in the editor and available as parameters.
*/
private int __numWhere = 6;

/**
Data values for boolean parameters.
*/
protected String _False = "False";
protected String _True = "True";

/**
List of time series read during discovery.  These are TS objects but with mainly the
metadata (TSIdent) filled in.
*/
private List<TS> __discoveryTSList = null;

/**
Constructor.
*/
public ReadKiWIS_Command () {
	super();
	setCommandName ( "ReadKiWIS" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException {
	String warning = "";
    String message;

    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

    String DataStore = parameters.getValue ( "DataStore" );
    String TSID = parameters.getValue ( "TSID" );
    String DataType = parameters.getValue ( "DataType" );
    String Interval = parameters.getValue ( "Interval" );
    String LocId = parameters.getValue ( "LocId" );
    String InputStart = parameters.getValue ( "InputStart" );
    String InputEnd = parameters.getValue ( "InputEnd" );
    String InputFiltersCheck = parameters.getValue ( "InputFiltersCheck" ); // Passed in from the editor, not an actual parameter.
    String SourceNumbers = parameters.getValue ( "SourceNumbers" );
    
	if ( (DataStore == null) || DataStore.isEmpty() ) {
        message = "The datastore must be specified.";
		warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the datastore." ) );
	}
	
	// Make sure that when LocId is specified the data type is not a wildcard.
	if ( (LocId != null) && !LocId.equals("") ) {
		if ( DataType.equals("*") ) {
            message = "The data type cannot be * when matching a single time series.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a specific data type." ) );
		}
	}

	// TODO SAM 2023-01-02 Need to check the WhereN parameters.

	if ( (InputStart != null) && !InputStart.equals("") &&
		!InputStart.equalsIgnoreCase("InputStart") &&
		!InputStart.equalsIgnoreCase("InputEnd") && (InputStart.indexOf("${") < 0)) {
		try {
			DateTime.parse(InputStart);
		}
		catch ( Exception e ) {
            message = "The input start date/time \"" + InputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a date/time or InputStart." ) );
		}
	}
	if ( (InputEnd != null) && !InputEnd.equals("") &&
		!InputEnd.equalsIgnoreCase("InputStart") &&
		!InputEnd.equalsIgnoreCase("InputEnd") && (InputEnd.indexOf("${") < 0)) {
		try {
			DateTime.parse( InputEnd );
		}
		catch ( Exception e ) {
            message = "The input end date/time \"" + InputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify a date/time or InputEnd." ) );
		}
	}

	String IncludeEstimates = parameters.getValue ( "IncludeEstimates" );
	if ((IncludeEstimates != null) && !IncludeEstimates.equals("")){
		if ( !IncludeEstimates.equalsIgnoreCase(_True) &&
			!IncludeEstimates.equalsIgnoreCase(_False) ) {
            message = "Invalid IncludeEstimates parameter \"" + IncludeEstimates + "\"";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify " + _False + " or " + _True + ", or blank for default of " + _True + "." ) );
		}
	}
	String IncludeMissing = parameters.getValue ( "IncludeMissing" );
	if ((IncludeMissing != null) && !IncludeMissing.equals("")){
		if ( !IncludeMissing.equalsIgnoreCase(_True) &&
			!IncludeMissing.equalsIgnoreCase(_False) ) {
            message = "Invalid IncludeMissing parameter \"" + IncludeMissing + "\"";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify " + _False + " or " + _True + ", or blank for default of " + _False + "." ) );
		}
	}

	if ( (SourceNumbers != null) && !SourceNumbers.equals("") && (SourceNumbers.indexOf("${") < 0)) {
		String [] sourceNumbers = StringUtil.toArray(StringUtil.breakStringList(SourceNumbers, ",", 0),true);
		for ( String sourceNumber : sourceNumbers ) {
			if ( !StringUtil.isInteger(sourceNumber) ) {
				message = "Invalid SourceNumber \"" + sourceNumber + "\"";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify source numbers as integers, separated by commas." ) );
			}
		}
	}
	
	// Make sure that some parameters are specified so that a query of all data is disallowed.
	
	if ( ((LocId == null) || LocId.isEmpty()) ) {
		
	}

    // If any issues were detected in the input filter add to the message string.
    if ( (InputFiltersCheck != null) && !InputFiltersCheck.isEmpty() ) {
    	warning += InputFiltersCheck;
    }
    
    // Check for invalid parameters.
    List<String> validList = new ArrayList<>();
    validList.add ( "DataStore" );
    //validList.add ( "TSID" );
    validList.add ( "LocId" );
    validList.add ( "TsShortName" );
    validList.add ( "DataType" );
    validList.add ( "Statistic" );
    validList.add ( "Interval" );
    int numFilters = 25; // Make a big number so all are allowed.
    for ( int i = 1; i <= numFilters; i++ ) { 
        validList.add ( "Where" + i );
    }
    validList.add ( "Alias" );
    validList.add ( "InputStart" );
    validList.add ( "InputEnd" );
    validList.add ( "IncludeEstimates" );
    validList.add ( "IncludeMissing" );
    validList.add ( "SourceNumbers" );
    validList.add ( "Timezone" );
    validList.add ( "Debug" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
 * Create properties for reading time series.
 * @param timezone time zone to be used for response, important for interval calculations
 * @param debug whether to run web service queries in debug
 */
private HashMap<String,Object> createReadProperties ( String timezone, boolean debug ) {
	HashMap<String,Object> readProperties = new HashMap<>();
	if ( (timezone != null) && !timezone.isEmpty() ) {
		readProperties.put("TimeZone", timezone );
	}
	if ( debug ) {
		readProperties.put("Debug", new Boolean(true) );
	}
	return readProperties;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed.
	return (new ReadKiWIS_JDialog ( parent, this )).ok();
}

/**
Return the list of time series read in discovery phase.
*/
private List<TS> getDiscoveryTSList () {
    return __discoveryTSList;
}

/**
Return the list of data objects read by this object in discovery mode.
The following classes can be requested:  TS
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c ) {
	List<TS> discovery_TS_List = getDiscoveryTSList ();
    if ( (discovery_TS_List == null) || (discovery_TS_List.size() == 0) ) {
        return null;
    }
    // Since all time series must be the same interval, check the class for the first one (e.g., MonthTS).
    TS datats = discovery_TS_List.get(0);
    // Also check the base class.
    if ( (c == TS.class) || (c == datats.getClass()) ) {
        return (List<T>)discovery_TS_List;
    }
    else {
        return null;
    }
}

// parseCommand is in parent class.

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException, CommandWarningException, CommandException {
	String routine = getClass().getSimpleName() + ".runCommandInternal", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
	TSCommandProcessor tsprocessor = (TSCommandProcessor)processor;
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    
    Boolean clearStatus = new Boolean(true); // Default.
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen.
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}
    
    boolean readData = true;
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTSList ( null );
        readData = false;
    }
    
	String LocId = parameters.getValue("LocId");
	if ( commandPhase == CommandPhaseType.RUN ) { 
	    LocId = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, LocId);
	}
	String DataType = parameters.getValue("DataType");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    DataType = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, DataType);
	}
	String Interval = parameters.getValue("Interval");
	if ( commandPhase == CommandPhaseType.RUN ) {
	    Interval = TSCommandProcessorUtil.expandParameterValue(getCommandProcessor(), this, Interval);
	}
	String InputStart = parameters.getValue("InputStart");
	if ( (InputStart == null) || InputStart.isEmpty() ) {
		InputStart = "${InputStart}";
	}
    String InputEnd = parameters.getValue("InputEnd");
	if ( (InputEnd == null) || InputEnd.isEmpty() ) {
		InputEnd = "${InputEnd}";
	}
	String Timezone = parameters.getValue ("Timezone" );
	String Debug = parameters.getValue ("Debug" );
	boolean debug = false; // Default
	if ( (Debug != null) && Debug.equalsIgnoreCase(_True) ) {
		debug = true;
	}
	
    DateTime InputStart_DateTime = null;
    DateTime InputEnd_DateTime = null;
	if ( commandPhase == CommandPhaseType.RUN ) {
		try {
			InputStart_DateTime = TSCommandProcessorUtil.getDateTime ( InputStart, "InputStart", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}
		try {
			InputEnd_DateTime = TSCommandProcessorUtil.getDateTime ( InputEnd, "InputEnd", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above.
			++warning_count;
		}
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Set up properties for the read:
	// - OK if null values

	PropList readProps = new PropList ( "ReadProps" );

	// Now try to read.

	List<TS> tslist = new ArrayList<TS>();	// List for time series results.
					// Will be added to for one time series read or replaced if a list is read.
	try {
        String Alias = parameters.getValue ( "Alias" );
        //String TSID = parameters.getValue ( "TSID" );
        String DataStore = parameters.getValue ( "DataStore" );
        KiWISDataStore dataStore = null;
		if ( (DataStore != null) && !DataStore.equals("") ) {
		    // User has indicated that a datastore should be used.
		    DataStore dataStore0 = ((TSCommandProcessor)getCommandProcessor()).getDataStoreForName( DataStore, KiWISDataStore.class );
	        if ( dataStore0 != null ) {
	            Message.printStatus(2, routine, "Selected datastore is \"" + dataStore0.getName() + "\"." );
				dataStore = (KiWISDataStore)dataStore0;
	        }
	    }
		if ( dataStore == null ) {
            message = "Cannot get KiWISDataStore for \"" + DataStore + "\".";
            Message.printWarning ( 2, routine, message );
            status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Verify that a KiWISDataStore datastore is properly configured." ) );
            throw new RuntimeException ( message );
        }
        else {
			// Have a datastore so try to read.
        	// See if a Where has been specified by checking for the first Where clause.
			String WhereN = parameters.getValue ( "Where" + 1 );
			//if ( (WhereN == null) || WhereN.isEmpty() ) {
			if ( (LocId != null) && !LocId.isEmpty() ) {
				// Have single location ID so try to read the matching time series.
				TSIdent tsident = new TSIdent();
				if ( LocId != null ) {
					tsident.setLocation(LocId);
				}
				tsident.setSource("KiWIS");
				if ( DataType != null ) {
					tsident.setType(DataType);
				}
				if ( Interval != null ) {
					tsident.setInterval(Interval);
				}
				String TSID = tsident.getIdentifier();
				// Version that reads a single time series using the TSID.
				Message.printStatus ( 2, routine, "Reading KiWIS web service time series \"" + TSID + "\"" );
				TS ts = null;
				try {
					HashMap<String,Object> readProperties = createReadProperties ( Timezone, debug );
	                ts = dataStore.readTimeSeries ( TSID, InputStart_DateTime, InputEnd_DateTime, readData, readProperties );
				}
				catch ( Exception e ) {
				    ts = null;
					message = "Unexpected error reading KiWIS time series \"" + TSID + "\" (" + e + ").";
					Message.printWarning ( 2, routine, message );
					Message.printWarning ( 2, routine, e );
	                status.addToLog ( commandPhase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Verify the time series identifier." ) );
	                throw new RuntimeException ( message );
				}
				finally {
				    if ( ts == null ) {
				        // Generate an event for listeners.
				        notifyCommandProcessorEventListeners(
					        new MissingObjectEvent(TSID,Class.forName("RTi.TS.TS"),"Time Series", this));
				    }
				}
				if ( ts != null ) {
					// Set the alias.
				    if ( Alias != null ) {
				        ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString(
			                    processor, ts, Alias, status, commandPhase) );
				    }
					tslist.add ( ts );
				}
	        }
			else {
	            // Read 1+ time series using the input filters.
				// Get the input needed to process the file.
				Message.printStatus(2, routine, "Reading multiple time series using input filter.");
				//String DataType = parameters.getValue ( "DataType" );
				//String Interval = parameters.getValue ( "Interval" );
				String InputName = parameters.getValue ( "InputName" );
				if ( InputName == null ) {
					InputName = "";
				}
				List<String> whereNList = new ArrayList<String>();
				int nfg = 0; // Used below.
				// User may have skipped where and left a blank so loop over a sufficiently large number of where parameters.
				for ( int ifg = 0; ifg < 25; ifg++ ) {
					WhereN = parameters.getValue ( "Where" + (ifg + 1) );
					if ( WhereN != null ) {
						++nfg;
						whereNList.add ( WhereN );
					}
				}
	
				// Initialize an input filter based on the data type.
	
				InputFilter_JPanel filterPanel = null;
	
				// Create the input filter panel.
				String dataTypeReq = "";
			    if ( dataTypeReq.indexOf("-") > 0 ) {
			        dataTypeReq = StringUtil.getToken(DataType,"-",0,1).trim();
			    }
			    else {
			        dataTypeReq = DataType.trim();
			    }
	
				filterPanel = new KiWIS_TimeSeries_InputFilter_JPanel ( dataStore, 5 );
	
				// Populate with the where information from the command.
	
				String filter_delim = ";";
				for ( int ifg = 0; ifg < nfg; ifg ++ ) {
					WhereN = whereNList.get(ifg);
	                if ( WhereN.length() == 0 ) {
	                    continue;
	                }
					// Set the filter.
					try {
	                    filterPanel.setInputFilter( ifg, WhereN, filter_delim );
					}
					catch ( Exception e ) {
	                    message = "Error setting where information using \"" + WhereN + "\"";
						Message.printWarning ( 2, routine,message);
						Message.printWarning ( 3, routine, e );
						++warning_count;
	                    status.addToLog ( commandPhase,
	                        new CommandLogRecord(CommandStatusType.FAILURE,
	                            message, "Report the problem to software support - also see the log file." ) );
					}
				}
			
				// Read the list of objects from which identifiers can be obtained.
			
				Message.printStatus ( 2, routine, "Getting the list of time series..." );
			
				// Create empty lists for catalogs from each major data category.
				List<TimeSeriesCatalog> tsCatalogList = new ArrayList<>();
				
				// Read the catalog.
				int size = 0;
				try {
					Integer kiwisTsid = null;
					String kiwisTsPath = null;
					tsCatalogList = dataStore.readTimeSeriesCatalog ( dataTypeReq, Interval, filterPanel, kiwisTsid, kiwisTsPath );
					size = tsCatalogList.size();
				}
				catch ( Exception e ) {
					// Probably no data.
				}
				
				// Make sure that size is set.
	       		if ( size == 0 ) {
					Message.printStatus ( 2, routine,"No KiWIS web service time series were found." );
			        // Warn if nothing was retrieved (can be overridden to ignore).
		            message = "No time series were read from the KiWIS web service.";
		            Message.printWarning ( warning_level, 
		                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
	                status.addToLog ( commandPhase,
	                    new CommandLogRecord(CommandStatusType.FAILURE,
	                        message, "Data may not be in database." +
	                        	"  Previous messages may provide more information." ) );
		            // Generate an event for listeners.
		            // FIXME SAM 2008-08-20 Need to put together a more readable id for reporting.
	                notifyCommandProcessorEventListeners(
	                    new MissingObjectEvent(DataType + ", " + Interval + ", see command for user-specified criteria",
	                        Class.forName("RTi.TS.TS"),"Time Series", this));
					return;
	       		}
			
				// Else, convert each header object to a TSID string and read the time series.
	
				Message.printStatus ( 2, "", "Reading " + size + " time series..." );
	
				String tsidentString = null; // TSIdent string.
				TS ts; // Time series to read.
				TimeSeriesCatalog tsCatalog;
				HashMap<String,Object> readProperties = createReadProperties ( Timezone, debug );
				for ( int i = 0; i < size; i++ ) {
					// Check to see if reading time series should be canceled because the command has been canceled.
					if ( tsprocessor.getCancelProcessingRequested() ) {
						// The user has requested that command processing should be canceled.
						// Check here in this command because a very large query could take a long time before a single command finishes.
						Message.printStatus(2, routine, "Cancel processing based on user request.");
						break;
					}
					// List in order of likelihood to improve performance.
					tsidentString = null; // Do this in case there is no active match.
					tsCatalog = (TimeSeriesCatalog)tsCatalogList.get(i);
					String locId = tsCatalog.getLocId();
					if ( locId.indexOf(".") >= 0 ) {
						locId = "'" + locId + "'";
					}
					tsidentString =
						locId
						+ ".KiWIS"
						+ "." + DataType
						+ "." + Interval;
		            // Update the progress.
					message = "Reading KiWIS web service time series " + (i + 1) + " of " + size + " \"" + tsidentString + "\"";
	                notifyCommandProgressListeners ( i, size, (float)-1.0, message );
					try {
					    ts = dataStore.readTimeSeries (
							tsidentString,
							InputStart_DateTime,
							InputEnd_DateTime, readData, readProperties );
						// Add the time series to the temporary list.  It will be further processed below.
		                if ( (ts != null) && (Alias != null) && !Alias.equals("") ) {
		                    ts.setAlias ( TSCommandProcessorUtil.expandTimeSeriesMetadataString(
		                        processor, ts, Alias, status, commandPhase) );
		                }
		                // Allow null to be added here.
						tslist.add ( ts );
					}
					catch ( Exception e ) {
						message = "Unexpected error reading KiWIS web service time series \"" + tsidentString + "\" (" + e + ").";
						Message.printWarning ( 2, routine, message );
						Message.printWarning ( 2, routine, e );
						++warning_count;
	                    status.addToLog ( commandPhase,
	                        new CommandLogRecord(CommandStatusType.FAILURE,
	                           message, "Report the problem to software support - also see the log file." ) );
					}
				}
			}
		}
    
        int size = 0;
        if ( tslist != null ) {
            size = tslist.size();
        }
        Message.printStatus ( 2, routine, "Read " + size + " KiWIS web service time series." );

        if ( commandPhase == CommandPhaseType.RUN ) {
            if ( tslist != null ) {
                // Further process the time series.
                // This makes sure the period is at least as long as the output period.

                int wc = TSCommandProcessorUtil.processTimeSeriesListAfterRead( processor, this, tslist );
                if ( wc > 0 ) {
                    message = "Error post-processing KiWIS web service time series after read.";
                    Message.printWarning ( warning_level, 
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }
    
                // Now add the list in the processor.
                
                int wc2 = TSCommandProcessorUtil.appendTimeSeriesListToResultsList ( processor, this, tslist );
                if ( wc2 > 0 ) {
                    message = "Error adding KiWIS web service time series after read.";
                    Message.printWarning ( warning_level, 
                        MessageUtil.formatMessageTag(command_tag,
                        ++warning_count), routine, message );
                        status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report the problem to software support." ) );
                    throw new CommandException ( message );
                }
            }
        }
        else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            setDiscoveryTSList ( tslist );
        }
        // Warn if nothing was retrieved (can be overridden to ignore).
        if ( (tslist == null) || (size == 0) ) {
            message = "No time series were read from the KiWIS web service.";
            Message.printWarning ( warning_level, 
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                status.addToLog ( commandPhase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Data may not be in database.  See previous messages." ) );
            // Generate an event for listeners.
            // TOD SAM 2008-08-20 Evaluate whether need here.
            //notifyCommandProcessorEventListeners(new MissingObjectEvent(DataType + ", " + Interval + filter_panel,this));
        }
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message ="Unexpected error reading time series from the KiWIS web service (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
               message, "Report the problem to software support - also see the log file." ) );
		throw new CommandException ( message );
	}

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		throw new CommandWarningException ( message );
	}
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the list of time series read in discovery phase.
*/
private void setDiscoveryTSList ( List<TS> discoveryTSList ) {
    __discoveryTSList = discoveryTSList;
}

/**
Return the string representation of the command.
@param props parameters for the command
*/
public String toString ( PropList props ) {
    if ( props == null ) {
        return getCommandName() + "()";
    }
	StringBuffer b = new StringBuffer ();
	String Alias = props.getValue("Alias"); // Alias added at end.
    String DataStore = props.getValue("DataStore");
    if ( (DataStore != null) && (DataStore.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "DataStore=\"" + DataStore + "\"" );
    }
	//String TSID = props.getValue("TSID");
	//if ( (TSID != null) && (TSID.length() > 0) ) {
	//	if ( b.length() > 0 ) {
	//		b.append ( "," );
	//	}
	//	b.append ( "TSID=\"" + TSID + "\"" );
	//}
	//if ( (TSID == null) || TSID.equals("") ) {
	    // The following need to be explicitly specified when not using the TSID.
        String DataType = props.getValue("DataType");
    	if ( (DataType != null) && (DataType.length() > 0) ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "DataType=\"" + DataType + "\"" );
    	}
    	String Interval = props.getValue("Interval");
    	if ( (Interval != null) && (Interval.length() > 0) ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "Interval=\"" + Interval + "\"" );
    	}
	//}
    // Match 1.
    String LocId = props.getValue("LocId");
  	if ( (LocId != null) && (LocId.length() > 0) ) {
   		if ( b.length() > 0 ) {
   			b.append ( "," );
   		}
   		b.append ( "LocId=\"" + LocId + "\"" );
   	}
    String TsShortName = props.getValue("TsShortName");
  	if ( (TsShortName != null) && (TsShortName.length() > 0) ) {
   		if ( b.length() > 0 ) {
   			b.append ( "," );
   		}
   		b.append ( "TsShortName=\"" + TsShortName + "\"" );
   	}
  	// Match 1+.
	String delim = ";";
    for ( int i = 1; i <= __numWhere; i++ ) {
    	String where = props.getValue("Where" + i);
    	if ( (where != null) && (where.length() > 0) && !where.startsWith(delim) ) {
    		if ( b.length() > 0 ) {
    			b.append ( "," );
    		}
    		b.append ( "Where" + i + "=\"" + where + "\"" );
    	}
    }
    if ( (Alias != null) && (Alias.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Alias=\"" + Alias + "\"" );
    }
	String InputStart = props.getValue("InputStart");
	if ( (InputStart != null) && (InputStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputStart=\"" + InputStart + "\"" );
	}
	String InputEnd = props.getValue("InputEnd");
	if ( (InputEnd != null) && (InputEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "InputEnd=\"" + InputEnd + "\"" );
	}
    String Timezone = props.getValue("Timezone");
    if ( (Timezone != null) && (Timezone.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Timezone=\"" + Timezone + "\"" );
    }
	String Debug = props.getValue("Debug");
	if ( (Debug != null) && (Debug.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Debug=" + Debug );
	}
    return getCommandName() + "("+ b.toString()+")";
}

}