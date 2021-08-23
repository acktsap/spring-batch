/*
 * Copyright 2021-2021 the original author or authors.
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

package org.springframework.batch.core.step

import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.BatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import javax.sql.DataSource

/**
 * @author Taeik Lim
 *
 */
internal class StepBuilderDslTests {

    @Test
    fun testTaskletWithBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("foo") {
                steps {
                    step("beanNameStep") {
                        tasklet("definedTasklet")
                    }
                    step("beanNameStepWithInit") {
                        tasklet("definedTasklet") {}
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(2, jobExecution.stepExecutions.size)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "beanNameStep" }.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "beanNameStepWithInit" }.status)
    }

    @Test
    fun testTaskletWithDirectDefinition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("foo") {
                steps {
                    step("directStep") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                    step("directStepWithInit") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }) {}
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(2, jobExecution.stepExecutions.size)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "directStep" }.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "directStepWithInit" }.status)
    }

    @Configuration
    @EnableBatchProcessing
    @EnableBatchDsl
    private open class TestConfiguration {

        @Bean
        fun dataSource(): DataSource {
            return EmbeddedDatabaseBuilder()
                .addScript("/org/springframework/batch/core/schema-drop-hsqldb.sql")
                .addScript("/org/springframework/batch/core/schema-hsqldb.sql")
                .generateUniqueName(true)
                .build()
        }

        @Bean
        fun definedTasklet(): Tasklet = TestTasklet()
    }

    private class TestTasklet : Tasklet {
        override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
            return RepeatStatus.FINISHED
        }
    }
}
