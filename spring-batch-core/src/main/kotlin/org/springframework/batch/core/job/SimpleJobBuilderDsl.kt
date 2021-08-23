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

import org.springframework.batch.core.BatchDslMarker
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.builder.SimpleJobBuilder
import org.springframework.batch.core.step.StepBuilderDsl
import org.springframework.beans.factory.BeanFactory

/**
 * @author Taeik Lim
 *
 */
@BatchDslMarker
class SimpleJobBuilderDsl internal constructor(
    private val beanFactory: BeanFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val jobBuilder: JobBuilder,
) {
    private lateinit var simpleJobBuilder: SimpleJobBuilder

    fun step(name: String) {
        val step = beanFactory.getBean(name, Step::class.java)
        step(step)
    }

    fun step(name: String, init: StepBuilderDsl.() -> Step) {
        val stepBuilder = stepBuilderFactory.get(name)
        val step = StepBuilderDsl(beanFactory, stepBuilder).let(init)
        step(step)
    }

    fun step(step: Step) {
        if (::simpleJobBuilder.isInitialized) {
            simpleJobBuilder.next(step)
        } else {
            simpleJobBuilder = jobBuilder.start(step)
        }
    }

    internal fun build(): Job = simpleJobBuilder.build()
}
