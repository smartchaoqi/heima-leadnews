package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ReadBehaviorService;
import com.heima.model.bahavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/read_behavior")
@RestController
public class ApReadBehaviorController {

    @Autowired
    private ReadBehaviorService readBehaviorService;

    @PostMapping
    public ResponseResult readBehavior(@RequestBody ReadBehaviorDto dto){
        return readBehaviorService.readBehavior(dto);
    }

}
