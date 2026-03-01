//package org.example.komatoro;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.env.AbstractEnvironment;
//import org.springframework.core.env.Environment;
//import org.springframework.core.env.PropertySource;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EnvironmentDebug implements ApplicationRunner {
//
//    @Autowired
//    private Environment environment;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        System.out.println("=== ACTIVE PROFILES ===");
//        for (String profile : environment.getActiveProfiles()) {
//            System.out.println("Profile: " + profile);
//        }
//
//        System.out.println("=== PROPERTIES ===");
//        System.out.println("server.port: " + environment.getProperty("server.port"));
//        System.out.println("app.name: " + environment.getProperty("app.name"));
//
//        System.out.println("=== ALL PROPERTY SOURCES ===");
//        for (PropertySource<?> source : ((AbstractEnvironment) environment).getPropertySources()) {
//            System.out.println("Source: " + source.getName());
//        }
//    }
//}
