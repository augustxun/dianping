package com.hmdp.service;

import com.hmdp.common.Result;
import com.hmdp.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result likeBlog(Long id);
    /**
     * 查询博文关注者
     * @param max
     * @param offset
     * @return
     */
    Result queryBlogOfFollow(Long max, Integer offset);

    /**
     * 保存博文并推送
     * @param blog
     * @return
     */
    Result saveBlog(Blog blog);

    /**
     * 获取博文点赞数
     * @param id
     * @return
     */
    Result queryBlogLikes(Long id);
}
