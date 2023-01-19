// ParameterType - results from getParameterTypeList service

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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KiWIS "getParameterTypeList" object.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ParameterType {
	/**
	 * "parametertype_name"
	 */
	@JsonProperty("parametertype_name")
	private String parameterTypeName = "";
	
	/**
	 * "parametertype_id"
	 */
	@JsonProperty("parametertype_id")
	private String parameterTypeId = "";
	
	/**
	 * Constructor needed by Jackson.
	 */
	public ParameterType ( ) {
	}

	/**
	 * Return the parameter type name.
	 * @return the parameter type name. 
	 */
	public String getParameterTypeName () {
		return this.parameterTypeName;
	}
	
	/**
	 * Return the parameter type number.
	 * @return the parameter type number. 
	 */
	public String getParameterTypeId () {
		return this.parameterTypeId;
	}
	
}
