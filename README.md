
# 🐾 Pet Finder

Pet Finder is a web application for finding pets and livestock in Zambia. It connects animal buyers with sellers through searchable listings, seller accounts, vaccination details, and buyer enquiries.

The backend is developed using **Java, Javalin, and MySQL**, while the frontend uses **HTML, CSS, and JavaScript**, served directly by Javalin.

**GitHub Repository:** https://github.com/Nachy22/pet-finder

---

## Project Team

| Member | Full Name | Student Number | 
|--------|-----------|----------------|
| 1 | Student One |       2410051     | 
| 2 | Student Two |       2410181     | 
| 3 | Student Three |     2410157     | 
| 4 | Student Four |      2410053     | 
| 5 | Student Five |      2410396     | 


---

# 1. THE PROBLEM

In Zambia, people looking for pets or livestock often have to search through scattered advertisements or contact sellers individually, making it difficult to compare available animals and their details.

Animal sellers also need an organized way to advertise their animals and receive enquiries from interested buyers.

**Pet Finder solves this problem** by bringing buyers and sellers together through searchable animal listings, seller profiles, vaccination information, and a simple enquiry system.

---

# 2. HOW TO RUN

The following instructions explain how to install and run Pet Finder on Ubuntu/Linux.

## 2.1 Prerequisites

Make sure the following tools are installed:

- Java JDK 17 or newer
- Apache Maven
- MySQL Server
- Git
- Visual Studio Code (optional)

The project targets Java 17 but can also be compiled using compatible newer Java versions, such as Java 25.

### Install the prerequisites

Open your terminal and run:

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven mysql-server git
```

Verify the installations:

```bash
java -version
mvn -version
git --version
```

## 2.2 Clone the Repository

Download the project from GitHub:

```bash
git clone https://github.com/Nachy22/pet-finder.git
```

Navigate into the project directory:

```bash
cd pet-finder
```

Open the project in Visual Studio Code:

```bash
code .
```

## 2.3 Start MySQL

Start the MySQL database server:

```bash
sudo systemctl start mysql
```

Verify that MySQL is running:

```bash
sudo systemctl status mysql
```

The expected status is:

```text
Active: active (running)
```

Press `q` to exit the status display.

## 2.4 Create the Database

For the first installation, create the database and required tables.

From the project directory, run:

```bash
sudo mysql < database/schema.sql
sudo mysql < database/phase4_migration.sql
```

These scripts create the `pet_finder` database and the application tables.

The main database tables include:

| Table | Purpose |
|-------|---------|
| sellers | Stores seller profiles and verification status |
| seller_credentials | Stores seller authentication credentials |
| animals | Stores pet and livestock information |
| listings | Stores animal advertisements |
| health_records | Stores animal vaccination and treatment records |
| enquiries | Stores messages from buyers |
| listing_images | Stores references to uploaded animal photos |
| reports | Stores reports about suspicious listings |

### Create a MySQL Application User

Open the MySQL administrative console:

```bash
sudo mysql
```

Run the following SQL commands:

```sql
CREATE USER IF NOT EXISTS 'petfinder_app'@'localhost'
IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';

ALTER USER 'petfinder_app'@'localhost'
IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE
ON pet_finder.*
TO 'petfinder_app'@'localhost';

EXIT;
```

Replace `CHOOSE_A_STRONG_PASSWORD` with your own secure password in both places.

**Important:** Never upload your database password to GitHub.

## 2.5 Configure Database Connection

In the terminal where you will run the Java application, configure the following environment variables:

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app

read -rsp 'MySQL application password: ' DB_PASSWORD
echo
export DB_PASSWORD
```

Enter the password created for the MySQL user `petfinder_app`.

This password is different from the password used by sellers to log in to Pet Finder.

These environment variables remain active for the current terminal session.

## 2.6 Build and Run the Application

Compile the application and execute its automated tests:

```bash
mvn clean test
```

Start the Javalin server:

```bash
mvn exec:java -Dexec.mainClass=com.petfinder.App
```

Once the application starts successfully, open your browser and visit:

