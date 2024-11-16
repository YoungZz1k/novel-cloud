package com.youngzz1k.novel.user.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.youngzz1k.novel.book.dto.resp.BookInfoRespDto;
import com.youngzz1k.novel.common.auth.JwtUtils;
import com.youngzz1k.novel.common.constant.CommonConsts;
import com.youngzz1k.novel.common.constant.DatabaseConsts;
import com.youngzz1k.novel.common.constant.ErrorCodeEnum;
import com.youngzz1k.novel.common.constant.SystemConfigConsts;
import com.youngzz1k.novel.common.resp.RestResp;
import com.youngzz1k.novel.config.exception.BusinessException;
import com.youngzz1k.novel.user.dao.entity.UserBookshelf;
import com.youngzz1k.novel.user.dao.entity.UserComment;
import com.youngzz1k.novel.user.dao.entity.UserFeedback;
import com.youngzz1k.novel.user.dao.entity.UserInfo;
import com.youngzz1k.novel.user.dao.mapper.UserBookshelfMapper;
import com.youngzz1k.novel.user.dao.mapper.UserCommentMapper;
import com.youngzz1k.novel.user.dao.mapper.UserFeedbackMapper;
import com.youngzz1k.novel.user.dao.mapper.UserInfoMapper;
import com.youngzz1k.novel.user.dto.req.UserCommentsReqDto;
import com.youngzz1k.novel.user.dto.req.UserInfoUptReqDto;
import com.youngzz1k.novel.user.dto.req.UserLoginReqDto;
import com.youngzz1k.novel.user.dto.req.UserRegisterReqDto;
import com.youngzz1k.novel.user.dto.resp.UserInfoRespDto;
import com.youngzz1k.novel.user.dto.resp.UserLoginRespDto;
import com.youngzz1k.novel.user.dto.resp.UserRegisterRespDto;
import com.youngzz1k.novel.user.manager.feign.BookFeignManager;
import com.youngzz1k.novel.user.manager.redis.VerifyCodeManager;
import com.youngzz1k.novel.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 会员模块 服务实现类
 *
 * @author YoungZz1k
 * @date 2024/11/17
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    private final VerifyCodeManager verifyCodeManager;

    private final UserFeedbackMapper userFeedbackMapper;

    private final UserBookshelfMapper userBookshelfMapper;

    private final UserCommentMapper userCommentMapper;

    private final BookFeignManager bookFeignManager;

    @Override
    public RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto) {
        // 校验图形验证码是否正确
        if (!verifyCodeManager.imgVerifyCodeOk(dto.getSessionId(), dto.getVelCode())) {
            // 图形验证码校验失败
            throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
        }

        // 校验手机号是否已注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        if (userInfoMapper.selectCount(queryWrapper) > 0) {
            // 手机号已注册
            throw new BusinessException(ErrorCodeEnum.USER_NAME_EXIST);
        }

        // 注册成功，保存用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(
            DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        userInfo.setUsername(dto.getUsername());
        userInfo.setNickName(dto.getUsername());
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfo.setSalt("0");
        userInfoMapper.insert(userInfo);

        // 删除验证码
        verifyCodeManager.removeImgVerifyCode(dto.getSessionId());

        // 生成JWT 并返回
        return RestResp.ok(
            UserRegisterRespDto.builder()
                .token(JwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
                .uid(userInfo.getId())
                .build()
        );

    }

    @Override
    public RestResp<UserLoginRespDto> login(UserLoginReqDto dto) {
        // 查询用户信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }

        // 判断密码是否正确
        if (!Objects.equals(userInfo.getPassword()
            , DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            // 密码错误
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        // 登录成功，生成JWT并返回
        return RestResp.ok(UserLoginRespDto.builder()
            .token(JwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
            .uid(userInfo.getId())
            .nickName(userInfo.getNickName()).build());
    }

    @Override
    public RestResp<Void> saveFeedback(Long userId, String content) {
        UserFeedback userFeedback = new UserFeedback();
        userFeedback.setUserId(userId);
        userFeedback.setContent(content);
        userFeedback.setCreateTime(LocalDateTime.now());
        userFeedback.setUpdateTime(LocalDateTime.now());
        userFeedbackMapper.insert(userFeedback);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptReqDto dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setUserSex(dto.getUserSex());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteFeedback(Long userId, Long id) {
        QueryWrapper<UserFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
            .eq(DatabaseConsts.UserFeedBackTable.COLUMN_USER_ID, userId);
        userFeedbackMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getBookshelfStatus(Long userId, String bookId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        return RestResp.ok(
            userBookshelfMapper.selectCount(queryWrapper) > 0
                ? CommonConsts.YES
                : CommonConsts.NO
        );
    }

    @Override
    public RestResp<UserInfoRespDto> getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return RestResp.ok(UserInfoRespDto.builder()
            .nickName(userInfo.getNickName())
            .userSex(userInfo.getUserSex())
            .userPhoto(userInfo.getUserPhoto())
            .build());
    }

    @Override
    public RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), userIds);
        return RestResp.ok(
            userInfoMapper.selectList(queryWrapper).stream().map(v -> UserInfoRespDto.builder()
                .id(v.getId())
                .username(v.getUsername())
                .userPhoto(v.getUserPhoto())
                .build()).collect(Collectors.toList()));
    }

    @Override
    public List<UserCommentsReqDto> listComments(Long userId,Integer pageNum, Integer pageSize) {
        QueryWrapper<UserComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<UserComment> userComments = userCommentMapper.selectList(queryWrapper);

        List<UserCommentsReqDto> res = new ArrayList<>();

        for (UserComment userComment : userComments) {
            UserCommentsReqDto userCommentsReqDto = new UserCommentsReqDto();

            RestResp<List<BookInfoRespDto>> bookInfoByIds = bookFeignManager.listBookInfoByIds(Lists.newArrayList(userComment.getBookId()));

            for (BookInfoRespDto bookInfoByIdsDatum : bookInfoByIds.getData()) {
                String bookName = bookInfoByIdsDatum.getBookName();

                userCommentsReqDto.setCommentId(userComment.getId());
                userCommentsReqDto.setCommentBook(bookName);
                userCommentsReqDto.setCommentContent(userComment.getCommentContent());
                userCommentsReqDto.setCommentTime(userComment.getUpdateTime());
                userCommentsReqDto.setCommentBookPic(bookInfoByIdsDatum.getPicUrl());

                res.add(userCommentsReqDto);
            }

        }

        return res;
    }
}
