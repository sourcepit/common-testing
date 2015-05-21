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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.Test;

/**
 * @author Bernd Vogt <bernd.vogt@sourcepit.org>
 */
public class EnvironmentTest {

   @Test
   public void testSystemProperties() {
      Environment env = Environment.newEnvironment(System.getenv(), System.getProperties());
      assertThat(env, IsNull.notNullValue());

      File userHome = env.getUserHome(); // prop
      assertThat(userHome, IsEqual.equalTo(new File(System.getProperty("user.home"))));

      File javaHome = env.getJavaHome(); // prop
      assertThat(javaHome, IsEqual.equalTo(new File(System.getProperty("java.home"))));
   }

   @Test
   public void testMavenHome() {
      Map<String, String> envs = new HashMap<String, String>();
      Properties properties = new Properties();
      Environment env = Environment.newEnvironment(envs, properties);

      // null
      assertThat(env.getMavenHome(), IsNull.nullValue());

      // PATH
      File file = new File("src/test/resources/maven.home/bin/");
      assertTrue(file.exists());

      envs.put("PATH", "foo" + File.pathSeparator + file.getPath() + File.pathSeparator + "murks/bin");
      assertThat(env.getMavenHome(), IsEqual.equalTo(file.getParentFile()));

      // ENV
      envs.put("MAVEN_HOME", "maven-home-1");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-1")));

      envs.put("MVN_HOME", "maven-home-2");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-2")));

      envs.put("M2_HOME", "maven-home-3");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-3")));

      envs.put("M3_HOME", "maven-home-4");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("maven-home-4")));

      // props
      properties.setProperty("maven.home", "foo");
      assertThat(env.getMavenHome(), IsEqual.equalTo(new File("foo")));
   }

   @Test
   public void testDefault() {
      Environment systemEnv = Environment.getSystem();
      Environment defaultEnv = Environment.get(null);
      assertThat(systemEnv, IsSame.sameInstance(defaultEnv));

      Map<String, String> envs = systemEnv.newEnvs();
      for (Entry<String, String> entry : System.getenv().entrySet()) {
         if ("MAVEN_OPTS".equals(entry.getKey()) && System.getProperty("javaagent") != null) {
            assertThat(entry.getValue() + " " + System.getProperty("javaagent"),
               IsEqual.equalTo(envs.get(entry.getKey())));
         }
         else {
            assertThat(entry.getValue(), IsEqual.equalTo(envs.get(entry.getKey())));
         }
      }

      Map<Object, Object> props = systemEnv.newProperties();
      for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
         assertThat(entry.getValue(), IsEqual.equalTo(props.get(entry.getKey())));
      }
   }

   @Test
   public void testEnvProperty() {
      Map<String, String> envs = new HashMap<String, String>();
      envs.put("FOO", "bar");
      envs.put("USER", "yoda");

      Properties props = new Properties();
      props.put("foo", "bar");
      props.put("env.MURKS", "pfusch");
      props.put("env.USER", "luke");

      Environment env = Environment.newEnvironment(envs, props);

      assertThat(env.newEnvs().size(), Is.is(3));
      assertThat(env.newEnvs().get("FOO"), IsEqual.equalTo("bar"));
      assertThat(env.newEnvs().get("MURKS"), IsEqual.equalTo("pfusch"));
      assertThat(env.newEnvs().get("USER"), IsEqual.equalTo("luke"));

      assertThat(3, Is.is(env.newProperties().size()));
      assertThat(env.newProperties().getProperty("foo"), IsEqual.equalTo("bar"));
      assertThat(env.newProperties().getProperty("env.MURKS"), IsEqual.equalTo("pfusch"));
      assertThat(env.newProperties().getProperty("env.USER"), IsEqual.equalTo("luke"));
   }

}