**http://localhost:7070**

### Available Pages

| Page | URL |
|------|-----|
| Homepage | http://localhost:7070/ |
| Browse Animals | http://localhost:7070/browse.html |
| Seller Registration | http://localhost:7070/seller-signup.html |
| Seller Login | http://localhost:7070/seller-login.html |
| API Health Check | http://localhost:7070/api/health |

To stop the application, press `Ctrl + C` in the terminal running Javalin.

## 2.7 Seller Verification

Newly registered sellers are initially unverified.

Their listings do not appear publicly until their account has been approved.

For local classroom demonstrations, an administrator can view registered sellers using:

```bash
sudo mysql -e "SELECT id, business_name, verified FROM pet_finder.sellers;"
```

After reviewing the seller's verification information, the administrator can approve the appropriate account.

For example, to approve seller ID `1`:

```bash
sudo mysql -e "UPDATE pet_finder.sellers SET verified = 1 WHERE id = 1;"
```

Replace `1` with the correct seller ID.

This is a local administrative procedure. The application does not expose a public account-approval endpoint.

---

# 3. YOUR CLASSES

Pet Finder follows **Object-Oriented Programming (OOP)** principles.

The Java classes are located in:

`src/main/java/com/petfinder/`

Each class has a specific responsibility within the application.

## 3.1 Main Application Class

| Class | Responsibility |
|-------|----------------|
| App | Starts the Javalin web server, registers REST API routes, and serves the frontend. |

## 3.2 Model Classes

These classes represent the main objects within the Pet Finder application.

| Class | Responsibility |
|-------|----------------|
| Animal | Abstract parent class containing common animal attributes and health records. |
| Pet | Represents pets and provides pet-specific information such as temperament. |
| Livestock | Represents livestock and provides information about the animal's purpose. |
| Seller | Stores seller business details, contact information, location, and verification status. |
| Listing | Connects an animal to a seller and manages price, quantity, and availability. |
| HealthRecord | Stores vaccination or treatment details and dates for animals. |
| Enquiry | Represents messages sent by potential buyers about animal listings. |

## 3.3 Service Classes

| Class | Responsibility |
|-------|----------------|
| ListingService | Uses Java collections to manage listings and demonstrate create, read, update, and delete operations. |

## 3.4 Repository Classes

Repository classes handle interactions between the Java application and the MySQL database.

| Class | Responsibility |
|-------|----------------|
| SellerRepository | Retrieves and stores seller information in MySQL. |
| ListingRepository | Retrieves, updates, and deletes animal listings in MySQL. |
| EnquiryRepository | Stores and retrieves buyer enquiries. |
| AccountRepository | Creates seller accounts and stores securely hashed passwords. |
| ApiWriteRepository | Performs database operations for listings, photos, enquiries, and reports. |

## 3.5 Database Class

| Class | Responsibility |
|-------|----------------|
| Database | Establishes and manages JDBC connections between Java and MySQL. |

## 3.6 API Classes

| Class | Responsibility |
|-------|----------------|
| ApiRoutes | Defines REST endpoints and handles requests, validation, authentication, and authorization. |
| ApiException | Provides consistent error handling for API-related problems. |
| Passwords | Securely hashes and verifies seller passwords using salted PBKDF2. |
| UploadStore | Manages uploaded animal images and private seller-verification documents. |

## 3.7 Custom Exception Class

| Class | Responsibility |
|-------|----------------|
| ListingNotFoundException | Custom exception thrown when a requested animal listing cannot be found. |

## 3.8 Demonstration Classes

| Class | Responsibility |
|-------|----------------|
| ModelDemo | Demonstrates inheritance, polymorphism, and Java collections. |
| Phase2Demo | Demonstrates OOP models, collections, and custom exception handling. |
| Phase3Demo | Demonstrates persistent MySQL storage by saving and retrieving records in separate Java processes. |

## 3.9 Automated Test Classes

The automated tests are located in:

`src/test/java/com/petfinder/`

