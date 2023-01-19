// QualityCode - results from getQualityCodes service

/* NoticeStart

OWF TSTool KiWIS Plugin
Copyright (C) 2022-2023 Open Water Foundation

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

/**
 * KiWIS "getQualityCodes" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class QualityCode {
	/**
	 * "key" - internal ID?
	 */
	private Integer key = null;
	
	/**
	 * "code" - one word quality value (e.g., "Excellent")
	 */
	private String code = "";
	
	/**
	 * "description" - longer phrase (e.g., "Data approved as excellent")
	 */
	private String description = "";

	/**
	 * "color" - color string for UI (e.g., "rgba(46, 139, 85, 1.0)")
	 */
	private String color = "";
	
	/**
	 * Default constructor used by Jackson.
	 */
	public QualityCode () {
		
	}

	/**
	 * Return the code.
	 * @return the code. 
	 */
	public String getCode () {
		return this.code;
	}
	
	/**
	 * Return the color.
	 * @return the color. 
	 */
	public String getColor () {
		return this.color;
	}
	
	/**
	 * Return the quality code description.
	 * @return the quality code description
	 */
	public String getDescription () {
		return this.description;
	}
	
	/**
	 * Return the station primary key.
	 */
	public Integer getKey () {
		return this.key;
	}
	
	/**
	 * Return a string representation of the object, useful for troubleshooting.
	 */
	public String toString() {
		return "key=" + this.key + " code=\"" + this.code + "\" description=\"" +
			this.description + "\" color=\"" + this.color + "\"";
	}
	
}
