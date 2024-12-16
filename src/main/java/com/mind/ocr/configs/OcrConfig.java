package com.mind.ocr.configs;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bpo-ocr")
public class OcrConfig {

    private String baseFolder;
    private List<ApplicationConfig> applications;

    // Getters and Setters for baseFolder
    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    // Getters and Setters for applications
    public List<ApplicationConfig> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationConfig> applications) {
        this.applications = applications;
    }

    // Inner class ApplicationConfig
    public static class ApplicationConfig {
        private String name;
        private String incoming;
        private String outgoing;
        private String errorneous;
        private String outputFormat;
        private String log;
        private String language;
        private String user;
        private String split; // New field
        private String searchable; // New field

        // Getters and Setters for all fields
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIncoming() {
            return incoming;
        }

        public void setIncoming(String incoming) {
            this.incoming = incoming;
        }

        public String getOutgoing() {
            return outgoing;
        }

        public void setOutgoing(String outgoing) {
            this.outgoing = outgoing;
        }

        public String getErrorneous() {
            return errorneous;
        }

        public void setErrorneous(String errorneous) {
            this.errorneous = errorneous;
        }

        public String getOutputFormat() {
            return outputFormat;
        }

        public void setOutputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
        }

        public String getLog() {
            return log;
        }

        public void setLog(String log) {
            this.log = log;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
        
        public String getSplit() {
            return split;
        }

        public void setSplit(String split) {
            this.split = split;
        }

        public String getSearchable() {
            return searchable;
        }

        public void setSearchable(String searchable) {
            this.searchable = searchable;
        }
    }
}
