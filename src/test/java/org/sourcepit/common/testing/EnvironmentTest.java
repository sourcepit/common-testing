/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.common.testing;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Test;

/**
 * @author Bernd Vogt <bernd.vogt@sourcepit.org>
 */
public class EnvironmentTest
{

   @Test
   public void testSystemProperties()
   {
      Environment env = new Environment(System.getenv(), System.getProperties());
      assertThat(env, IsNull.notNullValue());

      File userHome = env.getUserHome(); // prop
      assertThat(userHome, IsEqual.equalTo(new File(System.getProperty("user.home"))));

      File javaHome = env.getJavaHome(); // prop
      assertThat(javaHome, IsEqual.equalTo(new File(System.getProperty("java.home"))));
   }

   @Test
   public void testMavenHome()
   {
      Map<String, String> envs = new HashMap<String, String>();
      Properties properties = new Properties();
      Environment env = new Environment(envs, properties);

      // null
      assertThat(env.getMavenHome(), IsNull.nullValue());

      
      // ENV
      envs.put("MAVEN_HOME", "maven-home-1");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-1")));

      envs.put("MVN_HOME", "maven-home-2");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-2")));

      envs.put("M2_HOME", "maven-home-3");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-3")));

      envs.put("M3_HOME", "maven-home-4");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-4")));


      // PATH
      File file = new File("src/test/resources/maven.home/bin/");
      assertTrue(file.exists());

      envs.put("PATH", "foo" + File.pathSeparator + file.getPath() + File.pathSeparator + "murks/bin");
      assertThat(env.getMavenHome(), IsEqual.equalTo(file.getParentFile()));


      // props
      properties.setProperty("maven.home", "foo");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("foo")));
   }


}
