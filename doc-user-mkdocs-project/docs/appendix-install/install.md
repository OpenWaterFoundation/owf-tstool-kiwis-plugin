# TSTool / Install KiWIS Plugin #

This appendix describes how to install and configure the TSTool KiWIS Plugin.

* [Install TSTool](#install-tstool)
* [Install and Configure the TSTool KiWIS Web Services Plugin](#install-and-configure-the-tstool-kiwis-web-services-plugin)

-------

## Install TSTool ##

TSTool must be installed before installing the KiWIS plugin.
Typically the latest stable release should be used, although a development version can be installed
in order to use new features.
Multiple versions of TSTool can be installed at the same time.

1.  Download TSTool:
    *   Download the Windows version from the
        [State of Colorado's TSTool Software Downloads](https://opencdss.state.co.us/tstool/) page.
    *   Download the Linux version from the
        [Open Water Foundation TSTool download page](https://software.openwaterfoundation.org/tstool/).
2.  Run the installer and accept defaults.
3.  Run TSTool once by using the ***Start / CDSS / TSTool-Version*** menu on Windows
    (or run the `tstool` program on Linux).
    This will automatically create folders needed to install the plugin.

## Install and Configure the TSTool KiWIS Web Services Plugin ##

TSTool must have been previously installed and run at least once.

1.  Download the `tstool-kiwis-plugin` software installer file from the
    [TSTool KiWIS Download page](https://software.openwaterfoundation.org/tstool-kwis-plugin/).
    For example with a name similar to `owf-tstool-kiwis-plugin-1.0.0.jar`.
2.  If installing the plugin in user files and if TSTool was not run before,
    run TSTool once to automatically create user folders and files needed by the plugin.
3.  Copy the `jar` file to the following folder:
    1.  If installing in user files (`~` indicates user's home folder on Linux and `NN` is the TSTool major version):
        *   Windows: `C:\Users\user\.tstool\NN\plugins\owf-tstool-kiwis-plugin\`
        *   Linux: `~/.tstool/NN/plugins/owf-tstool-kiwis-plugin/`
    2.  If installing in system files on Linux (`opt`), install in the following folder:
        *   Windows: `C:\CDSS\TSTool-Version\plugins\owf-tstool-kiwis-plugin\`
        *   Linux: `/opt/tstool-version/plugins/owf-tstool-kiwis-plugin/`
4.  If an old version of the plugin was previous installed,
    delete the old `jar` file or move to the `plugins-old/` folder (same level as the `plugins` folder) to archive.
    Only one copy of the plugin `jar` file can be found in the `plugins` folder to avoid software conflicts.
    If the KiWIS plugin features are not functioning properly, it may be due to conflicting jar files.
    The ***Commands(Plugin)*** menu will usually contain duplicate menus if multiple `jar` files are found in the `plugins` folder.
5.  Configure one or more datastore configuration files according to the
    [KiWIS Data Web Services Datastore](../datastore-ref/KiWIS/KiWIS.md#datastore-configuration-file) documentation.
6.  Test web services access using TSTool by selecting the datastore name that was configured and selecting time series.
7.  If there are issues, use the ***View / Datastores*** menu item to list enabled datastores.
8.  If necessary, see the [Troubleshooting](../troubleshooting/troubleshooting.md) documentation.

TSTool will be enhanced in the future to provide a "plugin manager" to help with these tasks.
