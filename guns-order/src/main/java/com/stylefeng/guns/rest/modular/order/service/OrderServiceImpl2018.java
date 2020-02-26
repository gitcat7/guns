package com.stylefeng.guns.rest.modular.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVO;
import com.stylefeng.guns.api.cinema.vo.OrderQueryVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.UUIDUtil;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrder2017TMapper;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrder2018TMapper;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrderTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrder2018T;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrderT;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Service(interfaceClass = OrderServiceAPI.class, group = "order2018")
public class OrderServiceImpl2018 implements OrderServiceAPI {

    @Autowired
    private MoocOrder2018TMapper moocOrder2018TMapper;
    @Reference(interfaceClass = CinemaServiceAPI.class, check = false)
    private CinemaServiceAPI cinemaServiceAPI;
    @Autowired
    private FTPUtil ftpUtil;

    @Override
    public boolean isTrueSeats(String fieldId, String seats) {
        //根据FieldId找到对应的座位位置图
        String seatPath = moocOrder2018TMapper.getSeatsByFieldId(fieldId);
        //获取位置图,判断seats是否为真
        String fileStrByAddress = ftpUtil.getFileStrByAddress(seatPath);

        //将fileStrByAddress转换为json对象
        JSONObject jsonObject = JSONObject.parseObject(fileStrByAddress);
        //seats=1,2,3
        String ids = jsonObject.get("ids").toString();

        String[] idArrs = ids.split(",");
        String[] seatArrs = seats.split(",");
        int isTrue = 0;
        //每次匹配上,都给isTrue加1
        for (String idArr : idArrs) {
            for (String seatArr : seatArrs) {
                if(seatArr.equalsIgnoreCase(idArr)){
                    isTrue ++;
                }
            }
        }
        return isTrue == seatArrs.length;
    }

    @Override
    public boolean isNotSoldSeats(String fieldId, String seats) {
        EntityWrapper<MoocOrder2018T> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("field_id", fieldId);
        List<MoocOrder2018T> moocOrderTS = moocOrder2018TMapper.selectList(entityWrapper);
        String[] seatArrs = seats.split(",");
        for (MoocOrder2018T moocOrderT : moocOrderTS) {
            String[] ids = moocOrderT.getSeatsIds().split(",");
            for (String id : ids) {
                for (String seatArr : seatArrs) {
                    if(id.equalsIgnoreCase(seatArr)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public OrderVO saveOrderInfo(Integer fieldId, String soldSeats, String seatsName, Integer userId) {
        //编号
        String uuid = UUIDUtil.getUuid();

        //影片信息
        FilmInfoVO filmInfoByFieldId = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
        int filmId = Integer.parseInt(filmInfoByFieldId.getFilmId());
        //获取影院信息
        OrderQueryVO orderQueryVO = cinemaServiceAPI.getOrderNeeds(fieldId);
        int cinemaId = Integer.parseInt(orderQueryVO.getCinemaId());
        double filmPrice = Double.parseDouble(orderQueryVO.getFilmPrice());

        //求订单总金额
        int solds = soldSeats.split(",").length;
        double totalPrice = getTotalPrice(solds, filmPrice);
        MoocOrder2018T moocOrderT = new MoocOrder2018T();
        moocOrderT.setUuid(uuid);
        moocOrderT.setSeatsName(seatsName);
        moocOrderT.setSeatsIds(soldSeats);
        moocOrderT.setOrderUser(userId);
        moocOrderT.setOrderPrice(totalPrice);
        moocOrderT.setFilmPrice(filmPrice);
        moocOrderT.setFilmId(filmId);
        moocOrderT.setFieldId(fieldId);
        moocOrderT.setCinemaId(cinemaId);
        Integer insert = moocOrder2018TMapper.insert(moocOrderT);
        if(insert > 0){
            //返回查询结果
            OrderVO orderVO = moocOrder2018TMapper.getOrderInfoById(uuid);
            if(null == orderVO || null == orderVO.getOrderId()){
                log.error("订单信息查询失败,订单编号为{}", uuid);
                return null;
            }
            return orderVO;
        }
        //插入出错
        log.error("订单插入失败");
        return null;
    }

    private double getTotalPrice(int solds ,double filmPrice){
        BigDecimal soldDeci = new BigDecimal(solds);
        BigDecimal filmPriceDeci = new BigDecimal(filmPrice);
        BigDecimal result = soldDeci.multiply(filmPriceDeci);

        //四舍五入,取小数点后两位
        BigDecimal bigDecimal = result.setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    @Override
    public Page<OrderVO> getOrderByUserId(Integer userId, Page<OrderVO> page) {
        Page<OrderVO> result = new Page<>();
        if(null == userId){
            log.error("订单查询业务失败,用户编号为传入");
            return null;
        }else {
            List<OrderVO> orderInfoByUserId = moocOrder2018TMapper.getOrdersByUserId(userId, page);
            if(null == orderInfoByUserId && orderInfoByUserId.size() == 0){
                result.setTotal(0);
                result.setRecords(new ArrayList<>());
                return result;
            }
            //获取订单总数
            EntityWrapper<MoocOrder2018T> entityWrapper = new EntityWrapper<>();
            entityWrapper.eq("order_user", userId);
            Integer count = moocOrder2018TMapper.selectCount(entityWrapper);
            //将结果放入Page中
            result.setTotal(count);
            result.setRecords(orderInfoByUserId);
            return result;
        }
    }

    @Override
    public String getSoldSeatsByField(Integer fieldId) {
        if(null == fieldId){
            log.error("查询已售座位错误,未传入任何场次编号");
            return "";
        }
        return moocOrder2018TMapper.getSoldSeatsByField(fieldId);
    }

    @Override
    public OrderVO getOrderInfoById(String orderId) {
        return moocOrder2018TMapper.getOrderInfoById(orderId);
    }

    @Override
    public boolean paySuccess(String orderId) {
        MoocOrder2018T moocOrderT = new MoocOrder2018T();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(1);
        Integer integer = moocOrder2018TMapper.updateById(moocOrderT);
        return integer >= 1;
    }

    @Override
    public boolean payFail(String orderId) {
        MoocOrder2018T moocOrderT = new MoocOrder2018T();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(2);
        Integer integer = moocOrder2018TMapper.updateById(moocOrderT);
        return integer >= 1;
    }
}
