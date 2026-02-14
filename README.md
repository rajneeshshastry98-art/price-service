# Latest Value Price Service

In-memory Java service for tracking latest prices for financial instruments.

## Features

- Batch-based upload
- Atomic visibility of completed batches
- Thread-safe implementation
- Latest value determined using timestamp (asOf)
- In-memory using core Java only

## Technologies

- Java 17
- Maven
- JUnit 5

## Run tests

```bash
mvn clean test
