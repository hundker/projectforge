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

package org.projectforge.continuousdb;

import org.apache.commons.lang3.StringUtils;
import org.projectforge.common.DatabaseDialect;

/**
 * All database dialect specific implementations should be placed here.
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DatabaseSupport
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseSupport.class);

  private static boolean errorMessageShown = false;

  private static DatabaseSupport instance;

  private final DatabaseDialect dialect;

  public static void setInstance(final DatabaseSupport instance)
  {
    DatabaseSupport.instance = instance;
  }

  public static DatabaseSupport getInstance()
  {
    return instance;
  }

  public DatabaseSupport(final DatabaseDialect dialect)
  {
    log.info("Setting database dialect to: " + dialect.getAsString());
    this.dialect = dialect;
  }

  public DatabaseDialect getDialect()
  {
    return dialect;
  }

  /**
   * Optimization for getting sum of durations. Currently only an optimization for PostgreSQL is implemented:
   * "extract(epoch from sum(toProperty - fromProperty))". <br/>
   * If no optimization is given, the caller selects all database entries and aggregates via Java the sum (full table
   * scan).
   *
   * @param fromProperty
   * @param toProperty
   * @return part of select string or null if for the used database no optimization is given.
   */
  public String getIntervalInSeconds(final String fromProperty, final String toProperty)
  {
    if (dialect == DatabaseDialect.PostgreSQL) {
      return "EXTRACT(EPOCH FROM SUM(" + toProperty + " - " + fromProperty + "))"; // Seconds since 1970
    } else {
      if (!errorMessageShown) {
        errorMessageShown = true;
        log.warn(
            "No database optimization implemented for the used database. Please contact the developer if you have an installation with more than 10.000 time sheet entries for increasing performance");
      }
      // No optimization for this database.
      return null;
    }
  }

  // PK INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY

  /**
   * For Hypersoniq "GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY" is returned if the primary key
   * should be generated by the database, otherwise an empty string. <br/>
   * Expected result for Hypersoniq:
   * " <pk col name> INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY"
   */
  public String getPrimaryKeyAttributeSuffix(final TableAttribute primaryKey)
  {
    if (dialect == DatabaseDialect.HSQL && primaryKey.isGenerated()) {
      return " GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY";
    }
    return "";
  }

  /**
   * For Hypersoniq an empty string is returned if the pk has to be generated by Hypersonic, otherwise
   * ",\n  PRIMARY KEY (<pk col name>)".
   */
  public String getPrimaryKeyTableSuffix(final TableAttribute primaryKey)
  {
    if (dialect == DatabaseDialect.HSQL && primaryKey.isGenerated()) {
      return "";
    }
    return ",\n  PRIMARY KEY (" + primaryKey.getName() + ")";
  }

  public String getType(final TableAttribute attr)
  {
    switch (attr.getType()) {
      case CHAR:
        return "CHAR(" + attr.getLength() + ")";
      case VARCHAR:
        return "VARCHAR(" + attr.getLength() + ")";
      case BOOLEAN:
        return "BOOLEAN";
      case INT:
        if (dialect == DatabaseDialect.PostgreSQL) {
          return "INT4";
        } else {
          return "INT";
        }
      case LONG:
        if (dialect == DatabaseDialect.PostgreSQL) {
          return "INT8";
        } else {
          return "BIGINT";
        }
      case SHORT:
        return "SMALLINT";
      case TIMESTAMP:
        return "TIMESTAMP";
      case LOCALE:
        return "VARCHAR(255)";
      case DATE:
        return "DATE";
      case DECIMAL:
        return "DECIMAL(" + attr.getPrecision() + ", " + attr.getScale() + ")";
      case BINARY:
        if (dialect == DatabaseDialect.PostgreSQL) {
          return "BYTEA";
        } else {
          return "LONGVARBINARY";
        }
      default:
        throw new UnsupportedOperationException(
            "Type '" + attr.getType() + "' not supported for the current database dialect: " + dialect);
    }
  }

  public void addDefaultAndNotNull(final StringBuffer buf, final TableAttribute attr)
  {
    if (dialect != DatabaseDialect.HSQL) {
      if (!attr.isNullable()) {
        buf.append(" NOT NULL");
      }
      if (StringUtils.isNotBlank(attr.getDefaultValue())) {
        buf.append(" DEFAULT(").append(attr.getDefaultValue()).append(")");
      }
    } else {
      if (StringUtils.isNotBlank(attr.getDefaultValue())) {
        buf.append(" DEFAULT '").append(attr.getDefaultValue()).append("'");
      }
      if (!attr.isNullable()) {
        buf.append(" NOT NULL");
      }
    }
  }

  public String renameAttribute(final String table, final String oldName, final String newName)
  {
    final StringBuilder buf = new StringBuilder();
    buf.append("ALTER TABLE ").append(table).append(" ");
    if (dialect == DatabaseDialect.HSQL) {
      buf.append("ALTER COLUMN ").append(oldName).append(" RENAME TO ").append(newName);
    } else {
      buf.append("RENAME COLUMN ").append(oldName).append(" TO ").append(newName);
    }
    return buf.toString();
  }

  public String alterTableColumnVarCharLength(final String table, final String attribute, final int length)
  {
    if (dialect == DatabaseDialect.PostgreSQL) {
      return "ALTER TABLE " + table.toUpperCase() + " ALTER COLUMN " + attribute.toUpperCase() + " TYPE varchar(" + length + ")";
    } else {
      return "ALTER TABLE " + table.toLowerCase() + " ALTER COLUMN " + attribute.toLowerCase() + " varchar(" + length + ")";
    }
  }

  /**
   * Will be called on shutdown by WicketApplication.
   */
  public String getShutdownDatabaseStatement()
  {
    if (dialect == DatabaseDialect.HSQL) {
      return "SHUTDOWN COMPACT";
    } else {
      return null;
    }
  }

  public String createSequence(final String name)
  {
    // Only needed by PostgreSQL for creating the hibernate sequence.
    if (dialect == DatabaseDialect.PostgreSQL) {
      return "CREATE SEQUENCE " + name + " START 1";
    } else if (dialect == DatabaseDialect.HSQL) {
      // Not yet used, hibernate sequence not required.
      return "CREATE SEQUENCE " + name + " START WITH 1";
    }
    return null;
  }

  public String getQueryForAllUniqueConstraintNames()
  {
    if (dialect == DatabaseDialect.PostgreSQL) {
      return "SELECT conname FROM pg_constraint WHERE conrelid = (SELECT oid FROM pg_class WHERE LOWER(relname) = ? and contype='u');";
    } else if (dialect == DatabaseDialect.HSQL) {
      return "SELECT LOWER(CONSTRAINT_NAME) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE='UNIQUE' AND LOWER(TABLE_NAME) = ?;";
    }
    return null;
  }
}
