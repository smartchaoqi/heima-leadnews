package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
public class ApUserAuthController {
    @Autowired
    private ApUserRealnameService apUserRealnameService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody AuthDto authDto){
        return apUserRealnameService.list(authDto);
    }

    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto authDto){
        return apUserRealnameService.authFail(authDto);
    }

    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto authDto){
        return apUserRealnameService.authPass(authDto);
    }

}
