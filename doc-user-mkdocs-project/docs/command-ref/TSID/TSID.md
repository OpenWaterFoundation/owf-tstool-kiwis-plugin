# TSTool / Command / TSID for KiWIS #

* [Overview](#overview)
* [Command Editor](#command-editor)
* [Command Syntax](#command-syntax)
* [Examples](#examples)
* [Troubleshooting](#troubleshooting)
* [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for KiWIS causes a time series to be read from KiWIS web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [KiWIS Datastore Appendix](../../datastore-ref/KiWIS/KiWIS.md) for information about TSID syntax.

See also the [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md) command,
which reads one or more time series.

## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [KiWIS Datastore Appendix](../../datastore-ref/KiWIS/KiWIS.md) for information about TSID syntax.

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-kiwis-plugin/tree/master/test/commands/TSID/).

## Troubleshooting ##

## See Also ##

* [`ReadKiWIS`](../ReadKiWIS/ReadKiWIS.md) command for full control reading KiWIS time series
* [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
