package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient scheduleClient;

    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟队列中");

        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        scheduleClient.addTask(task);
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 消费任务，审核文章
     */
    @Override
    @Scheduled(fixedRate = 1000)
    public void scanNewsByTask() {
        log.info("消费任务，审核文章");
        ResponseResult result = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if (Objects.equals(200,result.getCode())&&result.getData()!=null){
            Task task = JSON.parseObject(JSON.toJSONString(result.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }
}
