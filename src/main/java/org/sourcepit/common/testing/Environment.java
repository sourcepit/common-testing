/**
 * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.common.testing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Bernd
 */
public final class Environment
{
   private static final class Holder
   {
      private final static Environment INSTANCE = new Environment();
   }

   public static Environment getInstance()
   {
      return Holder.INSTANCE;
   }

   public static Environment getSystemEnvironment()
   {
      return new Environment(System.getenv(), System.getProperties());
   }

   private final Map<String, String> envs;
   private final Properties properties;

   private Environment()
   {
      this(System.getenv(), loadProperties());
   }

   private static Properties loadProperties()
   {
      Properties properties = new Properties();
      ClassLoader cl = Environment.class.getClassLoader();
      InputStream is = cl.getResourceAsStream("osgiy-its.properties");
      if (is != null)
      {
         try
         {
            try
            {
               properties.load(is);
            }
            finally
            {
               is.close();
            }
         }
         catch (IOException e)
         {
            throw new IllegalStateException(e);
         }
      }
      return properties;
   }

   public Environment(Map<String, String> envs, Properties properties)
   {
      this.envs = envs;
      this.properties = properties;
   }

   public Map<String, String> newEnvironmentVariables()
   {
      final Map<String, String> environment = new HashMap<String, String>(System.getenv());

      final String mavenDir = getProperty("maven-dir");
      if (mavenDir != null)
      {
         environment.put("M2_HOME", mavenDir);
      }

      final String mavenOpts = getProperty("maven-opts");
      if (mavenOpts != null)
      {
         environment.put("MAVEN_OPTS", mavenOpts);
      }

      final String javaagent = System.getProperty("javaagent");
      if (javaagent != null)
      {
         String mvnOpts = environment.get("MAVEN_OPTS");
         if (mvnOpts == null)
         {
            mvnOpts = javaagent;
         }
         else
         {
            mvnOpts = (mvnOpts + " " + javaagent).trim();
         }
         environment.put("MAVEN_OPTS", mvnOpts);
      }

      String userHome = getProperty("user-home");
      if (userHome != null)
      {
         userHome = "-Duser.home=" + userHome;
         String mvnOpts = environment.get("MAVEN_OPTS");
         if (mvnOpts == null)
         {
            mvnOpts = userHome;
         }
         else
         {
            mvnOpts = (mvnOpts + " " + userHome).trim();
         }
         environment.put("MAVEN_OPTS", mvnOpts);
      }

      final String javaDir = getProperty("java-dir");
      if (javaDir != null)
      {
         environment.put("JAVA_HOME", javaDir);
      }
      else
      {
         environment.put("JAVA_HOME", System.getProperty("java.home"));
      }

      return environment;
   }

   public String getProperty(String name)
   {
      return properties.getProperty(name);
   }

   public String getProperty(String name, String defaultValue)
   {
      return properties.getProperty(name, defaultValue);
   }

   public String getProperty(String name, boolean required)
   {
      final String value = properties.getProperty(name);
      if (required && value == null)
      {
         throw new IllegalStateException("Property " + name + " is required but not set.");
      }
      return value;
   }

   public File getPropertyAsFile(String name)
   {
      final String path = getProperty(name, false);
      if (path == null)
      {
         return null;
      }
      return new File(path);
   }

   public File getPropertyAsFile(String name, boolean required)
   {
      return new File(getProperty(name, required));
   }

   public File getUserHome()
   {
      return getPropertyAsFile("user.home", true);
   }

   public File getOutputDir()
   {
      return getPropertyAsFile("output-dir", true);
   }

   public File getMavenDir()
   {
      return getPropertyAsFile("maven-dir", true);
   }

   public File getMavenHome()
   {
      String mvnHome = getProperty("maven.home");
      if (mvnHome != null)
      {
         return new File(mvnHome);
      }

      final String paths = envs.get("PATH");
      if (paths != null)
      {
         final File mavenHome = getMavenHome(paths);
         if (mavenHome != null)
         {
            return mavenHome;
         }
      }

      mvnHome = getEnv("M3_HOME", "M2_HOME", "MVN_HOME", "MAVEN_HOME");
      if (mvnHome != null)
      {
         return new File(mvnHome);
      }

      return null;
   }

   private String getEnv(String... names)
   {
      for (String name : names)
      {
         String env = envs.get(name);
         if (env != null)
         {
            return env;
         }
      }
      return null;
   }

   private File getMavenHome(String paths)
   {
      final File m2ConfFile = findFileInPaths(paths, "m2.conf");
      return m2ConfFile == null ? null : m2ConfFile.getParentFile().getParentFile();
   }

   private File findFileInPaths(String paths, final String name)
   {
      for (String path : paths.split(File.pathSeparator))
      {
         final File file = new File(path, name);
         if (file.exists())
         {
            return file;
         }
      }
      return null;
   }

   public boolean isDebugAllowed()
   {
      return Boolean.TRUE.toString().equals(getProperty("debug-allowed"));
   }

   public File getJavaHome()
   {
      return getPropertyAsFile("java.home", false);
   }
}
