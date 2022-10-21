package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;
    @Override
    @Async
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        //1.获取文章内容
        if (StringUtils.isNotBlank(content)) {
            //2.生成模板文件
            StringWriter writer = new StringWriter();
            Template template;
            try {
                template = configuration.getTemplate("article.ftl");
                Map<String, Object> params = new HashMap<>();
                params.put("content", JSONArray.parseArray(content));
                template.process(params,writer);
                InputStream is = new ByteArrayInputStream(writer.toString().getBytes());
                //3.上传文件
                String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", is);
                //4.修改staticurl
                ApArticle article = new ApArticle();
                article.setId(apArticle.getId());
                article.setStaticUrl(path);
                apArticleService.updateById(article);
                System.out.println(article);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
