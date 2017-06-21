/**
 Copyright 2017 John Kester (Jack Kester)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.github.slidekb.back;

import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;

import com.github.slidekb.api.PlatformSpecific;
import com.github.slidekb.api.PluginVersion;
import com.github.slidekb.api.SlideBarPlugin;
import com.github.slidekb.util.CurrentWorkingDirectoryClassLoader;
import com.github.slidekb.util.OsHelper;

public class PluginManager {

    private ArrayList<SlideBarPlugin> proci = new ArrayList<>();
    private CountDownLatch pluginsLoaded = new CountDownLatch(1);

    public PluginManager() {

    }

    /**
     * Adds plugins into the list and instances each.
     * 
     * @return true if successful.
     */
    protected boolean loadProcesses(int programVersion) {
        proci.clear();

        ServiceLoader<SlideBarPlugin> loader = ServiceLoader.load(SlideBarPlugin.class, CurrentWorkingDirectoryClassLoader.getCurrentWorkingDirectoryClassLoader());

        for (SlideBarPlugin currentImplementation : loader) {
            PluginVersion currentVersion = currentImplementation.getClass().getAnnotation(PluginVersion.class);

            if (currentVersion == null) {
                System.out.println("Found plugin " + currentImplementation.getClass().getCanonicalName() + " but it has no version annotation! Skipping.");
                continue;
            } else if (currentVersion.value() != programVersion) {
                System.out.println("Found plugin " + currentImplementation.getClass().getCanonicalName() + " but its version " + currentVersion.value() + " doesn't match program version " + programVersion + "! Skipping.");
                continue;
            } else {
                PlatformSpecific currentAnnotation = currentImplementation.getClass().getAnnotation(PlatformSpecific.class);

                if (currentAnnotation != null) { // Annotation present -> platform specific plugin
                    if (currentAnnotation.value() == OsHelper.getOS()) {
                        System.out.println("Loading platform dependant plugin " + currentImplementation.getClass().getCanonicalName() + " for platform " + OsHelper.getOS());
                        currentImplementation.setSliderManager(MainBack.getSlideMan());
                        proci.add(currentImplementation);
                    }
                } else { // No Annotation -> platform independent plugin
                    System.out.println("Loading platform independant plugin " + currentImplementation.getClass().getCanonicalName());
                    currentImplementation.setSliderManager(MainBack.getSlideMan());

                    proci.add(currentImplementation);
                }
            }
        }

        pluginsLoaded.countDown();
        return true;
    }

    public void waitUntilProcessesLoaded() throws InterruptedException {
        pluginsLoaded.await();
    }

    public ArrayList<SlideBarPlugin> getProci() {
        return proci;
    }

    protected void removeProci(boolean RemoveAll) {
        if (RemoveAll) {
            proci.removeAll(proci);
        }
    }
}
