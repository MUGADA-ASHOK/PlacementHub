# PlacementHub - Placement Drive Management System — Backend

This is the Spring Boot backend for the Placement Drive Management System. It handles authentication, role-based access control, drive lifecycle management, student eligibility matching, round-wise scoring, and resume storage. The frontend (React) talks to this over a REST API.

---

## What this service does

At a high level, it coordinates three parties — the placement cell (admins), companies, and students — through the full lifecycle of a campus recruitment drive. A drive doesn't just exist as a record in a table; it moves through states (draft → published → active → closed), and different roles can interact with it at different stages.

The backend enforces who can do what at every step. A company can only see its own drives. A student only sees drives they actually qualify for. An admin can see everything but a company's internal scoring decisions. None of this is handled by the frontend — the backend checks authority on every endpoint.

---

## Tech stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** — JWT-based auth, role enforcement via `@PreAuthorize`
- **Spring Data JPA** — repositories, derived queries, `Pageable` for pagination
- **PostgreSQL** — primary database
- **Cloudinary** — resume storage (uploaded as `resource_type=raw`)
- **Lombok** — reduces boilerplate on DTOs and entities
- **Maven** — build and dependency management

---

## Project structure

```
src/main/java/org/example/placement_drive_management/
├── controllers/
│   ├── AuthController.java           # /api/auth/** — public endpoints
│   ├── AdminControllers.java         # /api/admin/** — admin + super admin
│   ├── SuperAdminController.java     # /api/super-admin/** — super admin only
│   ├── CompanyController.java        # /api/company/** — company role
│   └── StudentProfileController.java # /api/student/profile/** — student role
│   └── StudentController.java        # /api/student/** — student role
├── service/
│   ├── AuthService.java
│   ├── AdminService.java
│   ├── CompanyService.java
│   ├── StudentProfileService.java
│   ├── StudentService.java
│   └── ApplicationRoundProjection.java  # interface projection for round applicants
├── entity/
│   ├── Student.java
│   ├── Admin.java
│   ├── Company.java
│   ├── Drive.java
│   ├── Eligibility.java
│   ├── Application.java
│   └── ApplicationRound.java
├── repository/
│   ├── StudentRepository.java
│   ├── AdminRepository.java
│   ├── CompanyRepository.java
│   ├── DriveRepository.java
│   ├── EligibilityRepository.java
│   ├── ApplicationRepository.java
│   └── ApplicationRoundRepository.java
├── dto/
│   ├── PageResponse.java             # Wraps paginated results
│   ├── StudentResponseDto.java
│   ├── StudentProfileDto.java
│   ├── DriveDto.java
│   ├── DriveRoundDto.java
│   ├── EligibilityDto.java
│   ├── ApplicationsDto.java
│   ├── ApplicationRoundDto.java
│   ├── CompanyDto.java
│   ├── AdminDto.java
│   └── auth/
│       ├── ApiResponse.java          # Standard response wrapper
│       ├── AuthResponse.java
│       ├── LoginRequest.java
│       ├── RegisterStudentRequest.java
│       ├── RegisterAdminRequest.java
│       ├── RegisterCompanyRequest.java
│       ├── RefreshTokenRequest.java
│       └── LogoutRequest.java
├── security/
│   ├── JwtService.java               # Token generation and validation
│   ├── JwtAuthFilter.java            # Attaches user to security context per request
│   └── SecurityConfig.java           # Public vs protected route config
└── config/
    └── CloudinaryConfig.java         # Cloudinary SDK setup
```

---

## Running locally

You need Java 17, Maven, and a PostgreSQL database.

**1. Clone and configure**

```bash
git clone <your-repo-url>
cd placement-drive-management
```

Open `src/main/resources/application.properties` and fill in:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pdms
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your_secret_key_at_least_256_bits_long
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=604800000

# Cloudinary
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

**2. Create the database**

```sql
CREATE DATABASE pdms;
```

Hibernate will create the tables on first run since `ddl-auto=update`.

**3. Run**

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. All API routes are under `/api`.

**4. Create the first Super Admin**

There's no UI for this. Insert directly into the database after the tables are created:

