package com.youngzz1k.novel.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youngzz1k.novel.user.dao.entity.UserComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户评论 Mapper 接口
 * </p>
 *
 * @author YoungZz1k
 * @date 2024/11/11
 */
public interface UserCommentMapper extends BaseMapper<UserComment> {

    List<UserComment> selectComments(@Param("userId") Long userId,@Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize);
}
