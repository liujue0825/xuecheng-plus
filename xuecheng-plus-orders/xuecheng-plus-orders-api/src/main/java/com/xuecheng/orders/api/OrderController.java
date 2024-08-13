package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 订单支付接口
 *
 * @author liujue
 */
@RestController
public class OrderController {

    @Value("${pay.alipay.APP_ID}")
    private String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    private String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    private String ALIPAY_PUBLIC_KEY;

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
            return null;
        }
        return orderService.createOrder(user.getId(), addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse response) throws AlipayApiException, IOException {
        XcPayRecord payRecord = orderService.getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("请重新点击支付获取二维码");
            return;
        }
        String status = payRecord.getStatus();
        if ("601002".equals(status)) {
            XueChengPlusException.cast("订单已支付，请勿重复支付");
        }
        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        // 获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
        // alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://aixm99.natappfree.cc/orders/paynotify");// 在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +
                "    \"total_amount\":" + payRecord.getTotalPrice() + "," +
                "    \"subject\":\"" + payRecord.getOrderName() + "\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");// 填充业务参数
        String form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
        response.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        response.getWriter().write(form);// 直接将完整的表单html输出到页面
        response.getWriter().flush();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    public PayRecordDto payresult(String payNo) {
        return orderService.queryPayResult(payNo);
    }

}