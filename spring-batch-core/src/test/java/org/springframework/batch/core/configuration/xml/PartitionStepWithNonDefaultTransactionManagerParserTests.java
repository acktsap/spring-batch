/*
 * Copyright 2006-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.configuration.xml;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dave Syer
 * @author Mahmoud Ben Hassine
 *
 */
@SpringJUnitConfig
class PartitionStepWithNonDefaultTransactionManagerParserTests {

	@Autowired
	private Job job;

	@Autowired
	private JobRepository jobRepository;

	@Test
	void testDefaultHandlerStep() throws Exception {
		assertNotNull(job);
		JobExecution jobExecution = jobRepository.createJobExecution(job.getName(), new JobParameters());
		job.execute(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

}
