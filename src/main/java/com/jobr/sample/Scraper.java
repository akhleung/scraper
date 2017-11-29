package com.jobr.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.post;

public class Scraper {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("Starting job scraper on port 8080");
        initExceptionHandler(e -> {
            System.out.println("ERROR: Couldn't initialize job scraper; make sure port 8080 is free");
            System.exit(1);
        });
        port(8080);
        post("/get_jobs", Scraper::handleGetJobs);
    }

    private static String handleGetJobs(Request request, Response response) {

        // use a parallel stream to fetch the pages concurrently -- for real-world code, it might be better to use
        // completable futures instead, so that we can have more fine-grained control over the parallelism level
        ArrayList<JobInfo> jobInfos = readUrls(request.body())
                .parallelStream()
                .map(Scraper::getJobDocument)
                .map(Scraper::extractJobInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        try {
            response.type("application/json");
            response.header("Content-Encoding", "gzip");
            return mapper.writer().writeValueAsString(jobInfos);
        } catch (JsonProcessingException e) {
            System.out.println("Error serializing job info: " + e.getMessage());
            response.status(500);
            return e.getMessage();
            // in real-world code, we should create a hierarchy of domain-specific exceptions, handle each kind,
            // and return appropriate status codes and error messages to the client
        }
    }

    // deserialize the list of urls in the incoming request
    private static List<String> readUrls(String json) {
        try {
            return new ArrayList<>(Arrays.asList(mapper.readValue(json, String[].class)));
        } catch (IOException e) {
            System.out.println("Error deserializing list of URLs");
            return Collections.emptyList();
        }
    }

    // fetch the job listing from Indeed.com and parse it
    private static Pair<String, Document> getJobDocument(String url) {
        try {
            System.out.println("Getting job listing at " + url);
            return Pair.of(url, Jsoup.connect(url).get());
        } catch (IOException e) {
            System.out.println("Error fetching job listing at " + url);
            return Pair.of(url, null);
        }
    }

    // extract the relevant info and bundle it up into our own domain objects
    private static JobInfo extractJobInfo(Pair<String, Document> urlAndDoc) {
        String url = urlAndDoc.getLeft();
        System.out.println("Extracting job info from " + url);
        Document jobDocument = urlAndDoc.getRight();
        if (jobDocument == null) {
            return null;
        }
        Elements titleElement = jobDocument.select(".jobtitle");
        Elements locationElement = jobDocument.select(".location");
        Elements companyElement = jobDocument.select(".company");
        String title = titleElement.isEmpty() ? "<NO TITLE>" : titleElement.get(0).text();
        String location = locationElement.isEmpty() ? "<NO LOCATION>" : locationElement.get(0).text();
        String company = companyElement.isEmpty() ? "<NO COMPANY>" : companyElement.get(0).text();
        return new JobInfo(title, location, company, url);
    }

    public static void stop() {
        System.out.println("Stopping the job scraper");
        Spark.stop();
    }

    // our domain-specific class for bundling up job info
    private static class JobInfo {

        String title;
        String location;
        String company;
        String url;

        public JobInfo(String title, String location, String company, String url) {
            this.title = title;
            this.location = location;
            this.company = company;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getLocation() {
            return location;
        }

        public String getCompany() {
            return company;
        }

        public String getUrl() {
            return url;
        }
    }
}
