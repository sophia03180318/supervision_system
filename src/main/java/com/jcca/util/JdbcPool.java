package com.jcca.util;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

@Slf4j
public class JdbcPool {
    private static String url;
    private static String user;
    private static String password;
    /**
     * 初始化连接数
     */
    private static int initCount;
    /**
     * 最大连接数
     */
    private static int maxCount;
    /**
     * 当前连接数
     */
    int currentCount = 0;
    /**
     * 连接池
     */
    LinkedList<Connection> connectionPool = new LinkedList<>();
    /**
     * 错误连接重试次数
     */
    static int errConnRetry = 1000;



    /**
     * 空参构造器，执行创建数据库连接池
     */
    public JdbcPool() {
        for (int i = 0; i < initCount; i++) {
            this.connectionPool.addLast(this.createConnection());
            this.currentCount++;
        }
    }

    /**
     * 创建连接
     *
     * @return 返回Connection对象
     * @throws SQLException
     */
    private Connection createConnection() {
        Connection con = null;
        for (int i = 0; i < errConnRetry; i++) {
            try {
                con = DriverManager.getConnection(url, user, password);
                if (con != null) {
                    break;
                }
                Integer time = RandomUtil.randomInt(1, 1000);
                Thread.sleep(3000 + time);
            } catch (Exception e) {
                log.error("第{}次创建mysql连接失败", i + 1, e);
            }
        }
        if (null == con) {
            throw new RuntimeException("创建mysql连接失败");
        }

        return con;
    }

    public synchronized Connection getConnection() {
        if (this.connectionPool.size() > 0) {
            return this.connectionPool.removeFirst();
        }
        if (this.currentCount < maxCount) {
            this.currentCount++;
            return this.createConnection();
        }
        throw new RuntimeException("数据库连接池中没有可用的连接");
    }

    /**
     * 释放资源
     *
     * @param conn
     */
    public synchronized void free(Connection conn) {
        this.connectionPool.addLast(conn);
    }


}
