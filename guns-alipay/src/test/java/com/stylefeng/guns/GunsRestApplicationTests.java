package com.stylefeng.guns;

import com.stylefeng.guns.rest.AliPayApplication;
import com.stylefeng.guns.rest.AliPayApplication;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AliPayApplication.class)
public class GunsRestApplicationTests {

	@Autowired
	private FTPUtil ftpUtil;

	@Test
	public void contextLoads() {


		String fileStrByAddress = ftpUtil.getFileStrByAddress("seats/123214.json");

		File file = new File("C:\\Users\\yao19\\Desktop\\qrcode\\qr-ffec65c7794c45498c4d0febf48e9e8c.png");
		boolean b = ftpUtil.uploadFile("qr-ffec65c7794c45498c4d0febf48e9e8c.png", file);
		System.out.println("上传是否成功 = "+b);
		System.out.println(fileStrByAddress);

	}

}
