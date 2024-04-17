package com.myfund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyFundApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyFundApplication.class, args);
		String buildVersion = System.getProperty("build.number");
		System.out.println("Wersja projektu: " + buildVersion);
	}

}
