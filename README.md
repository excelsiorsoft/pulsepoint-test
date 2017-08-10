## Web Page Classifier ##

A JAX-RS service for classifying websites; given a URL, retrieves, parses, and classifies the content of the document into a category.

### Requirements ###

Building this project requires the following dependencies:

* [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Apache Maven 3.3.3+](https://maven.apache.org/)

### Building ###

To build the project and run all tests, run:

```
mvn clean package
```

To build *without* running any tests:

```
mvn clean package -DskipTests
```

### Running ###

To run the service, run:

```
java -jar target/classifier-1.0.0-SNAPSHOT.jar 8080
```

This will bring up the classifier web application on port 8080.

Once started, the classifier web application can be tested by visiting ```http://localhost:8080/classifier/classify``` which should return the default "unknown" category

### Communicating with the application ###

This is a REST application that can be communicated with using any HTTP client.  Some curl examples below:

* Training the classifier

    ```
    curl -v -H 'Content-Type: application/json' -X POST -d '{"category":"test","document":"this is a test document"}' http://localhost:8080/classifier/train
    ```
    
* Classifying a URL

    ```
    curl -v 'http://localhost:8080/classifier/classify?url=http://www.google.com/'
    ```

### Layout ###

This is a simple project consisting of a single interface used for classification - [Classifier.java](src/main/java/com/pulsepoint/classifier/domain/Classifier.java).  A classifier has two methods:

* ```train``` - accepts some document text and the category that this document belongs to
* ```classify``` - accepts a URL and returns the category that the document at the supplied URL belongs to

Tests for this project include an "integration" test that

* starts the application using an embedded HTTP server
* starts an HTTP server that serves content retrieved from some test websites (see [test.data.json](src/test/resource/test.data.json))
* creates an HTTP client for the application
* trains the classifier application using some training data (see [training.data.json](src/test/resources/training.data.json) )
* runs through some tests that verify classifier performance

### Goal ###

The goal is simple - make whatever modifications necessary to the source to make all tests pass. 

