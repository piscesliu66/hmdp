package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3. 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 保存到 session 中
        session.setAttribute("code", code);
        // 5. 发送验证码
        log.info("发送手机验证码成功：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号，
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 手机号不正确，报错
            return Result.fail("手机号不正确");
        }

        // 3. 校验验证码，将前台传递的验证码与 session 中的验证码判断是否相等
        String code = loginForm.getCode();
        String cacheCode = (String) session.getAttribute("code");
        if (code == null || !code.equals(cacheCode)) {
            // 4. 不一致，报错
            return Result.fail("验证码不正确");
        }

        // 5. 根据手机号查询数据库，判断用户是否存在，
       /* QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        User user = baseMapper.selectOne(queryWrapper);*/
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 6. 不存在，则创建
            user = createWithPhone(phone);
        }

        // 7. 将用户信息储存在 session 中
        session.setAttribute("user", user);
        return Result.ok();
    }

    private User createWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        // 2. 保存用户
        save(user);
        return user;
    }
}
