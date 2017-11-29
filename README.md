# scraper
Sample scraper for job listings

This project requires Java 8 and Maven 3. To build, use the following command:

`mvn clean package`

This will create an executable jar inside the `target` folder (note that the test will attempt to start the server, which requires port 8080 to be free). To run the server, use the following command from the root project folder:

`java -jar target/scraper-1.0-SNAPSHOT-spring-boot.jar`

This will start the server on port 8080; from there, you can use your favorite client to start issuing requests.
