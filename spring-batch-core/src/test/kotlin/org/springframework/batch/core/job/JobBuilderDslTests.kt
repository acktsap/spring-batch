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

import org.junit.Assert.*
import org.junit.Test
import org.springframework.batch.core.*
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.configuration.BatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
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
internal class JobBuilderDslTests {

    @Test
    fun testValidator() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var validatorCallCount = 0
        val validator = JobParametersValidator {
            ++validatorCallCount
        }

        // when
        val job = batch {
            job("foo") {
                validator(validator)
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertTrue(1 <= validatorCallCount)
    }

    @Test
    fun testIncrementer() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val jobExplorer = context.getBean(JobExplorer::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var incrementerCallCount = 0
        val incrementer = JobParametersIncrementer {
            ++incrementerCallCount
            it
        }

        // when
        val job = batch {
            job("foo") {
                incrementer(incrementer)
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }
        val jobParameters = JobParametersBuilder(jobExplorer)
            .getNextJobParameters(job)
            .toJobParameters()
        val jobExecution = jobLauncher.run(job, jobParameters)

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(1, incrementerCallCount)
    }

    @Test
    fun testRepository() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val jobRepository = context.getBean(JobRepository::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var repositoryCallCount = 0

        // when
        val job = batch {
            job("foo") {
                repository(
                    object : JobRepository by jobRepository {
                        override fun update(jobExecution: JobExecution) {
                            ++repositoryCallCount
                            jobRepository.update(jobExecution)
                        }
                    }
                )
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertTrue(1 <= repositoryCallCount)
    }

    @Test
    fun testObjectListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var beforeJobCallCount = 0
        var afterJobCallCount = 0

        class TestListener {
            @BeforeJob
            fun beforeJob() {
                ++beforeJobCallCount
            }

            @AfterJob
            fun afterJob() {
                ++afterJobCallCount
            }
        }

        // when
        val job = batch {
            job("foo") {
                listener(TestListener())
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(1, beforeJobCallCount)
        assertEquals(1, afterJobCallCount)
    }

    @Test
    fun testJobExecutionListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var beforeJobCallCount = 0
        var afterJobCallCount = 0

        // when
        val job = batch {
            job("foo") {
                listener(
                    object : JobExecutionListener {
                        override fun beforeJob(jobExecution: JobExecution) {
                            ++beforeJobCallCount
                        }

                        override fun afterJob(jobExecution: JobExecution) {
                            ++afterJobCallCount
                        }
                    }
                )
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(1, beforeJobCallCount)
        assertEquals(1, afterJobCallCount)
    }

    @Test
    fun testPreventRestart() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)
        var tryCount = 0

        // when
        val job = batch {
            job("foo") {
                preventRestart()
                steps {
                    step("bar") {
                        tasklet { _, _ ->
                            if (tryCount == 0) {
                                ++tryCount
                                throw RuntimeException()
                            }
                            RepeatStatus.FINISHED
                        }
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertEquals(BatchStatus.FAILED, jobExecution.status)
        try {
            jobLauncher.run(job, JobParameters())
            fail()
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("JobInstance already exists and is not restartable"))
        }
        assertEquals(1, tryCount)
    }

    @Test
    fun testSteps() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("foo") {
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "bar" }.status)
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
    }
}
