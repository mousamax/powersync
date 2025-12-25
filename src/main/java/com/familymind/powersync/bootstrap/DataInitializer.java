package com.familymind.powersync.bootstrap;

import com.familymind.powersync.entity.Family;
import com.familymind.powersync.entity.Member;
import com.familymind.powersync.entity.Task;
import com.familymind.powersync.entity.TaskList;
import com.familymind.powersync.repository.FamilyRepository;
import com.familymind.powersync.repository.MemberRepository;
import com.familymind.powersync.repository.TaskListRepository;
import com.familymind.powersync.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (familyRepository.count() > 0) {
            log.info("Data already exists. Skipping initialization.");
            return;
        }

        log.info("Initializing default data...");

        // Family 1
        Family family1 = Family.builder()
                .name("The Smiths")
                .colorCode("#FF5733")
                .placeOfLiving("New York")
                .residenceType("Apartment")
                .build();
        family1 = familyRepository.save(family1);

        Member member1 = Member.builder()
                .name("John Smith")
                .email("john.smith@example.com")
                .family(family1)
                .memberRole("Admin")
                .birthDate(LocalDate.of(1980, 1, 1))
                .color("#33FF57")
                .isVerified(true)
                .build();
        memberRepository.save(member1);

        TaskList taskList1 = TaskList.builder()
                .name("Groceries")
                .family(family1)
                .build();
        taskList1 = taskListRepository.save(taskList1);

        createTask(taskList1, "Buy Milk", "2 gallons of whole milk", member1);
        createTask(taskList1, "Buy Eggs", "1 dozen large eggs", member1);
        createTask(taskList1, "Buy Bread", "Whole wheat bread", member1);

        // Family 2
        Family family2 = Family.builder()
                .name("The Does")
                .colorCode("#3357FF")
                .placeOfLiving("Los Angeles")
                .residenceType("House")
                .build();
        family2 = familyRepository.save(family2);

        Member member2 = Member.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .family(family2)
                .memberRole("Admin")
                .birthDate(LocalDate.of(1985, 5, 15))
                .color("#FF33A8")
                .isVerified(true)
                .build();
        memberRepository.save(member2);

        TaskList taskList2 = TaskList.builder()
                .name("Chores")
                .family(family2)
                .build();
        taskList2 = taskListRepository.save(taskList2);

        createTask(taskList2, "Clean Room", "Tidy up the living room", member2);
        createTask(taskList2, "Wash Dishes", "Empty the sink", member2);
        createTask(taskList2, "Laundry", "Wash and fold clothes", member2);

        log.info("Default data initialization completed.");
    }

    private void createTask(TaskList taskList, String title, String description, Member assignee) {
        Task task = Task.builder()
                .title(title)
                .description(description)
                .taskList(taskList)
                .isCompleted(false)
                .taskDate(LocalDate.now())
                .taskTime(LocalTime.of(10, 0))
                .assignedTo(assignee)
                .assignedBy(assignee)
                .build();
        taskRepository.save(task);
    }
}
