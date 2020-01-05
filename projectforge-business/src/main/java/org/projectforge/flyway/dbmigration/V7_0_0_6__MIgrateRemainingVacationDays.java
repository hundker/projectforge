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

package org.projectforge.flyway.dbmigration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.projectforge.framework.time.PFDateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Since ProjectForge version 7, remaining vacation days will be handled in a new table instead of handling as attribute of employee (without year info).
 */
public class V7_0_0_6__MIgrateRemainingVacationDays extends BaseJavaMigration {
  private static Logger log = LoggerFactory.getLogger(V7_0_0_6__MIgrateRemainingVacationDays.class);

  @Override
  public void migrate(Context context) throws Exception {
    log.info("Trying to migrate employees carried vacation days from previous year, if this feature is used.");
    DataSource ds = context.getConfiguration().getDataSource();
    JdbcTemplate jdbc = new JdbcTemplate(ds);

    // After installation of version 7 (snapshot), the cronjob for updating remaining vacation days yearly failed due to a bug in EmployeeAttrDO.parent (Kotlin-NPE)
    // So get the installation year:
    Timestamp release7InstalledOn = (Timestamp)jdbc.queryForObject("select installed_on from t_flyway_schema_version where version='7.0.0'", Timestamp.class);
    int release7InstalledOnYear = LocalDateTime.ofInstant(release7InstalledOn.toInstant(), PFDateTimeUtils.ZONE_UTC).getYear();

    SqlRowSet rs = jdbc.queryForRowSet("select p.modifiedat as modifiedat,p.value as value,e.pk as pk,e.tenant_id as tenant from t_fibu_employee_attr as p inner join t_fibu_employee as e on p.parent=e.pk where propertyname = 'previousyearleave' order by pk");
    int counter = 0;
    Date now = new Date();
    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(ds).withTableName("t_employee_vacation_remaining");
    while (rs.next()) {
      ++counter;
      Timestamp modifiedat = rs.getTimestamp("modifiedat");
      Integer employeeId = rs.getInt("pk");
      Integer tenantId = rs.getInt("tenant");
      BigDecimal value = rs.getBigDecimal("value");
      int year = LocalDateTime.ofInstant(modifiedat.toInstant(), PFDateTimeUtils.ZONE_UTC).getYear();
      if (year > release7InstalledOnYear) {
        year = release7InstalledOnYear; // Vacations were modified, but the attribute 'previousyearleave' is not of the current year.
      }
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("pk", counter);
      parameters.put("deleted", false);
      parameters.put("created",now);
      parameters.put("last_update", now);
      parameters.put("tenant_id", tenantId);
      parameters.put("employee_id", employeeId);
      parameters.put("year", year);
      parameters.put("pk", counter);
      parameters.put("carry_vacation_days_from_previous_year", value);
      simpleJdbcInsert.execute(parameters);
    }
    if (counter > 0) {
      log.info("Number of successful migrated entries: " + counter);
    } else {
      log.info("No vacation entries found to migrate (OK, if vacation feature wasn't used.)");
    }
  }
}