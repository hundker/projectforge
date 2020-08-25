/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.rest.dto

import org.projectforge.business.humanresources.HRPlanningDO
import org.projectforge.business.humanresources.HRPlanningEntryDO
import java.math.BigDecimal
import java.time.LocalDate


class HRPlanning(
        var week: LocalDate? = null,
        var formattedWeekOfYear: String? = null,
        var totalHours: BigDecimal? = null,
        var totalUnassignedHours: BigDecimal? = null,
        var user: User? = null,
        var entries: MutableList<HRPlanningEntryDO>? = mutableListOf()
) : BaseDTO<HRPlanningDO>() {

    /**
     * @see copyFrom
     */
    constructor(src: HRPlanningDO) : this() {
        copyFrom(src)
    }

    override fun copyFrom(src: HRPlanningDO) {
        super.copyFrom(src)
        formattedWeekOfYear = src.formattedWeekOfYear
        totalHours = src.totalHours
        totalUnassignedHours = src.totalUnassignedHours
        this.user = src.user?.let {
            User(it)
        }

        if(src.entries == null || src.entries!!.isEmpty()){
            val entry = HRPlanningEntryDO()
            entries!!.add(entry)
        } else {
            entries!!.addAll(src.entries!!)
        }
    }
}
