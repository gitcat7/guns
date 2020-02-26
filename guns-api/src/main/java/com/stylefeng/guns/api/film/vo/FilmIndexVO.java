package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FilmIndexVO implements Serializable {

    private List<BannerVO> banners;
    private FilmVO hotFilms;//热映
    private FilmVO soonFilms;//即将上映
    private List<FilmInfo> boxRanking;//票房排行榜
    private List<FilmInfo> expectRanking;//人气排行榜
    private List<FilmInfo> top;//前10,经典
}
