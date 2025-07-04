/*
 * Copyright 2018-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.samples.chunking;

import java.util.Arrays;

import jakarta.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.samples.common.DataSourceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jms.dsl.Jms;

/**
 * This configuration class is for the manager side of the remote chunking sample. The
 * manager step reads numbers from 1 to 6 and sends 2 chunks {1, 2, 3} and {4, 5, 6} to
 * workers for processing and writing.
 *
 * @author Mahmoud Ben Hassine
 */
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@EnableIntegration
@PropertySource("classpath:org/springframework/batch/samples/chunking/remote-chunking.properties")
@Import(DataSourceConfiguration.class)
public class ManagerConfiguration {

	@Value("${broker.url}")
	private String brokerUrl;

	@Autowired
	private RemoteChunkingManagerStepBuilderFactory managerStepBuilderFactory;

	@Bean
	public ActiveMQConnectionFactory connectionFactory() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(this.brokerUrl);
		return connectionFactory;
	}

	/*
	 * Configure outbound flow (requests going to workers)
	 */
	@Bean
	public DirectChannel requests() {
		return new DirectChannel();
	}

	@Bean
	public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory) {
		return IntegrationFlow.from(requests())
			.handle(Jms.outboundAdapter(connectionFactory).destination("requests"))
			.get();
	}

	/*
	 * Configure inbound flow (replies coming from workers)
	 */
	@Bean
	public QueueChannel replies() {
		return new QueueChannel();
	}

	@Bean
	public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
		return IntegrationFlow.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies"))
			.channel(replies())
			.get();
	}

	/*
	 * Configure manager step components
	 */
	@Bean
	public ListItemReader<Integer> itemReader() {
		return new ListItemReader<>(Arrays.asList(1, 2, 3, 4, 5, 6));
	}

	@Bean
	public TaskletStep managerStep() {
		return this.managerStepBuilderFactory.get("managerStep")
			.chunk(3)
			.reader(itemReader())
			.outputChannel(requests())
			.inputChannel(replies())
			.build();
	}

	@Bean
	public Job remoteChunkingJob(JobRepository jobRepository) {
		return new JobBuilder("remoteChunkingJob", jobRepository).start(managerStep()).build();
	}

}