```sql
INSERT INTO admin (name, email, password, role)
VALUES ('Super Admin', 'superadmin@yourinstitute.ac.in', '<bcrypt_hash>', 'ROLE_SUPER_ADMIN');
```

Generate the bcrypt hash however you prefer — there are online tools, or use Spring's `BCryptPasswordEncoder` in a quick test. After this, the super admin can log in through the normal `/api/auth/login` endpoint and register other admins from the dashboard.

---

## Authentication

Auth is JWT-based with two tokens — a short-lived access token and a longer-lived refresh token.

**Login** → returns `{ accessToken, refreshToken }`

Every subsequent request needs `Authorization: Bearer <accessToken>` in the header.

**Token refresh** → `POST /api/auth/refresh` with `{ refreshToken }` → returns new `{ accessToken, refreshToken }`. The old refresh token is invalidated immediately (rotation). If the frontend gets a 401, it calls this endpoint, swaps the token, and retries the original request without interrupting the user.

**Logout** → `POST /api/auth/logout` with the refresh token in the body and the access token in the Authorization header. The refresh token is deleted from the database, and the access token is blacklisted until it naturally expires.

The `JwtAuthFilter` runs on every request, validates the access token, and loads the user into the security context. Spring Security's `@PreAuthorize` on each controller method checks the role from there.

---

## Roles and what they can do

**ROLE_SUPER_ADMIN**
Can do everything an admin can, plus register and delete admins. There should only be one super admin — the one seeded manually.

**ROLE_ADMIN**
Registers companies and students (students self-register, but admins can create them too). Creates drives, sets eligibility, publishes drives to eligible students, closes or extends drives, views all applications and student profiles, previews student resumes.

**ROLE_COMPANY**
Sees only drives belonging to their company. Publishes interview rounds, enters scores and feedback per student per round, shortlists by top-K or cutoff score, previews resumes of students who applied to their drives.

**ROLE_STUDENT**
Sees only drives they're eligible for (based on their profile). Applies to drives, views their application status and round results, uploads resume, manages their academic profile.

---

## Pagination

Every endpoint that returns a list uses `Pageable` and returns a `PageResponse<T>`. Nothing returns an unbounded list.

```java
// PageResponse<T>
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

All paginated endpoints accept `page` (0-based) and `size` as query parameters with sensible defaults. The frontend reads `hasNext` and `hasPrevious` directly from this DTO — don't rename those fields or the pagination buttons will break.

---

## Key API endpoints

### Auth — public, no token needed

```
POST /api/auth/register/student
POST /api/auth/register/admin        # requires ROLE_SUPER_ADMIN
POST /api/auth/register/company
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

### Admin

```
GET  /api/admin/allStudents                          ?page=0&size=15
GET  /api/admin/student/{rollNo}/profile
GET  /api/admin/allCompanies                         ?page=0&size=15
GET  /api/admin/company/{companyId}/drives           ?page=0&size=15
GET  /api/admin/getAllActiveDrives                   ?page=0&size=15
POST /api/admin/company/addDrive
POST /api/admin/company/addDriveEligibility
PUT  /api/admin/publishDrives/{driveId}
PUT  /api/admin/closeDrive/{driveId}
PUT  /api/admin/extendDrive/{driveId}/{localDate}
PUT  /api/admin/updateDriveEligibility/{driveId}
DELETE /api/admin/deleteDrive/{driveId}
GET  /api/admin/getAllApplications/{driveId}         ?page=0&size=15
GET  /api/admin/getAllDriveRounds/{driveId}          ?page=0&size=15
GET  /api/admin/getApplicantsForDriveRound/{driveId}/{roundNo}  ?page=0&size=15
GET  /api/admin/student/{rollNo}/viewResume          # streams PDF inline

# Dashboard counts
GET  /api/admin/students/count
GET  /api/admin/companies/count
GET  /api/admin/activeDrives/count
GET  /api/admin/admins/count
```

### Super Admin

```
GET    /api/super-admin/allAdmins        ?page=0&size=15
DELETE /api/super-admin/delete-admin/{id}
```

### Company

