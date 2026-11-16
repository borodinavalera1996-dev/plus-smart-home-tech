package ru.yandex.practicum.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);
    void deleteByHubIdAndName(String hubId, String name);
}
