/*
 * Copyright 2014 Bernd Vogt and others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sourcepit.common.testing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author Bernd
 */
public final class Environment {
   private static final Map<String, Environment> environments = new HashMap<String, Environment>();

   public static Environment getSystem() {
      return get(null);
   }

   public static Environment get(String path) {
      synchronized (environments) {
         Environment environment = environments.get(path);
         if (environment == null) {
            environment = newEnvironment(path);
            environments.put(path, environment);
         }
         return environment;
      }
   }

   public static Environment newEnvironment(String path) {
      final Properties properties = new Properties();
      properties.putAll(System.getProperties());
      if (path != null) {
         properties.putAll(loadProperties(path));
      }
      return newEnvironment(new LinkedHashMap<String, String>(System.getenv()), properties);
   }

   public static Environment newEnvironment(final Map<String, String> envs, final Properties properties) {
      for (Entry<Object, Object> entry : properties.entrySet()) {
         String key = entry.getKey().toString();
         if (key.startsWith("env.") && key.length() > 4) {
            key = key.substring(4);
            final Object value = entry.getValue();
            envs.put(key, value == null ? null : value.toString());
         }
      }

      return new Environment(envs, properties);
   }

   private final Map<String, String> envs;
   private final Properties properties;

   private static Properties loadProperties(String path) {
      Properties properties = new Properties();
      ClassLoader cl = Environment.class.getClassLoader();
      InputStream is = cl.getResourceAsStream(path);
      if (is != null) {
         try {
            try {
               properties.load(is);
            }
            finally {
               is.close();
            }
         }
         catch (IOException e) {
            throw new IllegalStateException(e);
         }
      }
      return properties;
   }

   private Environment(Map<String, String> envs, Properties properties) {
      this.envs = envs;
      this.properties = properties;
   }

   public Map<String, String> newEnvs() {
      final Map<String, String> envs = new LinkedHashMap<String, String>(this.envs);

      final String javaagent = properties.getProperty("javaagent");
      if (javaagent != null) {
         String mvnOpts = envs.get("MAVEN_OPTS");
         if (mvnOpts == null) {
            mvnOpts = javaagent;
         }
         else {
            mvnOpts = (mvnOpts + " " + javaagent).trim();
         }
         envs.put("MAVEN_OPTS", mvnOpts);
      }

      return envs;
   }

   public Properties newProperties() {
      final Properties props = new Properties();
      props.putAll(properties);
      return props;
   }

   public String getProperty(String name) {
      return properties.getProperty(name);
   }

   public String getProperty(String name, String defaultValue) {
      return properties.getProperty(name, defaultValue);
   }

   public String getProperty(String name, boolean required) {
      final String value = properties.getProperty(name);
      if (required && value == null) {
         throw new IllegalStateException("Property " + name + " is required but not set.");
      }
      return value;
   }

   public File getPropertyAsFile(String name) {
      final String path = getProperty(name, false);
      if (path == null) {
         return null;
      }
      return new File(path);
   }

   public File getPropertyAsFile(String name, boolean required) {
      return new File(getProperty(name, required));
   }

   public File getUserHome() {
      return getPropertyAsFile("user.home", true);
   }

   public File getBuildDir() {
      return getPropertyAsFile("build.dir", true);
   }

   public File getResourcesDir() {
      return getPropertyAsFile("resources.dir", true);
   }

   public File getMavenHome() {
      String mvnHome = getProperty("maven.home");
      if (mvnHome != null) {
         return new File(mvnHome);
      }

      mvnHome = getEnv("M3_HOME", "M2_HOME", "MVN_HOME", "MAVEN_HOME");
      if (mvnHome != null) {
         return new File(mvnHome);
      }

      final String paths = envs.get("PATH");
      if (paths != null) {
         final File mavenHome = getMavenHome(paths);
         if (mavenHome != null) {
            return mavenHome;
         }
      }

      return null;
   }

   private String getEnv(String... names) {
      for (String name : names) {
         String env = envs.get(name);
         if (env != null) {
            return env;
         }
      }
      return null;
   }

   private File getMavenHome(String paths) {
      final File m2ConfFile = findFileInPaths(paths, "m2.conf");
      return m2ConfFile == null ? null : m2ConfFile.getParentFile().getParentFile();
   }

   private File findFileInPaths(String paths, final String name) {
      for (String path : paths.split(File.pathSeparator)) {
         final File file = new File(path, name);
         if (file.exists()) {
            return file;
         }
      }
      return null;
   }

   public boolean isDebugAllowed() {
      return Boolean.TRUE.toString().equals(getProperty("debug.allowed"));
   }

   public File getJavaHome() {
      return getPropertyAsFile("java.home", false);
   }
}
