package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    public static final short MAX_PAGE_SIZE = 50;

    @Override
    public ResponseResult load(ArticleHomeDto articleHomeDto, Short type) {
        Integer size = articleHomeDto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        articleHomeDto.setSize(Math.min(size, MAX_PAGE_SIZE));


        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE)&&!type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        if(StringUtils.isEmpty(articleHomeDto.getTag())){
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        if(articleHomeDto.getMaxBehotTime() == null) articleHomeDto.setMaxBehotTime(new Date());
        if(articleHomeDto.getMinBehotTime() == null) articleHomeDto.setMinBehotTime(new Date());

        List<ApArticle> apArticles = baseMapper.loadArticleList(articleHomeDto, type);
        return ResponseResult.okResult(apArticles);
    }

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        if (articleDto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle=new ApArticle();
        BeanUtils.copyProperties(articleDto,apArticle);
        if (articleDto.getId()==null){
            //保存文章
            save(apArticle);
            //保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            //保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }else{
            //修改文章
            updateById(apArticle);
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>().eq(ApArticleContent::getArticleId, apArticle.getId()));
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }
        //生成静态文件到minio中
        articleFreemarkerService.buildArticleToMinIO(apArticle,articleDto.getContent());
        return ResponseResult.okResult(apArticle.getId());
    }

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
}
