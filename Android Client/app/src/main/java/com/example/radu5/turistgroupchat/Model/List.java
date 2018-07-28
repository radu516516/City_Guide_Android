package com.example.radu5.turistgroupchat.Model;

/**
 * Created by radu5 on 5/3/2018.
 */

public class List
{
    private int id;
    private String title;
    private String description;
    private int creator;
    private int city_id;
    private String thumbnailUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List(int id, String title, String description, int creator, int city_id, String thumbnailUrl) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.city_id = city_id;
        this.thumbnailUrl = thumbnailUrl;
    }
    public List() {

    }
}