```
GET  /api/company/getAllDrives                               ?page=0&size=15
GET  /api/company/getRounds/{driveId}
GET  /api/company/allApplications/{driveId}                 ?page=0&size=15
GET  /api/company/allApplications/{driveId}/{roundNumber}   ?page=0&size=15
POST /api/company/publishDriveRound/{driveId}
POST /api/company/publishScore/{driveId}/{rollNo}/{roundNo}/{score}
POST /api/company/publishFeedback/{driveId}/{rollNo}/{roundNo}
POST /api/company/filterByTopK/{driveId}/{roundNo}/{topK}
POST /api/company/filterByCutOff/{driveId}/{roundNo}/{cutOff}
GET  /api/company/viewResume/{rollNo}                       # streams PDF inline
GET  /api/company/drives/count
GET  /api/company/activeDrives/count
```

### Student

```
GET  /api/student/profile
POST /api/student/profile/add
PUT  /api/student/profile/update
GET  /api/student/profile/allApplications            ?page=0&size=15
GET  /api/student/profile/allEligibleApplications    ?page=0&size=15
PUT  /api/student/profile/applyDrive/{driveId}
GET  /api/student/profile/allRounds/{driveId}
POST /api/student/profile/uploadResume
GET  /api/student/profile/viewResume                 # streams own PDF inline

# Dashboard counts
GET  /api/student/profile/getEligibleDrives
GET  /api/student/profile/getAppliedDrives
GET  /api/student/profile/getSelectedDrives
GET  /api/student/profile/getInProcessDrives
```

---

## Resume handling

Resumes are uploaded to Cloudinary as `resource_type=raw` (since they're PDFs, not images). Cloudinary serves these with `Content-Disposition: attachment` by default, which means the browser downloads them instead of rendering inline.

To work around this, there are proxy endpoints (`/viewResume`) that fetch the file from Cloudinary server-side, override the `Content-Disposition` header to `inline`, and stream the bytes back to the client. The frontend receives a blob and puts it in an `<iframe>`. This way resumes preview without any download.

Admins and companies hit separate `/viewResume` endpoints. The company endpoint verifies that the student actually applied to one of that company's drives before serving the file — a company can't request any student's resume just by knowing their roll number.

---

## Drive eligibility and publishing

When a drive is created it's in draft state — students can't see it. The admin sets eligibility criteria (minimum CGPA, allowed branches, passing year, max active backlogs, whether backlog history is allowed, gender filter). When the admin publishes the drive, the backend checks every student's profile against those criteria and creates `Application` records in `ELIGIBLE` status for everyone who qualifies. Those students then see the drive in their feed and can apply (which moves their status from `ELIGIBLE` to `APPLIED`).

This means eligibility is evaluated at publish time, not continuously. If a student's profile changes after a drive is published, it doesn't affect their eligibility for that drive.

---

## Application status flow

```
ELIGIBLE → APPLIED → INPROCESS → SELECTED
                               ↘ REJECTED
```

`ELIGIBLE` is set by the backend when the admin publishes a drive.
`APPLIED` is set when the student clicks apply.
`INPROCESS`, `SELECTED`, `REJECTED` are set based on round results.

Within each round, individual `ApplicationRound` records have their own status:
```
PENDING → CLEARED
        ↘ FAILED
```

---

## Deployment notes

For production, change `spring.jpa.hibernate.ddl-auto` from `update` to `validate` once the schema is stable — `update` is fine for development but you don't want it making schema changes in production. Use environment variables instead of hardcoded values in `application.properties`:

```bash
export DB_URL=jdbc:postgresql://your-prod-host:5432/pdms
export DB_USER=pdms_user
export DB_PASS=strongpassword
export JWT_SECRET=your_long_random_secret
export CLOUDINARY_CLOUD_NAME=...
export CLOUDINARY_API_KEY=...
export CLOUDINARY_API_SECRET=...
```

The frontend needs to be configured with the production API URL and served from a domain that the backend's CORS config allows.

---

## Frontend repo

The React frontend that consumes this API lives in a separate repository. It expects this backend running at the configured base URL and communicates via the endpoints listed above. See the frontend README for setup instructions on that side.
https://github.com/MUGADA-ASHOK/Placement_frontend