| Test Class | What It Tests |
|------------|---------------|
| PasswordsTest | Password hashing, verification, and invalid credentials. |
| AnimalModelTest | Animal inheritance, polymorphism, encapsulation, and validation. |
| ListingTest | Listing creation, updates, and validation. |
| SellerAndEnquiryTest | Seller and enquiry models. |
| ListingServiceTest | Collections, listing operations, and custom exceptions. |

---

# 4. OBJECT-ORIENTED PROGRAMMING CONCEPTS

Pet Finder demonstrates the following Java OOP concepts.

### 4.1 Encapsulation

The `Animal`, `Seller`, and `Listing` classes use private fields with controlled access through constructors and methods.

This protects object data from uncontrolled modification.

### 4.2 Inheritance

The `Pet` and `Livestock` classes inherit from the abstract `Animal` parent class.

This allows both classes to reuse common animal attributes and behavior.

### 4.3 Polymorphism

The `Pet` and `Livestock` classes override methods such as `describe()`.

The same method call produces different results depending on the type of animal object.

### 4.4 Abstraction

The `Animal` class is abstract and defines behavior that its subclasses must implement.

### 4.5 Collections

The application uses Java collections such as `ArrayList`, `List`, and `HashMap` to manage objects.

### 4.6 Custom Exceptions

The `ListingNotFoundException` class handles situations where a requested listing does not exist.

### 4.7 Persistent Storage

The application uses MySQL and JDBC to ensure that data remains saved after the Java application restarts.

---

# 5. MAIN FEATURES

## 5.1 Buyer Features

- Browse available pets and livestock.
- Search and filter animal listings.
- View animal details, including breed and price.
- View seller information.
- View vaccination and health records.
- Submit enquiries to sellers.
- Report suspicious animal listings.

## 5.2 Seller Features

- Register a seller account.
- Log in securely.
- Access a personal seller dashboard.
- Create animal listings.
- Upload animal photographs.
- Add vaccination and treatment information.
- Update listing prices and statuses.
- Manage animal advertisements.
- View enquiries from interested buyers.
- Log out securely.

## 5.3 Security Features

- Password hashing using salted PBKDF2.
- Seller authentication and session management.
- Authorization checks for seller-owned listings.
- Input validation.
- Controlled file uploads.
- Private storage of seller-verification documents.
- Seller verification before public listing visibility.

---

# 6. REST API ENDPOINTS

Pet Finder uses RESTful APIs to communicate between the frontend and backend.

| HTTP Method | Endpoint | Purpose |
|-------------|----------|---------|
| GET | /api/health | Check server status |
| GET | /api/data | Retrieve public seller and listing data |
| GET | /api/listings | Retrieve public animal listings |
| GET | /api/listings/{id} | Retrieve a specific animal listing |
| GET | /api/breeds | Retrieve breed information |
| POST | /api/auth/register | Register a seller |
| POST | /api/auth/login | Authenticate a seller |
| GET | /api/auth/me | Retrieve the authenticated seller |
| POST | /api/auth/logout | Log out a seller |
| GET | /api/sellers/me/listings | Retrieve a seller's listings |
| GET | /api/sellers/me/enquiries | Retrieve enquiries for a seller |
| POST | /api/listings | Create an animal listing |
| PUT / PATCH | /api/listings/{id} | Update a listing |
| DELETE | /api/listings/{id} | Delete a listing |
| POST | /api/enquiries | Submit a buyer enquiry |
| POST | /api/reports | Report a suspicious listing |

### Example API Request

Check whether the backend is running:

```bash
curl http://localhost:7070/api/health
```

Expected response:

```json
{
  "status": "ok"
}
```

---

# 7. PROJECT STRUCTURE

```text
pet-finder/
│
├── README.md
├── PHASES_TEST.md
├── pom.xml
├── LICENSE
│
├── database/
│   ├── schema.sql
│   ├── phase4_migration.sql
│   └── phase5_checks.sql
│
├── design/
│
├── scripts/
│   ├── phase5_all_checks.sh
│   └── phase5_api_checks.py
│
├── src/
│   ├── main/
│   │   ├── java/com/petfinder/
│   │   │   ├── App.java
│   │   │   ├── api/
│   │   │   ├── db/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   │
│   │   └── resources/public/
│   │       ├── css/
│   │       ├── images/
│   │       ├── js/
│   │       └── *.html
│   │
│   └── test/java/com/petfinder/
│
└── phase4_smoke.sh
```

