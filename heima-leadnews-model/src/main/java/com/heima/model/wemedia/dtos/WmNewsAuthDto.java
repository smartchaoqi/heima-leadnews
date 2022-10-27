package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class WmNewsAuthDto extends PageRequestDto {
    private Integer id;
    private String msg;
    private Integer status;
    private String title;
}
