package com.cmbellis.caffeevento.core.main;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by chris on 8/10/16.
 */
public class CoreApplication {

    private static ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();


    public static void main(String[] argv) throws Exception
    {
        // Print welcome banner.
        System.out.println("\nWelcome to My Launcher");
        System.out.println("======================\n");

        Properties configProps = new Properties();
        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, "./bundle");
        configProps.setProperty(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY, "install,update,start,uninstall");
        configProps.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        configProps.setProperty(Constants.FRAMEWORK_STORAGE, "cache");

        try
        {
            final Framework m_fwk = new FrameworkFactory().newFramework(configProps);
            m_fwk.init();
            AutoProcessor.process(configProps, m_fwk.getBundleContext());
//            scheduledExecutor.scheduleAtFixedRate(() -> printBundles(m_fwk.getBundleContext()), 5, 1, TimeUnit.SECONDS);
            m_fwk.start();
            m_fwk.waitForStop(0);
            System.exit(0);
        }
        catch (Exception ex)
        {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static void printBundles(BundleContext context) {
        Stream.of(context.getBundles())
                .forEach(System.out::println);
        System.out.println("-----------------");
    }

}
