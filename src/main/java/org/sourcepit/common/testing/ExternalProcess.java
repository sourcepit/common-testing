/**
 * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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

public class ExternalProcess extends ExternalResource
{
   private ProcessDestroyerImpl processDestroyer;

   @Override
   protected void before() throws Throwable
   {
      super.before();
      processDestroyer = new ProcessDestroyerImpl();
   }

   public DefaultExecutor newExecutor(File workingDir)
   {
      DefaultExecutor executor = newExecutor();
      executor.setWorkingDirectory(workingDir);
      return executor;
   }

   public DefaultExecutor newExecutor()
   {
      final DefaultExecutor executor = new DefaultExecutor();
      executor.setProcessDestroyer(processDestroyer);
      executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
      return executor;
   }

   public CommandLine newCommandLine(File executable, String... arguments)
   {
      final CommandLine cmd = new CommandLine(executable);
      addArguments(cmd, arguments);
      return cmd;
   }

   public CommandLine newCommandLine(String executable, String... arguments)
   {
      final CommandLine cmd = new CommandLine(executable);
      addArguments(cmd, arguments);
      return cmd;
   }

   protected void addArguments(final CommandLine cmd, String... arguments)
   {
      if (arguments != null)
      {
         cmd.addArguments(arguments);
      }
   }

   public int execute(Map<String, String> environment, File workingDir, String executable, String... arguments)
      throws IOException
   {
      final CommandLine command = newCommandLine(executable, arguments);
      return execute(environment, workingDir, command);
   }

   public int execute(Map<String, String> environment, File workingDir, File executable, String... arguments)
      throws IOException
   {
      final CommandLine command = newCommandLine(executable, arguments);
      return execute(environment, workingDir, command);
   }

   public int execute(Map<String, String> environment, File workingDir, CommandLine command) throws IOException
   {
      final DefaultExecutor executor = newExecutor(workingDir);
      return executor.execute(command, environment);
   }

   @Override
   protected void after()
   {
      destroy();
      super.after();
   }

   public void destroy()
   {
      processDestroyer.destroy();
   }

   private static class ProcessDestroyerImpl implements ProcessDestroyer
   {
      private final List<Process> processes = new ArrayList<Process>();

      public synchronized boolean add(Process process)
      {
         return processes.add(process);
      }

      public synchronized boolean remove(Process process)
      {
         return processes.remove(process);
      }

      public synchronized int size()
      {
         return processes.size();
      }

      public synchronized void destroy()
      {
         for (Process process : processes)
         {
            try
            {
               process.destroy();
            }
            catch (Throwable t)
            {
               System.err.println("Unable to terminate process during process shutdown");
            }
         }
      }
   }
}
