/*
 * Copyright 2010-2023 the original author or authors.
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
package org.springframework.batch.core.test.step;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for chunk oriented step
 * {@link org.springframework.batch.core.step.item.ChunkOrientedTasklet}.
 */
@SpringJUnitConfig(locations = "/simple-job-launcher-context.xml")
class ChunkOrientedStepIntegrationTests {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void testReaderWriterChunkStep() throws Exception {
		// given
		List<Integer> items = createItems(11);
		ListItemReader<Integer> itemReader = new ListItemReader<>(items);
		ListItemWriter<Integer> itemWriter = new ListItemWriter<>();
		Step step = new StepBuilder("step", jobRepository).<Integer, Integer>chunk(3, transactionManager)
			.reader(itemReader)
			.writer(itemWriter)
			.build();

		// when
		StepExecution stepExecution = execute(step);

		// then
		System.out.println(stepExecution);
		assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
		assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
		assertEquals(itemWriter.getWrittenItems(), items);
		assertEquals(items.size(), stepExecution.getReadCount());
		assertEquals(items.size(), stepExecution.getWriteCount());
		assertEquals(0, stepExecution.getReadSkipCount());
		assertEquals(0, stepExecution.getProcessSkipCount());
		assertEquals(0, stepExecution.getWriteSkipCount());
		assertEquals(0, stepExecution.getRollbackCount());
		assertEquals(4, stepExecution.getCommitCount());
	}

	private StepExecution execute(Step step) throws Exception {
		JobExecution jobExecution = jobRepository.createJobExecution("job" + Math.random(), new JobParameters());
		StepExecution stepExecution = jobExecution.createStepExecution("step");
		jobRepository.add(stepExecution);
		step.execute(stepExecution);
		return stepExecution;
	}

	private List<Integer> createItems(int count) {
		return IntStream.rangeClosed(1, count).boxed().toList();
	}

}
