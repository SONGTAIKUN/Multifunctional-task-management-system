package com.example.taskmanager.scheduler;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserMapper;
import com.example.taskmanager.service.TaskService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Component responsible for scheduling daily email reminders for tasks due today.
 * Uses Spring's @Scheduled annotation to run the reminder job every day at 8:00 AM.
 */
@Component
public class TaskReminderScheduler {

    private final TaskService taskService;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;

    public TaskReminderScheduler(TaskService taskService,
                                 UserMapper userMapper,
                                 JavaMailSender mailSender) {
        this.taskService = taskService;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
    }

    /**
     * Scheduled method that runs every day at 8:00 AM (cron expression: "0 0 8 * * ?").
     * It checks for tasks that are due today and sends email reminders to corresponding users.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyReminders() {
        // Get the start and end timestamps for today
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Fetch all tasks that are due today and not marked as deleted
        List<Task> dueToday = taskService.findByDeadlineBetween(todayStart, todayEnd);

        // Send email reminder to each user with a valid email address
        for (Task task : dueToday) {
            User user = userMapper.selectById(task.getUserId());
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                sendEmailReminder(user.getEmail(), task.getTitle(), task.getDeadline());
            }
        }
    }

    /**
     * Helper method to construct and send a simple email reminder.
     *
     * @param to the recipient's email address
     * @param taskTitle the title of the task
     * @param deadline the deadline of the task
     */
    private void sendEmailReminder(String to, String taskTitle, LocalDateTime deadline) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Today's task reminder");
        message.setText("You have a task due today:" + taskTitle + "\nDeadline: " + deadline);
        mailSender.send(message);
    }
}
