# TSTool / Command / ReadKiWIS #

* [Overview](#overview)
* [Command Editor](#command-editor)
    +   [Match Single Time Series](#match-single-time-series)
    +   [Match 1+ Time Series](#match-1-time-series)
* [Command Syntax](#command-syntax)
* [Examples](#examples)
* [Troubleshooting](#troubleshooting)
* [See Also](#see-also)

-------------------------

## Overview ##

The `ReadKiWIS` command reads one or more time series from KiWIS web services:

*   Read a single time series by matching a TSTool time series identifier (TSID).
*   Read 1+ time series using filters similar to the main TSTool window.

See the [KiWIS Data Web Services Appendix](../../datastore-ref/KiWIS/KiWIS.md)
for more information about KiWIS web service integration and limitations.
The command is designed to utilize web service query criteria to process large numbers of time series,
for example to produce real-time information products and perform historical data analysis and quality control.

See also the 
[TSID for KiWIS](../TSID/TSID.md) time series identifier command,
which reads time series for a single time series.

The ***Data type***, ***Data interval***, and ***Where*** command parameters and input fields
are similar to those in the main TSTool interface.
However, whereas the main TSTool interface first requires a query to find the
matching time series list and interactive select to copy specific time series identifiers into the ***Commands*** area,
the `ReadKiWIS` command reads the time series list and the corresponding data for the time series.
This can greatly shorten command files and simplify command logic, especially when processing many time series.
However, because the command can process many time series and web services are impacted by network speed,
running the command can take a while to complete.

Data for the location and other time series metadata,
as shown in the main TSTool interface, are set as time series properties, using web service data values.
These properties can be transferred to a table with the
[`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/)
command and processed further with other table commands.

## Command Editor ##

The following dialog is used to edit the command and illustrates the syntax for the command.
Two options are available for matching time series.

### Match Single Time Series ###

The following example illustrates how to read a single time series by specifying the data type and interval (top)
and location identifier and time series short name (***Match Single Time Series*** tab).
This approach is similar to using the general
[`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/)
command but offers parameters specific to KiWIS web services.

**<p style="text-align: center;">
![ReadKiWIS-single](ReadKiWIS-single.png)
</p>**

**<p style="text-align: center;">
`ReadKiWIS` Command Editor to Read a Single Time Series (<a href="../ReadKiWIS-single.png">see also the full-size image)</a>
</p>**

### Match 1+ Time Series ###

The following figure illustrates how to query multiple time series.
For example, this can be used to process all time series of a data type in the system
or all time series for a location.

**<p style="text-align: center;">
![ReadKiWIS-multiple](ReadKiWIS-multiple.png)
</p>**

**<p style="text-align: center;">
`ReadKiWIS` Command Editor to Read Multiple Time Series (<a href="../ReadKiWIS-multiple.png">see also the full-size image)</a>
</p>**

## Command Syntax ##

The command syntax is as follows:

```text
ReadKiWIS(Parameter="Value",...)
```

**<p style="text-align: center;">
Command Parameters
</p>**

|**Tab**|**Parameter**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|**Description**|**Default**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
|--------------|-----------------|-----------------|--|
|All|`DataStore`<br>**required**|The KiWIS datastore name to use for the web services connection, as per datastore configuration files (see the [KiWIS Web Services Datastore appendix](../../datastore-ref/KiWIS/KiWIS.md)). | None - must be specified. |
||`DataType`<br>**required**|The data type to be queried, corresponding to KiWIS `stationparameter_no`. | `*` to read all the time series. |
||`Interval`<br>**required**|The data interval for the time series, corresponding to the KiWIS `ts_spacing` converted from [ISO 8601 duration](https://en.wikipedia.org/wiki/ISO_8601) (e.g., `P1D`) to TSTool interval (e.g., `1Day`). The interval is currently NOT used to filter time series because other data uniquely identify the time series. | `*` - to read all the time series. |
|***Match Single Time Series***|`LocId`<br>**required**|The location identifier, corresponding to KiWIS `station_no`. | None - must be specified to read a single time series. |
| |`TsShortName`<br>**required**|The data type to be queried, corresponding to KiWIS `stationparameter_no`. | None - must be specified to read a single time series. |
||`TSID`| A view-only value that indicates the time series identifier that will result from the input parameters when a single time series is queried. | |
|***Match 1+ Time Series***|`WhereN`|When reading 1+ time series, the “where” clauses to be applied.  The filters match the values in the Where fields in the command editor dialog and the TSTool main interface.  The parameters should be named `Where1`, `Where2`, etc., with a gap resulting in the remaining items being ignored.  The format of each value is:<br>`Item;Operator;Value`<br>Where `Item` indicates a data field to be filtered on, `Operator` is the type of constraint, and `Value` is the value to be checked when querying.|If not specified, the query will not be limited and very large numbers of time series may be queried.|
|All|`Alias`<br>|The alias to assign to the time series, as a literal string or using the special formatting characters listed by the command editor.  The alias is a short identifier used by other commands to locate time series for processing, as an alternative to the time series identifier (`TSID`).|None – alias not assigned.|
||`InputStart`|Start of the period to query, specified as a date/time with a precision that matches the requested data interval.|Read all available data.|
||`InputEnd`|End of the period to query, specified as a date/time with a precision that matches the requested data interval.|Read all available data.|
||`Timezone`| **Not implemented.** Time zone for output, used when the web services time zone is not the same as that of the station. This may be implemented in the future. | Output will use the web service data time zone. |
||`Debug`| Used for troubleshooting:  `False` or `True`. | `False` |
|| `QueryParameters` | **Not implemented.**  It may be necessary to implement this or a similar command parameter to allow adding additional query parameters to the KiWIS `getTimeseriesValues` request.  The service supports returning additional parameters that are not built into this command. |

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-kiwis-plugin/tree/master/test/commands/ReadKiWIS/).

## Troubleshooting ##

## See Also ##

* [`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/) command
* [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command
* [TSID for KiWIS](../TSID/TSID.md) command
