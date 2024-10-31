# Exchange Rate Service

This is a simple Exchange Rate Service application created as a test task for Spribe Senior Java Developer vacancy. It demonstrates fundamental functionalities for handling currency exchange rates using a PostgreSQL database and external API integration with the Fixer API.

## Running the Application with Docker

To build and run this application locally, follow these steps:

1. **Extract the ZIP File**:
    - Unzip the project files to a preferred directory on your machine.

2. **Navigate to the Project Directory**:
   ```bash
   cd <extracted-directory>

3. **Build and Start Containers**:
   - Use Docker Compose to set up and run the application along with its dependencies.
   ```bash
   docker-compose up --build

4. **Access the Application**:
   - Once running, the service will be accessible at `http://localhost:8080`

## Simplifications
This project has been intentionally simplified as it’s a test application. Below are some just as an examples:

- **Exception Handling**: Basic error handling is implemented, but it’s incomplete. For a production application, we’d add more robust exception mapping and handling.
- **Aspect-Oriented Logging**: We could introduce an aspect for logging method entry and exit points.
- **Profiles**: Application profiles (e.g., `dev`, `prod`, `test`) could be set up for more fine-grained configuration management.

## Testing the Application

A Postman collection is included in the project to simplify testing. The collection provides example requests for interacting with the Exchange Rate Service. You can find this collection in the root folder at `/postman`.

## Limitations and Considerations

Due to limitations in the free subscription tier of the Fixer API, only EUR-based exchange rates are supported. As a result, any request for a base currency other than EUR will default to EUR for fetching exchange rates.

## Exchange Rate Registry Implementations

This project includes two implementations of the ExchangeRateRegistry, each designed for handling concurrent access to exchange rate data but with different approaches to synchronization:

- **ExchangeRateInMemoryReentrantLockRegistry**:
Initially, the registry was implemented using a ReentrantReadWriteLock to ensure thread-safe access to the map of exchange rates. This approach allows us to separate read and write operations: multiple reads can occur simultaneously, while write operations block all other access. However, using a lock on the entire map during writes can become inefficient when there are high volumes of requests, as all threads must wait when a write lock is held.

- **ExchangeRateInMemoryConcurrentRegistry**:
To improve concurrency, a second registry was implemented using ConcurrentHashMap. Unlike ReentrantReadWriteLock, ConcurrentHashMap locks only the specific bucket affected by a write operation. This finer-grained locking means that other threads can continue accessing or modifying unrelated portions of the map, even while a write is in progress. This implementation is typically better suited for high-traffic scenarios, as it reduces contention and potential bottlenecks caused by locking the entire map.

By providing both implementations, the project allows flexibility in handling concurrent access based on expected request volumes and usage patterns.