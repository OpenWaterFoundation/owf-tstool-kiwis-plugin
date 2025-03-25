# TSTool / KiWIS Data Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.

*   [TSTool core product release notes](http://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/).
*   [TSTool Version Compatibility](#tstool-version-compatibility)
*   [Release Note Details](#release-note-details)

----

## TSTool Version Compatibility ##

The following table lists TSTool and plugin software version compatibility.

**<p style="text-align: center;">
TSTool and Plugin Version Compatibility
</p>**

| **Plugin Version** | **Required TSTool Version** | **Comments** |
| -- | -- | -- |
| 2.0.0 | >=  15.0.0 | TSTool and plugin updated to Java 11, new plugin manager. |
| 1.1.2 | >= 14.6.0 | |
| 1.1.1 | >= 14.5.4 | |
| 1.1.0 | >= 14.5.3 | |

## Release Note Details ##

*   [Version 2.0.0](#version-200)
*   [Version 1.1.2](#version-112)
*   [Version 1.1.1](#version-111)
*   [Version 1.1.0](#version-110)
*   [Version 1.0.0](#version-100)

----------

## Version 2.0.0 ##

**Major release to use Java 11.**

*   ![change](change.png) Update the plugin to use Java 11:
    +   The Java version is consistent with TSTool 15.0.0.
    *   The plugin installation now uses a version folder,
        which allows multiple versions of the plugin to be installed at the same time,
        for use with different versions of TSTool.

## Version 1.1.2 ##

**Maintenance release to enable command indentation consistent with TSTool 14.6.0.**

**This plugin version requires at least TSTool 14.6.0.**

*   ![change](change.png) [1.1.2] The [`ReadKiWIS`](../command-ref/ReadKiWIS/ReadKiWIS.md) command has been updated:
    +   The command can be indented in command files as per TSTool 14.6.0 conventions.

## Version 1.1.1 ##

**Maintenance release to fix issues discovered during testing.**

*   ![bug](bug.png) [1.1.1] Enable handling of interpolation type numbers 103, 203, and 303,
    with behavior similar to interpolation types 403, 503, 603, and 703.
    This controls whether the KiWIS timestamp for interval time series is at the beginning or the end of the interval.
*   ![bug](bug.png) [1.1.1] Enable handling of interpolation type numbers 104, 204, and 304,
    with behavior similar to interpolation types 404, 504, 604, and 704.
    This controls whether the KiWIS timestamp for interval time series is at the beginning or the end of the interval.
*   ![bug](bug.png) [1.1.1] Previously, time series identifiers (TSIDs) similar to `'stationparameter_no'-ts_shortname`
    (single quotes around the first part) resulted in errors.  This has been fixed.
    **This requires updating to at least TSTool 14.5.4.**

## Version 1.1.0 ##

**Feature release - enable features including up to daily interval.**

**This plugin version requires at least TSTool version 14.5.3.**

*   ![change](change.png) [1.1.0] The [Datastore documentation](../datastore-ref/KiWIS/KiWIS.md)
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

## Version 1.0.0 ##

**Feature release - initial production release.**

*   ![new](new.png) [1.0.0] Initial production release:
    +   Main TSTool window includes browsing features to list KiWIS time series.
    +   [TSID for KiWIS](../command-ref/TSID/TSID.md) are recognized to read time series with default parameters.
    +   The [`ReadKiWIS`](../command-ref/ReadKiWIS/ReadKiWIS.md) command is provided to automate
        reading 1+ time series.
    +   Documentation is available for the [KiWIS datastore](../datastore-ref/KiWIS/KiWIS.md).
