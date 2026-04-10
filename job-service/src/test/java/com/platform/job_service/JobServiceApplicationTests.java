package com.platform.job_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"AWS_ACCESS_KEY_ID=dummy-access-key",
		"AWS_SECRET_ACCESS_KEY=dummy-secret-key"
})
class JobServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
