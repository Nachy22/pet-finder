
Pet Finder
Pet Finder is a web application for finding pets and livestock in Zambia. It connects animal buyers with sellers through searchable listings, seller accounts, vaccination details and buyer enquiries. The backend uses Java, Javalin and MySQL, and Javalin serves the HTML/CSS/JavaScript frontend.

Repository: https://github.com/Nachy22/pet-finder

Project team
The existing README listed five student numbers but used placeholder names. Replace the placeholders below with the correct names and each member's own GitHub username before submission.

Member	Full name	Student number	
1	Student One	      2410051	
2	Student Two      	2410181	
3	Student Three   	2410157	
4	Student Four	   2410053	
5	Student Five	   2410396	
1. THE PROBLEM
In Zambia, people looking for pets or livestock may have to search through scattered advertisements or contact sellers individually, making it difficult to compare available animals and their details. Animal sellers also need an organized way to advertise their animals and receive enquiries from interested buyers. Pet Finder brings buyers and sellers together through searchable listings, animal information, seller profiles and a simple enquiry system.

2. HOW TO RUN
These steps are for Ubuntu/Linux. You need Java 17 or newer, Maven, MySQL Server and Git. An existing Java installation such as Java 25 can also compile the project, which targets Java 17.

Step 1 — Get the project
git clone https://github.com/Nachy22/pet-finder.git
cd pet-finder
Open this directory in VS Code if desired:

code .
Step 2 — Install and start prerequisites
If the required tools are not installed:

sudo apt update
sudo apt install openjdk-17-jdk maven mysql-server git
Start MySQL:

sudo systemctl start mysql
sudo systemctl status mysql
Press q to leave the status display.

Step 3 — Create the database and tables (first run only)
From the root of the repository:

sudo mysql < database/schema.sql
sudo mysql < database/phase4_migration.sql
Create a separate MySQL user for the application:

sudo mysql
In the MySQL prompt, replace CHOOSE_A_STRONG_PASSWORD with a private password of your own:

CREATE USER IF NOT EXISTS 'petfinder_app'@'localhost'
IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';
ALTER USER 'petfinder_app'@'localhost'
IDENTIFIED BY 'CHOOSE_A_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE
ON pet_finder.* TO 'petfinder_app'@'localhost';
EXIT;
The two password placeholders above must be replaced with the same password. Keep that password private and out of Git.

Step 4 — Configure Java's database connection
In the same terminal that will start Javalin, run:

export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=pet_finder
export DB_USER=petfinder_app
read -rsp 'MySQL application password: ' DB_PASSWORD; echo
export DB_PASSWORD
Enter the MySQL application-user password, not a Pet Finder seller login password. Environment variables set this way last for the current terminal session.

Step 5 — Build and run the application
mvn clean test
mvn exec:java -Dexec.mainClass=com.petfinder.App
Leave the server terminal running. Open http://localhost:7070 in a browser.

Useful pages:

Home: http://localhost:7070/
Browse animals: http://localhost:7070/browse.html
Seller registration: http://localhost:7070/seller-signup.html
Seller login: http://localhost:7070/seller-login.html
API health check: http://localhost:7070/api/health
To stop the server, press Ctrl+C in the terminal running Javalin.

Seller verification for the classroom demo: New sellers begin unverified, so their listings do not appear publicly until an administrator approves the account. On a local development database, an administrator can check seller IDs with:

sudo mysql -e "SELECT id, business_name, verified FROM pet_finder.sellers;"
After reviewing the seller's verification information, an administrator can set verified=1 for the correct ID using the MySQL administrative account. This is a local demonstration procedure; there is no public seller-approval endpoint.

3. YOUR CLASSES
The following are the Java application classes in src/main/java/com/petfinder/. Each class has a distinct responsibility.

Java class	What it does
App	Starts the Javalin server, registers the API routes and serves the frontend.
model.Animal	Abstract parent class holding common animal information and health records.
model.Pet	Represents a pet and supplies pet-specific temperament and description behavior.
model.Livestock	Represents livestock and supplies its purpose and livestock-specific description behavior.
model.Seller	Represents a seller's contact, business, location and verification information.
model.Listing	Links an animal to a seller and manages its price, quantity and availability status.
model.HealthRecord	Holds an animal's vaccination or treatment name and date.
model.Enquiry	Represents a buyer's message about a particular listing.
service.ListingService	Manages an in-memory collection of listings and supports CRUD operations for the OOP demonstration.
repository.SellerRepository	Reads and writes seller information in MySQL.
repository.ListingRepository	Reads, updates and deletes persistent animal listings in MySQL.
repository.EnquiryRepository	Stores and retrieves buyer enquiries in MySQL.
repository.AccountRepository	Creates seller accounts and stores hashed password credentials.
repository.ApiWriteRepository	Performs database write operations for listings, photos, enquiries and reports.
db.Database	Creates and manages JDBC connections to the MySQL database.
api.ApiRoutes	Defines REST endpoints, request validation, authentication and authorization checks.
api.ApiException	Represents HTTP API errors for consistent error responses.
api.Passwords	Securely hashes and checks seller passwords using salted PBKDF2.
api.UploadStore	Handles uploaded listing images and private seller-verification documents.
exception.ListingNotFoundException	Custom exception raised when a requested listing does not exist.
ModelDemo	Demonstrates basic inheritance, polymorphism and collections.
Phase2Demo	Demonstrates OOP objects, collection operations and custom exception handling together.
Phase3Demo	Demonstrates writing and reading records across separate Java processes to verify persistence.
Key OOP concepts: Animal has private fields and controlled access (encapsulation); Pet and Livestock inherit from it (inheritance); their overridden describe() methods behave differently through the same Animal reference (polymorphism); ListingService uses lists/maps (collections); and ListingNotFoundException demonstrates custom exceptions.

