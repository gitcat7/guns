package com.stylefeng.guns.rest.modular.film.vo;

import com.stylefeng.guns.api.film.vo.BannerVO;
import com.stylefeng.guns.api.film.vo.FilmInfo;
import com.stylefeng.guns.api.film.vo.FilmVO;
import lombok.Data;

import java.util.List;

@Data
public class FilmIndexVO {

    private List<BannerVO> banners;
    private FilmVO hotFilms;//热映
    private FilmVO soonFilms;//即将上映
    private List<FilmInfo> boxRanking;//受欢迎影片
    private List<FilmInfo> expectRanking;//预约排行
    private List<FilmInfo> top;//前10,经典
}
