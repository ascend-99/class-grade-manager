package com.zhihang.grade.web;

import com.zhihang.grade.model.AnalyticsData;
import com.zhihang.grade.model.CourseStat;
import com.zhihang.grade.model.DistributionPoint;
import com.zhihang.grade.model.StudentRank;
import com.zhihang.grade.service.AnalyticsService;
import com.zhihang.grade.util.JsonUtil;
import com.zhihang.grade.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsServlet extends BaseServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long classId = Web.longParam(request, "classId", 0);
        long courseId = Web.longParam(request, "courseId", 0);
        String examName = Web.param(request, "examName");
        AnalyticsData data = analyticsService.load(
                classId > 0 ? classId : null,
                courseId > 0 ? courseId : null,
                examName);

        List<String> distLabels = new ArrayList<>();
        List<Integer> distCounts = new ArrayList<>();
        for (DistributionPoint point : data.getDistribution()) {
            distLabels.add(point.getLabel());
            distCounts.add(point.getCount());
        }
        List<String> courseLabels = new ArrayList<>();
        List<Double> courseAverages = new ArrayList<>();
        List<Double> coursePassRates = new ArrayList<>();
        for (CourseStat stat : data.getCourseStats()) {
            courseLabels.add(stat.getCourseName());
            courseAverages.add(stat.getAverage());
            coursePassRates.add(stat.getPassRate());
        }
        List<String> rankNames = new ArrayList<>();
        List<Double> rankAverages = new ArrayList<>();
        for (StudentRank rank : data.getRanking()) {
            rankNames.add(rank.getName());
            rankAverages.add(rank.getAverage());
        }

        request.setAttribute("data", data);
        request.setAttribute("distLabelsJson", JsonUtil.strings(distLabels));
        request.setAttribute("distCountsJson", JsonUtil.numbers(distCounts));
        request.setAttribute("courseLabelsJson", JsonUtil.strings(courseLabels));
        request.setAttribute("courseAveragesJson", JsonUtil.numbers(courseAverages));
        request.setAttribute("coursePassRatesJson", JsonUtil.numbers(coursePassRates));
        request.setAttribute("rankNamesJson", JsonUtil.strings(rankNames));
        request.setAttribute("rankAveragesJson", JsonUtil.numbers(rankAverages));
        request.setAttribute("pageTitle", "统计分析");
        request.setAttribute("pageEyebrow", "INSIGHTS");
        request.setAttribute("activeNav", "analytics");
        forward(request, response, "analytics.jsp");
    }
}
