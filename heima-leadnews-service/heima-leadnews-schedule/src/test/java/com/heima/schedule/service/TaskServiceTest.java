package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void addTask(){
        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(50);
        task.setParameters("task task".getBytes());
        task.setExecuteTime(new Date().getTime()+10000);
        long taskId = taskService.addTask(task);
        System.out.println(taskId);
    }

}