package org.projectforge.rest.dto

import org.projectforge.business.humanresources.HRPlanningEntryDO
import org.projectforge.common.i18n.Priority
import java.math.BigDecimal

class HRPlanningEntry(
        var projektNameOrStatus: String? = null,
        var priority: Priority? = null,
        var probability: Int? = null,
        var totalHours: BigDecimal? = null,
        var unassignedHours: BigDecimal? = null,
        var mondayHours: BigDecimal? = null,
        var tuesdayHours: BigDecimal? = null,
        var wednesdayHours: BigDecimal? = null,
        var thursdayHours: BigDecimal? = null,
        var fridayHours: BigDecimal? = null,
        var weekendHours: BigDecimal? = null,
        var description: String? = null
): BaseDTO<HRPlanningEntryDO>() {
    var planning: HRPlanning? = HRPlanning()
    var projekt: Projekt? = Projekt()

    fun initialize(obj: HRPlanningEntryDO){
        copyFrom(obj)

        if(obj.planning != null){
            planning!!.initialize(obj.planning!!)
        }

        if(obj.projekt != null){
            projekt!!.initialize(obj.projekt!!)
        }

        projektNameOrStatus = obj.projektNameOrStatus
        totalHours = obj.totalHours
        unassignedHours = obj.unassignedHours
    }
}