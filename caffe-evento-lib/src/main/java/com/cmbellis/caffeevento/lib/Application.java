package com.cmbellis.caffeevento.lib;

import com.cmbellis.caffeevento.lib.annotation.CEExport;
import com.cmbellis.caffeevento.lib.api.services.Service;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by chris on 8/21/16.
 */
public class Application {
    public static void main(String[] argv) throws Exception {
        // Print welcome banner.
        System.out.println("\nWelcome to CaffeEvento");
        System.out.println("Powered by Apache Felix!");
        System.out.println("======================\n");

        // Get all the lib stuff together
        Reflections reflections = new Reflections("com.cmbellis.caffeevento");
        List<String> libPackages = reflections
                .getTypesAnnotatedWith(CEExport.class)
                .stream()
                .map(Class::getPackage)
                .map(Package::getName)
                .collect(Collectors.toList());

        // Provide a few packages that might be used by services
        List<String> providedPackages = Arrays.asList("org.apache.commons.logging;version=1.2.0");
        libPackages.addAll(providedPackages);

        String export = libPackages.stream()
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));

        Properties configProps = new Properties();
        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, "./bundle");
        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY, "install,update,start,uninstall");
        configProps.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        configProps.setProperty(Constants.FRAMEWORK_STORAGE, "cache");
        configProps.setProperty(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, export);

        try {
            final Framework framework = new FrameworkFactory().newFramework(configProps);
            framework.init();
            AutoProcessor.process(configProps, framework.getBundleContext());

            framework.getBundleContext().addBundleListener(bundleEvent -> {
                if (bundleEvent.getBundle() != null && bundleEvent.getBundle().getRegisteredServices() != null) {
                    for (ServiceReference<?> serviceReference : bundleEvent.getBundle().getRegisteredServices()) {
                        final ServiceObjects<?> serviceObjects =
                                bundleEvent.getBundle().getBundleContext().getServiceObjects(serviceReference);
                        Object s = serviceObjects.getService();

                        if (s instanceof Service) {
                            if(bundleEvent.getType() == BundleEvent.STARTED) {
                                Service newService = (Service) s;
                                System.out.println("Service started: " + newService.getClass());
                            }
                        }
                    }
                }
            });

            framework.start();
            framework.waitForStop(0);
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

}
