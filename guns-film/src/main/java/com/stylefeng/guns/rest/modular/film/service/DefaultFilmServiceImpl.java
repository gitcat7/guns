package com.stylefeng.guns.rest.modular.film.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.rest.common.persistence.dao.*;
import com.stylefeng.guns.rest.common.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Service(interfaceClass = FilmServiceApi.class)
public class DefaultFilmServiceImpl implements FilmServiceApi {

    @Autowired
    private MoocBannerTMapper moocBannerTMapper;
    @Autowired
    private MoocFilmTMapper moocFilmTMapper;
    @Autowired
    private MoocCatDictTMapper moocCatDictTMapper;
    @Autowired
    private MoocSourceDictTMapper moocSourceDictTMapper;
    @Autowired
    private MoocYearDictTMapper moocYearDictTMapper;
    @Autowired
    private MoocFilmInfoTMapper moocFilmInfoTMapper;
    @Autowired
    private MoocActorTMapper moocActorTMapper;

    @Override
    public List<BannerVO> getBanners() {
        List<BannerVO> result = new ArrayList<>();
        List<MoocBannerT> moocBannerS = moocBannerTMapper.selectList(null);
        for (MoocBannerT moocBannerT : moocBannerS) {
            BannerVO bannerVO = new BannerVO();
            bannerVO.setBannerId(moocBannerT.getUuid() + "");
            bannerVO.setBannerAddress(moocBannerT.getBannerAddress());
            bannerVO.setBannerUrl(moocBannerT.getBannerUrl());
            result.add(bannerVO);
        }
        return result;
    }

    private List<FilmInfo> getFilmInfos(List<MoocFilmT> moocFilmS){
        ArrayList<FilmInfo> filmInfos = new ArrayList<>();
        for (MoocFilmT moocFilmT : moocFilmS) {
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setScore(moocFilmT.getFilmScore());
            filmInfo.setImgAddress(moocFilmT.getImgAddress());
            filmInfo.setFilmType(moocFilmT.getFilmType());
            filmInfo.setFilmScore(moocFilmT.getFilmScore());
            filmInfo.setFilmName(moocFilmT.getFilmName());
            filmInfo.setFilmId(moocFilmT.getUuid() + "");
            filmInfo.setExpectNum(moocFilmT.getFilmPresalenum());
            filmInfo.setBoxNum(moocFilmT.getFilmBoxOffice());
            filmInfo.setShowTime(DateUtil.getDay(moocFilmT.getFilmTime()));
            filmInfos.add(filmInfo);
        }
        return filmInfos;
    }

    @Override
    public FilmVO getHotFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        //判断受是首页需要的内容
        if(isLimit){
            //如果是,则限制条数,限制内容为热映影片
            Page<MoocFilmT> page = new Page<>(1, nums);
            List<MoocFilmT> moocFilmS = moocFilmTMapper.selectPage(page, entityWrapper);
            //组织filmInfos
            filmInfos = getFilmInfos(moocFilmS);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setFilmNum(moocFilmS.size());
        }else {
            //如果不是,则是列表页,同样需要限制内容为热映影片
            Page<MoocFilmT> page;
            //根据sortId的不同,来组织不同的Page对象
            //1-按照热门搜索,2-按时间搜索,3-按评价搜搜
            switch (sortId){
                case 1:
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
                case 2:
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage, nums, "film_score");
                    break;
                default:
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
            }
            //如果sourceId,yearID,catId不为99,则表示要按照对应的编号进行查询
            if(sourceId != 99){
                entityWrapper.eq("film_source", sourceId);
            }
            if(yearId != 99){
                entityWrapper.eq("film_date", yearId);
            }
            if(catId != 99){
                //以#隔开存储的,#4#2#22#
                String catStr = "%#" + catId + "#%";
                entityWrapper.like("film_cats", catStr + "");
            }
            List<MoocFilmT> moocFilmS = moocFilmTMapper.selectPage(page, entityWrapper);
            //组织filmInfos
            filmInfos = getFilmInfos(moocFilmS);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setFilmNum(moocFilmS.size());

