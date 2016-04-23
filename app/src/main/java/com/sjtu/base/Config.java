package com.sjtu.base;

import com.sjtu.api.ImpBaseAPI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	Properties prop = new Properties();

	public boolean init(InputStream in) {
		try {
			prop.load(in);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 用于区分API环境
	 *
	 * @return
     */
	public int getApiType() {
		String value = prop.getProperty("api").trim();
		if (value != null) {
			if ("sandbox".equals(value)) {
				return ImpBaseAPI.TEST_ENVIRONMENT;
			} else if ("prerelease".equals(value)) {
				return ImpBaseAPI.OFFICIAL_ENVIRONMENT;
			}
		}
		return ImpBaseAPI.OFFICIAL_ENVIRONMENT;
	}

	public String getVenderID() {
		return prop.getProperty("vender").trim();
	}

}
