package com.zhihang.grade.web.listener;

import com.zhihang.grade.config.Database;
import com.zhihang.grade.config.DemoData;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        long started = System.currentTimeMillis();
        Database.initialize();
        DemoData.seedIfEmpty();
        sce.getServletContext().setAttribute("appStartedAt", started);
        sce.getServletContext().log("知衡成绩管理系统启动完成，数据库：" + Database.url());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("知衡成绩管理系统已停止");
    }
}
