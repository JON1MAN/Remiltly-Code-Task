
## Remiltly-Code-Task

## Prerequirements

- Java 21
- Gradle
- Docker

## How to Launch the Application
1) Update with latest remote main
	```sh
	 git pull
	```
 **If you want to launch both backend and database via docker**
 
2) Pull necessary docker compose images (both postgres and backend)
	```sh
	docker-compose pull
	```

3) Run Docker compose (if there was an update):
    ```sh
     docker-compose up
    ```    
step 3 will run two containers backend (on 8080 port) and db (on 5432 port)

 **If you want to launch backend in your terminal session and database in container**
 
2) Pull necessary docker compose images (both postgres and backend)
	```sh
	docker-compose pull
	```

3) Run Docker compose (if there was an update):
    ```sh
     docker-compose up
    ```

4) Then stop backend container (because it runs with same compose as databse
   ```sh
     docker stop remitly_backend
   ```

5) Build with gradlew:
   ```sh
     ./gradlew build
    ```
5) Run with gradlew:
   ```sh
     ./gradlew bootRun
    ```

## Troubleshooting
 1) Ensure Docker is running .
 2) Give right permissions to **gradlew**  file
	 ```sh
		chmod + x gradlew
	```
3) Ensure you have java 21
4) gradle version 8.13

## Database
I use PostgreSQL running on port 5432 inside a docker container **remitly_postgres_db**

## init.sql

this is a script with already parsed data from excel file that you provided, this script will automatically run on start of **remitly_postgres_db** container once it started and fill swift_codes table with data.

## Structure of project

