package br.com.jrweb.conversionhub;

import org.springframework.boot.SpringApplication;

public class TestConversionHubApplication {

	public static void main(String[] args) {
		SpringApplication.from(ConversionHubApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