The project also includes automated JUnit test classes under src/test/java/com/petfinder/: PasswordsTest, AnimalModelTest, ListingTest, SellerAndEnquiryTest and ListingServiceTest.

Main features
Buyers can browse pets and livestock, view details and contact sellers through enquiries.
Sellers can register, log in and manage their own animal listings through a dashboard.
Listings store prices, quantities, statuses, photos and animal health records in MySQL.
Seller verification controls which listings can appear on the public browse page.
Buyers can report a suspicious listing.
Passwords are salted and hashed, and sensitive verification files are kept separate from public listing images.
Main REST API endpoints
Method	Endpoint	Purpose
GET	/api/health	Check server status.
GET	/api/data	Retrieve public seller and listing data.
GET	/api/listings	Browse public listings.
GET	/api/listings/{id}	View one public listing.
GET	/api/breeds	Retrieve breed information.
POST	/api/auth/register	Register a seller.
POST	/api/auth/login	Log in a seller.
GET	/api/auth/me	Retrieve the current authenticated seller.
POST	/api/auth/logout	Log out.
GET	/api/sellers/me/listings	Retrieve the logged-in seller's listings.
GET	/api/sellers/me/enquiries	Retrieve enquiries for the logged-in seller.
POST	/api/listings	Create a listing.
PUT / PATCH	/api/listings/{id}	Update a listing belonging to the seller.
DELETE	/api/listings/{id}	Delete a listing belonging to the seller.
POST	/api/enquiries	Submit an enquiry.
POST	/api/reports	Report a listing.
Project structure
pet-finder/
├── README.md                 # Main project documentation
├── PHASES_TEST.md            # Academic requirements and testing evidence
├── pom.xml                   # Maven configuration
├── database/                 # MySQL schema, migration and checks
├── design/                   # Interface mockups
├── scripts/                  # Live API and automated check scripts
├── src/
│   ├── main/
│   │   ├── java/com/petfinder/
│   │   │   ├── api/          # API routes, authentication, uploads
│   │   │   ├── db/           # JDBC database connection
│   │   │   ├── exception/    # Custom exceptions
│   │   │   ├── model/        # Domain classes
│   │   │   ├── repository/   # MySQL operations
│   │   │   └── service/      # Business logic
│   │   └── resources/public/ # Website HTML, CSS, JavaScript, images
│   └── test/java/com/petfinder/  # JUnit tests
└── phase4_smoke.sh           # Basic public API checks
Testing and academic requirements
On the completed local project, the team verified:

21 JUnit tests passed, with zero failures and errors (mvn clean test).
25 live API checks passed, with zero failures (python3 scripts/phase5_api_checks.py).
Zero database integrity violations in the tested database (sudo mysql < database/phase5_checks.sql).
Seller registration, login, listing creation and persistence, public listing visibility and buyer enquiries through the browser.
To repeat the checks, keep MySQL and Javalin running and use a second terminal:

mvn clean test
python3 scripts/phase5_api_checks.py
sudo mysql < database/phase5_checks.sql
See PHASES_TEST.md for the nine required academic concepts and the detailed testing evidence. Passing local tests does not mean the application has been certified for public production use.

Group contribution verification — required before submission
Lecturer's requirement: All five group members must have made commits to this repository from their own GitHub accounts.

Open https://github.com/Nachy22/pet-finder and select the main branch.
Click History / Commits and inspect the author account shown for each commit.
Confirm that each of the five students in the team table has at least one commit linked to their own GitHub account; then fill in the GitHub usernames above.
If a member appears missing, check the commit's author identity and whether its email is linked to their GitHub account. A merge performed by one person does not, by itself, prove that all five contributed.
For a local cross-check after git fetch origin, run:

git shortlog -sne origin/main
Verification status: Not yet confirmed from the available public commit-history view. The group must check the five linked GitHub identities before submission.

Security and scope
Do not commit .env files, real passwords, session cookies, uploaded identity documents, or user uploads.
Seller passwords are stored as salted hashes; the original password cannot be read from the database.
The app is suitable as a local academic demonstration. A public production deployment would require additional hardening, such as HTTPS, account recovery, rate limiting and a dedicated administrator verification workflow.
Uploaded listing photos and private verification documents are stored outside the Git repository under ~/.petfinder-uploads/.
License
See LICENSE.

⋮⋮⋮⋮
