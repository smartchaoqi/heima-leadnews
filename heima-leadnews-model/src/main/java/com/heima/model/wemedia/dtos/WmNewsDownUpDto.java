package com.heima.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmNewsDownUpDto {
    private Long id;
    /**
     * 是否上下架
     */
    private Short enable;
}
