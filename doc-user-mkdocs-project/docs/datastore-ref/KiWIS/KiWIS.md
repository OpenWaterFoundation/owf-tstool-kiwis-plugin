# TSTool / Datastore Reference / KiWIS Web Services #

* [Overview](#overview)
* [Standard Time Series Properties](#standard-time-series-properties)
* [Limitations](#limitations)
* [Datastore Configuration File](#datastore-configuration-file)
* [See Also](#see-also)

--------------------

## Overview ##

The KiWIS web services allow Kisters WISKI data to be queried by software,
including web applications and analysis tools such as TSTool.
TSTool accesses KiWIS web services using the KiWIS plugin
(see the [Install KiWIS Plugin](../../appendix-install/install.md) appendix).

See the [KiWIS documentation](https://data.northernwater.org/KiWIS/KiWIS?datasource=0&service=kisters&type=queryServices&request=getrequestinfo)
for information about available web services, including web service endpoints and parameters.

TSTool primarily uses the KiWIS `getTimeseriesList` service to list time series and retrieve time series metadata,
and the `getTimeseriesValues` service to retrieve time series for display and analysis.

The TSTool [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/)
command can be used to retrieve data from any web service and save to a file.
For example, a CSV format file can be saved and the resulting file can be read using commands such as
[`ReadTableFromDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromDelimitedFile/ReadTableFromDelimitedFile/)
(to read a table) and
[`ReadDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadDelimitedFile/ReadDelimitedFile/)
(to read time series). The
[`ReadTableFromJSON`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromJSON/ReadTableFromJSON/)
command can be used to read a JSON file that has been retrieved from web services.
Consequently, there is generally a way to use the web services to retrieve and process data in TSTool.

## Web Service to Time Series Mapping ##

Time series data objects in TSTool consist of various properties such as location identifier, data type, units,
and data arrays containing data values and flags.
To convert KiWIS data to time series requires joining KiWIS station, parameter, and time series list for metadata,
and time series values for data.

The TSTool main interface browsing tool displays joined information in order to list time series for selection.
The KiWIS `getTimeseriesList` service provides data for the TSTool time series list.
The ***Data type*** and ***Time step*** are general filters implemented for all datastores and the
***Where*** input filters are specific to KiWIS.

**<p style="text-align: center;">
![tstool-where](tstool-where.png)
</p>**

**<p style="text-align: center;">
TSTool Where Filters
</p>**

## Standard Time Series Properties ##

The general form of time series identifier used by TSTool is:

```
LocationID.DataSource.DataType.Interval~DatastoreName
```

If the `LocationID` does not result in a unique time series identifier,
the `LocType` can be used to avoid ambiguity by indicating the type of the location, as shown below:

```
LocType:LocationID.DataSource.DataType.Interval~DatastoreName
```

The standard time series identifier format for KiWIS web service time series is as follows,
where `KiWIS` is used for the data source.
Single quotes must be used if the `stationparameter_no` or `ts_shortname` include dash or period characters.
The TSTool TSID is similar to the KiWIS `ts_path` but does not use the `site_no`.

```
station_no.KiWIS.stationparameter_no-ts_shortname.ts_spacing~DataStoreName
station_no.KiWIS.'stationparameter_no'-'ts_shortname'.ts_spacing~DataStoreName
```

The following form uses `LocationType` and ensures a unique TSID.
However, using the `tsid_id` for the `LocationID` makes it difficult to
directly associate the time series with the station.
Single quotes must be used if the `stationparameter_no` or `ts_shortname` include dash or period characters.

```
ts_id:123456.KiWIS.stationparameter_no-ts_shortname.ts_spacing~DataStoreName
ts_id:123456.KiWIS.'stationparameter_no'-'ts_shortname'.ts_spacing~DataStoreName
```

The meaning of the TSID parts is as follows:

*   The `LocType` syntax with KiWIS `ts_id` ensures a unique TSID.
    However, the default TSID created by the TSTool main interface does not use the `ts_id` (described below).
*   The `LocationId` is set to:
    +   If `LocType` is `ts_id`:
        -   The KiWIS `ts_id` (integer) is used.
    +   If `LocType` syntax is not used:
        -   The KiWIS `station_no` (text) is used.
*   The `DataSource` is set to:
    +   The data source is currently always set to `KiWIS`.
    +   In the future it may be set to the entity from which data originates,
        such as the agency abbreviation.
    +   The `DataSource` is not currently used to uniquely identify time series
        because other KiWIS information is used.
*   The `DataType` is set to:
    +   KiWIS `stationparameter_no-ts_shortname` (`station_parameter`, a dash, and then `ts_shortname`).
    +   If the `station_parameter` contains a dash or period, surround it with single quotes.
    +   If the `ts_shortname` contains a dash or period, surround it with single quotes.
*   The `Interval` is set to:
    +   If the KiWIS `ts_spacing` is not blank,
        it is converted from the [ISO 8601 duration](https://en.wikipedia.org/wiki/ISO_8601)
        notation (e.g., `P1D` for daily interval) to the TSTool form (e.g., `1Day`).
    +   Only durations supported by TSTool are implemented:
        -   `Year` or `1Year`
        -   `Month` or `1Month`
        -   week or multiple day interval is not currently supported (can use `IrregDay`)
        -   `Day` or `1Day` (**not multiples of day**)
        -   `NHour` (multiple of hour are supported)
        -   `NMinute` (multiple of minute are supported)
        -   `IrregSecond` (irregular interval with date/time to second precision)
        -   other irregular intervals are supported and can be evaluated for use with KiWIS
    +   If the `ts_spacing` cannot be converted to an interval handled by TSTool,
        the `ts_spacing` is passed through and TSTool will not handle the time series.
    +   If the KiWIS `ts_spacing` is blank, TSTool will internally use `IrregSecond` for the interval
        because it has no other information.
    +   The interval is for information purposes and is not used to uniquely identify the time series
        because other KiWIS information is used.
*   The `DatastoreName` is taken from the datastore configuration file `Name` property:
    +   Multiple datastores can be configured, each pointing to a different KiWIS web service.
        Therefore, datastore names should be assigned with enough detail to avoid confusion.
        The following are typical examples:
        -   `KiWIS` - general datastore name but is ambiguous if more than one KiWIS system is accessed at the same time
        -   `KiWIS-northern` - example of KiWIS web services for Northern Water
        -   `KiWIS-northern-abc` - example of KiWIS web services for Northern Water (if second KiWIS server is available)

Additional mapping of KiWIS data to TSTool time series is as follows:

*   The `getTimeseriesValues` `Quality Code` is retrieved for each data value and is set as the data flag.
    The `getQualityCodes` service is used to retrieve the quality code data to allow conversion of numerical code to text.
    For example, a `Quality Code` value of `0` is converted to `Excellent`.

## Limitations ##

The following limitations and design issues have been identified during development of the KiWIS plugin.

1.  **Web service version:**
    1.  Is there a way to get the web service version?
    2.  If yes, then the `#@require` comment can be implemented to check the datastore version.
        This feature is useful to ensure that workflows are consistent with the datastore version.
        Currently this feature is not enabled and warn if inconsistent.
2.  **Location identifier:**
    1.  The KiWIS `ts_id` can be used to uniquely identify a time series,
        for example with the TSID format that uses location type syntax.
        However, the KiWIS `ts_id` is an internal database integer that does not have
        real-world meaning such as the station identifier.
    2.  The default implementation in TSTool is to use the `station_no` for the location,
        which makes it easier to relate the time series to its station.
        The data type is used with `station_no` to create a unique identifier,
        as described in the [Standard Time Series Properties](#standard-time-series-properties) section above.
3.  **Data type:**
    1.  The TSTool main interface and `ReadKWIS` command editor ***Data types*** contains the unique "data types" used by TSTool.
        KiWIS provides multiple multiple options for data type (typically called "paramerter" by KiWIS),
        some of which are more reeadable and verbose than others.
        For consistency, the `stationparameter_no` values are used, which are used in the
        `ts_path` and data type part of the TSTool TSID.
    2.  If feedback is that the `stationparameter_no` is not the best for data type,
        then the plugin code will need to be changed.
4.  **Unique time series identifier:** - The KiWIS `ts_path` provides a unique identifier that is similar to the TSTool TSID:
    1.  See item 2 above for **Location Identifier:**
    2.  The KiWIS `ts_path` contains `site_no`, `station_no`, `stationparameter_no`, and `ts_shortname`.
    3.  TSTool does not currently use the `site_no` in the TSID because `site_no` is used to group stations
        in a regional area, but `station_no` is typically unique without needing to use the `site_no`.
        The `site_no` might need to be used if a large system contains, for example,
        neighboring flood warning systems that use the same `station_no` in each system.
        The TSTool KiWIS design will be enhanced if this needs to be addressed.
5.  **Data interval:**
    1.  The KiWIS `ts_spacing` is often blank and therefore TSTool uses `IrregSecond` internally by default.
    2.  Blank `ts_spacing` values must be corrected in WISKI in order for TSTool to use the most appropriate time series interval.
    3.  The `getTimeseriesList` service does not allow filtering on the `ts_spacing` so the interval
        is mainly be used to allocate memory for the time series.
    4.  If a regular interval is not known for the time series, the plugin code cannot round the current time for queries.
        For example, it is generally useful to round the current time to an even interval.
    5.  Graphing irregular interval time series will result in lines being drawing over gaps
        whereas lines will not be drawn over gaps for regular interval time series.
6.  **Root URL**:
    1.  The datastore configuration file `ServiceRootURI` property currently specifies only the main server path
        and `?service=kisters&type=queryServices&datasource=0` is automatically added.
    2.  If this is not correct for all systems, the software may need to be updated to allow specifying
        these query parameters in the configuration file.
        The main question is probably about `datasource` (what does that mean)?
        **Will probably fix this before the production release.**
7.  **Quality code:**:
    1.  The `KiWIS Quality Code` field returned by the `getTimeseriesValues` service is an integer
        that must be converted to text using output from the `getQualityCodes` service.
        An integer is returned by the `getTimeseriesValues` service and the
        corresponding text code is a full word, such as `Excellent`.
    2.  Although full words are easy to read, they make it more difficult to visualize the data because the words overwrite on graphs.
        It may be possible to use a single character (or abbreviation) mapped to the numerical codes.
        This is more of an issue if other available data value flags are implemented in the future.
    3.  A quick review of some data shows that historical data has a flag of `Provisional`.
        The WISKI data should be reviewed to change the code to indicate validated data.
8.  **Time series manipulation:**
    1.  The KiWIS `getTimeseriesValues` service is quite complex and allows returning many fields
        and performing manipulations on the time series, such as aggregating and filling.
        Similar manipulations are provided by TSTool commands.
        The initital implementation of the TSTool KiWIS plugin does not attempt to use advanced
        features of the `getTimeSeriesValues` service.
        Allowing those query parameters in the `ReadKiWIS` command would allow, for example,
        comparing KiWIS calculations with TSTool calculations.
    2.  One complication is that because the manipulations occur "on the fly",
        additional work is needed to ensure that TSTool's representation of the time series is correct,
        such as the data type, interval, etc.
        Issues like the blank `ts_spacing` discussed above may impact more advanced features.
9.  **Caching:**
    1.  TSTool performance, in particular interactive features, is impacted by web service query times.
        Therefore, it is desirable to cache data in memory so that software does not need to requery web services.
        The trade-off is that when data are cached, changes in the WISKI database will not be visible in the TSTool
        session unless TSTool rereads the data.
        There is a balance betwee performance and having access to the most recent data.
    2.  Currently, TSTool does minimal caching needed to populate the interface choices,
        such as the data type list shown in the main user interface.
        The KiWIS `station_no` and `ts_shortname` are read from a cached list of time series
        when using the `ReadKiWIS` command editor to ensure fast performance.
        No time series list or data values are cached when reading time series.
        If performance or stale data are an issue, the implementation can be revisited.
    3.  If necessary, restart TSTool to use the latest KiWIS data.

## Datastore Configuration File ##

A datastore is configured by creating a datastore configuration file.

Create a user datastore configuration file `.tstool/NN/datastores/KiWIS-northern.cfg` (or similar) in the user's files,
for example by copying and modifying the following example, or copying from another installation.
The `NN` should agree with the major TSTool version, for example `14` as shown by the ***Help / About TSTool*** menu.
TSTool will attempt to open datastores for all enabled configuration files.

The following illustrates the KiWISDataStore datastore configuration file format
and configures a datastore named `KiWIS-northern`.
The `Name` property is the datastore name that will be used by the TSTool - the file name can be any name
but is often the same as the `Name` with extension `.cfg`.

```
# Configuration information for KiWISDataStore plugin datastore.
# This allows remote access to the KiWIS database using web services.
#
# The user will see the following when interacting with the datastore:
#
# Name - datastore name used in applications
# Description - datastore description for reports and user interfaces (short phrase)
#
# The following are needed to make database connection:
#
# Enabled - whether the datastore is enabled
# Type - must be KiWISDataStore
# Name - for example "KiWIS", "KiWIS-northern"
# ServiceRootURI - the web service root URI - specific services are appended

Enabled = True
#Enabled = False
Type = "KiWISDataStore"
Name = "KiWIS-northern"
Description = "Northern Water Kisters WISKI (KiWIS) web services"
ServiceRootURI = "https://data.northernwater.org/KiWIS/KiWIS"
ServiceApiDocumentationURI = "https://data.northernwater.org/KiWIS/KiWIS?datasource=0&service=kisters&type=queryServices&request=getrequestinfo"
```

**<p style="text-align: center;">
KiWIS Web Services DataStore Configuration File
</p>**

## See Also 

* [`ReadDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadDelimitedFile/ReadDelimitedFile/) command
* [`ReadKiWIS`](../../command-ref/ReadKiWIS/ReadKiWIS.md) command
* [`ReadTableFromDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromDelimitedFile/ReadTableFromDelimitedFile/) command
* [`ReadTableFromJSON`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromJSON/ReadTableFromJSON/) command
* [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/) command
