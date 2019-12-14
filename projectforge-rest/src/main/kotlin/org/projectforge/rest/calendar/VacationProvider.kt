/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.rest.calendar

import org.projectforge.business.calendar.CalendarStyleMap
import org.projectforge.business.vacation.repository.VacationDao
import org.projectforge.framework.calendar.Holidays
import org.projectforge.framework.persistence.user.entities.GroupDO
import org.projectforge.framework.time.PFDateTime

/**
 * Provides the vacation days of the employees. You may filter the vacation by ProjectForge groups.
 */
object VacationProvider {
    private val log = org.slf4j.LoggerFactory.getLogger(VacationProvider::class.java)
    private val holidays = Holidays.getInstance()

    fun addEvents(vacationDao: VacationDao,
                  start: PFDateTime,
                  end: PFDateTime,
                  events: MutableList<BigCalendarEvent>,
                  styleMap: CalendarStyleMap,
                  /**
                   * Vacation days will only be displayed for employees (users) who are member of at least one of the following groups:
                   */
                  groups: List<GroupDO>) {
        val vacations = vacationDao.getVacationForPeriodAndGroups(start, end, groups)
        vacations.forEach {
            val bgColor= "#ffa500"
            val fgColor= "#ffffff"

            events.add(BigCalendarEvent(
                    title = it.employee?.user?.getFullname(),
                    start = it.startDate!!,
                    end = it.endDate!!,
                    allDay = true,
                    category = "vacation",
                    bgColor = bgColor,
                    fgColor = fgColor,
                    dbId = it.id))

        }
    }
}