**/db/init/init.sql** - contains script with data that for db container start \
**/src/main/java/com.remitly/** - main code \
structure inside **/src/main/java/com.remitly/**:
```sh
	controller - contains dtos, mappers, controllers also contains GlobalExceptionHandler
	dao - contains daos and their repositories
	service - contains services
```

**/src/test/java/com.remitly/**  - unit and integration tests (structure for tests same as in main code)

**docker-compose.yml** - compose file for backend and database \
**Dockerfile** - docker file for project

## Task Overview

The goal was to develop an application capable of parsing a SWIFT code dataset from a provided file and storing it efficiently in a database for fast querying. The specific requirements were:

-   **SWIFT Code Identification**:
    
    -   Codes ending with `XXX` represent a bank's **headquarter**.
        
    -   Other codes are **branches**, which can be associated with a headquarter if the first 8 characters match.
        
    -   A SWIFT code can serve both as a headquarter and a branch.
        
-   **Formatting Constraints**:
    
    -   Country codes and names must be stored and returned as **uppercase** strings.
        
    -   Redundant columns in the file may be ignored.
        
-   **Data Storage Requirements**:
    
    -   Use a database (relational or non-relational) optimized for **low-latency** access.
        
    -   Support fast retrieval of:
        
        -   A specific SWIFT code
            
        -   All codes associated with a given country (via ISO-2 code)

## Explanation of my approach
To represent the SWIFT code structure, a single table/entity `swift_codes` was created.
### SwiftCode Entity

    @Entity
    @Table(name = "swift_codes")
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public class SwiftCode {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String countryISO2;

    @Column(nullable = false)
    private String countryName;

    private boolean isHeadquarter;

    @Column(nullable = false, unique = true)
    private String swiftCode;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @OneToMany(mappedBy = "headquarterId", cascade = CascadeType.ALL)
    private List<SwiftCode> branches = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "headquarter_id")
    private SwiftCode headquarterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country countryId;

    @Transactional
    public void addBranch(SwiftCode swiftCode) {
        this.branches.add(swiftCode);
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
    }


### Design Considerations (In My Opinion)

-   **Branch to Headquarter Mapping**:  
    I decided to represent the relationship between branches and their headquarters within the same table using a `ManyToOne` and `OneToMany` association. If a SWIFT code is a branch, it will have a reference to its headquarter via a foreign key. On the other hand, if it’s a headquarter, it can easily access its associated branches. This approach feels clean and aligns well with the logic that a branch can be linked by matching the first 8 characters of the SWIFT code.
    
-   **Soft Delete Strategy**:  
    I chose to implement a soft delete mechanism using an `isDeleted` flag. In the context of financial data, it's often critical to preserve historical records. This allows for better auditing, analytics, or even potential recovery scenarios. It’s also future-proof if we plan to add new features or want to analyze trends over time.
    
- **Country Normalization**:  
Since I wasn’t provided with any official or consistent mapping between ISO-2 codes and country names in the task description, I decided to rely on Java’s built-in `Locale` utility to validate and store both values. I also reached out via email to clarify this point but didn’t receive a response. In my opinion, storing both the ISO-2 code and the country name makes the system more reliable, especially if the frontend or analytics tools need both formats. Using `Locale` helps ensure consistency across the dataset without introducing manual mappings.

## REST Endpoints

### 1. Create SWIFT Code

**POST**  `/v1/swift-codes`

#### Request Body:

```
{
  "address": "string",
  "bankName": "string",
  "countryISO2": "string",
  "countryName": "string",
  "isHeadquarter": true,
  "swiftCode": "string"
}
```

----------

### 2. Get SWIFT Code by Code

**GET**  `/v1/swift-codes/{swift-code}`

#### Response for Headquarter:

```
{
  "address": "string",
  "bankName": "string",
  "countryISO2": "string",
  "countryName": "string",
  "isHeadquarter": true,
  "swiftCode": "string",
  "branches": [
    {
      "address": "string",
      "bankName": "string",
      "countryISO2": "string",
      "isHeadquarter": false,
      "swiftCode": "string"
    }
  ]
}
```

#### Response for Branch:

```
{
  "address": "string",
  "bankName": "string",
  "countryISO2": "string",
  "countryName": "string",
  "isHeadquarter": false,
  "swiftCode": "string",
  "branches": []
}
```

> Even for branches, an empty `branches` array is returned to keep the response structure consistent and simplify future frontend integration.

----------

### 3. Soft Delete SWIFT Code

**DELETE**  `/v1/swift-codes/{swift-code}`

> Marks the record as deleted by setting `isDeleted = true`. I am using soft-delete strategy, because we are operating on data, related to finances.

----------

### 4. Get SWIFT Codes by Country

**GET**  `/v1/swift-codes/country/{countryISO2code}`

> Returns all SWIFT codes associated with the given ISO-2 country code.

## Input Validation

Validation logic ensures data integrity before persisting:

```
private void validateSwiftCodeCreation(SwiftCodeDTO request) {
    if (swiftCodeAlreadyExists(request.getSwiftCode())) {
        throw new SwiftCodeAlreadyExistsException("Swift code: " + request.getSwiftCode() + ", already exists");
    } else if (request.isHeadquarter() && !isValidHeadQuarterSwiftCode(request.getSwiftCode())) {
        throw new SwiftCodeValidationException("Invalid format for headquarter provided, should end with XXX, actual: " + request.getSwiftCode());
    } else if (!validISO2CodeWithCountryName(request.getCountryISO2(), request.getCountryName())) {
        throw new SwiftCodeValidationException("Invalid country combination: ISO2 = '" + request.getCountryISO2() + "', name = '" + request.getCountryName() + "'");
    }
}
```

Country validation is done using Java's built-in `Locale` utilities (for example to prevent situations when somebody want to provide: EN, Poland and vice versa):

	private boolean validISO2CodeWithCountryName(String countryISO2, String countryName) { 
	    if (countryISO2 == null || countryName == null) return false;
	    for (String iso : Locale.getISOCountries()) {
	        if (iso.equalsIgnoreCase(countryISO2)) {
	            Locale locale = new Locale.Builder().setRegion(iso).build();
	            String displayCountry = locale.getDisplayCountry(Locale.ENGLISH);
	            return displayCountry.equalsIgnoreCase(countryName);
	        }
	    }
	    return false;
	}
