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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.rules.ExternalResource;

public class ExternalProcess extends ExternalResource {
   private ProcessDestroyerImpl processDestroyer;

   @Override
   protected void before() throws Throwable {
      super.before();
      processDestroyer = new ProcessDestroyerImpl();
   }

   public DefaultExecutor newExecutor(File workingDir) {
      DefaultExecutor executor = newExecutor();
      executor.setWorkingDirectory(workingDir);
      return executor;
   }

   public DefaultExecutor newExecutor() {
      final DefaultExecutor executor = new DefaultExecutor();
      executor.setProcessDestroyer(processDestroyer);
      executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
      return executor;
   }

   public CommandLine newCommandLine(File executable, String... arguments) {
      final CommandLine cmd = new CommandLine(executable);
      addArguments(cmd, arguments);
      return cmd;
   }

   public CommandLine newCommandLine(String executable, String... arguments) {
      final CommandLine cmd = new CommandLine(executable);
      addArguments(cmd, arguments);
      return cmd;
   }

   protected void addArguments(final CommandLine cmd, String... arguments) {
      if (arguments != null) {
         cmd.addArguments(arguments);
      }
   }

   public int execute(Map<String, String> environment, File workingDir, String executable, String... arguments)
      throws IOException {
      final CommandLine command = newCommandLine(executable, arguments);
      return execute(environment, workingDir, command);
   }

   public int execute(Map<String, String> environment, File workingDir, File executable, String... arguments)
      throws IOException {
      final CommandLine command = newCommandLine(executable, arguments);
      return execute(environment, workingDir, command);
   }

   public int execute(Map<String, String> environment, File workingDir, CommandLine command) throws IOException {
      final DefaultExecutor executor = newExecutor(workingDir);
      return executor.execute(command, environment);
   }

   @Override
   protected void after() {
      destroy();
      super.after();
   }

   public void destroy() {
      processDestroyer.destroy();
   }

   private static class ProcessDestroyerImpl implements ProcessDestroyer {
      private final List<Process> processes = new ArrayList<Process>();

      public synchronized boolean add(Process process) {
         return processes.add(process);
      }

      public synchronized boolean remove(Process process) {
         return processes.remove(process);
      }

      public synchronized int size() {
         return processes.size();
      }

      public synchronized void destroy() {
         for (Process process : processes) {
            try {
               process.destroy();
            }
            catch (Throwable t) {
               System.err.println("Unable to terminate process during process shutdown");
            }
         }
      }
   }
}
