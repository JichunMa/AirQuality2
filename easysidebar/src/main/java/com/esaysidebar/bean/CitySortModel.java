package com.esaysidebar.bean;

public class CitySortModel {

    private String name;//显示的数据
    private String sortLetters;//显示数据拼音的首字母
    private String cityPY;//城市的拼音

    public CitySortModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getCityPY() {
        return cityPY;
    }

    public void setCityPY(String cityPY) {
        this.cityPY = cityPY;
    }
}
