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

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Bernd
 */
public class Workspace extends ExternalResource {
   private String path;

   private File baseDir, workspaceDir;

   private boolean delete;

   public Workspace() {
      this(null, true);
   }

   public Workspace(File baseDir, String path, boolean delete) {
      this(new File(baseDir, path), delete);
   }

   public Workspace(File baseDir, boolean delete) {
      this.baseDir = baseDir;
      this.delete = delete;
   }

   public Statement apply(Statement base, Description description) {
      String className = description.getClassName();
      int idx = className.lastIndexOf('.');
      if (idx > -1) {
         className = className.substring(idx + 1);
      }
      path = className + "/" + description.getMethodName();
      return super.apply(base, description);
   }

   @Override
   protected void before() throws Exception {
      if (baseDir == null) {
         workspaceDir = newDir();
      }
      else {
         workspaceDir = new File(baseDir, path);
         if (workspaceDir.exists()) {
            delete();
         }
         workspaceDir.mkdirs();
      }
   }

   @Override
   protected void after() {
      if (delete) {
         delete();
      }
      super.after();
   }

   public void delete() {
      try {
         FileUtils.deleteDirectory(workspaceDir);
      }
      catch (IOException e) {
      }
   }

   /**
    * @return the location of this workspace directory.
    */
   public File getRoot() {
      if (workspaceDir == null) {
         throw new IllegalStateException("the workspace directory has not yet been created");
      }
      return workspaceDir;
   }

   /**
    * Returns a new fresh file with the given name under the workspace directory.
    */
   public File newFile(String fileName) throws IOException {
      File file = new File(getRoot(), fileName);
      if (!file.getParentFile().exists()) {
         file.getParentFile().mkdirs();
      }
      file.createNewFile();
      return file;
   }

   /**
    * Returns a new fresh file with a random name under the workspace directory.
    */
   public File newFile() throws IOException {
      return File.createTempFile("file", null, workspaceDir);
   }

   /**
    * Returns a new fresh directory with the given name under the workspace directory.
    */
   public File newDir(String... dirNames) {
      File file = getRoot();
      for (String dirName : dirNames) {
         file = new File(file, dirName);
         file.mkdir();
      }
      return file;
   }

   /**
    * Returns a new fresh directory with a random name under the workspace directory.
    */
   public File newDir() throws IOException {
      File createdDir = File.createTempFile("junit", "", workspaceDir);
      createdDir.delete();
      createdDir.mkdir();
      return createdDir;
   }

   public File importFileOrDir(File file) throws IOException {
      if (file.isDirectory()) {
         return importDir(file);
      }
      return importFile(file);
   }

   public File importFile(File srcFile) throws IOException {
      final File destFile = newFile(srcFile.getName());
      FileUtils.forceDelete(destFile);
      FileUtils.copyFile(srcFile, destFile);
      return destFile;
   }

   public File importDir(File dir) throws IOException {
      final File dst = newDir(dir.getName());
      FileUtils.forceDelete(dst);
      FileUtils.copyDirectory(dir, dst);
      return dst;
   }

}
