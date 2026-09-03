# CV Project Description

## Recommended CV entry

**Product Management and Storefront Platform** — Java 25, Spring Boot 4, MyBatis, MySQL, Vue 3

- Built a role-based REST application for administrator, seller, and customer workflows, with JWT authentication, BCrypt password storage, ownership checks, and validated product operations.
- Implemented a database-backed customer cart and transactional checkout that validates server-side prices and stock, creates one fulfilment order per product, and rolls back the complete basket when any line cannot be purchased.
- Managed schema changes with Flyway and added automated backend/frontend verification through Maven, Vitest, and GitHub Actions.
- Implemented pagination, search, safe image uploads, stock handling, and a deterministic latest-products feed in a maintainable modular monolith.

## Short version

Developed and tested a Java 25/Spring Boot 4 and Vue 3 shop-management application with role-based authorization, MySQL/Flyway persistence, a database-backed customer cart and transactional checkout, validated product workflows, and secure local image storage.

## Interview boundary

Describe the feed as “latest products” or “featured products”, not as personalized recommendation. The current application includes customer cart and fulfilment-order workflows, but does not collect payment details or integrate shipping, refunds, invoices, or user-interaction data, so it should not be presented as a complete commerce or recommender platform.

## Positioning for Swedish new-graduate applications

This project is a suitable supporting project for junior Java backend or full-stack applications because it demonstrates API design, relational persistence, authentication and authorization, input validation, automated testing, frontend migration, and CI. The modular-monolith architecture is an appropriate level for a new graduate: it gives concrete engineering decisions to discuss without claiming unnecessary microservices, cloud-scale infrastructure, or production traffic.

For a CV focused on an Uppsala University MSc profile, keep the three bullets above and place the project near the Java/Spring and software-engineering skills. Avoid invented user counts, performance numbers, team size, or deployment claims unless you can document them.