### Folder Descriptions

| Folder | Purpose |
|--------|---------|
| api | REST endpoints, authentication, and file uploads |
| db | Database connection configuration |
| exception | Custom exceptions |
| model | Java OOP domain classes |
| repository | MySQL database operations |
| service | Application business logic |
| resources/public | Frontend HTML, CSS, JavaScript, and images |
| src/test | Automated JUnit tests |
| database | Database schemas, migrations, and verification |
| scripts | Automated API testing scripts |
| design | Frontend interface mockups |

---

# 8. TESTING AND VERIFICATION

Pet Finder was tested using automated Java unit tests, API checks, database verification, and manual browser testing.

## 8.1 Java Unit Tests

Run:

```bash
mvn clean test
```

Verified results:

| Result | Total |
|--------|-------|
| Tests executed | 21 |
| Passed | 21 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |

**Result: BUILD SUCCESS**

## 8.2 REST API Tests

With MySQL and Javalin running, execute:

```bash
python3 scripts/phase5_api_checks.py
```

Verified results:

| Result | Total |
|--------|-------|
| Tests executed | 25 |
| Passed | 25 |
| Failed | 0 |

## 8.3 Database Integrity Tests

Execute:

```bash
sudo mysql < database/phase5_checks.sql
```

The database checks verify:

- Valid seller relationships.
- Valid animal and listing relationships.
- Health records linked to existing animals.
- Enquiries linked to existing listings.
- Valid listing prices and quantities.
- Valid seller credentials and image relationships.

**Verified result: 0 database integrity violations.**

## 8.4 Manual Application Testing

The following application functionality was successfully tested:

- Seller registration.
- Seller login.
- Seller dashboard access.
- Creating animal listings.
- Persistent storage after refreshing the dashboard.
- Seller verification.
- Public animal browsing.
- Buyer enquiries.
- Displaying enquiries in the seller dashboard.

More information about the nine academic requirements and testing is available in:

**PHASES_TEST.md**

> Passing local tests does not mean the application is certified for public production deployment.

---

# 9. GROUP CONTRIBUTION VERIFICATION

**Lecturer's Requirement:** All five group members must have made commits to the repository using their own GitHub accounts.

### How to Verify Contributions

1. Open the repository:

   https://github.com/Nachy22/pet-finder

2. Select the `main` branch.

3. Click **Commits** or **History**.

4. Review the GitHub account associated with each commit.

5. Confirm that all five group members have at least one commit associated with their own GitHub account.

6. Update the Project Team table with the correct GitHub usernames.

### Verify Using Git

From the project directory:

```bash
git fetch origin
git shortlog -sne origin/main
```

This displays the commit author names, email addresses, and contribution counts.

**Note:** A commit author name alone does not prove which GitHub account made the contribution. Check that the commits are associated with the correct student accounts on GitHub.

**Contribution Verification Status:** Pending confirmation of all five GitHub accounts.

---

# 10. SECURITY AND PROJECT SCOPE

Pet Finder was developed as an academic project demonstrating Java Object-Oriented Programming, RESTful APIs, frontend integration, and persistent database storage.

### Security Considerations

- Do not commit `.env` files or database passwords.
- Do not upload session cookies or private authentication tokens.
- Seller passwords are stored as salted hashes.
- Uploaded verification documents are stored separately from public listing images.
- Uploaded files are stored outside the Git repository under `~/.petfinder-uploads/`.
- Sellers can only manage listings that belong to their accounts.

For production use, the application would require additional security improvements, including HTTPS, account recovery, rate limiting, administrative verification workflows, and stronger deployment controls.

---

# 11. LICENSE

See the [LICENSE](LICENSE) file for the project's license information.

---

**Pet Finder — Connecting animal buyers and sellers in Zambia.**
