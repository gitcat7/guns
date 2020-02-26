package com.stylefeng.guns.rest.modular.vo;

import lombok.Data;

@Data
public class ResponseVO<M> {

    //返回状态[0-成功,1-业务失败,999-表示系统异常]
    private int status;
    private String msg;
    private M data;
    //图片前缀
    private String imgPre;
    private Integer nowPage;
    private Integer totalPage;

    private ResponseVO(){}

    public static<M> ResponseVO<M> success(int nowPage, int totalPage, String imgPre, M m){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        responseVO.setNowPage(nowPage);
        responseVO.setTotalPage(totalPage);
        return responseVO;
    }

    public static<M> ResponseVO<M> success(String imgPre, M m){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(0);
        responseVO.setData(m);
        responseVO.setImgPre(imgPre);
        return responseVO;
    }

    public static<M> ResponseVO<M> success(M m){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(0);
        responseVO.setData(m);
        return responseVO;
    }

    public static<M> ResponseVO<M> success(String msg){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(0);
        responseVO.setMsg(msg);
        return responseVO;
    }

    public static<M> ResponseVO<M> serviceFail(String msg){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(1);
        responseVO.setMsg(msg);
        return responseVO;
    }

    public static<M> ResponseVO<M> appFail(String msg){
        ResponseVO responseVO = new ResponseVO<>();
        responseVO.setStatus(999);
        responseVO.setMsg(msg);
        return responseVO;
    }

}
