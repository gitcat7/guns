package com.stylefeng.guns.rest.modular.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.api.alipay.AliPayServiceAPI;
import com.stylefeng.guns.api.alipay.vo.AliPayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.TokenBucket;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/order/")
public class OrderController {

    private static final String IMG_PRE = "http://www.yaokui.com";

    private static TokenBucket tokenBucket = new TokenBucket();

    @Reference(interfaceClass = OrderServiceAPI.class, check = false, group = "order2018")
    private OrderServiceAPI orderServiceAPI;

    @Reference(interfaceClass = OrderServiceAPI.class, check = false, group = "order2017")
    private OrderServiceAPI orderServiceAPI2017;

    @Reference(interfaceClass = AliPayServiceAPI.class, check = false)
    private AliPayServiceAPI aliPayServiceAPI;

    private ResponseVO error(Integer fieldId,String soldSeats,String seatsName){
        return ResponseVO.serviceFail("抱歉,下单人数太多,请稍后再试");
    }

    //购票(限流)
    /*
    信号隔离
    线程池切换
    线程切换
     */
    @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name="execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value= "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
    @PostMapping("buyTickets")
    public ResponseVO buyTickets(Integer fieldId,String soldSeats,String seatsName){
        try {
            if(!tokenBucket.getToken()){
                return ResponseVO.serviceFail("购票人数过多，请稍后再试");
            }
            //验证售出的票是否为真
            boolean isTrue = orderServiceAPI.isTrueSeats(fieldId + "", soldSeats);

            //已经销售的座位里,有没有这些座位
            boolean isNotSold = orderServiceAPI.isNotSoldSeats(fieldId + "", soldSeats);

            //验证,上述两个内容有一个不为真,则不创订单信息
            if(isNotSold && isTrue){
                //创建订单,注意获取登录人
                String userId = CurrentUser.getCurrentUser();
                if(null == userId || userId.trim().length() <= 0){
                    return ResponseVO.serviceFail("用户未登录");
                }
                OrderVO orderVO = orderServiceAPI.saveOrderInfo(fieldId, soldSeats, seatsName, Integer.parseInt(userId));
                if(null == orderVO){
                    log.error("购票业务异常");
                    return ResponseVO.serviceFail("购票业务异常");
                }
                return ResponseVO.success(orderVO);
            }
            return ResponseVO.serviceFail("订单中座位编号有问题");
        } catch (NumberFormatException e) {
            log.error("购票业务异常", e);
            return ResponseVO.serviceFail("购票业务异常");
        }
    }

    //获取订单
    @GetMapping("getOrderInfo")
    public ResponseVO getOrderInfo(@RequestParam(value = "nowPage", required = false, defaultValue = "1") Integer nowPage,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize){

        //获取当前登录人的信息
        String userId = CurrentUser.getCurrentUser();
        if(null == userId || userId.trim().length() <= 0){
            return ResponseVO.serviceFail("用户未登录");
        }
        //使用当前登录人获取已经购买的订单
        Page<OrderVO> page = new Page<>(nowPage, pageSize);

        Page<OrderVO> result = orderServiceAPI.getOrderByUserId(Integer.parseInt(userId), page);

        Page<OrderVO> result2017 = orderServiceAPI2017.getOrderByUserId(Integer.parseInt(userId), page);

        //合并结果
        int totalPages = (int) (result.getPages() + result2017.getPages());

        //订单总数合并
        ArrayList<OrderVO> orderVOList = new ArrayList<>();
        orderVOList.addAll(result.getRecords());
        orderVOList.addAll(result2017.getRecords());

        return ResponseVO.success(nowPage, (int)result.getPages(), "", orderVOList);
    }

    @PostMapping("getPayInfo")
    public ResponseVO getPayInfo(@RequestParam("orderId") String orderId){
        //订单二维码返回结果
        AliPayInfoVO aliPayInfoVO = aliPayServiceAPI.getQRCode(orderId);
        return ResponseVO.success(IMG_PRE, aliPayInfoVO);

    }

    @PostMapping("getPayResult")
    public ResponseVO getPayResult(@RequestParam("orderId") String orderId,
                                   @RequestParam(value = "tryNums", required = false, defaultValue = "1") Integer tryNums){
        //判断是否支付超时
        if(tryNums >= 4){
            return ResponseVO.serviceFail("订单支付失败, 请稍后重试");
        }
        AliPayResultVO aliPayResultVO = aliPayServiceAPI.getOrderStatus(orderId);
        if(null == aliPayResultVO || ToolUtil.isEmpty(aliPayResultVO.getOrderId())){
            AliPayResultVO resultVO = new AliPayResultVO();
            resultVO.setOrderId(orderId);
            resultVO.setOrderStatus(0);
            resultVO.setOrderMsg("支付失败");
            return ResponseVO.success(resultVO);
        }
        return ResponseVO.success(aliPayResultVO);

    }
}
