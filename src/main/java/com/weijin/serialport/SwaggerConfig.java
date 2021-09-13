package com.weijin.serialport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Devil
 * @create 2018-09-26 19:16
 */
@EnableSwagger2
@ComponentScan(basePackages = { "com.weijin.serialport" })
@Configuration
public class SwaggerConfig extends WebMvcConfigurationSupport {

	/**
	 * 设置一个开关，生产版本为false，关闭swagger
	 */
	@Value(value = "{swagger.enabled}")
	private String ebable;
    

	
	private final static String title = "weijin SerialPorts 接口文档";
	private final static String description = "weijin SerialPorts 接口文档";
	private final static String version = "1.0";
	private final static String srcpackage = "com.weijin.serialport";

	@Bean
	public Docket createRestApi() {
		// 可以设置为扫描多个包
		Predicate<RequestHandler> package1 = RequestHandlerSelectors.basePackage(srcpackage);
		Predicate<RequestHandler> package2 = RequestHandlerSelectors.basePackage(srcpackage);
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).enable(true).select()
				.apis(Predicates.or(package1, package2)).paths(PathSelectors.any()).build();
	}

	@SuppressWarnings("deprecation")
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(title).description(description).version(version).build();
	}

	/**
	 * 一定要写这个方法，不然访问swagger-ui.html页面会404
	 * 
	 * @param registry
	 */
	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").setCachePeriod(0);
	}
}