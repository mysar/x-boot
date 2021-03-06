package cn.exrick.xboot.config.security;

import cn.exrick.xboot.common.constant.SecurityConstant;
import cn.exrick.xboot.common.utils.ResponseUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * 登录成功处理类
 * @author Exrickx
 */
@Slf4j
@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${xboot.tokenExpireTime}")
    private Integer tokenExpireTime;

    @Value("${xboot.saveLoginTime}")
    private Integer saveLoginTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //用户选择保存多保存登录状态几天
        String saveTime = request.getParameter(SecurityConstant.SAVE_TIME);
        if(StrUtil.isNotBlank(saveTime)&&Boolean.valueOf(saveTime)){
            tokenExpireTime = saveLoginTime * 24;
        }
        String username = ((UserDetails)authentication.getPrincipal()).getUsername();
        String authorities = new Gson().toJson(((UserDetails)authentication.getPrincipal()).getAuthorities());
        //登陆成功生成JWT
        String token = Jwts.builder()
                //主题 放入用户名
                .setSubject(username)
                //自定义属性 放入用户拥有权限
                .claim(SecurityConstant.AUTHORITIES, authorities)
                //失效时间
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpireTime * 60 * 60 * 1000))
                //签名算法和密钥
                .signWith(SignatureAlgorithm.HS512, SecurityConstant.JWT_SIGN_KEY)
                .compact();
        token = SecurityConstant.TOKEN_SPLIT + token;
        log.info(token);

        ResponseUtil.out(response, ResponseUtil.resultMap(true,200,"登录成功", token));
    }
}