            //需要总页数
            int totalCount = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (int) Math.ceil(totalCount*1.0/nums);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);
        }
        return filmVO;
    }

    @Override
    public FilmVO getSoonFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {

        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "2");
        //判断受是首页需要的内容
        if(isLimit){
            //如果是,则限制条数,限制内容为热映影片
            Page<MoocFilmT> page = new Page<>(1, nums);
            List<MoocFilmT> moocFilmS = moocFilmTMapper.selectPage(page, entityWrapper);
            //组织filmInfos
            filmInfos = getFilmInfos(moocFilmS);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setFilmNum(moocFilmS.size());
        }else {
            //如果不是,则是列表页,同样需要限制内容为即将上映影片
            Page<MoocFilmT> page;
            //根据sortId的不同,来组织不同的Page对象
            //1-按照热门搜索,2-按时间搜索,3-按评价搜搜
            switch (sortId){
                case 1:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
                case 2:
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
                default:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
            }
            //如果sourceId,yearID,catId不为99,则表示要按照对应的编号进行查询
            if(sourceId != 99){
                entityWrapper.eq("film_source", sourceId);
            }
            if(yearId != 99){
                entityWrapper.eq("film_date", yearId);
            }
            if(catId != 99){
                //以#隔开存储的,#4#2#22#
                String catStr = "%#" + catId + "#%";
                entityWrapper.like("film_cats", catStr + "");
            }
            List<MoocFilmT> moocFilmS = moocFilmTMapper.selectPage(page, entityWrapper);
            //组织filmInfos
            filmInfos = getFilmInfos(moocFilmS);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setFilmNum(moocFilmS.size());

            //需要总页数
            int totalCount = moocFilmTMapper.selectCount(entityWrapper);
            int totalPages = (int) Math.ceil(totalCount*1.0/nums);
            filmVO.setFilmInfo(filmInfos);
            filmVO.setTotalPage(totalPages);
            filmVO.setNowPage(nowPage);

        }
        return filmVO;
    }

    @Override
    public FilmVO getClassicFilms(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVO filmVO = new FilmVO();
        List<FilmInfo> filmInfos = new ArrayList<>();

        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "3");
        //如果不是,则是列表页,同样需要限制内容为上映影片
        Page<MoocFilmT> page;
        //根据sortId的不同,来组织不同的Page对象
        //1-按照热门搜索,2-按时间搜索,3-按评价搜搜
        switch (sortId){
            case 1:
                page = new Page<>(nowPage, nums, "film_box_office");
                break;
            case 2:
                page = new Page<>(nowPage, nums, "film_time");
                break;
            case 3:
                page = new Page<>(nowPage, nums, "film_score");
                break;
            default:
                page = new Page<>(nowPage, nums, "film_box_office");
                break;
        }
        //如果sourceId,yearID,catId不为99,则表示要按照对应的编号进行查询
        if(sourceId != 99){
            entityWrapper.eq("film_source", sourceId);
        }
        if(yearId != 99){
            entityWrapper.eq("film_date", yearId);
        }
        if(catId != 99){
            //以#隔开存储的,#4#2#22#
            String catStr = "%#" + catId + "#%";
            entityWrapper.like("film_cats", catStr + "");
        }
        List<MoocFilmT> moocFilmS = moocFilmTMapper.selectPage(page, entityWrapper);
        //组织filmInfos
        filmInfos = getFilmInfos(moocFilmS);
        filmVO.setFilmInfo(filmInfos);
        filmVO.setFilmNum(moocFilmS.size());

        //需要总页数
        int totalCount = moocFilmTMapper.selectCount(entityWrapper);
        int totalPages = (int) Math.ceil(totalCount*1.0/nums);
        filmVO.setFilmInfo(filmInfos);
        filmVO.setTotalPage(totalPages);
        filmVO.setNowPage(nowPage);
        return filmVO;
    }

    @Override
    public List<FilmInfo> getBoxRanking() {
        //条件 -> 正在上映的,票房前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        Page<MoocFilmT> page = new Page<>(1, 10, "film_box_office");
        List<MoocFilmT> moocFilmTS = moocFilmTMapper.selectPage(page, entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilmTS);
        return filmInfos;
    }

    @Override
    public List<FilmInfo> getExpectRanking() {
        //条件 -> 即将上映的,预售前名10
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "2");
        Page<MoocFilmT> page = new Page<>(1, 10, "film_preSaleNum");
        List<MoocFilmT> moocFilmTS = moocFilmTMapper.selectPage(page, entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilmTS);
        return filmInfos;
    }

    @Override
    public List<FilmInfo> getTop() {
        //条件 -> 正在上映,评分前10名
        EntityWrapper<MoocFilmT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        Page<MoocFilmT> page = new Page<>(1, 10, "film_score");
        List<MoocFilmT> moocFilmTS = moocFilmTMapper.selectPage(page, entityWrapper);
        List<FilmInfo> filmInfos = getFilmInfos(moocFilmTS);
        return filmInfos;
    }

    @Override
    public List<CatVO> getCats() {
        List<CatVO> cats = new ArrayList<>();
        //查询实体对象MoocCatDictT
        List<MoocCatDictT> moocCats = moocCatDictTMapper.selectList(null);
        for (MoocCatDictT moocCat : moocCats) {
            CatVO catVO = new CatVO();
            catVO.setCatId(moocCat.getUuid() + "");
            catVO.setCatName(moocCat.getShowName());
            cats.add(catVO);
        }
        //将实体对象转换成业务对象CatVO
        return cats;
    }

    @Override
    public List<SourceVO> getSources() {
        List<SourceVO> sources = new ArrayList<>();
        //查询实体对象,MoocSourceDictT
        List<MoocSourceDictT> moocSourceDictTS = moocSourceDictTMapper.selectList(null);
        //转换成业务对象
        for (MoocSourceDictT moocSourceDictT : moocSourceDictTS) {
            SourceVO sourceVO = new SourceVO();
            sourceVO.setSourceId(moocSourceDictT.getUuid() + "");
            sourceVO.setSourceName(moocSourceDictT.getShowName());
            sources.add(sourceVO);
        }
        return sources;
    }

    @Override
    public List<YearVO> getYears() {
        List<YearVO> years = new ArrayList<>();
        //查询实体对象,-MoocYearDictT
        List<MoocYearDictT> moocYearDictTS = moocYearDictTMapper.selectList(null);
        //转换成业务对象-YearVO
        for (MoocYearDictT yearDictT : moocYearDictTS) {
            YearVO yearVO = new YearVO();
            yearVO.setYearId(yearDictT.getUuid() + "");
            yearVO.setYearName(yearDictT.getShowName());
            years.add(yearVO);
        }
        return years;
    }

    @Override
    public FilmDetailVO getFilmDetail(int searchType, String searchParam) {
        FilmDetailVO filmDetailVO;
        //判断searchType 1-按名称,2-按ID
        if(searchType == 1){
            filmDetailVO = moocFilmTMapper.getFilmDetailByName("%" + searchParam + "%");
        }else {
            filmDetailVO = moocFilmTMapper.getFilmDetailById(searchParam);
        }
        return filmDetailVO;
    }

    private MoocFilmInfoT getFilmInfo(String filmId){
        MoocFilmInfoT moocFilmInfoT = new MoocFilmInfoT();
        moocFilmInfoT.setFilmId(filmId);

        moocFilmInfoT = moocFilmInfoTMapper.selectOne(moocFilmInfoT);
        return moocFilmInfoT;
    }

    @Override
    public FilmDescVO getFilmDesc(String filmId) {
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);
        FilmDescVO filmDescVO = new FilmDescVO();
        filmDescVO.setBiography(filmInfo.getBiography());
        filmDescVO.setFilmId(filmId);
        return filmDescVO;
    }

    @Override
    public ImgVO getImgs(String filmId) {
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);
        String filmImgStr = filmInfo.getFilmImgs();
        String[] filmImgs = filmImgStr.split(",");
        ImgVO imgVO = new ImgVO();
        imgVO.setMainImg(filmImgs[0]);
        imgVO.setImg01(filmImgs[1]);
        imgVO.setImg02(filmImgs[2]);
        imgVO.setImg03(filmImgs[3]);
        imgVO.setImg04(filmImgs[4]);

        return imgVO;
    }

    @Override
    public ActorVO getDectInfo(String filmId) {
        MoocFilmInfoT filmInfo = getFilmInfo(filmId);
        //获取导演编号
        Integer directorId = filmInfo.getDirectorId();
        MoocActorT moocActorT = moocActorTMapper.selectById(directorId);

        ActorVO actorVO = new ActorVO();
        actorVO.setImgAddress(moocActorT.getActorImg());
        actorVO.setDirectorName(moocActorT.getActorName());
        return actorVO;
    }

    @Override
    public List<ActorVO> getActors(String fileId) {
        return moocActorTMapper.getActors(fileId);
    }
}
