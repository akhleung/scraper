package com.jobr.sample;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.JSONArray;

public class ScraperTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ScraperTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ScraperTest.class );
    }

    static String URLS = "[\n" +
            "    \"http://www.indeed.com/viewjob?jk=8cfd54301d909668\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=b17c354e3cabe4f1\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=38123d02e67210d9\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=81834413cc1a7f24\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=54273e324a066f37\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=3b2f0d7ef4afd07d\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=b3694b5ab11bb5fb\",\n" +
            "    \"http://www.indeed.com/viewjob?jk=4d0470179cb7e059\"\n" +
            "]";

    /**
     * Integration test
     */
    public void testScraper() {
        System.out.println("TESTING THE JOB SCRAPER ON 8 URLS");
        Scraper.main(null);
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post("http://localhost:8080/get_jobs")
                    .header("accept", "application/json")
                    .body(URLS)
                    .asJson();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        JsonNode body = response.getBody();
        assertTrue(body.isArray());
        JSONArray jsonArray = body.getArray();
        assertTrue(jsonArray.length() == 8);
        assertTrue(jsonArray.getJSONObject(0).getString("title") != null);
        Scraper.stop();
    }
}
