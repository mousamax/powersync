package com.familymind.powersync.controller;

import com.familymind.powersync.dto.WriteCheckpointRequest;
import com.familymind.powersync.entity.Family;
import com.familymind.powersync.entity.Member;
import com.familymind.powersync.entity.Task;
import com.familymind.powersync.entity.TaskList;
import com.familymind.powersync.repository.FamilyRepository;
import com.familymind.powersync.repository.MemberRepository;
import com.familymind.powersync.repository.TaskListRepository;
import com.familymind.powersync.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/powersync")
@RequiredArgsConstructor
@Slf4j
public class PowerSyncController {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final EntityManager entityManager;

    @PostMapping("/write-checkpoint")
    @Transactional
    public ResponseEntity<Map<String, Object>> writeCheckpoint(@RequestBody WriteCheckpointRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            for (var operation : request.operations()) {
                processOperation(operation);
                results.add(Map.of(
                        "op", operation.op(),
                        "table", operation.table(),
                        "success", true
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "processed", results.size(),
                    "results", results
            ));
        } catch (Exception e) {
            log.error("Error processing write operations", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    private void processOperation(WriteCheckpointRequest.WriteOperation operation) {
        switch (operation.table()) {
            case "task" -> processTaskOperation(operation);
            case "task_list" -> processTaskListOperation(operation);
            case "member" -> processMemberOperation(operation);
            case "family" -> processFamilyOperation(operation);
            default -> log.warn("Unknown table: {}", operation.table());
        }
    }

    private void processTaskOperation(WriteCheckpointRequest.WriteOperation op) {
        Map<String, Object> data = op.data();
        UUID id = UUID.fromString((String) data.get("id"));

        switch (op.op()) {
            case "PUT" -> {
                Task task = new Task();
                task.setId(id);
                task.setTitle((String) data.get("title"));
                task.setDescription((String) data.get("description"));
                task.setIsCompleted(Boolean.parseBoolean(String.valueOf(data.get("is_completed"))));
                
                if (data.get("task_list_id") != null) {
                    UUID taskListId = UUID.fromString((String) data.get("task_list_id"));
                    task.setTaskList(entityManager.getReference(TaskList.class, taskListId));
                }
                if (data.get("assigned_to") != null) {
                    UUID assignedToId = UUID.fromString((String) data.get("assigned_to"));
                    task.setAssignedTo(entityManager.getReference(Member.class, assignedToId));
                }
                if (data.get("task_date") != null) {
                    task.setTaskDate(LocalDate.parse((String) data.get("task_date")));
                }
                if (data.get("task_time") != null) {
                    task.setTaskTime(LocalTime.parse((String) data.get("task_time")));
                }
                if (data.get("position") != null) {
                    task.setPosition(Integer.parseInt(String.valueOf(data.get("position"))));
                }
                
                taskRepository.save(task);
            }
            case "PATCH" -> taskRepository.findById(id).ifPresent(task -> {
                if (data.containsKey("title")) task.setTitle((String) data.get("title"));
                if (data.containsKey("description")) task.setDescription((String) data.get("description"));
                if (data.containsKey("is_completed")) {
                    task.setIsCompleted(Boolean.parseBoolean(String.valueOf(data.get("is_completed"))));
                    if (task.getIsCompleted()) {
                        task.setCompletedAt(ZonedDateTime.now());
                    }
                }
                if (data.containsKey("task_date")) {
                    task.setTaskDate(LocalDate.parse((String) data.get("task_date")));
                }
                if (data.containsKey("position")) {
                    task.setPosition(Integer.parseInt(String.valueOf(data.get("position"))));
                }
                taskRepository.save(task);
            });
            case "DELETE" -> taskRepository.deleteById(id);
        }
    }

    private void processTaskListOperation(WriteCheckpointRequest.WriteOperation op) {
        Map<String, Object> data = op.data();
        UUID id = UUID.fromString((String) data.get("id"));

        switch (op.op()) {
            case "PUT" -> {
                TaskList taskList = new TaskList();
                taskList.setId(id);
                taskList.setName((String) data.get("name"));
                if (data.get("family_id") != null) {
                    UUID familyId = UUID.fromString((String) data.get("family_id"));
                    taskList.setFamily(entityManager.getReference(Family.class, familyId));
                }
                if (data.get("sort_by") != null) {
                    taskList.setSortBy((String) data.get("sort_by"));
                }
                taskListRepository.save(taskList);
            }
            case "PATCH" -> taskListRepository.findById(id).ifPresent(taskList -> {
                if (data.containsKey("name")) taskList.setName((String) data.get("name"));
                if (data.containsKey("sort_by")) taskList.setSortBy((String) data.get("sort_by"));
                taskListRepository.save(taskList);
            });
            case "DELETE" -> taskListRepository.deleteById(id);
        }
    }

    private void processMemberOperation(WriteCheckpointRequest.WriteOperation op) {
        Map<String, Object> data = op.data();
        UUID id = UUID.fromString((String) data.get("id"));

        switch (op.op()) {
            case "PATCH" -> memberRepository.findById(id).ifPresent(member -> {
                if (data.containsKey("name")) member.setName((String) data.get("name"));
                if (data.containsKey("color")) member.setColor((String) data.get("color"));
                if (data.containsKey("image")) member.setImage((String) data.get("image"));
                if (data.containsKey("birth_date")) {
                    member.setBirthDate(LocalDate.parse((String) data.get("birth_date")));
                }
                memberRepository.save(member);
            });
        }
    }

    private void processFamilyOperation(WriteCheckpointRequest.WriteOperation op) {
        Map<String, Object> data = op.data();
        UUID id = UUID.fromString((String) data.get("id"));

        switch (op.op()) {
            case "PATCH" -> familyRepository.findById(id).ifPresent(family -> {
                if (data.containsKey("name")) family.setName((String) data.get("name"));
                if (data.containsKey("color_code")) family.setColorCode((String) data.get("color_code"));
                if (data.containsKey("place_of_living")) family.setPlaceOfLiving((String) data.get("place_of_living"));
                if (data.containsKey("residence_type")) family.setResidenceType((String) data.get("residence_type"));
                if (data.containsKey("family_image")) family.setFamilyImage((String) data.get("family_image"));
                familyRepository.save(family);
            });
        }
    }
}
