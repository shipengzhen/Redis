package com.bdqn.spz.redis.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件
 * @author sunyanxia
 *
 */
public class ConfigReadUtil {

	private static Properties p = new Properties();
	
	private static final String MARKET_CONFIG_PATH = "/sys.properties";
	
	private ConfigReadUtil(){
		
	}
	
	public static Properties getInstance(){
		InputStream is = ConfigReadUtil.class.getResourceAsStream(MARKET_CONFIG_PATH);
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}
}
