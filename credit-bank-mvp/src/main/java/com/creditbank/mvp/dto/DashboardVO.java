package com.creditbank.mvp.dto;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏聚合数据。
 */
public class DashboardVO {

    /** KPI 卡片 */
    private KpiDTO kpi;

    /** 分类统计（继续教育/职业教育/...） */
    private List<Map<String, Object>> categories;

    /** 同比对比 */
    private List<YoYDTO> yoy;

    /** 省份分布 */
    private List<Map<String, Object>> provinces;

    /** 月度趋势 */
    private List<Map<String, Object>> monthlyTrend;

    public KpiDTO getKpi() { return kpi; }
    public void setKpi(KpiDTO kpi) { this.kpi = kpi; }
    public List<Map<String, Object>> getCategories() { return categories; }
    public void setCategories(List<Map<String, Object>> categories) { this.categories = categories; }
    public List<YoYDTO> getYoy() { return yoy; }
    public void setYoy(List<YoYDTO> yoy) { this.yoy = yoy; }
    public List<Map<String, Object>> getProvinces() { return provinces; }
    public void setProvinces(List<Map<String, Object>> provinces) { this.provinces = provinces; }
    public List<Map<String, Object>> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<Map<String, Object>> monthlyTrend) { this.monthlyTrend = monthlyTrend; }

    public static class KpiDTO {
        private long totalArchives;
        private long totalCertifications;
        private long totalRules;
        private long totalCenters;

        public long getTotalArchives() { return totalArchives; }
        public void setTotalArchives(long totalArchives) { this.totalArchives = totalArchives; }
        public long getTotalCertifications() { return totalCertifications; }
        public void setTotalCertifications(long totalCertifications) { this.totalCertifications = totalCertifications; }
        public long getTotalRules() { return totalRules; }
        public void setTotalRules(long totalRules) { this.totalRules = totalRules; }
        public long getTotalCenters() { return totalCenters; }
        public void setTotalCenters(long totalCenters) { this.totalCenters = totalCenters; }
    }

    public static class YoYDTO {
        private String name;
        private long lastYear;
        private long thisYear;
        private long growth;
        private String growthRate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getLastYear() { return lastYear; }
        public void setLastYear(long lastYear) { this.lastYear = lastYear; }
        public long getThisYear() { return thisYear; }
        public void setThisYear(long thisYear) { this.thisYear = thisYear; }
        public long getGrowth() { return growth; }
        public void setGrowth(long growth) { this.growth = growth; }
        public String getGrowthRate() { return growthRate; }
        public void setGrowthRate(String growthRate) { this.growthRate = growthRate; }
    }
}
