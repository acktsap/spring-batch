package org.springframework.batch.core.configuration

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchDsl
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import javax.sql.DataSource

internal class BatchDslTests {

    @Test
    fun testJobWithDefinedName() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val job = batch {
            job("definedJob") {
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
    }

    @Test
    fun testJobWithDefinition() {
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
    }

    @Test
    fun testStepWithDefinedName() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val step = batch {
            step("definedStep")
        }
        val job = batch {
            job("foo") {
                steps {
                    step(step)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "definedStep" }.status)
    }

    @Test
    fun testStepWithDefinition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean(JobLauncher::class.java)
        val batch = context.getBean(BatchDsl::class.java)

        // when
        val step = batch {
            step("bar") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
        val job = batch {
            job("foo") {
                steps {
                    step(step)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.status)
        assertEquals(BatchStatus.COMPLETED, jobExecution.stepExecutions.first { it.stepName == "bar" }.status)
    }

    private var jobRepository: JobRepository? = null

    @Configuration
    @EnableBatchProcessing
    @EnableBatchDsl
    private open class TestConfiguration {

        @Bean
        fun definedJob(batch: BatchDsl): Job = batch {
            job("definedJob") {
                steps {
                    step("bar") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }
        }

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
