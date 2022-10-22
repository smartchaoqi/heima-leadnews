package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Override
    public long addTask(Task task) {
        //1.添加任务到数据库中
        boolean success = addTaskToDb(task);

        if (success){
            //2.添加任务到redis中
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(long taskId) {
        //删除任务,更新任务日志
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);
        //删除redis数据
        if (task!=null){
            removeTaskFromCache(task);
            return true;
        }
        return false;
    }

    @Override
    public Task poll(int type, int priority) {
        try{
            String key=type+"_"+priority;
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(task_json)){
                Task task = JSON.parseObject(task_json, Task.class);
                //修改数据库信息
                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
                return task;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("poll task exception");
        }
        return null;
    }

    /**
     * 删除redis数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {
        String key=task.getTaskType()+"_"+task.getPriority();

        if (task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else{
            cacheService.zRemove(ScheduleConstants.FUTURE+key,JSON.toJSONString(task));
        }

    }

    /**
     * 删除任务，更新任务日志
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        try{
            //删除任务
            taskinfoMapper.deleteById(taskId);
            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            Task task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
            return task;
        }catch (Exception e){
            log.error("task cancel exception taskId={}",taskId);
            e.printStackTrace();
        }
        return null;
    }


    @Autowired
    private CacheService cacheService;

    /**
     * 任务添加到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        String key=task.getTaskType()+"_"+task.getPriority();
        //获取5分钟之后的时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE,5);
        long nextScheduleTime = instance.getTimeInMillis();

        //2.1 执行时间小于等于当前时间 添加到list
        if (task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        }else if (task.getExecuteTime()<=nextScheduleTime){
            //2.2 执行时间大于当前时间 && 小于预设时间(now()+5min) 存入zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),task.getExecuteTime());
        }
    }

    private boolean addTaskToDb(Task task) {
        try{
            //保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //设置taskId
            task.setTaskId(taskinfo.getTaskId());

            //保存任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 未来数据定时刷新
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){

        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);

        if (StringUtils.isNotBlank(token)){
            //所有未来数据key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

            for (String futureKey : futureKeys) {
                String topicKey=ScheduleConstants.TOPIC+futureKey.split(ScheduleConstants.FUTURE)[1];

                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                //同步数据
                if (!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                }
            }
        }
    }
}
