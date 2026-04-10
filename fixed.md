Created fixed.md for 7-spring-cloud-microservices changes.
Added interaction-api module to share DTO classes and Feign contracts between commerce services.
Added shopping-store module with product CRUD, quantity state updates, soft delete, Config Server, and Eureka support.
Added shopping-cart module with cart operations, warehouse Feign call, Circuit Breaker configuration, Config Server, and Eureka support.
Added warehouse module with stock management, stock booking check, random warehouse address, Config Server, and Eureka support.
Updated commerce parent POM to include interaction-api, shopping-store, shopping-cart, and warehouse modules.
Updated Config Server search paths to include commerce service configuration files.
Added external Config Server YAML files for shopping-store, shopping-cart, and warehouse.
Handled Circuit Breaker no-fallback errors in shopping-cart as warehouse unavailable responses.
Removed fixed H2 driver from commerce Config Server datasource settings so CI PostgreSQL URLs can select the PostgreSQL driver.
Added PostgreSQL JDBC runtime dependency to shopping-store, shopping-cart, and warehouse.
Enabled Hibernate namespace creation for commerce schemas when services run against PostgreSQL.
Changed shopping-store quantityState endpoint to accept productId and quantityState as query parameters for Postman compatibility.
Changed shopping-store category listing to return products from the category regardless of ACTIVE or DEACTIVATE state.
Changed warehouse cart check so it validates stock and calculates booking data without subtracting quantities on every cart update.
Reduced shopping-cart Eureka registry fetch interval so Feign sees warehouse faster during CI startup.
