# TSTool KiWIS Plugin / Introduction #

*   [Introduction](#introduction)
*   [TSTool use with KiWIS Web Services](#tstool-use-with-kiwis-web-services)

----------------------

## Introduction ##

TSTool is a powerful software tool that automates time series processing and product generation.
It was originally developed for the State of Colorado to process data for basin modeling and has since
been enhanced to work with many data sources including:

*   United States Geological Survey (USGS) web service and file formats
*   Natural Resources Conservation Service (NRCS) web services
*   Regional Climate Center (RCC) Applied Climate Information Service (ACIS) web services
*   US Army Corps of Engineers DSS data files
*   others

TSTool is maintained by the Open Water Foundation,
which also enhances the software based on project needs.

*   See the latest [TSTool Documentation](https://opencdss.state.co.us/tstool/latest/doc-user/) to learn about core TSTool features.
*   See the [TSTool Download website](https://opencdss.state.co.us/tstool/) for the most recent software versions and documentation.
*   See the [KiWIS Plugin Download website](https://software.openwaterfoundation.org/tstool-kiwis-plugin/).

## TSTool use with KiWIS Web Services ##

KiWIS Web Services provide access to data that are maintained in the
[Kisters WISKI system](https://www.kisters.net/NA/products/wiski/).
See the following resources:

*   [KiWIS Web Service Documentation](https://data.northernwater.org/KiWIS/KiWIS?datasource=0&service=kisters&type=queryServices&request=getrequestinfo)

The [KiWIS datastore documentation](../datastore-ref/KiWIS/KiWIS.md) describes how TSTool integrates with KiWIS.

The [`ReadKiWIS`](../command-ref/ReadKiWIS/ReadKiWIS.md) command can be used to read time series,
in addition to time series identifiers that are generated from the main TSTool interface.
