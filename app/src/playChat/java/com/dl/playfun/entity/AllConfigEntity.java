package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author wulei
 */
public class AllConfigEntity {

    @SerializedName("program_time")
    private List<ConfigItemEntity> programTime;
    @SerializedName("hope_object")
    private List<ConfigItemEntity> hopeObject;
    @SerializedName("report_reason")
    private List<ConfigItemEntity> reportReason;
    private EvaluateConfigEntity evaluate;
    private List<OccupationConfigItemEntity> occupation;
    private List<ConfigItemEntity> city;
    private List<ConfigItemEntity> theme;
    private List<ConfigItemEntity> height;
    private List<ConfigItemEntity> weight;
    private SystemConfigEntity config;
    @SerializedName("default_home_page")
    private String defaultHomePage;

    //任务中心相关配置
    private SystemConfigTaskEntity task;

    public SystemConfigTaskEntity getTask() {
        return task;
    }

    public void setTask(SystemConfigTaskEntity task) {
        this.task = task;
    }

    public List<ConfigItemEntity> getProgramTime() {
        return programTime;
    }

    public void setProgramTime(List<ConfigItemEntity> programTime) {
        this.programTime = programTime;
    }

    public List<ConfigItemEntity> getHopeObject() {
        return hopeObject;
    }

    public void setHopeObject(List<ConfigItemEntity> hopeObject) {
        this.hopeObject = hopeObject;
    }

    public List<ConfigItemEntity> getReportReason() {
        return reportReason;
    }

    public void setReportReason(List<ConfigItemEntity> reportReason) {
        this.reportReason = reportReason;
    }

    public EvaluateConfigEntity getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(EvaluateConfigEntity evaluate) {
        this.evaluate = evaluate;
    }

    public List<OccupationConfigItemEntity> getOccupation() {
        return occupation;
    }

    public void setOccupation(List<OccupationConfigItemEntity> occupation) {
        this.occupation = occupation;
    }

    public List<ConfigItemEntity> getCity() {
        return city;
    }

    public void setCity(List<ConfigItemEntity> city) {
        this.city = city;
    }

    public List<ConfigItemEntity> getTheme() {
        return theme;
    }

    public void setTheme(List<ConfigItemEntity> theme) {
        this.theme = theme;
    }

    public List<ConfigItemEntity> getHeight() {
        return height;
    }

    public void setHeight(List<ConfigItemEntity> height) {
        this.height = height;
    }

    public List<ConfigItemEntity> getWeight() {
        return weight;
    }

    public void setWeight(List<ConfigItemEntity> weight) {
        this.weight = weight;
    }

    public SystemConfigEntity getConfig() {
        return config;
    }

    public void setConfig(SystemConfigEntity config) {
        this.config = config;
    }

    public String getDefaultHomePage() {
        return defaultHomePage;
    }

    public void setDefaultHomePage(String defaultHomePage) {
        this.defaultHomePage = defaultHomePage;
    }
}
