package com.chen.context.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.common.utils.Consts;
import com.chen.common.utils.R;
import com.chen.context.entity.BlBlog;
import com.chen.context.entity.vo.BlogImgUrl;
import com.chen.context.service.BlBlogService;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 博客表 前端控制器
 * </p>
 *
 * @author chen
 * @since 2021-02-14
 */
@RestController
@RequestMapping("/root")
public class BlBlogControllerRoot {

    @Autowired
    BlBlogService blBlogService;  //注入 BlBlogService

    @Autowired
    @Qualifier(value = "restHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 给路由守卫返回是否登录成功
     * 因为设置了 SpringSecurity 的角色权限限制，所以如果能够请求到该api那就登录成功了
     * @return
     */
    @GetMapping("status")
    public R UserStatus() {

        return R.ok().data(Consts.CODE, 1).data("data","登录成功");
    }

    /**
     * 添加博客
     *
     * @author chen
     */
    @PostMapping("saveBlog")
    public R saveBlog(@RequestBody BlBlog blBlog) {
        //先判断新文章的标题是否唯一
        QueryWrapper<BlBlog> t = new QueryWrapper<>();
        t.select("blog_title").eq("blog_title",blBlog.getBlogTitle());

        if(blBlogService.getOne(t) == null) {

            blBlogService.save(blBlog);

            //因为前端提交来的blBlog对象还缺少 id，点赞数等，我只想要获取到 mysql 自动生成的id
            QueryWrapper<BlBlog> queryWrapper = new QueryWrapper<>();
            //只查询 blog_id 字段
            queryWrapper.select("blog_id").eq("blog_title", blBlog.getBlogTitle());
            blBlog.setBlogId(blBlogService.getOne(queryWrapper).getBlogId());

            //向ES添加文档
            //创建索引请求，并将数据放入请求
            IndexRequest indexRequest = new IndexRequest("blog")
                    .timeout("1s")
                    .source(JSON.toJSONString(blBlog), XContentType.JSON);

            //RestHighLevelClient客户端发送索引请求，并获取响应结果
            try {
                client.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return R.ok().data(Consts.CODE, 1);
        } else return R.error();
    }

    /**
     * 删除博客
     *
     * @author chen
     */
    @GetMapping("delete/{id}")
    public R delete(@PathVariable("id") String id) {
        //先查询数据获取到该文章的文档数据
        SearchRequest searchRequest = new SearchRequest("blog");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件-完全匹配，博客文章id
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("blogId",id);
        searchSourceBuilder.query(termQueryBuilder);

        searchSourceBuilder.timeout(new TimeValue(60)); //设置超时

        //将查询请求放入请求
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;

        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取到文档ID
        String documentId = null;

        for (SearchHit documentFields : searchResponse.getHits().getHits()){
            documentId = documentFields.getId();
            try {
                //删除ES文档内容
                DeleteRequest deleteRequest = new DeleteRequest()
                        .index("blog")
                        .id(documentId)
                        .timeout("1s");    //设置超时

                client.delete(deleteRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        blBlogService.removeById(id);
        return R.ok().data(Consts.CODE, 1);
    }

    /**
     * 修改博客
     *
     * @author chen
     */
    @PostMapping("updateBlog")
    public R updateBlog(@RequestBody BlBlog blBlog) {
        //先查询数据获取到该文章的文档数据
        SearchRequest searchRequest = new SearchRequest("blog");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件-完全匹配，博客文章id
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("blogId",blBlog.getBlogId());
        searchSourceBuilder.query(termQueryBuilder);

        searchSourceBuilder.timeout(new TimeValue(60)); //设置超时

        //将查询请求放入请求
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;

        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取到文档ID
        String documentId = null;

        for (SearchHit documentFields : searchResponse.getHits().getHits()){
            documentId = documentFields.getId();
            try {
                //更新ES文档内容
                UpdateRequest updateRequest = new UpdateRequest()
                        .index("blog")
                        .id(documentId)
                        .doc(JSON.toJSONString(blBlog),XContentType.JSON)
                        .timeout("1s");    //设置超时

                client.update(updateRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //mybatis-plus 提供的save接口，插入一条记录，只需要传入一个实体类对象
        boolean b = blBlogService.updateById(blBlog);
        if (b) return R.ok().data(Consts.CODE, 1);   //如果插入成功就返回一个成功的消息
        else return R.error();
    }

    /**
     * 更新文章图片服务器地址，如 ![](www.baidu.com/img/1.png)
     * 变成 ![](www.google.com/img/1.png)
     *
     * @author chen
     */
    @PostMapping("updateImgUrl")
    public R updateImgUrl(@RequestBody BlogImgUrl blogImgUrl) {
        QueryWrapper<BlBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("blog_id", "blog_content", "blog_content_img");
        List<BlBlog> list = blBlogService.list(queryWrapper);

        String content = null;
        String replacedContent = null;
        String replaceContent = null;
        Pattern p = null;
        Matcher matcher = null;

        for (BlBlog blog : list) {
            //先将取到的文章内容赋予 content
            content = blog.getBlogContent();

            //被替换内容，正则匹配 ![](https://www.baidu.com/img/ 这段内容
            replacedContent = "!\\[\\]\\(.*?//.*?/.*?/";

            //将内容变成 传来的 url + 文件图片地址/
            replaceContent = "![](" + blogImgUrl.getUrl() + blog.getBlogContentImg() + "/";

            //创建 Pattern 对象
            p = Pattern.compile(replacedContent);
            //创建 matcher 对象
            matcher = p.matcher(content);
            content = matcher.replaceAll(replaceContent);

            //将新内容写入文章
            blog.setBlogContent(content);

            blBlogService.updateById(blog);
        }
        return R.ok().data(Consts.CODE, 1);
    }

    /**
     * 匹配3个 /
     *
     * @author chen
     */
    @PostMapping("updateImgUrlP")
    public R updateImgUrlP(@RequestBody BlogImgUrl blogImgUrl) {
        QueryWrapper<BlBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("blog_id", "blog_content", "blog_content_img");
        List<BlBlog> list = blBlogService.list(queryWrapper);

        String content = null;
        String replacedContent = null;
        String replaceContent = null;
        Pattern p = null;
        Matcher matcher = null;

        for (BlBlog blog : list) {
            //先将取到的文章内容赋予 content
            content = blog.getBlogContent();

            //被替换内容，正则匹配 ![](https://www.baidu.com/img/java/ 这段内容
            replacedContent = "!\\[\\]\\(.*?//.*?/.*?/.*?/";

            //将内容变成 传来的 url + 文件图片地址/
            replaceContent = "![](" + blogImgUrl.getUrl() + blog.getBlogContentImg() + "/";

            //创建 Pattern 对象
            p = Pattern.compile(replacedContent);
            //创建 matcher 对象
            matcher = p.matcher(content);
            content = matcher.replaceAll(replaceContent);

            //将新内容写入文章
            blog.setBlogContent(content);

            blBlogService.updateById(blog);
        }
        return R.ok().data(Consts.CODE, 1);
    }

    /**
     * 更新文章封面图片服务器地址，如 https://www.google.com/PageImg/java.png
     * 变成 https://www.chendark.com/PageImg/java.png
     *
     * @author chen
     */
    @PostMapping("updatePageImgUrl")
    public R updatePageImgUrl(@RequestBody BlogImgUrl blogImgUrl) {
        QueryWrapper<BlBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("blog_id", "blog_image");
        List<BlBlog> list = blBlogService.list(queryWrapper);

        String content = null;
        String replacedContent = null;
        String replaceContent = null;
        Pattern p = null;
        Matcher matcher = null;

        for (BlBlog blog : list) {
            //先将取到的文章封面地址内容内容赋予 content
            content = blog.getBlogImage();

            //被替换内容，正则匹配 https://www.google.com/PageImg/ 这段内容
            replacedContent = ".*?//.*?/.*?/";

            //将内容变成 传来的 url ,借用 blogImgUrl 这个对象来介绍传来的值
            replaceContent = blogImgUrl.getUrl();

            //创建 Pattern 对象
            p = Pattern.compile(replacedContent);
            //创建 matcher 对象
            matcher = p.matcher(content);
            content = matcher.replaceAll(replaceContent);

            //将新内容写入
            blog.setBlogImage(content);

            blBlogService.updateById(blog);
        }
        return R.ok().data(Consts.CODE, 1);
    }

    /**
     * 匹配三个 /
     *
     * @author chen
     */
    @PostMapping("updatePageImgUrlP")
    public R updatePageImgUrlP(@RequestBody BlogImgUrl blogImgUrl) {
        QueryWrapper<BlBlog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("blog_id", "blog_image");
        List<BlBlog> list = blBlogService.list(queryWrapper);

        String content = null;
        String replacedContent = null;
        String replaceContent = null;
        Pattern p = null;
        Matcher matcher = null;

        for (BlBlog blog : list) {
            //先将取到的文章封面地址内容内容赋予 content
            content = blog.getBlogImage();

            //被替换内容，正则匹配 https://www.google.com/PageImg/ 这段内容
            replacedContent = ".*?//.*?/.*?/.*?/";

            //将内容变成 传来的 url ,借用 blogImgUrl 这个对象来介绍传来的值
            replaceContent = blogImgUrl.getUrl();

            //创建 Pattern 对象
            p = Pattern.compile(replacedContent);
            //创建 matcher 对象
            matcher = p.matcher(content);
            content = matcher.replaceAll(replaceContent);

            //将新内容写入
            blog.setBlogImage(content);

            blBlogService.updateById(blog);
        }
        return R.ok().data(Consts.CODE, 1);
    }

    /**
     * 创建 blog 索引
     * 导出JSON信息并添加文档至(PUT)ES索引
     * 查询出所有匹配的列表
     *
     * @author chen
     */
    @PutMapping("putES")
    public R putES() {
        //创建索引
        CreateIndexRequest request = new CreateIndexRequest("blog");
        //RestHighLevelClient客户端执行创建请求，并返回请求响应
        CreateIndexResponse createIndexResponse = null;
        try {
            createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //索引创建成功才执行
        if (createIndexResponse.isAcknowledged()) {
            //从数据库获取到文章数据
            List<BlBlog> blogList = blBlogService.list();

            //创建BulkRequest请求
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.timeout("6s");  //设置超时

            //批量添加
            for (int i = 0; i < blogList.size(); i++) {
                bulkRequest.add(
                        new IndexRequest("blog")
                                .source(JSONArray.toJSONString(blogList.get(i)), XContentType.JSON)
                );
            }

            BulkResponse bulkResponse = null;
            try {
                bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bulkResponse.hasFailures()) { //如果操作失败则返回 error
                return R.error();
            } else {
                return R.ok();
            }
        }else {
            return R.error();
        }
    }

    /**
     * 删除 blog 索引
     *
     * @author chen
     */
    @DeleteMapping("deleteES")
    public R deleteES() {
        DeleteIndexRequest request = new DeleteIndexRequest("blog");
        try {
            client.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.ok();
    }

}
