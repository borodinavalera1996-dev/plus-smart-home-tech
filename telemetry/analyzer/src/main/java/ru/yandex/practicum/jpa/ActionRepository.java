package ru.yandex.practicum.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}