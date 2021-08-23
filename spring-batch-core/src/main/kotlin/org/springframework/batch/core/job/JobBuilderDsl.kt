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

import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.BeanFactory

/**
 * @author Taeik Lim
 *
 */
@BatchDslMarker
class JobBuilderDsl internal constructor(
    private val beanFactory: BeanFactory,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    name: String
) {
    private val jobBuilder = jobBuilderFactory.get(name)

    fun validator(jobParametersValidator: JobParametersValidator) {
        jobBuilder.validator(jobParametersValidator)
    }

    fun incrementer(jobParametersIncrementer: JobParametersIncrementer) {
        jobBuilder.incrementer(jobParametersIncrementer)
    }

    fun repository(jobRepository: JobRepository) {
        jobBuilder.repository(jobRepository)
    }

    fun listener(listener: Any) {
        jobBuilder.listener(listener)
    }

    fun listener(listener: JobExecutionListener) {
        jobBuilder.listener(listener)
    }

    fun preventRestart() {
        jobBuilder.preventRestart()
    }

    fun steps(init: SimpleJobBuilderDsl.() -> Unit): Job = SimpleJobBuilderDsl(beanFactory, stepBuilderFactory, jobBuilder).apply(init).build()
}
