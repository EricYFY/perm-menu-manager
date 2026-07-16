package com.example.permmenu.config;

import com.example.permmenu.dto.ResultVO;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLSyntaxErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        String msg = e.getMessage();
        String causeMsg = cause.getMessage();
        if (msg == null)
            msg = "";
        if (causeMsg == null)
            causeMsg = "";

        // 处理查不到表的常见错误
        if (e instanceof BadSqlGrammarException || cause instanceof SQLSyntaxErrorException ||
                msg.contains("doesn't exist") || causeMsg.contains("doesn't exist")) {
            return ResultVO.error(500, "数据库中不存在该表，请检查当前环境的配置是否正确！(详细报错: " + getShortMsg(causeMsg) + ")");
        }

        if (msg.contains("Communications link failure") || causeMsg.contains("Communications link failure")) {
            return ResultVO.error(500, "数据库连接失败，请检查环境的网络或账号密码是否正确！");
        }

        return ResultVO.error(500, "操作失败: " + getShortMsg(causeMsg));
    }

    private String getShortMsg(String fullMsg) {
        if (fullMsg.length() > 200) {
            return fullMsg.substring(0, 200) + "...";
        }
        return fullMsg;
    }
}
