Added infra/discovery-server Maven module.
Added DiscoveryServer Spring Boot main class with @EnableEurekaServer.
Configured discovery-server on port 8761 with self-registration disabled.
Registered discovery-server module in infra/pom.xml.
Added Eureka client dependency to infra/config-server.
Configured config-server to use a random port with server.port=0.
Configured config-server to register in Eureka at http://localhost:8761/eureka/.
Configured config-server Eureka instance hostname as localhost.
Added Eureka client dependency to telemetry/aggregator.
Configured telemetry/aggregator to import configserver via Eureka discovery.
Added Eureka client dependency to telemetry/analyzer.
Configured telemetry/analyzer to import configserver via Eureka discovery.
Added Eureka client dependency to telemetry/collector.
Configured telemetry/collector to import configserver via Eureka discovery.
Disabled external Config Server and Eureka for collector integration tests via test resources.
Added collector integration test topic properties for standalone embedded Kafka tests.
