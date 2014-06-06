/*
 * PackratPackageInfo.java
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
package org.rstudio.studio.client.packrat.model;

import org.rstudio.studio.client.workbench.views.packages.model.PackageInfo;

public class PackratPackageInfo extends PackageInfo
{
   protected PackratPackageInfo()
   {
   }

   public final native String getPackratVersion() /*-{
      return this["packrat.version"];
   }-*/;
   
   public final native String getPackratSource() /*-{
      return this["packrat.source"];
   }-*/;
   
   public final native boolean getCurrentlyUsed() /*-{
      return this["currently.used"];
   }-*/;

   public final native boolean getInPackratLibary() /*-{
      return this["in.packrat.library"];
   }-*/;
}