# TSTool / KiWIS Data Web Services Plugin / Release Notes #

* [Changes in Version 1.1.0](#changes-in-version-110)
* [Changes in Version 1.0.0](#changes-in-version-100)

----------

## Changes in Version 1.1.0 ##

**Feature release - enable features including up to daily interval.**

**This plugin version requires at least TSTool version 14.5.3.**

*   ![new](change.png) [1.1.0] The [Datastore documentation](../datastore-ref/KiWIS/KiWIS.md)
    has been updated to reflect updates:
    +   The `ServiceRootURI` configuration file property must now contain everything prior to the service name,
        which provides flexibility for different systems.
    +   Documentation has been clarified based on feedback from Kisters technical support.
*   ![change](change.png) [1.1.0] General changes:
    +   If the KiWIS `ts_spacing` is blank, the TSTool `IrregSecond` interval is used.
        This ensures that TSTool will properly handle the time series.
    +   The TSTool main interface now lists available time series data intervals and the interval
        can be used to filter the time series list.
    +   Checks have been added to detect unsupported requests:
        -   Data interval larger than day is not supported, pending additional information about KiWIS web services.
        -   Invalid requests, such as using `Read24HourAsDay` with non-daily, are checked.
*   ![change](change.png) [1.1.0] The [`ReadKiWIS`](../command-ref/ReadKiWIS/ReadKiWIS.md) command has been updated:
    +   The web services documentation for the system can now be viewed from the command.
    +   Time series data intervals are listed and can be used to limit the time series list that is read.
    +   The `IrregularInterval` parameter has been added to allow reading regular interval time series
        as irregular, in order to preserve KiWIS timestamps.
        The output time series TSID will reflect the parameter.
    +   The `Read24HourAsDay` and `ReadDayAs24Hour` parameters have been added to allow
        control of how to handle zero hour midnight for dates and dates with time.
        The output time series TSID will reflect the parameter.

## Changes in Version 1.0.0 ##

**Feature release - initial production release.**

*   ![new](new.png) [1.0.0] Initial production release:
    +   Main TSTool window includes browsing features to list KiWIS time series.
    +   [TSID for KiWIS](../command-ref/TSID/TSID.md) are recognized to read time series with default parameters.
    +   The [`ReadKiWIS`](../command-ref/ReadKiWIS/ReadKiWIS.md) command is provided to automate
        reading 1+ time series.
    +   Documentation is available for the [KiWIS datastore](../datastore-ref/KiWIS/KiWIS.md).
