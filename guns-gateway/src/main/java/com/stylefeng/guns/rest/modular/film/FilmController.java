package com.stylefeng.guns.rest.modular.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/film/")
public class FilmController {

    private static final String IMG_PRE = "www.yaokui.com";

    @Reference(interfaceClass = FilmServiceApi.class, check = false)
    private FilmServiceApi filmServiceApi;

    @Reference(interfaceClass = FilmAsyncServiceApi.class, async = true, check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;

    //获取首页信息
    /*
    API网关:
        1.接口功能聚合[API聚合]
        好处:
            1.六个接口,一次请求,同一时刻节省了五次HTTP请求
            2.同一个接口对外暴露,降低前后端分离开发的难度和复杂度
        坏处:
            1.一次获取数据过多,容易出现问题
     */
    @GetMapping("getIndex")
    public ResponseVO getIndex(){
        FilmIndexVO filmIndexVO = new FilmIndexVO();
        //获取banner信息
        filmIndexVO.setBanners(filmServiceApi.getBanners());
        //获取正在热映的电影
        filmIndexVO.setHotFilms(filmServiceApi.getHotFilms(true, 8, 1, 99, 99, 99, 99));
        //获取即将上映的电影
        filmIndexVO.setSoonFilms(filmServiceApi.getSoonFilms(true, 8, 1, 99, 99, 99, 99));
        //票房排行榜
        filmIndexVO.setBoxRanking(filmServiceApi.getBoxRanking());
        //获取受欢迎的榜单
        filmIndexVO.setExpectRanking(filmServiceApi.getExpectRanking());
        //获取前一百
        filmIndexVO.setTop(filmServiceApi.getTop());

        return ResponseVO.success(IMG_PRE, filmIndexVO);
    }

    /*
    影片条件列表查询
     */
    @GetMapping("getConditionList")
    public ResponseVO getConditionList(@RequestParam(value = "catId", defaultValue = "99", required = false) String catId,
                                       @RequestParam(value = "sourceId", defaultValue = "99", required = false) String sourceId,
                                       @RequestParam(value = "yearId", defaultValue = "99", required = false) String yearId
                                    ){
        //标识符
        boolean flag = false;
        FilmConditionVO filmConditionVO = new FilmConditionVO();
        //类型集合
        List<CatVO> cats = filmServiceApi.getCats();
        List<CatVO> catResult = filmServiceApi.getCats();
        CatVO catVO = null;
        for (CatVO cat : cats) {
            //判断集合是否存在catId,如果存在,则将对应的实体变成active状态
            if(cat.getCatId().equals("99")){
                catVO = cat;
                continue;
            }
            if(cat.getCatId().equals(catId)){
                flag = true;
                cat.setActive(true);
            }else{
                cat.setActive(false);
            }
            catResult.add(cat);
        }
        //如果不存在,则默认将全部变成active状态
        if(!flag){
            catVO.setActive(true);
        }else{
            catVO.setActive(false);
        }
        catResult.add(catVO);

        //片源集合
        flag = false;
        List<SourceVO> sources = filmServiceApi.getSources();
        List<SourceVO> sourcesResult = new ArrayList<>();
        SourceVO sourceVO = null;
        for (SourceVO source : sources) {
            if(source.getSourceId().equals("99")){
                sourceVO = source;
                continue;
            }
            if(source.getSourceId().equals(sourceId)){
                flag = true;
                source.setActive(true);
            }else {
                source.setActive(false);
            }
            sourcesResult.add(source);
        }
        if(!flag){
            sourceVO.setActive(true);
        }else {
            sourceVO.setActive(false);
        }
        sourcesResult.add(sourceVO);

        //年代集合
        flag = false;
        List<YearVO> years = filmServiceApi.getYears();
        ArrayList<YearVO> yearResult = new ArrayList<>();
        YearVO yearVO = null;
        for (YearVO year : years) {
            if(year.getYearId().equals("99")){
                yearVO = year;
                continue;
            }
            if(year.getYearId().equals(yearId)){
                flag = true;
                year.setActive(true);
            }else {
                year.setActive(false);
            }
            yearResult.add(year);
        }
        if(!flag){
            yearVO.setActive(true);
        }else {
            yearVO.setActive(false);
        }
        yearResult.add(yearVO);

        filmConditionVO.setCatInfo(catResult);
        filmConditionVO.setSourceInfo(sourcesResult);
        filmConditionVO.setYearInfo(yearResult);
        return ResponseVO.success(filmConditionVO);
    }

    @GetMapping("getFilms")
    public ResponseVO getFilms(FilmRequestVO filmRequestVO){
        String img_pre = "www.yaokui.com";
        FilmVO filmVo;
        //根据shotType判断影片查询类型
        switch (filmRequestVO.getShowType()){
            case 1:
                filmVo = filmServiceApi.getHotFilms(
                        false,
                        filmRequestVO.getPageSize(),
                        filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),
                        filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()
                );
                break;
            case 2:
                filmVo = filmServiceApi.getSoonFilms(
                        false,
                        filmRequestVO.getPageSize(),
                        filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),
                        filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()
                );
                break;
            case 3:
                filmVo = filmServiceApi.getClassicFilms(
                        filmRequestVO.getPageSize(),
                        filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),
                        filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()
                );
                break;
            default:
                filmVo = filmServiceApi.getHotFilms(
                        false,
                        filmRequestVO.getPageSize(),
                        filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),
                        filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()
                );
                break;
        }
        //根据sortId排序
        //添加各种条件查询
        //判断当前是第几页
        return ResponseVO.success(filmVo.getNowPage(),
                                    filmVo.getTotalPage(),
                                    img_pre,
                                    filmVo.getFilmInfo());
    }

    @GetMapping("films/{searchParam}")
    public ResponseVO films(@PathVariable("searchParam")String searchParam, int searchType) throws ExecutionException, InterruptedException {
        //根据searchType判断查询类型
        FilmDetailVO filmDetail = filmServiceApi.getFilmDetail(searchType, searchParam);
        if(null == filmDetail){
            return ResponseVO.serviceFail("没有可查询的影片");
        }else if(null == filmDetail.getFilmId() || filmDetail.getFilmId().trim().length() == 0){
            return ResponseVO.serviceFail("没有可查询的影片");
        }
        //不同的查询类型,传入的条件会略有不同
        String filmId = filmDetail.getFilmId();
        //查询影片的详细信息 -> dubbo的异步调用
        //获取影片描述信息
//        FilmDescVO filmDescVO = filmAsyncServiceApi.getFilmDesc(filmId);
        filmAsyncServiceApi.getFilmDesc(filmId);
        Future<FilmDescVO> descVOFuture = RpcContext.getContext().getFuture();
        //获取图片信息
        filmAsyncServiceApi.getImgs(filmId);
        Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();
        //获取导演信息
        filmAsyncServiceApi.getDectInfo(filmId);
        Future<ActorVO> actorVOFuture = RpcContext.getContext().getFuture();
        //获取演员信息
        filmAsyncServiceApi.getActors(filmId);
        Future<ActorVO> actorsVOFuture = RpcContext.getContext().getFuture();

        InfoRequestVO infoRequestVO = new InfoRequestVO();

        //组织Actor属性
        ActorRequestVO actorRequestVO = new ActorRequestVO();
        actorRequestVO.setActors((List<ActorVO>) actorsVOFuture.get());
        actorRequestVO.setDirector(actorVOFuture.get());

        //组织Info对象
        infoRequestVO.setActors(actorRequestVO);
        infoRequestVO.setBiography(descVOFuture.get().getBiography());
        infoRequestVO.setFilmId(filmId);
        infoRequestVO.setImgVO(imgVOFuture.get());

        //组织成返回值
        filmDetail.setInfo04(infoRequestVO);
        return ResponseVO.success("www.yaokui.com", filmDetail);
    }
}
