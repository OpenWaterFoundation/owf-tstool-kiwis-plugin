# TSTool / Command / TSID for KiWIS #

* [Overview](#overview)
* [Command Editor](#command-editor)
* [Command Syntax](#command-syntax)
* [Examples](#examples)
* [Troubleshooting](#troubleshooting)
* [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for KiWIS causes a single time series to be read from KiWIS web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [KiWIS Datastore Appendix](../../datastore-ref/KiWIS/KiWIS.md) for information about TSID syntax.

See also the [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md) command,
which reads one or more time series and provides parameters for control over how data are read.

The TSTool KiWIS plugin automatically manipulates time series timestamps to be consistent
with TSTool, as follows:

*   Irregular interval time series:
    +   use timestamps from KiWIS web services without changing
*   Regular interval time series:
    +   if necessary, timestamps are adjusted from beginning of interval to end of interval
        based on the KiWIS interpolation type
    +   day interval time series use dates from the day previous to KiWIS timestamp
        midnight zero hour (time is discarded);
        for example, the KiWIS timestamp 2022-02-02T00:00:00 becomes 2022-02-01
    +   run-time warnings are generated for unsupported intervals or cases;
        use the [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md)
        command parameters `IrregularInterval`, `Read24HourAsDay`, and `ReadDayAs24Hour`
        
Warning:  If a [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md) command was used with parameters that cause a
change in the time series identifier, for example, reading KiWIS `1Day` interval time series
as `24Hour`, the resulting TSID cannot be used to read time series because it does not match a KiWIS TSID.
KiWIS TSID commands should only be generated from the TSTool time series list
or matching time series in the list.

## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [KiWIS Datastore Appendix](../../datastore-ref/KiWIS/KiWIS.md) for information about TSID syntax.

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-kiwis-plugin/tree/master/test/commands/TSID/).

## Troubleshooting ##

*   See the [`ReadKiWIS` command troubleshooting](../ReadKiWIS/ReadKiWIS.md#troubleshooting) documentation.

## See Also ##

*   [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md) command for full control reading KiWIS time series
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
