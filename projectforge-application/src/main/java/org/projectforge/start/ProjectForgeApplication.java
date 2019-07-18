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

package org.projectforge.start;

import org.projectforge.framework.time.DateHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TimeZone;

@SpringBootApplication(
        scanBasePackages = {"org.projectforge", "de.micromata.mgc.jpa.spring"},
        // Needed for Spring 5.5, exception was:
        // java.lang.ClassCastException: org.springframework.orm.jpa.EntityManagerHolder cannot be cast to org.springframework.orm.hibernate5.SessionHolder
        exclude = HibernateJpaAutoConfiguration.class
)
@ServletComponentScan({"org.projectforge.web", "org.projectforge.business.teamcal.servlet"})
public class ProjectForgeApplication {
  static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectForgeApplication.class);

  private static final String ADDITIONAL_LOCATION_ARG = "--spring.config.additional-location=";

  public static void main(String[] args) throws Exception {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion != null && javaVersion.compareTo("1.9") >= 0) {
      log.error("******************************************************************************************************************************************");
      log.error("***** ProjectForge doesn't support versions higher than Java 1.8!!!! Please downgrade. Sorry, we're working on newer Java versions. ******");
      log.error("******************************************************************************************************************************************");
    }
    args = addDefaultAdditionalLocation(args);
    System.setProperty("user.timezone", "UTC");
    TimeZone.setDefault(DateHelper.UTC);
    SpringApplication.run(ProjectForgeApplication.class, args);
  }

  /**
   * For easier configuration, ProjectForge adds ${user.home}/ProjectForge/projectforge.properties as
   * --spring.config.additional-location.
   * <br/>
   * If the additional-location is already given in the args, nothing will done by this method.
   *
   * @param args The main args.
   * @return The main args extended by the new --spring.config.additional-location if not already given.
   */
  static String[] addDefaultAdditionalLocation(String[] args) {
    boolean configFound = false;
    if (checkConfiguration("config", "application-default.properties")) configFound = true;
    if (checkConfiguration("config", "application.properties")) configFound = true;
    if (checkConfiguration(".", "application-default.properties")) configFound = true;
    if (checkConfiguration(".", "application.properties")) configFound = true;
    if (configFound) {
      File propertiesFile = getAddtionLocation();
      if (propertiesFile.exists()) {
        log.warn("Ignoring configuration '" + propertiesFile.getAbsolutePath() + "', configuration in current directory already found (see messages above).");
      }
      // Do nothing (configuration found in current directory:
      return args;
    }
    boolean additionalLocationFound = false;
    if (args == null || args.length == 0) {
      args = new String[]{getAddtionalLocationArg()};
    } else {
      for (String arg : args) {
        if (arg != null && arg.startsWith(ADDITIONAL_LOCATION_ARG)) {
          additionalLocationFound = true;
          break;
        }
      }
      if (!additionalLocationFound) {
        args = Arrays.copyOf(args, args.length + 1);
        args[args.length - 1] = getAddtionalLocationArg();
      }
    }
    if (!additionalLocationFound) {
      File file = getAddtionLocation();
      if (file.exists()) {
        log.info("Configuration from '" + file.getAbsolutePath() + "' will be used.");
      }
    }
    return args;
  }

  /**
   * $HOME is replaced by the user's home.
   * @return --spring.config.additional-location=file:/$HOME/ProjectForge/projectforge.properties
   */
  static String getAddtionalLocationArg() {
    return ADDITIONAL_LOCATION_ARG + "file:" + getAddtionLocation().getAbsolutePath();
  }

  static File getAddtionLocation() {
    return Paths.get(System.getProperty("user.home"), "ProjectForge", "projectforge.properties").toFile();
  }

  private static boolean checkConfiguration(String dir, String filename) {
    File file = new File(dir, filename);
    if (file.exists()) {
      log.info("Configuration from '" + file.getAbsolutePath() + "' will be used.");
      return true;
    }
    return false;
  }
}
