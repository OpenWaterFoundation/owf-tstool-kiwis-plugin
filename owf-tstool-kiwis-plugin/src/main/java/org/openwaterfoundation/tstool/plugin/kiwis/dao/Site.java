// Site - results from getSiteList service

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

package org.openwaterfoundation.tstool.plugin.kiwis.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KiWIS "getSiteList" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Site {
	/**
	 * "site_name"
	 */
	@JsonProperty("site_name")
	private String siteName = "";
	
	/**
	 * "site_no"
	 */
	@JsonProperty("site_no")
	private String siteNo = "";
	
	/**
	 * "site_id"
	 */
	@JsonProperty("site_id")
	private String siteId = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Site () {
		
	}

	/**
	 * Return the site identifier.
	 * @return the site identifier. 
	 */
	public String getSiteId () {
		return this.siteId;
	}

	/**
	 * Return the site name.
	 * @return the site name. 
	 */
	public String getSiteName () {
		return this.siteName;
	}
	
	/**
	 * Return the site number.
	 * @return the site number. 
	 */
	public String getSiteNo () {
		return this.siteNo;
	}
	
}