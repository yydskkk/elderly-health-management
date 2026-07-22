package com.elderly.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 老年人健康监测与预警平台 主启动类
 *
 * @author elderly-health
 */
@SpringBootApplication
@MapperScan("com.elderly.health.mapper")
public class HealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthApplication.class, args);
        System.out.println("====== 老年人健康监测与预警平台后端启动成功 ======");
    }
}
