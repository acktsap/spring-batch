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

package org.springframework.batch.core.job

import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.BatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
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
internal class SimpleJobBuilderDslTests {

    @Test
    fun testStepWithDefinedName() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("testJob") {
                steps {
                    step("definedStep")
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "definedStep" }.status)
    }

    @Test
    fun testStepWithDirectDefinition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("testJob") {
                steps {
                    step("testStep") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "testStep" }.status)
    }

    @Test
    fun testStepByPassingStep() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        val step = batch {
            step("testStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                steps {
                    step(step)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "testStep" }.status)
    }

    @Configuration
    @EnableBatchProcessing
    @EnableBatchDsl
    private open class TestConfiguration {

        @Bean
        fun definedStep(batch: BatchDsl): Step = batch {
            step("definedStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }

        @Bean
        fun dataSource(): DataSource {
            return EmbeddedDatabaseBuilder()
                .addScript("/org/springframework/batch/core/schema-drop-hsqldb.sql")
                .addScript("/org/springframework/batch/core/schema-hsqldb.sql")
                .generateUniqueName(true)
                .build()
        }
    }
}
