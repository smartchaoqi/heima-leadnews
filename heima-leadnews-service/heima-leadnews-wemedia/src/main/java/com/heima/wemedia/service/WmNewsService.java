package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 条件查询文章列表
     * @param dto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     * 发布或修改文章
     * @param wmNewsDto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto wmNewsDto);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDownUpDto dto);

    ResponseResult listVo(WmNewsAuthDto dto);

    ResponseResult oneVo(Long id);

    ResponseResult authFail(WmNewsAuthDto dto);

    ResponseResult authPass(WmNewsAuthDto dto);
}
