/*
 * YamlTree.java
 *
 * Copyright (C) 2009-14 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.rmarkdown.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class YamlTree
{
   private class YamlTreeNode
   {
      public YamlTreeNode(String line)
      {
         yamlLine = line;
         key = getKey(line);
         indentLevel = getIndentLevel();
      }
      
      public void addChild(YamlTreeNode child)
      {
         children.add(child);
         child.parent = this;
      }
      
      public String getIndent()
      {
         RegExp whitespace = RegExp.compile("^\\s*");
         MatchResult result = whitespace.exec(yamlLine);
         if (result == null)
            return "";
         else
            return result.getGroup(0);
      }
      
      public String getValue()
      {
         int idx = yamlLine.indexOf(":");
         if (idx < 0)
            return "";
         return yamlLine.substring(idx + 2);
      }
      
      public void setValue(String value)
      {
         int idx = yamlLine.indexOf(":");
         if (idx < 0)
            return;
         yamlLine = yamlLine.substring(0, idx + 2) + value;
      }
      
      public String yamlLine;
      public String key;
      public int indentLevel = 0;
      public YamlTreeNode parent = null;
      public List<YamlTreeNode> children = new ArrayList<YamlTreeNode>();
      
      private String getKey(String line)
      {
         RegExp keyReg = RegExp.compile("^\\s*([^:]+):");
         MatchResult result = keyReg.exec(line);
         if (result == null)
            return "";
          else
            return result.getGroup(1);
      }

      private int getIndentLevel()
      {
         return getIndent().length();
      }
   }
   
   public YamlTree(String yaml)
   {
      root_ = createYamlTree(yaml);
      keyMap_ = new HashMap<String, YamlTreeNode>();
      createKeyMap(root_, keyMap_);
   }
   
   public String toString()
   {
      return yamlFromTree(root_);
   }
   
   public void reorder(List<String> orderedKeys)
   {
      // Sort the YAML lines into a tree
      if (!keyMap_.containsKey(orderedKeys.get(0)))
         return;
      YamlTreeNode parent = keyMap_.get(orderedKeys.get(0)).parent;
      
      // Move around subtrees to match the given list of ordered keys 
      // (swap subtrees into a position matching that specified)
      for (int i = 0; i < orderedKeys.size(); i++)
      {
         for (int j = i; j < parent.children.size(); j++)
         {
            YamlTreeNode child = parent.children.get(j);
            if (orderedKeys.get(i) == child.key)
            {
               YamlTreeNode previousChild = parent.children.get(i);
               parent.children.set(i, child);
               parent.children.set(j, previousChild);
            }
         }
      }
   }

   public List<String> getChildKeys(String parentKey)
   {
      if (!keyMap_.containsKey(parentKey))
         return null;
      YamlTreeNode parent = keyMap_.get(parentKey);
      ArrayList<String> result = new ArrayList<String>();
      for (YamlTreeNode child: parent.children)
      {
         result.add(child.key);
      }
      return result;
   }
   
   public void addYamlValue(String parentKey, String key, String value)
   {
      String line = key + ": " + value;
      if (keyMap_.containsKey(parentKey))
      {
         YamlTreeNode parent = keyMap_.get(parentKey);
         YamlTreeNode child = 
               new YamlTreeNode(parent.getIndent() + "  " + line);
         keyMap_.put(key, child);
         parent.addChild(child);
      }
   }
   
   public String getKeyValue(String key)
   {
      if (keyMap_.containsKey(key))
         return keyMap_.get(key).getValue();
      return "";
   }

   public void setKeyValue(String key, String value)
   {
      if (keyMap_.containsKey(key))
         keyMap_.get(key).setValue(value);
   }

   private YamlTreeNode createYamlTree(String yaml)
   {
      String[] lines = yaml.split("\n");
      YamlTreeNode root = new YamlTreeNode("");
      root.indentLevel = -2;
      YamlTreeNode currentParent = root;
      YamlTreeNode lastNode = root;
      int currentIndentLevel = 0;
      for (String line: lines)
      {
         YamlTreeNode child = new YamlTreeNode(line);
         if (child.indentLevel > currentIndentLevel)
         {
            // Descending: we're recording children of the previous line
            currentParent = lastNode;
         }
         else if (child.indentLevel < currentIndentLevel)
         {
            // Ascending: find the first parent node in the hierarchy that has
            // an indent level less than this node's
            do
            {
               currentParent = currentParent.parent;
            }
            while (currentParent.indentLevel >= child.indentLevel);
         }

         currentIndentLevel = child.indentLevel;
         currentParent.addChild(child);
         lastNode = child;
      }
      return root;
   }
   
   private static String yamlFromTree(YamlTreeNode root)
   {
      String yaml = "";
      for (YamlTreeNode child: root.children)
      {
         yaml += (child.yamlLine + '\n');
         yaml += yamlFromTree(child);
      }
      return yaml;
   }
   
   private static void createKeyMap(YamlTreeNode root, 
                                    Map<String, YamlTreeNode> output)
   {
      for (YamlTreeNode child: root.children)
      {
         output.put(child.key, child);
         createKeyMap(child, output);
      }
   }
   
   private final Map<String, YamlTreeNode> keyMap_;
   private final YamlTreeNode root_;
}
