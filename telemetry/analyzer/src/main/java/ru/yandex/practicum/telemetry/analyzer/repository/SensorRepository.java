package ru.yandex.practicum.telemetry.analyzer.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, String> {

    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    long countByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<Sensor> findByIdAndHubId(String id, String hubId);

    List<Sensor> findByIdInAndHubId(Collection<String> ids, String hubId);
}
