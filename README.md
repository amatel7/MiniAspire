# MiniAspire
## Description

This project is a loan management system developed using Java, SQL, Spring Boot, and Maven. It allows users to manage loans and repayments.
This project uses h2 db which is an in-memory database.

## Assumptions
This project works on the following assumptions:
- System already has registered users and an admin user.
- To support above assumption, I have created a data.sql file which inserts some users and an admin user in the db at the time of application startup.
- Application has token authentication. currently token is being picked from same db where users resides and have no expiry but in real world scenario it should be fetched from auth service.


## Installation

### Prerequisites

- Java 17
- Maven

### Steps

1. Clone the repository
2. Navigate to the project directory
3. Run `mvn clean install` to build the project
4. Run `java -jar target/mini-aspire-0.0.1-SNAPSHOT.jar` to start the application

### H2 Db Console
- H2 db console can be accessed at http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: admin
- Password: password

## Usage

Provide instructions on how to use your application. Include any endpoints, methods, required parameters, and expected responses.
Apis can be tested using postman or any other rest client.
Supported Apis are:
1. POST : /api/loans - Create a loan
2. GET  : /api/loans - Get all loans and their repayments
3. GET  : /api/loans/{loanId} - Get a loan and its repayments
3. POST : /api/admin/loans/{loanId}/approve - Approve a loan
4. POST : /api/v1/loans/repayments/{repaymentId} - loan repayment by user

## Testing

This project uses JUnit for testing. Run `mvn test` to execute the tests.
