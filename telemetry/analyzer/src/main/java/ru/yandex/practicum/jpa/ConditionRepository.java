package ru.yandex.practicum.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa.model.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
